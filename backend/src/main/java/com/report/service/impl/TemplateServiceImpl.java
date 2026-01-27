package com.report.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.report.entity.ReportTemplate;
import com.report.entity.TemplateSection;
import com.report.mapper.ReportTemplateMapper;
import com.report.mapper.TemplateSectionMapper;
import com.report.service.TemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TemplateServiceImpl extends ServiceImpl<ReportTemplateMapper, ReportTemplate> implements TemplateService {

    private final TemplateSectionMapper sectionMapper;

    @Override
    public List<ReportTemplate> getActiveTemplates() {
        return list(new LambdaQueryWrapper<ReportTemplate>()
                .eq(ReportTemplate::getIsActive, true)
                .orderByDesc(ReportTemplate::getCreatedAt));
    }

    @Override
    public List<TemplateSection> getTemplateSections(Long templateId) {
        return sectionMapper.selectByTemplateIdOrdered(templateId);
    }

    @Override
    public TemplateSection addSection(Long templateId, TemplateSection section) {
        section.setTemplateId(templateId);
        section.setIsActive(true);
        if (section.getSortOrder() == null) {
            section.setSortOrder(0);
        }
        sectionMapper.insert(section);
        return section;
    }

    @Override
    public TemplateSection updateSection(Long sectionId, TemplateSection section) {
        section.setId(sectionId);
        sectionMapper.updateById(section);
        return sectionMapper.selectById(sectionId);
    }

    @Override
    public void deleteSection(Long sectionId) {
        TemplateSection section = new TemplateSection();
        section.setId(sectionId);
        section.setIsActive(false);
        sectionMapper.updateById(section);
    }

    @Override
    @org.springframework.transaction.annotation.Transactional(rollbackFor = Exception.class)
    public ReportTemplate createTemplateWithSections(ReportTemplate template, List<String> sectionTitles) {
        // 1. Save template
        template.setIsActive(true);
        save(template);

        // 2. Save sections
        if (sectionTitles != null && !sectionTitles.isEmpty()) {
            for (int i = 0; i < sectionTitles.size(); i++) {
                TemplateSection section = new TemplateSection();
                section.setTemplateId(template.getId());
                section.setTitle(sectionTitles.get(i));
                section.setSectionKey("section_" + System.currentTimeMillis() + "_" + i); // Auto-generate key
                section.setSectionType("RICH_TEXT");
                section.setSortOrder(i + 1);
                section.setIsActive(true);
                sectionMapper.insert(section);
            }
        }
        return template;
    }
}
