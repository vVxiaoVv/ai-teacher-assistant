import { createRouter, createWebHistory } from 'vue-router'
import Login from './components/Login.vue'
import StudentPortraitList from './components/StudentPortraitList.vue'
import Layout from './views/Layout.vue'
import Dashboard from './views/Dashboard.vue'
import { getCookie } from './utils/index.js'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: Login
  },
  {
    path: '/',
    component: Layout,
    meta: { requiresAuth: true },
    children: [
      {
        path: '',
        name: 'Home',
        component: Dashboard
      },
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: Dashboard
      },
      {
        path: 'student-portrait',
        name: 'StudentPortrait',
        component: StudentPortraitList
      },
      {
        path: 'student-portrait/:id',
        name: 'StudentPortraitDetail',
        component: () => import('./views/StudentPortraitDetail.vue')
      },
      {
        path: 'lesson-plan',
        redirect: '/lesson-plan/list'
      },
      {
        path: 'lesson-plan/upload',
        name: 'LessonPlanUpload',
        component: () => import('./views/LessonPlanUpload.vue')
      },
      {
        path: 'lesson-plan/list',
        name: 'LessonPlanList',
        component: () => import('./views/LessonPlanList.vue')
      },
      {
        path: 'video-examples',
        name: 'VideoExamples',
        component: () => import('./views/VideoExamples.vue')
      },
      {
        path: 'video-correction-history',
        name: 'VideoCorrectionHistory',
        component: () => import('./views/VideoCorrectionHistory.vue')
      },
      {
        path: 'teacher-portrait',
        name: 'TeacherPortrait',
        component: () => import('./views/TeacherPortraitList.vue')
      },
      {
        path: 'teacher-portrait/detail',
        name: 'TeacherPortraitDetail',
        component: () => import('./views/TeacherPortrait.vue')
      },
      {
        path: 'script/:id',
        name: 'ScriptDetail',
        component: () => import('./views/ScriptDetail.vue')
      }
    ]
  },
  { path: '/:pathMatch(.*)*', redirect: '/login' }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫，验证登录状态
router.beforeEach((to, from, next) => {
  if (to.matched.some(record => record.meta.requiresAuth)) {
    // 检查是否有token、userId或username（从Cookie中获取）
    const token = getCookie('token')
    const userId = getCookie('userId')
    const username = getCookie('username')
    
    if (!token && !userId && !username) {
      // 没有认证信息，重定向到登录页
      next({ path: '/login' })
    } else {
      // 有认证信息，继续访问
      next()
    }
  } else {
    // 不需要认证的页面，直接访问
    next()
  }
})

export default router
