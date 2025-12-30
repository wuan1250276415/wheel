-- 任务挑战模块数据库表

-- 1. 任务模板表
CREATE TABLE IF NOT EXISTS `task_template` (
  `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
  `task_code` VARCHAR(50) NOT NULL UNIQUE COMMENT '任务唯一编码',
  `task_name` VARCHAR(100) NOT NULL COMMENT '任务名称',
  `task_type` TINYINT NOT NULL COMMENT '任务类型: 1-转盘挑战 2-情侣互动 3-探索挑战 4-社交挑战',
  `description` TEXT COMMENT '任务描述',
  `difficulty` TINYINT NOT NULL COMMENT '难度: 1-简单 2-中等 3-困难',
  `period_type` TINYINT NOT NULL COMMENT '周期类型: 1-每日 2-每周',
  `target_count` INT NOT NULL COMMENT '目标次数',
  `reward_type` TINYINT NOT NULL COMMENT '奖励类型: 1-虚拟勋章 2-转盘次数',
  `reward_value` INT NOT NULL COMMENT '奖励值',
  `status` TINYINT DEFAULT 1 COMMENT '状态: 0-禁用 1-启用',
  `create_by`   bigint(20)             DEFAULT 0 COMMENT '创建人',
  `update_by`   bigint(20)             DEFAULT 0 COMMENT '更新人',
  `deleted`     tinyint(1)    NOT NULL DEFAULT 0 COMMENT '逻辑删除标志：0-已删除，1-未删除',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  INDEX `idx_period_status` (`period_type`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='任务模板表';

-- 2. 用户任务表
CREATE TABLE IF NOT EXISTS `user_task` (
  `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `task_id` BIGINT NOT NULL COMMENT '任务模板ID',
  `current_count` INT DEFAULT 0 COMMENT '当前进度',
  `target_count` INT NOT NULL COMMENT '目标进度',
  `status` TINYINT DEFAULT 0 COMMENT '状态: 0-进行中 1-已完成 2-已领奖 3-已过期',
  `period_start` DATE NOT NULL COMMENT '周期开始日期',
  `period_end` DATE NOT NULL COMMENT '周期结束日期',
  `completed_at` DATETIME COMMENT '完成时间',
  `claimed_at` DATETIME COMMENT '领奖时间',
  `create_by`   bigint(20)             DEFAULT 0 COMMENT '创建人',
  `update_by`   bigint(20)             DEFAULT 0 COMMENT '更新人',
  `deleted`     tinyint(1)    NOT NULL DEFAULT 0 COMMENT '逻辑删除标志：0-已删除，1-未删除',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  UNIQUE KEY `uk_user_task_period` (`user_id`, `task_id`, `period_start`),
  INDEX `idx_user_status` (`user_id`, `status`),
  INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户任务表';

-- 3. 任务奖励表
CREATE TABLE IF NOT EXISTS `task_reward` (
  `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `task_id` BIGINT NOT NULL COMMENT '任务ID',
  `user_task_id` BIGINT NOT NULL UNIQUE COMMENT '用户任务ID',
  `reward_type` TINYINT NOT NULL COMMENT '奖励类型: 1-虚拟勋章 2-转盘次数',
  `reward_value` INT NOT NULL COMMENT '奖励值',
  `create_by`   bigint(20)             DEFAULT 0 COMMENT '创建人',
  `update_by`   bigint(20)             DEFAULT 0 COMMENT '更新人',
  `deleted`     tinyint(1)    NOT NULL DEFAULT 0 COMMENT '逻辑删除标志：0-已删除，1-未删除',
  `claimed_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '领取时间',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  INDEX `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='任务奖励表';

-- 4. 插入默认任务模板
INSERT INTO `task_template` (`task_code`, `task_name`, `task_type`, `description`, `difficulty`, `period_type`, `target_count`, `reward_type`, `reward_value`, `status`)
VALUES
('DAILY_SPIN_3', '每日转盘新手', 1, '每天转动转盘3次', 1, 1, 3, 2, 1, 1),
('DAILY_SPIN_5', '每日转盘达人', 1, '每天转动转盘5次', 2, 1, 5, 2, 2, 1),
('DAILY_SPIN_10', '每日转盘狂热者', 1, '每天转动转盘10次', 3, 1, 10, 2, 3, 1),
('WEEKLY_COUPLE_3', '情侣互动体验', 2, '本周与情侣一起转盘3次', 1, 2, 3, 1, 1, 1),
('WEEKLY_COUPLE_7', '情侣互动达人', 2, '本周与情侣一起转盘7次', 2, 2, 7, 1, 2, 1),
('WEEKLY_EXPLORE_3', '探索新类别', 3, '本周尝试3个不同的转盘分类', 1, 2, 3, 2, 2, 1),
('WEEKLY_EXPLORE_5', '全能探索者', 3, '本周尝试5个不同的转盘分类', 3, 2, 5, 2, 3, 1);
