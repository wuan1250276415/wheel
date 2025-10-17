# 爱的转盘 - 游戏惩罚与奖励系统

一个可爱温馨的转盘程序,用于记录夫妻之间游戏后的惩罚或奖励。每次转盘结果都会自动保存到数据库,支持历史记录查询和统计分析。

## 功能特性

- 🎯 **精美转盘**: 流畅的转盘动画,可爱的粉色系配色
- 👫 **双人模式**: 支持两位玩家独立记录
- 💾 **自动记录**: 每次转盘结果自动保存到数据库
- 📜 **历史查询**: 查看所有历史记录,支持按玩家和类型筛选
- 📊 **统计分析**: 查看转盘统计、幸运值、结果分布等
- 🎨 **可爱风格**: 粉色系UI设计,emoji表情,圆润动画

## 技术栈

- **后端**: FastAPI + SQLAlchemy + SQLite
- **前端**: HTML5 + CSS3 + Vanilla JavaScript
- **数据库**: SQLite (轻量级,无需额外配置)

## 安装步骤

### 1. 克隆或下载项目

```bash
cd wheel
```

### 2. 安装依赖

```bash
pip install -r requirements.txt
```

### 3. 运行程序

```bash
python main.py
```

### 4. 访问应用

在浏览器中打开: http://localhost:8000

## 使用说明

### 主页面 - 转盘
1. 选择玩家(老公或老婆)
2. 点击"开始转盘"按钮
3. 等待转盘停止,查看结果
4. 结果会自动保存到数据库

### 历史记录页面
- 查看所有转盘历史
- 可按玩家筛选(老公/老婆)
- 可按类型筛选(奖励/惩罚)
- 显示详细的时间和结果

### 统计数据页面
- 总转盘次数统计
- 各玩家的转盘次数、奖励、惩罚统计
- 幸运值计算(奖励占比)
- 结果分布图表

## 自定义转盘选项

### 方法1: 直接修改数据库初始化代码

编辑 `database.py` 文件中的 `default_options` 列表:

```python
default_options = [
    WheelOption(text="你的选项", type="reward", emoji="💕", color="#FFB6C1"),
    # 添加更多选项...
]
```

**选项类型**:
- `type`: "reward" (奖励) 或 "punishment" (惩罚)
- `emoji`: 任意emoji表情
- `color`: 16进制颜色代码

**推荐颜色**:
- 粉色系奖励: `#FFB6C1`, `#FFC0CB`, `#FFD4E5`, `#FFE4E1`, `#FFF0F5`, `#FFEBF0`
- 紫色系惩罚: `#E6E6FA`, `#D8BFD8`, `#DDA0DD`, `#EE82EE`, `#DA70D6`, `#BA55D3`

### 方法2: 清空数据库后重新初始化

```bash
# 删除旧数据库
rm wheel_game.db

# 重新运行程序,会自动创建新数据库
python main.py
```

## 项目结构

```
wheel/
├── main.py                 # FastAPI主程序
├── database.py            # 数据库模型和初始化
├── requirements.txt       # Python依赖
├── README.md             # 项目文档
├── static/               # 静态文件目录
│   ├── index.html       # 主页面(转盘)
│   ├── history.html     # 历史记录页面
│   ├── stats.html       # 统计数据页面
│   ├── style.css        # 样式表
│   └── wheel.js         # 转盘JavaScript逻辑
└── wheel_game.db        # SQLite数据库(自动生成)
```

## API接口文档

### 获取转盘选项
```
GET /api/options
```

### 转动转盘
```
POST /api/spin
Content-Type: application/json

{
  "player_name": "老公"
}
```

### 获取历史记录
```
GET /api/records?player_name=老公&limit=50
```

### 获取统计数据
```
GET /api/stats
```

### 删除记录
```
DELETE /api/records/{record_id}
```

## 数据库表结构

### spin_records (转盘记录表)
- id: 主键
- player_name: 玩家名字
- result: 转盘结果
- result_type: 类型(reward/punishment)
- created_at: 创建时间

### wheel_options (转盘选项表)
- id: 主键
- text: 选项文字
- type: 类型(reward/punishment)
- emoji: emoji表情
- color: 颜色
- is_active: 是否启用

## 部署建议

### 本地使用
直接运行 `python main.py` 即可,数据保存在本地SQLite数据库。

### 服务器部署
1. 使用 Gunicorn 或 Uvicorn 作为生产服务器
2. 配置Nginx反向代理
3. 可选: 将SQLite改为PostgreSQL或MySQL

```bash
# 使用Uvicorn生产部署
uvicorn main:app --host 0.0.0.0 --port 8000 --workers 4
```

## 隐私说明

- 所有数据仅存储在本地SQLite数据库
- 不会上传或发送任何数据到外部服务器
- 建议仅在私人设备上使用

## 后续扩展建议

- [ ] 添加音效和更多动画效果
- [ ] 支持自定义玩家名称
- [ ] 添加转盘选项管理页面(增删改)
- [ ] 导出历史记录为Excel
- [ ] 添加用户认证和多账户支持
- [ ] 手机端适配优化
- [ ] 添加转盘主题切换

## 技术支持

如有问题或建议,欢迎联系!

---

用爱制作 ❤️ 只属于我们的回忆
