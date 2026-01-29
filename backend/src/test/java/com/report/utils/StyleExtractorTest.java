package com.report.utils;

import com.report.entity.TemplateStyle;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * StyleExtractor 单元测试
 */
@DisplayName("样式提取工具测试")
class StyleExtractorTest {

    @Test
    @DisplayName("创建默认样式 - 应返回5个样式配置")
    void testCreateDefaultStyles() {
        Long templateId = 1L;

        List<TemplateStyle> styles = StyleExtractor.createDefaultStyles(templateId);

        assertNotNull(styles, "样式列表不应为空");
        assertEquals(5, styles.size(), "应有5个样式（4级标题 + 1正文）");

        // 验证所有样式都关联正确的模板ID
        for (TemplateStyle style : styles) {
            assertEquals(templateId, style.getTemplateId(), "模板ID应匹配");
        }
    }

    @Test
    @DisplayName("默认样式 - 应包含正确的样式类型")
    void testDefaultStylesContainCorrectTypes() {
        Long templateId = 1L;

        List<TemplateStyle> styles = StyleExtractor.createDefaultStyles(templateId);

        // 验证样式类型
        assertTrue(styles.stream().anyMatch(s -> "HEADING_1".equals(s.getStyleType())),
                "应包含 HEADING_1");
        assertTrue(styles.stream().anyMatch(s -> "HEADING_2".equals(s.getStyleType())),
                "应包含 HEADING_2");
        assertTrue(styles.stream().anyMatch(s -> "HEADING_3".equals(s.getStyleType())),
                "应包含 HEADING_3");
        assertTrue(styles.stream().anyMatch(s -> "HEADING_4".equals(s.getStyleType())),
                "应包含 HEADING_4");
        assertTrue(styles.stream().anyMatch(s -> "BODY".equals(s.getStyleType())),
                "应包含 BODY");
    }

    @Test
    @DisplayName("默认标题样式 - 字号应从大到小递减")
    void testHeadingFontSizesDecrease() {
        Long templateId = 1L;

        List<TemplateStyle> styles = StyleExtractor.createDefaultStyles(templateId);

        Integer heading1Size = styles.stream()
                .filter(s -> "HEADING_1".equals(s.getStyleType()))
                .findFirst().map(TemplateStyle::getFontSize).orElse(0);

        Integer heading2Size = styles.stream()
                .filter(s -> "HEADING_2".equals(s.getStyleType()))
                .findFirst().map(TemplateStyle::getFontSize).orElse(0);

        Integer heading3Size = styles.stream()
                .filter(s -> "HEADING_3".equals(s.getStyleType()))
                .findFirst().map(TemplateStyle::getFontSize).orElse(0);

        Integer heading4Size = styles.stream()
                .filter(s -> "HEADING_4".equals(s.getStyleType()))
                .findFirst().map(TemplateStyle::getFontSize).orElse(0);

        assertTrue(heading1Size > heading2Size, "Heading 1 应大于 Heading 2");
        assertTrue(heading2Size > heading3Size, "Heading 2 应大于 Heading 3");
        assertTrue(heading3Size > heading4Size, "Heading 3 应大于 Heading 4");
    }

    @Test
    @DisplayName("默认标题样式 - 应为加粗")
    void testHeadingStylesBold() {
        Long templateId = 1L;

        List<TemplateStyle> styles = StyleExtractor.createDefaultStyles(templateId);

        for (int i = 1; i <= 4; i++) {
            int level = i;
            TemplateStyle heading = styles.stream()
                    .filter(s -> ("HEADING_" + level).equals(s.getStyleType()))
                    .findFirst()
                    .orElse(null);

            assertNotNull(heading, "应存在 HEADING_" + level);
            assertTrue(heading.getBold(), "HEADING_" + level + " 应为加粗");
        }
    }

    @Test
    @DisplayName("默认正文样式 - 不应加粗")
    void testBodyStyleNotBold() {
        Long templateId = 1L;

        List<TemplateStyle> styles = StyleExtractor.createDefaultStyles(templateId);

        TemplateStyle body = styles.stream()
                .filter(s -> "BODY".equals(s.getStyleType()))
                .findFirst()
                .orElse(null);

        assertNotNull(body, "应存在 BODY 样式");
        assertFalse(body.getBold(), "正文不应加粗");
        assertEquals("宋体", body.getFontFamily(), "正文应使用宋体");
        assertEquals(12, body.getFontSize(), "正文字号应为12");
    }

    @Test
    @DisplayName("默认样式 - 应有行间距设置")
    void testDefaultStylesHaveLineSpacing() {
        Long templateId = 1L;

        List<TemplateStyle> styles = StyleExtractor.createDefaultStyles(templateId);

        for (TemplateStyle style : styles) {
            assertNotNull(style.getLineSpacing(),
                    style.getStyleType() + " 应有行间距设置");
            assertTrue(style.getLineSpacing() > 0,
                    style.getStyleType() + " 行间距应大于0");
        }
    }
}
