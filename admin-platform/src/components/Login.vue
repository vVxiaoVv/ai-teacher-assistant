<template>
  <div class="login-wrapper" :style="backgroundStyle">
    <!-- 左上角品牌信息 -->
    <div class="login-brand-top">
      <h1 class="login-title">好师多磨 管理平台</h1>
      <p class="login-desc">让每一次试讲，更接近理想课堂</p>
    </div>
    
    <!-- 登录表单区域 -->
    <div class="login-container">
      <!-- 右侧登录表单 -->
      <div class="login-right">
        <div class="login-form-wrapper">
        <!-- 登录表单 -->
        <el-form 
          ref="loginFormRef" 
          :model="loginForm" 
          :rules="loginRules"
          @keyup.enter.native="login()"
            class="login-form"
        >
          <el-form-item prop="username">
              <el-input 
                v-model="loginForm.username" 
                placeholder="请输入用户名"
                size="large"
                :prefix-icon="User"
                clearable
              ></el-input>
          </el-form-item>
          <el-form-item prop="password">
              <el-input 
                v-model="loginForm.password" 
                type="password" 
                placeholder="请输入密码" 
                show-password
                size="large"
                :prefix-icon="Lock"
                @keyup.enter="login"
              ></el-input>
          </el-form-item>

          <el-form-item>
              <el-button 
                type="primary" 
                @click="login" 
                :loading="loading" 
                class="login-btn-submit"
                size="large"
              >
                {{ loading ? '登录中...' : '登录' }}
              </el-button>
          </el-form-item>
            
          <div class="login-links">
              <el-link type="primary" :underline="false" @click="handleRegister">立即注册</el-link>
              <el-link type="primary" :underline="false" @click="handleResetPassword">忘记密码</el-link>
          </div>
        </el-form>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import axios from 'axios'
import { setCookie, getCookie } from '../utils/index.js'

const router = useRouter()

// 登录状态
const loading = ref(false)

// 表单引用
const loginFormRef = ref(null)

// 背景图片样式
const backgroundStyle = ref({
  backgroundImage: `url(/img/school-building.jpg)`
})

// 登录表单
const loginForm = reactive({
  username: '',
  password: ''
})

// 登录表单规则
const loginRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ]
}

// 登录方法
const login = () => {
  if (!loginFormRef.value) return
  
  loginFormRef.value.validate((valid) => {
    if (valid) {
      loading.value = true
      
      // 调用服务端登录接口
axios.post('/api/user/login', {
  username: loginForm.username,
  password: loginForm.password
      }, {
        withCredentials: true // 确保携带Cookie
})
  .then(response => {
    const data = response.data
        console.log('登录响应完整数据:', JSON.stringify(data, null, 2))
        console.log('响应状态码:', response.status)
        console.log('响应头:', response.headers)
        
        // 检查登录是否成功 - LoginResponse直接返回，包含success字段
        if (data && (data.success === true || data.success === 'true')) {
          console.log('登录成功，开始设置Cookie')
          
          // 后端已经将userId和token写入Cookie（HttpOnly），这里也保存一份到前端Cookie（用于前端读取）
          if (data.userId) {
            const userId = String(data.userId)
            setCookie('userId', userId)
            console.log('设置userId Cookie:', userId)
          }
          
          // 保存token到前端Cookie（后端已设置HttpOnly Cookie，这里再设置一份供前端读取）
          // 注意：后端设置的HttpOnly Cookie前端JavaScript无法读取，所以需要前端也设置一份
          if (data.token) {
            const token = String(data.token).trim()
            if (token) {
              setCookie('token', token)
              console.log('设置token Cookie，长度:', token.length, '前20字符:', token.substring(0, 20) + '...')
            } else {
              console.warn('token为空字符串')
            }
          } else {
            console.warn('响应中没有token字段，响应数据:', data)
          }
          
          if (data.username) {
            setCookie('username', data.username)
            console.log('设置username Cookie:', data.username)
          }
          
          if (data.role) {
            setCookie('role', data.role)
            console.log('设置role Cookie:', data.role)
          }
          
          // 验证Cookie是否设置成功
          // 立即检查一次
          let token = getCookie('token')
          console.log('立即检查token:', token ? '存在，长度:' + token.length : '不存在')
          console.log('当前所有Cookie:', document.cookie)
          
          // 如果立即检查没有，等待一下再检查（给浏览器时间设置Cookie）
          if (!token) {
            setTimeout(() => {
              token = getCookie('token')
              console.log('延迟检查token:', token ? '存在，长度:' + token.length : '不存在')
              console.log('延迟检查时所有Cookie:', document.cookie)
              
              // 无论前端Cookie是否设置成功，都尝试跳转
              // 因为后端已设置HttpOnly Cookie，浏览器会在请求中自动携带
              ElMessage.success(data.message || '登录成功')
              
              // 直接跳转，路由守卫会检查Cookie
              router.push('/').then(() => {
                console.log('路由跳转成功')
              }).catch(err => {
                console.error('路由跳转失败:', err)
                // 如果路由跳转失败，使用window.location强制跳转
                window.location.href = '/'
              })
            }, 500)
          } else {
            ElMessage.success(data.message || '登录成功')
            // 立即跳转
            router.push('/').then(() => {
              console.log('路由跳转成功')
            }).catch(err => {
              console.error('路由跳转失败:', err)
      window.location.href = '/'  
            })
          }
    } else {
          console.error('登录失败，响应数据:', data)
          ElMessage.error(data.message || data.msg || '登录失败')
    }
  })
  .catch(error => {
    console.error('登录失败:', error)
        const errorMessage = error.response?.data?.message || error.message || '登录失败，请稍后重试'
        ElMessage.error(errorMessage)
  })
  .finally(() => {
    loading.value = false
  })
    }
  })
}

// 注册处理
const handleRegister = () => {
  ElMessage.info('注册功能开发中...')
}

// 忘记密码处理
const handleResetPassword = () => {
  ElMessage.info('忘记密码功能开发中...')
}
</script>

<style scoped>
/* 登录页面容器 */
.login-wrapper {
  position: relative;
  width: 100vw;
  height: 100vh;
  overflow: hidden;
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
}

/* 背景图遮罩层 - 更柔和的遮罩 */
.login-wrapper::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, rgba(91, 141, 239, 0.15) 0%, rgba(74, 122, 217, 0.25) 100%);
  z-index: 1;
}

/* 左上角品牌信息 */
.login-brand-top {
  position: absolute;
  top: 50px;
  left: 50px;
  z-index: 3;
  color: #fff;
  animation: fadeInLeft 0.8s ease-out;
  max-width: 500px;
}

/* 登录表单容器 */
.login-container {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  z-index: 2;
  padding: 0 60px;
}

.login-title {
  font-size: 52px;
  font-weight: 800;
  margin: 0 0 16px 0;
  text-shadow: 0 4px 12px rgba(0, 0, 0, 0.5), 0 2px 4px rgba(0, 0, 0, 0.3);
  letter-spacing: 3px;
  color: #fff;
  line-height: 1.2;
  text-transform: none;
  white-space: nowrap;
}

.login-desc {
  font-size: 18px;
  margin: 0;
  font-weight: 500;
  text-shadow: 0 2px 6px rgba(0, 0, 0, 0.4), 0 1px 2px rgba(0, 0, 0, 0.3);
  opacity: 0.95;
  line-height: 1.6;
  color: rgba(255, 255, 255, 0.98);
  letter-spacing: 0.5px;
}

/* 右侧登录表单 */
.login-right {
  width: 420px;
  background: rgba(255, 255, 255, 0.3);
  backdrop-filter: blur(8px);
  border-radius: 20px;
  padding: 50px 40px;
  box-shadow: 0 8px 32px rgba(91, 141, 239, 0.15);
  animation: fadeInRight 0.8s ease-out;
  border: 1px solid rgba(255, 255, 255, 0.3);
}

.login-form-wrapper {
  width: 100%;
}

.login-form-title {
  font-size: 28px;
  font-weight: 600;
  color: #2C3E50;
  margin: 0 0 8px 0;
  text-align: center;
}

.login-form-subtitle {
  font-size: 14px;
  color: #5A6C7D;
  margin: 0 0 35px 0;
  text-align: center;
}

.login-form {
  margin-top: 10px;
}

/* 输入框样式 - 更柔和的设计 */
:deep(.el-input__wrapper) {
  border-radius: 10px;
  border: 1px solid #E2E8F0;
  background-color: #F8FAFC;
  box-shadow: none;
  padding: 0 15px;
  transition: all 0.3s ease;
}

:deep(.el-input__wrapper:hover) {
  border-color: #5B8DEF;
  background-color: #FFFFFF;
}

:deep(.el-input__wrapper.is-focus) {
  border-color: #5B8DEF;
  background-color: #FFFFFF;
  box-shadow: 0 0 0 3px rgba(91, 141, 239, 0.1);
}

:deep(.el-input__inner) {
  font-size: 15px;
  height: 48px;
  color: #2C3E50;
  font-weight: 400;
}

:deep(.el-input__inner::placeholder) {
  color: #A8B5C1;
  font-weight: 400;
}

:deep(.el-input__prefix) {
  color: #5B8DEF;
}

/* 登录按钮 */
.login-btn-submit {
  width: 100%;
  margin-top: 10px;
  height: 48px;
  font-size: 16px;
  font-weight: 500;
  border-radius: 10px;
  border: none;
  background: linear-gradient(135deg, #5B8DEF 0%, #4A7AD9 100%);
  color: #fff;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 4px 12px rgba(91, 141, 239, 0.3);
}

.login-btn-submit:hover {
  background: linear-gradient(135deg, #6B9DFF 0%, #5B8DEF 100%);
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(91, 141, 239, 0.4);
}

.login-btn-submit:active {
  transform: translateY(0);
  box-shadow: 0 2px 8px rgba(91, 141, 239, 0.3);
}

.login-btn-submit.is-loading {
  background: linear-gradient(135deg, #5B8DEF 0%, #4A7AD9 100%);
}

/* 登录链接样式 */
.login-links {
  display: flex;
  justify-content: space-between;
  margin-top: 25px;
  padding-top: 20px;
  border-top: 1px solid rgba(255, 255, 255, 0.2);
}

:deep(.el-link--primary) {
  color: #5B8DEF;
  font-size: 16px;
  font-weight: 600;
  transition: all 0.3s ease;
  text-decoration: none;
  text-shadow: 0 1px 2px rgba(255, 255, 255, 0.3);
}

:deep(.el-link--primary:hover) {
  color: #4A7AD9;
  transform: translateX(3px);
  text-shadow: 0 2px 4px rgba(91, 141, 239, 0.4);
}

/* 表单验证错误样式 */
:deep(.el-form-item.is-error .el-input__wrapper) {
  border-color: #F56565;
  background-color: #FFF5F5;
}

:deep(.el-form-item__error) {
  color: #F56565;
  font-size: 12px;
  padding-top: 4px;
}

/* 动画 */
@keyframes fadeInLeft {
  from {
    opacity: 0;
    transform: translateX(-30px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

@keyframes fadeInRight {
  from {
    opacity: 0;
    transform: translateX(30px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .login-container {
    justify-content: center;
    padding: 20px;
  }
  
  .login-brand-top {
    top: 40px;
    left: 40px;
    max-width: 400px;
}

  .login-title {
    font-size: 42px;
    letter-spacing: 2px;
  }
  
  .login-desc {
    font-size: 16px;
  }
  
  .login-right {
    width: 100%;
    max-width: 420px;
}
}

@media (max-width: 768px) {
  .login-brand-top {
    top: 30px;
    left: 30px;
    max-width: 300px;
}

  .login-title {
    font-size: 36px;
    letter-spacing: 2px;
    white-space: normal;
  }
  
  .login-desc {
    font-size: 14px;
  }
  
  .login-container {
    padding: 15px;
}

  .login-right {
    padding: 40px 30px;
  }
  
  .login-form-title {
    font-size: 24px;
  }
}

@media (max-width: 480px) {
  .login-brand-top {
    top: 20px;
    left: 20px;
    max-width: 250px;
}

  .login-title {
    font-size: 28px;
    letter-spacing: 1px;
    white-space: normal;
  }
  
  .login-desc {
    font-size: 12px;
  }
}
</style>
