package com.report.utils;

import com.report.entity.TemplateStyle;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xwpf.usermodel.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Word 模板样式提取工具
 * 从上传的 Word 文档中提取标题和正文的样式配置
 * 包括提取 Word 内置样式 ID 用于 paragraph.setStyle()
 */
@Slf4j
public class StyleExtractor {

    // Word 中的样式 ID 映射（支持中英文）
    private static final Map<String, String> HEADING_STYLE_MAP = new HashMap<>();

    // 样式 ID 到层级的映射
    private static final Map<String, Integer> STYLE_ID_TO_LEVEL = new HashMap<>();

    static {
        // 英文标题样式 ID
        HEADING_STYLE_MAP.put("1", "HEADING_1");
        HEADING_STYLE_MAP.put("2", "HEADING_2");
        HEADING_STYLE_MAP.put("3", "HEADING_3");
        HEADING_STYLE_MAP.put("4", "HEADING_4");
        HEADING_STYLE_MAP.put("heading1", "HEADING_1");
        HEADING_STYLE_MAP.put("heading2", "HEADING_2");
        HEADING_STYLE_MAP.put("heading3", "HEADING_3");
        HEADING_STYLE_MAP.put("heading4", "HEADING_4");
        HEADING_STYLE_MAP.put("Heading1", "HEADING_1");
        HEADING_STYLE_MAP.put("Heading2", "HEADING_2");
        HEADING_STYLE_MAP.put("Heading3", "HEADING_3");
        HEADING_STYLE_MAP.put("Heading4", "HEADING_4");
        HEADING_STYLE_MAP.put("Heading 1", "HEADING_1");
        HEADING_STYLE_MAP.put("Heading 2", "HEADING_2");
        HEADING_STYLE_MAP.put("Heading 3", "HEADING_3");
        HEADING_STYLE_MAP.put("Heading 4", "HEADING_4");

        // 中文 Word 样式 ID（简体中文Office）
        HEADING_STYLE_MAP.put("标题1", "HEADING_1");
        HEADING_STYLE_MAP.put("标题2", "HEADING_2");
        HEADING_STYLE_MAP.put("标题3", "HEADING_3");
        HEADING_STYLE_MAP.put("标题4", "HEADING_4");
        HEADING_STYLE_MAP.put("标题 1", "HEADING_1");
        HEADING_STYLE_MAP.put("标题 2", "HEADING_2");
        HEADING_STYLE_MAP.put("标题 3", "HEADING_3");
        HEADING_STYLE_MAP.put("标题 4", "HEADING_4");

        // 反向映射：样式 ID -> 层级
        STYLE_ID_TO_LEVEL.put("1", 1);
        STYLE_ID_TO_LEVEL.put("2", 2);
        STYLE_ID_TO_LEVEL.put("3", 3);
        STYLE_ID_TO_LEVEL.put("4", 4);
        STYLE_ID_TO_LEVEL.put("Heading1", 1);
        STYLE_ID_TO_LEVEL.put("Heading2", 2);
        STYLE_ID_TO_LEVEL.put("Heading3", 3);
        STYLE_ID_TO_LEVEL.put("Heading4", 4);
        STYLE_ID_TO_LEVEL.put("heading1", 1);
        STYLE_ID_TO_LEVEL.put("heading2", 2);
        STYLE_ID_TO_LEVEL.put("heading3", 3);
        STYLE_ID_TO_LEVEL.put("heading4", 4);
    }

    /**
     * 从 Word 模板文件中提取样式配置
     *
     * @param file       上传的 Word 模板文件
     * @param templateId 关联的模板 ID
     * @return 提取的样式列表
     */
    public static List<TemplateStyle> extractStyles(MultipartFile file, Long templateId) throws IOException {
        return extractStyles(file.getInputStream(), templateId);
    }

    /**
     * 从输入流中提取样式配置
     */
    public static List<TemplateStyle> extractStyles(InputStream inputStream, Long templateId) throws IOException {
        Map<String, TemplateStyle> styleMap = new HashMap<>();
        Map<String, String> detectedStyleIds = new HashMap<>(); // styleType -> wordStyleId

        try (XWPFDocument doc = new XWPFDocument(inputStream)) {
            // 1. 首先从文档样式定义中探测可用的样式 ID
            detectAvailableStyles(doc, detectedStyleIds);

            // 2. 从文档段落中推断样式
            extractFromParagraphs(doc, templateId, styleMap, detectedStyleIds);

            // 3. 填充缺失的样式
            fillMissingStyles(templateId, styleMap, detectedStyleIds);
        } catch (Exception e) {
            log.warn("提取样式时出错，使用默认样式: {}", e.getMessage());
            return createDefaultStyles(templateId);
        }

        log.info("从模板提取了 {} 个样式配置", styleMap.size());
        return new ArrayList<>(styleMap.values());
    }

    /**
     * 探测文档中可用的样式 ID
     */
    private static void detectAvailableStyles(XWPFDocument doc, Map<String, String> detectedStyleIds) {
        try {
            XWPFStyles styles = doc.getStyles();
            if (styles == null) {
                return;
            }

            // 检查可能的样式 ID
            for (int level = 1; level <= 4; level++) {
                String styleType = "HEADING_" + level;
                String foundStyleId = null;

                // 尝试不同的样式 ID
                List<String> candidates = List.of(
                        "Heading" + level,
                        String.valueOf(level),
                        "标题" + level,
                        "标题 " + level,
                        "heading" + level);

                for (String candidate : candidates) {
                    try {
                        XWPFStyle style = styles.getStyle(candidate);
                        if (style != null) {
                            foundStyleId = candidate;
                            log.debug("发现样式: {} -> {}", styleType, foundStyleId);
                            break;
                        }
                    } catch (Exception e) {
                        // 继续尝试
                    }
                }

                if (foundStyleId != null) {
                    detectedStyleIds.put(styleType, foundStyleId);
                }
            }

            // 检查正文样式
            List<String> bodyCandidates = List.of("Normal", "a", "正文", "body");
            for (String candidate : bodyCandidates) {
                try {
                    XWPFStyle style = styles.getStyle(candidate);
                    if (style != null) {
                        detectedStyleIds.put("BODY", candidate);
                        log.debug("发现正文样式: {}", candidate);
                        break;
                    }
                } catch (Exception e) {
                    // 继续尝试
                }
            }

        } catch (Exception e) {
            log.debug("样式探测出错: {}", e.getMessage());
        }
    }

    /**
     * 从文档段落中推断样式
     */
    private static void extractFromParagraphs(XWPFDocument doc, Long templateId,
            Map<String, TemplateStyle> styleMap,
            Map<String, String> detectedStyleIds) {
        for (XWPFParagraph paragraph : doc.getParagraphs()) {
            String styleId = paragraph.getStyleID();
            String styleType = null;

            if (styleId != null && !styleId.isEmpty()) {
                styleType = HEADING_STYLE_MAP.get(styleId);
                if (styleType == null) {
                    styleType = matchStyleType(styleId);
                }
            } else {
                // 无样式ID的段落视为正文
                if (!styleMap.containsKey("BODY") && !paragraph.getText().trim().isEmpty()) {
                    styleType = "BODY";
                }
            }

            if (styleType != null && !styleMap.containsKey(styleType)) {
                TemplateStyle ts = extractFromParagraph(paragraph, templateId, styleType, styleId);
                if (ts != null) {
                    // 使用探测到的样式 ID 或原始样式 ID
                    String wordStyleId = detectedStyleIds.getOrDefault(styleType, styleId);
                    ts.setWordStyleId(wordStyleId);

                    styleMap.put(styleType, ts);
                    log.debug("从段落提取样式: {} -> {}, wordStyleId: {}", styleId, styleType, wordStyleId);
                }
            }
        }
    }

    /**
     * 从段落提取样式
     */
    private static TemplateStyle extractFromParagraph(XWPFParagraph paragraph, Long templateId,
            String styleType, String originalStyleId) {
        TemplateStyle ts = new TemplateStyle();
        ts.setTemplateId(templateId);
        ts.setStyleType(styleType);

        // 从段落的第一个 Run 获取字符属性
        List<XWPFRun> runs = paragraph.getRuns();
        if (!runs.isEmpty()) {
            XWPFRun run = runs.get(0);

            // 字体
            String fontFamily = run.getFontFamily();
            if (fontFamily != null && !fontFamily.isEmpty()) {
                ts.setFontFamily(fontFamily);
            }

            // 字号
            int fontSize = run.getFontSize();
            if (fontSize > 0) {
                ts.setFontSize(fontSize);
            }

            // 加粗
            ts.setBold(run.isBold());

            // 斜体
            ts.setItalic(run.isItalic());

            // 颜色
            String color = run.getColor();
            if (color != null) {
                ts.setFontColor(color);
            }
        }

        // 段落间距
        int spacingAfter = paragraph.getSpacingAfter();
        if (spacingAfter > 0) {
            ts.setSpacingAfter((double) spacingAfter / 20.0);
        }

        int spacingBefore = paragraph.getSpacingBefore();
        if (spacingBefore > 0) {
            ts.setSpacingBefore((double) spacingBefore / 20.0);
        }

        // 行间距
        double lineSpacing = paragraph.getSpacingBetween();
        if (lineSpacing > 0) {
            ts.setLineSpacing(lineSpacing);
        }

        // 对齐方式
        ParagraphAlignment alignment = paragraph.getAlignment();
        if (alignment != null) {
            switch (alignment) {
                case LEFT -> ts.setAlignment("LEFT");
                case CENTER -> ts.setAlignment("CENTER");
                case RIGHT -> ts.setAlignment("RIGHT");
                case BOTH -> ts.setAlignment("JUSTIFY");
                default -> {
                }
            }
        }

        // 首行缩进
        int firstLineIndent = paragraph.getFirstLineIndent();
        if (firstLineIndent > 0) {
            ts.setFirstLineIndent((double) firstLineIndent / 20.0);
        }

        return ts;
    }

    /**
     * 匹配样式类型
     */
    private static String matchStyleType(String styleId) {
        if (styleId == null)
            return null;

        String lower = styleId.toLowerCase();

        // 匹配标题样式
        for (int i = 1; i <= 4; i++) {
            if (lower.contains("heading" + i) || lower.contains("标题" + i) ||
                    lower.equals(String.valueOf(i))) {
                return "HEADING_" + i;
            }
        }

        // 匹配正文样式
        if (lower.contains("normal") || lower.contains("正文") ||
                lower.equals("a") || lower.equals("body")) {
            return "BODY";
        }

        return null;
    }

    /**
     * 填充缺失的样式
     */
    private static void fillMissingStyles(Long templateId, Map<String, TemplateStyle> styleMap,
            Map<String, String> detectedStyleIds) {
        // 确保有 4 级标题样式
        for (int i = 1; i <= 4; i++) {
            String styleType = "HEADING_" + i;
            if (!styleMap.containsKey(styleType)) {
                TemplateStyle defaultStyle = TemplateStyle.createDefaultHeading(templateId, i);
                // 设置默认的 Word 样式 ID
                String wordStyleId = detectedStyleIds.getOrDefault(styleType, "Heading" + i);
                defaultStyle.setWordStyleId(wordStyleId);
                styleMap.put(styleType, defaultStyle);
                log.debug("使用默认样式: {}, wordStyleId: {}", styleType, wordStyleId);
            }
        }

        // 确保有正文样式
        if (!styleMap.containsKey("BODY")) {
            TemplateStyle bodyStyle = TemplateStyle.createDefaultBody(templateId);
            String bodyStyleId = detectedStyleIds.getOrDefault("BODY", "Normal");
            bodyStyle.setWordStyleId(bodyStyleId);
            styleMap.put("BODY", bodyStyle);
            log.debug("使用默认正文样式, wordStyleId: {}", bodyStyleId);
        }
    }

    /**
     * 创建默认样式集
     */
    public static List<TemplateStyle> createDefaultStyles(Long templateId) {
        List<TemplateStyle> styles = new ArrayList<>();

        for (int i = 1; i <= 4; i++) {
            TemplateStyle heading = TemplateStyle.createDefaultHeading(templateId, i);
            heading.setWordStyleId("Heading" + i);
            styles.add(heading);
        }

        TemplateStyle body = TemplateStyle.createDefaultBody(templateId);
        body.setWordStyleId("Normal");
        styles.add(body);

        return styles;
    }
}
