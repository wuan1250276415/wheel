-- ================================================
-- 会员订阅系统
-- 创建时间: 2025-01-27
-- 描述: 支持会员等级、订阅套餐、支付集成、自动续费
-- ================================================

-- ================================================
-- 1. 会员套餐表
-- ================================================
DROP TABLE IF EXISTS `membership_plan`;
CREATE TABLE `membership_plan`
(
    `id`            bigint(20)     NOT NULL AUTO_INCREMENT COMMENT '套餐ID',
    `plan_name`     varchar(100)   NOT NULL COMMENT '套餐名称',
    `plan_key`      varchar(50)    NOT NULL COMMENT '套餐标识key（如：vip_monthly, svip_annual）',
    `tier`          tinyint(1)     NOT NULL COMMENT '会员等级：1-VIP，2-SVIP',
    `duration_days` int(11)        NOT NULL COMMENT '时长（天）',
    `price`         decimal(10, 2) NOT NULL COMMENT '价格（元）',
    `original_price` decimal(10, 2) DEFAULT NULL COMMENT '原价（元）',
    `benefits`      json           NOT NULL COMMENT '权益配置JSON',
    `description`   text COMMENT '套餐描述',
    `is_recommended` tinyint(1)    DEFAULT 0 COMMENT '是否推荐：0-否，1-是',
    `status`        tinyint(1)     NOT NULL DEFAULT 1 COMMENT '套餐状态：0-禁用，1-启用',
    `sort_order`    int(11)        DEFAULT 0 COMMENT '排序权重',
    `create_time`   datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`   datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by`     bigint(20)     DEFAULT 0 COMMENT '创建人',
    `update_by`     bigint(20)     DEFAULT 0 COMMENT '更新人',
    `deleted`       tinyint(1)     NOT NULL DEFAULT 0 COMMENT '逻辑删除标志：0-已删除，1-未删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_plan_key` (`plan_key`),
    KEY             `idx_tier` (`tier`),
    KEY             `idx_status` (`status`),
    KEY             `idx_sort_order` (`sort_order`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='会员套餐表';

-- ================================================
-- 2. 用户会员表
-- ================================================
DROP TABLE IF EXISTS `user_membership`;
CREATE TABLE `user_membership`
(
    `id`         bigint(20) NOT NULL AUTO_INCREMENT COMMENT '记录ID',
    `user_id`    bigint(20) NOT NULL COMMENT '用户ID',
    `plan_id`    bigint(20) NOT NULL COMMENT '套餐ID',
    `tier`       tinyint(1) NOT NULL COMMENT '会员等级：1-VIP，2-SVIP',
    `start_time` datetime   NOT NULL COMMENT '开始时间',
    `end_time`   datetime   NOT NULL COMMENT '结束时间',
    `status`     tinyint(1) NOT NULL DEFAULT 1 COMMENT '状态：0-待激活，1-有效，2-已过期，3-已取消',
    `auto_renew` tinyint(1) NOT NULL DEFAULT 0 COMMENT '自动续费：0-否，1-是',
    `order_id`   bigint(20) DEFAULT NULL COMMENT '关联订单ID',
    `create_time` datetime  NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime  NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by`  bigint(20) DEFAULT 0 COMMENT '创建人',
    `update_by`  bigint(20) DEFAULT 0 COMMENT '更新人',
    `deleted`    tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除标志：0-已删除，1-未删除',
    PRIMARY KEY (`id`),
    KEY          `idx_user_id` (`user_id`),
    KEY          `idx_plan_id` (`plan_id`),
    KEY          `idx_status` (`status`),
    KEY          `idx_end_time` (`end_time`),
    KEY          `idx_auto_renew` (`auto_renew`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='用户会员表';

-- ================================================
-- 3. 支付订单表
-- ================================================
DROP TABLE IF EXISTS `payment_order`;
CREATE TABLE `payment_order`
(
    `id`             bigint(20)     NOT NULL AUTO_INCREMENT COMMENT '订单ID',
    `order_no`       varchar(50)    NOT NULL COMMENT '订单号（唯一）',
    `user_id`        bigint(20)     NOT NULL COMMENT '用户ID',
    `plan_id`        bigint(20)     NOT NULL COMMENT '套餐ID',
    `amount`         decimal(10, 2) NOT NULL COMMENT '订单金额（元）',
    `payment_method` tinyint(1)     NOT NULL COMMENT '支付方式：1-微信支付，2-支付宝',
    `payment_status` tinyint(1)     NOT NULL DEFAULT 0 COMMENT '支付状态：0-待支付，1-支付成功，2-支付失败，3-已取消，4-已退款',
    `trade_no`       varchar(100)   DEFAULT NULL COMMENT '第三方交易号',
    `paid_at`        datetime       DEFAULT NULL COMMENT '支付时间',
    `refunded_at`    datetime       DEFAULT NULL COMMENT '退款时间',
    `refund_reason`  varchar(500)   DEFAULT NULL COMMENT '退款原因',
    `callback_data`  json           DEFAULT NULL COMMENT '支付回调数据',
    `remark`         varchar(500)   DEFAULT NULL COMMENT '备注',
    `create_time`    datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`    datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by`      bigint(20)     DEFAULT 0 COMMENT '创建人',
    `update_by`      bigint(20)     DEFAULT 0 COMMENT '更新人',
    `deleted`        tinyint(1)     NOT NULL DEFAULT 0 COMMENT '逻辑删除标志：0-已删除，1-未删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_no` (`order_no`),
    KEY              `idx_user_id` (`user_id`),
    KEY              `idx_plan_id` (`plan_id`),
    KEY              `idx_payment_status` (`payment_status`),
    KEY              `idx_create_time` (`create_time`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='支付订单表';

-- ================================================
-- 4. 初始化会员套餐数据
-- ================================================
INSERT INTO `membership_plan` (`plan_name`, `plan_key`, `tier`, `duration_days`, `price`, `original_price`, `benefits`,
                                `description`, `is_recommended`, `status`, `sort_order`)
VALUES ('VIP月度会员', 'vip_monthly', 1, 30, 9.90, 9.90, '{
  "maxWheelCount": 100,
  "exclusiveThemes": false,
  "priorityAudit": true,
  "advancedStats": true,
  "adFree": true,
  "vipSupport": false
}', 'VIP会员月度套餐，享受基础高级权益', 0, 1, 1),

       ('VIP季度会员', 'vip_quarterly', 1, 90, 24.90, 29.70, '{
         "maxWheelCount": 100,
         "exclusiveThemes": false,
         "priorityAudit": true,
         "advancedStats": true,
         "adFree": true,
         "vipSupport": false
       }', 'VIP会员季度套餐，优惠17%', 1, 1, 2),

       ('VIP年度会员', 'vip_annual', 1, 365, 88.00, 118.80, '{
         "maxWheelCount": 100,
         "exclusiveThemes": false,
         "priorityAudit": true,
         "advancedStats": true,
         "adFree": true,
         "vipSupport": false
       }', 'VIP会员年度套餐，优惠26%', 0, 1, 3),

       ('SVIP月度会员', 'svip_monthly', 2, 30, 19.90, 19.90, '{
         "maxWheelCount": -1,
         "exclusiveThemes": true,
         "priorityAudit": true,
         "advancedStats": true,
         "adFree": true,
         "vipSupport": true,
         "customContent": true
       }', 'SVIP会员月度套餐，无限转盘次数+专属权益', 0, 1, 4),

       ('SVIP季度会员', 'svip_quarterly', 2, 90, 49.90, 59.70, '{
         "maxWheelCount": -1,
         "exclusiveThemes": true,
         "priorityAudit": true,
         "advancedStats": true,
         "adFree": true,
         "vipSupport": true,
         "customContent": true
       }', 'SVIP会员季度套餐，优惠17%', 0, 1, 5),

       ('SVIP年度会员', 'svip_annual', 2, 365, 168.00, 238.80, '{
         "maxWheelCount": -1,
         "exclusiveThemes": true,
         "priorityAudit": true,
         "advancedStats": true,
         "adFree": true,
         "vipSupport": true,
         "customContent": true
       }', 'SVIP会员年度套餐，优惠30%', 1, 1, 6);
