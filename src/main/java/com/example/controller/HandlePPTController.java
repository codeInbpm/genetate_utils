package com.example.controller;

import org.apache.commons.io.FileUtils;

import org.apache.poi.xslf.usermodel.*;
import org.python.util.PythonInterpreter;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class HandlePPTController {

    public static void main(String[] args) throws IOException {
        // 加载 PPT 模板
        ClassPathResource resource = new ClassPathResource("templates/template_new_1.pptx");
        XMLSlideShow ppt = new XMLSlideShow(resource.getInputStream());

        // 获取第一张幻灯片作为模板
        XSLFSlide templateSlide = ppt.getSlides().get(0);

        // 假设这些数据是你要展示在每一页上的数据
        List<Map<String, String>> dataList = new ArrayList<>();
        Map<String, String> data1 = new HashMap<>();
        data1.put("valveType", "项目1");
        data1.put("checkItem", "检查项目1");
        data1.put("itemRequest", "要求1");
        data1.put("owner", "负责任1");
        data1.put("customer", "客户1");
        data1.put("plateConfirm", "板块1");
        data1.put("completeTime", "完成时间-1");
        data1.put("statusAndRisk", "状态及风险");
        data1.put("remPlan", "补救计划-1");
        data1.put("conclusion", "结论-1");
        dataList.add(data1);

        Map<String, String> data2 = new HashMap<>();
        data2.put("valveType", "项目2");
        data2.put("checkItem", "检查项目2");
        data2.put("itemRequest", "要求2");
        data2.put("owner", "负责任2");
        data2.put("customer", "客户2");
        data2.put("plateConfirm", "板块2");
        data2.put("completeTime", "完成时间-2");
        data2.put("statusAndRisk", "状态及风险2");
        data2.put("remPlan", "补救计划-2");
        data2.put("conclusion", "结论-2");
        dataList.add(data2);

        Map<String, String> data3 = new HashMap<>();
        data3.put("valveType", "项目3");
        data3.put("checkItem", "检查项目3");
        data3.put("itemRequest", "要求3");
        data3.put("owner", "负责任3");
        data3.put("customer", "客户3");
        data3.put("plateConfirm", "板块3");
        data3.put("completeTime", "完成时间-3");
        data3.put("statusAndRisk", "状态及风险3");
        data3.put("remPlan", "补救计划-3");
        data3.put("conclusion", "结论-3");
        dataList.add(data3);

        // 需要生成的页数
        int totalPages = dataList.size();

        // 遍历数据列表，复制模板幻灯片并填充数据
        for (int i = 0; i < totalPages; i++) {
            // 复制模板幻灯片
            XSLFSlide newSlide = ppt.createSlide().importContent(templateSlide);

            // 获取当前数据
            Map<String, String> currentData = dataList.get(i);
            String valveType = currentData.get("valveType");
            String checkItem = currentData.get("checkItem");
            String itemRequest = currentData.get("itemRequest");
            HashMap<Object, String> objectObjectHashMap = new HashMap<>();
            objectObjectHashMap.put("owner", "负责任1");
            objectObjectHashMap.put("customer", "客户1");
            objectObjectHashMap.put("plateConfirm", "板块1");
            objectObjectHashMap.put("remPlan", "补救结论");
            String planDate = currentData.get("planDate");
            String statusAndRisk = currentData.get("statusAndRisk");

            // 遍历新幻灯片中的形状，替换文本
            for (XSLFShape shape : newSlide.getShapes()) {
                // 替换文本形状
                if (shape instanceof XSLFTextShape) {
                    XSLFTextShape textShape = (XSLFTextShape) shape;
                    String originalText = textShape.getText();

                    // 替换占位符
                    originalText = originalText
                            .replace("{{valveType}}", valveType)
                            .replace("{{checkItem}}", checkItem)
                            .replace("{{itemRequest}}", itemRequest)
                            .replace("{{owner}}}", objectObjectHashMap.get("owner"))
                            .replace("{{customer}}}", objectObjectHashMap.get("customer"))
                            .replace("{{itemRequest}}", itemRequest)
                            .replace("{{statusAndRisk}}", statusAndRisk);

                    textShape.setText(originalText);
                }
                if(shape instanceof XSLFAutoShape){
                    XSLFAutoShape autoShape = (XSLFAutoShape) shape;
                    String text = autoShape.getText();
                    text = text.replace("{{owner}}", objectObjectHashMap.get("owner"));
                    text = text.replace("{{remPlan}}", objectObjectHashMap.get("remPlan"));
                    autoShape.setText(text);  // 设置替换后的文本

                }
                if(shape instanceof XSLFPictureShape){
                    XSLFPictureShape pictureShape = (XSLFPictureShape) shape;
                    String text = objectObjectHashMap.get("owner");
                    text = text.replace("{{owner}}", objectObjectHashMap.get("owner"));
                        }

                        // 替换表格内容
                        if (shape instanceof XSLFTable) {
                            XSLFTable table = (XSLFTable) shape;
                            for (int row = 0; row < table.getNumberOfRows(); row++) {
                                for (int col = 0; col < table.getNumberOfColumns(); col++) {
                                    XSLFTableCell cell = table.getCell(row, col);
                                    String cellText = cell.getText();


                                    // 替换占位符
                            cellText = cellText
                                    .replace("{{checkItem}}", objectObjectHashMap.get("planDate"))
                                    .replace("{{owner}}", objectObjectHashMap.get("owner"))
                                    .replace("{{customer}}", objectObjectHashMap.get("customer"))
                                    .replace("{{vehicleStage}}", objectObjectHashMap.get("key2"))
                                    .replace("{{chargePersonName}}", objectObjectHashMap.get("kkkkk"))
                                    .replace("{{shortTermMeasures}}", objectObjectHashMap.get("kkkkk"))
                                    .replace("{{problemCode}}", "呵呵呵呵")
                                    .replace("{{conclusion}}", "结论")
                                    .replace("{{remPlan}}", objectObjectHashMap.get("remPlan"))
                                    .replace("{{planDate}}", planDate);

                            cell.setText(cellText);

                            for (XSLFTextParagraph paragraph : cell.getTextParagraphs()) {
                                for (XSLFTextRun textRun : paragraph.getTextRuns()) {
//                        textRun.setFontColor(Color.BLACK);

                                    if (objectObjectHashMap.values().stream().anyMatch(cellText::contains)) {
//                                        textRun.setFontSize(28.0);
                                        textRun.setBold(true);
                                    }

                                    if (cellText.contains(objectObjectHashMap.get("key"))) {
//                                        textRun.setFontSize(18.0);
                                    } else {
                                        if (cellText.contains(dataList.get(i).get("itemRequest"))) {
//                                            textRun.setFontSize(12.0);
//                                            textRun.setFontColor(Color.BLUE);
                                            textRun.setBold(true);
                                        } else if (cellText.contains(" ")) {
//                                            textRun.setFontSize(20.0);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 删除原始的第一张幻灯片
        ppt.removeSlide(0);

        // 输出到新的 PPT 文件
        File outputFile = new File("/Users/benwang/Desktop/output_multiple_pages.pptx");
        ppt.write(FileUtils.openOutputStream(outputFile));
        ppt.close();

        System.out.println("PPT 文件生成成功！");
    }
}