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
        name: 'LessonPlanList',
        component: () => import('./views/LessonPlanList.vue')
      },
      {
        path: 'lesson-plan/upload',
        name: 'LessonPlanUpload',
        component: () => import('./views/LessonPlanUpload.vue')
      },
      {
        path: 'video-examples',
        name: 'VideoExamples',
        component: () => import('./views/VideoExamples.vue')
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

router.beforeEach((to, from, next) => {
  if (to.matched.some(record => record.meta.requiresAuth)) {
    const token = getCookie('token')
    const userId = getCookie('userId')
    const username = getCookie('username')
    
    if (!token && !userId && !username) {
      next({ path: '/login' })
    } else {
      next()
    }
  } else {
    next()
  }
})

export default router
