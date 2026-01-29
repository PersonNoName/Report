package com.report.utils;

import com.report.entity.TemplateStyle;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xwpf.usermodel.*;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.*;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Word 样式管理器
 * 负责从模板读取样式、复制样式定义到新文档、提供样式映射
 */
@Slf4j
public class StyleManager {

    // 默认的样式 ID 映射（层级 -> 样式 ID）
    private static final Map<Integer, String> DEFAULT_HEADING_STYLE_IDS = new HashMap<>();
    private static final String DEFAULT_BODY_STYLE_ID = "Normal";

    static {
        // Word 默认的英文样式 ID
        DEFAULT_HEADING_STYLE_IDS.put(1, "Heading1");
        DEFAULT_HEADING_STYLE_IDS.put(2, "Heading2");
        DEFAULT_HEADING_STYLE_IDS.put(3, "Heading3");
        DEFAULT_HEADING_STYLE_IDS.put(4, "Heading4");
    }

    /**
     * 从模板文档中复制样式定义到目标文档
     * 如果模板不存在或读取失败，则确保目标文档有默认样式
     *
     * @param targetDoc    目标文档
     * @param templatePath 模板文件路径
     */
    public static void copyStylesFromTemplate(XWPFDocument targetDoc, Path templatePath) {
        if (templatePath == null || !Files.exists(templatePath)) {
            log.warn("模板文件不存在，使用默认样式");
            ensureDefaultStyles(targetDoc);
            return;
        }

        try (InputStream is = Files.newInputStream(templatePath);
                XWPFDocument templateDoc = new XWPFDocument(is)) {

            copyStyles(templateDoc, targetDoc);
            log.info("从模板复制了样式定义");

        } catch (IOException e) {
            log.warn("读取模板文件失败，使用默认样式: {}", e.getMessage());
            ensureDefaultStyles(targetDoc);
        }
    }

    /**
     * 从源文档复制样式到目标文档
     */
    private static void copyStyles(XWPFDocument source, XWPFDocument target) {
        try {
            XWPFStyles sourceStyles = source.getStyles();
            XWPFStyles targetStyles = target.getStyles();

            if (sourceStyles == null) {
                return;
            }

            // 确保目标文档有样式对象
            if (targetStyles == null) {
                target.createStyles();
                targetStyles = target.getStyles();
            }

            // 复制需要的样式
            String[] styleIds = { "Heading1", "Heading2", "Heading3", "Heading4", "Normal",
                    "1", "2", "3", "4", "a",
                    "标题1", "标题2", "标题3", "标题4", "正文" };

            for (String styleId : styleIds) {
                XWPFStyle style = sourceStyles.getStyle(styleId);
                if (style != null) {
                    try {
                        // 只有当目标文档没有这个样式时才复制
                        if (targetStyles.getStyle(styleId) == null) {
                            targetStyles.addStyle(style);
                        }
                    } catch (Exception e) {
                        log.debug("复制样式 {} 失败: {}", styleId, e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            log.warn("复制样式过程出错: {}", e.getMessage());
        }
    }

    /**
     * 确保文档有默认的标题样式
     * 如果样式不存在则创建
     */
    public static void ensureDefaultStyles(XWPFDocument doc) {
        try {
            XWPFStyles styles = doc.getStyles();
            if (styles == null) {
                doc.createStyles();
                styles = doc.getStyles();
            }

            // 创建标题样式
            for (int level = 1; level <= 4; level++) {
                String styleId = "Heading" + level;
                if (styles.getStyle(styleId) == null) {
                    createHeadingStyle(styles, level);
                }
            }

        } catch (Exception e) {
            log.warn("创建默认样式失败: {}", e.getMessage());
        }
    }

    /**
     * 创建标题样式
     */
    private static void createHeadingStyle(XWPFStyles styles, int level) {
        try {
            CTStyle ctStyle = CTStyle.Factory.newInstance();
            ctStyle.setStyleId("Heading" + level);

            // 设置样式名称
            CTString styleName = CTString.Factory.newInstance();
            styleName.setVal("Heading " + level);
            ctStyle.setName(styleName);

            // 设置为段落样式
            ctStyle.setType(STStyleType.PARAGRAPH);

            // 设置基于 Normal 样式
            CTString basedOn = CTString.Factory.newInstance();
            basedOn.setVal("Normal");
            ctStyle.setBasedOn(basedOn);

            // 用于目录
            ctStyle.setQFormat(CTOnOff.Factory.newInstance());

            // 设置段落属性 - 使用 CTPPrGeneral
            CTPPrGeneral pPr = ctStyle.addNewPPr();
            CTSpacing spacing = pPr.addNewSpacing();
            spacing.setBefore(BigInteger.valueOf(240)); // 12pt before
            spacing.setAfter(BigInteger.valueOf(120)); // 6pt after

            // 设置大纲级别（用于目录生成）
            CTDecimalNumber outlineLvl = pPr.addNewOutlineLvl();
            outlineLvl.setVal(BigInteger.valueOf(level - 1));

            // 设置字符属性
            CTRPr rPr = ctStyle.addNewRPr();

            // 粗体
            rPr.addNewB();

            // 字号根据层级递减
            int fontSize = 24 - (level - 1) * 2; // Heading1=24pt, Heading2=22pt, etc
            CTHpsMeasure sz = rPr.addNewSz();
            sz.setVal(BigInteger.valueOf(fontSize * 2)); // 半磅为单位
            CTHpsMeasure szCs = rPr.addNewSzCs();
            szCs.setVal(BigInteger.valueOf(fontSize * 2));

            // 添加到样式集合
            XWPFStyle xwpfStyle = new XWPFStyle(ctStyle, styles);
            styles.addStyle(xwpfStyle);

            log.debug("创建了默认样式: Heading{}", level);
        } catch (Exception e) {
            log.warn("创建 Heading{} 样式失败: {}", level, e.getMessage());
        }
    }

    /**
     * 获取层级对应的样式 ID
     * 优先使用数据库中保存的样式 ID，否则使用默认值
     */
    public static String getStyleId(int level, Map<String, TemplateStyle> styleMap) {
        String styleType = "HEADING_" + level;
        TemplateStyle style = styleMap.get(styleType);

        if (style != null && style.getWordStyleId() != null && !style.getWordStyleId().isEmpty()) {
            return style.getWordStyleId();
        }

        // 返回默认样式 ID
        return DEFAULT_HEADING_STYLE_IDS.getOrDefault(level, "Heading1");
    }

    /**
     * 获取正文样式 ID
     */
    public static String getBodyStyleId(Map<String, TemplateStyle> styleMap) {
        TemplateStyle style = styleMap.get("BODY");

        if (style != null && style.getWordStyleId() != null && !style.getWordStyleId().isEmpty()) {
            return style.getWordStyleId();
        }

        return DEFAULT_BODY_STYLE_ID;
    }

    /**
     * 安全地设置段落样式
     * 如果样式不存在，尝试使用替代样式
     */
    public static void applyStyle(XWPFParagraph paragraph, String styleId) {
        try {
            paragraph.setStyle(styleId);
        } catch (Exception e) {
            log.debug("样式 {} 设置失败，尝试替代方案", styleId);
            try {
                // 尝试使用英文样式名
                if (styleId.startsWith("标题")) {
                    String level = styleId.replace("标题", "").trim();
                    paragraph.setStyle("Heading" + level);
                } else if (styleId.equals("正文")) {
                    paragraph.setStyle("Normal");
                }
            } catch (Exception ex) {
                log.warn("无法设置样式: {}", styleId);
            }
        }
    }

    /**
     * 检查样式是否存在于文档中
     */
    public static boolean styleExists(XWPFDocument doc, String styleId) {
        try {
            XWPFStyles styles = doc.getStyles();
            return styles != null && styles.getStyle(styleId) != null;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 获取所有可能的样式 ID（用于探测模板中实际使用的样式）
     */
    public static List<String> getPossibleStyleIds(int level) {
        return switch (level) {
            case 1 -> List.of("Heading1", "1", "标题1", "标题 1", "heading1");
            case 2 -> List.of("Heading2", "2", "标题2", "标题 2", "heading2");
            case 3 -> List.of("Heading3", "3", "标题3", "标题 3", "heading3");
            case 4 -> List.of("Heading4", "4", "标题4", "标题 4", "heading4");
            default -> List.of("Normal", "a", "正文", "body");
        };
    }

    /**
     * 探测文档中实际存在的样式 ID
     */
    public static String detectStyleId(XWPFDocument doc, int level) {
        List<String> candidates = getPossibleStyleIds(level);

        for (String candidate : candidates) {
            if (styleExists(doc, candidate)) {
                return candidate;
            }
        }

        // 返回默认值
        return level > 0 ? "Heading" + level : "Normal";
    }
}
