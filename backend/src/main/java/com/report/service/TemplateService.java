package com.report.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.report.entity.ReportTemplate;
import com.report.entity.TemplateSection;

import java.util.List;

public interface TemplateService extends IService<ReportTemplate> {

    /**
     * 获取所有激活的模板
     */
    List<ReportTemplate> getActiveTemplates();

    /**
     * 获取模板的章节结构
     */
    List<TemplateSection> getTemplateSections(Long templateId);

    /**
     * 添加章节到模板
     */
    TemplateSection addSection(Long templateId, TemplateSection section);

    /**
     * 更新章节
     */
    TemplateSection updateSection(Long sectionId, TemplateSection section);

    /**
     * 删除章节（软删除）
     */
    void deleteSection(Long sectionId);
}
