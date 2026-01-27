package com.report.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 章节数据 DTO，用于 POI-TL 模板渲染
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SectionData {

    /**
     * 章节标题
     */
    private String title;

    /**
     * 章节内容（纯文本或 HTML）
     */
    private String content;

    /**
     * 层级，1 表示一级标题，2 表示二级标题，以此类推
     */
    private int level;

    /**
     * 子章节
     */
    private List<SectionData> children = new ArrayList<>();

    public SectionData(String title, String content, int level) {
        this.title = title;
        this.content = content;
        this.level = level;
    }
}
