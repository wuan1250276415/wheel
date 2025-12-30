package com.basebackend.wheel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.basebackend.wheel.entity.TaskTemplate;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TaskTemplateMapper extends BaseMapper<TaskTemplate> {

    List<TaskTemplate> selectActiveTemplates(@Param("periodType") Integer periodType);
}
