import { createApp } from 'vue'
import './style.css'
import './assets/scss/index.scss'
import App from './App.vue'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import { ElMessage } from 'element-plus'
import axios from 'axios'
import router from './router'
import { getCookie, removeCookie } from './utils/index.js'

const app = createApp(App)
app.use(ElementPlus)
app.use(router)

// 配置axios
// 不设置baseURL，让每个请求明确指定完整路径，由Vite代理处理
// axios.defaults.baseURL = '/api'
// 配置axios允许携带Cookie
axios.defaults.withCredentials = true

// 请求拦截器，添加token到请求头
axios.interceptors.request.use(
  config => {
    const token = getCookie('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 响应拦截器，处理认证失败等情况
axios.interceptors.response.use(
  response => {
    return response
  },
  error => {
    if (error.response && error.response.status === 401) {
      // 认证失败，清除token并重定向到登录页
      removeCookie('userId')
      removeCookie('token')
      removeCookie('username')
      removeCookie('role')
      router.push('/login')
      ElMessage.error('登录已过期，请重新登录')
    }
    return Promise.reject(error)
  }
)

app.config.globalProperties.$axios = axios

app.mount('#app')
