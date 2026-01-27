package com.report.controller;

import com.report.common.Result;
import com.report.entity.ReportContent;
import com.report.entity.ReportInstance;
import com.report.service.ExportService;
import com.report.service.ReferenceService;
import com.report.service.ReportService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;
    private final ExportService exportService;
    private final ReferenceService referenceService;

    /**
     * 获取报告列表
     */
    @GetMapping
    public Result<List<ReportInstance>> getReports(
            @RequestParam(required = false) Long userId) {
        return Result.success(reportService.getUserReports(userId));
    }

    /**
     * 创建新报告
     */
    @PostMapping
    public Result<ReportInstance> createReport(@RequestBody ReportInstance report) {
        return Result.success(reportService.createReport(report));
    }

    /**
     * 获取报告详情
     */
    @GetMapping("/{id}")
    public Result<Map<String, Object>> getReport(@PathVariable Long id) {
        return Result.success(reportService.getReportDetail(id));
    }

    /**
     * 更新报告基本信息
     */
    @PutMapping("/{id}")
    public Result<ReportInstance> updateReport(
            @PathVariable Long id,
            @RequestBody ReportInstance report) {
        report.setId(id);
        reportService.updateById(report);
        return Result.success(reportService.getById(id));
    }

    /**
     * 保存章节内容
     */
    @PutMapping("/{id}/contents/{sectionKey}")
    public Result<ReportContent> saveContent(
            @PathVariable Long id,
            @PathVariable String sectionKey,
            @RequestBody Map<String, String> body) {
        String contentHtml = body.get("contentHtml");
        return Result.success(reportService.saveContent(id, sectionKey, contentHtml));
    }

    /**
     * 归档报告
     */
    @PostMapping("/{id}/finalize")
    public Result<Void> finalizeReport(@PathVariable Long id) {
        reportService.finalizeReport(id);
        // 自动归档到参考资料库
        referenceService.archiveFromReport(id);
        return Result.success(null);
    }

    /**
     * 导出Word文档
     */
    @GetMapping("/{id}/export")
    public void exportWord(@PathVariable Long id, HttpServletResponse response) throws Exception {
        exportService.exportToWord(id, response);
    }
}
