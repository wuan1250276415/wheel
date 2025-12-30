-- ============================================
-- 管理后台RBAC权限系统数据库迁移脚本
-- 版本: V20250130
-- 描述: 创建管理员、角色、权限相关表及初始化数据
-- ============================================

-- 1. 角色表
CREATE TABLE IF NOT EXISTS sys_role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    role_code VARCHAR(50) NOT NULL UNIQUE COMMENT '角色编码：ROLE_ADMIN, ROLE_AUDITOR, ROLE_CUSTOMER_SERVICE',
    role_name VARCHAR(100) NOT NULL COMMENT '角色名称',
    description VARCHAR(500) COMMENT '角色描述',
    status TINYINT DEFAULT 1 COMMENT '状态：0=禁用, 1=启用',
    sort_order INT DEFAULT 0 COMMENT '排序权重',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0=正常, 1=已删除',
    INDEX idx_role_code (role_code),
    INDEX idx_status (status),
    INDEX idx_deleted (deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统角色表';

-- 2. 权限表
CREATE TABLE IF NOT EXISTS sys_permission (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    permission_code VARCHAR(100) NOT NULL UNIQUE COMMENT '权限编码：membership:plan:create',
    permission_name VARCHAR(100) NOT NULL COMMENT '权限名称',
    resource_type VARCHAR(20) COMMENT '资源类型：menu, button, api',
    resource_path VARCHAR(200) COMMENT 'API路径或菜单路径',
    parent_id BIGINT DEFAULT 0 COMMENT '父权限ID',
    description VARCHAR(500) COMMENT '权限描述',
    status TINYINT DEFAULT 1 COMMENT '状态：0=禁用, 1=启用',
    sort_order INT DEFAULT 0 COMMENT '排序权重',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0=正常, 1=已删除',
    INDEX idx_permission_code (permission_code),
    INDEX idx_parent (parent_id),
    INDEX idx_deleted (deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统权限表';

-- 3. 角色权限关联表
CREATE TABLE IF NOT EXISTS sys_role_permission (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    permission_id BIGINT NOT NULL COMMENT '权限ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_role_permission (role_id, permission_id),
    INDEX idx_role (role_id),
    INDEX idx_permission (permission_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色权限关联表';

-- 4. 管理员表
CREATE TABLE IF NOT EXISTS sys_admin (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '登录账号',
    password VARCHAR(255) NOT NULL COMMENT '加密密码(BCrypt)',
    real_name VARCHAR(100) COMMENT '真实姓名',
    phone VARCHAR(20) COMMENT '手机号',
    email VARCHAR(100) COMMENT '邮箱',
    avatar_url VARCHAR(500) COMMENT '头像URL',
    status TINYINT DEFAULT 1 COMMENT '状态：0=禁用, 1=正常',
    last_login_time DATETIME COMMENT '最后登录时间',
    last_login_ip VARCHAR(50) COMMENT '最后登录IP',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0=正常, 1=已删除',
    INDEX idx_username (username),
    INDEX idx_status (status),
    INDEX idx_deleted (deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统管理员表';

-- 5. 管理员角色关联表
CREATE TABLE IF NOT EXISTS sys_admin_role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    admin_id BIGINT NOT NULL COMMENT '管理员ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_admin_role (admin_id, role_id),
    INDEX idx_admin (admin_id),
    INDEX idx_role (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='管理员角色关联表';

-- 6. 操作日志表
CREATE TABLE IF NOT EXISTS sys_operation_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    admin_id BIGINT COMMENT '操作人ID',
    admin_name VARCHAR(100) COMMENT '操作人姓名',
    operation VARCHAR(50) COMMENT '操作类型：CREATE, UPDATE, DELETE, APPROVE, REJECT',
    module VARCHAR(50) COMMENT '模块：membership, audit, user',
    resource_id BIGINT COMMENT '资源ID',
    resource_type VARCHAR(50) COMMENT '资源类型',
    detail TEXT COMMENT '详细信息（JSON格式）',
    ip_address VARCHAR(50) COMMENT '操作IP',
    user_agent VARCHAR(500) COMMENT '用户代理',
    execute_time INT COMMENT '执行时长（毫秒）',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_admin (admin_id),
    INDEX idx_module (module),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='操作日志表';

-- ============================================
-- 初始化基础角色数据
-- ============================================
INSERT INTO sys_role (role_code, role_name, description, sort_order) VALUES
('ROLE_SUPER_ADMIN', '超级管理员', '拥有所有权限，可管理系统所有功能', 1),
('ROLE_ADMIN', '管理员', '会员管理、订单管理权限', 2),
('ROLE_AUDITOR', '内容审核员', '内容审核相关权限', 3),
('ROLE_CUSTOMER_SERVICE', '客服', '客服工单处理权限', 4);

-- ============================================
-- 初始化权限数据（树形结构）
-- ============================================

-- 会员管理模块权限
INSERT INTO sys_permission (permission_code, permission_name, resource_type, resource_path, parent_id, sort_order) VALUES
-- 一级菜单
('membership', '会员管理', 'menu', '/membership', 0, 1),
-- 二级菜单 - 套餐管理
('membership:plan', '套餐管理', 'menu', '/membership/plan', 1, 1),
('membership:plan:view', '查看套餐', 'button', NULL, 2, 1),
('membership:plan:create', '新建套餐', 'button', NULL, 2, 2),
('membership:plan:update', '编辑套餐', 'button', NULL, 2, 3),
('membership:plan:delete', '删除套餐', 'button', NULL, 2, 4),
-- 二级菜单 - 用户会员管理
('membership:user', '用户会员管理', 'menu', '/membership/user', 1, 2),
('membership:user:view', '查看用户会员', 'button', NULL, 7, 1),
('membership:user:extend', '延期会员', 'button', NULL, 7, 2),
('membership:user:upgrade', '升级会员', 'button', NULL, 7, 3),
-- 二级菜单 - 订单管理
('membership:order', '订单管理', 'menu', '/membership/order', 1, 3),
('membership:order:view', '查看订单', 'button', NULL, 11, 1),
('membership:order:refund', '退款处理', 'button', NULL, 11, 2);

-- 内容审核模块权限
INSERT INTO sys_permission (permission_code, permission_name, resource_type, resource_path, parent_id, sort_order) VALUES
-- 一级菜单
('audit', '内容审核', 'menu', '/audit', 0, 2),
-- 二级菜单 - 审核队列
('audit:queue', '审核队列', 'menu', '/audit/queue', 14, 1),
('audit:queue:view', '查看队列', 'button', NULL, 15, 1),
('audit:queue:approve', '通过审核', 'button', NULL, 15, 2),
('audit:queue:reject', '拒绝审核', 'button', NULL, 15, 3),
-- 二级菜单 - 审核历史
('audit:history', '审核历史', 'menu', '/audit/history', 14, 2),
('audit:history:view', '查看历史', 'button', NULL, 19, 1);

-- 系统管理模块权限
INSERT INTO sys_permission (permission_code, permission_name, resource_type, resource_path, parent_id, sort_order) VALUES
-- 一级菜单
('system', '系统管理', 'menu', '/system', 0, 3),
-- 二级菜单 - 管理员管理
('system:admin', '管理员管理', 'menu', '/system/admin', 21, 1),
('system:admin:view', '查看管理员', 'button', NULL, 22, 1),
('system:admin:create', '新建管理员', 'button', NULL, 22, 2),
('system:admin:update', '编辑管理员', 'button', NULL, 22, 3),
('system:admin:delete', '删除管理员', 'button', NULL, 22, 4),
-- 二级菜单 - 角色管理
('system:role', '角色管理', 'menu', '/system/role', 21, 2),
('system:role:view', '查看角色', 'button', NULL, 27, 1),
('system:role:create', '新建角色', 'button', NULL, 27, 2),
('system:role:update', '编辑角色', 'button', NULL, 27, 3),
('system:role:delete', '删除角色', 'button', NULL, 27, 4),
-- 二级菜单 - 权限管理
('system:permission', '权限管理', 'menu', '/system/permission', 21, 3),
-- 二级菜单 - 操作日志
('system:log', '操作日志', 'menu', '/system/log', 21, 4);

-- ============================================
-- 初始化超级管理员账号
-- ============================================
-- 密码：admin123（BCrypt加密后的值）
-- 注意：此密码仅用于初始化，生产环境务必修改
INSERT INTO sys_admin (username, password, real_name, status) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '超级管理员', 1);

-- ============================================
-- 分配超级管理员角色
-- ============================================
INSERT INTO sys_admin_role (admin_id, role_id)
SELECT 1, id FROM sys_role WHERE role_code = 'ROLE_SUPER_ADMIN';

-- ============================================
-- 分配超级管理员所有权限
-- ============================================
INSERT INTO sys_role_permission (role_id, permission_id)
SELECT r.id, p.id FROM sys_role r, sys_permission p
WHERE r.role_code = 'ROLE_SUPER_ADMIN' AND p.deleted = 0;

-- ============================================
-- 完成初始化
-- ============================================
