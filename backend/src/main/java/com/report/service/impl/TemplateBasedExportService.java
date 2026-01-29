package com.report.service.impl;

import com.report.entity.TemplateSection;
import com.report.entity.TemplateStyle;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xwpf.usermodel.*;
import org.apache.xmlbeans.XmlCursor;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.*;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * 基于模板的 Word 导出服务
 * 核心逻辑：自动识别模板中的标题位置，在标题下方插入内容
 * 保持模板的完整样式
 */
@Slf4j
@Service
public class TemplateBasedExportService {

    /**
     * 基于模板导出 Word 文档
     *
     * @param templatePath    模板文件路径
     * @param sections        模板章节列表
     * @param contentMap      section_key -> 内容 的映射
     * @param bodyStyleConfig 正文样式配置（可选）
     * @param outputStream    输出流
     */
    public void exportWithTemplate(Path templatePath,
            List<TemplateSection> sections,
            Map<String, String> contentMap,
            TemplateStyle bodyStyleConfig,
            OutputStream outputStream) throws IOException {

        if (templatePath == null || !Files.exists(templatePath)) {
            throw new IOException("模板文件不存在: " + templatePath);
        }

        // 构建标题 -> section 映射
        Map<String, TemplateSection> titleToSection = new LinkedHashMap<>();
        for (TemplateSection section : sections) {
            if (section.getTitle() != null && !section.getTitle().trim().isEmpty()) {
                titleToSection.put(normalizeTitle(section.getTitle()), section);
            }
        }

        log.info("开始模板式导出，共 {} 个章节标题需要匹配", titleToSection.size());

        try (InputStream is = Files.newInputStream(templatePath);
                XWPFDocument doc = new XWPFDocument(is)) {

            // 获取正文样式信息（用于新插入的内容段落）
            String bodyStyleId = detectBodyStyleId(doc);
            log.info("检测到正文样式 ID: {}", bodyStyleId);

            // 遍历所有段落，找到标题并在其后插入内容
            List<XWPFParagraph> paragraphs = new ArrayList<>(doc.getParagraphs());
            int insertedCount = 0;

            for (int i = 0; i < paragraphs.size(); i++) {
                XWPFParagraph para = paragraphs.get(i);
                String paraText = normalizeTitle(para.getText());

                if (paraText.isEmpty()) {
                    continue;
                }

                // 检查是否匹配某个标题
                TemplateSection matchedSection = titleToSection.get(paraText);
                if (matchedSection != null) {
                    String sectionKey = matchedSection.getSectionKey();
                    String content = contentMap.get(sectionKey);

                    log.debug("匹配到标题: '{}' -> sectionKey: {}", paraText, sectionKey);

                    // 仅当内容不为空时插入
                    if (content != null && !content.trim().isEmpty()) {
                        String plainText = stripHtml(content);
                        if (!plainText.isEmpty()) {
                            insertContentAfterParagraph(doc, para, plainText, bodyStyleId, bodyStyleConfig);
                            insertedCount++;
                            log.debug("在标题 '{}' 下方插入内容", matchedSection.getTitle());
                        }
                    }
                }
            }

            log.info("模板式导出完成，共插入 {} 个内容段落", insertedCount);

            // 写入输出流
            doc.write(outputStream);
        }
    }

    /**
     * 检测文档中的正文样式 ID
     */
    private String detectBodyStyleId(XWPFDocument doc) {
        // 尝试常见的正文样式 ID
        String[] possibleIds = { "Normal", "a", "a0", "正文", "BodyText" };

        if (doc.getStyles() != null) {
            for (String id : possibleIds) {
                try {
                    if (doc.getStyles().getStyle(id) != null) {
                        return id;
                    }
                } catch (Exception e) {
                    // 忽略
                }
            }
        }

        return "Normal"; // 默认返回 Normal
    }

    /**
     * 在指定段落后插入内容段落
     */
    private void insertContentAfterParagraph(XWPFDocument doc,
            XWPFParagraph headingPara,
            String content,
            String bodyStyleId,
            TemplateStyle bodyStyleConfig) {

        // 使用 XmlCursor 在标题后插入新段落
        XmlCursor cursor = headingPara.getCTP().newCursor();
        cursor.toNextSibling();

        XWPFParagraph newPara = doc.insertNewParagraph(cursor);
        if (newPara == null) {
            // 如果插入失败，使用 createParagraph 作为备选
            newPara = doc.createParagraph();
            log.warn("使用 XmlCursor 插入失败，使用 createParagraph 作为备选");
        }

        cursor.dispose();

        // 设置段落样式
        try {
            newPara.setStyle(bodyStyleId);
        } catch (Exception e) {
            log.debug("设置样式 {} 失败: {}", bodyStyleId, e.getMessage());
        }

        // 添加内容
        XWPFRun run = newPara.createRun();

        // 处理多行内容
        String[] lines = content.split("\n");
        boolean first = true;
        for (String line : lines) {
            String trimmedLine = line.trim();
            if (!trimmedLine.isEmpty()) {
                if (!first) {
                    run.addBreak();
                }
                run.setText(trimmedLine);
                first = false;
            }
        }

        // 应用样式配置（如果提供）
        if (bodyStyleConfig != null) {
            applyStyleConfig(newPara, run, bodyStyleConfig);
        } else {
            // 使用默认正文格式
            applyDefaultBodyFormat(newPara, run);
        }
    }

    /**
     * 应用样式配置
     */
    private void applyStyleConfig(XWPFParagraph para, XWPFRun run, TemplateStyle style) {
        if (style.getFontFamily() != null && !style.getFontFamily().isEmpty()) {
            run.setFontFamily(style.getFontFamily());
        }

        if (style.getFontSize() != null && style.getFontSize() > 0) {
            run.setFontSize(style.getFontSize());
        }

        if (style.getBold() != null) {
            run.setBold(style.getBold());
        }

        if (style.getItalic() != null) {
            run.setItalic(style.getItalic());
        }

        if (style.getFontColor() != null && !style.getFontColor().isEmpty()
                && !"auto".equals(style.getFontColor())) {
            run.setColor(style.getFontColor());
        }

        // 首行缩进
        if (style.getFirstLineIndent() != null && style.getFirstLineIndent() > 0) {
            para.setFirstLineIndent(style.getFirstLineIndent().intValue() * 20);
        }

        // 行间距
        if (style.getLineSpacing() != null && style.getLineSpacing() > 0) {
            setLineSpacing(para, style.getLineSpacing());
        }

        // 段后间距
        if (style.getSpacingAfter() != null && style.getSpacingAfter() > 0) {
            para.setSpacingAfter(style.getSpacingAfter().intValue() * 20);
        }
    }

    /**
     * 应用默认正文格式
     */
    private void applyDefaultBodyFormat(XWPFParagraph para, XWPFRun run) {
        run.setFontFamily("宋体");
        run.setFontSize(12);
        run.setBold(false);

        // 首行缩进（约两个中文字符）
        para.setFirstLineIndent(480);

        // 1.5倍行距
        setLineSpacing(para, 1.5);
    }

    /**
     * 设置行间距
     */
    private void setLineSpacing(XWPFParagraph para, double lineSpacing) {
        try {
            CTPPr pPr = para.getCTP().getPPr();
            if (pPr == null) {
                pPr = para.getCTP().addNewPPr();
            }
            CTSpacing spacing = pPr.getSpacing();
            if (spacing == null) {
                spacing = pPr.addNewSpacing();
            }
            // 行距 = 倍数 * 240
            spacing.setLine(BigInteger.valueOf((long) (lineSpacing * 240)));
            spacing.setLineRule(STLineSpacingRule.AUTO);
        } catch (Exception e) {
            log.debug("设置行距失败: {}", e.getMessage());
        }
    }

    /**
     * 标准化标题文本（去除首尾空白）
     */
    private String normalizeTitle(String title) {
        if (title == null) {
            return "";
        }
        return title.trim();
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
