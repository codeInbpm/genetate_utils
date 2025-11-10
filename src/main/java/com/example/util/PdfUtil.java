package com.example.util;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.*;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
public class PdfUtil {

    /**
     * 填充 PDF 模板
     * @param templatePath 模板路径
     * @param outputPath 输出路径
     * @param data 填充值
     * @param fontPath 字体路径（ttf / otf）
     */
    public static void fillTemplate(String templatePath, String outputPath,
                                    Map<String, String> data, String fontPath)
            throws IOException, DocumentException {

        PdfReader reader = new PdfReader(templatePath);
        FileOutputStream out = new FileOutputStream(outputPath);
        PdfStamper stamper = new PdfStamper(reader, out);
        AcroFields form = stamper.getAcroFields();

        BaseFont bf = BaseFont.createFont(fontPath, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
        form.addSubstitutionFont(bf);

        for (Map.Entry<String, String> entry : data.entrySet()) {
            form.setField(entry.getKey(), entry.getValue());
        }

        // 如果希望输出的 PDF 不可编辑：
        stamper.setFormFlattening(true);

        stamper.close();
        reader.close();
    }
    public static void replacePlaceholders(String templatePath, String outputPath,
                                           Map<String, String> data, String fontPath)
            throws IOException, DocumentException {

        PdfReader reader = new PdfReader(templatePath);
        FileOutputStream out = new FileOutputStream(outputPath);
        PdfStamper stamper = new PdfStamper(reader, out);

        // 加载字体（支持中文）
        BaseFont bf = BaseFont.createFont(fontPath, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);

        // 遍历每页
        for (int page = 1; page <= reader.getNumberOfPages(); page++) {
            PdfContentByte canvas = stamper.getOverContent(page);  // 在内容上层绘制

            // 获取页面内容字节（简化搜索）
            byte[] contentBytes = reader.getPageContent(page);
            String content = new String(contentBytes, "UTF-8");  // 假设 UTF-8；实际 PDF 可能需解码

            // 正则匹配所有 {{key}}
            Pattern pattern = Pattern.compile("\\{\\{([^}]+)\\}\\}");
            Matcher matcher = pattern.matcher(content);

            while (matcher.find()) {
                String key = matcher.group(1);
                String replacement = data.get(key);
                if (replacement == null) continue;

                // 粗略位置计算（实际需精确：用 PdfTextExtractor 或 StrategyClass 解析位置）
                // 这里简化：假设占位符在页面中心，字体大小 12（需根据模板调整）
                float x = 100;  // 起始 X（从模板测量）
                float y = reader.getPageSizeWithRotation(page).getHeight() - 100;  // 起始 Y（从底部）
                float fontSize = 12;

                // 绘制替换文本（覆盖旧的）
                canvas.beginText();
                canvas.setFontAndSize(bf, fontSize);
                canvas.setTextMatrix(x, y);
                canvas.showText(replacement);
                canvas.endText();

                // 可选：擦除旧文本（复杂，用白矩形覆盖或内容流替换）
                // canvas.rectangle(x - 5, y - fontSize - 5, replacement.length() * 6, fontSize + 10);
                // canvas.fill();
            }
        }

        stamper.close();
        reader.close();
    }
}