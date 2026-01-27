package com.report.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.report.entity.ReferenceMaterial;

import java.util.List;

public interface ReferenceService extends IService<ReferenceMaterial> {

    /**
     * 搜索参考资料
     */
    List<ReferenceMaterial> search(String sectionKey, String keyword);

    /**
     * 保存为标准话术
     */
    ReferenceMaterial saveAsStandard(String sectionKey, String contentText, String tags, Long userId);

    /**
     * 从报告归档时自动保存参考资料
     */
    void archiveFromReport(Long reportId);
}
