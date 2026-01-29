package com.report.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * 模板样式配置实体
 * 存储从上传的 Word 模板中提取的样式信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@TableName("template_style")
public class TemplateStyle {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 关联模板 ID
     */
    private Long templateId;

    /**
     * 样式类型: HEADING_1, HEADING_2, HEADING_3, HEADING_4, BODY
     */
    private String styleType;

    /**
     * 字体名称
     */
    private String fontFamily;

    /**
     * 字号（磅）
     */
    private Integer fontSize;

    /**
     * 是否加粗
     */
    private Boolean bold;

    /**
     * 是否斜体
     */
    private Boolean italic;

    /**
     * 字体颜色（十六进制，如 "000000"）
     */
    private String fontColor;

    /**
     * 行间距（倍数，如 1.5）
     */
    private Double lineSpacing;

    /**
     * 段前间距（磅）
     */
    private Double spacingBefore;

    /**
     * 段后间距（磅）
     */
    private Double spacingAfter;

    /**
     * 对齐方式: LEFT, CENTER, RIGHT, JUSTIFY
     */
    private String alignment;

    /**
     * 首行缩进（磅）
     */
    private Double firstLineIndent;

    /**
     * Word 内置样式 ID，如 "Heading1", "1", "标题1" 等
     * 用于通过 paragraph.setStyle() 设置段落样式
     */
    private String wordStyleId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    /**
     * 样式类型枚举
     */
    public enum StyleType {
        HEADING_1("HEADING_1", 1),
        HEADING_2("HEADING_2", 2),
        HEADING_3("HEADING_3", 3),
        HEADING_4("HEADING_4", 4),
        BODY("BODY", 0);

        private final String code;
        private final int level;

        StyleType(String code, int level) {
            this.code = code;
            this.level = level;
        }

        public String getCode() {
            return code;
        }

        public int getLevel() {
            return level;
        }

        public static StyleType fromLevel(int level) {
            for (StyleType type : values()) {
                if (type.level == level) {
                    return type;
                }
            }
            return BODY;
        }

        public static StyleType fromCode(String code) {
            for (StyleType type : values()) {
                if (type.code.equals(code)) {
                    return type;
                }
            }
            return BODY;
        }
    }

    /**
     * 创建默认的标题样式
     */
    public static TemplateStyle createDefaultHeading(Long templateId, int level) {
        TemplateStyle style = new TemplateStyle();
        style.setTemplateId(templateId);
        style.setStyleType("HEADING_" + level);
        style.setFontFamily("黑体");
        style.setBold(true);
        style.setLineSpacing(1.5);

        switch (level) {
            case 1:
                style.setFontSize(22);
                style.setSpacingAfter(12.0);
                break;
            case 2:
                style.setFontSize(18);
                style.setSpacingAfter(10.0);
                break;
            case 3:
                style.setFontSize(16);
                style.setSpacingAfter(8.0);
                break;
            case 4:
                style.setFontSize(14);
                style.setSpacingAfter(6.0);
                break;
            default:
                style.setFontSize(12);
                style.setSpacingAfter(6.0);
        }

        return style;
    }

    /**
     * 创建默认的正文样式
     */
    public static TemplateStyle createDefaultBody(Long templateId) {
        TemplateStyle style = new TemplateStyle();
        style.setTemplateId(templateId);
        style.setStyleType("BODY");
        style.setFontFamily("宋体");
        style.setFontSize(12);
        style.setBold(false);
        style.setLineSpacing(1.5);
        style.setSpacingAfter(6.0);
        style.setFirstLineIndent(24.0); // 两个字符的缩进
        return style;
    }
}
