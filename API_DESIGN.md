# 后端API设计文档

## 1. 概述

本文档基于前端项目（admin-platform和uni-app）的API需求，设计了基于renren-fast框架的后端接口方案。所有接口遵循RESTful设计风格，使用JSON格式进行数据交换。

## 2. 接口分类

### 2.1 用户认证接口

| 接口名称 | 请求方法 | 请求路径 | 功能描述 | 所属模块 |
|---------|---------|---------|---------|---------|
| 用户登录 | POST | /api/user/login | 用户登录认证 | sys |
| 用户注册 | POST | /api/user/register | 用户注册 | sys |

### 2.2 学生画像接口

| 接口名称 | 请求方法 | 请求路径 | 功能描述 | 所属模块 |
|---------|---------|---------|---------|---------|
| 获取学生画像列表 | GET | /api/student-portrait/list | 获取学生画像列表 | admin |
| 获取学生画像详情 | GET | /api/student-portrait/{id} | 获取单个学生画像详情 | admin |
| 创建学生画像 | POST | /api/student-portrait/create | 创建学生画像 | admin |
| 更新学生画像 | PUT | /api/student-portrait/update/{id} | 更新学生画像 | admin |
| 删除学生画像 | DELETE | /api/student-portrait/{id} | 删除学生画像 | admin |

### 2.3 视频分析接口

| 接口名称 | 请求方法 | 请求路径 | 功能描述 | 所属模块 |
|---------|---------|---------|---------|---------|
| 获取分析历史 | GET | /api/video-analysis/history | 获取视频分析历史记录 | app |
| 分析视频 | POST | /api/video-analysis/analyze | 提交视频分析请求 | app |

### 2.4 视频纠正接口

| 接口名称 | 请求方法 | 请求路径 | 功能描述 | 所属模块 |
|---------|---------|---------|---------|---------|
| 获取纠正结果 | GET | /api/video-correction/result/{taskId} | 获取视频纠正结果 | app |

## 3. 接口详细设计

### 3.1 用户认证接口

#### 3.1.1 用户登录

**请求URL**：`/api/user/login`

**请求方法**：POST

**请求体**：
```json
{
  "username": "admin",
  "password": "123456"
}
```

**响应**：
```json
{
  "code": 0,
  "msg": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9.eyJ1c2VyX2lkIjoyLCJ1c2VybmFtZSI6ImFkbWluIiwiZXhwIjoxNjE5NTUyNzAyfQ.5e8f8f8f8f8f8f8f8f8f8f8f8f8f8f8f",
    "expire": 604800,
    "user": {
      "id": 2,
      "username": "admin",
      "realName": "管理员",
      "avatar": "https://avatar.xxx.com/1.jpg"
    }
  }
}
```

#### 3.1.2 用户注册

**请求URL**：`/api/user/register`

**请求方法**：POST

**请求体**：
```json
{
  "username": "testuser",
  "password": "123456",
  "realName": "测试用户",
  "email": "test@example.com",
  "phone": "13800138000"
}
```

**响应**：
```json
{
  "code": 0,
  "msg": "注册成功",
  "data": {
    "id": 10,
    "username": "testuser",
    "realName": "测试用户",
    "email": "test@example.com",
    "phone": "13800138000"
  }
}
```

### 3.2 学生画像接口

#### 3.2.1 获取学生画像列表

**请求URL**：`/api/student-portrait/list`

**请求方法**：GET

**请求参数**：
- page: 页码（默认1）
- size: 每页条数（默认10）
- name: 学生姓名（可选，用于搜索）
- classId: 班级ID（可选，用于筛选）

**响应**：
```json
{
  "code": 0,
  "msg": "获取成功",
  "data": {
    "total": 100,
    "list": [
      {
        "id": 1,
        "name": "张三",
        "classId": 1,
        "className": "高三一班",
        "gender": "男",
        "age": 18,
        "score": 95,
        "behavior": "积极主动",
        "createdAt": "2023-01-01 10:00:00"
      },
      // 更多学生画像...
    ],
    "pageNum": 1,
    "pageSize": 10,
    "totalPage": 10
  }
}
```

#### 3.2.2 获取学生画像详情

**请求URL**：`/api/student-portrait/{id}`

**请求方法**：GET

**响应**：
```json
{
  "code": 0,
  "msg": "获取成功",
  "data": {
    "id": 1,
    "name": "张三",
    "classId": 1,
    "className": "高三一班",
    "gender": "男",
    "age": 18,
    "score": 95,
    "behavior": "积极主动",
    "details": {
      "attendance": 98,
      "homeworkCompletion": 95,
      "participation": 90
    },
    "createdAt": "2023-01-01 10:00:00",
    "updatedAt": "2023-01-02 15:30:00"
  }
}
```

#### 3.2.3 创建学生画像

**请求URL**：`/api/student-portrait/create`

**请求方法**：POST

**请求体**：
```json
{
  "name": "李四",
  "classId": 1,
  "gender": "女",
  "age": 17,
  "score": 92,
  "behavior": "认真刻苦",
  "details": {
    "attendance": 99,
    "homeworkCompletion": 98,
    "participation": 85
  }
}
```

**响应**：
```json
{
  "code": 0,
  "msg": "创建成功",
  "data": {
    "id": 2,
    "name": "李四",
    "classId": 1,
    "className": "高三一班",
    "gender": "女",
    "age": 17,
    "score": 92,
    "behavior": "认真刻苦",
    "createdAt": "2023-01-03 09:00:00"
  }
}
```

#### 3.2.4 更新学生画像

**请求URL**：`/api/student-portrait/update/{id}`

**请求方法**：PUT

**请求体**：
```json
{
  "name": "李四",
  "classId": 1,
  "gender": "女",
  "age": 17,
  "score": 94,
  "behavior": "认真刻苦，进步明显",
  "details": {
    "attendance": 99,
    "homeworkCompletion": 98,
    "participation": 90
  }
}
```

**响应**：
```json
{
  "code": 0,
  "msg": "更新成功",
  "data": {
    "id": 2,
    "name": "李四",
    "classId": 1,
    "className": "高三一班",
    "gender": "女",
    "age": 17,
    "score": 94,
    "behavior": "认真刻苦，进步明显",
    "updatedAt": "2023-01-04 14:20:00"
  }
}
```

#### 3.2.5 删除学生画像

**请求URL**：`/api/student-portrait/{id}`

**请求方法**：DELETE

**响应**：
```json
{
  "code": 0,
  "msg": "删除成功",
  "data": null
}
```

### 3.3 视频分析接口

#### 3.3.1 获取分析历史

**请求URL**：`/api/video-analysis/history`

**请求方法**：GET

**请求参数**：
- page: 页码（默认1）
- size: 每页条数（默认10）
- videoUrl: 视频URL（可选，用于搜索）

**响应**：
```json
{
  "code": 0,
  "msg": "获取成功",
  "data": {
    "total": 50,
    "list": [
      {
        "id": 1,
        "videoUrl": "https://video.xxx.com/video1.mp4",
        "analyzedAt": "2023-01-01 10:00:00",
        "result": {
          "message": "分析结果概述",
          "details": {
            "score": 85,
            "comments": ["语速适中", "肢体语言丰富"]
          }
        }
      },
      // 更多分析历史...
    ],
    "pageNum": 1,
    "pageSize": 10,
    "totalPage": 5
  }
}
```

#### 3.3.2 分析视频

**请求URL**：`/api/video-analysis/analyze`

**请求方法**：POST

**请求体**：
```json
{
  "videoUrl": "https://video.xxx.com/new-video.mp4"
}
```

**响应**：
```json
{
  "code": 0,
  "msg": "分析成功",
  "data": {
    "id": 51,
    "videoUrl": "https://video.xxx.com/new-video.mp4",
    "analyzedAt": "2023-01-05 09:30:00",
    "result": {
      "message": "分析结果概述",
      "details": {
        "score": 90,
        "comments": ["表达清晰", "内容丰富"]
      }
    }
  }
}
```

### 3.4 视频纠正接口

#### 3.4.1 获取纠正结果

**请求URL**：`/api/video-correction/result/{taskId}`

**请求方法**：GET

**响应**：
```json
{
  "code": 0,
  "msg": "获取成功",
  "data": {
    "taskId": "task-123456",
    "status": "COMPLETED",
    "videoUrl": "https://video.xxx.com/original.mp4",
    "correctedVideoUrl": "https://video.xxx.com/corrected.mp4",
    "corrections": [
      {
        "type": "speech",
        "description": "语速过快",
        "suggestion": "适当放慢语速"
      },
      {
        "type": "gesture",
        "description": "手势过多",
        "suggestion": "减少不必要的手势"
      }
    ],
    "createdAt": "2023-01-01 10:00:00",
    "completedAt": "2023-01-01 10:30:00"
  }
}
```

## 4. 数据模型设计

### 4.1 用户表（sys_user）

| 字段名 | 数据类型 | 长度 | 主键 | 说明 |
|-------|---------|------|------|------|
| id | bigint | 20 | 是 | 用户ID |
| username | varchar | 50 | 是 | 用户名 |
| password | varchar | 100 | 否 | 密码 |
| salt | varchar | 50 | 否 | 盐值 |
| real_name | varchar | 50 | 否 | 真实姓名 |
| email | varchar | 100 | 否 | 邮箱 |
| phone | varchar | 11 | 否 | 手机号 |
| avatar | varchar | 255 | 否 | 头像 |
| status | tinyint | 4 | 否 | 状态（0：禁用，1：启用） |
| create_time | datetime | 否 | 否 | 创建时间 |
| update_time | datetime | 否 | 否 | 更新时间 |

### 4.2 学生画像表（student_portrait）

| 字段名 | 数据类型 | 长度 | 主键 | 说明 |
|-------|---------|------|------|------|
| id | bigint | 20 | 是 | 画像ID |
| name | varchar | 50 | 否 | 学生姓名 |
| class_id | bigint | 20 | 否 | 班级ID |
| gender | varchar | 10 | 否 | 性别 |
| age | int | 11 | 否 | 年龄 |
| score | int | 11 | 否 | 分数 |
| behavior | varchar | 255 | 否 | 行为描述 |
| details | text | 否 | 否 | 详细信息（JSON格式） |
| create_time | datetime | 否 | 否 | 创建时间 |
| update_time | datetime | 否 | 否 | 更新时间 |

### 4.3 视频分析历史表（video_analysis_history）

| 字段名 | 数据类型 | 长度 | 主键 | 说明 |
|-------|---------|------|------|------|
| id | bigint | 20 | 是 | 记录ID |
| user_id | bigint | 20 | 否 | 用户ID |
| video_url | varchar | 255 | 否 | 视频URL |
| analyzed_at | datetime | 否 | 否 | 分析时间 |
| result | text | 否 | 否 | 分析结果（JSON格式） |
| create_time | datetime | 否 | 否 | 创建时间 |

### 4.4 视频纠正任务表（video_correction_task）

| 字段名 | 数据类型 | 长度 | 主键 | 说明 |
|-------|---------|------|------|------|
| id | varchar | 50 | 是 | 任务ID |
| user_id | bigint | 20 | 否 | 用户ID |
| video_url | varchar | 255 | 否 | 原始视频URL |
| corrected_video_url | varchar | 255 | 否 | 纠正后视频URL |
| status | varchar | 20 | 否 | 任务状态（PENDING, PROCESSING, COMPLETED, FAILED） |
| corrections | text | 否 | 否 | 纠正内容（JSON格式） |
| create_time | datetime | 否 | 否 | 创建时间 |
| completed_time | datetime | 否 | 否 | 完成时间 |

## 5. 实现计划

### 5.1 基础准备

1. 克隆renren-fast项目
2. 配置数据库连接
3. 创建所需的数据表

### 5.2 接口实现顺序

1. **用户认证接口**
   - 基于renren-fast现有登录接口修改，去除验证码验证
   - 实现用户注册接口

2. **学生画像接口**
   - 创建学生画像模块（controller, service, dao, entity）
   - 实现CRUD接口

3. **视频分析接口**
   - 创建视频分析模块
   - 实现分析历史查询接口
   - 实现视频分析接口

4. **视频纠正接口**
   - 创建视频纠正模块
   - 实现纠正结果查询接口

### 5.3 技术栈

- 后端框架：Spring Boot
- ORM框架：MyBatis Plus
- 安全框架：Shiro/JWT
- 数据库：MySQL
- API文档：Swagger

## 6. 安全考虑

1. 所有接口采用JWT认证机制
2. 对敏感数据进行加密存储
3. 实现请求频率限制，防止暴力攻击
4. 对输入参数进行校验，防止SQL注入和XSS攻击
5. 实现适当的权限控制，确保用户只能访问自己的数据

## 7. 部署与测试

1. 配置应用服务器（Tomcat）
2. 部署应用到服务器
3. 使用Postman等工具测试接口功能
4. 与前端项目集成测试

## 8. 维护与扩展

1. 定期备份数据库
2. 监控接口性能和错误率
3. 根据业务需求扩展新接口
4. 定期更新依赖库，修复安全漏洞

---

**文档版本**：1.0
**创建日期**：2023-01-01
**更新日期**：2023-01-01
**创建人**：AI Assistant