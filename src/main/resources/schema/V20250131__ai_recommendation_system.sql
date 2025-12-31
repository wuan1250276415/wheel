-- ================================================
-- AI 推荐模块数据库表
-- 版本: 1.0.0
-- 创建时间: 2025-01-31
-- ================================================

USE `wheel_api`;

-- ================================================
-- 1. 用户画像表
-- ================================================
DROP TABLE IF EXISTS `user_profile`;
CREATE TABLE `user_profile`
(
    `id`                  bigint(20)   NOT NULL AUTO_INCREMENT COMMENT '画像ID',
    `user_id`             bigint(20)   NOT NULL COMMENT '用户ID',
    `favorite_categories` json         DEFAULT NULL COMMENT '偏好分类及权重 {"1": 0.8, "2": 0.5}',
    `active_time_slots`   json         DEFAULT NULL COMMENT '活跃时段 [{"hour": 20, "weight": 0.9}]',
    `behavior_tags`       json         DEFAULT NULL COMMENT '行为标签 ["romantic", "adventurous"]',
    `total_spins`         int(11)      NOT NULL DEFAULT 0 COMMENT '总转盘次数',
    `last_active_time`    datetime     DEFAULT NULL COMMENT '最后活跃时间',
    `is_stale`            tinyint(1)   NOT NULL DEFAULT 0 COMMENT '是否过期：0-否，1-是',
    `create_time`         datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`         datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by`           bigint(20)            DEFAULT 0 COMMENT '创建人',
    `update_by`           bigint(20)            DEFAULT 0 COMMENT '更新人',
    `deleted`             tinyint(1)   NOT NULL DEFAULT 0 COMMENT '逻辑删除标志：0-未删除，1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_id` (`user_id`),
    KEY `idx_last_active` (`last_active_time`),
    KEY `idx_is_stale` (`is_stale`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户画像表';

-- ================================================
-- 2. 推荐记录表
-- ================================================
DROP TABLE IF EXISTS `recommendation_log`;
CREATE TABLE `recommendation_log`
(
    `id`                   bigint(20)   NOT NULL AUTO_INCREMENT COMMENT '记录ID',
    `user_id`              bigint(20)   NOT NULL COMMENT '用户ID',
    `content_id`           bigint(20)   NOT NULL COMMENT '内容ID',
    `recommendation_score` double       DEFAULT NULL COMMENT '推荐分数',
    `recommendation_reason` varchar(200) DEFAULT NULL COMMENT '推荐原因',
    `is_clicked`           tinyint(1)   NOT NULL DEFAULT 0 COMMENT '是否点击：0-否，1-是',
    `click_time`           datetime     DEFAULT NULL COMMENT '点击时间',
    `session_id`           varchar(64)  DEFAULT NULL COMMENT '会话ID',
    `create_time`          datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`          datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by`            bigint(20)            DEFAULT 0 COMMENT '创建人',
    `update_by`            bigint(20)            DEFAULT 0 COMMENT '更新人',
    `deleted`              tinyint(1)   NOT NULL DEFAULT 0 COMMENT '逻辑删除标志：0-未删除，1-已删除',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_content_id` (`content_id`),
    KEY `idx_create_time` (`create_time`),
    KEY `idx_session_id` (`session_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='推荐记录表';

-- ================================================
-- 3. 用户偏好调查表
-- ================================================
DROP TABLE IF EXISTS `user_preference_survey`;
CREATE TABLE `user_preference_survey`
(
    `id`                   bigint(20)   NOT NULL AUTO_INCREMENT COMMENT '调查ID',
    `user_id`              bigint(20)   NOT NULL COMMENT '用户ID',
    `selected_categories`  json         DEFAULT NULL COMMENT '选择的分类ID列表',
    `skipped`              tinyint(1)   NOT NULL DEFAULT 0 COMMENT '是否跳过：0-否，1-是',
    `create_time`          datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`          datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by`            bigint(20)            DEFAULT 0 COMMENT '创建人',
    `update_by`            bigint(20)            DEFAULT 0 COMMENT '更新人',
    `deleted`              tinyint(1)   NOT NULL DEFAULT 0 COMMENT '逻辑删除标志：0-未删除，1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户偏好调查表';

-- ================================================
-- 4. 扩展 wheel_content 表（添加推荐相关字段）
-- ================================================
ALTER TABLE `wheel_content` 
    ADD COLUMN `popularity_score` double DEFAULT 0 COMMENT '热度分数' AFTER `is_system`,
    ADD COLUMN `difficulty_level` tinyint(1) DEFAULT 1 COMMENT '难度等级：1-简单，2-中等，3-困难' AFTER `popularity_score`,
    ADD COLUMN `feature_vector` json DEFAULT NULL COMMENT '特征向量' AFTER `difficulty_level`;

-- 添加热度分数索引
ALTER TABLE `wheel_content` ADD INDEX `idx_popularity_score` (`popularity_score`);
