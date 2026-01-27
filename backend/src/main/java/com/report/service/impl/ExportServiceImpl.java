package com.report.service.impl;

import com.deepoove.poi.XWPFTemplate;
import com.report.entity.ReportContent;
import com.report.entity.ReportInstance;
import com.report.mapper.ReportContentMapper;
import com.report.mapper.ReportInstanceMapper;
import com.report.service.ExportService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExportServiceImpl implements ExportService {

    private final ReportInstanceMapper reportMapper;
    private final ReportContentMapper contentMapper;

    @Value("${file.template-dir:./templates}")
    private String templateDir;

    @Override
    public void exportToWord(Long reportId, HttpServletResponse response) throws Exception {
        ReportInstance report = reportMapper.selectById(reportId);
        if (report == null) {
            throw new RuntimeException("报告不存在: " + reportId);
        }

        // 获取报告内容
        List<ReportContent> contents = contentMapper.selectByReportInstanceId(reportId);
        Map<String, String> contentMap = contents.stream()
                .collect(Collectors.toMap(
                        ReportContent::getSectionKey,
                        c -> c.getContentHtml() != null ? c.getContentHtml() : ""));

        // 构建模板数据
        Map<String, Object> data = new HashMap<>();
        data.put("report_title", report.getReportName());
        data.put("start_date",
                report.getStartDate() != null ? report.getStartDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                        : "");
        data.put("end_date",
                report.getEndDate() != null ? report.getEndDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                        : "");

        // 添加各章节内容
        contentMap.forEach((key, htmlContent) -> {
            // 简单处理：直接使用文本内容
            // 如需富文本，可以使用 HtmlRenderData
            data.put(key, htmlContent.replaceAll("<[^>]*>", ""));
        });

        // 加载模板
        InputStream templateStream;
        try {
            ClassPathResource resource = new ClassPathResource("templates/template.docx");
            templateStream = resource.getInputStream();
        } catch (Exception e) {
            log.warn("未找到模板文件，使用默认输出");
            // 如果没有模板，直接返回错误
            throw new RuntimeException("未配置Word模板文件");
        }

        // 渲染模板
        XWPFTemplate template = XWPFTemplate.compile(templateStream).render(data);

        // 设置响应头
        String fileName = URLEncoder.encode(report.getReportName() + ".docx", StandardCharsets.UTF_8);
        response.setContentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");

        // 写出文件
        template.write(response.getOutputStream());
        template.close();
    }
}
