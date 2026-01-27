package com.report.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.report.entity.ReferenceMaterial;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ReferenceMaterialMapper extends BaseMapper<ReferenceMaterial> {

    /**
     * 根据章节key搜索参考资料
     */
    List<ReferenceMaterial> searchBySectionKey(
            @Param("sectionKey") String sectionKey,
            @Param("keyword") String keyword);
}
