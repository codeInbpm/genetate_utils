package com.example.util.test;

import com.example.util.PdfUtil4;

import java.util.*;

public class FillPdfDemo4 {
    public static void main(String[] args) {
        try {
            String template = "src/main/resources/static/template_final.pdf";
            String output = "src/main/resources/output/final_filled.pdf";

            List<Map<String, String>> dataList = new ArrayList<>();

            Map<String, String> data1 = new HashMap<>();
            data1.put("responsible", "张雷明");
            data1.put("makesure", "王一凡");
            data1.put("a", "13");
            data1.put("b", "5");
            data1.put("c", "8");
            dataList.add(data1);

            PdfUtil4.generateMultiPagePdf(template, output, dataList, "mac");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}