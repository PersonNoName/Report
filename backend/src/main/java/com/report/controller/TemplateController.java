package com.report.controller;

import com.report.common.Result;
import com.report.entity.ReportTemplate;
import com.report.entity.TemplateSection;
import com.report.service.TemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

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
    @DeleteMapping("/sections/{sectionId}")
    public Result<Void> deleteSection(@PathVariable Long sectionId) {
        templateService.deleteSection(sectionId);
        return Result.success(null);
    }
}
