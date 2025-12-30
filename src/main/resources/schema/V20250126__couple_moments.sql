-- ================================================
-- 情侣动态分享模块数据库表
-- 版本: 1.0.0
-- 创建时间: 2025-01-26
-- ================================================

USE `wheel_api`;

-- ================================================
-- 1. 情侣动态表
-- ================================================
DROP TABLE IF EXISTS `couple_moment`;
CREATE TABLE `couple_moment`
(
    `id`            bigint(20)   NOT NULL AUTO_INCREMENT COMMENT '动态ID',
    `couple_id`     bigint(20)   NOT NULL COMMENT '情侣关系ID',
    `user_id`       bigint(20)   NOT NULL COMMENT '发布者用户ID',
    `content`       varchar(2000) NOT NULL COMMENT '动态文本内容',
    `images`        json         DEFAULT NULL COMMENT '图片URL数组（最多9张）',
    `visibility`    tinyint(1)   NOT NULL DEFAULT 0 COMMENT '可见性：0-仅情侣可见，1-公开',
    `like_count`    int(11)      NOT NULL DEFAULT 0 COMMENT '点赞数',
    `comment_count` int(11)      NOT NULL DEFAULT 0 COMMENT '评论数',
    `create_time`   datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`   datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by`     bigint(20)            DEFAULT 0 COMMENT '创建人',
    `update_by`     bigint(20)            DEFAULT 0 COMMENT '更新人',
    `deleted`       tinyint(1)   NOT NULL DEFAULT 0 COMMENT '逻辑删除标志：0-已删除，1-未删除',
    PRIMARY KEY (`id`),
    KEY             `idx_couple_time` (`couple_id`, `create_time` DESC),
    KEY             `idx_user_time` (`user_id`, `create_time` DESC),
    KEY             `idx_visibility` (`visibility`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='情侣动态表';

-- ================================================
-- 2. 动态点赞表
-- ================================================
DROP TABLE IF EXISTS `moment_like`;
CREATE TABLE `moment_like`
(
    `id`          bigint(20) NOT NULL AUTO_INCREMENT COMMENT '点赞ID',
    `moment_id`   bigint(20) NOT NULL COMMENT '动态ID',
    `user_id`     bigint(20) NOT NULL COMMENT '点赞用户ID',
    `create_time` datetime   NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime   NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by`   bigint(20)          DEFAULT 0 COMMENT '创建人',
    `update_by`   bigint(20)          DEFAULT 0 COMMENT '更新人',
    `deleted`     tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除标志：0-已删除，1-未删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_moment_user` (`moment_id`, `user_id`, `deleted`),
    KEY           `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='动态点赞表';

-- ================================================
-- 3. 动态评论表
-- ================================================
DROP TABLE IF EXISTS `moment_comment`;
CREATE TABLE `moment_comment`
(
    `id`          bigint(20)    NOT NULL AUTO_INCREMENT COMMENT '评论ID',
    `moment_id`   bigint(20)    NOT NULL COMMENT '动态ID',
    `user_id`     bigint(20)    NOT NULL COMMENT '评论用户ID',
    `content`     varchar(1000) NOT NULL COMMENT '评论内容',
    `create_time` datetime      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by`   bigint(20)             DEFAULT 0 COMMENT '创建人',
    `update_by`   bigint(20)             DEFAULT 0 COMMENT '更新人',
    `deleted`     tinyint(1)    NOT NULL DEFAULT 0 COMMENT '逻辑删除标志：0-已删除，1-未删除',
    PRIMARY KEY (`id`),
    KEY           `idx_moment_time` (`moment_id`, `create_time` DESC),
    KEY           `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='动态评论表';
