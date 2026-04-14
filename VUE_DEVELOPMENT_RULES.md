# Vue 前端开发规范

## 1. 项目结构

### 1.1 基础目录结构
```
├── admin-platform/          # 管理端项目
│   ├── public/             # 静态资源
│   ├── src/                # 源代码
│   │   ├── assets/         # 全局资源
│   │   │   ├── img/        # 图片资源
│   │   │   ├── scss/       # SCSS样式
│   │   │   └── fonts/      # 字体资源
│   │   ├── components/     # 公共组件
│   │   ├── views/          # 页面组件
│   │   ├── router/         # 路由配置
│   │   ├── store/          # 状态管理
│   │   ├── utils/          # 工具函数
│   │   ├── api/            # API请求
│   │   ├── App.vue         # 根组件
│   │   ├── main.js         # 入口文件
│   │   └── style.css       # 全局样式
│   ├── .eslintrc.js        # ESLint配置
│   ├── vite.config.js      # Vite配置
│   └── package.json        # 项目配置
├── app-platform/           # App端项目
│   └── ...                 # 类似管理端结构
```

### 1.2 目录命名规范
- 目录名使用小写字母，多单词用连字符连接（kebab-case）
- 如：`components/`、`views/`、`utils/`

## 2. 命名规范

### 2.1 文件命名
- **组件文件**：使用 PascalCase（大驼峰）
  - 如：`Login.vue`、`StudentPortraitList.vue`
- **工具文件**：使用 kebab-case（短横线连接）
  - 如：`api-client.js`、`date-utils.js`
- **样式文件**：
  - 全局样式：使用 kebab-case
    - 如：`_variables.scss`、`_base.scss`
  - 组件样式：与组件同名
    - 如：`Login.vue` 对应 `<style scoped>`

### 2.2 变量命名
- **普通变量**：使用 camelCase（小驼峰）
  - 如：`userName`、`isLogin`
- **常量**：使用 UPPER_SNAKE_CASE（全大写下划线连接）
  - 如：`API_BASE_URL`、`MAX_RETRY_COUNT`
- **组件变量**：使用 camelCase
  - 如：`loginForm`、`studentList`

### 2.3 函数命名
- 使用 camelCase
- 动词开头，清晰表达函数功能
  - 如：`getUserInfo()`、`handleLogin()`、`validateForm()`

### 2.4 组件命名
- 使用 PascalCase
- 语义化命名，清晰表达组件功能
  - 如：`LoginForm`、`StudentCard`、`NavigationBar`

## 3. 组件开发规范

### 3.1 组件结构
每个 Vue 组件应遵循以下结构：
```vue
<template>
  <!-- 模板内容 -->
</template>

<script setup>
// 组件逻辑
</script>

<style scoped lang="scss">
// 组件样式
</style>
```

### 3.2 Script Setup 规范
- 使用 `<script setup>` 语法糖（Vue 3）
- 导入顺序：外部依赖 → 内部组件 → 工具函数 → API
- 定义顺序：ref/reactive → computed → watch → 函数 → 生命周期钩子

```javascript
<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import StudentCard from './StudentCard.vue'
import { getUserList } from '@/api/user'
import { formatDate } from '@/utils/date'

// 响应式数据
const isLoading = ref(false)
const userList = ref([])

// 计算属性
const activeUserCount = computed(() => {
  return userList.value.filter(user => user.status === 'active').length
})

// 方法
const fetchUserList = async () => {
  isLoading.value = true
  try {
    const response = await getUserList()
    userList.value = response.data
  } catch (error) {
    ElMessage.error('获取用户列表失败')
  } finally {
    isLoading.value = false
  }
}

// 生命周期钩子
onMounted(() => {
  fetchUserList()
})
</script>
```

### 3.3 模板规范
- 根元素使用语义化标签
- 避免深层嵌套（最多不超过 4 层）
- 使用 v-if/v-else-if/v-else 替代多个 v-if
- 列表渲染必须使用 key
- 事件命名使用 camelCase

```vue
<template>
  <div class="user-list">
    <h2 class="user-list__title">用户列表</h2>
    
    <el-card v-for="user in userList" :key="user.id" class="user-card">
      <StudentCard :user="user" @edit="handleEditUser" />
    </el-card>
    
    <el-empty v-if="userList.length === 0" description="暂无用户数据" />
    <el-loading v-if="isLoading" fullscreen />
  </div>
</template>
```

### 3.4 样式规范
- 使用 SCSS 语法（`<style lang="scss">`）
- 组件样式使用 `scoped` 属性
- 命名空间：使用 BEM 命名规范
  - 如：`.user-list`、`.user-list__title`、`.user-list__item--active`
- 使用变量：定义在 `_variables.scss` 中
- 避免使用 `!important`

```scss
<style lang="scss" scoped>
@import '../assets/scss/_variables.scss';

.user-list {
  padding: 20px;
  
  &__title {
    font-size: 20px;
    color: $--color-primary;
    margin-bottom: 20px;
  }
  
  .user-card {
    margin-bottom: 16px;
    
    &:hover {
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
    }
  }
}
</style>
```

## 4. 代码规范

### 4.1 ESLint 配置
- 使用标准的 ESLint 配置
- 规则参考项目根目录下的 `.eslintrc.js`

### 4.2 代码格式
- 缩进：2 个空格
- 引号：单引号
- 分号：不使用
- 逗号：最后一个元素后添加（多行时）

### 4.3 注释规范
- 复杂逻辑必须添加注释
- 组件和函数添加 JSDoc 注释

```javascript
/**
 * 用户登录函数
 * @param {Object} loginData - 登录数据
 * @param {string} loginData.username - 用户名
 * @param {string} loginData.password - 密码
 * @returns {Promise<Object>} 登录结果
 */
const login = async (loginData) => {
  // 登录逻辑
}
```

## 5. 路由规范

### 5.1 路由配置
- 使用 Vue Router 4.x
- 路由文件分离：主路由、子路由
- 使用懒加载优化性能

```javascript
// router/index.js
import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'main',
      component: () => import('../views/main.vue'),
      children: [
        {
          path: '/student/list',
          name: 'studentList',
          component: () => import('../views/StudentList.vue')
        },
        {
          path: '/lesson/plan',
          name: 'lessonPlan',
          component: () => import('../views/LessonPlanList.vue')
        }
      ]
    },
    {
      path: '/login',
      name: 'login',
      component: () => import('../components/Login.vue')
    }
  ]
})

export default router
```

### 5.2 路由命名
- 路由名称使用 camelCase
- 如：`studentList`、`lessonPlan`

## 6. API 请求规范

### 6.1 API 目录结构
```
src/api/
├── index.js           # API入口
├── user.js            # 用户相关API
├── student.js         # 学生相关API
└── lesson.js          # 课程相关API
```

### 6.2 API 封装
- 使用 Axios 封装 API 请求
- 统一处理请求/响应拦截器
- 错误处理机制

```javascript
// api/user.js
import axios from 'axios'

// 用户相关API
const userApi = {
  /**
   * 用户登录
   * @param {Object} data - 登录数据
   */
  login: (data) => axios.post('/api/user/login', data),
  
  /**
   * 获取用户信息
   */
  getUserInfo: () => axios.get('/api/user/info'),
  
  /**
   * 获取用户列表
   * @param {Object} params - 查询参数
   */
  getUserList: (params) => axios.get('/api/user/list', { params })
}

export default userApi
```

## 7. 状态管理

### 7.1 Pinia 规范（Vue 3）
- 模块拆分：按功能拆分 store
- 命名规范：使用 camelCase
- 状态定义：使用 ref/reactive

```javascript
// store/user.js
import { defineStore } from 'pinia'

export const useUserStore = defineStore('user', () => {
  // 状态
  const userInfo = ref(null)
  const token = ref(localStorage.getItem('token') || '')
  
  // 操作
  function setUserInfo(info) {
    userInfo.value = info
  }
  
  function setToken(newToken) {
    token.value = newToken
    localStorage.setItem('token', newToken)
  }
  
  function logout() {
    userInfo.value = null
    token.value = ''
    localStorage.removeItem('token')
  }
  
  return {
    userInfo,
    token,
    setUserInfo,
    setToken,
    logout
  }
})
```

## 8. 组件库使用

### 8.1 Element Plus
- 统一使用 Element Plus 组件库
- 按需导入：减少打包体积
- 统一主题配置：在 `assets/scss/_variables.scss` 中定义

```javascript
// main.js
import { createApp } from 'vue'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import App from './App.vue'

const app = createApp(App)
app.use(ElementPlus)
app.mount('#app')
```

## 9. 性能优化

### 9.1 组件优化
- 使用 `v-if` 和 `v-show` 合理选择
- 避免不必要的重新渲染：使用 `v-memo`
- 长列表优化：使用虚拟滚动

### 9.2 资源优化
- 图片懒加载
- 静态资源压缩
- 代码分割

### 9.3 网络优化
- API 请求缓存
- 减少 HTTP 请求
- 使用 CDN 加速

## 10. 测试规范

### 10.1 单元测试
- 使用 Vitest 进行单元测试
- 测试文件与被测试文件同目录
- 测试文件名：`*.test.js` 或 `*.spec.js`

### 10.2 E2E 测试
- 使用 Cypress 进行端到端测试
- 测试用例放在 `cypress/e2e/` 目录

## 11. 部署规范

### 11.1 环境配置
- 开发环境：`.env.development`
- 生产环境：`.env.production`
- 测试环境：`.env.test`

### 11.2 构建命令
```bash
# 开发环境
npm run dev

# 生产构建
npm run build

# 预览构建
npm run preview
```

## 12. Git 规范

### 12.1 分支命名
- 主分支：`main`
- 开发分支：`develop`
- 特性分支：`feature/feature-name`
- 修复分支：`fix/bug-name`

### 12.2 Commit 规范
- 使用 Conventional Commits 规范
- 格式：`type(scope): description`
- 类型：
  - feat: 新功能
  - fix: 修复bug
  - docs: 文档修改
  - style: 代码格式调整
  - refactor: 代码重构
  - test: 测试相关
  - chore: 构建或工具变更

## 13. 开发工具

### 13.1 推荐工具
- 编辑器：VS Code
- 插件：
  - ESLint
  - Prettier
  - Volar
  - SCSS IntelliSense
  - GitLens

### 13.2 配置文件
- `.vscode/settings.json`：编辑器配置
- `.prettierrc`：Prettier配置

---

本规范适用于管理端和App端的Vue开发，确保代码风格一致、可维护性高。