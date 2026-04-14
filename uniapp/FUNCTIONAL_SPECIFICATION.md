# Uniapp 磨课助手 功能说明文档

## 项目概述

**项目名称**: 磨课助手 (AI Teacher Assistant)

**产品定位**: 面向教师的 AI 教学辅助应用，帮助教师通过视频分析、智能反馈和模拟课堂等功能，打磨和优化教学设计。

**技术架构**: 基于 uniapp 框架开发的跨平台移动应用，支持 iOS/Android/H5 多端部署。

**核心理念**: "让每一次试讲，都更接近理想课堂"

---

## 功能模块架构

```
┌─────────────────────────────────────────────────────────────────┐
│                        用户认证模块                              │
│                         (登录页)                                 │
└─────────────────────────────┬───────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                          首页                                    │
│                      (功能入口中心)                               │
└─────────────────────────────┬───────────────────────────────────┘
                              │
        ┌─────────────────────┼─────────────────────┐
        │                     │                     │
        ▼                     ▼                     ▼
┌───────────────┐    ┌───────────────┐    ┌───────────────┐
│   核心功能    │    │   管理功能    │    │   AI能力     │
├───────────────┤    ├───────────────┤    ├───────────────┤
│ • 视频纠偏    │    │ • 教案管理    │    │ • 语音合成    │
│ • 纠偏历史    │    │ • 课堂管理    │    │ • 虚拟课堂    │
│ • 教师画像    │    │ • 学生画像    │    │              │
└───────────────┘    └───────────────┘    └───────────────┘
```

---

## 详细功能说明

### 1. 用户认证模块

| 页面 | 路径 | 功能描述 |
|------|------|----------|
| 登录页 | `pages/login/index` | 用户登录认证，支持账号密码登录，登录状态本地持久化 |

**核心功能**:
- 账号密码表单验证
- 调用 `/api/user/login` 接口进行认证
- 登录成功后保存 `token`、`userId`、`username`、`role` 到本地存储
- 自动检测已登录状态并跳转首页
- 错误提示与加载状态反馈

**用户交互流程**:
1. 用户输入账号（手机号/工号）和密码
2. 点击登录按钮提交表单
3. 系统验证凭证
4. 成功后跳转首页，失败则显示错误信息

---

### 2. 首页模块

| 页面 | 路径 | 功能描述 |
|------|------|----------|
| 首页 | `pages/index/index` | 应用主入口，展示所有功能模块卡片 |

**功能入口列表**:

| 功能名称 | 图标 | 描述 | 目标页面 |
|---------|------|------|----------|
| 视频纠偏 | 📹 | 上传试讲视频，系统自动生成多维度分析报告 | `/pages/video-correction/index` |
| AI跟读 | 🔊 | 文字转语音合成 | `/pages/speech-synthesis/index` |
| 纠偏历史 | 📋 | 查看历史上传的视频纠偏报告 | `/pages/history/index` |
| 教案管理 | 📚 | 上传教案文件，自动生成讲课逐字稿 | `/pages/lesson-plan/index` |
| 学生画像管理 | 👤 | 管理学生画像信息，记录学生特点 | `/pages/student-portrait/index` |
| 课堂管理 | 🏛️ | 创建和管理课堂，添加学生到课堂 | `/pages/classroom/index` |
| 教师画像 | 👨‍🏫 | 查看教师画像分析，了解教学风格 | `/pages/teacher-portrait/index` |

**交互特性**:
- 顶部 Hero 区域展示应用品牌和标语
- 用户头像显示（支持自定义头像和默认占位符）
- 退出登录功能（带二次确认）
- 卡片式功能入口设计，支持点击缩放反馈

---

### 3. 视频纠偏模块

| 页面 | 路径 | 功能描述 |
|------|------|----------|
| 视频纠偏 | `pages/video-correction/index` | 上传或录制试讲视频进行 AI 分析 |

**核心功能**:

#### 3.1 视频上传
- 从相册选择视频文件上传
- 支持常见视频格式
- 显示上传进度和状态

#### 3.2 现场录制
- 调用设备摄像头实时录制
- 最长支持30分钟录制时长
- 支持前后摄像头切换

#### 3.3 URL分析
- 输入视频文件的URL地址
- 支持远程视频分析
- 适用于已上传到云端的视频

#### 3.4 分析结果展示
- **总体概述**: 课堂整体评价
- **课堂结构建议**: 教学环节设计优化建议
- **互动性建议**: 师生互动改进建议
- **语言表达建议**: 表达技巧优化建议

**任务状态**:
| 状态 | 说明 |
|------|------|
| PROCESSING | 分析进行中，自动每5秒轮询 |
| DONE | 分析完成，显示结果 |
| FAILED | 分析失败，提示重试 |

**API 接口**:
- `POST /api/video-correction/upload` - 上传视频文件
- `GET /api/video-correction/result/{taskId}` - 获取分析结果
- `POST /api/video-analysis/analyze` - URL 视频分析

---

### 4. 教案管理模块

| 页面 | 路径 | 功能描述 |
|------|------|----------|
| 教案列表 | `pages/lesson-plan/index` | 教案列表展示与管理 |
| 上传教案 | `pages/lesson-plan/upload` | 上传新教案文件 |
| 教案详情 | `pages/lesson-plan/detail` | 查看教案详情和逐字稿 |

**核心功能**:

#### 4.1 教案列表
- 分页展示所有教案
- 按标题搜索筛选
- 显示上传时间和关联课堂
- 逐字稿状态标识

#### 4.2 上传教案
- 支持文件格式: TXT, DOC, DOCX
- 关联到指定课堂
- 自动提取内容摘要

#### 4.3 逐字稿生成
- AI 自动分析教案内容
- 生成包含师生互动的讲课逐字稿
- 包含教师提问和学生回答设计
- 生成时间约1-5分钟

#### 4.4 逐字稿查看
- 完整展示生成的逐字稿内容
- 支持滚动阅读

**API 接口**:
- `GET /api/lesson-plan/list` - 获取教案列表（分页）
- `POST /api/lesson-plan/upload` - 上传教案文件
- `GET /api/lesson-plan/{id}` - 获取教案详情
- `GET /api/script/lesson-plan/{id}` - 获取逐字稿
- `POST /api/script/generate/{id}` - 生成逐字稿

---

### 5. 课堂管理模块

| 页面 | 路径 | 功能描述 |
|------|------|----------|
| 课堂列表 | `pages/classroom/index` | 课堂列表展示与管理 |
| 创建/编辑课堂 | `pages/classroom/add` | 创建或编辑课堂配置 |
| 课堂详情 | `pages/classroom/detail` | 查看课堂详细信息 |

**核心功能**:

#### 5.1 课堂列表
- 分页展示所有课堂
- 按课堂名称搜索
- 显示课堂ID和学生数量
- 支持编辑和查看详情

#### 5.2 创建课堂
- 输入课堂名称（必填）
- 从学生画像库选择学生（必填）
- 添加课堂描述（可选）
- 多选学生支持

#### 5.3 编辑课堂
- 修改课堂基本信息
- 调整学生名单
- 更新课堂描述

**数据字段**:
| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 课堂唯一标识 |
| classroomName | String | 课堂名称 |
| description | String | 课堂描述 |
| studentIds | List<Long> | 关联学生ID列表 |
| studentCount | Integer | 学生数量 |

**API 接口**:
- `GET /api/classroom/list` - 获取课堂列表（分页）
- `POST /api/classroom/create` - 创建课堂
- `PUT /api/classroom/update/{id}` - 更新课堂
- `GET /api/classroom/{id}` - 获取课堂详情

---

### 6. 学生画像模块

| 页面 | 路径 | 功能描述 |
|------|------|----------|
| 学生列表 | `pages/student-portrait/index` | AI 学生画像列表 |
| 添加/编辑学生 | `pages/student-portrait/add` | 创建或编辑学生画像 |

**核心功能**:

#### 6.1 学生列表
- 卡片式展示学生信息
- 显示头像、姓名、特点描述
- 按姓名搜索
- 分页加载

#### 6.2 创建/编辑学生
- 设置学生姓名
- 描述学生特点和学习风格
- 上传学生头像（可选）
- 支持删除学生（带确认）

**数据字段**:
| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 学生唯一标识 |
| studentName | String | 学生姓名 |
| characteristics | String | 学生特点描述 |
| photoUrl | String | 头像URL |

**API 接口**:
- `GET /api/student-portrait/list` - 获取学生列表（分页）
- `POST /api/student-portrait/create` - 创建学生
- `PUT /api/student-portrait/update/{id}` - 更新学生
- `DELETE /api/student-portrait/{id}` - 删除学生
- `GET /api/student-portrait/{id}` - 获取学生详情

---

### 7. 教师画像模块

| 页面 | 路径 | 功能描述 |
|------|------|----------|
| 画像列表 | `pages/teacher-portrait/index` | 教师画像列表 |
| 画像详情 | `pages/teacher-portrait/detail` | 教师能力分析详情 |

**核心功能**:

#### 7.1 画像列表
- 展示教师头像和用户名
- 显示已使用的历史记录数
- 综合得分展示
- 生成/重新生成画像按钮

#### 7.2 画像详情
- 六维度能力雷达图分析
- 各维度详细评分
- 改进建议

**评估维度**:

| 维度 | 字段名 | 说明 |
|------|--------|------|
| 教学基本功 | teachingFoundation | 专业知识、教学技能 |
| 教学过程设计 | teachingProcessDesign | 课堂环节设计、教学流程 |
| 教学仪态 | teachingManner | 仪表、教态、肢体语言 |
| 多媒体与板书 | multimediaAndBlackboard | PPT运用、板书设计 |
| 课堂氛围 | classroomAtmosphere | 互动性、学生参与度 |
| 时间节奏控制 | timeRhythmControl | 时间分配、节奏把控 |

**API 接口**:
- `GET /api/teacher-portrait/list` - 获取画像列表
- `POST /api/teacher-portrait/generate` - 生成/重新生成画像
- `GET /api/teacher-portrait/{userId}` - 获取画像详情

---

### 8. 纠偏历史模块

| 页面 | 路径 | 功能描述 |
|------|------|----------|
| 历史列表 | `pages/history/index` | 视频分析历史记录 |
| 详情页 | `pages/history/detail` | 分析报告详情 |

**核心功能**:

#### 8.1 历史列表
- 分页展示历史分析记录
- 按视频URL搜索
- 时间友好显示（刚刚、X分钟前、X天前）
- 自动提取课题名称作为标题
- 显示分析摘要

#### 8.2 报告详情
- 完整的AI分析报告内容
- 格式化HTML内容显示
- 支持跳转回列表

**数据字段**:
| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 记录ID |
| title | String | 分析标题 |
| videoUrl | String | 视频URL |
| formattedMessage | String | 格式化分析内容 |
| rawMessage | String | 原始分析内容 |
| createTime | DateTime | 创建时间 |

**API 接口**:
- `GET /api/video-analysis/history` - 获取历史记录（分页）

---

### 9. AI 能力模块

| 页面 | 路径 | 功能描述 |
|------|------|----------|
| 语音合成 | `pages/speech-synthesis/index` | 文字转语音 TTS |
| 虚拟课堂 | `pages/virtual-classroom/index` | 模拟课堂（开发中） |

#### 9.1 语音合成功能
- 输入文字（最多1000字）
- 选择语音角色（男声/女声多种选择）
- 实时语音合成
- 在线播放合成语音
- 支持从其他页面传递文本

#### 9.2 虚拟课堂（规划中）
- 设定教学目标与班级画像
- 模拟学生提问与回答
- 模拟课堂氛围
- 实时课堂效果评估

---

## 通用组件

| 组件 | 路径 | 功能描述 |
|------|------|----------|
| SpeechSynthesis | `components/SpeechSynthesis.vue` | 语音合成通用组件，支持文本输入、角色选择、合成播放 |

---

## 网络请求

**请求封装文件**: `common/request.js`

**功能特性**:
- 自动添加 `X-User-Id` 请求头（用户ID）
- 自动添加 `Authorization: Bearer {token}` 请求头
- 支持环境自动切换
  - H5环境: `http://localhost:8080`
  - 移动端: 配置的服务器IP
- 默认5分钟超时（适配 AI 长时处理场景）
- 统一的 Promise 封装
- 支持自定义 API 地址存储

**导出函数**:
- `request(options)` - 基础请求封装
- `uniRequest(options)` - 增强请求封装，支持完整URL
- `API_BASE_URL` - 当前API基础地址

---

## 数据存储

使用 `uni.setStorageSync` / `uni.getStorageSync` 进行本地数据持久化：

| 存储键 | 类型 | 说明 |
|--------|------|------|
| `token` | String | JWT认证令牌 |
| `userId` | String | 用户ID |
| `username` | String | 用户名 |
| `role` | String | 用户角色 |
| `API_BASE_URL` | String | 自定义API地址（可选） |
| `history_detail_item` | Object | 历史详情临时数据（用于页面间传递） |

---

## 页面路由配置

配置文件: `pages.json`

**页面总数**: 19个

| 序号 | 页面路径 | 标题 | 导航样式 |
|------|----------|------|----------|
| 1 | pages/login/index | 登录 | custom |
| 2 | pages/index/index | 磨课助手 | default |
| 3 | pages/video-correction/index | 视频纠偏 | default |
| 4 | pages/speech-recognition/index | 语音识别 | custom |
| 5 | pages/speech-synthesis/index | 语音合成 | default |
| 6 | pages/virtual-classroom/index | 虚拟课堂 | default |
| 7 | pages/history/index | 纠偏历史 | default |
| 8 | pages/history/detail | 分析详情 | default |
| 9 | pages/lesson-plan/upload | 上传教案 | default |
| 10 | pages/lesson-plan/index | 教案列表 | default |
| 11 | pages/lesson-plan/detail | 教案详情 | default |
| 12 | pages/student-portrait/index | 学生画像 | default |
| 13 | pages/student-portrait/add | 添加学生 | default |
| 14 | pages/classroom/index | 课堂管理 | default |
| 15 | pages/classroom/add | 创建课堂 | default |
| 16 | pages/classroom/detail | 课堂详情 | default |
| 17 | pages/teacher-portrait/index | 教师画像 | default |
| 18 | pages/teacher-portrait/detail | 画像详情 | default |
| 19 | pages/video-example/history | 历史视频范例 | default |

---

## 全局样式

配置文件: `App.vue`

**基础配置**:
- 背景色: `#f5f5f5`
- 字体族: 系统默认无衬线字体

---

## 后端API汇总

### 用户模块
| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /api/user/login | 用户登录 |
| GET | /api/user/info | 获取用户信息 |

### 视频分析模块
| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /api/video-correction/upload | 上传视频 |
| GET | /api/video-correction/result/{taskId} | 获取分析结果 |
| POST | /api/video-analysis/analyze | URL视频分析 |
| GET | /api/video-analysis/history | 获取历史记录 |

### 教案模块
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/lesson-plan/list | 教案列表 |
| POST | /api/lesson-plan/upload | 上传教案 |
| GET | /api/lesson-plan/{id} | 教案详情 |
| GET | /api/script/lesson-plan/{id} | 获取逐字稿 |
| POST | /api/script/generate/{id} | 生成逐字稿 |

### 课堂模块
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/classroom/list | 课堂列表 |
| POST | /api/classroom/create | 创建课堂 |
| PUT | /api/classroom/update/{id} | 更新课堂 |
| GET | /api/classroom/{id} | 课堂详情 |

### 学生画像模块
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/student-portrait/list | 学生列表 |
| POST | /api/student-portrait/create | 创建学生 |
| PUT | /api/student-portrait/update/{id} | 更新学生 |
| DELETE | /api/student-portrait/{id} | 删除学生 |

### 教师画像模块
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/teacher-portrait/list | 画像列表 |
| POST | /api/teacher-portrait/generate | 生成画像 |
| GET | /api/teacher-portrait/{userId} | 画像详情 |

---

## 版本信息

| 项目 | 值 |
|------|-----|
| 文档版本 | 1.0 |
| 生成日期 | 2025-12-26 |
| 项目框架 | uniapp |
| 页面总数 | 19 |
| 功能模块 | 9 |

---

## 附录：目录结构

```
uniapp/
├── App.vue                 # 应用入口
├── main.js                 # 主入口文件
├── manifest.json           # 应用配置
├── pages.json              # 页面路由配置
├── common/
│   └── request.js          # 网络请求封装
├── components/
│   └── SpeechSynthesis.vue # 语音合成组件
├── pages/
│   ├── login/              # 登录模块
│   ├── index/              # 首页模块
│   ├── video-correction/   # 视频纠偏模块
│   ├── history/            # 历史记录模块
│   ├── lesson-plan/        # 教案管理模块
│   ├── classroom/          # 课堂管理模块
│   ├── student-portrait/   # 学生画像模块
│   ├── teacher-portrait/   # 教师画像模块
│   ├── speech-synthesis/   # 语音合成模块
│   ├── speech-recognition/ # 语音识别模块
│   ├── virtual-classroom/  # 虚拟课堂模块
│   └── video-example/      # 视频范例模块
└── static/                 # 静态资源
    └── education-bg.jpg    # 背景图片
```

