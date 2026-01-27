package com.report.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 报告内容详情表（纵表设计，解决扩展性）
 * 无论有多少个标题，都存在这里
 */
@Data
@TableName(value = "report_content", autoResultMap = true)
public class ReportContent {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 关联报告实例ID
     */
    private Long reportInstanceId;

    /**
     * 对应 template_section 的 key
     */
    private String sectionKey;

    /**
     * 存储富文本 HTML
     */
    private String contentHtml;

    /**
     * 如果是表格或复杂数据，可存JSON
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> contentJson;

    /**
     * 版本号
     */
    private Integer version;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
