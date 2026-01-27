package com.report.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 参考资料库/话术库
 * 实现"参考过去"、"存为资料"、"替换"的功能
 */
@Data
@TableName("reference_material")
public class ReferenceMaterial {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 这条资料属于哪个模块(如属于"总结"还是"计划")
     */
    private String sectionKey;

    /**
     * 资料内容
     */
    private String contentText;

    /**
     * 标签，方便检索（逗号分隔）
     */
    private String tags;

    /**
     * 来源：如果来自某次历史周报，记录ID
     */
    private Long sourceReportId;

    /**
     * 是否为"标准话术"(由用户手动保存的)
     */
    private Boolean isStandard;

    /**
     * 创建人ID
     */
    private Long createdBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
