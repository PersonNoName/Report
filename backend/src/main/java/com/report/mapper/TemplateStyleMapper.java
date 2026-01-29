package com.report.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.report.entity.TemplateStyle;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 模板样式 Mapper
 */
@Mapper
public interface TemplateStyleMapper extends BaseMapper<TemplateStyle> {

    /**
     * 根据模板 ID 查询所有样式配置
     */
    List<TemplateStyle> selectByTemplateId(@Param("templateId") Long templateId);

    /**
     * 根据模板 ID 和样式类型查询
     */
    TemplateStyle selectByTemplateIdAndType(@Param("templateId") Long templateId,
            @Param("styleType") String styleType);

    /**
     * 删除模板的所有样式配置
     */
    int deleteByTemplateId(@Param("templateId") Long templateId);
}
