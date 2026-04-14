<template>
  <view class="page">
    <view class="header">
      <view class="header-title">课堂管理</view>
      <view class="add-btn" @tap="handleAdd">
        <text class="add-icon">+</text>
        <text>创建课堂</text>
      </view>
    </view>

    <!-- 搜索栏 -->
    <view class="search-section">
      <view class="search-input-wrapper">
        <input
          class="search-input"
          v-model="searchForm.classroomName"
          placeholder="请输入课堂名称"
          @confirm="handleSearch"
        />
        <view class="search-btn" @tap="handleSearch">搜索</view>
      </view>
      <view class="reset-btn" @tap="handleReset">重置</view>
    </view>

    <!-- 加载状态 -->
    <view v-if="loading" class="loading-container">
      <text>加载中...</text>
    </view>

    <!-- 课堂列表 -->
    <view v-else-if="classroomList.length > 0" class="classroom-list">
      <view
        v-for="classroom in classroomList"
        :key="classroom.id"
        class="classroom-card"
        @tap="handleViewDetail(classroom)"
      >
        <view class="card-header">
          <view class="card-title">{{ classroom.classroomName || '未命名课堂' }}</view>
          <view class="card-id">ID: {{ classroom.id }}</view>
        </view>
        <view class="card-content">
          <view class="info-item">
            <text class="info-label">学生数量：</text>
            <text class="info-value">{{ classroom.studentCount || 0 }} 人</text>
          </view>
          <view v-if="classroom.description" class="card-desc">
            {{ getSummary(classroom.description) }}
          </view>
        </view>
        <view class="card-actions">
          <view class="action-btn edit-btn" @tap.stop="handleEdit(classroom)">编辑</view>
          <view class="action-btn detail-btn" @tap.stop="handleViewDetail(classroom)">详情</view>
        </view>
      </view>
    </view>

    <!-- 空状态 -->
    <view v-else class="empty-container">
      <text class="empty-text">暂无课堂数据</text>
    </view>

    <!-- 分页 -->
    <view v-if="total > 0" class="pagination">
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
      classroomList: [],
      total: 0,
      currentPage: 1,
      pageSize: 10,
      searchForm: {
        classroomName: ''
      }
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
    // 从编辑页返回时刷新数据
    this.loadData();
  },
  methods: {
    async loadData() {
      this.loading = true;
      try {
        const params = {
          page: this.currentPage - 1,
          size: this.pageSize
        };
        
        if (this.searchForm.classroomName && this.searchForm.classroomName.trim()) {
          params.classroomName = this.searchForm.classroomName.trim();
        }
        
        const res = await uniRequest({
          url: '/api/classroom/list',
          method: 'GET',
          data: params
        });
        
        console.log('课堂列表响应:', JSON.stringify(res.data, null, 2));
        if (res.statusCode === 200 && res.data) {
          // 后端直接返回 PageResultDto，不是包装在 R 对象中
          if (res.data.content !== undefined) {
            const rawList = res.data.content || [];
            // 确保每个课堂对象都有 classroomName 字段
            this.classroomList = rawList.map(item => {
              // 调试：打印每个课堂对象
              console.log('课堂对象:', JSON.stringify(item, null, 2));
              // 如果 classroomName 为空，尝试从其他可能的字段名获取
              if (!item.classroomName && item.name) {
                item.classroomName = item.name;
              }
              return item;
            });
            console.log('处理后的课堂列表数据:', JSON.stringify(this.classroomList, null, 2));
            this.total = res.data.totalElements || 0;
            this.currentPage = (res.data.number || 0) + 1;
          } else if (res.data.code === 0 && res.data.data) {
            // 兼容包装在 R 对象中的情况
            const rawList = res.data.data.content || [];
            this.classroomList = rawList.map(item => {
              if (!item.classroomName && item.name) {
                item.classroomName = item.name;
              }
              return item;
            });
            this.total = res.data.data.totalElements || 0;
            this.currentPage = (res.data.data.number || 0) + 1;
          } else {
            console.error('响应数据格式错误:', res.data);
            uni.showToast({
              title: res.data.msg || '加载失败',
              icon: 'none'
            });
            this.classroomList = [];
            this.total = 0;
          }
        } else {
          console.error('请求失败:', res);
          uni.showToast({
            title: '请求失败',
            icon: 'none'
          });
          this.classroomList = [];
          this.total = 0;
        }
      } catch (error) {
        console.error('加载数据失败:', error);
        uni.showToast({
          title: '加载失败，请稍后重试',
          icon: 'none'
        });
        this.classroomList = [];
        this.total = 0;
      } finally {
        this.loading = false;
      }
    },
    handleSearch() {
      this.currentPage = 1;
      this.loadData();
    },
    handleReset() {
      this.searchForm.classroomName = '';
      this.currentPage = 1;
      this.loadData();
    },
    handleAdd() {
      uni.navigateTo({
        url: '/pages/classroom/add'
      });
    },
    handleEdit(classroom) {
      uni.navigateTo({
        url: `/pages/classroom/add?id=${classroom.id}`
      });
    },
    handleViewDetail(classroom) {
      uni.navigateTo({
        url: `/pages/classroom/detail?id=${classroom.id}`
      });
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
    getSummary(text) {
      if (!text) return '';
      if (text.length > 50) {
        return text.substring(0, 50) + '...';
      }
      return text;
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

.add-btn {
  display: flex;
  align-items: center;
  gap: 8rpx;
  padding: 16rpx 32rpx;
  background: linear-gradient(135deg, #3498DB, #2980B9);
  color: #FFFFFF;
  border-radius: 8rpx;
  font-size: 28rpx;
}

.add-icon {
  font-size: 32rpx;
  font-weight: 600;
}

.search-section {
  display: flex;
  gap: 16rpx;
  padding: 24rpx 32rpx;
  background: #FFFFFF;
  border-bottom: 1rpx solid #E5E5E5;
}

.search-input-wrapper {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 16rpx;
  padding: 16rpx 24rpx;
  background: #F7F8FA;
  border-radius: 8rpx;
}

.search-input {
  flex: 1;
  font-size: 28rpx;
  color: #1F2329;
}

.search-btn {
  padding: 8rpx 24rpx;
  background: #3498DB;
  color: #FFFFFF;
  border-radius: 6rpx;
  font-size: 24rpx;
}

.reset-btn {
  padding: 16rpx 24rpx;
  color: #646A73;
  font-size: 28rpx;
}

.loading-container {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 100rpx 0;
  color: #86909C;
  font-size: 28rpx;
}

.classroom-list {
  padding: 24rpx 32rpx;
}

.classroom-card {
  padding: 32rpx;
  margin-bottom: 24rpx;
  background: #FFFFFF;
  border-radius: 16rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.04);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16rpx;
}

.card-title {
  font-size: 32rpx;
  font-weight: 500;
  color: #1F2329;
}

.card-id {
  font-size: 24rpx;
  color: #86909C;
}

.card-content {
  margin-bottom: 24rpx;
}

.info-item {
  margin-bottom: 12rpx;
  font-size: 28rpx;
}

.info-label {
  color: #646A73;
}

.info-value {
  color: #1F2329;
  font-weight: 500;
}

.card-desc {
  font-size: 26rpx;
  color: #646A73;
  line-height: 1.5;
  margin-top: 12rpx;
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

.edit-btn {
  background: #E8F4FF;
  color: #3498DB;
}

.detail-btn {
  background: #F0F9FF;
  color: #3498DB;
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

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 32rpx;
  padding: 32rpx;
}

.page-btn {
  padding: 16rpx 32rpx;
  background: #FFFFFF;
  color: #3498DB;
  border-radius: 8rpx;
  font-size: 28rpx;
  border: 1rpx solid #E5E5E5;
}

.page-btn.disabled {
  color: #86909C;
  background: #F7F8FA;
  border-color: #E5E5E5;
}

.page-info {
  color: #646A73;
  font-size: 28rpx;
}
</style>

