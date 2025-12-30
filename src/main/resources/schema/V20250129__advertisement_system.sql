-- 广告系统数据库迁移脚本
-- @author wheel-api
-- @date 2025-01-29

-- 1. 广告位配置表
CREATE TABLE IF NOT EXISTS `ad_placement` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '广告位ID',
    `placement_key` VARCHAR(50) NOT NULL COMMENT '广告位标识key（如home_banner, wheel_bottom）',
    `placement_name` VARCHAR(100) NOT NULL COMMENT '广告位名称',
    `description` VARCHAR(255) DEFAULT NULL COMMENT '广告位描述',
    `max_ads` INT DEFAULT 1 COMMENT '最大同时展示广告数量',
    `width` INT DEFAULT NULL COMMENT '推荐宽度（px）',
    `height` INT DEFAULT NULL COMMENT '推荐高度（px）',
    `status` TINYINT DEFAULT 1 COMMENT '状态：0-禁用 1-启用',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_placement_key` (`placement_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='广告位配置表';

-- 2. 广告内容表
CREATE TABLE IF NOT EXISTS `advertisement` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '广告ID',
    `ad_name` VARCHAR(100) NOT NULL COMMENT '广告名称',
    `placement_id` BIGINT NOT NULL COMMENT '广告位ID',
    `ad_type` TINYINT NOT NULL COMMENT '广告类型：1-图片 2-视频 3-文字',
    `content_url` VARCHAR(500) DEFAULT NULL COMMENT '广告内容URL（图片/视频地址）',
    `content_text` VARCHAR(500) DEFAULT NULL COMMENT '广告文本内容',
    `link_url` VARCHAR(500) DEFAULT NULL COMMENT '点击跳转链接',
    `priority` INT DEFAULT 0 COMMENT '优先级（数值越大优先级越高）',
    `start_time` DATETIME DEFAULT NULL COMMENT '广告开始时间',
    `end_time` DATETIME DEFAULT NULL COMMENT '广告结束时间',
    `status` TINYINT DEFAULT 1 COMMENT '状态：0-禁用 1-启用',
    `impression_count` BIGINT DEFAULT 0 COMMENT '展示次数',
    `click_count` BIGINT DEFAULT 0 COMMENT '点击次数',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_placement_id` (`placement_id`),
    KEY `idx_status_priority` (`status`, `priority` DESC),
    KEY `idx_time_range` (`start_time`, `end_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='广告内容表';

-- 3. 广告展示记录表
CREATE TABLE IF NOT EXISTS `ad_impression` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '记录ID',
    `ad_id` BIGINT NOT NULL COMMENT '广告ID',
    `user_id` BIGINT DEFAULT NULL COMMENT '用户ID（未登录时为NULL）',
    `placement_key` VARCHAR(50) NOT NULL COMMENT '广告位标识',
    `impression_type` TINYINT NOT NULL COMMENT '类型：1-展示 2-点击',
    `device_type` VARCHAR(20) DEFAULT NULL COMMENT '设备类型',
    `ip_address` VARCHAR(50) DEFAULT NULL COMMENT 'IP地址',
    `user_agent` VARCHAR(255) DEFAULT NULL COMMENT 'User Agent',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_ad_id` (`ad_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='广告展示记录表';

-- 插入默认广告位配置
INSERT INTO `ad_placement` (`placement_key`, `placement_name`, `description`, `max_ads`, `width`, `height`, `status`) VALUES
('home_banner', '首页顶部横幅', '首页顶部横幅广告位', 1, 750, 200, 1),
('wheel_bottom', '转盘页底部', '转盘页面底部广告位', 1, 750, 150, 1),
('result_popup', '结果弹窗广告', '转盘结果展示弹窗中的广告', 1, 600, 400, 1);

-- 插入示例广告（可选，用于测试）
INSERT INTO `advertisement` (`ad_name`, `placement_id`, `ad_type`, `content_text`, `link_url`, `priority`, `status`) VALUES
('示例文字广告', 1, 3, '🎉 升级VIP，享受无广告体验！', '/pages/membership/membership', 100, 1);
