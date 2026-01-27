package com.report.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.report.entity.ReportContent;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ReportContentMapper extends BaseMapper<ReportContent> {

    /**
     * 获取报告实例的所有内容
     */
    List<ReportContent> selectByReportInstanceId(@Param("reportInstanceId") Long reportInstanceId);
}
