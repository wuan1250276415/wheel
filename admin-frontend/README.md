# 情侣转盘管理后台

基于 Vue 3 + TypeScript + Element Plus 的管理后台系统。

## 技术栈

- Vue 3.4
- TypeScript 5.3
- Vite 5.0
- Element Plus 2.5
- Vue Router 4.2
- Pinia 2.1
- Axios 1.6

## 开发

```bash
# 安装依赖
npm install

# 启动开发服务器
npm run dev

# 构建生产版本
npm run build

# 预览生产构建
npm run preview
```

## 功能模块

### 本地业务模块
- 登录认证
- 仪表盘
- 会员管理
  - 套餐管理
  - 用户会员
  - 订单管理
- 内容审核
  - 审核队列
  - 审核历史

### 组织架构模块（通过 Gateway）
- 用户管理
- 角色管理
- 部门管理
- 应用管理
- 资源菜单管理
- 字典管理

## 权限系统

使用 RBAC 基于角色的权限控制：

- 路由级权限控制
- 按钮级权限控制（v-permission 指令）
- 基于 JWT 的认证机制

## Gateway 配置

项目集成了基础平台的组织架构服务，通过 Gateway 进行转发：

- **Gateway 地址**: `http://192.168.66.126:8280`
- **用户服务**: `basebackend-user-api`
- **系统服务**: `basebackend-system-api`

详细配置请参考 [GATEWAY_CONFIG.md](./GATEWAY_CONFIG.md)
