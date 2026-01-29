package com.report.service.impl;

import com.report.dto.SectionData;
import com.report.entity.TemplateStyle;
import com.report.utils.StyleManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xwpf.usermodel.*;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSpacing;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STLineSpacingRule;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * 纯 POI 实现的 Word 导出服务
 * 使用 Word 内置样式（Heading1、Normal 等）而非直接格式设置
 */
@Slf4j
@Service
public class WordExportService {

    /**
     * 导出报告到 Word 文档
     *
     * @param outputStream 输出流
     * @param templatePath 模板文件路径（用于复制样式）
     * @param reportName   报告名称
     * @param dateRange    日期范围
     * @param sections     章节数据树
     * @param styleMap     样式配置映射
     */
    public void exportReport(OutputStream outputStream,
            Path templatePath,
            String reportName,
            String dateRange,
            List<SectionData> sections,
            Map<String, TemplateStyle> styleMap) throws IOException {

        try (XWPFDocument document = createDocument(templatePath)) {
            // 1. 添加报告标题
            addReportTitle(document, reportName);

            // 2. 添加日期范围
            if (dateRange != null && !dateRange.trim().isEmpty()) {
                addDateRange(document, dateRange);
            }

            // 3. 添加分隔线
            addSeparator(document);

            // 4. 递归添加章节内容
            addSections(document, sections, styleMap);

            // 5. 写入输出流
            document.write(outputStream);
        }
    }

    /**
     * 创建文档，如果有模板则从模板复制样式
     */
    private XWPFDocument createDocument(Path templatePath) throws IOException {
        XWPFDocument document;

        if (templatePath != null && Files.exists(templatePath)) {
            // 基于模板创建文档（会继承模板的样式定义）
            try (InputStream is = Files.newInputStream(templatePath)) {
                XWPFDocument templateDoc = new XWPFDocument(is);
                document = new XWPFDocument();

                // 复制样式从模板到新文档
                StyleManager.copyStylesFromTemplate(document, templatePath);

                // 关闭模板文档
                templateDoc.close();
            } catch (Exception e) {
                log.warn("从模板创建文档失败，使用空白文档: {}", e.getMessage());
                document = new XWPFDocument();
                StyleManager.ensureDefaultStyles(document);
            }
        } else {
            // 创建空白文档并添加默认样式
            document = new XWPFDocument();
            StyleManager.ensureDefaultStyles(document);
        }

        return document;
    }

    /**
     * 添加报告标题
     */
    private void addReportTitle(XWPFDocument document, String reportName) {
        XWPFParagraph titlePara = document.createParagraph();
        titlePara.setAlignment(ParagraphAlignment.CENTER);

        XWPFRun titleRun = titlePara.createRun();
        titleRun.setText(reportName);
        titleRun.setBold(true);
        titleRun.setFontSize(24);
        titleRun.setFontFamily("黑体");

        // 段后间距
        setSpacingAfter(titlePara, 240);
    }

    /**
     * 添加日期范围
     */
    private void addDateRange(XWPFDocument document, String dateRange) {
        XWPFParagraph datePara = document.createParagraph();
        datePara.setAlignment(ParagraphAlignment.CENTER);

        XWPFRun dateRun = datePara.createRun();
        dateRun.setText("报告周期：" + dateRange);
        dateRun.setFontSize(12);
        dateRun.setFontFamily("宋体");
        dateRun.setColor("666666");

        setSpacingAfter(datePara, 120);
    }

    /**
     * 添加分隔线
     */
    private void addSeparator(XWPFDocument document) {
        XWPFParagraph sepPara = document.createParagraph();
        sepPara.setAlignment(ParagraphAlignment.CENTER);
        sepPara.setBorderBottom(Borders.SINGLE);
        setSpacingAfter(sepPara, 240);
    }

    /**
     * 递归添加章节内容
     */
    private void addSections(XWPFDocument document,
            List<SectionData> sections,
            Map<String, TemplateStyle> styleMap) {
        if (sections == null || sections.isEmpty()) {
            return;
        }

        for (SectionData section : sections) {
            // 1. 添加标题
            addSectionTitle(document, section.getTitle(), section.getLevel(), styleMap);

            // 2. 添加内容
            if (section.getContent() != null && !section.getContent().trim().isEmpty()) {
                addSectionContent(document, section.getContent(), styleMap);
            }

            // 3. 递归处理子章节
            if (section.getChildren() != null && !section.getChildren().isEmpty()) {
                addSections(document, section.getChildren(), styleMap);
            }
        }
    }

    /**
     * 添加章节标题，使用 Word 内置样式
     */
    private void addSectionTitle(XWPFDocument document,
            String title,
            int level,
            Map<String, TemplateStyle> styleMap) {
        if (title == null || title.trim().isEmpty()) {
            return;
        }

        XWPFParagraph titlePara = document.createParagraph();

        // 获取并应用样式 ID
        String styleId = StyleManager.getStyleId(level, styleMap);
        try {
            titlePara.setStyle(styleId);
            log.debug("为标题 '{}' 应用样式: {}", title, styleId);
        } catch (Exception e) {
            log.debug("样式 {} 应用失败，尝试替代方案", styleId);
            // 尝试其他可能的样式 ID
            tryAlternativeStyles(document, titlePara, level);
        }

        XWPFRun titleRun = titlePara.createRun();
        titleRun.setText(title);

        // 如果样式设置失败，手动设置基本格式作为后备
        if (!hasStyleApplied(titlePara)) {
            applyFallbackTitleFormat(titleRun, level);
        }
    }

    /**
     * 添加章节内容，使用正文样式
     */
    private void addSectionContent(XWPFDocument document,
            String content,
            Map<String, TemplateStyle> styleMap) {
        String plainText = stripHtml(content);
        if (plainText.isEmpty()) {
            return;
        }

        XWPFParagraph contentPara = document.createParagraph();

        // 应用正文样式
        String bodyStyleId = StyleManager.getBodyStyleId(styleMap);
        try {
            contentPara.setStyle(bodyStyleId);
        } catch (Exception e) {
            log.debug("正文样式 {} 应用失败", bodyStyleId);
        }

        // 首行缩进
        contentPara.setFirstLineIndent(480); // 约两个中文字符

        XWPFRun contentRun = contentPara.createRun();

        // 处理多行文本
        String[] lines = plainText.split("\n");
        boolean first = true;
        for (String line : lines) {
            String trimmedLine = line.trim();
            if (!trimmedLine.isEmpty()) {
                if (!first) {
                    contentRun.addBreak();
                }
                contentRun.setText(trimmedLine);
                first = false;
            }
        }
    }

    /**
     * 尝试应用替代样式
     */
    private void tryAlternativeStyles(XWPFDocument document, XWPFParagraph para, int level) {
        List<String> alternatives = StyleManager.getPossibleStyleIds(level);

        for (String altStyleId : alternatives) {
            try {
                if (StyleManager.styleExists(document, altStyleId)) {
                    para.setStyle(altStyleId);
                    return;
                }
            } catch (Exception e) {
                // 继续尝试下一个
            }
        }
    }

    /**
     * 检查段落是否已应用了样式
     */
    private boolean hasStyleApplied(XWPFParagraph para) {
        String styleId = para.getStyleID();
        return styleId != null && !styleId.isEmpty();
    }

    /**
     * 应用后备标题格式（当样式设置失败时）
     */
    private void applyFallbackTitleFormat(XWPFRun run, int level) {
        run.setBold(true);
        run.setFontFamily("黑体");

        switch (level) {
            case 1 -> run.setFontSize(22);
            case 2 -> run.setFontSize(18);
            case 3 -> run.setFontSize(16);
            case 4 -> run.setFontSize(14);
            default -> run.setFontSize(12);
        }
    }

    /**
     * 设置段后间距
     */
    private void setSpacingAfter(XWPFParagraph para, int twips) {
        try {
            if (para.getCTP().getPPr() == null) {
                para.getCTP().addNewPPr();
            }
            CTSpacing spacing = para.getCTP().getPPr().getSpacing();
            if (spacing == null) {
                spacing = para.getCTP().getPPr().addNewSpacing();
            }
            spacing.setAfter(BigInteger.valueOf(twips));
            spacing.setLine(BigInteger.valueOf(360)); // 1.5倍行距
            spacing.setLineRule(STLineSpacingRule.AUTO);
        } catch (Exception e) {
            // 忽略
        }
    }

    /**
     * 去除 HTML 标签，转换为纯文本
     */
    private String stripHtml(String html) {
        if (html == null) {
            return "";
        }

        // 转换换行标签
        String text = html.replaceAll("(?i)<br\\s*/?>", "\n");
        text = text.replaceAll("(?i)</p>", "\n");
        text = text.replaceAll("(?i)<p[^>]*>", "");

        // 移除其他 HTML 标签
        text = text.replaceAll("<[^>]*>", "");

        // 转换 HTML 实体
        text = text.replace("&nbsp;", " ");
        text = text.replace("&lt;", "<");
        text = text.replace("&gt;", ">");
        text = text.replace("&amp;", "&");
        text = text.replace("&quot;", "\"");

        return text.trim();
    }
}
