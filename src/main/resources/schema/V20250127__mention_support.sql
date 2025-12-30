-- ================================================
-- 添加 @ 提及功能支持
-- 版本: 1.0.0
-- 创建时间: 2025-01-27
-- ================================================

USE `wheel_api`;

-- 为评论表添加提及用户字段
ALTER TABLE `moment_comment`
    ADD COLUMN `mentioned_user_ids` json DEFAULT NULL COMMENT '被@提及的用户ID列表';
