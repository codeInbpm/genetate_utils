package com.example.util.test;

import com.example.util.PdfUtil;
import java.util.HashMap;
import java.util.Map;

public class FillPdfDemo2 {
    public static void main(String[] args) {
        try {
            String template = "src/main/resources/static/template.pdf";
            String output = "src/main/resources/output/filled.pdf";
            String fontPath = "/System/Library/Fonts/ZitherIndia.otf";     // Mac 中文字体

            Map<String, String> data = new HashMap<>();
            // 只需放占位符 key，无需 a/b/c 等（除非模板也有）
            data.put("makesure", "zlmmmmmm");
            data.put("responsible", "zlm");  // 如果有 {{responsible}}
            data.put("a", "123123");  // 如果有 {{responsible}}

            // 调用新方法
            PdfUtil.replacePlaceholders(template, output, data, fontPath);

            System.out.println("✅ PDF 已生成（占位符替换）: " + output);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}