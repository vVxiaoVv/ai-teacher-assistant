<template>
  <view class="container">
    <view class="section">
      <view class="section-title">上传试讲视频</view>
      <view class="tips">支持从相册选择或现场录制，建议横屏录制，画面清晰、声音清楚。</view>
      <view class="button-group">
        <button 
          type="default" 
          :loading="uploading" 
          :disabled="uploading" 
          @tap="chooseVideoFromAlbum"
          class="action-btn"
        >
          {{ uploading ? '正在上传...' : '选择视频' }}
        </button>
        <button 
          type="primary" 
          :loading="uploading" 
          :disabled="uploading" 
          @tap="recordVideo"
          class="action-btn record-btn"
        >
          {{ uploading ? '正在上传...' : '现场录制' }}
        </button>
      </view>
      <view v-if="fileName" class="file-name">已选择：{{ fileName }}</view>
    </view>

    <view class="section">
      <view class="section-title">或输入视频URL</view>
      <view class="tips">输入视频文件的URL地址进行分析</view>
      <input 
        class="url-input" 
        v-model="videoUrl" 
        placeholder="请输入视频URL地址"
        type="text"
      />
      <button 
        type="primary" 
        :loading="analyzing" 
        :disabled="analyzing || !videoUrl" 
        @tap="analyzeByUrl"
        style="margin-top: 20rpx;"
      >
        {{ analyzing ? '分析中...' : '开始分析' }}
      </button>
    </view>

    <view class="section" v-if="taskId">
      <view class="section-title">任务状态：{{ statusText }}</view>
      <view class="status-row">
        <view class="status-dot" :class="'status-' + (status || 'unknown')"></view>
        <text class="status-text">
          {{ statusHint }}
        </text>
      </view>
      <button size="mini" @tap="refreshResult">手动刷新</button>
    </view>

    <view class="section" v-if="result">
      <view class="section-title">分析结果</view>

      <view class="result-block">
        <text class="result-label">总体概述：</text>
        <text>{{ result.summary }}</text>
      </view>

      <view class="result-block">
        <text class="result-label">课堂结构建议：</text>
        <text>{{ result.structureAdvice }}</text>
      </view>

      <view class="result-block">
        <text class="result-label">互动性建议：</text>
        <text>{{ result.interactionAdvice }}</text>
      </view>

      <view class="result-block">
        <text class="result-label">语言表达建议：</text>
        <text>{{ result.languageAdvice }}</text>
      </view>
    </view>

    <view class="section" v-if="analysisResult">
      <view class="section-title">URL分析结果</view>
      <view class="result-block">
        <text class="result-label">分析内容：</text>
        <text class="result-content">{{ formatHtmlContent(analysisResult.formattedMessage || analysisResult.rawMessage || '暂无内容') }}</text>
      </view>
      <view class="result-block" v-if="analysisResult.rawMessage && analysisResult.rawMessage !== analysisResult.formattedMessage">
        <text class="result-label">原始内容：</text>
        <text class="result-content">{{ formatHtmlContent(analysisResult.rawMessage) }}</text>
      </view>
    </view>
  </view>
</template>

<script>
import { API_BASE_URL, uniRequest } from '../../common/request.js';

export default {
  data() {
    return {
      filePath: '',
      fileName: '',
      taskId: '',
      status: '',
      result: null,
      uploading: false,
      autoPollTimer: null,
      videoUrl: '',
      analyzing: false,
      analysisResult: null
    };
  },
  computed: {
    statusText() {
      if (this.status === 'PROCESSING') return '分析中';
      if (this.status === 'DONE') return '完成';
      if (this.status === 'FAILED') return '失败';
      return this.status || '未知';
    },
    statusHint() {
      if (this.status === 'PROCESSING') {
        return '系统正在分析您的试讲视频，请保持网络连接，结果通常在数十秒内生成。';
      }
      if (this.status === 'DONE') {
        return '分析已完成，您可以查看下方的详细指导建议。';
      }
      if (this.status === 'FAILED') {
        return '分析失败，请稍后重试或检查网络情况。';
      }
      return '已创建任务，等待分析开始。';
    }
  },
  onLoad(options) {
    // 如果从历史记录页面跳转过来，自动加载指定任务的结果
    if (options.taskId) {
      this.taskId = options.taskId;
      this.loadResult();
    }
  },
  onUnload() {
    this.clearAutoPoll();
  },
  methods: {
    chooseVideoFromAlbum() {
      uni.chooseVideo({
        sourceType: ['album'],
        success: (res) => {
          this.filePath = res.tempFilePath;
          this.fileName = '试讲视频';
          this.uploadVideo();
        },
        fail: () => {
          uni.showToast({ title: '选择视频失败', icon: 'none' });
        }
      });
    },
    recordVideo() {
      uni.chooseVideo({
        sourceType: ['camera'],
        maxDuration: 1800, // 最长30分钟
        camera: 'back', // 使用后置摄像头
        success: (res) => {
          this.filePath = res.tempFilePath;
          this.fileName = '现场录制视频';
          this.uploadVideo();
        },
        fail: (err) => {
          console.error('录制视频失败:', err);
          if (err.errMsg && err.errMsg.includes('cancel')) {
            // 用户取消录制，不显示错误提示
            return;
          }
          uni.showToast({ title: '录制视频失败', icon: 'none' });
        }
      });
    },
    uploadVideo() {
      if (!this.filePath) return;

      this.uploading = true;
      uni.showLoading({ title: '上传中...' });
      uni.uploadFile({
        url: API_BASE_URL + '/api/video-correction/upload',
        filePath: this.filePath,
        name: 'file',
        formData: {
          title: '试讲视频'
        },
        success: (uploadFileRes) => {
          uni.hideLoading();
          this.uploading = false;
          try {
            const response = JSON.parse(uploadFileRes.data);
            // 后端返回格式: {code: 0, msg: "success", data: {taskId: "...", status: "..."}}
            if (response.code === 0 && response.data) {
              this.taskId = response.data.taskId;
              this.status = response.data.status;
              uni.showToast({ title: '上传成功，开始分析', icon: 'success' });
              this.startAutoPoll();
            } else {
              uni.showToast({ title: response.msg || '上传失败', icon: 'none' });
            }
          } catch (e) {
            console.error('解析返回失败:', e);
            uni.showToast({ title: '解析返回失败', icon: 'none' });
          }
        },
        fail: () => {
          uni.hideLoading();
          this.uploading = false;
          uni.showToast({ title: '上传失败', icon: 'none' });
        }
      });
    },
    loadResult() {
      if (!this.taskId) return;
      uni.showLoading({ title: '加载中...' });
      uni.request({
        url: API_BASE_URL + '/api/video-correction/result/' + this.taskId,
        method: 'GET',
        success: (res) => {
          uni.hideLoading();
          // 后端返回格式: {code: 0, msg: "success", data: {...}}
          if (res.statusCode === 200 && res.data && res.data.code === 0 && res.data.data) {
            this.result = res.data.data;
            this.status = res.data.data.status;
            if (this.status === 'PROCESSING') {
              this.startAutoPoll();
            }
          } else {
            uni.showToast({ title: res.data?.msg || '加载失败', icon: 'none' });
          }
        },
        fail: () => {
          uni.hideLoading();
          uni.showToast({ title: '加载失败', icon: 'none' });
        }
      });
    },
    refreshResult() {
      if (!this.taskId) return;

      uniRequest({
        url: API_BASE_URL + '/api/video-correction/result/' + this.taskId,
        method: 'GET',
        success: (res) => {
          // 后端返回格式: {code: 0, msg: "success", data: {...}}
          if (res.statusCode === 200 && res.data && res.data.code === 0 && res.data.data) {
            this.result = res.data.data;
            this.status = res.data.data.status;
            if (this.status === 'DONE' || this.status === 'FAILED') {
              this.clearAutoPoll();
            }
          } else {
            console.error('获取结果失败:', res.data);
            if (this.status !== 'PROCESSING') {
              uni.showToast({ title: res.data?.msg || '获取结果失败', icon: 'none' });
            }
          }
        },
        fail: (err) => {
          console.error('请求失败:', err);
          if (this.status !== 'PROCESSING') {
            uni.showToast({ title: '获取结果失败', icon: 'none' });
          }
        }
      });
    },
    startAutoPoll() {
      this.clearAutoPoll();
      this.autoPollTimer = setInterval(() => {
        if (this.status === 'DONE' || this.status === 'FAILED') {
          this.clearAutoPoll();
          return;
        }
        this.refreshResult();
      }, 5000);
    },
    clearAutoPoll() {
      if (this.autoPollTimer) {
        clearInterval(this.autoPollTimer);
        this.autoPollTimer = null;
      }
    },
    analyzeByUrl() {
      if (!this.videoUrl || !this.videoUrl.trim()) {
        uni.showToast({
          title: '请输入视频URL',
          icon: 'none'
        });
        return;
      }

      this.analyzing = true;
      uni.showLoading({ title: '分析中...' });
      
      uniRequest({
        url: '/api/video-analysis/analyze',
        method: 'POST',
        header: {
          'Content-Type': 'application/json'
        },
        data: {
          videoUrl: this.videoUrl.trim()
        },
        timeout: 300000 // 5分钟超时
      }).then((res) => {
        uni.hideLoading();
        this.analyzing = false;
        
        console.log('视频分析接口响应:', JSON.stringify(res, null, 2));
        
        if (res.statusCode === 200 && res.data) {
          // 后端返回格式: {code: 0, msg: "success", data: {formattedMessage: "...", rawMessage: "..."}}
          if (res.data.code === 0 && res.data.data) {
            const analysisData = res.data.data;
            console.log('分析结果数据:', JSON.stringify(analysisData, null, 2));
            
            // 使用 $set 确保 Vue 响应式更新
            this.$set(this, 'analysisResult', analysisData);
            
            // 强制更新视图
            this.$forceUpdate();
            
            console.log('analysisResult 已设置:', this.analysisResult);
            console.log('formattedMessage:', this.analysisResult?.formattedMessage);
            
            uni.showToast({
              title: '分析完成',
              icon: 'success'
            });
          } else {
            console.error('接口返回错误:', res.data);
            uni.showToast({
              title: res.data.msg || '分析失败',
              icon: 'none'
            });
          }
        } else {
          console.error('响应格式错误:', res);
          uni.showToast({
            title: res.data?.msg || '分析失败',
            icon: 'none'
          });
        }
      }).catch((err) => {
        uni.hideLoading();
        this.analyzing = false;
        console.error('请求失败:', err);
        uni.showToast({
          title: '网络错误，请重试',
          icon: 'none'
        });
      });
    },
    formatHtmlContent(text) {
      if (!text) return '暂无内容';
      
      // 将 HTML 转义字符转换为可显示的内容
      let formatted = String(text);
      
      // 将 <br>、<br/>、<br /> 转换为换行符
      formatted = formatted.replace(/<br\s*\/?>/gi, '\n');
      
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
    }
  }
};
</script>

<style>
.container {
  padding: 40rpx;
}

.section {
  margin-bottom: 40rpx;
}

.section-title {
  font-size: 32rpx;
  font-weight: 600;
  margin-bottom: 20rpx;
}

.tips {
  font-size: 24rpx;
  color: #888;
  margin-bottom: 20rpx;
}

.file-name {
  margin-top: 20rpx;
  font-size: 26rpx;
  color: #666666;
}

.status-row {
  flex-direction: row;
  display: flex;
  align-items: center;
  margin-bottom: 16rpx;
}

.status-dot {
  width: 16rpx;
  height: 16rpx;
  border-radius: 50%;
  margin-right: 12rpx;
}

.status-PROCESSING {
  background-color: #facc15;
}

.status-DONE {
  background-color: #22c55e;
}

.status-FAILED {
  background-color: #ef4444;
}

.status-unknown {
  background-color: #9ca3af;
}

.status-text {
  font-size: 24rpx;
  color: #666;
}

.result-block {
  margin-bottom: 20rpx;
  font-size: 28rpx;
  line-height: 1.6;
}

.result-label {
  font-weight: bold;
  margin-right: 10rpx;
}

.url-input {
  width: 100%;
  padding: 20rpx;
  border: 2rpx solid #e5e7eb;
  border-radius: 12rpx;
  font-size: 28rpx;
  margin-bottom: 20rpx;
  background-color: #ffffff;
}

.button-group {
  display: flex;
  gap: 20rpx;
  margin-bottom: 20rpx;
}

.action-btn {
  flex: 1;
}

.record-btn {
  background: linear-gradient(135deg, #FF6B6B, #EE5A5A);
  border: none;
}

.record-btn:active {
  opacity: 0.8;
}

.result-content {
  white-space: pre-wrap;
  word-break: break-word;
  line-height: 1.8;
}
</style>


