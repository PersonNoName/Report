package com.report.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 报告模板定义表
 * 如：周报模板V1, 月报模板V2024
 */
@Data
@TableName("report_template")
public class ReportTemplate {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 模板名称，如 "研发部通用周报"
     */
    private String name;

    /**
     * 模板描述
     */
    private String description;

    /**
     * 对应的空白Word模板文件存储路径(含占位符)
     */
    private String baseDocxUrl;

    /**
     * 是否激活
     */
    private Boolean isActive;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
