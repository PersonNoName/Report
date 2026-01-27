package com.report.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.report.entity.TemplateSection;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TemplateSectionMapper extends BaseMapper<TemplateSection> {

    /**
     * 获取模板的所有激活章节，按排序权重排列
     */
    List<TemplateSection> selectByTemplateIdOrdered(@Param("templateId") Long templateId);
}
