package com.basebackend.wheel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.basebackend.wheel.entity.SensitiveWord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 敏感词库 Mapper
 *
 * @author wheel-api
 * @since 2025-02-01
 */
@Mapper
public interface SensitiveWordMapper extends BaseMapper<SensitiveWord> {

    /**
     * 查询所有启用的敏感词
     *
     * @return 敏感词列表
     */
    List<SensitiveWord> selectAllEnabled();

    /**
     * 根据分类查询敏感词
     *
     * @param category 分类
     * @return 敏感词列表
     */
    List<SensitiveWord> selectByCategory(@Param("category") Integer category);

    /**
     * 根据风险等级查询敏感词
     *
     * @param level 风险等级
     * @return 敏感词列表
     */
    List<SensitiveWord> selectByLevel(@Param("level") Integer level);

    /**
     * 检查敏感词是否存在
     *
     * @param word 敏感词
     * @return 是否存在
     */
    boolean existsByWord(@Param("word") String word);

    /**
     * 批量插入敏感词
     *
     * @param words 敏感词列表
     * @return 插入数量
     */
    int batchInsert(@Param("words") List<SensitiveWord> words);

    /**
     * 统计各分类敏感词数量
     *
     * @return 分类统计
     */
    List<java.util.Map<String, Object>> countByCategory();
}
