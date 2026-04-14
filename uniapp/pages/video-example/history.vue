<template>
  <view class="page">
    <view class="container">
      <view class="header">
        <text class="title">历史视频范例</text>
      </view>
      
      <!-- 查询条件区域 -->
      <view class="search-form">
        <view class="form-row">
          <view class="form-item">
            <text class="label">视频主题</text>
            <input 
              class="input" 
              v-model="searchParams.topic" 
              placeholder="请输入视频主题" 
              type="text"
            />
          </view>
        </view>
        
        <view class="form-row">
          <view class="form-item">
            <text class="label">开始时间</text>
            <input 
              class="input" 
              v-model="searchParams.startTime" 
              placeholder="YYYY-MM-DD HH:mm" 
              type="text"
            />
          </view>
          
          <view class="form-item">
            <text class="label">结束时间</text>
            <input 
              class="input" 
              v-model="searchParams.endTime" 
              placeholder="YYYY-MM-DD HH:mm" 
              type="text"
            />
          </view>
        </view>
        
        <view class="search-actions">
          <button class="search-btn" @tap="search">查询</button>
          <button class="reset-btn" @tap="reset">重置</button>
        </view>
      </view>
      
      <!-- 视频范例列表 -->
      <view class="list">
        <view 
          v-for="item in videoExamples" 
          :key="item.id" 
          class="list-item"
          @tap="viewDetail(item)"
        >
          <view class="item-content">
            <view class="item-title">{{ item.topic }}</view>
            <view class="item-lesson-plan">关联教案：{{ item.lessonPlanTitle }}</view>
            <view class="item-time">{{ formatTime(item.createTime) }}</view>
          </view>
          <view class="item-arrow">›</view>
        </view>
        
        <!-- 空状态 -->
        <view v-if="videoExamples.length === 0 && !isLoading" class="empty">
          <text class="empty-text">暂无视频范例数据</text>
        </view>
      </view>
      
      <!-- 加载中 -->
      <view v-if="isLoading" class="loading">
        <text class="loading-text">加载中...</text>
      </view>
      
      <!-- 分页控件 -->
      <view v-if="totalElements > 0" class="pagination">
        <button 
          class="page-btn" 
          @tap="prevPage" 
          :disabled="!hasPrevious || isLoading"
        >上一页</button>
        <view class="page-info">
          <text>第 {{ currentPage + 1 }} 页，共 {{ totalPages }} 页</text>
        </view>
        <button 
          class="page-btn" 
          @tap="nextPage" 
          :disabled="!hasNext || isLoading"
        >下一页</button>
      </view>
    </view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      searchParams: {
        topic: '',
        startTime: '',
        endTime: ''
      },
      videoExamples: [],
      currentPage: 0,
      pageSize: 10,
      totalElements: 0,
      totalPages: 0,
      hasPrevious: false,
      hasNext: false,
      isLoading: false
    };
  },
  onLoad() {
    // 页面加载时默认查询第一页数据
    this.search();
  },
  methods: {
    // 查询视频范例
    search() {
      // 重置到第一页
      this.currentPage = 0;
      this.loadData();
    },
    
    // 重置查询条件
    reset() {
      this.searchParams = {
        topic: '',
        startTime: '',
        endTime: ''
      };
      this.search();
    },
    
    // 加载数据
    loadData() {
      this.isLoading = true;
      
      // 构建请求参数
      const params = {
        page: this.currentPage,
        size: this.pageSize
      };
      
      // 添加条件查询参数
      if (this.searchParams.topic.trim()) {
        params.topic = this.searchParams.topic.trim();
      }
      
      if (this.searchParams.startTime.trim()) {
        params.startTime = this.searchParams.startTime.trim();
      }
      
      if (this.searchParams.endTime.trim()) {
        params.endTime = this.searchParams.endTime.trim();
      }
      
      // 调用后端API查询视频范例
      uni.request({
        url: 'http://localhost:8080/api/video-example/list',
        method: 'GET',
        data: params,
        success: (res) => {
          if (res.statusCode === 200 && res.data) {
            this.videoExamples = res.data.content || [];
            this.totalElements = res.data.totalElements || 0;
            this.totalPages = res.data.totalPages || 0;
            this.hasPrevious = res.data.hasPrevious || false;
            this.hasNext = res.data.hasNext || false;
          } else {
            uni.showToast({
              title: '查询失败，请重试',
              icon: 'none'
            });
          }
        },
        fail: (err) => {
          console.error('查询失败：', err);
          uni.showToast({
            title: '网络错误，请重试',
            icon: 'none'
          });
        },
        complete: () => {
          this.isLoading = false;
        }
      });
    },
    
    // 上一页
    prevPage() {
      if (this.hasPrevious) {
        this.currentPage--;
        this.loadData();
      }
    },
    
    // 下一页
    nextPage() {
      if (this.hasNext) {
        this.currentPage++;
        this.loadData();
      }
    },
    
    // 查看详情
    viewDetail(item) {
      // 这里可以跳转到视频范例详情页，目前简化处理为弹出信息
      uni.showModal({
        title: '视频范例详情',
        content: `主题：${item.topic}\n关联教案：${item.lessonPlanTitle}\n视频URL：${item.videoUrl}`,
        showCancel: false
      });
    },
    
    // 格式化时间
    formatTime(timeStr) {
      if (!timeStr) return '';
      try {
        const date = new Date(timeStr);
        return date.toLocaleString('zh-CN', {
          year: 'numeric',
          month: '2-digit',
          day: '2-digit',
          hour: '2-digit',
          minute: '2-digit'
        });
      } catch (e) {
        return timeStr;
      }
    }
  }
};
</script>

<style>
.page {
  min-height: 100vh;
  background-color: #F7F8FA;
}

.container {
  padding: 20rpx;
}

.header {
  margin-bottom: 30rpx;
}

.title {
  font-size: 36rpx;
  font-weight: bold;
  color: #333;
}

.search-form {
  background-color: #fff;
  padding: 20rpx;
  border-radius: 10rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.1);
}

.form-row {
  display: flex;
  flex-direction: row;
  margin-bottom: 20rpx;
}

.form-item {
  flex: 1;
  margin-right: 20rpx;
}

.form-item:last-child {
  margin-right: 0;
}

.label {
  display: block;
  margin-bottom: 10rpx;
  font-size: 28rpx;
  color: #666;
}

.input {
  width: 100%;
  padding: 20rpx;
  border: 1rpx solid #ddd;
  border-radius: 8rpx;
  font-size: 28rpx;
  box-sizing: border-box;
}

.search-actions {
  display: flex;
  justify-content: flex-end;
}

.search-btn {
  padding: 0 30rpx;
  background-color: #007AFF;
  color: #fff;
  border: none;
  border-radius: 8rpx;
  font-size: 28rpx;
  margin-right: 20rpx;
}

.reset-btn {
  padding: 0 30rpx;
  background-color: #fff;
  color: #333;
  border: 1rpx solid #ddd;
  border-radius: 8rpx;
  font-size: 28rpx;
}

.list {
  background-color: #fff;
  border-radius: 10rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.1);
}

.list-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20rpx;
  border-bottom: 1rpx solid #f0f0f0;
}

.list-item:last-child {
  border-bottom: none;
}

.item-content {
  flex: 1;
}

.item-title {
  font-size: 32rpx;
  font-weight: 500;
  color: #333;
  margin-bottom: 10rpx;
}

.item-lesson-plan {
  font-size: 28rpx;
  color: #666;
  margin-bottom: 10rpx;
}

.item-time {
  font-size: 24rpx;
  color: #999;
}

.item-arrow {
  font-size: 32rpx;
  color: #ccc;
}

.empty {
  padding: 80rpx 0;
  text-align: center;
}

.empty-text {
  font-size: 28rpx;
  color: #999;
}

.loading {
  padding: 40rpx 0;
  text-align: center;
}

.loading-text {
  font-size: 28rpx;
  color: #666;
}

.pagination {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20rpx;
  background-color: #fff;
  border-radius: 10rpx;
  box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.1);
}

.page-btn {
  padding: 0 30rpx;
  background-color: #007AFF;
  color: #fff;
  border: none;
  border-radius: 8rpx;
  font-size: 28rpx;
}

.page-btn[disabled] {
  background-color: #ccc;
}

.page-info {
  font-size: 28rpx;
  color: #666;
}
</style>