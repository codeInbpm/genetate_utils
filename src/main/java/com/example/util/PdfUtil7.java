package com.example.util;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.pdf.parser.*;
import com.itextpdf.text.pdf.parser.Vector;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.function.Function;

/**
 * 支持：
 * 1. 多页模板填充 (List<Map<String,String>>)
 * 2. {{key}} 占位符精确定位+覆盖+写入
 * 3. 自动加载中文字体，IDENTITY_H 支持中文
 * 4. 状态勾选：基于 data 中的 "status" 字段（值："red"、"green" 或 "yellow"），自动检测模板中颜色标签("绿"、"黄"、"红")位置，在对应标签右侧添加 ✅
 *    - 无需调整模板：通过文本提取定位颜色标签，计算右侧坐标绘制勾选
 *    - 假设模板中颜色框内/旁有对应中文标签；若标签不同，调整 extractTextPositions 中的 Pattern
 */
public class PdfUtil7 {

    /**
     * 生成 PDF，按 dataList 每条生成一页
     */
    public static void generate(String templatePath, String outputPath,
                                List<Map<String, String>> dataList, String osType)
            throws Exception {

        // 加载模板 PDF & 字体
        PdfReader templateReader = new PdfReader(templatePath);
        BaseFont font = loadChineseFont(osType);

        // 抽取占位符坐标
        Map<String, List<Position>> placeholderPositions = extractTextPositions(templateReader, 1,
                Pattern.compile("\\{\\{[^{}]+}}", Pattern.MULTILINE),
                match -> match.substring(2, match.length() - 2).trim());

        // 抽取颜色标签位置（绿、黄、红）
        Map<String, List<Position>> colorPositions = extractTextPositions(templateReader, 1,
                Pattern.compile("(绿|黄|红)"),
                match -> match);

        // 调试输出：打印颜色位置（运行后查看控制台，确认是否提取到所有标签位置）
        System.out.println("=== 颜色标签位置提取结果 ===");
        for (Map.Entry<String, List<Position>> entry : colorPositions.entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue());
        }
        System.out.println("=========================");

        // 输出 PDF
        Document document = new Document(templateReader.getPageSizeWithRotation(1));
        PdfCopy copy = new PdfCopy(document, new FileOutputStream(outputPath));
        document.open();

        for (Map<String, String> data : dataList) {
            // 用 temp 处理模板
            ByteArrayOutputStream tempOut = new ByteArrayOutputStream();
            PdfReader tmpReader = new PdfReader(templatePath);
            PdfStamper stamper = new PdfStamper(tmpReader, tempOut);

            replacePlaceholdersOnPage(stamper, 1, data, placeholderPositions, font);

            // 处理状态勾选：在对应颜色标签右侧添加 ✅
            String status = data.get("status");
            if (status != null) {
                addStatusCheck(stamper, 1, status, colorPositions, font);
            }

            stamper.close();
            tmpReader.close();

            PdfReader newPageReader = new PdfReader(tempOut.toByteArray());
            PdfImportedPage importedPage = copy.getImportedPage(newPageReader, 1);
            copy.addPage(importedPage);
            newPageReader.close();
        }

        document.close();
        templateReader.close();
        System.out.println("PdfUtil7 生成完成: " + outputPath);
    }

    // ---------------------- 通用文本位置提取（占位符或颜色标签） ----------------------
    static Map<String, List<Position>> extractTextPositions(PdfReader reader, int pageNum, Pattern pattern,
                                                            Function<String, String> keyExtractor)
            throws IOException {
        Map<String, List<Position>> positions = new LinkedHashMap<>();
        PlaceholderExtractionStrategy strategy = new PlaceholderExtractionStrategy();
        String fullText = PdfTextExtractor.getTextFromPage(reader, pageNum, strategy);

        // 调试输出：打印提取的全文文本和块信息（可选，确认匹配）
        System.out.println("提取全文: " + fullText.substring(0, Math.min(200, fullText.length())) + "...");
        System.out.println("块数量: " + strategy.getChunks().size());

        Matcher m = pattern.matcher(fullText);
        List<ChunkInfo> chunks = strategy.getChunks();

        while (m.find()) {
            String match = m.group();
            String key = keyExtractor.apply(match);
            int startIdx = m.start();
            int endIdx = m.end();

            float minX = Float.MAX_VALUE;
            float maxX = Float.MIN_VALUE;
            float placeholderY = 0f;
            boolean found = false;
            int cumLen = 0;
            for (ChunkInfo ci : chunks) {
                int len = ci.text.length();
                int chunkStartIdx = cumLen;
                int chunkEndIdx = cumLen + len;
                int overlapStart = Math.max(startIdx, chunkStartIdx);
                int overlapEnd = Math.min(endIdx, chunkEndIdx);
                if (overlapStart < overlapEnd) {
                    found = true;
                    if (placeholderY == 0f) {
                        placeholderY = ci.start.get(Vector.I2);
                    }
                    int subOffset = overlapStart - chunkStartIdx;
                    float subStartX = ci.start.get(Vector.I1) + subOffset * ci.charWidthApprox;
                    float subWidth = (overlapEnd - overlapStart) * ci.charWidthApprox;
                    float subEndX = subStartX + subWidth;
                    minX = Math.min(minX, subStartX);
                    maxX = Math.max(maxX, subEndX);
                }
                cumLen += len;
            }
            if (found && minX < Float.MAX_VALUE) {
                float width = maxX - minX;
                positions.computeIfAbsent(key, k -> new ArrayList<>())
                        .add(new Position(minX, placeholderY, 12f, width));
            }
        }
        return positions;
    }

    // ---------------------- 添加状态勾选（在颜色标签右侧绘制 ✔） ----------------------
    static void addStatusCheck(PdfStamper stamper, int pageNum, String status,
                               Map<String, List<Position>> colorPositions, BaseFont bf)
            throws DocumentException {
        // status 到颜色标签的映射
        final Map<String, String> STATUS_TO_TEXT = Map.of(
                "green", "绿",
                "yellow", "黄",
                "red", "红"
        );

        String colorText = STATUS_TO_TEXT.get(status);
        if (colorText == null) return;

        List<Position> posList = colorPositions.get(colorText);
        if (posList == null || posList.isEmpty()) {
            System.out.println("警告: 未找到位置 for " + colorText);
            return;
        }

        // 按 Y 升序排序，取底部位置（状态栏小标签，如“绿”）
        posList.sort(Comparator.comparingDouble(p -> p.y));
        Position p = posList.get(0); // 底部位置
        float checkX = p.x + p.width + 50f; // 右侧 + 小间距（紧贴，像模板）
        float checkY = p.y + 2f; // 同基线 Y（水平右侧，不下移）
        float checkSize = 30f; // 适中大小，匹配模板勾

        PdfContentByte canvas = stamper.getOverContent(pageNum);

        // 增强：白底覆盖原可能勾位置（水平矩形，覆盖右侧原√）
        canvas.saveState();
        canvas.setColorFill(BaseColor.WHITE);
        canvas.rectangle(checkX - 1f, checkY - checkSize * 0.2f, checkSize * 0.6f, checkSize * 0.6f);
        canvas.fill();
        canvas.restoreState();

        // 绘制新勾
        canvas.saveState();
        canvas.setColorFill(BaseColor.BLACK);
        canvas.beginText();
        canvas.setFontAndSize(bf, checkSize);
        canvas.setTextMatrix(checkX, checkY);
        canvas.showText("✔");  // 保持 ✔，兼容好
        canvas.endText();
        canvas.restoreState();

        System.out.println("选中位置 for " + status + ": " + p + "，添加 ✔ at (" + checkX + ", " + checkY + ")");
    }


    // ---------------------- 字体加载（系统路径 + IDENTITY_H，支持中文无额外 JAR） ----------------------
    static BaseFont loadChineseFont(String osType) throws DocumentException, IOException {
        List<String> fallbackFonts = new ArrayList<>();
        // Mac: Hiragino (Simplified Chinese, index 0 for Regular) + Arial Unicode (fallback, .ttf)
        if ("mac".equalsIgnoreCase(osType)) {
            fallbackFonts.add("/System/Library/Fonts/Hiragino SansGB.ttc,0");
            fallbackFonts.add("/Library/Fonts/Arial Unicode.ttf");
        } else if ("win".equalsIgnoreCase(osType)) {
            fallbackFonts.add("C:/Windows/Fonts/simsun.ttc,0");
            fallbackFonts.add("C:/Windows/Fonts/msyh.ttc,0");
        } else {
            fallbackFonts.add("STSong-Light");
            fallbackFonts.add("Helvetica");
        }
        for (String fontPath : fallbackFonts) {
            try {
                return BaseFont.createFont(fontPath, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            } catch (Exception ignored) {}
        }
        throw new IOException("字体加载失败");
    }

    // ---------------------- 替换占位符（精确覆盖原文本区域 + 完整行高） ----------------------
    static void replacePlaceholdersOnPage(PdfStamper stamper, int pageNum,
                                          Map<String, String> data,
                                          Map<String, List<Position>> positions,
                                          BaseFont bf) throws DocumentException {
        PdfContentByte canvas = stamper.getOverContent(pageNum);
        for (Map.Entry<String, String> e : data.entrySet()) {
            String key = e.getKey();
            List<Position> posList = positions.get(key);
            if (posList == null) continue;
            for (Position p : posList) {
                float coverWidth = Math.max(p.width, getTextWidth(bf, p.fontSize, e.getValue()) * 1.2f);
                float lineHeight = p.fontSize * 1.5f;
                float coverY = p.y - p.fontSize * 0.3f;

                // 白底精确覆盖（不透明，防止原 {{key}} 透出）
                canvas.saveState();
                canvas.setColorFill(BaseColor.WHITE);
                canvas.rectangle(p.x, coverY, coverWidth, lineHeight);
                canvas.fill();
                canvas.restoreState();

                // 写入新文字（基线对齐，颜色黑色确保可见）
                // 对于空字符串，showText("") 不会绘制任何内容，仅覆盖背景
                canvas.saveState();
                canvas.setColorFill(BaseColor.BLACK);
                canvas.beginText();
                canvas.setFontAndSize(bf, p.fontSize);
                canvas.setTextMatrix(p.x, p.y);
                canvas.showText(e.getValue());
                canvas.endText();
                canvas.restoreState();
            }
        }
    }

    // 辅助：计算文本宽度
    private static float getTextWidth(BaseFont bf, float fontSize, String text) {
        return bf.getWidthPoint(text, fontSize);
    }

    static class ChunkInfo {
        String text;
        Vector start;
        float charWidthApprox;
        ChunkInfo(String t, Vector s, float cw) {
            text = t;
            start = s;
            charWidthApprox = cw;
        }
    }

    static class PlaceholderExtractionStrategy implements TextExtractionStrategy {
        private final List<ChunkInfo> chunks = new ArrayList<>();
        public List<ChunkInfo> getChunks() { return chunks; }
        @Override public void renderText(TextRenderInfo info) {
            String text = info.getText();
            if (text != null && !text.trim().isEmpty()) {
                Vector baselineStart = info.getBaseline().getStartPoint();
                float baselineEndX = info.getBaseline().getEndPoint().get(Vector.I1);
                float totalWidth = baselineEndX - baselineStart.get(Vector.I1);
                int len = text.length();
                if (len > 0) {
                    float charWidthApprox = totalWidth / len;
                    for (int i = 0; i < len; i++) {
                        String ch = text.substring(i, i + 1);
                        float chStartX = baselineStart.get(Vector.I1) + i * charWidthApprox;
                        Vector chStart = new Vector(chStartX, baselineStart.get(Vector.I2), baselineStart.get(Vector.I3));
                        chunks.add(new ChunkInfo(ch, chStart, charWidthApprox));
                    }
                }
            }
        }
        @Override public String getResultantText() {
            StringBuilder sb = new StringBuilder();
            for (ChunkInfo c : chunks) sb.append(c.text);
            return sb.toString();
        }
        @Override public void beginTextBlock() {}
        @Override public void endTextBlock() {}
        @Override public void renderImage(ImageRenderInfo imageRenderInfo) {}
    }

    // Position 类已优化 toString（日志更清晰）
    static class Position {
        float x, y, fontSize, width;
        Position(float x, float y, float fontSize, float width) {
            this.x = x; this.y = y; this.fontSize = fontSize; this.width = width;
        }

        @Override
        public String toString() {
            return String.format("Position{x=%.2f, y=%.2f, width=%.2f}", x, y, width);
        }
    }
}