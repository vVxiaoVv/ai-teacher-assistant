<template>
  <view class="page">
    <view v-if="loading" class="loading-container">
      <text>加载中...</text>
    </view>

    <view v-else-if="portraitData" class="detail-container">
      <view class="detail-header">
        <view class="header-avatar">
          <image
            v-if="userAvatar"
            :src="userAvatar"
            mode="aspectFill"
            class="avatar-img"
          />
          <view v-else class="avatar-placeholder">
            <text>{{ getInitial(portraitData.username) }}</text>
          </view>
        </view>
        <view class="header-info">
          <view class="header-name">{{ portraitData.username }}</view>
          <view class="header-subtitle">已使用 {{ portraitData.historyCount || 0 }} 条历史记录进行分析</view>
        </view>
      </view>

      <view v-if="portraitData.description" class="description-section">
        <view class="section-title">画像描述</view>
        <view class="description-content">{{ portraitData.description }}</view>
      </view>

      <view v-if="portraitData.hexagramScore" class="score-section">
        <view class="section-title">六芒星评分</view>
        <view class="score-list">
          <view class="score-item">
            <text class="score-label">教学基本功</text>
            <text class="score-value">{{ formatScore(portraitData.hexagramScore.teachingFoundation) }}</text>
          </view>
          <view class="score-item">
            <text class="score-label">教学过程设计</text>
            <text class="score-value">{{ formatScore(portraitData.hexagramScore.teachingProcessDesign) }}</text>
          </view>
          <view class="score-item">
            <text class="score-label">教态</text>
            <text class="score-value">{{ formatScore(portraitData.hexagramScore.teachingManner) }}</text>
          </view>
          <view class="score-item">
            <text class="score-label">多媒体与板书运用</text>
            <text class="score-value">{{ formatScore(portraitData.hexagramScore.multimediaAndBlackboard) }}</text>
          </view>
          <view class="score-item">
            <text class="score-label">课堂气氛</text>
            <text class="score-value">{{ formatScore(portraitData.hexagramScore.classroomAtmosphere) }}</text>
          </view>
          <view class="score-item">
            <text class="score-label">时间节奏掌控</text>
            <text class="score-value">{{ formatScore(portraitData.hexagramScore.timeRhythmControl) }}</text>
          </view>
        </view>
        <view class="average-score">
          <text class="average-label">综合得分</text>
          <text class="average-value">{{ calculateAverageScore(portraitData.hexagramScore) }}</text>
          <text class="average-unit">分</text>
        </view>
      </view>

      <view v-else class="no-data">
        <text>暂无评分数据</text>
      </view>
    </view>

    <view v-else class="error-container">
      <text>加载失败</text>
    </view>
  </view>
</template>

<script>
import { uniRequest } from '../../common/request.js';

export default {
  data() {
    return {
      loading: true,
      portraitData: null,
      userAvatar: ''
    };
  },
  onLoad(options) {
    if (options.userId) {
      this.loadPortraitDetail(parseInt(options.userId));
    } else {
      uni.showToast({
        title: '参数错误',
        icon: 'none'
      });
      setTimeout(() => {
        uni.navigateBack();
      }, 1500);
    }
  },
  methods: {
    async loadPortraitDetail(userId) {
      this.loading = true;
      try {
        const res = await uniRequest({
          url: '/api/teacher-portrait/get',
          method: 'GET',
          data: {
            userId: userId
          }
        });
        
        if (res.statusCode === 200 && res.data) {
          if (res.data.code === 0 && res.data.data) {
            this.portraitData = res.data.data;
            if (this.portraitData.avatarUrl) {
              this.userAvatar = this.portraitData.avatarUrl;
            } else if (this.portraitData.username) {
              this.userAvatar = `https://ui-avatars.com/api/?name=${encodeURIComponent(this.portraitData.username)}&size=200`;
            }
          } else {
            uni.showToast({
              title: res.data.msg || '加载失败',
              icon: 'none'
            });
            setTimeout(() => {
              uni.navigateBack();
            }, 1500);
          }
        }
      } catch (error) {
        console.error('加载画像详情失败:', error);
        uni.showToast({
          title: '加载失败，请稍后重试',
          icon: 'none'
        });
        setTimeout(() => {
          uni.navigateBack();
        }, 1500);
      } finally {
        this.loading = false;
      }
    },
    formatScore(score) {
      if (score === null || score === undefined) return '-';
      return score.toFixed(1);
    },
    calculateAverageScore(hexagramScore) {
      if (!hexagramScore) return '0.0';
      
      const scores = hexagramScore;
      const values = [
        scores.teachingFoundation || 0,
        scores.teachingProcessDesign || 0,
        scores.teachingManner || 0,
        scores.multimediaAndBlackboard || 0,
        scores.classroomAtmosphere || 0,
        scores.timeRhythmControl || 0
      ];
      
      const average = values.reduce((sum, val) => sum + val, 0) / values.length;
      return average.toFixed(1);
    },
    getInitial(name) {
      if (!name) return '?';
      return name.charAt(0).toUpperCase();
    }
  }
};
</script>

<style scoped>
.page {
  min-height: 100vh;
  background: #F7F8FA;
  padding: 32rpx;
}

.loading-container,
.error-container {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 200rpx 0;
  color: #86909C;
  font-size: 28rpx;
}

.detail-container {
  background: #FFFFFF;
  border-radius: 16rpx;
  padding: 32rpx;
}

.detail-header {
  display: flex;
  align-items: center;
  gap: 24rpx;
  margin-bottom: 32rpx;
  padding-bottom: 24rpx;
  border-bottom: 1rpx solid #E5E5E5;
}

.header-avatar {
  width: 120rpx;
  height: 120rpx;
  border-radius: 50%;
  overflow: hidden;
  flex-shrink: 0;
  background: #F7F8FA;
}

.avatar-img {
  width: 100%;
  height: 100%;
}

.avatar-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #E67E22, #D35400);
  color: #FFFFFF;
  font-size: 48rpx;
  font-weight: 600;
}

.header-info {
  flex: 1;
  min-width: 0;
}

.header-name {
  font-size: 36rpx;
  font-weight: 600;
  color: #1F2329;
  margin-bottom: 8rpx;
}

.header-subtitle {
  font-size: 24rpx;
  color: #86909C;
}

.description-section {
  margin-bottom: 32rpx;
}

.section-title {
  font-size: 32rpx;
  font-weight: 500;
  color: #1F2329;
  margin-bottom: 16rpx;
}

.description-content {
  font-size: 28rpx;
  color: #1F2329;
  line-height: 1.8;
  white-space: pre-wrap;
  word-break: break-word;
  padding: 24rpx;
  background: #F7F8FA;
  border-radius: 8rpx;
}

.score-section {
  margin-top: 32rpx;
}

.score-list {
  margin-bottom: 24rpx;
}

.score-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24rpx;
  margin-bottom: 16rpx;
  background: #F7F8FA;
  border-radius: 8rpx;
}

.score-label {
  font-size: 28rpx;
  color: #646A73;
}

.score-value {
  font-size: 32rpx;
  font-weight: 600;
  color: #E67E22;
}

.average-score {
  display: flex;
  align-items: baseline;
  justify-content: center;
  gap: 8rpx;
  padding: 32rpx;
  background: linear-gradient(135deg, #FFF7E6, #FFE7BA);
  border-radius: 8rpx;
}

.average-label {
  font-size: 28rpx;
  color: #646A73;
}

.average-value {
  font-size: 56rpx;
  font-weight: 600;
  color: #E67E22;
}

.average-unit {
  font-size: 28rpx;
  color: #646A73;
}

.no-data {
  padding: 80rpx;
  text-align: center;
  color: #86909C;
  font-size: 28rpx;
}
</style>


