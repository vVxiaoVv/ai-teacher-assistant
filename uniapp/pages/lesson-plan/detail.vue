<template>
  <view class="page">
    <view v-if="loading" class="loading-container">
      <text>加载中...</text>
    </view>

    <view v-else-if="lessonPlan" class="detail-container">
      <view class="detail-header">
        <view class="header-title">{{ lessonPlan.title }}</view>
        <view class="header-id">ID: {{ lessonPlan.id }}</view>
      </view>

      <view v-if="viewMode === 'script'" class="script-section">
        <view class="section-title">逐字稿</view>
        <view v-if="script" class="script-content">{{ typeof script === 'string' ? script : (script.content || JSON.stringify(script)) }}</view>
        <view v-else-if="loading" class="loading-container">
          <text>加载逐字稿中...</text>
        </view>
        <view v-else class="error-container">
          <text>逐字稿不存在或加载失败</text>
        </view>
        <view v-if="script" class="script-actions">
          <view class="btn copy-btn" @tap="handleCopyScript">复制</view>
        </view>
      </view>

      <view v-else class="content-section">
        <view class="section-title">教案内容</view>
        <view class="content-text">{{ lessonPlan.content }}</view>
        <view class="content-actions">
          <view class="btn copy-btn" @tap="handleCopyContent">复制内容</view>
          <view
            v-if="!hasScript"
            class="btn generate-btn"
            :class="{ loading: generating }"
            @tap="handleGenerateScript"
          >
            {{ generating ? '生成中...' : '生成逐字稿' }}
          </view>
          <view
            v-else
            class="btn view-script-btn"
            @tap="handleViewScript"
          >
            查看逐字稿
          </view>
        </view>
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
      lessonPlan: null,
      script: null,
      hasScript: false,
      generating: false,
      viewMode: 'content' // 'content' or 'script'
    };
  },
  onLoad(options) {
    if (options.id) {
      const id = parseInt(options.id);
      if (isNaN(id)) {
        uni.showToast({
          title: '参数错误：ID无效',
          icon: 'none'
        });
        setTimeout(() => {
          uni.navigateBack();
        }, 1500);
        return;
      }
      this.viewMode = options.view === 'script' ? 'script' : 'content';
      
      // 先加载教案详情，然后根据模式加载相应内容
      this.loadLessonPlanDetail(id).then(() => {
        // 如果教案加载成功，再加载逐字稿或检查逐字稿状态
        if (this.lessonPlan) {
          if (this.viewMode === 'script') {
            // 如果是查看逐字稿模式，加载逐字稿
            this.loadScript(id);
          } else {
            // 否则检查逐字稿状态
            this.checkScriptStatus(id);
          }
        }
      });
    } else {
      uni.showToast({
        title: '参数错误：缺少ID',
        icon: 'none'
      });
      setTimeout(() => {
        uni.navigateBack();
      }, 1500);
    }
  },
  methods: {
    async loadLessonPlanDetail(id) {
      this.loading = true;
      try {
        const res = await uniRequest({
          url: `/api/lesson-plan/${id}`,
          method: 'GET'
        });
        
        if (res.statusCode === 200 && res.data) {
          // 后端直接返回 LessonPlan 对象，不是包装在 R 对象中
          if (res.data.id !== undefined) {
            this.lessonPlan = res.data;
          } else if (res.data.code === 0 && res.data.data) {
            // 兼容包装在 R 对象中的情况
            this.lessonPlan = res.data.data;
          } else {
            uni.showToast({
              title: res.data.msg || '加载失败',
              icon: 'none'
            });
            setTimeout(() => {
              uni.navigateBack();
            }, 1500);
          }
        } else if (res.statusCode === 404) {
          uni.showToast({
            title: '教案不存在',
            icon: 'none'
          });
          setTimeout(() => {
            uni.navigateBack();
          }, 1500);
        } else {
          uni.showToast({
            title: '加载失败',
            icon: 'none'
          });
          setTimeout(() => {
            uni.navigateBack();
          }, 1500);
        }
      } catch (error) {
        console.error('加载教案详情失败:', error);
        let errorMsg = '加载失败，请稍后重试';
        
        if (error.statusCode === 404) {
          errorMsg = '教案不存在';
        } else if (error.errMsg && error.errMsg.includes('request:fail')) {
          errorMsg = '网络请求失败，请检查网络连接';
        }
        
        uni.showToast({
          title: errorMsg,
          icon: 'none',
          duration: 3000
        });
        setTimeout(() => {
          uni.navigateBack();
        }, 1500);
      } finally {
        this.loading = false;
      }
    },
    async checkScriptStatus(id) {
      try {
        // 使用正确的接口：根据教案ID获取逐字稿
        const res = await uniRequest({
          url: `/api/script/lesson-plan/${id}`,
          method: 'GET'
        });
        // 后端直接返回 Script 对象，不是包装在 R 对象中
        if (res.statusCode === 200 && res.data) {
          if (res.data.content !== undefined || res.data.id !== undefined) {
            this.hasScript = true;
          } else {
            this.hasScript = false;
          }
        } else {
          this.hasScript = false;
        }
      } catch (error) {
        // 404 表示没有逐字稿，这是正常的
        if (error.statusCode === 404 || (error.errMsg && error.errMsg.includes('404'))) {
          this.hasScript = false;
        } else {
          console.error('检查逐字稿状态失败:', error);
          this.hasScript = false;
        }
      }
    },
    async loadScript(id) {
      // 设置单独的加载状态，避免与教案详情加载冲突
      const scriptLoading = !this.lessonPlan; // 如果教案还没加载，使用全局loading
      if (scriptLoading) {
        this.loading = true;
      }
      try {
        // 使用正确的接口：根据教案ID获取逐字稿
        const res = await uniRequest({
          url: `/api/script/lesson-plan/${id}`,
          method: 'GET'
        });
        // 后端直接返回 Script 对象，不是包装在 R 对象中
        if (res.statusCode === 200 && res.data) {
          // 直接使用返回的 Script 对象的 content 字段
          if (res.data.content !== undefined) {
            this.script = res.data.content;
          } else if (res.data.id !== undefined) {
            // 如果返回的是 Script 对象但没有 content，尝试使用整个对象
            this.script = res.data;
          } else {
            this.script = res.data;
          }
        } else if (res.statusCode === 404) {
          uni.showToast({
            title: '逐字稿不存在',
            icon: 'none'
          });
          this.script = null;
        } else {
          this.script = null;
        }
      } catch (error) {
        console.error('加载逐字稿失败:', error);
        let errorMsg = '加载逐字稿失败';
        
        if (error.statusCode === 404) {
          errorMsg = '逐字稿不存在';
        } else if (error.errMsg && error.errMsg.includes('request:fail')) {
          errorMsg = '网络请求失败，请检查网络连接';
        }
        
        uni.showToast({
          title: errorMsg,
          icon: 'none',
          duration: 3000
        });
        this.script = null;
      } finally {
        if (scriptLoading) {
          this.loading = false;
        }
      }
    },
    async handleGenerateScript() {
      this.generating = true;
      try {
        const res = await uniRequest({
          url: `/api/script/generate/${this.lessonPlan.id}`,
          method: 'POST'
        });
        
        // 后端直接返回 Script 对象，不是包装在 R 对象中
        if (res.statusCode === 200 && res.data) {
          if (res.data.content !== undefined || res.data.id !== undefined) {
            uni.showToast({
              title: '生成成功',
              icon: 'success'
            });
            this.hasScript = true;
            await this.loadScript(this.lessonPlan.id);
            this.viewMode = 'script';
          } else if (typeof res.data === 'string') {
            uni.showToast({
              title: res.data || '生成失败',
              icon: 'none'
            });
          } else {
            uni.showToast({
              title: '生成成功',
              icon: 'success'
            });
            this.hasScript = true;
            await this.loadScript(this.lessonPlan.id);
            this.viewMode = 'script';
          }
        } else {
          uni.showToast({
            title: res.data?.msg || '生成失败',
            icon: 'none'
          });
        }
      } catch (error) {
        console.error('生成逐字稿失败:', error);
        uni.showToast({
          title: '生成失败，请稍后重试',
          icon: 'none'
        });
      } finally {
        this.generating = false;
      }
    },
    handleViewScript() {
      this.viewMode = 'script';
      this.loadScript(this.lessonPlan.id);
    },
    async handleCopyContent() {
      const text = this.lessonPlan.content || '';
      try {
        // #ifdef H5
        await navigator.clipboard.writeText(text);
        uni.showToast({
          title: '复制成功',
          icon: 'success'
        });
        // #endif
        // #ifndef H5
        uni.setClipboardData({
          data: text,
          success: () => {
            uni.showToast({
              title: '复制成功',
              icon: 'success'
            });
          }
        });
        // #endif
      } catch (error) {
        uni.showToast({
          title: '复制失败',
          icon: 'none'
        });
      }
    },
    async handleCopyScript() {
      const text = typeof this.script === 'string' ? this.script : (this.script.content || '');
      try {
        // #ifdef H5
        await navigator.clipboard.writeText(text);
        uni.showToast({
          title: '复制成功',
          icon: 'success'
        });
        // #endif
        // #ifndef H5
        uni.setClipboardData({
          data: text,
          success: () => {
            uni.showToast({
              title: '复制成功',
              icon: 'success'
            });
          }
        });
        // #endif
      } catch (error) {
        uni.showToast({
          title: '复制失败',
          icon: 'none'
        });
      }
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
  margin-bottom: 32rpx;
  padding-bottom: 24rpx;
  border-bottom: 1rpx solid #E5E5E5;
}

.header-title {
  font-size: 36rpx;
  font-weight: 600;
  color: #1F2329;
  margin-bottom: 8rpx;
}

.header-id {
  font-size: 24rpx;
  color: #86909C;
}

.section-title {
  font-size: 32rpx;
  font-weight: 500;
  color: #1F2329;
  margin-bottom: 24rpx;
}

.content-section,
.script-section {
  margin-top: 32rpx;
}

.content-text,
.script-content {
  font-size: 28rpx;
  color: #1F2329;
  line-height: 1.8;
  white-space: pre-wrap;
  word-break: break-word;
  padding: 24rpx;
  background: #F7F8FA;
  border-radius: 8rpx;
  margin-bottom: 32rpx;
  max-height: 800rpx;
  overflow-y: auto;
}

.content-actions,
.script-actions {
  display: flex;
  gap: 24rpx;
}

.btn {
  flex: 1;
  padding: 24rpx;
  border-radius: 8rpx;
  text-align: center;
  font-size: 32rpx;
  font-weight: 500;
}

.copy-btn {
  background: #F7F8FA;
  color: #646A73;
}

.generate-btn {
  background: linear-gradient(135deg, #52C41A, #389E0D);
  color: #FFFFFF;
}

.generate-btn.loading {
  opacity: 0.6;
}

.view-script-btn {
  background: linear-gradient(135deg, #3370FF, #1E6FFF);
  color: #FFFFFF;
}
</style>


