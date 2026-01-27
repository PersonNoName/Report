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
    public ReportTemplate createTemplateWithSections(ReportTemplate template,
            List<com.report.utils.WordUtil.SectionNode> sectionNodes) {
        // 1. Save template
        template.setIsActive(true);
        save(template);

        // 2. Save sections recursively
        if (sectionNodes != null && !sectionNodes.isEmpty()) {
            java.util.Set<String> usedKeys = new java.util.HashSet<>();
            saveSectionsRecursive(template.getId(), null, sectionNodes, usedKeys);
        }
        return template;
    }

    private void saveSectionsRecursive(Long templateId, Long parentId,
            List<com.report.utils.WordUtil.SectionNode> nodes, java.util.Set<String> usedKeys) {
        if (nodes == null || nodes.isEmpty()) {
            return;
        }

        for (int i = 0; i < nodes.size(); i++) {
            com.report.utils.WordUtil.SectionNode node = nodes.get(i);

            TemplateSection section = new TemplateSection();
            section.setTemplateId(templateId);
            section.setParentId(parentId);
            section.setTitle(node.getTitle());

            // Generate semantic key for POI-TL
            String baseKey = generateSemanticKey(node.getTitle());
            String uniqueKey = baseKey;
            int counter = 1;
            while (usedKeys.contains(uniqueKey)) {
                uniqueKey = baseKey + "_" + counter++;
            }
            usedKeys.add(uniqueKey);

            section.setSectionKey(uniqueKey);
            section.setSectionType("RICH_TEXT");
            section.setSortOrder(i + 1);
            section.setIsActive(true);

            sectionMapper.insert(section);

            // Recurse for children
            if (node.getChildren() != null && !node.getChildren().isEmpty()) {
                saveSectionsRecursive(templateId, section.getId(), node.getChildren(), usedKeys);
            }
        }
    }

    private String generateSemanticKey(String title) {
        if (title == null)
            return "section";
        // Convert to Pinyin? Too complex without library.
        // Just strip special chars. Keep Chinese.
        // Replace whitespace with underscore.
        String key = title.trim().replaceAll("\\s+", "_");
        // Remove non-word chars (keeping Chinese, alphanumeric, underscore)
        // \w matches [a-zA-Z0-9_]. Java regex for chinese is \u4e00-\u9fa5
        key = key.replaceAll("[^\\w\u4e00-\u9fa5]", "");
        if (key.isEmpty())
            return "section";
        return key;
    }
}
