-- ================================================
-- 转盘主题皮肤系统
-- 创建时间: 2025-01-27
-- 描述: 支持多种转盘主题，用户可购买和切换
-- ================================================

-- ================================================
-- 1. 皮肤模板表
-- ================================================
DROP TABLE IF EXISTS `wheel_theme`;
CREATE TABLE `wheel_theme`
(
    `id`           bigint(20)   NOT NULL AUTO_INCREMENT COMMENT '主题ID',
    `theme_name`   varchar(100) NOT NULL COMMENT '主题名称',
    `theme_key`    varchar(50)  NOT NULL COMMENT '主题标识key（如：romantic, tech, retro）',
    `description`  text COMMENT '主题描述',
    `theme_config` json         NOT NULL COMMENT '主题配置（CSS变量JSON）',
    `preview_url`  varchar(500) DEFAULT NULL COMMENT '预览图URL',
    `price`        int(11)      NOT NULL DEFAULT 0 COMMENT '积分价格（0表示免费）',
    `is_default`   tinyint(1)   NOT NULL DEFAULT 0 COMMENT '是否默认主题：0-否，1-是',
    `status`       tinyint(1)   NOT NULL DEFAULT 1 COMMENT '主题状态：0-禁用，1-启用',
    `sort_order`   int(11)      DEFAULT 0 COMMENT '排序权重',
    `create_time`  datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`  datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by`    bigint(20)   DEFAULT 0 COMMENT '创建人',
    `update_by`    bigint(20)   DEFAULT 0 COMMENT '更新人',
    `deleted`      tinyint(1)   NOT NULL DEFAULT 1 COMMENT '逻辑删除标志：0-已删除，1-未删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_theme_key` (`theme_key`),
    KEY            `idx_status` (`status`),
    KEY            `idx_sort_order` (`sort_order`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='转盘主题模板表';

-- ================================================
-- 2. 用户主题表
-- ================================================
DROP TABLE IF EXISTS `user_theme`;
CREATE TABLE `user_theme`
(
    `id`            bigint(20) NOT NULL AUTO_INCREMENT COMMENT '记录ID',
    `user_id`       bigint(20) NOT NULL COMMENT '用户ID',
    `theme_id`      bigint(20) NOT NULL COMMENT '主题ID',
    `is_active`     tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否使用中：0-否，1-是',
    `purchased_at`  datetime   DEFAULT NULL COMMENT '购买时间',
    `purchase_type` tinyint(1) DEFAULT 0 COMMENT '获取方式：0-免费，1-购买，2-赠送，3-活动',
    `create_time`   datetime   NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`   datetime   NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by`     bigint(20) DEFAULT 0 COMMENT '创建人',
    `update_by`     bigint(20) DEFAULT 0 COMMENT '更新人',
    `deleted`       tinyint(1) NOT NULL DEFAULT 1 COMMENT '逻辑删除标志：0-已删除，1-未删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_theme` (`user_id`, `theme_id`),
    KEY             `idx_user_id` (`user_id`),
    KEY             `idx_theme_id` (`theme_id`),
    KEY             `idx_is_active` (`is_active`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='用户主题拥有表';

-- ================================================
-- 3. 初始化默认主题数据
-- ================================================
INSERT INTO `wheel_theme` (`theme_name`, `theme_key`, `description`, `theme_config`, `preview_url`, `price`,
                           `is_default`, `status`, `sort_order`)
VALUES ('浪漫粉', 'romantic', '梦幻粉色渐变，温馨浪漫的情侣主题', '{
  "wheelBgGradient": "linear-gradient(135deg, #ffd1ff 0%, #fad0c4 100%)",
  "wheelBorderColor": "rgba(255, 255, 255, 0.6)",
  "wheelBoxShadow": "0 10rpx 30rpx rgba(255, 182, 193, 0.4), inset 0 0 40rpx rgba(255, 255, 255, 0.8)",
  "glowBgIdle": "radial-gradient(circle, rgba(255, 182, 193, 0.4) 0%, rgba(255, 105, 180, 0) 70%)",
  "glowBgActive": "radial-gradient(circle, rgba(255, 20, 147, 0.6) 0%, rgba(255, 105, 180, 0.2) 80%)",
  "ringOuterColor": "rgba(255, 255, 255, 0.6)",
  "ringInnerColor": "rgba(255, 255, 255, 0.8)",
  "ringInnerStyle": "dotted",
  "ringInnerWidth": "4rpx",
  "decoratorBg": "white",
  "decoratorShadow": "0 0 10rpx white",
  "btnBg": "white",
  "btnShadow": "0 8rpx 20rpx rgba(255, 105, 180, 0.3)",
  "btnIconColor": "#ff69b4",
  "btnIconContent": "❤",
  "centerDecoTextColor": "#fff",
  "centerDecoFontFamily": "Courier New, Courier, monospace",
  "popupBg": "rgba(0, 0, 0, 0.4)",
  "popupCardBg": "rgba(255, 255, 255, 0.95)",
  "popupTitleColor": "#888",
  "popupResultTextColor": "#d63384",
  "popupResultBorderColor": "#ffe4e1",
  "popupCloseBtnBg": "linear-gradient(135deg, #ff9a9e 0%, #ff6a88 100%)",
  "popupCloseBtnShadow": "0 10rpx 20rpx rgba(255, 106, 136, 0.3)"
}', '/static/themes/preview_romantic.png', 0, 1, 1, 1),

       ('赛博科技', 'tech', '未来科技感，蓝色霓虹赛博风格', '{
         "wheelBgGradient": "linear-gradient(135deg, #0d1a26 0%, #030a10 100%)",
         "wheelBorderColor": "rgba(0, 255, 255, 0.5)",
         "wheelBoxShadow": "0 0 40rpx rgba(0, 255, 255, 0.3), inset 0 0 20rpx rgba(0, 191, 255, 0.5)",
         "glowBgIdle": "radial-gradient(circle, rgba(0, 255, 255, 0.3) 0%, rgba(0, 255, 255, 0) 70%)",
         "glowBgActive": "radial-gradient(circle, rgba(0, 255, 255, 0.7) 0%, rgba(0, 255, 255, 0.1) 80%)",
         "ringOuterColor": "rgba(0, 255, 255, 0.4)",
         "ringInnerColor": "rgba(0, 255, 255, 0.6)",
         "ringInnerStyle": "solid",
         "ringInnerWidth": "1rpx",
         "decoratorBg": "cyan",
         "decoratorShadow": "0 0 12rpx cyan",
         "btnBg": "#0a192f",
         "btnShadow": "0 0 25rpx rgba(0, 255, 255, 0.4)",
         "btnIconColor": "#64ffda",
         "btnIconContent": "⏻",
         "centerDecoTextColor": "#64ffda",
         "centerDecoFontFamily": "Roboto Mono, monospace",
         "popupBg": "rgba(10, 25, 47, 0.6)",
         "popupCardBg": "linear-gradient(150deg, #0a192f, #133b5c)",
         "popupTitleColor": "#8892b0",
         "popupResultTextColor": "#64ffda",
         "popupResultBorderColor": "rgba(0, 255, 255, 0.5)",
         "popupCloseBtnBg": "#64ffda",
         "popupCloseBtnShadow": "0 0 20rpx rgba(100, 255, 218, 0.3)"
       }', '/static/themes/preview_tech.png', 100, 0, 1, 2),

       ('复古街机', 'retro', '80年代街机风格，霓虹色彩碰撞', '{
         "wheelBgGradient": "conic-gradient(from 90deg at 50% 50%, #ff00ff, #ffff00, #00ffff, #ff00ff)",
         "wheelBorderColor": "#000",
         "wheelBoxShadow": "8rpx 8rpx 0 #000, inset 0 0 10rpx #000",
         "glowBgIdle": "none",
         "glowBgActive": "none",
         "ringOuterColor": "#000",
         "ringInnerColor": "#FFF",
         "ringInnerStyle": "dashed",
         "ringInnerWidth": "4rpx",
         "decoratorBg": "yellow",
         "decoratorShadow": "none",
         "btnBg": "#ff00ff",
         "btnShadow": "6rpx 6rpx 0 #000",
         "btnIconColor": "yellow",
         "btnIconContent": "★",
         "centerDecoTextColor": "#fff",
         "centerDecoFontFamily": "Press Start 2P, cursive",
         "popupBg": "rgba(0, 0, 0, 0.5)",
         "popupCardBg": "#fff",
         "popupTitleColor": "#555",
         "popupResultTextColor": "#ff00ff",
         "popupResultBorderColor": "#000",
         "popupCloseBtnBg": "yellow",
         "popupCloseBtnShadow": "4rpx 4rpx 0 #000"
       }', '/static/themes/preview_retro.png', 150, 0, 1, 3),

       ('新春贺喜', 'festival', '中国传统新年主题，红金喜庆', '{
         "wheelBgGradient": "radial-gradient(circle, #ffd700, #c00)",
         "wheelBorderColor": "rgba(255, 215, 0, 0.8)",
         "wheelBoxShadow": "0 0 50rpx rgba(255, 0, 0, 0.5), inset 0 0 30rpx #ffeb3b",
         "glowBgIdle": "radial-gradient(circle, rgba(255, 215, 0, 0.4) 0%, rgba(255, 0, 0, 0) 70%)",
         "glowBgActive": "radial-gradient(circle, rgba(255, 223, 0, 0.7) 0%, rgba(255, 0, 0, 0.2) 80%)",
         "ringOuterColor": "rgba(255, 215, 0, 0.7)",
         "ringInnerColor": "rgba(255, 215, 0, 0.9)",
         "ringInnerStyle": "solid",
         "ringInnerWidth": "2rpx",
         "decoratorBg": "gold",
         "decoratorShadow": "0 0 10rpx gold",
         "btnBg": "#c00",
         "btnShadow": "0 5rpx 15rpx rgba(139, 0, 0, 0.4)",
         "btnIconColor": "#ffd700",
         "btnIconContent": "福",
         "btnIconFontFamily": "Ma Shan Zheng, cursive",
         "centerDecoTextColor": "#ffd700",
         "centerDecoFontFamily": "Ma Shan Zheng, cursive",
         "popupBg": "rgba(100, 0, 0, 0.3)",
         "popupCardBg": "radial-gradient(circle at top, #fffbf0, #fff2d0)",
         "popupTitleColor": "#a80000",
         "popupResultTextColor": "#c00",
         "popupResultBorderColor": "#ffd700",
         "popupCloseBtnBg": "linear-gradient(135deg, #ff5f5f, #c00)",
         "popupCloseBtnShadow": "0 10rpx 20rpx rgba(192, 0, 0, 0.3)"
       }', '/static/themes/preview_festival.png', 200, 0, 1, 4);
