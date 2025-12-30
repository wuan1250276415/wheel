-- 聊天消息撤回字段
ALTER TABLE chat_message
    ADD COLUMN `recalled_by` bigint(20) NULL COMMENT '撤回操作用户ID' AFTER `status`,
    ADD COLUMN `recalled_at` datetime NULL COMMENT '撤回时间' AFTER `recalled_by`;

ALTER TABLE chat_message
    MODIFY COLUMN `status` tinyint(1) NOT NULL DEFAULT 1 COMMENT '消息状态：0-已删除，1-正常，2-已撤回';

-- 确保撤回字段默认可为空
UPDATE chat_message
SET recalled_by = NULL, recalled_at = NULL
WHERE status <> 2 AND recalled_by IS NOT NULL;
