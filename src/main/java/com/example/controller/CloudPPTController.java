//package com.example.controller;
//
//import freemarker.template.Configuration;
//import freemarker.template.Template;
//import freemarker.template.TemplateException;
//import org.apache.poi.xslf.usermodel.*;
//import org.springframework.core.io.ClassPathResource;
//import org.springframework.web.bind.annotation.RestController;
//
//import java.awt.*;
//import java.io.File;
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.Objects;
//
//@RestController
//public class CloudPPTController {
//
//    public static void main(String[] args) throws IOException, TemplateException {
//        // 加载 PPT 模板
//        ClassPathResource resource = new ClassPathResource("templates/Template3.pptx");
//        XMLSlideShow ppt = new XMLSlideShow(resource.getInputStream());
//
//        // 创建 FreeMarker 配置
//        Configuration cfg = new Configuration(Configuration.VERSION_2_3_31);
//        cfg.setClassForTemplateLoading(CloudPPTController.class, "/");
//
//        // 创建一个 Map 来存储需要替换的占位符和新值
//        Map<String, String> placeholders = new HashMap<>();
//        placeholders.put("{{project}}", "新项目名称");
//        placeholders.put("{{username}}", "xoxoxoxoxoxo");
//        placeholders.put("{{problemCode}}", "12345");
//        placeholders.put("{{whenTime}}", "2024-10-15");
//        placeholders.put("{{chargePersonName}}", "负责人姓名");
//        placeholders.put("{{severity}}", "严重性");
//        placeholders.put("{{problemStatus}}", "问题状态");
//        placeholders.put("{{problemDesc}}", "问题描述");
//        placeholders.put("{{rootCauseAnalysis}}", "根本原因分析");
//        placeholders.put("{{progressInContainment}}", "控制进度");
//        placeholders.put("{{shortTermMeasures}}", "短期措施");
//        placeholders.put("{{LongTermMeasures}}", "长期措施");
//
//        // 遍历所有幻灯片
//        for (XSLFSlide slide : ppt.getSlides()) {
//            // 遍历所有形状
//            for (XSLFShape shape : slide.getShapes()) {
//                if (shape instanceof XSLFTextShape) {
//                    XSLFTextShape textShape = (XSLFTextShape) shape;
//                    String text = textShape.getText();
//
//                    // 使用 FreeMarker 替换文本
//                    String replacedText = replacePlaceholders(text, placeholders);
//                    textShape.setText(replacedText);
//                } else if (shape instanceof XSLFTable) {
//                    // 遍历表格的每一行
//                    XSLFTable table = (XSLFTable) shape;
//
//                    // 遍历每个 map 以获取问题详细信息
//                    List<Map<String, Object>> d2Info = List.of(Map.of(
//                            "PROJECT_NAME", "项目1",
//                            "PROBLEM_CODE", "问题代码1",
//                            "PROBLEM_SOURCE", "问题来源1",
//                            "WHEN_TIME", "2023-07-01",
//                            "CHARGE_PERSON_NAME", "负责人1",
//                            "SEVERITY", "严重程度1",
//                            "VEHICLE_STAGE", "什么stage",
//                            "PROBLEM_STATUS", "问题状态1",
//                            "PROBLEM_DESC", "问题描述1"
//                    ));
//                    for (Map<String, Object> problemDetails : d2Info) {
//                        // 获取问题详细信息
//                        String project = Objects.toString(problemDetails.get("PROJECT_NAME"), "");
//                        String problemCode = Objects.toString(problemDetails.get("PROBLEM_CODE"), "");
//                        String problemSource = Objects.toString(problemDetails.get("PROBLEM_SOURCE"), "");
//                        String problemDesc = Objects.toString(problemDetails.get("PROBLEM_DESC"), "");
//                        String vehicleStage = Objects.toString(problemDetails.get("VEHICLE_STAGE"), "");
//                        String whenTime = Objects.toString(problemDetails.get("WHEN_TIME"), "");
//                        String chargePersonName = Objects.toString(problemDetails.get("CHARGE_PERSON_NAME"), "");
//                        String severity = Objects.toString(problemDetails.get("SEVERITY"), "");
//
//                        // 创建一个映射来存储占位符和对应的替换值
//                        Map<String, String> replacements = new HashMap<>();
//                        replacements.put("${project}", project); // 使用正确的占位符格式
//                        replacements.put("${{project}}", project); // 使用正确的占位符格式
//                        replacements.put("${problemCode}", problemCode);
//                        replacements.put("${problemStatus}", problemCode);
//                        replacements.put("${problemSource}", problemSource);
//                        replacements.put("${vehicleStage}", vehicleStage);
//                        replacements.put("${problemDesc}", problemDesc);
//                        replacements.put("${whenTime}", whenTime);
//                        replacements.put("${rootCauseAnalysis}", severity);
//                        replacements.put("${chargePersonName}", chargePersonName);
//                        replacements.put("${severity}", severity);
//
//                        // 遍历表格的每一行和每一列
//                        for (int i = 0; i < table.getRows().size(); i++) {
//                            XSLFTableRow row = table.getRows().get(i); // 获取行
//                            for (int j = 0; j < row.getCells().size(); j++) {
//                                XSLFTableCell cell = row.getCells().get(j); // 获取单元格
//
//                                // 遍历段落
//                                for (XSLFTextParagraph paragraph : cell.getTextParagraphs()) {
//                                    // 遍历文本运行
//                                    for (XSLFTextRun textRun : paragraph.getTextRuns()) {
//                                        String cellText = textRun.getText();
//
//                                        // 替换占位符
//                                        for (Map.Entry<String, String> entry : replacements.entrySet()) {
//                                            cellText = cellText.replace(entry.getKey(), entry.getValue());
//                                        }
//
//                                        // 设置更新后的文本
//                                        textRun.setText(cellText);
//                                        textRun.setFontColor(Color.BLUE); // 设置字体颜色
//                                    }
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//        }
//
//// 保存修改后的演示文稿
//        try (FileOutputStream out = new FileOutputStream(new File("/Users/benwang/Desktop/8dpp_project3.pptx"))) {
//            ppt.write(out);
//        }
//    }
//    private static String replacePlaceholders(String text, Map<String, String> placeholders) {
//        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
//            // 使用替换内容的格式
//            text = text.replace(entry.getKey(), entry.getValue());
//        }
//        return text;
//    }
//}