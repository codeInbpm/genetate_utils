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

/**
 * PDF 工具类：基于模板生成多页 PDF，替换静态文本占位符 {{key}}
 * 最终版本（2025.11） - 修复与优化：
 * 1. 字体加载自动 fallback（优先系统 TTF/TTC 路径 + IDENTITY_H，无需 iTextAsian.jar）
 * 2. 文本提取策略修复（getResultantText 实现）
 * 3. Y坐标偏移优化（精确基线对齐 + 行高覆盖）
 * 4. 允许多页数据填充
 * 5. 稳定绘制图层 & 精确边界框计算（基于字符位置覆盖原占位符）
 * 6. 占位符匹配增强（忽略多余空格，支持跨 chunk 精确边界）
 * 7. 修复 TextRenderInfo.getEndPoint() 调用（使用 getBaseline().getEndPoint()）
 * 8. 修复 DocumentFont.getSize() 不存在（默认字体大小 12f）
 * 9. 字符识别式替换：计算占位符精确边界框，覆盖原文本区域，然后插入新文本
 * 10. 覆盖层优化：使用 overContent + 完整行高白底 + 调试模式（可选红框）
 * 11. 字体渲染修复：使用 IDENTITY_H + EMBEDDED，支持中文（Mac: PingFang/Arial Unicode；Win: SimSun）
 */
public class PdfUtil4 {

    /**
     * 根据模板生成多页 PDF
     * @param templatePath 模版路径
     * @param outputPath 输出路径
     * @param dataList 每页数据
     * @param osType 操作系统类型（"mac" / "win" / "other"）
     */
    public static void generateMultiPagePdf(String templatePath, String outputPath,
                                            List<Map<String, String>> dataList, String osType)
            throws IOException, DocumentException {

        // 1️⃣ 加载模板 PDF & 字体
        PdfReader templateReader = new PdfReader(templatePath);
        BaseFont baseFont = loadChineseFont(osType);

        // 2️⃣ 提取第一页占位符坐标（精确边界框）
        Map<String, List<Position>> placeholderPositions = extractPlaceholderPositions(templateReader, 1);

        // 3️⃣ 输出 PDF
        Document document = new Document(templateReader.getPageSizeWithRotation(1));
        PdfCopy copy = new PdfCopy(document, new FileOutputStream(outputPath));
        document.open();

        // 4️⃣ 多页填充循环
        for (int i = 0; i < dataList.size(); i++) {
            ByteArrayOutputStream tempOut = new ByteArrayOutputStream();
            PdfReader tempReader = new PdfReader(templatePath);
            PdfStamper tempStamper = new PdfStamper(tempReader, tempOut);

            Map<String, String> currentData = dataList.get(i);
            replacePlaceholdersOnPage(tempStamper, 1, currentData, placeholderPositions, baseFont);

            tempStamper.close();
            tempReader.close();

            PdfReader pageReader = new PdfReader(tempOut.toByteArray());
            PdfImportedPage importedPage = copy.getImportedPage(pageReader, 1);
            copy.addPage(importedPage);
            pageReader.close();
        }

        document.close();
        templateReader.close();

        System.out.println("✅ PDF 多页生成完成: " + outputPath);
    }

    // ---------------------- 字体加载（系统路径 + IDENTITY_H，支持中文无额外 JAR） ----------------------
    private static BaseFont loadChineseFont(String osType) throws DocumentException, IOException {
        List<String> fallbackFonts = new ArrayList<>();
        if ("mac".equalsIgnoreCase(osType)) {
            // Mac: PingFang (Simplified Chinese, index 0 for Regular) + Arial Unicode (fallback, .ttf)
            fallbackFonts.add("/System/Library/Fonts/Hiragino SansGB.ttc,0");
            fallbackFonts.add("/Library/Fonts/Arial Unicode.ttf");
            fallbackFonts.add("/System/Library/Fonts/Helvetica.ttf");  // 英文 fallback
        } else if ("win".equalsIgnoreCase(osType)) {
            // Win: SimSun (宋体, index 0) + YaHei
            fallbackFonts.add("C:/Windows/Fonts/simsun.ttc,0");
            fallbackFonts.add("C:/Windows/Fonts/msyh.ttc,0");
            fallbackFonts.add("C:/Windows/Fonts/arial.ttf");  // 英文 fallback
        } else {
            // 其他: 优先内置，fallback 标准
            fallbackFonts.add("STSong-Light");  // 如果有 Asian JAR
            fallbackFonts.add("Helvetica");
        }

        for (String fontPath : fallbackFonts) {
            try {
                System.out.println("🔤 尝试加载系统字体: " + fontPath);
                // IDENTITY_H + EMBEDDED: 支持 Unicode/中文，无需 Asian JAR
                return BaseFont.createFont(fontPath, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            } catch (Exception e) {
                System.err.println("⚠️ 字体加载失败 [" + fontPath + "]，尝试下一个: " + e.getMessage());
            }
        }
        throw new IOException("❌ 所有系统字体加载失败，无法支持显示。请检查路径或添加 iTextAsian.jar");
    }

    // ---------------------- 提取占位符（精确边界框） ----------------------
    private static Map<String, List<Position>> extractPlaceholderPositions(PdfReader reader, int pageNum)
            throws IOException {
        Map<String, List<Position>> positions = new LinkedHashMap<>();
        PlaceholderExtractionStrategy strategy = new PlaceholderExtractionStrategy();
        String fullText = PdfTextExtractor.getTextFromPage(reader, pageNum, strategy);

        System.out.println("📄 完整提取文本:\n" + fullText);

        // 增强匹配：忽略多余空格，匹配 {{key}}
        Pattern p = Pattern.compile("\\{\\{[^{}]+\\}\\}");
        Matcher m = p.matcher(fullText);
        List<ChunkInfo> chunks = strategy.getChunks();

        while (m.find()) {
            String match = m.group();
            String key = match.substring(2, match.length() - 2).trim();  // trim 空格

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
                        placeholderY = ci.start.get(Vector.I2);  // 取第一个重叠 chunk 的 y（假设同行）
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
                float startX = minX;
                float width = maxX - minX;
                Position pos = new Position(startX, placeholderY, 12f, width);
                positions.computeIfAbsent(key, k -> new ArrayList<>()).add(pos);
                System.out.printf("🪶 占位符 [%s] at startX=%.2f, y=%.2f, exactWidth=%.2f\n", key, startX, placeholderY, width);
            } else {
                System.out.println("⚠️ 无法精确定位占位符边界: " + key);
            }
        }

        if (positions.isEmpty()) {
            System.out.println("❌ 无任何占位符找到，请检查 PDF 模板文本层");
        }
        return positions;
    }

    // ---------------------- 替换占位符（精确覆盖原文本区域 + 完整行高） ----------------------
    private static void replacePlaceholdersOnPage(PdfStamper stamper, int pageNum,
                                                  Map<String, String> data,
                                                  Map<String, List<Position>> positions,
                                                  BaseFont bf) throws DocumentException {

        PdfContentByte canvas = stamper.getOverContent(pageNum);

        for (Map.Entry<String, String> e : data.entrySet()) {
            String key = e.getKey();
            String val = e.getValue();
            List<Position> posList = positions.get(key);
            if (posList == null || posList.isEmpty()) {
                System.out.println("❌ 未找到占位符: " + key);
                continue;
            }

            for (Position p : posList) {
                float startX = p.x;
                float y = p.y;  // 精确基线对齐
                float originalWidth = p.width;
                float newTextWidth = getTextWidth(bf, p.fontSize, val);
                float coverWidth = Math.max(originalWidth, newTextWidth * 1.2f);  // 增加缓冲 20%
                float lineHeight = p.fontSize * 1.5f;  // 完整行高覆盖（ascent + descent + leading）
                float coverY = y - p.fontSize * 0.3f;  // 从基线下 30% 开始，确保覆盖整个 glyph 高度

                // 白底精确覆盖（不透明，防止原 {{key}} 透出）
                canvas.saveState();
                canvas.setColorFill(BaseColor.WHITE);
                canvas.rectangle(startX, coverY, coverWidth, lineHeight);
                canvas.fill();
                canvas.restoreState();

                // 调试红框（可选，生产时注释掉以隐藏）
                // canvas.saveState();
                // canvas.setColorStroke(BaseColor.RED);
                // canvas.setLineWidth(1f);
                // canvas.rectangle(startX, coverY, coverWidth, lineHeight);
                // canvas.stroke();
                // canvas.restoreState();

                // 写入新文字（基线对齐，颜色黑色确保可见）
                canvas.saveState();
                canvas.setColorFill(BaseColor.BLACK);
                canvas.beginText();
                canvas.setFontAndSize(bf, p.fontSize);
                canvas.setTextMatrix(startX, y);
                canvas.showText(val);
                canvas.endText();
                canvas.restoreState();

                System.out.printf("✅ 替换 {{" + key + "}} → '%s' at (%.2f, %.2f), coverW=%.2f, lineH=%.2f\n", val, startX, y, coverWidth, lineHeight);
            }
        }
    }

    // 辅助：计算文本宽度
    private static float getTextWidth(BaseFont bf, float fontSize, String text) throws DocumentException {
        return bf.getWidthPoint(text, fontSize);
    }

    // ---------------------- 内部结构类 ----------------------
    static class ChunkInfo {
        String text;
        Vector start;
        float spaceWidth;
        float charWidthApprox;  // 平均字符宽度
        float fontSize;

        ChunkInfo(String t, Vector s, float spaceW, float charW, float fs) {
            text = t;
            start = s;
            spaceWidth = spaceW;
            charWidthApprox = charW;
            fontSize = fs;
        }
    }

    static class PlaceholderExtractionStrategy implements TextExtractionStrategy {
        private final List<ChunkInfo> chunks = new ArrayList<>();

        public List<ChunkInfo> getChunks() {
            return chunks;
        }

        @Override
        public void renderText(TextRenderInfo renderInfo) {
            String text = renderInfo.getText();
            if (text != null && !text.trim().isEmpty()) {
                Vector startPoint = renderInfo.getBaseline().getStartPoint();
                float spaceWidth = renderInfo.getSingleSpaceWidth();
                float endX = renderInfo.getBaseline().getEndPoint().get(Vector.I1);
                float charWidthApprox = text.length() > 0 ? (endX - startPoint.get(Vector.I1)) / text.length() : spaceWidth;
                float fontSize = 12f;  // 默认字体大小
                chunks.add(new ChunkInfo(text, startPoint, spaceWidth, charWidthApprox, fontSize));
            }
        }

        @Override
        public void beginTextBlock() {}
        @Override
        public void endTextBlock() {}
        @Override
        public void renderImage(ImageRenderInfo imageRenderInfo) {}

        @Override
        public String getResultantText() {
            StringBuilder sb = new StringBuilder();
            for (ChunkInfo ci : chunks) {
                sb.append(ci.text);
            }
            return sb.toString();
        }
    }

    static class Position {
        float x, y, fontSize, width;
        Position(float x, float y, float fontSize, float width) {
            this.x = x;
            this.y = y;
            this.fontSize = fontSize;
            this.width = width;
        }
    }
}