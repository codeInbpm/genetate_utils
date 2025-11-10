package com.example.util.test;

import com.example.util.PdfUtil7;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 在指定状态时，自动添加勾选
 */
public class FillPdfDemo9 {
    public static void main(String[] args) {
        try {
            String template = "src/main/resources/static/template_final.pdf";
            String output = "src/main/resources/output/final_multi_pages4.pdf";

            List<Map<String, String>> dataList = new ArrayList<>();

            Map<String, String> p1 = new HashMap<>();
            p1.put("responsible", "A-张三");
            p1.put("makesure", "A-李四");
            p1.put("a", "10"); p1.put("b", "20"); p1.put("c", "30");
            p1.put("status", "red");  // 新增：指定状态颜色，决定哪个框下方打勾
            dataList.add(p1);

            Map<String, String> p2 = new HashMap<>();
            p2.put("responsible", "B-王五");
            p2.put("makesure", "B-赵六");
            p2.put("a", "99"); p2.put("b", "42"); p2.put("c", "78");p2.put("d", "99");
            p2.put("status", "green");  // 示例：绿色状态
            dataList.add(p2);

            Map<String, String> p3 = new HashMap<>();
            p3.put("responsible", "C-彭于晏");
            p3.put("makesure", "C-周杰伦");
            p3.put("a", "5"); p3.put("b", "8"); p3.put("c", "11");p3.put("g", "xx");
            p3.put("status", "yellow");  // 示例：黄色状态
            dataList.add(p3);

            PdfUtil7.generate(template, output, dataList, "mac");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}