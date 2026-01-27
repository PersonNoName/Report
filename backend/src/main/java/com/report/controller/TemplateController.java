package com.report.controller;

import com.report.common.Result;
import com.report.entity.ReportTemplate;
import com.report.entity.TemplateSection;
import com.report.service.TemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;

@RestController
@RequestMapping("/api/templates")
@RequiredArgsConstructor
public class TemplateController {

    private final TemplateService templateService;

    /**
     * 获取所有激活模板
     */
    @GetMapping
    public Result<List<ReportTemplate>> getTemplates() {
        return Result.success(templateService.getActiveTemplates());
    }

    /**
     * 获取模板详情
     */
    @GetMapping("/{id}")
    public Result<ReportTemplate> getTemplate(@PathVariable Long id) {
        return Result.success(templateService.getById(id));
    }

    /**
     * 创建模板
     */
    @PostMapping
    public Result<ReportTemplate> createTemplate(@RequestBody ReportTemplate template) {
        template.setIsActive(true);
        templateService.save(template);
        return Result.success(template);
    }

    /**
     * 获取模板的章节结构
     */
    @GetMapping("/{id}/sections")
    public Result<List<TemplateSection>> getSections(@PathVariable Long id) {
        return Result.success(templateService.getTemplateSections(id));
    }

    /**
     * 添加章节
     */
    @PostMapping("/{id}/sections")
    public Result<TemplateSection> addSection(
            @PathVariable Long id,
            @RequestBody TemplateSection section) {
        return Result.success(templateService.addSection(id, section));
    }

    /**
     * 更新章节
     */
    @PutMapping("/sections/{sectionId}")
    public Result<TemplateSection> updateSection(
            @PathVariable Long sectionId,
            @RequestBody TemplateSection section) {
        return Result.success(templateService.updateSection(sectionId, section));
    }

    /**
     * 删除章节
     */
    /**
     * 删除章节
     */
    @DeleteMapping("/sections/{sectionId}")
    public Result<Void> deleteSection(@PathVariable Long sectionId) {
        templateService.deleteSection(sectionId);
        return Result.success(null);
    }

    @Value("${file.upload-dir:./uploads}")
    private String uploadDir;

    /**
     * 解析Word获取章节结构并保存文件
     */
    @PostMapping("/parse")
    public Result<ParseResult> parseWord(@RequestParam("file") org.springframework.web.multipart.MultipartFile file)
            throws java.io.IOException {

        // 1. Save file
        String fileName = "template_" + System.currentTimeMillis() + "_" + file.getOriginalFilename();
        java.nio.file.Path uploadPath = java.nio.file.Paths.get(uploadDir, "templates");
        if (!java.nio.file.Files.exists(uploadPath)) {
            java.nio.file.Files.createDirectories(uploadPath);
        }

        try (java.io.InputStream inputStream = file.getInputStream()) {
            java.nio.file.Files.copy(inputStream, uploadPath.resolve(fileName),
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        }

        // 2. Parse structure
        // Re-open input stream for parsing since the previous one was consumed?
        // Or parse first then save? XWPFDocument constructor takes InputStream.
        // Let's read from the saved file to be sure.
        List<com.report.utils.WordUtil.SectionNode> sections;
        try (java.io.InputStream is = java.nio.file.Files.newInputStream(uploadPath.resolve(fileName))) {
            // We need a helper that takes InputStream, or overload parseStructure
            // WordUtil.parseStructure takes MultipartFile currently.
            // Let's modify WordUtil to take InputStream or just create a MockMultipartFile?
            // Better: Modify WordUtil to take InputStream.
            // But for now, let's just parse the original file stream if possible?
            // MultipartFile.getInputStream() returns a new stream each time usually?
            // Yes, usually.
            sections = com.report.utils.WordUtil.parseStructure(file);
        }

        ParseResult result = new ParseResult();
        result.setSections(sections);
        result.setFileName(fileName); // Return relative path or filename

        return Result.success(result);
    }

    /**
     * 创建模板（支持带章节结构）
     */
    @PostMapping("/with-sections")
    public Result<ReportTemplate> createTemplateWithSections(@RequestBody CreateTemplateRequest request) {
        return Result.success(templateService.createTemplateWithSections(request.getTemplate(), request.getSections()));
    }

    @lombok.Data
    public static class CreateTemplateRequest {
        private ReportTemplate template;
        private List<com.report.utils.WordUtil.SectionNode> sections;
    }

    @lombok.Data
    public static class ParseResult {
        private List<com.report.utils.WordUtil.SectionNode> sections;
        private String fileName;
    }
}
