package com.report.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.report.entity.ReportTemplate;
import com.report.entity.TemplateSection;
import com.report.entity.TemplateStyle;
import com.report.mapper.ReportTemplateMapper;
import com.report.mapper.TemplateSectionMapper;
import com.report.mapper.TemplateStyleMapper;
import com.report.service.TemplateService;
import com.report.utils.StyleExtractor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TemplateServiceImpl extends ServiceImpl<ReportTemplateMapper, ReportTemplate> implements TemplateService {

    private final TemplateSectionMapper sectionMapper;
    private final TemplateStyleMapper styleMapper;

    @Value("${file.upload-dir:./uploads}")
    private String uploadDir;

    /**
     * 从文件路径提取并保存样式（用于模板创建后）
     */
    public List<TemplateStyle> extractAndSaveStylesFromPath(Long templateId, String templateFileName) {
        try {
            Path filePath = Paths.get(uploadDir, "templates", templateFileName);
            if (!Files.exists(filePath)) {
                log.warn("模板文件不存在: {}, 使用默认样式", filePath);
                return saveDefaultStyles(templateId);
            }

            // 删除旧样式
            styleMapper.deleteByTemplateId(templateId);

            // 从文件提取样式
            try (InputStream is = Files.newInputStream(filePath)) {
                List<TemplateStyle> styles = StyleExtractor.extractStyles(is, templateId);
                for (TemplateStyle style : styles) {
                    styleMapper.insert(style);
                }
                log.info("为模板 {} 从文件提取并保存了 {} 个样式配置", templateId, styles.size());
                return styles;
            }
        } catch (Exception e) {
            log.error("从文件提取样式失败: {}", e.getMessage(), e);
            return saveDefaultStyles(templateId);
        }
    }

    private List<TemplateStyle> saveDefaultStyles(Long templateId) {
        List<TemplateStyle> defaultStyles = StyleExtractor.createDefaultStyles(templateId);
        for (TemplateStyle style : defaultStyles) {
            styleMapper.insert(style);
        }
        return defaultStyles;
    }

    @Override
    public List<TemplateStyle> extractAndSaveStyles(Long templateId, MultipartFile file) {
        try {
            // 1. 先删除模板的旧样式配置
            styleMapper.deleteByTemplateId(templateId);

            // 2. 从上传的文件中提取样式
            List<TemplateStyle> styles = StyleExtractor.extractStyles(file, templateId);

            // 3. 保存新的样式配置
            for (TemplateStyle style : styles) {
                styleMapper.insert(style);
            }

            log.info("为模板 {} 保存了 {} 个样式配置", templateId, styles.size());
            return styles;
        } catch (IOException e) {
            log.error("提取样式失败: {}", e.getMessage(), e);
            // 返回默认样式
            List<TemplateStyle> defaultStyles = StyleExtractor.createDefaultStyles(templateId);
            for (TemplateStyle style : defaultStyles) {
                styleMapper.insert(style);
            }
            return defaultStyles;
        }
    }

    @Override
    public List<TemplateStyle> getTemplateStyles(Long templateId) {
        List<TemplateStyle> styles = styleMapper.selectByTemplateId(templateId);
        if (styles == null || styles.isEmpty()) {
            // 如果没有样式配置，返回默认样式（但不保存）
            return StyleExtractor.createDefaultStyles(templateId);
        }
        return styles;
    }

    @Override
    public Map<String, TemplateStyle> getTemplateStyleMap(Long templateId) {
        List<TemplateStyle> styles = getTemplateStyles(templateId);
        return styles.stream()
                .collect(Collectors.toMap(
                        TemplateStyle::getStyleType,
                        style -> style,
                        (s1, s2) -> s1 // 如果有重复，保留第一个
                ));
    }

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

        // 3. 自动提取并保存样式（从上传的 Word 文件）
        if (template.getBaseDocxUrl() != null && !template.getBaseDocxUrl().isEmpty()) {
            extractAndSaveStylesFromPath(template.getId(), template.getBaseDocxUrl());
        } else {
            // 没有关联 Word 文件，仍然保存默认样式
            saveDefaultStyles(template.getId());
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
