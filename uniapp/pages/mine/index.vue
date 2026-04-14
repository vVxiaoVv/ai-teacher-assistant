<template>
  <view class="page">
    <view class="page-bg-layer"></view>
    
    <!-- 用户信息卡片 -->
    <view class="user-card">
      <view class="user-header">
        <image 
          v-if="avatarUrl" 
          class="user-avatar-large" 
          :src="avatarUrl" 
          mode="aspectFill"
        ></image>
        <view v-else class="user-avatar-large-placeholder">
          <text class="avatar-text-large">{{ usernameFirstChar }}</text>
        </view>
        <view class="user-info">
          <view class="username">{{ username || '用户' }}</view>
          <view class="user-role" v-if="role">{{ role }}</view>
        </view>
      </view>
    </view>

    <!-- 功能列表 -->
    <view class="menu-section">
      <view class="menu-item" @tap="handleLogout">
        <view class="menu-icon logout-icon">🚪</view>
        <text class="menu-text">退出登录</text>
        <text class="menu-arrow">›</text>
      </view>
    </view>

    <!-- 底部导航栏 -->
    <BottomNavBar 
      :currentTab="'mine'" 
      @tab-change="handleTabChange"
    />
  </view>
</template>

<script>
import BottomNavBar from '../../components/BottomNavBar.vue';
import { uniRequest } from '../../common/request.js';

export default {
  components: {
    BottomNavBar
  },
  data() {
    return {
      avatarUrl: '',
      username: '',
      role: ''
    };
  },
  computed: {
    usernameFirstChar() {
      if (this.username) {
        return this.username.charAt(0).toUpperCase();
      }
      return '用';
    }
  },
  onLoad() {
    this.loadUserInfo();
  },
  onShow() {
    this.loadUserInfo();
  },
  methods: {
    handleTabChange(tab) {
      if (tab !== 'mine') {
        // 如果切换到其他标签，跳转到首页
        uni.redirectTo({
          url: `/pages/index/index?tab=${tab}`
        });
      }
    },
    loadUserInfo() {
      // 从本地存储加载用户信息
      this.username = uni.getStorageSync('username') || '';
      this.role = uni.getStorageSync('role') || '';
      
      // 从服务器获取用户详细信息（包括头像）
      this.fetchUserInfo();
    },
    async fetchUserInfo() {
      try {
        const res = await uniRequest({
          url: '/api/user/info',
          method: 'GET'
        });
        
        if (res.statusCode === 200 && res.data) {
          const data = res.data;
          if (data.code === 0 && data.data) {
            this.avatarUrl = data.data.avatarUrl || '';
            if (data.data.username) {
              this.username = data.data.username;
            }
            if (data.data.role) {
              this.role = data.data.role;
            }
          }
        }
      } catch (error) {
        console.error('获取用户信息失败:', error);
      }
    },
    handleLogout() {
      uni.showModal({
        title: '提示',
        content: '确定要退出登录吗？',
        confirmText: '退出',
        cancelText: '取消',
        success: (res) => {
          if (res.confirm) {
            // 清除本地存储的用户信息
            uni.removeStorageSync('token');
            uni.removeStorageSync('userId');
            uni.removeStorageSync('username');
            uni.removeStorageSync('role');
            
            uni.showToast({
              title: '已退出登录',
              icon: 'success'
            });
            
            // 延迟跳转到登录页
            setTimeout(() => {
              uni.reLaunch({
                url: '/pages/login/index'
              });
            }, 500);
          }
        }
      });
    }
  }
};
</script>

<style scoped>
.page {
  min-height: 100vh;
  background: #F7F8FA;
  position: relative;
  padding-bottom: calc(100rpx + env(safe-area-inset-bottom));
}

.page-bg-layer {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(180deg, #F7F8FA 0%, #FFFFFF 100%);
  pointer-events: none;
  z-index: 0;
}

.user-card {
  background: #FFFFFF;
  margin: 32rpx;
  padding: 40rpx;
  border-radius: 24rpx;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.08);
  position: relative;
  z-index: 1;
}

.user-header {
  display: flex;
  align-items: center;
  gap: 32rpx;
}

.user-avatar-large {
  width: 120rpx;
  height: 120rpx;
  border-radius: 50%;
  border: 4rpx solid #E8F4FD;
}

.user-avatar-large-placeholder {
  width: 120rpx;
  height: 120rpx;
  border-radius: 50%;
  background: linear-gradient(135deg, #4A90D9, #357ABD);
  display: flex;
  align-items: center;
  justify-content: center;
  border: 4rpx solid #E8F4FD;
}

.avatar-text-large {
  font-size: 48rpx;
  color: #FFFFFF;
  font-weight: 600;
}

.user-info {
  flex: 1;
}

.username {
  font-size: 40rpx;
  font-weight: 600;
  color: #1F2329;
  margin-bottom: 8rpx;
}

.user-role {
  font-size: 28rpx;
  color: #86909C;
}

.menu-section {
  background: #FFFFFF;
  margin: 0 32rpx;
  border-radius: 24rpx;
  overflow: hidden;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.08);
  position: relative;
  z-index: 1;
}

.menu-item {
  display: flex;
  align-items: center;
  padding: 32rpx;
  border-bottom: 1rpx solid #F0F0F0;
  transition: background 0.2s;
}

.menu-item:last-child {
  border-bottom: none;
}

.menu-item:active {
  background: #F7F8FA;
}

.menu-icon {
  font-size: 40rpx;
  margin-right: 24rpx;
  width: 48rpx;
  text-align: center;
}

.menu-text {
  flex: 1;
  font-size: 32rpx;
  color: #1F2329;
}

.menu-arrow {
  font-size: 32rpx;
  color: #86909C;
}

.logout-icon {
  color: #F56C6C;
}
</style>