package com.report.utils;

import org.apache.poi.xwpf.usermodel.*;

import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 生成 POI-TL 导出模板的工具类
 * 运行此类可创建默认的 export_template.docx
 */
public class TemplateGenerator {

    public static void main(String[] args) throws Exception {
        // 目标路径
        Path outputPath = Paths.get("src/main/resources/templates/export_template.docx");

        // 确保目录存在
        Files.createDirectories(outputPath.getParent());

        try (XWPFDocument doc = new XWPFDocument()) {
            // 1. 标题占位符 - 居中，黑体，24号
            XWPFParagraph titlePara = doc.createParagraph();
            titlePara.setAlignment(ParagraphAlignment.CENTER);
            titlePara.setSpacingAfter(200);
            XWPFRun titleRun = titlePara.createRun();
            titleRun.setText("{{reportName}}");
            titleRun.setBold(true);
            titleRun.setFontSize(24);
            titleRun.setFontFamily("黑体");

            // 2. 日期范围占位符 - 居中，小四
            XWPFParagraph datePara = doc.createParagraph();
            datePara.setAlignment(ParagraphAlignment.CENTER);
            datePara.setSpacingAfter(400);
            XWPFRun dateRun = datePara.createRun();
            dateRun.setText("{{dateRange}}");
            dateRun.setFontSize(12);
            dateRun.setFontFamily("宋体");

            // 3. 章节占位符
            XWPFParagraph sectionPara = doc.createParagraph();
            XWPFRun sectionRun = sectionPara.createRun();
            sectionRun.setText("{{sections}}");

            // 保存文件
            try (FileOutputStream fos = new FileOutputStream(outputPath.toFile())) {
                doc.write(fos);
            }

            System.out.println("模板创建成功: " + outputPath.toAbsolutePath());
        }
    }
}
