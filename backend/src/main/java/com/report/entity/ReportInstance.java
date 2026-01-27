package com.report.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 报告实例表
 * 每一次写报告就是生成一条记录
 */
@Data
@TableName("report_instance")
public class ReportInstance {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 关联模板ID
     */
    private Long templateId;

    /**
     * 创建用户ID
     */
    private Long userId;

    /**
     * 报告名称，如 "2023年10月第4周周报"
     */
    private String reportName;

    /**
     * 报告周期开始日期
     */
    private LocalDate startDate;

    /**
     * 报告周期结束日期
     */
    private LocalDate endDate;

    /**
     * 状态: DRAFT(草稿), FINALIZED(已归档)
     */
    private String status;

    /**
     * 关联的本周Excel数据源路径
     */
    private String sourceExcelUrl;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
