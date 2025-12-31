-- ================================================
-- 情侣报告模块数据库表
-- 版本: 1.0.0
-- 创建时间: 2025-01-30
-- ================================================

USE `wheel_api`;

-- ================================================
-- 1. 情侣报告表
-- ================================================
DROP TABLE IF EXISTS `couple_report`;
CREATE TABLE `couple_report`
(
    `id`                  bigint(20)   NOT NULL AUTO_INCREMENT COMMENT '报告ID',
    `couple_id`           bigint(20)   NOT NULL COMMENT '情侣关系ID',
    `report_type`         tinyint(1)   NOT NULL COMMENT '报告类型: 1-周报, 2-月报, 3-年报',
    `start_date`          date         NOT NULL COMMENT '统计开始日期',
    `end_date`            date         NOT NULL COMMENT '统计结束日期',
    `report_data`         json         NOT NULL COMMENT '报告数据JSON',
    `compatibility_score` int(11)      DEFAULT NULL COMMENT '默契度评分(0-100)',
    `generated_at`        datetime     NOT NULL COMMENT '生成时间',
    `create_time`         datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`         datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by`           bigint(20)            DEFAULT 0 COMMENT '创建人',
    `update_by`           bigint(20)            DEFAULT 0 COMMENT '更新人',
    `deleted`             tinyint(1)   NOT NULL DEFAULT 0 COMMENT '逻辑删除标志：0-未删除，1-已删除',
    PRIMARY KEY (`id`),
    KEY                   `idx_couple_type` (`couple_id`, `report_type`),
    KEY                   `idx_generated_at` (`generated_at`),
    KEY                   `idx_couple_date` (`couple_id`, `start_date`, `end_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='情侣报告表';

-- ================================================
-- 2. 情侣纪念日表
-- ================================================
DROP TABLE IF EXISTS `couple_anniversary`;
CREATE TABLE `couple_anniversary`
(
    `id`               bigint(20)   NOT NULL AUTO_INCREMENT COMMENT '纪念日ID',
    `couple_id`        bigint(20)   NOT NULL COMMENT '情侣关系ID',
    `anniversary_type` tinyint(1)   NOT NULL COMMENT '纪念日类型: 1-恋爱纪念日, 2-生日, 3-自定义',
    `anniversary_date` date         NOT NULL COMMENT '纪念日日期',
    `name`             varchar(100) NOT NULL COMMENT '纪念日名称',
    `remind_days`      int(11)      NOT NULL DEFAULT 7 COMMENT '提前提醒天数',
    `created_by`       bigint(20)   DEFAULT NULL COMMENT '创建者用户ID',
    `create_time`      datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`      datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `update_by`        bigint(20)            DEFAULT 0 COMMENT '更新人',
    `deleted`          tinyint(1)   NOT NULL DEFAULT 0 COMMENT '逻辑删除标志：0-未删除，1-已删除',
    PRIMARY KEY (`id`),
    KEY                `idx_couple_id` (`couple_id`),
    KEY                `idx_anniversary_date` (`anniversary_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='情侣纪念日表';
