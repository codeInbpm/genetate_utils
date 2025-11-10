package com.example.util;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import java.io.*;
import java.util.*;
import java.util.List;

public class PdfUtil6 {

    /**
     * 当模板只有 1 页时，根据 dataList 数据条数自动复制模板，生成多页 PDF
     */
    public static void generateTemplatePages(String templatePath, String outputPath,
                                             List<Map<String, String>> dataList, String osType)
            throws Exception {

        PdfReader templateReader = new PdfReader(templatePath);
        int templatePages = templateReader.getNumberOfPages();
//        if (templatePages != 1) {
//            throw new IllegalArgumentException("模板 PDF 必须只有 1 页");
//        }

        BaseFont font = PdfUtil5.loadChineseFont(osType);
        Map<String, List<PdfUtil5.Position>> placeholderPositions =
                PdfUtil5.extractPlaceholderPositions(templateReader, 1);

        Document document = new Document(templateReader.getPageSizeWithRotation(1));
        PdfCopy copy = new PdfCopy(document, new FileOutputStream(outputPath));
        document.open();

        for (int i = 0; i < dataList.size(); i++) {
            ByteArrayOutputStream tempOut = new ByteArrayOutputStream();
            PdfReader tmpReader = new PdfReader(templatePath);
            PdfStamper stamper = new PdfStamper(tmpReader, tempOut);

            PdfUtil5.replacePlaceholdersOnPage(stamper, 1, dataList.get(i), placeholderPositions, font);

            stamper.close();
            tmpReader.close();

            PdfReader newPageReader = new PdfReader(tempOut.toByteArray());
            PdfImportedPage importedPage = copy.getImportedPage(newPageReader, 1);
            copy.addPage(importedPage);
            newPageReader.close();
        }

        document.close();
        templateReader.close();

        System.out.println("✅ PdfUtil6 多页生成成功: " + outputPath);
    }
}
