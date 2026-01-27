package com.report.service;

import jakarta.servlet.http.HttpServletResponse;

public interface ExportService {

    /**
     * 导出报告为Word文档
     */
    void exportToWord(Long reportId, HttpServletResponse response) throws Exception;
}
