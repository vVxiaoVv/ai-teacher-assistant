<template>
  <view class="page">
    <!-- 顶部标题栏 -->
    <view class="header">
      <view class="header-title">选择逐字稿</view>
    </view>

    <!-- 搜索栏 -->
    <view class="search-section">
      <view class="search-input-wrapper" :class="{ focused: searchFocused }">
        <input
          class="search-input"
          v-model="searchKeyword"
          placeholder="搜索教案标题"
          @focus="searchFocused = true"
          @blur="searchFocused = false"
          @confirm="handleSearch"
        />
        <view class="search-btn" @tap="handleSearch">搜索</view>
      </view>
    </view>

    <!-- 加载状态 -->
    <view v-if="loading" class="loading-container">
      <view class="loading-spinner"></view>
      <text class="loading-text">加载中...</text>
    </view>

    <!-- 逐字稿列表 -->
    <view v-else-if="scriptList.length > 0" class="script-list">
      <view
        v-for="item in scriptList"
        :key="item.lessonPlanId"
        class="script-card"
        @tap="selectScript(item)"
      >
        <view class="card-header">
          <view class="card-title">{{ item.lessonPlanTitle }}</view>
          <view class="card-badge">已生成逐字稿</view>
        </view>
        <view class="card-content">
          <view class="script-preview">{{ getScriptPreview(item.scriptContent) }}</view>
          <view class="card-meta">
            <text class="meta-time">{{ formatTime(item.uploadTime) }}</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 空状态 -->
    <view v-else class="empty-container">
      <view class="empty-icon">📄</view>
      <text class="empty-text">暂无已生成逐字稿的教案</text>
      <view class="empty-tip">请先上传教案并生成逐字稿</view>
      <view class="empty-btn" @tap="goToUpload">去上传教案</view>
    </view>

    <!-- 分页 -->
    <view v-if="total > 0 && totalPages > 1" class="pagination">
      <view
        class="page-btn"
        :class="{ disabled: currentPage === 1 }"
        @tap="handlePrevPage"
      >
        上一页
      </view>
      <view class="page-info">
        {{ currentPage }} / {{ totalPages }}
      </view>
      <view
        class="page-btn"
        :class="{ disabled: currentPage === totalPages }"
        @tap="handleNextPage"
      >
        下一页
      </view>
    </view>
  </view>
</template>

<script>
import { uniRequest } from '../../common/request.js';

export default {
  data() {
    return {
      loading: false,
      scriptList: [],
      total: 0,
      currentPage: 1,
      pageSize: 10,
      searchFocused: false,
      searchKeyword: ''
    };
  },
  computed: {
    totalPages() {
      return Math.ceil(this.total / this.pageSize);
    }
  },
  onLoad() {
    this.loadData();
  },
  onShow() {
    // 返回页面时刷新数据
    this.loadData();
  },
  methods: {
    async loadData() {
      this.loading = true;
      this.scriptList = [];
      
      try {
        const params = {
          page: this.currentPage - 1,
          size: this.pageSize
        };
        
        if (this.searchKeyword && this.searchKeyword.trim()) {
          params.title = this.searchKeyword.trim();
        }
        
        // 获取教案列表
        const res = await uniRequest({
          url: '/api/lesson-plan/list',
          method: 'GET',
          data: params
        });
        
        if (res.statusCode === 200 && res.data) {
          let lessonPlanList = [];
          
          if (res.data.content !== undefined) {
            lessonPlanList = res.data.content || [];
            this.total = res.data.totalElements || 0;
            this.currentPage = (res.data.number || 0) + 1;
          } else if (res.data.code === 0 && res.data.data) {
            lessonPlanList = res.data.data.content || [];
            this.total = res.data.data.totalElements || 0;
            this.currentPage = (res.data.data.number || 0) + 1;
          }
          
          // 检查每个教案是否有逐字稿，并获取逐字稿内容
          const scriptsWithContent = [];
          for (const lessonPlan of lessonPlanList) {
            try {
              const scriptRes = await uniRequest({
                url: `/api/script/lesson-plan/${lessonPlan.id}`,
                method: 'GET'
              });
              
              if (scriptRes.statusCode === 200 && scriptRes.data) {
                const scriptContent = scriptRes.data.content || scriptRes.data;
                if (scriptContent) {
                  scriptsWithContent.push({
                    lessonPlanId: lessonPlan.id,
                    lessonPlanTitle: lessonPlan.title,
                    scriptContent: typeof scriptContent === 'string' ? scriptContent : JSON.stringify(scriptContent),
                    uploadTime: lessonPlan.uploadTime
                  });
                }
              }
            } catch (err) {
              // 404 表示没有逐字稿，跳过
              if (err.statusCode !== 404) {
                console.error('获取逐字稿失败:', err);
              }
            }
          }
          
          this.scriptList = scriptsWithContent;
          this.total = scriptsWithContent.length;
        }
      } catch (error) {
        console.error('加载数据失败:', error);
        uni.showToast({
          title: '加载失败，请稍后重试',
          icon: 'none'
        });
      } finally {
        this.loading = false;
      }
    },
    handleSearch() {
      this.currentPage = 1;
      this.loadData();
    },
    handlePrevPage() {
      if (this.currentPage > 1) {
        this.currentPage--;
        this.loadData();
      }
    },
    handleNextPage() {
      if (this.currentPage < this.totalPages) {
        this.currentPage++;
        this.loadData();
      }
    },
    getScriptPreview(content) {
      if (!content) return '';
      // 移除HTML标签
      const plainText = content.replace(/<[^>]*>/g, '').replace(/&nbsp;/g, ' ');
      // 截取前100个字符
      if (plainText.length > 100) {
        return plainText.substring(0, 100) + '...';
      }
      return plainText;
    },
    formatTime(timeStr) {
      if (!timeStr) return '';
      try {
        const date = new Date(timeStr);
        const year = date.getFullYear();
        const month = String(date.getMonth() + 1).padStart(2, '0');
        const day = String(date.getDate()).padStart(2, '0');
        const hours = String(date.getHours()).padStart(2, '0');
        const minutes = String(date.getMinutes()).padStart(2, '0');
        return `${year}-${month}-${day} ${hours}:${minutes}`;
      } catch (e) {
        return timeStr;
      }
    },
    selectScript(item) {
      // 将逐字稿内容通过 eventChannel 传递，避免 URL 过长
      uni.navigateTo({
        url: '/pages/virtual-classroom/record',
        success: (res) => {
          res.eventChannel.emit('scriptData', {
            lessonPlanId: item.lessonPlanId,
            lessonPlanTitle: item.lessonPlanTitle,
            scriptContent: item.scriptContent
          });
        }
      });
    },
    goToUpload() {
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
  background: #FFFFFF;
  padding: 40rpx;
  padding-top: calc(8vh + 40rpx);
}

/* 顶部标题栏 */
.header {
  margin-bottom: 32rpx;
}

.header-title {
  font-size: 40rpx;
  font-weight: 600;
  color: #333333;
}

/* 搜索栏 */
.search-section {
  margin-bottom: 32rpx;
}

.search-input-wrapper {
  display: flex;
  align-items: center;
  height: 84rpx;
  padding: 0 24rpx;
  background: #F5F7FA;
  border-radius: 16rpx;
  transition: box-shadow 0.3s;
}

.search-input-wrapper.focused {
  box-shadow: 0 0 0 2rpx #4A90D9;
}

.search-input {
  flex: 1;
  height: 84rpx;
  font-size: 28rpx;
  color: #333333;
  background: transparent;
}

.search-input::placeholder {
  color: #999999;
}

.search-btn {
  padding: 16rpx 24rpx;
  background: #4A90D9;
  color: #FFFFFF;
  font-size: 26rpx;
  border-radius: 8rpx;
}

/* 加载状态 */
.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 200rpx 0;
}

.loading-spinner {
  width: 48rpx;
  height: 48rpx;
  border: 4rpx solid #E5E5E5;
  border-top-color: #4A90D9;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 24rpx;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.loading-text {
  font-size: 28rpx;
  color: #666666;
}

/* 逐字稿列表 */
.script-list {
  margin-bottom: 32rpx;
}

.script-card {
  padding: 32rpx;
  background: #FFFFFF;
  border-radius: 16rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.06);
  margin-bottom: 24rpx;
  transition: transform 0.2s;
}

.script-card:active {
  transform: scale(0.98);
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16rpx;
}

.card-title {
  font-size: 32rpx;
  font-weight: 500;
  color: #333333;
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.card-badge {
  font-size: 22rpx;
  color: #52C41A;
  background: #F6FFED;
  padding: 6rpx 16rpx;
  border-radius: 8rpx;
  flex-shrink: 0;
  margin-left: 16rpx;
}

.card-content {
  margin-top: 16rpx;
}

.script-preview {
  font-size: 26rpx;
  color: #666666;
  line-height: 1.6;
  margin-bottom: 16rpx;
}

.card-meta {
  display: flex;
  align-items: center;
}

.meta-time {
  font-size: 24rpx;
  color: #999999;
}

/* 空状态 */
.empty-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 150rpx 0;
}

.empty-icon {
  font-size: 80rpx;
  margin-bottom: 24rpx;
}

.empty-text {
  font-size: 30rpx;
  color: #333333;
  margin-bottom: 12rpx;
}

.empty-tip {
  font-size: 26rpx;
  color: #999999;
  margin-bottom: 32rpx;
}

.empty-btn {
  padding: 20rpx 48rpx;
  background: #4A90D9;
  color: #FFFFFF;
  font-size: 28rpx;
  border-radius: 12rpx;
}

/* 分页 */
.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 32rpx;
  padding: 32rpx 0;
}

.page-btn {
  padding: 16rpx 32rpx;
  background: #F5F7FA;
  color: #333333;
  font-size: 26rpx;
  border-radius: 8rpx;
}

.page-btn.disabled {
  color: #CCCCCC;
  pointer-events: none;
}

.page-info {
  font-size: 26rpx;
  color: #666666;
}
</style>

