package com.example.util.test;

import com.example.util.PdfUtil;

import java.util.HashMap;
import java.util.Map;

public class FillPdfDemo {
    public static void main(String[] args) {
        try {
            String template = "src/main/resources/static/template.pdf"; // 模板路径
            String output = "src/main/resources/output/filled.pdf";     // 输出路径
            String fontPath = "/System/Library/Fonts/ZitherIndia.otf";     // Mac 中文字体

            Map<String, String> data = new HashMap<>();
            data.put("a", "12");
            data.put("b", "34");
            data.put("c", "56");
            data.put("d", "78");
            data.put("e", "910");
            data.put("f", "1112");
            data.put("g", "1314");
            data.put("h", "1516");
            data.put("i", "1718");
            data.put("responsible", "zlm");
            data.put("makesure", "zlmmmmmm");

            PdfUtil.fillTemplate(template, output, data, fontPath);

            System.out.println("✅ PDF 已生成: " + output);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}