package com.example.util;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.pdf.parser.*;
import com.itextpdf.text.pdf.parser.TextRenderInfo;
import com.itextpdf.text.pdf.parser.Vector;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * PDF 工具类：基于模板生成多页 PDF，替换静态文本占位符 {{key}}
 * 修复：收集所有 chunk，合并 fullText 匹配占位符（允许空格），映射到起始 chunk 位置。
 */
public class PdfUtil2 {

    public static void generateMultiPagePdf(String templatePath, String outputPath,
                                            List<Map<String, String>> dataList, String fontPath)
            throws IOException, DocumentException {

        // 提取模板位置（仅一次）
        PdfReader templateReader = new PdfReader(templatePath);
        BaseFont bf = BaseFont.createFont(fontPath, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
        Map<String, List<Position>> placeholderPositions = extractPlaceholderPositions(templateReader, 1, bf);

        // 输出文档
        Document document = new Document(templateReader.getPageSizeWithRotation(1));
        PdfCopy copy = new PdfCopy(document, new FileOutputStream(outputPath));
        document.open();

        int totalPages = dataList.size();

        // 为每个数据项：创建临时填充 PDF，复制其第1页到输出
        for (int i = 0; i < totalPages; i++) {
            ByteArrayOutputStream tempOut = new ByteArrayOutputStream();
            PdfReader tempReader = new PdfReader(templatePath);
            PdfStamper tempStamper = new PdfStamper(tempReader, tempOut);

            // 当前数据
            Map<String, String> currentData = dataList.get(i);

            // 替换占位符（在临时 stamper 上）
            replacePlaceholdersOnPage(tempStamper, 1, currentData, placeholderPositions, bf);

            tempStamper.close();
            tempReader.close();

            // 从临时 PDF 复制第1页到输出
            PdfReader pageReader = new PdfReader(tempOut.toByteArray());
            PdfImportedPage importedPage = copy.getImportedPage(pageReader, 1);
            copy.addPage(importedPage);
            pageReader.close();
        }

        document.close();
        templateReader.close();
    }

    /**
     * 提取模板页面中所有 {{key}} 的位置
     */
    private static Map<String, List<Position>> extractPlaceholderPositions(PdfReader reader, int pageNum,
                                                                           BaseFont bf) throws IOException {
        Map<String, List<Position>> positions = new HashMap<>();
        PlaceholderExtractionStrategy strategy = new PlaceholderExtractionStrategy();
        String fullText = PdfTextExtractor.getTextFromPage(reader, pageNum, strategy);
        System.out.println("完整提取文本: " + fullText);

        // 后处理：匹配占位符并映射位置
        findPlaceholders(positions, fullText, strategy.getChunks());

        return positions;
    }

    /**
     * 后处理：从 fullText 匹配占位符，映射到 chunks 的位置
     */
    private static void findPlaceholders(Map<String, List<Position>> positions, String fullText, List<ChunkInfo> chunks) {
        // 模式：匹配 {{...}} 允许内部任意非}字符（包括空格）
        Pattern p = Pattern.compile("\\{\\{[^}]+\\}\\}");
        Matcher m = p.matcher(fullText);

        while (m.find()) {
            String match = m.group();
            String key = match.substring(2, match.length() - 2).trim();  // 提取 key 并 trim 空格

            int startIdx = m.start();
            int endIdx = m.end();

            // 查找起始 chunk
            int cumLen = 0;
            Vector chunkStart = null;
            float totalWidth = 0;
            float spaceWidth = 0;  // 从第一个相关 chunk 取

            for (ChunkInfo ci : chunks) {
                int chunkLen = ci.text.length();
                if (cumLen <= startIdx && startIdx < cumLen + chunkLen) {
                    chunkStart = ci.start;
                    spaceWidth = ci.spaceWidth;
                    totalWidth = (endIdx - startIdx) * spaceWidth;  // 近似宽度
                    break;
                }
                cumLen += chunkLen;
            }

            if (chunkStart != null) {
                float x = chunkStart.get(Vector.I1);
                float y = chunkStart.get(Vector.I2);
                float fontSize = 12f;
                Position pos = new Position(x, y, fontSize, totalWidth);
                positions.computeIfAbsent(key, k -> new ArrayList<>()).add(pos);
                System.out.println("找到占位符 {{" + key + "}} at (" + x + ", " + y + "), width: " + totalWidth);
            }
        }
    }

    /**
     * 在指定页面替换占位符：覆盖旧位置，绘制新文本
     */
    private static void replacePlaceholdersOnPage(PdfStamper stamper, int pageNum,
                                                  Map<String, String> data,
                                                  Map<String, List<Position>> positions, BaseFont bf) {
        PdfContentByte canvas = stamper.getUnderContent(pageNum);  // 改为 under
        for (Map.Entry<String, String> entry : data.entrySet()) {
            String key = entry.getKey();
            String replacement = entry.getValue();
            List<Position> posList = positions.get(key);
            if (posList != null && !posList.isEmpty()) {
                for (Position pos : posList) {
                    float x = pos.getX();
                    float y = pos.getY() + 10; // 调整基线偏移
                    float width = pos.getWidth();
                    float height = pos.getFontSize() * 1.2f;

                    // 调试矩形
                    canvas.setColorStroke(BaseColor.RED);
                    canvas.rectangle(x, y - height, width, height);
                    canvas.stroke();

                    // 白底覆盖原占位符
                    canvas.setColorFill(BaseColor.WHITE);
                    canvas.rectangle(x, y - height, width, height);
                    canvas.fill();

                    // 绘制文字（使用中文字体）
                    canvas.beginText();
                    canvas.setFontAndSize(bf, pos.getFontSize());
                    canvas.setTextMatrix(x, y);
                    canvas.showText(replacement);
                    canvas.endText();
                }
                System.out.println("✅ 替换 {{" + key + "}} 为: " + replacement);
            } else {
                System.out.println("❌ 未找到占位符: {{" + key + "}}");
            }
        }
    }

    /**
     * Chunk 信息类
     */
    static class ChunkInfo {
        String text;
        Vector start;
        float spaceWidth;
        float fontSize;

        ChunkInfo(String text, Vector start, float spaceWidth, float fontSize) {
            this.text = text;
            this.start = start;
            this.spaceWidth = spaceWidth;
            this.fontSize = fontSize;
        }
    }

    /**
     * 自定义提取策略：收集所有 chunk（不直接匹配）
     */
    static class PlaceholderExtractionStrategy implements TextExtractionStrategy {
        private final List<ChunkInfo> chunks = new ArrayList<>();
        private final StringBuilder fullText = new StringBuilder();

        public List<ChunkInfo> getChunks() { return chunks; }
        public String getResultantText() { return fullText.toString(); }

        @Override
        public void beginTextBlock() {}

        @Override
        public void endTextBlock() {}

        @Override
        public void renderImage(ImageRenderInfo imageRenderInfo) {}

        @Override
        public void renderText(TextRenderInfo renderInfo) {
            String text = renderInfo.getText();
            if (text != null && !text.trim().isEmpty()) {
                fullText.append(text);

                // 调试：打印含 {} 的 chunk
                if (text.contains("{") || text.contains("}")) {
                    System.out.println("Chunk 调试 - 原始: '" + text + "'");
                }

                Vector startPoint = renderInfo.getBaseline().getStartPoint();
                float spaceWidth = renderInfo.getSingleSpaceWidth();
                float fontSize = 12f;  // 默认
                chunks.add(new ChunkInfo(text, startPoint, spaceWidth, fontSize));
            }
        }
    }

    /**
     * 自定义位置类
     */
    static class Position {
        float x, y, fontSize, width;

        Position(float x, float y, float fontSize, float width) {
            this.x = x;
            this.y = y;
            this.fontSize = fontSize;
            this.width = width;
        }

        float getX() { return x; }
        float getY() { return y; }
        float getFontSize() { return fontSize; }
        float getWidth() { return width; }
    }
}