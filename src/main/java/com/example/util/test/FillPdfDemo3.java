package com.example.util.test;

import com.example.util.PdfUtil;
import com.example.util.PdfUtil2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FillPdfDemo3 {
    public static void main(String[] args) {
        try {
            String template = "src/main/resources/static/template.pdf";
            String output = "src/main/resources/output/filled.pdf";
            String fontPath = "/System/Library/Fonts/Symbol.ttf";     // Mac 中文字体

            // 数据列表（类似 PPT dataList）
            List<Map<String, String>> dataList = new ArrayList<>();
            Map<String, String> data1 = new HashMap<>();            // 只需放占位符 key，无需 a/b/c 等（除非模板也有）
            data1.put("makesure", "zlmmmmmm");
            data1.put("responsible", "zlm");  // 如果有 {{responsible}}
            data1.put("a", "123123");  // 如果有 {{responsible}}
            data1.put("b", "123123");  // 如果有 {{responsible}}
            data1.put("c", "123123");  // 如果有 {{responsible}}
            dataList.add(data1);

            // 调用新方法
            PdfUtil2.generateMultiPagePdf(template, output, dataList, fontPath);

            System.out.println("✅ PDF 已生成（占位符替换）: " + output);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}