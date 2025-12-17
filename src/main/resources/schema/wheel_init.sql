-- ================================================
-- 情侣情趣转盘小程序数据库初始化脚本
-- 版本: 1.0.0
-- 作者: wheel-api
-- 创建时间: 2025-12-16
-- ================================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS `wheel_api` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE `wheel_api`;

-- ================================================
-- 1. 用户表
-- ================================================
DROP TABLE IF EXISTS `wheel_user`;
CREATE TABLE `wheel_user` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `phone_number` varchar(255) NOT NULL COMMENT '手机号（加密）',
  `nickname` varchar(100) DEFAULT NULL COMMENT '用户昵称',
  `avatar_url` varchar(500) DEFAULT NULL COMMENT '头像URL',
  `gender` tinyint(1) DEFAULT 0 COMMENT '性别：0-未知，1-男，2-女',
  `age` int(3) DEFAULT NULL COMMENT '年龄',
  `city` varchar(100) DEFAULT NULL COMMENT '城市',
  `status` tinyint(1) DEFAULT 1 COMMENT '用户状态：0-禁用，1-正常，2-待审核',
  `last_login_time` datetime DEFAULT NULL COMMENT '最后登录时间',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` bigint(20) DEFAULT 0 COMMENT '创建人',
  `updated_by` bigint(20) DEFAULT 0 COMMENT '更新人',
  `deleted` tinyint(1) DEFAULT 1 COMMENT '逻辑删除标志：0-已删除，1-未删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_phone_number` (`phone_number`),
  KEY `idx_status` (`status`),
  KEY `idx_created_time` (`created_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- ================================================
-- 2. 情侣关系表
-- ================================================
DROP TABLE IF EXISTS `couple_relationship`;
CREATE TABLE `couple_relationship` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '关系ID',
  `user_id_1` bigint(20) NOT NULL COMMENT '用户ID-发起方',
  `user_id_2` bigint(20) NOT NULL COMMENT '用户ID-接受方',
  `status` tinyint(1) DEFAULT 0 COMMENT '关系状态：0-待确认，1-已确认，2-已解除',
  `invite_code` varchar(50) NOT NULL COMMENT '邀请码',
  `confirmed_at` datetime DEFAULT NULL COMMENT '确认时间',
  `unbound_at` datetime DEFAULT NULL COMMENT '解除时间',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` bigint(20) DEFAULT 0 COMMENT '创建人',
  `updated_by` bigint(20) DEFAULT 0 COMMENT '更新人',
  `deleted` tinyint(1) DEFAULT 1 COMMENT '逻辑删除标志：0-已删除，1-未删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_ids` (`user_id_1`, `user_id_2`),
  UNIQUE KEY `uk_invite_code` (`invite_code`),
  KEY `idx_user_id_1` (`user_id_1`),
  KEY `idx_user_id_2` (`user_id_2`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='情侣关系表';

-- ================================================
-- 3. 转盘分类表
-- ================================================
DROP TABLE IF EXISTS `wheel_category`;
CREATE TABLE `wheel_category` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '分类ID',
  `category_name` varchar(100) NOT NULL COMMENT '分类名称',
  `description` text COMMENT '分类描述',
  `sort_order` int(11) DEFAULT 0 COMMENT '排序权重',
  `icon_url` varchar(500) DEFAULT NULL COMMENT '分类图标URL',
  `theme_color` varchar(20) DEFAULT NULL COMMENT '主题色',
  `status` tinyint(1) DEFAULT 1 COMMENT '分类状态：0-禁用，1-启用',
  `is_system` tinyint(1) DEFAULT 0 COMMENT '是否系统内置：0-否，1-是',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` bigint(20) DEFAULT 0 COMMENT '创建人',
  `updated_by` bigint(20) DEFAULT 0 COMMENT '更新人',
  `deleted` tinyint(1) DEFAULT 1 COMMENT '逻辑删除标志：0-已删除，1-未删除',
  PRIMARY KEY (`id`),
  KEY `idx_status` (`status`),
  KEY `idx_sort_order` (`sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='转盘分类表';

-- ================================================
-- 4. 转盘内容表
-- ================================================
DROP TABLE IF EXISTS `wheel_content`;
CREATE TABLE `wheel_content` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '内容ID',
  `category_id` bigint(20) NOT NULL COMMENT '分类ID',
  `content_text` varchar(200) NOT NULL COMMENT '内容文本',
  `weight` double(10,2) DEFAULT 1.00 COMMENT '权重值',
  `create_user_id` bigint(20) DEFAULT NULL COMMENT '创建用户ID',
  `audit_status` tinyint(1) DEFAULT 0 COMMENT '审核状态：0-待审核，1-已通过，2-已拒绝',
  `auditor_id` bigint(20) DEFAULT NULL COMMENT '审核人ID',
  `audited_at` datetime DEFAULT NULL COMMENT '审核时间',
  `audit_comment` text COMMENT '审核意见',
  `tags` varchar(500) DEFAULT NULL COMMENT '标签（JSON）',
  `is_system` tinyint(1) DEFAULT 0 COMMENT '是否系统内置：0-否，1-是',
  `status` tinyint(1) DEFAULT 1 COMMENT '内容状态：0-禁用，1-启用',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` bigint(20) DEFAULT 0 COMMENT '创建人',
  `updated_by` bigint(20) DEFAULT 0 COMMENT '更新人',
  `deleted` tinyint(1) DEFAULT 1 COMMENT '逻辑删除标志：0-已删除，1-未删除',
  PRIMARY KEY (`id`),
  KEY `idx_category_id` (`category_id`),
  KEY `idx_audit_status` (`audit_status`),
  KEY `idx_create_user_id` (`create_user_id`),
  KEY `idx_weight` (`weight`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='转盘内容表';

-- ================================================
-- 5. 转盘配置表
-- ================================================
DROP TABLE IF EXISTS `wheel_config`;
CREATE TABLE `wheel_config` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '配置ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `wheel_radius` int(11) DEFAULT 150 COMMENT '转盘半径（像素）',
  `option_count` int(11) DEFAULT 8 COMMENT '选项数量',
  `animation_duration` int(11) DEFAULT 3000 COMMENT '动画时长（毫秒）',
  `theme_style` tinyint(1) DEFAULT 0 COMMENT '主题风格：0-默认，1-浪漫，2-可爱，3-简约',
  `show_history` tinyint(1) DEFAULT 1 COMMENT '是否显示历史：0-否，1-是',
  `sound_effect` tinyint(1) DEFAULT 1 COMMENT '声音效果：0-关闭，1-开启',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `updated_by` bigint(20) DEFAULT 0 COMMENT '更新人',
  `created_by` bigint(20) DEFAULT 0 COMMENT '创建人',
  `deleted` tinyint(1) DEFAULT 1 COMMENT '逻辑删除标志：0-已删除，1-未删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='转盘配置表';

-- ================================================
-- 6. 转盘记录表（按月分区）
-- ================================================
DROP TABLE IF EXISTS `wheel_spin_record`;
CREATE TABLE `wheel_spin_record` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `content_id` bigint(20) NOT NULL COMMENT '内容ID',
  `result_text` varchar(200) NOT NULL COMMENT '结果文本',
  `category_id` bigint(20) NOT NULL COMMENT '分类ID',
  `spin_duration` bigint(20) DEFAULT NULL COMMENT '旋转时间（毫秒）',
  `spin_time` datetime NOT NULL COMMENT '旋转时间',
  `ip_address` varchar(45) DEFAULT NULL COMMENT 'IP地址',
  `device_id` varchar(100) DEFAULT NULL COMMENT '设备ID',
  `client_type` tinyint(1) DEFAULT 0 COMMENT '客户端类型：0-未知，1-微信小程序，2-H5，3-APP',
  `is_anomaly` tinyint(1) DEFAULT 0 COMMENT '是否异常：0-否，1-是',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `created_by` bigint(20) DEFAULT 0 COMMENT '创建人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `updated_by` bigint(20) DEFAULT 0 COMMENT '更新人',
  `deleted` tinyint(1) DEFAULT 1 COMMENT '逻辑删除标志：0-已删除，1-未删除',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_content_id` (`content_id`),
  KEY `idx_category_id` (`category_id`),
  KEY `idx_spin_time` (`spin_time`),
  KEY `idx_ip_address` (`ip_address`),
  KEY `idx_device_id` (`device_id`),
  KEY `idx_is_anomaly` (`is_anomaly`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='转盘记录表'
  PARTITION BY RANGE (YEAR(`spin_time`)*100 + MONTH(`spin_time`)) (
    PARTITION p202501 VALUES LESS THAN (202502),
    PARTITION p202502 VALUES LESS THAN (202503),
    PARTITION p202503 VALUES LESS THAN (202504),
    PARTITION p202504 VALUES LESS THAN (202505),
    PARTITION p202505 VALUES LESS THAN (202506),
    PARTITION p202506 VALUES LESS THAN (202507),
    PARTITION p202507 VALUES LESS THAN (202508),
    PARTITION p202508 VALUES LESS THAN (202509),
    PARTITION p202509 VALUES LESS THAN (202510),
    PARTITION p202510 VALUES LESS THAN (202511),
    PARTITION p202511 VALUES LESS THAN (202512),
    PARTITION p202512 VALUES LESS THAN (202601),
    PARTITION pmax VALUES LESS THAN MAXVALUE
  );

-- ================================================
-- 7. 用户行为统计表
-- ================================================
DROP TABLE IF EXISTS `user_behavior_stats`;
CREATE TABLE `user_behavior_stats` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '统计ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `stats_date` date NOT NULL COMMENT '统计日期',
  `spin_count` int(11) DEFAULT 0 COMMENT '转盘次数',
  `usage_duration` int(11) DEFAULT 0 COMMENT '使用时长（分钟）',
  `preferred_category_id` bigint(20) DEFAULT NULL COMMENT '偏好分类ID',
  `avg_spin_duration` bigint(20) DEFAULT NULL COMMENT '平均转盘时长（毫秒）',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) DEFAULT 1 COMMENT '逻辑删除标志：0-已删除，1-未删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_date` (`user_id`, `stats_date`),
  KEY `idx_stats_date` (`stats_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户行为统计表';

-- ================================================
-- 8. 内容审核日志表
-- ================================================
DROP TABLE IF EXISTS `content_audit_log`;
CREATE TABLE `content_audit_log` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `content_id` bigint(20) NOT NULL COMMENT '内容ID',
  `submit_user_id` bigint(20) NOT NULL COMMENT '提交用户ID',
  `audit_status` tinyint(1) NOT NULL COMMENT '审核状态：0-待审核，1-已通过，2-已拒绝',
  `auditor_id` bigint(20) DEFAULT NULL COMMENT '审核人ID',
  `audited_at` datetime DEFAULT NULL COMMENT '审核时间',
  `audit_comment` text COMMENT '审核意见',
  `ai_audit_result` text COMMENT 'AI预审结果（JSON）',
  `audit_type` tinyint(1) DEFAULT 0 COMMENT '审核类型：0-自动审核，1-人工审核',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `deleted` tinyint(1) DEFAULT 1 COMMENT '逻辑删除标志：0-已删除，1-未删除',
  PRIMARY KEY (`id`),
  KEY `idx_content_id` (`content_id`),
  KEY `idx_submit_user_id` (`submit_user_id`),
  KEY `idx_audit_status` (`audit_status`),
  KEY `idx_audited_at` (`audited_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='内容审核日志表';

-- ================================================
-- 9. 内容举报表
-- ================================================
DROP TABLE IF EXISTS `content_report`;
CREATE TABLE `content_report` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '举报ID',
  `content_id` bigint(20) NOT NULL COMMENT '内容ID',
  `reporter_user_id` bigint(20) NOT NULL COMMENT '举报人用户ID',
  `report_reason` tinyint(1) NOT NULL COMMENT '举报原因：0-色情低俗，1-暴力血腥，2-政治敏感，3-其他',
  `report_description` text COMMENT '举报详细描述',
  `handle_status` tinyint(1) DEFAULT 0 COMMENT '处理状态：0-待处理，1-已处理，2-已忽略',
  `handler_id` bigint(20) DEFAULT NULL COMMENT '处理人ID',
  `handled_at` datetime DEFAULT NULL COMMENT '处理时间',
  `handle_result` text COMMENT '处理结果',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `deleted` tinyint(1) DEFAULT 1 COMMENT '逻辑删除标志：0-已删除，1-未删除',
  PRIMARY KEY (`id`),
  KEY `idx_content_id` (`content_id`),
  KEY `idx_reporter_user_id` (`reporter_user_id`),
  KEY `idx_handle_status` (`handle_status`),
  KEY `idx_created_time` (`created_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='内容举报表';

-- ================================================
-- 插入初始数据
-- ================================================

-- 插入默认分类数据
INSERT INTO `wheel_category` (`category_name`, `description`, `sort_order`, `icon_url`, `theme_color`, `status`, `is_system`) VALUES
('聊天话题', '增进了解的有趣话题', 100, '/icons/chat.png', '#FF6B9D', 1, 1),
('亲密游戏', '增进感情的小游戏', 90, '/icons/game.png', '#C44569', 1, 1),
('约会建议', '浪漫约会活动推荐', 80, '/icons/date.png', '#FFA07A', 1, 1),
('生活服务', '日常生活互助任务', 70, '/icons/service.png', '#98D8C8', 1, 1),
('运动健身', '情侣运动挑战', 60, '/icons/sport.png', '#F7DC6F', 1, 1),
('美食探索', '一起品尝美食', 50, '/icons/food.png', '#BB8FCE', 1, 1),
('学习成长', '共同学习新技能', 40, '/icons/study.png', '#85C1E9', 1, 1),
('娱乐休闲', '轻松有趣的娱乐活动', 30, '/icons/entertainment.png', '#82E0AA', 1, 1);

-- 插入示例转盘内容（聊天话题）
INSERT INTO `wheel_content` (`category_id`, `content_text`, `weight`, `is_system`, `audit_status`) VALUES
(1, '今天最开心的事情是什么？', 1.0, 1, 1),
(1, '如果可以环球旅行，你最想去哪里？', 1.0, 1, 1),
(1, '分享一个你的童年趣事', 1.0, 1, 1),
(1, '你最喜欢的一首歌是什么？为什么？', 1.0, 1, 1),
(1, '描述一下你理想中的完美一天', 1.0, 1, 1),
(1, '你最想拥有的超能力是什么？', 1.0, 1, 1),
(1, '如果回到过去，你想对10年前的自己说什么？', 1.0, 1, 1),
(1, '分享一个你的小癖好或习惯', 1.0, 1, 1);

-- 插入示例转盘内容（亲密游戏）
INSERT INTO `wheel_content` (`category_id`, `content_text`, `weight`, `is_system`, `audit_status`) VALUES
(2, '互相给对方一个拥抱30秒', 1.0, 1, 1),
(2, '一起做10个深蹲', 1.0, 1, 1),
(2, '闭眼让对方喂你吃一颗糖果', 1.0, 1, 1),
(2, '互相夸对方3个优点', 1.0, 1, 1),
(2, '一起看日落并分享感受', 1.0, 1, 1),
(2, '给对方按摩5分钟', 1.0, 1, 1),
(2, '一起做一道简单的菜', 1.0, 1, 1),
(2, '模仿对方最经典的动作或表情', 1.0, 1, 1);

-- ================================================
-- 创建存储过程（可选）
-- ================================================

DELIMITER $$

-- 生成邀请码的存储过程
CREATE PROCEDURE GenerateInviteCode(
    OUT inviteCode VARCHAR(50)
)
BEGIN
    DECLARE chars VARCHAR(62) DEFAULT 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789';
    DECLARE i INT DEFAULT 0;
    DECLARE result VARCHAR(50) DEFAULT '';
    DECLARE len INT DEFAULT 8;

    WHILE i < len DO
        SET result = CONCAT(result, SUBSTRING(chars, FLOOR(RAND() * LENGTH(chars)) + 1, 1));
        SET i = i + 1;
    END WHILE;

    -- 检查邀请码是否已存在
    WHILE EXISTS (SELECT 1 FROM couple_relationship WHERE invite_code = result) DO
        SET i = 0;
        SET result = '';
        WHILE i < len DO
            SET result = CONCAT(result, SUBSTRING(chars, FLOOR(RAND() * LENGTH(chars)) + 1, 1));
            SET i = i + 1;
        END WHILE;
    END WHILE;

    SET inviteCode = result;
END$$

DELIMITER ;

-- ================================================
-- 创建触发器（可选）
-- ================================================

DELIMITER $$

-- 自动生成邀请码的触发器
CREATE TRIGGER tr_couple_relationship_generate_code
BEFORE INSERT ON couple_relationship
FOR EACH ROW
BEGIN
    CALL GenerateInviteCode(@code);
    SET NEW.invite_code = @code;
END$$

DELIMITER ;

-- ================================================
-- 创建视图（可选）
-- ================================================

-- 用户转盘统计视图
CREATE VIEW v_user_wheel_stats AS
SELECT
    u.id AS user_id,
    u.nickname,
    u.phone_number,
    COUNT(r.id) AS total_spins,
    COUNT(CASE WHEN r.created_time >= CURDATE() THEN 1 END) AS today_spins,
    c.category_name AS most_used_category
FROM wheel_user u
LEFT JOIN wheel_spin_record r ON u.id = r.user_id
LEFT JOIN wheel_category c ON r.category_id = c.id
WHERE u.status = 1
GROUP BY u.id, u.nickname, u.phone_number;

-- ================================================
-- 数据库初始化完成
-- ================================================
