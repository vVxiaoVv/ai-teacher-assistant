<template>
  <view class="page">
    <view class="header">
      <view class="header-title">教师画像</view>
    </view>

    <!-- 加载状态 -->
    <view v-if="loading" class="loading-container">
      <text>加载中...</text>
    </view>

    <!-- 画像列表 -->
    <view v-else-if="portraitList.length > 0" class="portrait-list">
      <view
        v-for="portrait in portraitList"
        :key="portrait.userId"
        class="portrait-card"
        @tap="handleViewDetail(portrait)"
      >
        <view class="card-header">
          <view class="card-avatar">
            <image
              v-if="getAvatarUrl(portrait) && !portrait._avatarError"
              :src="getAvatarUrl(portrait)"
              mode="aspectFill"
              class="avatar-img"
              @error="handleAvatarError(portrait)"
            />
            <view v-else class="avatar-placeholder">
              <text>{{ getInitial(portrait.username) }}</text>
            </view>
          </view>
          <view class="card-info">
            <view class="card-name">{{ portrait.username }}</view>
            <view class="card-subtitle">已使用 {{ portrait.historyCount || 0 }} 条历史记录</view>
          </view>
        </view>
        <view class="card-content">
          <view v-if="portrait.hexagramScore && portrait.description" class="score-summary">
            <view class="score-item">
              <text class="score-label">综合得分</text>
              <text class="score-value">{{ calculateAverageScore(portrait.hexagramScore) }}</text>
              <text class="score-unit">分</text>
            </view>
          </view>
          <view v-else class="no-score">
            <text class="no-score-text">暂无画像数据</text>
          </view>
        </view>
        <view class="card-actions">
          <view
            v-if="portrait.description"
            class="action-btn detail-btn"
            @tap.stop="handleViewDetail(portrait)"
          >
            查看详情
          </view>
          <view
            v-if="portrait.description"
            class="action-btn regenerate-btn"
            :class="{ loading: generatingPortrait[portrait.userId] }"
            @tap.stop="handleRegeneratePortrait(portrait)"
          >
            {{ generatingPortrait[portrait.userId] ? '生成中...' : '重新生成' }}
          </view>
          <view
            v-else
            class="action-btn generate-btn"
            :class="{ loading: generatingPortrait[portrait.userId] }"
            @tap.stop="handleGeneratePortrait(portrait)"
          >
            {{ generatingPortrait[portrait.userId] ? '生成中...' : '生成画像' }}
          </view>
        </view>
      </view>
    </view>

    <!-- 空状态 -->
    <view v-else class="empty-container">
      <text class="empty-text">暂无教师画像数据</text>
    </view>
  </view>
</template>

<script>
import { uniRequest } from '../../common/request.js';

export default {
  data() {
    return {
      loading: false,
      portraitList: [],
      generatingPortrait: {}
    };
  },
  onLoad() {
    this.loadData();
  },
  methods: {
    async loadData() {
      this.loading = true;
      try {
        const res = await uniRequest({
          url: '/api/teacher-portrait/list',
          method: 'GET'
        });
        
        if (res.statusCode === 200 && res.data) {
          if (res.data.code === 0 && res.data.data) {
            this.portraitList = res.data.data || [];
            // 清除之前的头像错误标记
            this.portraitList.forEach(portrait => {
              if (portrait._avatarError) {
                this.$delete(portrait, '_avatarError');
              }
            });
            console.log('教师画像列表加载成功，数量:', this.portraitList.length);
            // 调试：打印每个老师的头像信息
            this.portraitList.forEach(portrait => {
              console.log(`老师: ${portrait.username}, avatarUrl: ${portrait.avatarUrl || 'null/empty'}, 生成URL: ${this.getAvatarUrl(portrait)}`);
            });
          } else {
            uni.showToast({
              title: res.data.msg || '加载失败',
              icon: 'none'
            });
            this.portraitList = [];
          }
        }
      } catch (error) {
        console.error('加载画像列表失败:', error);
        uni.showToast({
          title: '加载失败，请稍后重试',
          icon: 'none'
        });
        this.portraitList = [];
      } finally {
        this.loading = false;
      }
    },
    handleViewDetail(portrait) {
      uni.navigateTo({
        url: `/pages/teacher-portrait/detail?userId=${portrait.userId}`
      });
    },
    async handleGeneratePortrait(portrait) {
      this.$set(this.generatingPortrait, portrait.userId, true);
      try {
        const res = await uniRequest({
          url: '/api/teacher-portrait/generate',
          method: 'POST',
          data: {
            userId: portrait.userId
          }
        });
        
        if (res.statusCode === 200) {
          uni.showToast({
            title: '生成成功',
            icon: 'success'
          });
          this.loadData();
        } else {
          uni.showToast({
            title: res.data?.msg || '生成失败',
            icon: 'none'
          });
        }
      } catch (error) {
        console.error('生成画像失败:', error);
        uni.showToast({
          title: '生成失败，请稍后重试',
          icon: 'none'
        });
      } finally {
        this.$set(this.generatingPortrait, portrait.userId, false);
      }
    },
    async handleRegeneratePortrait(portrait) {
      this.$set(this.generatingPortrait, portrait.userId, true);
      try {
        const res = await uniRequest({
          url: '/api/teacher-portrait/generate',
          method: 'POST',
          data: {
            userId: portrait.userId
          }
        });
        
        if (res.statusCode === 200) {
          uni.showToast({
            title: '重新生成成功',
            icon: 'success'
          });
          this.loadData();
        } else {
          uni.showToast({
            title: res.data?.msg || '生成失败',
            icon: 'none'
          });
        }
      } catch (error) {
        console.error('重新生成画像失败:', error);
        uni.showToast({
          title: '生成失败，请稍后重试',
          icon: 'none'
        });
      } finally {
        this.$set(this.generatingPortrait, portrait.userId, false);
      }
    },
    getAvatarUrl(portrait) {
      // 优先使用后端返回的 avatarUrl（来自 user 表的 avatar_url 字段）
      if (portrait.avatarUrl) {
        const url = portrait.avatarUrl.trim();
        // 如果是完整的URL（http:// 或 https:// 开头），直接返回
        if (url.startsWith('http://') || url.startsWith('https://')) {
          return url;
        }
        // 如果是相对路径，可能需要添加base URL（但通常avatar_url应该是完整URL）
        // 这里先直接返回，如果后端返回的是完整URL，应该没问题
        return url;
      }
      // 如果后端没有返回 avatarUrl，使用 ui-avatars.com 生成头像作为fallback
      if (portrait.username) {
        return `https://ui-avatars.com/api/?name=${encodeURIComponent(portrait.username)}&size=200`;
      }
      return '';
    },
    handleAvatarError(portrait) {
      // 图片加载失败时，清除 avatarUrl，让占位符显示
      console.warn('头像加载失败:', portrait.username, portrait.avatarUrl);
      // 注意：这里不能直接修改 portrait.avatarUrl，因为它是响应式数据
      // 可以通过设置一个标记来避免再次尝试加载
      this.$set(portrait, '_avatarError', true);
      // 强制更新视图
      this.$forceUpdate();
    },
    getInitial(name) {
      if (!name) return '?';
      return name.charAt(0).toUpperCase();
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
    }
  }
};
</script>

<style scoped>
.page {
  min-height: 100vh;
  background: #F7F8FA;
  padding-bottom: 40rpx;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 32rpx;
  background: #FFFFFF;
  border-bottom: 1rpx solid #E5E5E5;
}

.header-title {
  font-size: 36rpx;
  font-weight: 600;
  color: #1F2329;
}

.loading-container {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 100rpx 0;
  color: #86909C;
  font-size: 28rpx;
}

.portrait-list {
  padding: 24rpx 32rpx;
}

.portrait-card {
  padding: 32rpx;
  margin-bottom: 24rpx;
  background: #FFFFFF;
  border-radius: 16rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.04);
}

.card-header {
  display: flex;
  align-items: center;
  gap: 24rpx;
  margin-bottom: 24rpx;
}

.card-avatar {
  width: 96rpx;
  height: 96rpx;
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
  font-size: 36rpx;
  font-weight: 600;
}

.card-info {
  flex: 1;
  min-width: 0;
}

.card-name {
  font-size: 32rpx;
  font-weight: 500;
  color: #1F2329;
  margin-bottom: 8rpx;
}

.card-subtitle {
  font-size: 24rpx;
  color: #86909C;
}

.card-content {
  margin-bottom: 24rpx;
}

.score-summary {
  padding: 24rpx;
  background: #F7F8FA;
  border-radius: 8rpx;
}

.score-item {
  display: flex;
  align-items: baseline;
  gap: 8rpx;
}

.score-label {
  font-size: 28rpx;
  color: #646A73;
}

.score-value {
  font-size: 48rpx;
  font-weight: 600;
  color: #E67E22;
}

.score-unit {
  font-size: 28rpx;
  color: #646A73;
}

.no-score {
  padding: 24rpx;
  text-align: center;
}

.no-score-text {
  font-size: 28rpx;
  color: #86909C;
}

.card-actions {
  display: flex;
  gap: 16rpx;
}

.action-btn {
  flex: 1;
  padding: 16rpx;
  border-radius: 8rpx;
  text-align: center;
  font-size: 28rpx;
}

.detail-btn {
  background: #E8F4FF;
  color: #3370FF;
}

.generate-btn {
  background: linear-gradient(135deg, #E67E22, #D35400);
  color: #FFFFFF;
}

.generate-btn.loading {
  opacity: 0.6;
}

.regenerate-btn {
  background: #FFF7E6;
  color: #E67E22;
}

.regenerate-btn.loading {
  opacity: 0.6;
}

.empty-container {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 200rpx 0;
}

.empty-text {
  color: #86909C;
  font-size: 28rpx;
}
</style>


