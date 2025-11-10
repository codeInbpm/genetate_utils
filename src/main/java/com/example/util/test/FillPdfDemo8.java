package com.example.util.test;

import com.example.util.PdfUtil7;

import java.util.*;

/**
 * Demo8：演示 dataList 来自动态来源（例如数据库或接口）
 * - list 的长度未知（可能 1 页，也可能 N 页）
 * - 遍历 list，取出每条 map 填到 PDF 模板
 */
public class FillPdfDemo8 {
    public static void main(String[] args) {
        try {
            String template = "src/main/resources/static/template_final.pdf";
            String output   = "src/main/resources/output/final_multi_pages_demo8.pdf";

            List<Map<String, String>> dataList = loadData();

            // ✅ 调用 PdfUtil7（只需要调用一次）
            PdfUtil7.generate(template, output, dataList, detectOS());

            System.out.println("✅ Demo8Pdf 生成成功 → " + output);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static List<Map<String, String>> loadData() {
        List<Map<String, String>> list = new ArrayList<>();

        for (int i = 1; i <= 4; i++) {
            Map<String, String> m = new HashMap<>();
            m.put("responsible", "人员-" + i);
            m.put("makesure", "确认人-" + i);
            m.put("a", String.valueOf(i * 10));
            m.put("b", String.valueOf(i * 20));
            m.put("c", String.valueOf(i * 30));
            list.add(m);
        }
        return list;
    }

    /** 自动识别 Mac / Windows，供 PdfUtil7 加载字体使用 */
    private static String detectOS() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) return "win";
        if (os.contains("mac")) return "mac";
        return "other";
    }
}
