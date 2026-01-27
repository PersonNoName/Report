package com.report.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.report.entity.ReportContent;
import com.report.entity.ReportInstance;
import com.report.entity.TemplateSection;
import com.report.mapper.ReportContentMapper;
import com.report.mapper.ReportInstanceMapper;
import com.report.mapper.TemplateSectionMapper;
import com.report.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl extends ServiceImpl<ReportInstanceMapper, ReportInstance> implements ReportService {

    private final ReportContentMapper contentMapper;
    private final TemplateSectionMapper sectionMapper;

    @Override
    public List<ReportInstance> getUserReports(Long userId) {
        return list(new LambdaQueryWrapper<ReportInstance>()
                .eq(userId != null, ReportInstance::getUserId, userId)
                .orderByDesc(ReportInstance::getCreatedAt));
    }

    @Override
    @Transactional
    public ReportInstance createReport(ReportInstance report) {
        report.setStatus("DRAFT");
        report.setCreatedAt(LocalDateTime.now());
        save(report);

        // 根据模板创建空白内容
        List<TemplateSection> sections = sectionMapper.selectByTemplateIdOrdered(report.getTemplateId());
        for (TemplateSection section : sections) {
            ReportContent content = new ReportContent();
            content.setReportInstanceId(report.getId());
            content.setSectionKey(section.getSectionKey());
            content.setContentHtml("");
            content.setVersion(1);
            content.setUpdatedAt(LocalDateTime.now());
            contentMapper.insert(content);
        }

        return report;
    }

    @Override
    public Map<String, Object> getReportDetail(Long reportId) {
        ReportInstance report = getById(reportId);
        List<ReportContent> contents = contentMapper.selectByReportInstanceId(reportId);
        List<TemplateSection> sections = sectionMapper.selectByTemplateIdOrdered(report.getTemplateId());

        Map<String, ReportContent> contentMap = contents.stream()
                .collect(Collectors.toMap(ReportContent::getSectionKey, c -> c));

        Map<String, Object> result = new HashMap<>();
        result.put("report", report);
        result.put("sections", sections);
        result.put("contents", contentMap);
        return result;
    }

    @Override
    @Transactional
    public ReportContent saveContent(Long reportId, String sectionKey, String contentHtml) {
        ReportContent existing = contentMapper.selectOne(
                new LambdaQueryWrapper<ReportContent>()
                        .eq(ReportContent::getReportInstanceId, reportId)
                        .eq(ReportContent::getSectionKey, sectionKey));

        if (existing != null) {
            existing.setContentHtml(contentHtml);
            existing.setVersion(existing.getVersion() + 1);
            existing.setUpdatedAt(LocalDateTime.now());
            contentMapper.updateById(existing);
            return existing;
        } else {
            ReportContent content = new ReportContent();
            content.setReportInstanceId(reportId);
            content.setSectionKey(sectionKey);
            content.setContentHtml(contentHtml);
            content.setVersion(1);
            content.setUpdatedAt(LocalDateTime.now());
            contentMapper.insert(content);
            return content;
        }
    }

    @Override
    public void finalizeReport(Long reportId) {
        ReportInstance report = getById(reportId);
        report.setStatus("FINALIZED");
        report.setUpdatedAt(LocalDateTime.now());
        updateById(report);
    }
}
