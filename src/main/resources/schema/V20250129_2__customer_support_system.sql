-- 客服工单系统数据库迁移脚本
-- @author wheel-api
-- @date 2025-01-29

-- 1. 客服人员表
CREATE TABLE IF NOT EXISTS `support_staff` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '客服ID',
    `staff_name` VARCHAR(50) NOT NULL COMMENT '客服姓名',
    `staff_email` VARCHAR(100) DEFAULT NULL COMMENT '客服邮箱',
    `is_vip_dedicated` TINYINT DEFAULT 0 COMMENT '是否为VIP专属客服：0-否 1-是',
    `max_tickets` INT DEFAULT 50 COMMENT '最大同时处理工单数',
    `current_tickets` INT DEFAULT 0 COMMENT '当前处理工单数',
    `status` TINYINT DEFAULT 1 COMMENT '状态：0-离线 1-在线',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_is_vip_dedicated` (`is_vip_dedicated`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客服人员表';

-- 2. 工单表
CREATE TABLE IF NOT EXISTS `support_ticket` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '工单ID',
    `ticket_no` VARCHAR(32) NOT NULL COMMENT '工单编号',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `title` VARCHAR(200) NOT NULL COMMENT '工单标题',
    `description` TEXT COMMENT '问题描述',
    `category` VARCHAR(50) DEFAULT NULL COMMENT '问题分类',
    `priority` TINYINT DEFAULT 1 COMMENT '优先级：1-低 2-中 3-高 4-紧急',
    `status` TINYINT DEFAULT 1 COMMENT '状态：1-待处理 2-处理中 3-待回复 4-已解决 5-已关闭',
    `assigned_to` BIGINT DEFAULT NULL COMMENT '分配客服ID',
    `rating` TINYINT DEFAULT NULL COMMENT '满意度评分：1-5星',
    `rating_comment` VARCHAR(500) DEFAULT NULL COMMENT '评价内容',
    `resolved_at` DATETIME DEFAULT NULL COMMENT '解决时间',
    `closed_at` DATETIME DEFAULT NULL COMMENT '关闭时间',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by` BIGINT DEFAULT NULL COMMENT '创建人',
    `update_by` BIGINT DEFAULT NULL COMMENT '更新人',
    `deleted` TINYINT DEFAULT 0 COMMENT '删除标记：0-未删除 1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_ticket_no` (`ticket_no`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_assigned_to` (`assigned_to`),
    KEY `idx_status` (`status`),
    KEY `idx_priority` (`priority` DESC),
    KEY `idx_create_time` (`create_time` DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工单表';

-- 3. 工单回复表
CREATE TABLE IF NOT EXISTS `ticket_reply` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '回复ID',
    `ticket_id` BIGINT NOT NULL COMMENT '工单ID',
    `is_staff` TINYINT NOT NULL COMMENT '是否客服回复：0-用户 1-客服',
    `sender_id` BIGINT NOT NULL COMMENT '发送者ID',
    `sender_name` VARCHAR(50) DEFAULT NULL COMMENT '发送者姓名',
    `content` TEXT NOT NULL COMMENT '回复内容',
    `attachments` VARCHAR(1000) DEFAULT NULL COMMENT '附件URL（JSON数组）',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '删除标记：0-未删除 1-已删除',
    PRIMARY KEY (`id`),
    KEY `idx_ticket_id` (`ticket_id`),
    KEY `idx_create_time` (`create_time` ASC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工单回复表';

-- 4. 工单评价表
CREATE TABLE IF NOT EXISTS `ticket_rating` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '评价ID',
    `ticket_id` BIGINT NOT NULL COMMENT '工单ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `rating` TINYINT NOT NULL COMMENT '评分：1-5星',
    `comment` VARCHAR(500) DEFAULT NULL COMMENT '评价内容',
    `tags` VARCHAR(200) DEFAULT NULL COMMENT '评价标签（JSON数组）',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_ticket_id` (`ticket_id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工单评价表';

-- 5. 常见问题表
CREATE TABLE IF NOT EXISTS `faq` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'FAQ ID',
    `category` VARCHAR(50) NOT NULL COMMENT '问题分类',
    `question` VARCHAR(500) NOT NULL COMMENT '问题',
    `answer` TEXT NOT NULL COMMENT '答案',
    `view_count` INT DEFAULT 0 COMMENT '浏览次数',
    `helpful_count` INT DEFAULT 0 COMMENT '有帮助次数',
    `sort_order` INT DEFAULT 0 COMMENT '排序',
    `status` TINYINT DEFAULT 1 COMMENT '状态：0-禁用 1-启用',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_category` (`category`),
    KEY `idx_status_sort` (`status`, `sort_order` DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='常见问题表';

-- 插入示例客服人员
INSERT INTO `support_staff` (`staff_name`, `staff_email`, `is_vip_dedicated`, `max_tickets`, `status`) VALUES
('普通客服-小王', 'wang@example.com', 0, 50, 1),
('普通客服-小李', 'li@example.com', 0, 50, 1),
('VIP专属客服-张经理', 'zhang@example.com', 1, 30, 1);

-- 插入示例FAQ
INSERT INTO `faq` (`category`, `question`, `answer`, `sort_order`, `status`) VALUES
('账户问题', '如何修改个人信息？', '登录后进入"个人中心"-"编辑资料"，即可修改昵称、头像等信息。', 100, 1),
('账户问题', '忘记密码怎么办？', '在登录页面点击"忘记密码"，输入手机号获取验证码重置密码。', 90, 1),
('情侣功能', '如何邀请情侣？', '进入"情侣"页面，点击"邀请情侣"，分享邀请码给对方。对方输入邀请码即可建立关系。', 100, 1),
('情侣功能', '如何解除情侣关系？', '进入"情侣"页面，点击"管理"-"解除关系"，需双方确认后才能解除。', 80, 1),
('转盘功能', '转盘次数有限制吗？', '普通用户每日50次转盘机会，VIP用户无限制。', 100, 1),
('转盘功能', '如何添加自定义内容？', '进入"内容管理"页面，点击"+"添加新内容，提交审核通过后即可在转盘中出现。', 90, 1),
('会员功能', 'VIP有哪些特权？', 'VIP享有：无限转盘次数、专属主题、优先审核、高级统计、去广告等特权。', 100, 1),
('会员功能', 'SVIP比VIP多哪些特权？', 'SVIP额外享有：全部主题、最高审核优先级、VIP专属客服等特权。', 90, 1);
