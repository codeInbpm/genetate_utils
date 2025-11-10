package com.example.util.test;

import com.example.util.PdfUtil4;
import com.example.util.PdfUtil5;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FillPdfDemo5 {
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

            PdfUtil5.generateMultiPagePdf(template, output, dataList, "mac");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}