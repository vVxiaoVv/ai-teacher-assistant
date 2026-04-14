<template>
  <view class="page">
    <view class="content">
      <view class="header">
        <text class="title">{{ getItemTitle(item) }}</text>
        <text class="time">{{ formatTime(item.createTime) }}</text>
      </view>
      <view class="message-container">
        <text class="message">{{ formatHtmlContent(item.formattedMessage || item.rawMessage || '暂无内容') }}</text>
      </view>
      <button class="copy-btn" @tap="copyToClipboard">复制内容到粘贴板</button>
    </view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      item: {}
    };
  },
  onLoad(options) {
    // 从临时存储中读取历史项数据
    try {
      const storedItem = uni.getStorageSync('history_detail_item');
      if (storedItem) {
        this.item = storedItem;
        // 读取后清除临时存储
        uni.removeStorageSync('history_detail_item');
      } else {
        // 兼容旧方式：从URL参数读取（向后兼容）
        if (options.item) {
          try {
            this.item = JSON.parse(decodeURIComponent(options.item));
          } catch (parseError) {
            console.error('解析URL参数失败:', parseError);
            throw new Error('数据格式错误');
          }
        } else {
          throw new Error('未找到数据');
        }
      }
    } catch (e) {
      console.error('加载数据失败:', e);
      // 确保清除可能残留的临时存储
      try {
        uni.removeStorageSync('history_detail_item');
      } catch (clearError) {
        // 忽略清除错误
      }
      uni.showToast({
        title: '数据加载失败',
        icon: 'none'
      });
      // 延迟返回上一页
      setTimeout(() => {
        uni.navigateBack();
      }, 1500);
    }
  },
  onUnload() {
    // 页面卸载时清除临时存储，防止数据残留
    try {
      uni.removeStorageSync('history_detail_item');
    } catch (e) {
      // 忽略清除错误
    }
  },
  methods: {
    getItemTitle(item) {
      if (!item) return '视频分析';
      
      // 从URL中提取文件名（去除扩展名）
      let videoName = '视频分析';
      if (item.videoUrl) {
        try {
          // 提取URL末尾的文件名
          const urlParts = item.videoUrl.split('/');
          let fileName = urlParts[urlParts.length - 1];
          
          // 去除查询参数（如果有）
          if (fileName.includes('?')) {
            fileName = fileName.split('?')[0];
          }
          
          // 去除扩展名
          if (fileName.includes('.')) {
            fileName = fileName.substring(0, fileName.lastIndexOf('.'));
          }
          
          // 如果文件名不为空，使用文件名；否则使用默认名称
          if (fileName && fileName.trim()) {
            videoName = decodeURIComponent(fileName);
          }
        } catch (e) {
          console.error('解析URL失败:', e);
        }
      }
      
      // 格式化时间：年月日时分
      const timeStr = this.formatDateTime(item.createTime);
      
      // 组合标题：视频名称 + 年月日时分
      return `${videoName} ${timeStr}`;
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
    },
    formatTime(timeStr) {
      if (!timeStr) return '';
      try {
        const date = new Date(timeStr);
        return date.toLocaleString('zh-CN', {
          year: 'numeric',
          month: '2-digit',
          day: '2-digit',
          hour: '2-digit',
          minute: '2-digit',
          second: '2-digit'
        });
      } catch (e) {
        return timeStr;
      }
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
    },
    copyToClipboard() {
      const text = this.item.formattedMessage || this.item.rawMessage || '';
      if (!text) {
        uni.showToast({
          title: '没有可复制的内容',
          icon: 'none'
        });
        return;
      }
      
      // 复制时也进行格式化处理
      const formattedText = this.formatHtmlContent(text);
      
      uni.setClipboardData({
        data: formattedText,
        success: () => {
          uni.showToast({
            title: '复制成功',
            icon: 'success'
          });
        },
        fail: () => {
          uni.showToast({
            title: '复制失败',
            icon: 'none'
          });
        }
      });
    }
  }
};
</script>

<style>
.page {
  min-height: 100vh;
  background-color: #f5f5f5;
}

.content {
  padding: 24rpx;
}

.header {
  background-color: #ffffff;
  border-radius: 20rpx;
  padding: 28rpx 24rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.06);
}

.title {
  font-size: 32rpx;
  font-weight: 600;
  color: #111827;
  display: block;
  margin-bottom: 12rpx;
}

.time {
  font-size: 24rpx;
  color: #9ca3af;
}

.message-container {
  background-color: #ffffff;
  border-radius: 20rpx;
  padding: 28rpx 24rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.06);
  min-height: 400rpx;
}

.message {
  font-size: 28rpx;
  color: #4b5563;
  line-height: 1.8;
  white-space: pre-wrap;
  word-break: break-word;
}

.copy-btn {
  width: 100%;
  padding: 20rpx;
  background-color: #007aff;
  color: #ffffff;
  border-radius: 12rpx;
  font-size: 32rpx;
  font-weight: 500;
}
</style>