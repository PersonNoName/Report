package com.report.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.report.entity.ReportContent;
import com.report.entity.ReportInstance;

import java.util.List;
import java.util.Map;

public interface ReportService extends IService<ReportInstance> {

    /**
     * 获取用户的所有报告
     */
    List<ReportInstance> getUserReports(Long userId);

    /**
     * 创建新报告实例
     */
    ReportInstance createReport(ReportInstance report);

    /**
     * 获取报告详情及所有内容
     */
    Map<String, Object> getReportDetail(Long reportId);

    /**
     * 保存/更新章节内容
     */
    ReportContent saveContent(Long reportId, String sectionKey, String contentHtml);

    /**
     * 归档报告
     */
    void finalizeReport(Long reportId);
}
