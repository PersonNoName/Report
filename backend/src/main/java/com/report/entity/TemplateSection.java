package com.report.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

/**
 * 模板章节结构表（核心扩展表）
 * 在这里配置标题，前端根据此表动态渲染菜单
 */
@Data
@TableName("template_section")
public class TemplateSection {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 关联 report_template
     */
    private Long templateId;

    /**
     * 唯一标识，对应Word模板里的占位符 e.g., "weekly_summary"
     */
    private String sectionKey;

    /**
     * 标题名称 e.g., "本周工作总结"
     */
    private String title;

    /**
     * 类型：RICH_TEXT(富文本), TABLE(表格), CHART(图表)
     */
    private String sectionType;

    /**
     * 排序权重，决定在前端和Word中的顺序
     */
    private Integer sortOrder;

    /**
     * 支持多级标题
     */
    private Long parentId;

    /**
     * 软删除标记
     */
    private Boolean isActive;
}
