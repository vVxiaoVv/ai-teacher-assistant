<template>
  <view class="page">
    <!-- 顶部导航栏 -->
    <view class="nav-bar">
      <view class="nav-back" @tap="goBack">
        <text class="back-icon">‹</text>
        <text class="back-text">返回</text>
      </view>
      <view class="nav-title">语音合成</view>
      <view class="nav-right"></view>
    </view>
    
    <!-- 页面内容 -->
    <view class="content">
      <!-- 介绍说明 -->
      <view class="intro-section">
        <view class="intro-icon">🔊</view>
        <view class="intro-text">
          <text class="intro-title">文字转语音</text>
          <text class="intro-desc">输入文字，选择喜欢的语音角色，一键生成语音</text>
        </view>
      </view>
      
      <!-- 语音合成组件 -->
      <SpeechSynthesis 
        :default-text="defaultText"
        :show-subtitle="true"
        :max-length="1000"
        @synthesis-complete="onSynthesisComplete"
        @synthesis-error="onSynthesisError"
      />
      
      <!-- 使用提示 -->
      <view class="tips-section">
        <view class="tips-title">使用提示</view>
        <view class="tips-list">
          <view class="tip-item">
            <text class="tip-icon">💡</text>
            <text class="tip-text">支持最多 1000 字的文本合成</text>
          </view>
          <view class="tip-item">
            <text class="tip-icon">🎭</text>
            <text class="tip-text">多种语音角色可选，包括男声和女声</text>
          </view>
          <view class="tip-item">
            <text class="tip-icon">📱</text>
            <text class="tip-text">合成的语音可以在线播放</text>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import SpeechSynthesis from '@/components/SpeechSynthesis.vue';

export default {
  components: {
    SpeechSynthesis
  },
  
  data() {
    return {
      defaultText: ''
    };
  },
  
  onLoad(options) {
    // 支持从其他页面传递默认文本
    if (options.text) {
      this.defaultText = decodeURIComponent(options.text);
    }
  },
  
  methods: {
    goBack() {
      uni.navigateBack();
    },
    
    onSynthesisComplete(data) {
      console.log('[TTS Page] 合成完成:', data);
      uni.showToast({
        title: '语音合成成功',
        icon: 'success'
      });
    },
    
    onSynthesisError(err) {
      console.error('[TTS Page] 合成失败:', err);
      uni.showToast({
        title: err.message || '合成失败',
        icon: 'none'
      });
    }
  }
};
</script>

<style scoped>
.page {
  min-height: 100vh;
  background: linear-gradient(180deg, #f0f7f5 0%, #f5f7fa 100%);
}

/* 导航栏 */
.nav-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20rpx 32rpx;
  padding-top: calc(20rpx + env(safe-area-inset-top));
  background: linear-gradient(135deg, #4ECDC4, #45B7AA);
}

.nav-back {
  display: flex;
  align-items: center;
  padding: 12rpx 16rpx;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 8rpx;
}

.back-icon {
  font-size: 36rpx;
  color: #fff;
  margin-right: 4rpx;
}

.back-text {
  font-size: 28rpx;
  color: #fff;
}

.nav-title {
  font-size: 36rpx;
  font-weight: 600;
  color: #fff;
}

.nav-right {
  width: 120rpx;
}

/* 内容区域 */
.content {
  padding: 32rpx;
}

/* 介绍区域 */
.intro-section {
  display: flex;
  align-items: center;
  padding: 32rpx;
  background: #fff;
  border-radius: 16rpx;
  margin-bottom: 24rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.06);
}

.intro-icon {
  font-size: 64rpx;
  margin-right: 24rpx;
}

.intro-text {
  flex: 1;
}

.intro-title {
  display: block;
  font-size: 32rpx;
  font-weight: 600;
  color: #333;
  margin-bottom: 8rpx;
}

.intro-desc {
  display: block;
  font-size: 26rpx;
  color: #666;
  line-height: 1.5;
}

/* 提示区域 */
.tips-section {
  margin-top: 32rpx;
  padding: 24rpx;
  background: rgba(78, 205, 196, 0.1);
  border-radius: 12rpx;
  border: 1rpx solid rgba(78, 205, 196, 0.2);
}

.tips-title {
  font-size: 28rpx;
  font-weight: 500;
  color: #45B7AA;
  margin-bottom: 16rpx;
}

.tips-list {
  display: flex;
  flex-direction: column;
  gap: 12rpx;
}

.tip-item {
  display: flex;
  align-items: center;
}

.tip-icon {
  font-size: 28rpx;
  margin-right: 12rpx;
}

.tip-text {
  font-size: 26rpx;
  color: #666;
}
</style>




