-- 为wheel_content表添加审核优先级字段
ALTER TABLE wheel_api.wheel_content
    ADD COLUMN audit_priority INT DEFAULT 1 COMMENT '审核优先级：1-普通，2-VIP，3-SVIP';

-- 为审核优先级和审核状态创建索引，优化审核队列查询
CREATE INDEX idx_audit_priority_status ON wheel_api.wheel_content(audit_priority DESC, audit_status, create_time);
