package com.report.service.impl;

import com.deepoove.poi.XWPFTemplate;
import com.deepoove.poi.config.Configure;
import com.report.dto.SectionData;
import com.report.entity.ReportContent;
import com.report.entity.ReportInstance;
import com.report.entity.ReportTemplate;
import com.report.entity.TemplateSection;
import com.report.mapper.ReportContentMapper;
import com.report.mapper.ReportInstanceMapper;
import com.report.policy.SectionRenderPolicy;
import com.report.service.ExportService;
import com.report.service.TemplateService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExportServiceImpl implements ExportService {

    private final ReportInstanceMapper reportMapper;
    private final ReportContentMapper contentMapper;
    private final TemplateService templateService;

    @Value("${file.template-dir:./templates}")
    private String templateDir;

    @Value("${file.upload-dir:./uploads}")
    private String uploadDir;

    @Override
    public void exportToWord(Long reportId, HttpServletResponse response) throws Exception {
        // 1. 加载报告实例
        ReportInstance report = reportMapper.selectById(reportId);
        if (report == null) {
            throw new RuntimeException("报告不存在: " + reportId);
        }

        // 2. 加载报告内容
        List<ReportContent> contents = contentMapper.selectByReportInstanceId(reportId);
        Map<String, String> contentMap = contents.stream()
                .collect(Collectors.toMap(
                        ReportContent::getSectionKey,
                        c -> c.getContentHtml() != null ? c.getContentHtml() : "",
                        (v1, v2) -> v1)); // 处理重复 key

        // 3. 加载模板章节结构
        List<TemplateSection> sections = templateService.getTemplateSections(report.getTemplateId());
        List<SectionData> sectionTree = buildSectionDataTree(sections, contentMap);

        // 4. 准备渲染数据
        Map<String, Object> data = new HashMap<>();
        data.put("reportName", report.getReportName());
        data.put("dateRange", formatDateRange(report));
        data.put("sections", sectionTree);

        // 5. 配置 POI-TL
        Configure config = Configure.builder()
                .bind("sections", new SectionRenderPolicy())
                .build();

        // 6. 加载 Word 模板
        InputStream templateStream = loadTemplate(report.getTemplateId());

        // 7. 渲染并输出
        try (XWPFTemplate template = XWPFTemplate.compile(templateStream, config).render(data)) {
            String fileName = URLEncoder.encode(report.getReportName() + ".docx", StandardCharsets.UTF_8);
            response.setContentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
            template.writeAndClose(response.getOutputStream());
        } finally {
            if (templateStream != null) {
                templateStream.close();
            }
        }
    }

    /**
     * 加载 Word 模板文件
     * 优先使用用户上传的模板，否则使用默认模板
     */
    private InputStream loadTemplate(Long templateId) throws Exception {
        // 尝试加载用户上传的模板
        ReportTemplate reportTemplate = templateService.getById(templateId);
        if (reportTemplate != null && reportTemplate.getBaseDocxUrl() != null
                && !reportTemplate.getBaseDocxUrl().isEmpty()) {
            Path path = Paths.get(uploadDir, "templates", reportTemplate.getBaseDocxUrl());
            if (Files.exists(path)) {
                log.info("使用用户上传的模板: {}", path);
                return Files.newInputStream(path);
            }
        }

        // 尝试加载默认模板
        try {
            ClassPathResource resource = new ClassPathResource("templates/export_template.docx");
            if (resource.exists()) {
                log.info("使用默认导出模板: templates/export_template.docx");
                return resource.getInputStream();
            }
        } catch (Exception e) {
            log.warn("默认模板加载失败", e);
        }

        // 最后尝试旧的模板路径
        try {
            ClassPathResource resource = new ClassPathResource("templates/template.docx");
            if (resource.exists()) {
                log.info("使用旧版模板: templates/template.docx");
                return resource.getInputStream();
            }
        } catch (Exception e) {
            log.warn("旧版模板加载失败", e);
        }

        throw new RuntimeException("未找到可用的 Word 模板文件");
    }

    /**
     * 构建章节数据树
     */
    private List<SectionData> buildSectionDataTree(List<TemplateSection> sections,
            Map<String, String> contentMap) {
        // 构建 ID -> Section 映射
        Map<Long, TemplateSection> sectionMap = sections.stream()
                .collect(Collectors.toMap(TemplateSection::getId, s -> s));

        // 构建 ID -> SectionData 映射
        Map<Long, SectionData> dataMap = new HashMap<>();
        for (TemplateSection section : sections) {
            SectionData data = new SectionData();
            data.setTitle(section.getTitle());
            data.setContent(contentMap.getOrDefault(section.getSectionKey(), ""));
            data.setLevel(calculateLevel(section, sectionMap));
            data.setChildren(new ArrayList<>());
            dataMap.put(section.getId(), data);
        }

        // 构建树结构
        List<SectionData> roots = new ArrayList<>();
        for (TemplateSection section : sections) {
            SectionData data = dataMap.get(section.getId());
            if (section.getParentId() == null || !dataMap.containsKey(section.getParentId())) {
                roots.add(data);
            } else {
                dataMap.get(section.getParentId()).getChildren().add(data);
            }
        }

        // 排序
        sortSectionDataRecursive(roots, sections, dataMap);

        return roots;
    }

    /**
     * 计算章节层级
     */
    private int calculateLevel(TemplateSection section, Map<Long, TemplateSection> sectionMap) {
        int level = 1;
        Long parentId = section.getParentId();
        while (parentId != null && sectionMap.containsKey(parentId)) {
            level++;
            parentId = sectionMap.get(parentId).getParentId();
        }
        return level;
    }

    /**
     * 递归排序章节
     */
    private void sortSectionDataRecursive(List<SectionData> dataList,
            List<TemplateSection> sections,
            Map<Long, SectionData> dataMap) {
        // 创建 SectionData -> sortOrder 的映射
        Map<SectionData, Integer> sortOrderMap = new HashMap<>();
        for (TemplateSection section : sections) {
            SectionData data = dataMap.get(section.getId());
            sortOrderMap.put(data, section.getSortOrder() != null ? section.getSortOrder() : 0);
        }

        // 排序当前层级
        dataList.sort(Comparator.comparingInt(d -> sortOrderMap.getOrDefault(d, 0)));

        // 递归排序子节点
        for (SectionData data : dataList) {
            if (data.getChildren() != null && !data.getChildren().isEmpty()) {
                sortSectionDataRecursive(data.getChildren(), sections, dataMap);
            }
        }
    }

    /**
     * 格式化日期范围
     */
    private String formatDateRange(ReportInstance report) {
        StringBuilder sb = new StringBuilder();
        if (report.getStartDate() != null) {
            sb.append(report.getStartDate().toString());
        }
        sb.append(" 至 ");
        if (report.getEndDate() != null) {
            sb.append(report.getEndDate().toString());
        }
        return sb.toString();
    }
}
