# Gateway 接口配置说明

本项目的组织架构相关接口通过网关（Gateway）进行转发，实现服务间的路由和负载均衡。

## Gateway 配置

**Gateway 基础地址**: `http://192.168.66.126:8280`

### 服务路由

项目中配置了三个请求实例，分别用于不同的服务：

#### 1. 本地管理后台服务 (default)
- **Request 实例**: `request`
- **Base URL**: `/api` (通过 Vite 代理)
- **用途**: 本项目的会员管理、内容审核等业务接口
- **示例接口**:
  - `/api/admin/auth/login` - 管理员登录
  - `/api/admin/membership/plans` - 会员套餐管理
  - `/api/admin/audit/queue` - 内容审核

#### 2. 用户服务 (user-api)
- **Request 实例**: `userApiRequest`
- **Base URL**: `http://192.168.66.126:8280/basebackend-user-api`
- **用途**: 用户、角色等组织架构接口
- **示例接口**:
  - `/api/user` - 用户管理
  - `/api/user/roles` - 角色管理
  - `/api/user/auth/login` - 用户登录（如果需要）

#### 3. 系统服务 (system-api)
- **Request 实例**: `systemApiRequest`
- **Base URL**: `http://192.168.66.126:8280/basebackend-system-api`
- **用途**: 部门、应用、资源菜单、字典等系统管理接口
- **示例接口**:
  - `/api/system/depts` - 部门管理
  - `/api/system/application` - 应用管理
  - `/api/system/application/resource` - 资源菜单管理
  - `/api/system/dicts` - 字典管理

## API 文件结构

```
src/api/
├── auth.ts          # 本地管理后台认证接口
├── membership.ts    # 会员管理接口（本地）
├── audit.ts         # 内容审核接口（本地）
└── system.ts        # 组织架构接口（通过 gateway）
    ├── 用户管理
    ├── 角色管理
    ├── 部门管理
    ├── 应用管理
    ├── 资源菜单管理
    └── 字典管理
```

## 使用示例

### 1. 用户管理

```typescript
import { queryUsers, createUser, updateUser, deleteUser } from '@/api/system'

// 查询用户列表
const users = await queryUsers({
  current: 1,
  size: 10,
  username: 'admin'
})

// 创建用户
await createUser({
  username: 'newuser',
  password: '123456',
  nickname: '新用户',
  deptId: 1,
  roleIds: [1, 2]
})
```

### 2. 部门管理

```typescript
import { getDeptTree, createDept, updateDept, deleteDept } from '@/api/system'

// 获取部门树
const deptTree = await getDeptTree()

// 创建部门
await createDept({
  deptName: '研发部',
  parentId: 0,
  leader: '张三',
  status: 1
})
```

### 3. 角色管理

```typescript
import { queryRoles, createRole, updateRole, deleteRole } from '@/api/system'

// 查询角色列表
const roles = await queryRoles({ current: 1, size: 10 })

// 创建角色
await createRole({
  roleName: '管理员',
  roleKey: 'admin',
  roleSort: 1,
  menuIds: [1, 2, 3]
})
```

## 注意事项

1. **跨域处理**: Gateway 需要配置 CORS，允许前端域名访问
2. **Token 传递**: 所有请求实例都会自动添加 `Authorization: Bearer <token>` 头
3. **错误处理**: 统一的响应拦截器会处理 401、403、500 等错误
4. **超时时间**: 所有请求默认超时时间为 30 秒
5. **响应格式**: 统一的 `Result<T>` 格式，包含 `code`、`msg`、`data` 字段

## 开发环境配置

本地开发时，可以通过 Vite 代理避免跨域问题：

```typescript
// vite.config.ts
export default defineConfig({
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:8180',
        changeOrigin: true
      },
      '/basebackend-user-api': {
        target: 'http://192.168.66.126:8280',
        changeOrigin: true
      },
      '/basebackend-system-api': {
        target: 'http://192.168.66.126:8280',
        changeOrigin: true
      }
    }
  }
})
```

## 生产环境配置

生产环境下，前端静态资源通过 Nginx 部署，需要配置反向代理：

```nginx
location /basebackend-user-api/ {
    proxy_pass http://192.168.66.126:8280/basebackend-user-api/;
    proxy_set_header Host $host;
    proxy_set_header X-Real-IP $remote_addr;
}

location /basebackend-system-api/ {
    proxy_pass http://192.168.66.126:8280/basebackend-system-api/;
    proxy_set_header Host $host;
    proxy_set_header X-Real-IP $remote_addr;
}
```
