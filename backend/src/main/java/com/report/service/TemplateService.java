package com.report.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.report.entity.ReportTemplate;
import com.report.entity.TemplateSection;
import com.report.entity.TemplateStyle;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface TemplateService extends IService<ReportTemplate> {

    /**
     * 从上传的 Word 模板中提取样式并保存
     * 
     * @param templateId 模板 ID
     * @param file       上传的 Word 文件
     * @return 提取并保存的样式列表
     */
    List<TemplateStyle> extractAndSaveStyles(Long templateId, MultipartFile file);

    /**
     * 获取模板的样式配置
     * 
     * @param templateId 模板 ID
     * @return 样式列表
     */
    List<TemplateStyle> getTemplateStyles(Long templateId);

    /**
     * 获取模板的样式配置（按样式类型映射）
     * 
     * @param templateId 模板 ID
     * @return 样式类型 -> 样式配置的映射
     */
    Map<String, TemplateStyle> getTemplateStyleMap(Long templateId);

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

    /**
     * 创建模板及章节
     */
    ReportTemplate createTemplateWithSections(ReportTemplate template,
            List<com.report.utils.WordUtil.SectionNode> sectionNodes);
}
