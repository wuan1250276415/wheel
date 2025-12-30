-- 为wheel_theme表添加会员等级要求字段
ALTER TABLE wheel_api.wheel_theme
    ADD COLUMN required_tier INT DEFAULT 0 COMMENT '会员等级要求：0-无要求，1-VIP，2-SVIP';

-- 更新现有主题数据示例（根据实际业务需求调整）
-- UPDATE wheel_api.wheel_theme SET required_tier = 1 WHERE theme_key IN ('premium_theme_1', 'premium_theme_2');
-- UPDATE wheel_api.wheel_theme SET required_tier = 2 WHERE theme_key IN ('exclusive_theme_1', 'exclusive_theme_2');
