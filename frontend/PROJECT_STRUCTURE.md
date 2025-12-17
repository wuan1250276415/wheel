# 情侣转盘小程序 - 前端项目结构

## 目录结构

```
frontend/
├── src/
│   ├── api/                    # API 接口层
│   │   ├── request.ts         # HTTP 请求封装
│   │   ├── auth.ts            # 认证相关接口
│   │   ├── wheel.ts           # 转盘相关接口
│   │   ├── couple.ts          # 情侣关系接口
│   │   └── statistics.ts      # 统计相关接口
│   │
│   ├── stores/                 # Pinia 状态管理
│   │   ├── user.ts            # 用户状态管理
│   │   └── wheel.ts           # 转盘状态管理
│   │
│   ├── components/             # 公共组件
│   │   └── WheelCanvas.vue    # 转盘 Canvas 组件
│   │
│   ├── pages/                  # 页面文件
│   │   ├── index/             # 首页
│   │   │   └── index.vue
│   │   ├── wheel/             # 转盘页
│   │   │   └── wheel.vue
│   │   ├── profile/           # 个人中心
│   │   │   └── profile.vue
│   │   ├── couple/            # 情侣关系
│   │   │   └── couple.vue
│   │   ├── statistics/        # 数据统计
│   │   │   └── statistics.vue
│   │   ├── content/           # 内容中心（预留）
│   │   │   └── content.vue
│   │   └── settings/          # 设置页（预留）
│   │       └── settings.vue
│   │
│   ├── utils/                  # 工具函数
│   │   └── index.ts           # 通用工具函数
│   │
│   ├── types/                  # 类型定义
│   │   └── global.d.ts        # 全局类型声明
│   │
│   ├── App.vue                 # 应用根组件
│   ├── main.ts                 # 应用入口文件
│   └── uni.scss                # 全局样式
│
├── static/                     # 静态资源
│   ├── icons/                  # 图标文件
│   │   ├── home.png
│   │   ├── home-active.png
│   │   ├── wheel.png
│   │   ├── wheel-active.png
│   │   ├── profile.png
│   │   └── profile-active.png
│   │
│   └── sounds/                 # 音效文件
│       ├── success.mp3
│       └── spin.mp3
│
├── pages.json                  # 页面路由配置
├── manifest.json               # 应用配置
├── package.json                # 依赖配置
├── vite.config.ts              # Vite 构建配置
├── tsconfig.json               # TypeScript 配置
├── tsconfig.node.json          # Node 环境 TypeScript 配置
├── .env.example                # 环境变量示例
├── .gitignore                  # Git 忽略文件
└── README.md                   # 项目说明文档
```

## 核心模块说明

### 1. API 层 (api/)

负责与后端 API 通信，包括请求封装、响应拦截、错误处理等。

- **request.ts**: HTTP 请求封装，统一处理请求头、Token、响应拦截
- **auth.ts**: 用户认证相关 API（登录、注册、获取用户信息等）
- **wheel.ts**: 转盘功能相关 API（转盘配置、内容、旋转、历史记录等）
- **couple.ts**: 情侣关系相关 API（邀请、配对、解除等）
- **statistics.ts**: 数据统计相关 API（用户统计、系统概览、趋势等）

### 2. 状态管理 (stores/)

使用 Pinia 进行状态管理，实现跨组件状态共享。

- **user.ts**: 用户信息、认证状态、情侣关系状态
- **wheel.ts**: 转盘配置、内容列表、历史记录、旋转状态

### 3. 组件 (components/)

可复用的 Vue 组件。

- **WheelCanvas.vue**: 基于 HTML5 Canvas 的转盘组件，支持：
  - 自适应大小
  - 权重随机
  - 平滑动画
  - 自定义主题

### 4. 页面 (pages/)

应用的主要页面，每个页面包含完整的业务逻辑。

- **index**: 首页 - 欢迎信息、快速操作、今日数据
- **wheel**: 转盘页 - 分类筛选、转盘组件、历史记录
- **profile**: 个人中心 - 用户信息、统计数据、功能菜单
- **couple**: 情侣关系 - 邀请配对、关系管理
- **statistics**: 数据统计 - 个人数据、趋势图表、排行

### 5. 工具函数 (utils/)

通用工具函数集合。

- 时间格式化
- 数据验证
- 防抖节流
- 数组操作
- 图片压缩
- 等等...

### 6. 类型定义 (types/)

TypeScript 类型声明文件。

- 全局 API 响应类型
- 业务实体类型
- 页面参数类型

## 技术栈

### 框架与库

- **uni-app 3.x**: 跨平台应用框架
- **Vue 3**: 前端框架
- **TypeScript**: JavaScript 超集
- **Pinia**: 状态管理库
- **Vite**: 构建工具

### 开发工具

- **ESLint**: 代码检查
- **Prettier**: 代码格式化
- **@dcloudio/types**: UniApp 类型声明

## 开发规范

### 1. 命名规范

- **组件名**: PascalCase（如 `WheelCanvas`）
- **文件名**: kebab-case（如 `wheel-canvas.vue`）
- **变量名**: camelCase（如 `userInfo`）
- **常量名**: UPPER_SNAKE_CASE（如 `API_BASE_URL`）
- **CSS 类名**: kebab-case（如 `wheel-container`）

### 2. 代码风格

- 使用 TypeScript 严格模式
- 优先使用组合式 API (`setup`)
- 组件 props 使用 interface 定义
- 使用 Pinia 进行状态管理
- API 调用使用 async/await

### 3. 目录规范

- 页面组件放在 `pages/` 对应文件夹下
- 公共组件放在 `components/` 根目录
- 相关功能 API 放在同一个文件
- 工具函数按功能分类

### 4. 样式规范

- 使用 rpx 单位确保跨平台兼容
- 使用 SCSS 预处理器
- 遵循 BEM 命名规范
- 颜色、间距等使用变量统一管理

## 常用命令

### 安装依赖
```bash
npm install
```

### 开发运行
```bash
# H5 开发
npm run dev:h5

# 微信小程序
npm run dev:mp-weixin

# 支付宝小程序
npm run dev:mp-alipay
```

### 构建打包
```bash
# H5 构建
npm run build:h5

# 微信小程序构建
npm run build:mp-weixin
```

### 代码检查
```bash
# ESLint 检查
npm run lint

# 类型检查
npm run type-check
```

## 注意事项

1. **跨平台兼容性**: 注意 H5、微信小程序等平台的 API 差异
2. **性能优化**: Canvas 绘制注意避免频繁重绘
3. **数据缓存**: 合理使用本地存储和缓存机制
4. **错误处理**: 所有 API 调用都需要完善的错误处理
5. **用户体验**: 注意加载状态、错误提示、动画效果

## 部署说明

### H5 部署
1. 执行 `npm run build:h5` 构建
2. 将 `dist/build/h5` 目录部署到服务器
3. 配置 Nginx 静态文件服务

### 小程序部署
1. 执行 `npm run build:mp-weixin` 构建
2. 使用微信开发者工具打开 `dist/build/mp-weixin` 目录
3. 上传代码到微信后台审核发布

## 许可证

MIT License
