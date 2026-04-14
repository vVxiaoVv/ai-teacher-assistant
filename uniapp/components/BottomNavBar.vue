<template>
  <view class="bottom-nav-bar">
    <view 
      class="nav-item" 
      :class="{ active: currentTab === 'home' }"
      @tap="handleTabClick('home')"
    >
      <view class="nav-icon">🏠</view>
      <text class="nav-text">首页</text>
    </view>
    <view 
      class="nav-item" 
      :class="{ active: currentTab === 'reports' }"
      @tap="handleTabClick('reports')"
    >
      <view class="nav-icon">📊</view>
      <text class="nav-text">报告</text>
    </view>
    <view 
      class="nav-item" 
      :class="{ active: currentTab === 'management' }"
      @tap="handleTabClick('management')"
    >
      <view class="nav-icon">⚙️</view>
      <text class="nav-text">管理</text>
    </view>
    <view 
      class="nav-item" 
      :class="{ active: currentTab === 'mine' }"
      @tap="handleTabClick('mine')"
    >
      <view class="nav-icon">👤</view>
      <text class="nav-text">我的</text>
    </view>
  </view>
</template>

<script>
export default {
  name: 'BottomNavBar',
  props: {
    currentTab: {
      type: String,
      default: 'home'
    }
  },
  methods: {
    handleTabClick(tab) {
      if (this.currentTab === tab) {
        return; // 如果点击的是当前标签，不处理
      }
      
      // 获取当前页面路径
      const pages = getCurrentPages();
      const currentPage = pages[pages.length - 1];
      const currentRoute = currentPage.route;
      
      // 如果点击的是"我的"标签，跳转到我的页面
      if (tab === 'mine') {
        if (currentRoute !== 'pages/mine/index') {
          uni.redirectTo({
            url: '/pages/mine/index'
          });
        }
        return;
      }
      
      // 其他标签都跳转到首页，并传递标签参数
      if (currentRoute !== 'pages/index/index') {
        uni.redirectTo({
          url: `/pages/index/index?tab=${tab}`
        });
      } else {
        // 如果已经在首页，直接触发事件切换标签
        this.$emit('tab-change', tab);
      }
    }
  }
};
</script>

<style scoped>
.bottom-nav-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  height: 100rpx;
  padding-bottom: env(safe-area-inset-bottom);
  background: #FFFFFF;
  border-top: 1rpx solid #EEEEEE;
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: space-around;
  box-shadow: 0 -2rpx 10rpx rgba(0, 0, 0, 0.05);
  z-index: 999;
}

.nav-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 8rpx 0;
  transition: all 0.2s;
}

.nav-icon {
  font-size: 40rpx;
  margin-bottom: 4rpx;
  line-height: 1;
  transition: transform 0.2s;
}

.nav-item:active .nav-icon {
  transform: scale(0.9);
}

.nav-text {
  font-size: 22rpx;
  color: #86909C;
  transition: color 0.2s;
}

.nav-item.active .nav-text {
  color: #4A90D9;
  font-weight: 500;
}

.nav-item.active .nav-icon {
  transform: scale(1.1);
}
</style>
