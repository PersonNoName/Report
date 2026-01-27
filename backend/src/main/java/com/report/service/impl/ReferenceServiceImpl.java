package com.report.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.report.entity.ReferenceMaterial;
import com.report.entity.ReportContent;
import com.report.mapper.ReferenceMaterialMapper;
import com.report.mapper.ReportContentMapper;
import com.report.service.ReferenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReferenceServiceImpl extends ServiceImpl<ReferenceMaterialMapper, ReferenceMaterial>
        implements ReferenceService {

    private final ReportContentMapper contentMapper;

    @Override
    public List<ReferenceMaterial> search(String sectionKey, String keyword) {
        return baseMapper.searchBySectionKey(sectionKey, keyword);
    }

    @Override
    public ReferenceMaterial saveAsStandard(String sectionKey, String contentText, String tags, Long userId) {
        ReferenceMaterial material = new ReferenceMaterial();
        material.setSectionKey(sectionKey);
        material.setContentText(contentText);
        material.setTags(tags);
        material.setIsStandard(true);
        material.setCreatedBy(userId);
        material.setCreatedAt(LocalDateTime.now());
        save(material);
        return material;
    }

    @Override
    public void archiveFromReport(Long reportId) {
        List<ReportContent> contents = contentMapper.selectByReportInstanceId(reportId);
        for (ReportContent content : contents) {
            if (content.getContentHtml() != null && !content.getContentHtml().isBlank()) {
                ReferenceMaterial material = new ReferenceMaterial();
                material.setSectionKey(content.getSectionKey());
                material.setContentText(content.getContentHtml());
                material.setSourceReportId(reportId);
                material.setIsStandard(false);
                material.setCreatedAt(LocalDateTime.now());
                save(material);
            }
        }
    }
}
