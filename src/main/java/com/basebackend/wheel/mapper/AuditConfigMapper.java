package com.basebackend.wheel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.basebackend.wheel.entity.AuditConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 审核配置 Mapper
 *
 * @author wheel-api
 * @since 2025-02-01
 */
@Mapper
public interface AuditConfigMapper extends BaseMapper<AuditConfig> {

    /**
     * 根据配置键查询配置
     *
     * @param configKey 配置键
     * @return 配置记录
     */
    AuditConfig selectByKey(@Param("configKey") String configKey);

    /**
     * 查询所有配置
     *
     * @return 配置列表
     */
    List<AuditConfig> selectAllConfigs();

    /**
     * 更新配置值
     *
     * @param configKey   配置键
     * @param configValue 配置值
     * @return 更新数量
     */
    int updateValueByKey(@Param("configKey") String configKey, @Param("configValue") String configValue);

    /**
     * 批量查询配置
     *
     * @param configKeys 配置键列表
     * @return 配置列表
     */
    List<AuditConfig> selectByKeys(@Param("configKeys") List<String> configKeys);

    /**
     * 插入或更新配置
     *
     * @param config 配置对象
     * @return 影响行数
     */
    int insertOrUpdate(AuditConfig config);
}
