<template>
  <view class="page">
    <view class="page-bg-layer"></view>
    <view class="hero">
      <image
        class="hero-bg"
        src="/static/education-bg.jpg"
        mode="aspectFill"
      ></image>
      <view class="hero-mask"></view>
      <view class="hero-content">
        <view class="hero-header">
          <view class="app-badge">AI 教学助手</view>
          <view class="header-right">
            <image 
              v-if="avatarUrl" 
              class="user-avatar" 
              :src="avatarUrl" 
              mode="aspectFill"
            ></image>
            <view v-else class="user-avatar-placeholder">
              <text class="avatar-text">用</text>
            </view>
            <view class="logout-btn" @tap="handleLogout">
              <text class="logout-icon">退出</text>
            </view>
          </view>
        </view>
        <view class="title">磨课助手</view>
        <view class="subtitle">让每一次试讲，都更接近理想课堂</view>
      </view>
    </view>

    <view class="container">
      <view class="section-title"></view>

      <view class="card-list">
        <!-- 视频纠偏 - 首页 -->
        <view 
          v-if="shouldShowCard('video-correction')" 
          class="card" 
          @tap="goVideoCorrection"
        >
          <view class="card-icon video-icon">
            <view class="icon-inner">📹</view>
          </view>
          <view class="card-content">
            <view class="card-title">视频纠偏</view>
            <view class="card-desc">
              上传试讲视频，系统自动生成课堂结构、互动情况与语言表达等多维度分析报告
            </view>
          </view>
          <view class="card-arrow">›</view>
        </view>

        <!-- 虚拟课堂 - 首页 -->
        <view 
          v-if="shouldShowCard('virtual-classroom')" 
          class="card" 
          @tap="goVirtualClassroom"
        >
          <view class="card-icon virtual-classroom-icon">
            <view class="icon-inner">🎬</view>
          </view>
          <view class="card-content">
            <view class="card-title">虚拟课堂</view>
            <view class="card-desc">
              边看逐字稿边录制教学视频，支持AI跟读，帮助您更好地练习讲课
            </view>
          </view>
          <view class="card-arrow">›</view>
        </view>

        <!-- 纠偏历史 - 报告 -->
        <view 
          v-if="shouldShowCard('history')" 
          class="card" 
          @tap="goHistory"
        >
          <view class="card-icon history-icon">
            <view class="icon-inner">📋</view>
          </view>
          <view class="card-content">
            <view class="card-title">纠偏历史</view>
            <view class="card-desc">
              查看历史上传的视频纠偏报告，回顾过往分析与改进建议
            </view>
          </view>
          <view class="card-arrow">›</view>
        </view>

        <!-- 教师画像 - 报告 -->
        <view 
          v-if="shouldShowCard('teacher-portrait')" 
          class="card" 
          @tap="goTeacherPortrait"
        >
          <view class="card-icon teacher-portrait-icon">
            <view class="icon-inner">👨‍🏫</view>
          </view>
          <view class="card-content">
            <view class="card-title">教师画像</view>
            <view class="card-desc">
              查看教师画像分析，了解教学风格和特点，持续改进教学能力
            </view>
          </view>
          <view class="card-arrow">›</view>
        </view>

        <!-- 教案管理 - 管理 -->
        <view 
          v-if="shouldShowCard('lesson-plan')" 
          class="card" 
          @tap="goLessonPlan"
        >
          <view class="card-icon lesson-plan-icon">
            <view class="icon-inner">📚</view>
          </view>
          <view class="card-content">
            <view class="card-title">教案管理</view>
            <view class="card-desc">
              上传教案文件，自动生成讲课逐字稿，包含老师提问和学生回答
            </view>
          </view>
          <view class="card-arrow">›</view>
        </view>

        <!-- 学生画像管理 - 管理 -->
        <view 
          v-if="shouldShowCard('student-portrait')" 
          class="card" 
          @tap="goStudentPortrait"
        >
          <view class="card-icon student-portrait-icon">
            <view class="icon-inner">👤</view>
          </view>
          <view class="card-content">
            <view class="card-title">学生画像管理</view>
            <view class="card-desc">
              管理学生画像信息，记录学生特点，为个性化教学提供支持
            </view>
          </view>
          <view class="card-arrow">›</view>
        </view>

        <!-- 课堂管理 - 管理 -->
        <view 
          v-if="shouldShowCard('classroom')" 
          class="card" 
          @tap="goClassroom"
        >
          <view class="card-icon classroom-icon">
            <view class="icon-inner">🏛️</view>
          </view>
          <view class="card-content">
            <view class="card-title">课堂管理</view>
            <view class="card-desc">
              创建和管理课堂，添加学生到课堂，组织教学活动
            </view>
          </view>
          <view class="card-arrow">›</view>
        </view>
      </view>
    </view>

    <view class="footer-decoration">
      <view class="decoration-circle circle-1"></view>
      <view class="decoration-circle circle-2"></view>
    </view>

    <!-- 底部导航栏 -->
    <BottomNavBar 
      :currentTab="currentTab" 
      @tab-change="handleTabChange"
    />
  </view>
</template>

<script>
import { uniRequest } from '../../common/request.js';
import BottomNavBar from '../../components/BottomNavBar.vue';

export default {
  components: {
    BottomNavBar
  },
  data() {
    return {
      avatarUrl: '',
      currentTab: 'home' // home, reports, management, mine
    };
  },
  onLoad(options) {
    // 从URL参数获取标签
    if (options.tab) {
      this.currentTab = options.tab;
    }
    this.fetchUserInfo();
  },
  onShow() {
    // 每次显示页面时刷新用户信息
    this.fetchUserInfo();
  },
  computed: {
    filteredCards() {
      switch(this.currentTab) {
        case 'home':
          return ['video-correction', 'virtual-classroom'];
        case 'reports':
          return ['history', 'teacher-portrait'];
        case 'management':
          return ['lesson-plan', 'student-portrait', 'classroom'];
        default:
          return [];
      }
    }
  },
  methods: {
    shouldShowCard(cardId) {
      return this.filteredCards.includes(cardId);
    },
    handleTabChange(tab) {
      this.currentTab = tab;
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
          }
        }
      } catch (error) {
        console.error('获取用户信息失败:', error);
        // 获取失败时使用默认头像（空字符串，显示占位符）
        this.avatarUrl = '';
      }
    },
    goVideoCorrection() {
      uni.navigateTo({
        url: '/pages/video-correction/index'
      });
    },
    goVirtualClassroom() {
      uni.navigateTo({
        url: '/pages/virtual-classroom/index'
      });
    },
    goHistory() {
      uni.navigateTo({
        url: '/pages/history/index'
      });
    },
    goLessonPlan() {
      uni.navigateTo({
        url: '/pages/lesson-plan/index'
      });
    },
    goStudentPortrait() {
      uni.navigateTo({
        url: '/pages/student-portrait/index'
      });
    },
    goClassroom() {
      uni.navigateTo({
        url: '/pages/classroom/index'
      });
    },
    goTeacherPortrait() {
      uni.navigateTo({
        url: '/pages/teacher-portrait/index'
      });
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

<style>
.page {
  min-height: 100vh;
  background: #F7F8FA;
  position: relative;
  overflow: hidden;
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

.hero {
  position: relative;
  height: 360rpx;
  overflow: hidden;
  z-index: 1;
}

.hero-bg {
  width: 100%;
  height: 100%;
  opacity: 1;
}

.hero-mask {
  position: absolute;
  left: 0;
  top: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(180deg, rgba(0, 0, 0, 0.3) 0%, rgba(0, 0, 0, 0.5) 100%);
}

.hero-content {
  position: absolute;
  left: 40rpx;
  right: 40rpx;
  bottom: 40rpx;
  color: #ffffff;
}

.hero-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.app-badge {
  display: inline-flex;
  padding: 8rpx 20rpx;
  font-size: 24rpx;
  border-radius: 8rpx;
  background: rgba(255, 255, 255, 0.2);
  color: #ffffff;
  font-weight: 500;
  backdrop-filter: blur(10rpx);
}

.logout-btn {
  padding: 8rpx 20rpx;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 8rpx;
  backdrop-filter: blur(10rpx);
  border: 1rpx solid rgba(255, 255, 255, 0.3);
  transition: all 0.3s;
}

.logout-btn:active {
  background: rgba(255, 255, 255, 0.3);
  transform: scale(0.95);
}

.user-avatar {
  width: 56rpx;
  height: 56rpx;
  border-radius: 50%;
  border: 2rpx solid rgba(255, 255, 255, 0.5);
  background: rgba(255, 255, 255, 0.2);
}

.user-avatar-placeholder {
  width: 56rpx;
  height: 56rpx;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.2);
  border: 2rpx solid rgba(255, 255, 255, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  backdrop-filter: blur(10rpx);
}

.avatar-text {
  font-size: 24rpx;
  color: #ffffff;
  font-weight: 500;
}

.logout-icon {
  font-size: 24rpx;
  color: #ffffff;
  font-weight: 500;
}

.title {
  font-size: 52rpx;
  font-weight: 600;
  margin-bottom: 12rpx;
  color: #ffffff;
  letter-spacing: 0;
}

.subtitle {
  font-size: 28rpx;
  color: rgba(255, 255, 255, 0.9);
  font-weight: 400;
  line-height: 1.5;
}

.container {
  padding: 32rpx 32rpx calc(100rpx + env(safe-area-inset-bottom) + 100rpx);
  margin-top: -40rpx;
  position: relative;
  z-index: 1;
}

.section-title {
  font-size: 28rpx;
  color: #646A73;
  margin-bottom: 24rpx;
}

.card-list {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.card {
  display: flex;
  flex-direction: row;
  align-items: flex-start;
  padding: 32rpx;
  border-radius: 16rpx;
  background: #FFFFFF;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.04), 0 1rpx 2rpx rgba(0, 0, 0, 0.06);
  position: relative;
  transition: all 0.3s ease;
}

.card:active {
  transform: scale(0.98);
  box-shadow: 0 1rpx 4rpx rgba(0, 0, 0, 0.08);
}

.card.disabled {
  opacity: 0.6;
}

.card-icon {
  width: 88rpx;
  height: 88rpx;
  border-radius: 16rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 24rpx;
  flex-shrink: 0;
}

.icon-inner {
  font-size: 44rpx;
  line-height: 1;
}

.video-icon {
  background: linear-gradient(135deg, #3370FF, #1E6FFF);
}

.class-icon {
  background: linear-gradient(135deg, #00D4AA, #00B894);
}

.history-icon {
  background: linear-gradient(135deg, #7B68EE, #6A5ACD);
}

.lesson-plan-icon {
  background: linear-gradient(135deg, #FF9500, #FF8800);
}

.speech-icon {
  background: linear-gradient(135deg, #FF6B6B, #EE5A5A);
}

.tts-icon {
  background: linear-gradient(135deg, #4ECDC4, #45B7AA);
}

.student-portrait-icon {
  background: linear-gradient(135deg, #9B59B6, #8E44AD);
}

.classroom-icon {
  background: linear-gradient(135deg, #3498DB, #2980B9);
}

.teacher-portrait-icon {
  background: linear-gradient(135deg, #E67E22, #D35400);
}

.virtual-classroom-icon {
  background: linear-gradient(135deg, #1ABC9C, #16A085);
}

.card-content {
  flex: 1;
}

.card-title {
  font-size: 32rpx;
  font-weight: 500;
  margin-bottom: 12rpx;
  color: #1F2329;
  line-height: 1.4;
}

.card-desc {
  font-size: 28rpx;
  color: #646A73;
  line-height: 1.6;
}

.card-arrow {
  font-size: 32rpx;
  color: #86909C;
  margin-left: 16rpx;
  align-self: center;
  flex-shrink: 0;
}

.card-tag {
  font-size: 24rpx;
  color: #F7BA1E;
  padding: 8rpx 16rpx;
  border-radius: 8rpx;
  background: #FFF7E6;
  margin-left: 16rpx;
  align-self: center;
  flex-shrink: 0;
}

.footer-decoration {
  position: relative;
  height: 200rpx;
  width: 100%;
  overflow: hidden;
  margin-top: -100rpx;
  z-index: 0;
}

.decoration-circle {
  position: absolute;
  border-radius: 50%;
  opacity: 0.06;
}

.circle-1 {
  width: 400rpx;
  height: 400rpx;
  left: -150rpx;
  bottom: -100rpx;
  background: #3370FF;
}

.circle-2 {
  width: 300rpx;
  height: 300rpx;
  right: -100rpx;
  bottom: 0;
  background: #00D4AA;
}
</style>