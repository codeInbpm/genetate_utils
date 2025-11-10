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
 * 支持：
 * 1. 多页模板填充 (List<Map<String,String>>)
 * 2. {{key}} 占位符精确定位+覆盖+写入
 * 3. 自动加载中文字体，IDENTITY_H 支持中文
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

        // 首次抽取占位符坐标
        Map<String, List<Position>> positions = extractPlaceholderPositions(templateReader, 1);

        //  输出 PDF
        Document document = new Document(templateReader.getPageSizeWithRotation(1));
        PdfCopy copy = new PdfCopy(document, new FileOutputStream(outputPath));
        document.open();

        for (Map<String, String> data : dataList) {
            // 用 temp 处理模板
            ByteArrayOutputStream tempOut = new ByteArrayOutputStream();
            PdfReader tmpReader = new PdfReader(templatePath);
            PdfStamper stamper = new PdfStamper(tmpReader, tempOut);

            replacePlaceholdersOnPage(stamper, 1, data, positions, font);

            stamper.close();
            tmpReader.close();

            PdfReader newPageReader = new PdfReader(tempOut.toByteArray());
            PdfImportedPage importedPage = copy.getImportedPage(newPageReader, 1);
            copy.addPage(importedPage);
            newPageReader.close();
        }

        document.close();
        templateReader.close();
        System.out.println("✅ PdfUtil7 生成完成: " + outputPath);
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

    // ---------------------- 提取占位符（精确边界框） ----------------------
    static Map<String, List<Position>> extractPlaceholderPositions(PdfReader reader, int pageNum)
            throws IOException {
        Map<String, List<Position>> positions = new LinkedHashMap<>();
        PlaceholderExtractionStrategy strategy = new PlaceholderExtractionStrategy();
        String fullText = PdfTextExtractor.getTextFromPage(reader, pageNum, strategy);

        // 增强匹配：忽略多余空格，匹配 {{key}}
        Pattern p = Pattern.compile("\\{\\{[^{}]+}}",
                Pattern.MULTILINE);
        Matcher m = p.matcher(fullText);
        List<ChunkInfo> chunks = strategy.getChunks();

        while (m.find()) {
            String match = m.group();
            String key = match.substring(2, match.length() - 2).trim();
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
                Vector start = info.getBaseline().getStartPoint();
                float endX = info.getBaseline().getEndPoint().get(Vector.I1);
                float charWidthApprox = text.length() > 0 ? (endX - start.get(Vector.I1)) / text.length() : 1f;
                chunks.add(new ChunkInfo(text, start, charWidthApprox));
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

    static class Position {
        float x, y, fontSize, width;
        Position(float x, float y, float fontSize, float width) {
            this.x = x; this.y = y; this.fontSize = fontSize; this.width = width;
        }
    }
}