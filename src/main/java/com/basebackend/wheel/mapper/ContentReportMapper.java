package com.basebackend.wheel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.basebackend.wheel.entity.ContentReport;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ContentReportMapper extends BaseMapper<ContentReport> {


    /**
     * 根据内容ID查询举报
     *
     * @param contentId 内容ID
     * @return 举报列表
     */
    List<ContentReport> selectByContentId(@Param("contentId") Long contentId);

}