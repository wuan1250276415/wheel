package com.basebackend.wheel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.basebackend.wheel.entity.BackupConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 备份配置 Mapper
 *
 * @author wheel-api
 * @since 2025-02-02
 */
@Mapper
public interface BackupConfigMapper extends BaseMapper<BackupConfig> {

    /**
     * 根据配置键查询配置
     *
     * @param configKey 配置键
     * @return 配置记录
     */
    BackupConfig selectByKey(@Param("configKey") String configKey);

    /**
     * 查询所有配置
     *
     * @return 配置列表
     */
    List<BackupConfig> selectAllConfigs();

    /**
     * 更新配置值
     *
     * @param configKey   配置键
     * @param configValue 配置值
     * @param updateBy    更新人ID
     * @return 更新数量
     */
    int updateValueByKey(
            @Param("configKey") String configKey,
            @Param("configValue") String configValue,
            @Param("updateBy") Long updateBy
    );

    /**
     * 批量查询配置
     *
     * @param configKeys 配置键列表
     * @return 配置列表
     */
    List<BackupConfig> selectByKeys(@Param("configKeys") List<String> configKeys);

    /**
     * 插入或更新配置
     *
     * @param config 配置对象
     * @return 影响行数
     */
    int insertOrUpdate(BackupConfig config);

    /**
     * 批量更新配置
     *
     * @param configs 配置列表
     * @return 影响行数
     */
    int batchUpdate(@Param("configs") List<BackupConfig> configs);
}
