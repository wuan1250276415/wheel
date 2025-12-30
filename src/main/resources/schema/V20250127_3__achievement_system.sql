-- 成就系统数据库表

-- 1. 成就模板表
CREATE TABLE IF NOT EXISTS `achievement_template` (
  `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
  `achievement_code` VARCHAR(50) NOT NULL UNIQUE COMMENT '成就唯一编码',
  `achievement_name` VARCHAR(100) NOT NULL COMMENT '成就名称',
  `description` TEXT COMMENT '成就描述',
  `icon_url` VARCHAR(500) COMMENT '图标URL',
  `rarity` TINYINT NOT NULL COMMENT '稀有度: 1-普通 2-稀有 3-史诗 4-传说',
  `unlock_condition` JSON COMMENT '解锁条件(JSON)',
  `is_hidden` TINYINT DEFAULT 0 COMMENT '是否隐藏: 0-否 1-是',
  `category` TINYINT COMMENT '类别: 1-转盘类 2-情侣类 3-探索类 4-社交类',
  `status` TINYINT DEFAULT 1 COMMENT '状态: 0-禁用 1-启用',
  `create_by` BIGINT(20) DEFAULT 0 COMMENT '创建人',
  `update_by` BIGINT(20) DEFAULT 0 COMMENT '更新人',
  `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除标志',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  INDEX `idx_rarity` (`rarity`),
  INDEX `idx_category` (`category`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='成就模板表';

-- 2. 用户成就表
CREATE TABLE IF NOT EXISTS `user_achievement` (
  `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `achievement_id` BIGINT NOT NULL COMMENT '成就ID',
  `progress` INT DEFAULT 0 COMMENT '当前进度',
  `target_value` INT DEFAULT 0 COMMENT '目标值',
  `is_unlocked` TINYINT DEFAULT 0 COMMENT '是否解锁: 0-未解锁 1-已解锁',
  `unlocked_at` DATETIME COMMENT '解锁时间',
  `create_by` BIGINT(20) DEFAULT 0 COMMENT '创建人',
  `update_by` BIGINT(20) DEFAULT 0 COMMENT '更新人',
  `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除标志',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  UNIQUE KEY `uk_user_achievement` (`user_id`, `achievement_id`),
  INDEX `idx_user_unlocked` (`user_id`, `is_unlocked`),
  INDEX `idx_unlocked_time` (`unlocked_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户成就表';

-- 3. 插入默认成就模板
INSERT INTO `achievement_template` (`achievement_code`, `achievement_name`, `description`, `rarity`, `unlock_condition`, `is_hidden`, `category`, `status`)
VALUES
-- 转盘类成就
('SPIN_NEWBIE', '转盘新手', '完成首次转盘', 1, '{"type":"spin_count","value":1}', 0, 1, 1),
('SPIN_MASTER_10', '转盘达人', '累计转盘10次', 1, '{"type":"spin_count","value":10}', 0, 1, 1),
('SPIN_MASTER_50', '转盘专家', '累计转盘50次', 2, '{"type":"spin_count","value":50}', 0, 1, 1),
('SPIN_MASTER_100', '转盘大师', '累计转盘100次', 3, '{"type":"spin_count","value":100}', 0, 1, 1),
('SPIN_LEGEND', '转盘传说', '累计转盘500次', 4, '{"type":"spin_count","value":500}', 0, 1, 1),
('DAILY_SPIN_7', '坚持不懈', '连续7天转盘', 2, '{"type":"consecutive_days","value":7}', 0, 1, 1),

-- 情侣类成就
('COUPLE_FIRST', '情侣先锋', '成功配对情侣关系', 1, '{"type":"couple_paired","value":1}', 0, 2, 1),
('COUPLE_DAYS_30', '甜蜜30天', '情侣关系保持30天', 2, '{"type":"couple_days","value":30}', 0, 2, 1),
('COUPLE_DAYS_100', '百日纪念', '情侣关系保持100天', 3, '{"type":"couple_days","value":100}', 0, 2, 1),
('COUPLE_SPIN_10', '默契搭档', '与情侣共同转盘10次', 2, '{"type":"couple_spin","value":10}', 0, 2, 1),

-- 探索类成就
('EXPLORE_CATEGORY_3', '好奇宝宝', '尝试3个不同分类', 1, '{"type":"category_count","value":3}', 0, 3, 1),
('EXPLORE_CATEGORY_ALL', '全能探索者', '尝试所有分类', 3, '{"type":"category_all","value":1}', 0, 3, 1),
('CONTENT_CREATOR', '内容创造者', '提交5条原创内容', 2, '{"type":"content_create","value":5}', 0, 3, 1),

-- 社交类成就
('SHARE_FIRST', '乐于分享', '首次分享转盘结果', 1, '{"type":"share_count","value":1}', 0, 4, 1),
('SHARE_MASTER', '分享达人', '分享转盘结果10次', 2, '{"type":"share_count","value":10}', 0, 4, 1),

-- 隐藏成就
('LUCKY_STAR', '幸运之星', '单日转盘20次', 4, '{"type":"daily_spin","value":20}', 1, 1, 1),
('NIGHT_OWL', '夜猫子', '在凌晨2-4点转盘', 3, '{"type":"time_range","start":"02:00","end":"04:00"}', 1, 1, 1);
