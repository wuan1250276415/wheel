package com.basebackend.wheel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.basebackend.wheel.entity.WheelCategory;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface WheelCategoryMapper extends BaseMapper<WheelCategory> {
    int deleteByPrimaryKey(Long id);


    WheelCategory selectByPrimaryKey(Long id);


    int updateByPrimaryKey(WheelCategory record);

    /**
     * 获取启用的分类列表，按排序权重降序
     *
     * @return 分类列表
     */
    List<WheelCategory> selectEnabledCategoriesOrderBySort();
}