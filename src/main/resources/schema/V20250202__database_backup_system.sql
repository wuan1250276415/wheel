-- ================================================
-- 数据备份与恢复模块数据库迁移脚本
-- 版本: V20250202
-- 作者: wheel-api
-- 创建时间: 2025-02-02
-- 需求: 1.5, 2.1, 5.1-5.4
-- ================================================

USE `wheel_api`;

-- ================================================
-- 1. 备份记录表
-- ================================================
CREATE TABLE IF NOT EXISTS `backup_record` (
    `id`            BIGINT(20)   NOT NULL AUTO_INCREMENT COMMENT '备份记录ID',
    `backup_type`   TINYINT(1)   NOT NULL COMMENT '备份类型: 1-每日 2-每周 3-手动',
    `filename`      VARCHAR(255) NOT NULL COMMENT '备份文件名',
    `file_path`     VARCHAR(500) NOT NULL COMMENT '文件存储路径',
    `file_size`     BIGINT(20)   DEFAULT NULL COMMENT '文件大小(字节)',
    `is_encrypted`  TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '是否加密: 0-否 1-是',
    `status`        TINYINT(1)   NOT NULL COMMENT '状态: 0-进行中 1-成功 2-失败',
    `description`   VARCHAR(500) DEFAULT NULL COMMENT '备份描述',
    `duration_ms`   BIGINT(20)   DEFAULT NULL COMMENT '备份耗时(毫秒)',
    `error_message` TEXT         DEFAULT NULL COMMENT '错误信息',
    `create_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by`     BIGINT(20)   DEFAULT 0 COMMENT '创建人ID(手动备份)',
    `update_by`     BIGINT(20)   DEFAULT 0 COMMENT '更新人',
    `deleted`       TINYINT(1)   DEFAULT 1 COMMENT '逻辑删除标志：0-已删除，1-未删除',
    PRIMARY KEY (`id`),
    KEY `idx_backup_type` (`backup_type`),
    KEY `idx_status` (`status`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='备份记录表';

-- ================================================
-- 2. 恢复记录表
-- ================================================
CREATE TABLE IF NOT EXISTS `restore_record` (
    `id`                    BIGINT(20)   NOT NULL AUTO_INCREMENT COMMENT '恢复记录ID',
    `backup_id`             BIGINT(20)   DEFAULT NULL COMMENT '源备份ID',
    `pre_restore_backup_id` BIGINT(20)   DEFAULT NULL COMMENT '恢复前备份ID',
    `source_type`           TINYINT(1)   NOT NULL COMMENT '来源类型: 1-本地备份 2-上传文件',
    `source_filename`       VARCHAR(255) DEFAULT NULL COMMENT '源文件名',
    `status`                TINYINT(1)   NOT NULL COMMENT '状态: 0-进行中 1-成功 2-失败 3-已回滚',
    `duration_ms`           BIGINT(20)   DEFAULT NULL COMMENT '恢复耗时(毫秒)',
    `error_message`         TEXT         DEFAULT NULL COMMENT '错误信息',
    `create_time`           DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`           DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by`             BIGINT(20)   NOT NULL COMMENT '操作人ID',
    `update_by`             BIGINT(20)   DEFAULT 0 COMMENT '更新人',
    `deleted`               TINYINT(1)   DEFAULT 1 COMMENT '逻辑删除标志：0-已删除，1-未删除',
    PRIMARY KEY (`id`),
    KEY `idx_backup_id` (`backup_id`),
    KEY `idx_status` (`status`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='恢复记录表';

-- ================================================
-- 3. 备份配置表
-- ================================================
CREATE TABLE IF NOT EXISTS `backup_config` (
    `id`           BIGINT(20)   NOT NULL AUTO_INCREMENT COMMENT '配置ID',
    `config_key`   VARCHAR(100) NOT NULL COMMENT '配置键',
    `config_value` VARCHAR(500) NOT NULL COMMENT '配置值',
    `description`  VARCHAR(255) DEFAULT NULL COMMENT '配置描述',
    `create_time`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by`    BIGINT(20)   DEFAULT 0 COMMENT '创建人',
    `update_by`    BIGINT(20)   DEFAULT 0 COMMENT '更新人ID',
    `deleted`      TINYINT(1)   DEFAULT 1 COMMENT '逻辑删除标志：0-已删除，1-未删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_config_key` (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='备份配置表';

-- ================================================
-- 4. 插入默认备份配置
-- ================================================
INSERT INTO `backup_config` (`config_key`, `config_value`, `description`) VALUES
    ('daily_cron', '0 0 2 * * ?', '每日备份cron表达式'),
    ('weekly_cron', '0 0 3 ? * SUN', '每周备份cron表达式'),
    ('cleanup_cron', '0 0 4 * * ?', '清理任务cron表达式'),
    ('health_check_cron', '0 0 * * * ?', '健康检查cron表达式'),
    ('retention_days', '30', '备份保留天数'),
    ('min_backup_count', '7', '最少保留备份数'),
    ('storage_path', '/data/backups', '备份存储路径'),
    ('encryption_enabled', 'true', '是否启用加密'),
    ('alert_threshold_hours', '48', '无备份告警阈值(小时)'),
    ('storage_warning_threshold_gb', '50', '存储告警阈值(GB)')
ON DUPLICATE KEY UPDATE `update_time` = CURRENT_TIMESTAMP;

-- ================================================
-- 数据库迁移完成
-- ================================================
