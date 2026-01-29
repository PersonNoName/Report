package com.report.policy;

import com.report.dto.SectionData;
import com.report.entity.TemplateStyle;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SectionRenderPolicy 单元测试
 */
@DisplayName("章节渲染策略测试")
class SectionRenderPolicyTest {

    @Test
    @DisplayName("默认构造函数 - 应创建空样式配置")
    void testDefaultConstructor() {
        SectionRenderPolicy policy = new SectionRenderPolicy();
        assertNotNull(policy, "应成功创建策略实例");
    }

    @Test
    @DisplayName("带样式配置构造函数 - 应接受样式映射")
    void testConstructorWithStyleConfig() {
        Map<String, TemplateStyle> styleConfig = new HashMap<>();

        TemplateStyle heading1 = new TemplateStyle();
        heading1.setStyleType("HEADING_1");
        heading1.setFontFamily("黑体");
        heading1.setFontSize(22);
        heading1.setBold(true);
        styleConfig.put("HEADING_1", heading1);

        TemplateStyle body = new TemplateStyle();
        body.setStyleType("BODY");
        body.setFontFamily("宋体");
        body.setFontSize(12);
        styleConfig.put("BODY", body);

        SectionRenderPolicy policy = new SectionRenderPolicy(styleConfig);
        assertNotNull(policy, "应成功创建带样式配置的策略实例");
    }

    @Test
    @DisplayName("null样式配置 - 应使用空映射")
    void testConstructorWithNullConfig() {
        SectionRenderPolicy policy = new SectionRenderPolicy(null);
        assertNotNull(policy, "传入null应创建使用空配置的策略实例");
    }

    @Test
    @DisplayName("SectionData - 应正确存储数据")
    void testSectionDataFields() {
        SectionData data = new SectionData();
        data.setTitle("测试标题");
        data.setContent("测试内容");
        data.setLevel(1);

        assertEquals("测试标题", data.getTitle());
        assertEquals("测试内容", data.getContent());
        assertEquals(1, data.getLevel());
        assertNotNull(data.getChildren());
        assertTrue(data.getChildren().isEmpty());
    }

    @Test
    @DisplayName("SectionData构造器 - 应正确初始化")
    void testSectionDataConstructor() {
        SectionData data = new SectionData("标题", "内容", 2);

        assertEquals("标题", data.getTitle());
        assertEquals("内容", data.getContent());
        assertEquals(2, data.getLevel());
    }
}
