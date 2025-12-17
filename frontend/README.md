# 情侣转盘小程序前端

基于 uni-app + Vue 3 + TypeScript + Pinia 构建的跨平台前端应用

## 技术栈

- **框架**: uni-app 3.x
- **UI 库**: uni-ui
- **状态管理**: Pinia
- **开发语言**: TypeScript
- **构建工具**: Vite
- **样式**: SCSS

## 项目结构

```
frontend/
├── src/
│   ├── api/              # API 接口封装
│   │   ├── request.ts    # HTTP 请求封装
│   │   ├── auth.ts       # 认证相关 API
│   │   ├── wheel.ts      # 转盘相关 API
│   │   ├── couple.ts     # 情侣关系 API
│   │   └── statistics.ts # 统计相关 API
│   ├── stores/           # Pinia 状态管理
│   │   ├── user.ts       # 用户状态
│   │   └── wheel.ts      # 转盘状态
│   ├── components/       # 公共组件
│   │   └── WheelCanvas.vue # 转盘 Canvas 组件
│   ├── pages/            # 页面文件
│   │   ├── index/        # 首页
│   │   ├── wheel/        # 转盘页
│   │   ├── profile/      # 个人中心
│   │   ├── couple/       # 情侣关系
│   │   └── statistics/   # 数据统计
│   ├── App.vue           # 应用入口
│   └── main.ts           # 主入口文件
├── pages.json            # 页面配置
├── manifest.json         # 应用配置
├── package.json          # 依赖配置
├── vite.config.ts        # Vite 配置
└── README.md             # 项目说明
```

## 功能特性

### 1. 认证系统
- JWT Token 认证
- 自动刷新 Token
- 登录/注册
- 用户信息管理

### 2. 转盘功能
- Canvas 2D 绘制转盘
- 权重随机算法
- 平滑动画效果
- 自定义转盘配置
- 转盘历史记录

### 3. 情侣关系
- 邀请码生成
- 扫码/输入邀请码配对
- 情侣关系管理
- 解除关系

### 4. 数据统计
- 用户个人统计
- 系统概览统计
- 转盘使用趋势
- 热门内容排行
- 分类使用统计

## 开发指南

### 安装依赖

```bash
cd frontend
npm install
```

### 开发运行

```bash
# H5 开发
npm run dev:h5

# 微信小程序开发
npm run dev:mp-weixin
```

### 构建打包

```bash
# 构建 H5
npm run build:h5

# 构建微信小程序
npm run build:mp-weixin
```

## 页面说明

### 1. 首页 (pages/index/index)
- 欢迎信息展示
- 快速操作入口
- 今日数据概览
- 最近转盘结果

### 2. 转盘页 (pages/wheel/wheel)
- 分类筛选
- Canvas 转盘组件
- 转盘配置
- 历史记录列表

### 3. 个人中心 (pages/profile/profile)
- 用户信息卡片
- 个人统计数据
- 功能菜单
- 退出登录

### 4. 情侣关系 (pages/couple/couple)
- 情侣状态展示
- 创建邀请码
- 接受邀请
- 解除关系

### 5. 数据统计 (pages/statistics/statistics)
- 个人数据统计
- 使用趋势图表
- 热门内容排行
- 分类统计数据

## 组件说明

### WheelCanvas
基于 HTML5 Canvas 2D API 实现的转盘组件

**属性**:
- `size`: 转盘大小（像素）
- `contents`: 转盘内容列表
- `isSpinning`: 是否正在旋转

**事件**:
- `spin`: 开始转盘
- `result`: 转盘结果

## 状态管理

### UserStore
用户认证和信息管理
- 登录/登出
- Token 管理
- 用户信息
- 情侣关系状态

### WheelStore
转盘相关状态管理
- 转盘配置
- 转盘内容
- 历史记录
- 旋转状态

## 样式规范

### 颜色规范
- 主色: #FF69B4 (粉红色)
- 辅色: #FF1493 (深粉红)
- 背景: #FFF0F5 (淡粉色)
- 文字: #333333 (深灰)

### 单位规范
- 使用 rpx 单位确保跨平台兼容性
- 推荐使用 20rpx 的倍数作为间距

## API 接口

### 认证相关
- POST /api/auth/login - 用户登录
- POST /api/auth/register - 用户注册
- GET /api/auth/profile - 获取用户信息
- PUT /api/auth/profile - 更新用户信息
- POST /api/auth/refresh - 刷新 Token

### 转盘相关
- GET /api/wheel/config - 获取转盘配置
- POST /api/wheel/config - 保存转盘配置
- GET /api/wheel/contents - 获取转盘内容
- POST /api/wheel/spin - 执行转盘
- GET /api/wheel/history - 获取历史记录

### 情侣关系
- POST /api/couple/invite - 创建邀请码
- POST /api/couple/accept - 接受邀请
- GET /api/couple/info - 获取情侣信息
- DELETE /api/couple/break - 解除关系

### 数据统计
- GET /api/statistics/user - 用户统计
- GET /api/statistics/overview - 系统概览
- GET /api/statistics/spin-trend - 转盘趋势
- GET /api/statistics/popular-content - 热门内容
- GET /api/statistics/category - 分类统计

## 注意事项

1. **跨域问题**: 开发时需要在后端配置 CORS 或使用代理
2. **小程序限制**: 某些 H5 API 在小程序中不可用
3. **性能优化**: Canvas 绘制需要注意性能，避免频繁重绘
4. **数据缓存**: 合理使用本地存储和缓存机制
5. **错误处理**: 所有 API 调用都需要错误处理

## 许可证

MIT License
