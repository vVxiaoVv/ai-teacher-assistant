<template>
  <view class="page">
    <!-- 主要操作区域 -->
    <view class="action-container">
      <!-- 选择历史逐字稿 -->
      <view class="action-card" @tap="goToSelectScript">
        <view class="card-left">
          <view class="icon-box history">
            <text class="icon">📋</text>
          </view>
          <view class="card-info">
            <text class="card-title">选择历史逐字稿</text>
            <text class="card-desc">从已生成的教案逐字稿中选择</text>
          </view>
        </view>
        <text class="card-arrow">›</text>
      </view>

      <!-- 上传教案生成逐字稿 -->
      <view class="action-card" @tap="goToUploadLessonPlan">
        <view class="card-left">
          <view class="icon-box upload">
            <text class="icon">📤</text>
          </view>
          <view class="card-info">
            <text class="card-title">上传教案生成逐字稿</text>
            <text class="card-desc">上传新教案并自动生成逐字稿</text>
          </view>
        </view>
        <text class="card-arrow">›</text>
      </view>
    </view>

    <!-- 底部说明 -->
    <view class="footer-tips">
      <view class="tips-wrapper">
        <text class="tips-item">查看逐字稿</text>
        <text class="tips-sep">→</text>
        <text class="tips-item">录制视频</text>
        <text class="tips-sep">→</text>
        <text class="tips-item">导出保存</text>
        <view class="tips-tag">支持AI跟读</view>
      </view>
    </view>
  </view>
</template>

<script>
export default {
  data() {
    return {};
  },
  methods: {
    // 跳转到选择历史逐字稿页面
    goToSelectScript() {
      uni.navigateTo({
        url: '/pages/virtual-classroom/select-script'
      });
    },
    // 跳转到上传教案页面（带重定向参数）
    goToUploadLessonPlan() {
      uni.navigateTo({
        url: '/pages/lesson-plan/upload?redirect=virtual-classroom'
      });
    }
  }
};
</script>

<style scoped>
.page {
  min-height: 100vh;
  background: #F7F8FA;
  padding: 40rpx;
  display: flex;
  flex-direction: column;
}

/* 主要操作区域 - 居中分布 */
.action-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 32rpx;
  padding-bottom: 160rpx; /* 为底部说明留出空间 */
}

.action-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 40rpx 32rpx;
  background: #FFFFFF;
  border-radius: 24rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.04);
  transition: all 0.2s ease;
}

.action-card:active {
  transform: scale(0.98);
  background: #FAFAFA;
}

.card-left {
  display: flex;
  align-items: center;
  gap: 28rpx;
}

.icon-box {
  width: 92rpx;
  height: 92rpx;
  border-radius: 20rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.icon-box.history {
  background: rgba(74, 144, 217, 0.1);
  color: #4A90D9;
}

.icon-box.upload {
  background: rgba(82, 196, 26, 0.1);
  color: #52C41A;
}

.icon {
  font-size: 44rpx;
}

.card-info {
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.card-title {
  font-size: 32rpx;
  font-weight: 600;
  color: #333333;
}

.card-desc {
  font-size: 24rpx;
  color: #999999;
}

.card-arrow {
  font-size: 40rpx;
  color: #DDDDDD;
  margin-left: 16rpx;
}

/* 底部说明 */
.footer-tips {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: #FFFFFF;
  padding: 32rpx 40rpx;
  padding-bottom: calc(32rpx + env(safe-area-inset-bottom));
  border-top: 1rpx solid #F0F0F0;
}

.tips-wrapper {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12rpx;
  flex-wrap: wrap;
}

.tips-item {
  font-size: 24rpx;
  color: #666666;
}

.tips-sep {
  font-size: 20rpx;
  color: #CCCCCC;
}

.tips-tag {
  margin-left: 12rpx;
  font-size: 20rpx;
  color: #4A90D9;
  background: rgba(74, 144, 217, 0.08);
  padding: 4rpx 12rpx;
  border-radius: 6rpx;
  border: 1rpx solid rgba(74, 144, 217, 0.2);
}
</style>
