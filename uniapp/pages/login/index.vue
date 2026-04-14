<template>
  <view class="login-page">
    <!-- 背景图片层 -->
    <view class="bg-layer">
      <image
        class="bg-image"
        src="/static/education-bg.jpg"
        mode="aspectFill"
      ></image>
      <view class="bg-mask"></view>
    </view>

    <!-- 登录表单容器 -->
    <view class="login-container">
      <view class="login-card">
        <!-- Logo和标题 -->
        <view class="login-header">
          <view class="app-badge">AI 教学助手</view>
          <view class="login-title">磨课助手</view>
          <view class="login-subtitle">让每一次试讲，都更接近理想课堂</view>
        </view>

        <!-- 登录表单 -->
        <view class="login-form">
          <view class="form-item">
            <input
              class="form-input"
              v-model="loginForm.username"
              placeholder="请输入用户名"
              placeholder-style="color: rgba(255, 255, 255, 0.6)"
              :disabled="loading"
            />
          </view>

          <view class="form-item">
            <input
              class="form-input"
              v-model="loginForm.password"
              type="password"
              placeholder="请输入密码"
              placeholder-style="color: rgba(255, 255, 255, 0.6)"
              :disabled="loading"
              @confirm="handleLogin"
            />
          </view>

          <button
            class="login-btn"
            :class="{ 'btn-loading': loading }"
            :disabled="loading || !canLogin"
            @tap="handleLogin"
          >
            {{ loading ? '登录中...' : '登录' }}
          </button>

          <view v-if="errorMessage" class="error-message">
            {{ errorMessage }}
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import { API_BASE_URL } from '../../common/request.js';

export default {
  data() {
    return {
      loginForm: {
        username: '',
        password: ''
      },
      loading: false,
      errorMessage: ''
    };
  },
  computed: {
    canLogin() {
      return this.loginForm.username.trim() && this.loginForm.password.trim();
    }
  },
  onLoad() {
    // 检查是否已登录
    const token = uni.getStorageSync('token');
    const userId = uni.getStorageSync('userId');
    if (token || userId) {
      // 已登录，跳转到首页
      uni.redirectTo({
        url: '/pages/index/index'
      });
    }
  },
  methods: {
    handleLogin() {
      if (!this.canLogin) {
        uni.showToast({
          title: '请输入用户名和密码',
          icon: 'none'
        });
        return;
      }

      this.loading = true;
      this.errorMessage = '';

      uni.request({
        url: API_BASE_URL + '/api/user/login',
        method: 'POST',
        header: {
          'Content-Type': 'application/json'
        },
        data: {
          username: this.loginForm.username.trim(),
          password: this.loginForm.password
        },
        success: (res) => {
          this.loading = false;
          
          if (res.statusCode === 200 && res.data) {
            const data = res.data;
            
            if (data.success === true || data.success === 'true') {
              // 登录成功，保存用户信息
              if (data.token) {
                uni.setStorageSync('token', data.token);
              }
              if (data.userId) {
                uni.setStorageSync('userId', String(data.userId));
              }
              if (data.username) {
                uni.setStorageSync('username', data.username);
              }
              if (data.role) {
                uni.setStorageSync('role', data.role);
              }

              uni.showToast({
                title: '登录成功',
                icon: 'success'
              });

              // 延迟跳转，让用户看到成功提示
              setTimeout(() => {
                uni.redirectTo({
                  url: '/pages/index/index'
                });
              }, 500);
            } else {
              // 登录失败
              this.errorMessage = data.message || '登录失败，请检查用户名和密码';
            }
          } else {
            this.errorMessage = '登录失败，请稍后重试';
          }
        },
        fail: (err) => {
          this.loading = false;
          console.error('登录请求失败:', err);
          this.errorMessage = '网络错误，请检查网络连接';
        }
      });
    }
  }
};
</script>

<style>
.login-page {
  width: 100%;
  min-height: 100vh;
  position: relative;
  overflow: hidden;
}

.bg-layer {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 0;
}

.bg-image {
  width: 100%;
  height: 100%;
  opacity: 1;
}

.bg-mask {
  position: absolute;
  left: 0;
  top: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(180deg, rgba(0, 0, 0, 0.3) 0%, rgba(0, 0, 0, 0.5) 100%);
}

.login-container {
  position: relative;
  z-index: 1;
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40rpx;
}

.login-card {
  width: 100%;
  max-width: 600rpx;
  background: rgba(255, 255, 255, 0.15);
  border-radius: 24rpx;
  padding: 60rpx 48rpx;
  box-shadow: 0 8rpx 32rpx rgba(0, 0, 0, 0.15);
  backdrop-filter: blur(20rpx);
  border: 1rpx solid rgba(255, 255, 255, 0.2);
}

.login-header {
  text-align: center;
  margin-bottom: 60rpx;
}

.app-badge {
  display: inline-flex;
  padding: 8rpx 20rpx;
  font-size: 24rpx;
  border-radius: 8rpx;
  background: rgba(255, 255, 255, 0.2);
  color: #ffffff;
  font-weight: 500;
  margin-bottom: 24rpx;
  backdrop-filter: blur(10rpx);
}

.login-title {
  font-size: 48rpx;
  font-weight: 600;
  color: #ffffff;
  margin-bottom: 16rpx;
  letter-spacing: 0;
  text-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.2);
}

.login-subtitle {
  font-size: 26rpx;
  color: rgba(255, 255, 255, 0.9);
  line-height: 1.5;
}

.login-form {
  width: 100%;
}

.form-item {
  margin-bottom: 32rpx;
}

.form-input {
  width: 100%;
  height: 88rpx;
  padding: 0 24rpx;
  background: rgba(255, 255, 255, 0.15);
  border: 1rpx solid rgba(255, 255, 255, 0.3);
  border-radius: 12rpx;
  font-size: 28rpx;
  color: #ffffff;
  box-sizing: border-box;
  transition: all 0.3s;
  backdrop-filter: blur(10rpx);
}

.form-input:focus {
  border-color: rgba(255, 255, 255, 0.5);
  background: rgba(255, 255, 255, 0.2);
}

.form-input[disabled] {
  background: rgba(255, 255, 255, 0.1);
  color: rgba(255, 255, 255, 0.5);
}

.login-btn {
  width: 100%;
  height: 88rpx;
  background: rgba(255, 255, 255, 0.25);
  color: #ffffff;
  border: 1rpx solid rgba(255, 255, 255, 0.4);
  border-radius: 12rpx;
  font-size: 32rpx;
  font-weight: 500;
  margin-top: 20rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.15);
  transition: all 0.3s;
  backdrop-filter: blur(10rpx);
}

.login-btn:active {
  transform: scale(0.98);
  background: rgba(255, 255, 255, 0.3);
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.2);
}

.login-btn[disabled] {
  background: rgba(255, 255, 255, 0.1);
  border-color: rgba(255, 255, 255, 0.2);
  color: rgba(255, 255, 255, 0.5);
  box-shadow: none;
}

.btn-loading {
  opacity: 0.8;
}

.error-message {
  margin-top: 24rpx;
  padding: 16rpx 20rpx;
  background: rgba(255, 77, 79, 0.2);
  border: 1rpx solid rgba(255, 77, 79, 0.4);
  border-radius: 8rpx;
  color: #ffffff;
  font-size: 24rpx;
  text-align: center;
  line-height: 1.5;
  backdrop-filter: blur(10rpx);
}
</style>

