<template>
  <view class="page">
    <view class="header">
      <view class="header-title">学生画像管理</view>
      <view class="add-btn" @tap="handleAdd">
        <text class="add-icon">+</text>
        <text>添加学生</text>
      </view>
    </view>

    <!-- 搜索栏 -->
    <view class="search-section">
      <view class="search-input-wrapper">
        <input
          class="search-input"
          v-model="searchForm.studentName"
          placeholder="请输入学生姓名"
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

    <!-- 学生列表 -->
    <view v-else-if="studentList.length > 0" class="student-list">
      <view
        v-for="student in studentList"
        :key="student.id"
        class="student-card"
        @tap="handleEdit(student)"
      >
        <view class="card-avatar">
          <image
            v-if="getAvatarUrl(student)"
            :src="getAvatarUrl(student)"
            mode="aspectFill"
            class="avatar-img"
          />
          <view v-else class="avatar-placeholder">
            <text>{{ getInitial(student.studentName) }}</text>
          </view>
        </view>
        <view class="card-content">
          <view class="card-name">{{ student.studentName }}</view>
          <view v-if="student.characteristics" class="card-desc">
            {{ getSummary(student.characteristics) }}
          </view>
          <view v-else class="card-desc-empty">暂无描述</view>
        </view>
        <view class="card-actions">
          <view class="action-btn edit-btn" @tap.stop="handleEdit(student)">编辑</view>
          <view class="action-btn delete-btn" @tap.stop="handleDelete(student)">删除</view>
        </view>
      </view>
    </view>

    <!-- 空状态 -->
    <view v-else class="empty-container">
      <text class="empty-text">暂无学生数据</text>
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
      studentList: [],
      total: 0,
      currentPage: 1,
      pageSize: 10,
      searchForm: {
        studentName: ''
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
  methods: {
    async loadData() {
      this.loading = true;
      try {
        const params = {
          page: this.currentPage - 1,
          size: this.pageSize
        };
        
        if (this.searchForm.studentName && this.searchForm.studentName.trim()) {
          params.studentName = this.searchForm.studentName.trim();
        }
        
        const res = await uniRequest({
          url: '/api/student-portrait/list',
          method: 'GET',
          data: params
        });
        
        if (res.statusCode === 200 && res.data) {
          // 后端直接返回 PageResultDto，不是包装在 R 对象中
          if (res.data.content !== undefined) {
            this.studentList = res.data.content || [];
            this.total = res.data.totalElements || 0;
            this.currentPage = (res.data.number || 0) + 1;
          } else if (res.data.code === 0 && res.data.data) {
            // 兼容包装在 R 对象中的情况
            this.studentList = res.data.data.content || [];
            this.total = res.data.data.totalElements || 0;
            this.currentPage = (res.data.data.number || 0) + 1;
          } else {
            console.error('响应数据格式错误:', res.data);
            uni.showToast({
              title: res.data.msg || '加载失败',
              icon: 'none'
            });
            this.studentList = [];
            this.total = 0;
          }
        } else {
          console.error('请求失败:', res);
          uni.showToast({
            title: '请求失败',
            icon: 'none'
          });
          this.studentList = [];
          this.total = 0;
        }
      } catch (error) {
        console.error('加载数据失败:', error);
        uni.showToast({
          title: '加载失败，请稍后重试',
          icon: 'none'
        });
        this.studentList = [];
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
      this.searchForm.studentName = '';
      this.currentPage = 1;
      this.loadData();
    },
    handleAdd() {
      uni.navigateTo({
        url: '/pages/student-portrait/add'
      });
    },
    handleEdit(student) {
      uni.navigateTo({
        url: `/pages/student-portrait/add?id=${student.id}`
      });
    },
    handleDelete(student) {
      uni.showModal({
        title: '确认删除',
        content: `确定要删除学生"${student.studentName}"吗？此操作不可恢复。`,
        confirmText: '删除',
        cancelText: '取消',
        success: async (res) => {
          if (res.confirm) {
            try {
              const deleteRes = await uniRequest({
                url: `/api/student-portrait/${student.id}`,
                method: 'DELETE'
              });
              
              if (deleteRes.statusCode === 200) {
                // 后端返回字符串 "删除成功" 或实体对象
                const successMsg = typeof deleteRes.data === 'string' ? deleteRes.data : '删除成功';
                uni.showToast({
                  title: successMsg,
                  icon: 'success'
                });
                this.loadData();
              } else {
                let errorMsg = '删除失败';
                if (deleteRes.data) {
                  if (typeof deleteRes.data === 'string') {
                    errorMsg = deleteRes.data;
                  } else if (deleteRes.data.msg) {
                    errorMsg = deleteRes.data.msg;
                  }
                }
                uni.showToast({
                  title: errorMsg,
                  icon: 'none'
                });
              }
            } catch (error) {
              console.error('删除失败:', error);
              uni.showToast({
                title: '删除失败，请稍后重试',
                icon: 'none'
              });
            }
          }
        }
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
    getAvatarUrl(student) {
      if (student.photoUrl) {
        if (student.photoUrl.startsWith('http://') || student.photoUrl.startsWith('https://')) {
          return student.photoUrl;
        }
        return student.photoUrl;
      }
      return '';
    },
    getInitial(name) {
      if (!name) return '?';
      return name.charAt(0).toUpperCase();
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
  background: linear-gradient(135deg, #3370FF, #1E6FFF);
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
  background: #3370FF;
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

.student-list {
  padding: 24rpx 32rpx;
}

.student-card {
  display: flex;
  align-items: center;
  gap: 24rpx;
  padding: 32rpx;
  margin-bottom: 24rpx;
  background: #FFFFFF;
  border-radius: 16rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.04);
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
  background: linear-gradient(135deg, #9B59B6, #8E44AD);
  color: #FFFFFF;
  font-size: 36rpx;
  font-weight: 600;
}

.card-content {
  flex: 1;
  min-width: 0;
}

.card-name {
  font-size: 32rpx;
  font-weight: 500;
  color: #1F2329;
  margin-bottom: 12rpx;
}

.card-desc {
  font-size: 26rpx;
  color: #646A73;
  line-height: 1.5;
}

.card-desc-empty {
  font-size: 26rpx;
  color: #86909C;
  font-style: italic;
}

.card-actions {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
  flex-shrink: 0;
}

.action-btn {
  padding: 12rpx 24rpx;
  border-radius: 6rpx;
  font-size: 24rpx;
  text-align: center;
}

.edit-btn {
  background: #E8F4FF;
  color: #3370FF;
}

.delete-btn {
  background: #FFF1F0;
  color: #F53F3F;
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
  color: #3370FF;
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

