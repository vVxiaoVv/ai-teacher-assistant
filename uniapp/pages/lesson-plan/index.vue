<template>
  <view class="page">
    <!-- 顶部标题栏 -->
    <view class="header">
      <view class="header-title">教案列表</view>
      <view class="add-btn" @tap="handleUpload">
        <text class="add-icon">+</text>
        <text>上传教案</text>
      </view>
    </view>

    <!-- 搜索栏 -->
    <view class="search-section">
      <view class="search-input-wrapper" :class="{ focused: searchFocused }">
        <input
          class="search-input"
          v-model="searchForm.title"
          placeholder="请输入教案标题"
          @focus="searchFocused = true"
          @blur="searchFocused = false"
          @confirm="handleSearch"
        />
        <view class="search-btn" @tap="handleSearch">搜索</view>
      </view>
      <view class="reset-btn" @tap="handleReset">重置</view>
    </view>

    <!-- 加载状态 -->
    <view v-if="loading" class="loading-container">
      <view class="loading-spinner"></view>
      <text class="loading-text">加载中...</text>
    </view>

    <!-- 教案列表 -->
    <view v-else-if="lessonPlanList.length > 0" class="lesson-plan-list">
      <view
        v-for="lessonPlan in lessonPlanList"
        :key="lessonPlan.id"
        class="lesson-plan-card"
        @tap="handleViewDetail(lessonPlan)"
      >
        <view class="card-header">
          <view class="card-title">{{ lessonPlan.title }}</view>
        </view>
        <view class="card-content">
          <view class="info-item">
            <text class="info-value">{{ formatTime(lessonPlan.uploadTime) }}</text>
          </view>
          <view v-if="lessonPlan.classroomName" class="info-item">
            <text class="info-value">{{ lessonPlan.classroomName }}</text>
          </view>
          <view v-if="lessonPlan.contentSummary" class="card-desc">
            {{ lessonPlan.contentSummary }}
          </view>
        </view>
        <view class="card-actions">
          <view class="action-btn detail-btn" @tap.stop="handleViewDetail(lessonPlan)">查看详情</view>
          <view
            v-if="!lessonPlan.hasScript"
            class="action-btn generate-btn"
            :class="{ loading: generatingScript[lessonPlan.id] }"
            @tap.stop="handleGenerateScript(lessonPlan)"
          >
            {{ generatingScript[lessonPlan.id] ? '生成中...' : '生成逐字稿' }}
          </view>
          <view
            v-else
            class="action-btn script-btn"
            @tap.stop="handleViewScript(lessonPlan)"
          >
            查看逐字稿
          </view>
        </view>
      </view>
    </view>

    <!-- 空状态 -->
    <view v-else class="empty-container">
      <text class="empty-text">暂无教案数据</text>
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
      lessonPlanList: [],
      total: 0,
      currentPage: 1,
      pageSize: 10,
      generatingScript: {},
      searchFocused: false,
      searchForm: {
        title: ''
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
    // 从上传页返回时刷新数据
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
        
        if (this.searchForm.title && this.searchForm.title.trim()) {
          params.title = this.searchForm.title.trim();
        }
        
        const res = await uniRequest({
          url: '/api/lesson-plan/list',
          method: 'GET',
          data: params
        });
        
        if (res.statusCode === 200 && res.data) {
          // 后端直接返回 PageResultDto，不是包装在 R 对象中
          if (res.data.content !== undefined) {
            this.lessonPlanList = res.data.content || [];
            this.total = res.data.totalElements || 0;
            this.currentPage = (res.data.number || 0) + 1;
            
            // 检查每个教案是否有逐字稿
            for (const item of this.lessonPlanList) {
              await this.checkScriptStatus(item);
            }
          } else if (res.data.code === 0 && res.data.data) {
            // 兼容包装在 R 对象中的情况
            this.lessonPlanList = res.data.data.content || [];
            this.total = res.data.data.totalElements || 0;
            this.currentPage = (res.data.data.number || 0) + 1;
            
            // 检查每个教案是否有逐字稿
            for (const item of this.lessonPlanList) {
              await this.checkScriptStatus(item);
            }
          } else {
            console.error('响应数据格式错误:', res.data);
            uni.showToast({
              title: res.data.msg || '加载失败',
              icon: 'none'
            });
            this.lessonPlanList = [];
            this.total = 0;
          }
        } else {
          console.error('请求失败:', res);
          uni.showToast({
            title: '请求失败',
            icon: 'none'
          });
          this.lessonPlanList = [];
          this.total = 0;
        }
      } catch (error) {
        console.error('加载数据失败:', error);
        let errorMsg = '加载失败，请稍后重试';
        
        // 检查是否是连接错误
        if (error.errMsg && error.errMsg.includes('request:fail')) {
          if (error.errMsg.includes('ERR_CONNECTION_REFUSED') || error.errMsg.includes('连接失败')) {
            errorMsg = '无法连接到服务器，请检查后端服务是否运行';
          } else if (error.errMsg.includes('timeout')) {
            errorMsg = '请求超时，请检查网络连接';
          } else {
            errorMsg = '网络请求失败，请检查网络连接';
          }
        } else if (error.statusCode) {
          if (error.statusCode === 404) {
            errorMsg = '接口不存在';
          } else if (error.statusCode === 500) {
            errorMsg = '服务器错误';
          } else {
            errorMsg = `请求失败 (${error.statusCode})`;
          }
        }
        
        uni.showToast({
          title: errorMsg,
          icon: 'none',
          duration: 3000
        });
        this.lessonPlanList = [];
        this.total = 0;
      } finally {
        this.loading = false;
      }
    },
    async checkScriptStatus(lessonPlan) {
      try {
        // 使用正确的接口：根据教案ID获取逐字稿
        const res = await uniRequest({
          url: `/api/script/lesson-plan/${lessonPlan.id}`,
          method: 'GET'
        });
        // 后端直接返回 Script 对象，不是包装在 R 对象中
        if (res.statusCode === 200 && res.data) {
          // 如果有 content 字段，说明有逐字稿
          if (res.data.content !== undefined || res.data.id !== undefined) {
            this.$set(lessonPlan, 'hasScript', true);
            this.$set(lessonPlan, 'scriptContent', res.data.content);
          } else {
            this.$set(lessonPlan, 'hasScript', false);
            this.$set(lessonPlan, 'scriptContent', null);
          }
        } else {
          this.$set(lessonPlan, 'hasScript', false);
          this.$set(lessonPlan, 'scriptContent', null);
        }
      } catch (error) {
        // 404 表示没有逐字稿，这是正常的
        if (error.statusCode === 404 || (error.response && error.response.status === 404)) {
          this.$set(lessonPlan, 'hasScript', false);
          this.$set(lessonPlan, 'scriptContent', null);
        } else {
          console.error('检查逐字稿状态失败:', error);
          this.$set(lessonPlan, 'hasScript', false);
          this.$set(lessonPlan, 'scriptContent', null);
        }
      }
    },
    handleSearch() {
      this.currentPage = 1;
      this.loadData();
    },
    handleReset() {
      this.searchForm.title = '';
      this.currentPage = 1;
      this.loadData();
    },
    handleUpload() {
      uni.navigateTo({
        url: '/pages/lesson-plan/upload'
      });
    },
    handleViewDetail(lessonPlan) {
      uni.navigateTo({
        url: `/pages/lesson-plan/detail?id=${lessonPlan.id}`,
        fail: (err) => {
          console.error('跳转失败:', err);
          uni.showToast({
            title: '跳转失败，请重试',
            icon: 'none'
          });
        }
      });
    },
    async handleGenerateScript(lessonPlan) {
      this.$set(this.generatingScript, lessonPlan.id, true);
      try {
        uni.showToast({
          title: '正在生成逐字稿，请耐心等待...',
          icon: 'loading',
          duration: 2000
        });
        
        const res = await uniRequest({
          url: `/api/script/generate/${lessonPlan.id}`,
          method: 'POST',
          timeout: 300000 // 5分钟超时（AI生成可能需要较长时间）
        });
        
        // 后端直接返回 Script 对象，不是包装在 R 对象中
        if (res.statusCode === 200 && res.data) {
          // 检查返回的数据是否是 Script 对象
          if (res.data.content !== undefined || res.data.id !== undefined) {
            uni.showToast({
              title: '生成成功',
              icon: 'success'
            });
            // 重新检查逐字稿状态，获取最新的内容
            await this.checkScriptStatus(lessonPlan);
          } else if (typeof res.data === 'string') {
            // 如果返回的是错误消息字符串
            uni.showToast({
              title: res.data || '生成失败',
              icon: 'none',
              duration: 3000
            });
          } else {
            uni.showToast({
              title: '生成成功',
              icon: 'success'
            });
            await this.checkScriptStatus(lessonPlan);
          }
        } else {
          uni.showToast({
            title: typeof res.data === 'string' ? res.data : (res.data?.msg || '生成失败'),
            icon: 'none',
            duration: 3000
          });
        }
      } catch (error) {
        console.error('生成逐字稿失败:', error);
        let errorMsg = '生成失败，请稍后重试';
        
        if (error.errMsg) {
          if (error.errMsg.includes('timeout') || error.errMsg.includes('超时')) {
            errorMsg = '生成超时，AI服务响应时间较长，请稍后查看结果';
          } else if (error.errMsg.includes('request:fail')) {
            errorMsg = '网络请求失败，请检查网络连接';
          } else if (error.errMsg.includes('canceled')) {
            errorMsg = '请求被取消，请重试';
          }
        } else if (error.statusCode === 400) {
          errorMsg = error.data || '请求参数错误';
        } else if (error.statusCode === 500) {
          errorMsg = '服务器错误，请稍后重试';
        }
        
        uni.showToast({
          title: errorMsg,
          icon: 'none',
          duration: 3000
        });
      } finally {
        this.$set(this.generatingScript, lessonPlan.id, false);
      }
    },
    handleViewScript(lessonPlan) {
      uni.navigateTo({
        url: `/pages/lesson-plan/detail?id=${lessonPlan.id}&view=script`,
        fail: (err) => {
          console.error('跳转失败:', err);
          uni.showToast({
            title: '跳转失败，请重试',
            icon: 'none'
          });
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
    getSummary(text) {
      if (!text) return '';
      if (text.length > 100) {
        return text.substring(0, 100) + '...';
      }
      return text;
    }
  }
};
</script>

<style scoped>
/* 极简高级设计规范 - 教案列表页面 */

/* 色彩变量 */
/* 主色：#E63946（核心紫红色） */
/* 辅助色：#F5F7FA（浅灰，仅用于背景分层） */
/* 基底色：#FFFFFF（纯白） */
/* 文本色：#333333（深灰，主文本）、#666666（中灰，辅助文本） */
/* 警示色：#E53935（低饱和红，仅用于错误提示） */

.page {
  min-height: 100vh;
  background: #FFFFFF;
  padding: 40rpx;
  padding-top: calc(8vh + 40rpx);
  padding-bottom: 40rpx;
}

/* 顶部标题栏 */
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 120rpx;
  margin-bottom: 64rpx;
  background: transparent;
}

.header-title {
  font-size: 40rpx;
  font-weight: 600;
  color: #E63946;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', sans-serif;
}

.add-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8rpx;
  height: 76rpx;
  padding: 0 24rpx;
  background: #E63946;
  color: #FFFFFF;
  border-radius: 24rpx;
  font-size: 28rpx;
  font-weight: 500;
  transition: transform 0.2s;
}

.add-btn:active {
  transform: scale(0.98);
}

.add-icon {
  font-size: 32rpx;
  font-weight: 600;
  line-height: 1;
}

/* 搜索栏 */
.search-section {
  display: flex;
  align-items: center;
  gap: 24rpx;
  margin-bottom: 64rpx;
  width: 85%;
  margin-left: auto;
  margin-right: auto;
}

.search-input-wrapper {
  flex: 1;
  position: relative;
  display: flex;
  align-items: center;
  height: 84rpx;
  padding: 0 24rpx;
  background: #FFFFFF;
  border-radius: 24rpx;
  transition: border-bottom 0.3s;
}

.search-input-wrapper::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  width: 0;
  height: 4rpx;
  background: #E63946;
  transition: width 0.3s;
}

.search-input-wrapper.focused::after {
  width: 100%;
}

.search-input {
  flex: 1;
  height: 84rpx;
  font-size: 30rpx;
  color: #333333;
  background: transparent;
}

.search-input::placeholder {
  color: #666666;
  font-size: 28rpx;
}

.search-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 84rpx;
  padding: 0 32rpx;
  background: #E63946;
  color: #FFFFFF;
  border-radius: 24rpx;
  font-size: 28rpx;
  font-weight: 500;
  margin-left: 16rpx;
  transition: transform 0.2s;
}

.search-btn:active {
  transform: scale(0.98);
}

.reset-btn {
  padding: 0 16rpx;
  color: #E63946;
  font-size: 26rpx;
  text-decoration: none;
  transition: text-decoration 0.2s;
}

.reset-btn:active {
  text-decoration: underline;
}

/* 加载状态 */
.loading-container {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  padding: 200rpx 0;
  min-height: 40vh;
}

.loading-spinner {
  width: 48rpx;
  height: 48rpx;
  border: 4rpx solid #F5F7FA;
  border-top-color: #E63946;
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
  color: #666666;
  font-size: 28rpx;
}

/* 教案列表 */
.lesson-plan-list {
  width: 90%;
  margin: 0 auto;
}

.lesson-plan-card {
  padding: 40rpx;
  margin-bottom: 32rpx;
  background: #FFFFFF;
  border-radius: 24rpx;
  box-shadow: 0 6rpx 16rpx rgba(230, 57, 70, 0.05);
  border: 2rpx solid transparent;
  transition: border-color 0.3s, transform 0.2s;
}

.lesson-plan-card:active {
  transform: scale(0.98);
  border-color: #E63946;
}

.card-header {
  margin-bottom: 24rpx;
}

.card-title {
  font-size: 36rpx;
  font-weight: 500;
  color: #333333;
  line-height: 1.5;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', sans-serif;
}

.card-content {
  margin-bottom: 32rpx;
}

.info-item {
  margin-bottom: 12rpx;
  font-size: 24rpx;
  color: #666666;
  line-height: 1.5;
}

.info-value {
  color: #666666;
  font-size: 24rpx;
}

.card-desc {
  margin-top: 16rpx;
  font-size: 30rpx;
  color: #333333;
  line-height: 1.5;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', sans-serif;
}

/* 操作按钮 */
.card-actions {
  display: flex;
  gap: 32rpx;
}

.action-btn {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  height: 92rpx;
  border-radius: 24rpx;
  font-size: 30rpx;
  font-weight: 500;
  text-align: center;
  transition: transform 0.2s, opacity 0.2s;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', sans-serif;
}

.action-btn:active {
  transform: scale(0.98);
}

.detail-btn {
  background: #FFFFFF;
  color: #E63946;
  border: 2rpx solid #E63946;
}

.generate-btn {
  background: #E63946;
  color: #FFFFFF;
}

.generate-btn.loading {
  opacity: 0.7;
  pointer-events: none;
}

.script-btn {
  background: #E63946;
  color: #FFFFFF;
}

/* 空状态 */
.empty-container {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 40vh 0;
  min-height: 50vh;
}

.empty-text {
  color: #666666;
  font-size: 28rpx;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', sans-serif;
}

/* 分页 */
.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 64rpx;
  padding: 64rpx 0;
  margin-top: 32rpx;
}

.page-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 92rpx;
  padding: 0 32rpx;
  background: #FFFFFF;
  color: #E63946;
  border: 2rpx solid #E63946;
  border-radius: 24rpx;
  font-size: 28rpx;
  font-weight: 500;
  transition: transform 0.2s, background 0.2s, color 0.2s, border-color 0.2s;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', sans-serif;
}

.page-btn:active {
  transform: scale(0.98);
}

.page-btn.disabled {
  background: #F5F7FA;
  color: #666666;
  border-color: #F5F7FA;
  pointer-events: none;
}

.page-info {
  color: #666666;
  font-size: 28rpx;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', sans-serif;
}
</style>


