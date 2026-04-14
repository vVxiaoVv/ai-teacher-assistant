<template>
  <view class="page">
    <!-- 搜索条件 -->
    <view class="search-section">
      <input 
        class="search-input" 
        v-model="searchVideoUrl" 
        placeholder="搜索视频URL"
        @confirm="handleSearch"
      />
      <button size="mini" @tap="handleSearch">搜索</button>
      <button size="mini" @tap="handleReset" style="margin-left: 10rpx;">重置</button>
    </view>

    <view v-if="loading" class="loading-container">
      <text>加载中...</text>
    </view>

    <view v-else-if="!loading && historyList.length === 0" class="empty-container">
      <view class="empty-icon">📝</view>
      <view class="empty-text">暂无历史记录</view>
      <view class="empty-desc">上传视频或输入URL后，分析报告将显示在这里</view>
    </view>

    <view v-else class="container">
      <view class="history-list">
        <view
          v-for="item in historyList"
          :key="item.id"
          class="history-item"
          @tap="viewDetail(item)"
        >
          <view class="item-header">
            <view class="item-title">{{ getItemTitle(item) }}</view>
          </view>
          <view class="item-summary">{{ getSummary(item.formattedMessage) }}</view>
          <view class="item-footer">
            <text class="item-time">{{ formatTime(item.createTime) }}</text>
            <text class="item-arrow">›</text>
          </view>
        </view>
      </view>

      <!-- 分页控件 -->
      <view class="pagination">
        <button 
          size="mini" 
          :disabled="currentPage === 0" 
          @tap="goToPage(currentPage - 1)"
        >
          上一页
        </button>
        <text class="page-info">第 {{ currentPage + 1 }} / {{ totalPages }} 页</text>
        <button 
          size="mini" 
          :disabled="currentPage >= totalPages - 1" 
          @tap="goToPage(currentPage + 1)"
        >
          下一页
        </button>
      </view>
    </view>
  </view>
</template>

<script>
import { API_BASE_URL, uniRequest } from '../../common/request.js';

export default {
  data() {
    return {
      loading: true,
      historyList: [],
      searchVideoUrl: '',
      currentPage: 0,
      pageSize: 10,
      totalPages: 1,
      totalElements: 0
    };
  },
  onLoad() {
    this.loadHistory();
  },
  onShow() {
    // 从详情页返回时不需要刷新，保持当前状态
    // 如果需要刷新，可以在详情页返回时调用
  },
  methods: {
    loadHistory() {
      this.loading = true;
      
      // 构建查询参数
      let url = '/api/video-analysis/history?page=' + this.currentPage + '&size=' + this.pageSize;
      if (this.searchVideoUrl && this.searchVideoUrl.trim()) {
        url += '&videoUrl=' + encodeURIComponent(this.searchVideoUrl.trim());
      }
      
      uniRequest({
        url: url,
        method: 'GET'
      }).then((res) => {
        this.loading = false;
        
        console.log('历史记录接口响应:', JSON.stringify(res, null, 2));
        
        if (res.statusCode === 200 && res.data) {
          // 后端返回格式: {code: 0, msg: "success", data: {content: [...], totalPages: ..., ...}}
          if (res.data.code === 0 && res.data.data) {
            const pageData = res.data.data;
            console.log('分页数据:', JSON.stringify(pageData, null, 2));
            
            // 使用 $set 确保 Vue 响应式更新
            this.$set(this, 'historyList', pageData.content || []);
            this.$set(this, 'totalPages', pageData.totalPages || 1);
            this.$set(this, 'totalElements', pageData.totalElements || 0);
            this.$set(this, 'currentPage', pageData.number || 0);
            
            // 强制更新视图
            this.$forceUpdate();
            
            console.log('历史记录列表已设置，数量:', this.historyList.length);
            console.log('总页数:', this.totalPages);
            console.log('当前页:', this.currentPage);
            
            if (this.historyList.length === 0) {
              console.log('历史记录列表为空');
            }
          } else {
            console.error('接口返回错误:', res.data);
            uni.showToast({
              title: res.data.msg || '加载失败',
              icon: 'none'
            });
          }
        } else {
          console.error('响应格式错误:', res);
          uni.showToast({
            title: '加载失败',
            icon: 'none'
          });
        }
      }).catch((err) => {
        this.loading = false;
        console.error('请求失败:', err);
        uni.showToast({
          title: '网络错误',
          icon: 'none'
        });
      });
    },
    handleSearch() {
      this.currentPage = 0;
      this.loadHistory();
    },
    handleReset() {
      this.searchVideoUrl = '';
      this.currentPage = 0;
      this.loadHistory();
    },
    goToPage(page) {
      if (page < 0 || page >= this.totalPages) {
        return;
      }
      this.currentPage = page;
      this.loadHistory();
      // 滚动到顶部
      uni.pageScrollTo({
        scrollTop: 0,
        duration: 300
      });
    },
    viewDetail(item) {
      // 跳转到详情页，使用临时存储传递数据，避免URL参数长度限制
      try {
        // 使用临时存储保存数据
        uni.setStorageSync('history_detail_item', item);
        uni.navigateTo({
          url: '/pages/history/detail'
        });
      } catch (e) {
        console.error('跳转失败:', e);
        uni.showToast({
          title: '跳转失败',
          icon: 'none'
        });
      }
    },
    getSummary(message) {
      if (!message || message === null || message === undefined) {
        return '暂无概述';
      }
      let msgStr = String(message);
      
      // 先处理 HTML 转义字符
      msgStr = this.formatHtmlContent(msgStr);
      
      // 取前100个字符
      return msgStr.length > 100 ? msgStr.substring(0, 100) + '...' : msgStr;
    },
    formatHtmlContent(text) {
      if (!text) return '';
      
      // 将 HTML 转义字符转换为可显示的内容
      let formatted = String(text);
      
      // 将 <br>、<br/>、<br /> 转换为换行符（在列表页摘要中转换为空格）
      formatted = formatted.replace(/<br\s*\/?>/gi, ' ');
      
      // 将其他常见的 HTML 转义字符转换
      formatted = formatted.replace(/&nbsp;/g, ' ');  // 非断行空格
      formatted = formatted.replace(/&lt;/g, '<');   // 小于号
      formatted = formatted.replace(/&gt;/g, '>');   // 大于号
      formatted = formatted.replace(/&amp;/g, '&');  // 和号
      formatted = formatted.replace(/&quot;/g, '"'); // 双引号
      formatted = formatted.replace(/&#39;/g, "'");  // 单引号
      formatted = formatted.replace(/&apos;/g, "'"); // 单引号（另一种写法）
      
      // 将其他 HTML 标签去除（但保留内容）
      formatted = formatted.replace(/<[^>]+>/g, '');
      
      return formatted;
    },
    getItemTitle(item) {
      if (!item) return '视频分析';
      
      // 直接使用 video_analysis_record 的 title 字段
      if (item.title && item.title.trim()) {
        return item.title.trim();
      }
      
      // 如果没有 title 字段，使用默认值
      return '视频分析';
    },
    extractTitleFromMessage(message) {
      if (!message) return null;
      
      // 查找"课题："后面的内容
      const lines = message.split('\n');
      for (let line of lines) {
        line = line.trim();
        // 匹配格式：课题：[内容] 或 课题:[内容]
        if (line.includes('课题：') || line.includes('课题:')) {
          const index = line.indexOf('课题：') !== -1 ? line.indexOf('课题：') : line.indexOf('课题:');
          if (index !== -1) {
            let title = line.substring(index + 3).trim();
            // 如果标题包含换行符，只取第一行
            if (title.includes('\n')) {
              title = title.split('\n')[0].trim();
            }
            // 去除可能的后续内容（如"综合等级"等）
            if (title.includes('综合等级')) {
              title = title.substring(0, title.indexOf('综合等级')).trim();
            }
            // 去除 HTML 标签和转义字符
            title = this.formatHtmlContent(title);
            return title && title.trim() ? title.trim() : null;
          }
        }
      }
      
      return null;
    },
    formatTime(timeStr) {
      if (!timeStr) return '';
      // 处理后端返回的时间字符串，格式化为友好显示
      try {
        const date = new Date(timeStr);
        const now = new Date();
        const diff = now - date;
        const minutes = Math.floor(diff / 60000);
        const hours = Math.floor(diff / 3600000);
        const days = Math.floor(diff / 86400000);

        if (minutes < 1) {
          return '刚刚';
        } else if (minutes < 60) {
          return minutes + '分钟前';
        } else if (hours < 24) {
          return hours + '小时前';
        } else if (days < 7) {
          return days + '天前';
        } else {
          return date.toLocaleDateString('zh-CN', {
            month: '2-digit',
            day: '2-digit'
          });
        }
      } catch (e) {
        return timeStr;
      }
    },
    formatDateTime(timeStr) {
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
        return '';
      }
    }
  }
};
</script>

<style>
.page {
  min-height: 100vh;
  background-color: #f5f5f5;
}

.search-section {
  padding: 20rpx;
  background-color: #ffffff;
  display: flex;
  flex-direction: row;
  align-items: center;
  margin-bottom: 20rpx;
}

.search-input {
  flex: 1;
  padding: 16rpx 20rpx;
  border: 2rpx solid #e5e7eb;
  border-radius: 8rpx;
  font-size: 28rpx;
  margin-right: 20rpx;
  background-color: #f9fafb;
}

.loading-container,
.empty-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 120rpx 40rpx;
}

.empty-icon {
  font-size: 120rpx;
  margin-bottom: 24rpx;
}

.empty-text {
  font-size: 32rpx;
  color: #666;
  margin-bottom: 12rpx;
}

.empty-desc {
  font-size: 26rpx;
  color: #999;
}

.container {
  padding: 24rpx;
}

.history-list {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.history-item {
  background-color: #ffffff;
  border-radius: 20rpx;
  padding: 28rpx 24rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.06);
}

.item-header {
  display: flex;
  flex-direction: row;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16rpx;
}

.item-title {
  font-size: 32rpx;
  font-weight: 600;
  color: #111827;
  flex: 1;
}

.item-status {
  font-size: 22rpx;
  padding: 4rpx 12rpx;
  border-radius: 12rpx;
  font-weight: 500;
}

.status-processing {
  background-color: #dbeafe;
  color: #2563eb;
}

.status-done {
  background-color: #d1fae5;
  color: #059669;
}

.status-failed {
  background-color: #fee2e2;
  color: #dc2626;
}

.item-summary {
  font-size: 26rpx;
  color: #6b7280;
  line-height: 1.6;
  margin-bottom: 16rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.item-footer {
  display: flex;
  flex-direction: row;
  justify-content: space-between;
  align-items: center;
}

.item-time {
  font-size: 24rpx;
  color: #9ca3af;
}

.item-arrow {
  font-size: 32rpx;
  color: #d1d5db;
}

.pagination {
  display: flex;
  flex-direction: row;
  justify-content: center;
  align-items: center;
  padding: 40rpx 20rpx;
  gap: 30rpx;
}

.page-info {
  font-size: 28rpx;
  color: #666;
}
</style>

