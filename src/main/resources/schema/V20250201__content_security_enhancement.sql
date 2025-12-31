-- ================================================
-- 内容安全模块增强数据库迁移脚本
-- 版本: V20250201
-- 作者: wheel-api
-- 创建时间: 2025-02-01
-- 需求: 1.4, 4.4, 3.2
-- ================================================

USE `wheel_api`;

-- ================================================
-- 1. 敏感词库表
-- ================================================
CREATE TABLE IF NOT EXISTS `sensitive_word` (
    `id`          BIGINT(20)   NOT NULL AUTO_INCREMENT COMMENT '敏感词ID',
    `word`        VARCHAR(100) NOT NULL COMMENT '敏感词',
    `category`    TINYINT(1)   NOT NULL DEFAULT 5 COMMENT '分类：1-色情 2-暴力 3-政治 4-广告 5-其他',
    `level`       TINYINT(1)   NOT NULL DEFAULT 2 COMMENT '风险等级：1-低 2-中 3-高',
    `variants`    JSON         DEFAULT NULL COMMENT '变体词列表（JSON数组）',
    `status`      TINYINT(1)   NOT NULL DEFAULT 1 COMMENT '状态：0-禁用 1-启用',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by`   BIGINT(20)   DEFAULT 0 COMMENT '创建人',
    `update_by`   BIGINT(20)   DEFAULT 0 COMMENT '更新人',
    `deleted`     TINYINT(1)   DEFAULT 1 COMMENT '逻辑删除标志：0-已删除，1-未删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_word` (`word`),
    KEY `idx_category` (`category`),
    KEY `idx_level` (`level`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='敏感词库表';

-- ================================================
-- 2. 黑名单表
-- ================================================
CREATE TABLE IF NOT EXISTS `blacklist` (
    `id`            BIGINT(20)   NOT NULL AUTO_INCREMENT COMMENT '黑名单ID',
    `type`          TINYINT(1)   NOT NULL COMMENT '类型：1-用户 2-IP 3-设备',
    `target_id`     VARCHAR(100) NOT NULL COMMENT '目标ID（用户ID/IP地址/设备ID）',
    `reason`        VARCHAR(500) DEFAULT NULL COMMENT '封禁原因',
    `duration`      INT(11)      DEFAULT 0 COMMENT '封禁时长（分钟），0表示永久',
    `expire_at`     DATETIME     DEFAULT NULL COMMENT '过期时间',
    `operator_id`   BIGINT(20)   DEFAULT NULL COMMENT '操作人ID',
    `status`        TINYINT(1)   NOT NULL DEFAULT 1 COMMENT '状态：0-已解除 1-生效中',
    `appeal_status` TINYINT(1)   DEFAULT 0 COMMENT '申诉状态：0-未申诉 1-申诉中 2-申诉通过 3-申诉驳回',
    `appeal_reason` VARCHAR(500) DEFAULT NULL COMMENT '申诉理由',
    `appeal_time`   DATETIME     DEFAULT NULL COMMENT '申诉时间',
    `appeal_handler_id` BIGINT(20) DEFAULT NULL COMMENT '申诉处理人ID',
    `appeal_handled_at` DATETIME DEFAULT NULL COMMENT '申诉处理时间',
    `appeal_result` VARCHAR(500) DEFAULT NULL COMMENT '申诉处理结果',
    `create_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by`     BIGINT(20)   DEFAULT 0 COMMENT '创建人',
    `update_by`     BIGINT(20)   DEFAULT 0 COMMENT '更新人',
    `deleted`       TINYINT(1)   DEFAULT 1 COMMENT '逻辑删除标志：0-已删除，1-未删除',
    PRIMARY KEY (`id`),
    KEY `idx_type_target` (`type`, `target_id`),
    KEY `idx_status` (`status`),
    KEY `idx_expire_at` (`expire_at`),
    KEY `idx_appeal_status` (`appeal_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='黑名单表';

-- ================================================
-- 3. 用户举报信誉表
-- ================================================
CREATE TABLE IF NOT EXISTS `user_report_credibility` (
    `id`                BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '信誉ID',
    `user_id`           BIGINT(20) NOT NULL COMMENT '用户ID',
    `credibility_score` INT(11)    NOT NULL DEFAULT 100 COMMENT '信誉分（0-100）',
    `total_reports`     INT(11)    NOT NULL DEFAULT 0 COMMENT '总举报数',
    `valid_reports`     INT(11)    NOT NULL DEFAULT 0 COMMENT '有效举报数',
    `invalid_reports`   INT(11)    NOT NULL DEFAULT 0 COMMENT '无效举报数',
    `last_report_at`    DATETIME   DEFAULT NULL COMMENT '最后举报时间',
    `create_time`       DATETIME   NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`       DATETIME   NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by`         BIGINT(20) DEFAULT 0 COMMENT '创建人',
    `update_by`         BIGINT(20) DEFAULT 0 COMMENT '更新人',
    `deleted`           TINYINT(1) DEFAULT 1 COMMENT '逻辑删除标志：0-已删除，1-未删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_id` (`user_id`),
    KEY `idx_credibility_score` (`credibility_score`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户举报信誉表';

-- ================================================
-- 4. 审核配置表
-- ================================================
CREATE TABLE IF NOT EXISTS `audit_config` (
    `id`           BIGINT(20)   NOT NULL AUTO_INCREMENT COMMENT '配置ID',
    `config_key`   VARCHAR(100) NOT NULL COMMENT '配置键',
    `config_value` VARCHAR(500) NOT NULL COMMENT '配置值',
    `description`  VARCHAR(200) DEFAULT NULL COMMENT '配置说明',
    `create_time`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by`    BIGINT(20)   DEFAULT 0 COMMENT '创建人',
    `update_by`    BIGINT(20)   DEFAULT 0 COMMENT '更新人',
    `deleted`      TINYINT(1)   DEFAULT 1 COMMENT '逻辑删除标志：0-已删除，1-未删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_config_key` (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='审核配置表';

-- ================================================
-- 5. 扩展 content_report 表添加新字段
-- ================================================
ALTER TABLE `content_report`
    ADD COLUMN IF NOT EXISTS `report_type` TINYINT(1) DEFAULT 0 COMMENT '举报类型：0-内容 1-用户 2-评论' AFTER `reporter_user_id`,
    ADD COLUMN IF NOT EXISTS `report_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '举报时间' AFTER `report_type`,
    ADD COLUMN IF NOT EXISTS `evidence_urls` JSON DEFAULT NULL COMMENT '证据截图URL列表' AFTER `report_description`,
    ADD COLUMN IF NOT EXISTS `reporter_credibility` INT(11) DEFAULT 100 COMMENT '举报人信誉分' AFTER `evidence_urls`,
    ADD COLUMN IF NOT EXISTS `priority` TINYINT(1) DEFAULT 1 COMMENT '优先级：1-普通 2-高 3-紧急' AFTER `reporter_credibility`;

-- 添加索引
ALTER TABLE `content_report`
    ADD INDEX IF NOT EXISTS `idx_priority` (`priority`),
    ADD INDEX IF NOT EXISTS `idx_report_time` (`report_time`);

-- ================================================
-- 6. 插入默认审核配置
-- ================================================
INSERT INTO `audit_config` (`config_key`, `config_value`, `description`) VALUES
    ('ai_pass_threshold', '30', 'AI审核通过阈值（风险分低于此值自动通过）'),
    ('ai_reject_threshold', '80', 'AI审核拒绝阈值（风险分高于此值自动拒绝）'),
    ('report_priority_threshold', '3', '举报数达到此值提升优先级'),
    ('vip_audit_priority', '2', 'VIP用户审核优先级'),
    ('svip_audit_priority', '3', 'SVIP用户审核优先级'),
    ('sensitive_word_hot_reload_interval', '300', '敏感词库热更新间隔（秒）'),
    ('blacklist_cache_ttl', '3600', '黑名单缓存过期时间（秒）'),
    ('max_report_per_content_per_user', '1', '同一用户对同一内容最大举报次数'),
    ('low_credibility_threshold', '30', '低信誉用户阈值'),
    ('credibility_decrease_on_invalid', '10', '无效举报信誉分扣减值'),
    ('credibility_increase_on_valid', '5', '有效举报信誉分增加值')
ON DUPLICATE KEY UPDATE `update_time` = CURRENT_TIMESTAMP;

-- ================================================
-- 7. 插入示例敏感词数据
-- ================================================
INSERT INTO `sensitive_word` (`word`, `category`, `level`, `variants`, `status`) VALUES
    ('测试敏感词1', 5, 1, '["测试敏感词一", "测试敏感词壹"]', 1),
    ('测试敏感词2', 5, 2, NULL, 1)
ON DUPLICATE KEY UPDATE `update_time` = CURRENT_TIMESTAMP;

-- ================================================
-- 数据库迁移完成
-- ================================================
