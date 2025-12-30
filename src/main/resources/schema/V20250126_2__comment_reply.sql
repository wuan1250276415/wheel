-- ================================================
-- 评论回复功能：添加父评论ID字段
-- 版本: 1.1.0
-- 创建时间: 2025-01-26
-- ================================================

USE `wheel_api`;

-- 添加父评论ID字段
ALTER TABLE `moment_comment`
ADD COLUMN `parent_comment_id` bigint(20) DEFAULT NULL COMMENT '父评论ID（用于回复）' AFTER `moment_id`,
ADD COLUMN `reply_to_user_id` bigint(20) DEFAULT NULL COMMENT '回复的目标用户ID' AFTER `parent_comment_id`,
ADD COLUMN `reply_to_nickname` varchar(100) DEFAULT NULL COMMENT '回复的目标用户昵称' AFTER `reply_to_user_id`,
ADD INDEX `idx_parent_comment` (`parent_comment_id`);
