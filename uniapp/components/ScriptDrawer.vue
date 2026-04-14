<template>
  <view class="script-drawer" :class="{ expanded: isExpanded }">
    <!-- 拖拽手柄 -->
    <view class="drawer-handle" @tap="toggleExpand">
      <view class="handle-bar"></view>
      <view class="handle-actions">
        <view class="handle-left">
          <text class="handle-title">逐字稿</text>
          <text v-if="hasCopied" class="copied-badge">已复制 ✓</text>
        </view>
        <text class="handle-toggle">{{ isExpanded ? '收起' : '展开' }}</text>
      </view>
    </view>

    <!-- 逐字稿内容区 -->
    <scroll-view 
      class="drawer-content" 
      scroll-y 
      :style="{ height: contentHeight }"
    >
      <view 
        id="scriptTextArea"
        class="script-text" 
        @touchend="onTextSelect"
        @mouseup="onTextSelect"
      >
        {{ cleanedContent }}
      </view>
    </scroll-view>

    <!-- 选中文本提示 -->
    <view v-if="selectedText" class="selection-bar">
      <text class="selection-text">已选中 {{ selectedText.length }} 字</text>
      <view class="selection-actions">
        <view class="selection-btn" @tap="copySelected">
          <text>复制选中</text>
        </view>
        <view class="selection-btn primary" @tap="ttsSelected">
          <text>🔊 朗读选中</text>
        </view>
      </view>
    </view>

    <!-- 底部操作栏 -->
    <view class="drawer-actions">
      <view v-if="!selectedText" class="action-tip">
        <text class="tip-text">💡 选中文本后可点击朗读</text>
      </view>
      <view class="action-btn tts-btn" @tap="handleTTS">
        <text class="action-icon">🔊</text>
        <text class="action-text">{{ isSpeaking ? '停止朗读' : 'AI跟读' }}</text>
      </view>
    </view>
  </view>
</template>

<script>
export default {
  name: 'ScriptDrawer',
  props: {
    // 逐字稿内容
    content: {
      type: String,
      default: ''
    },
    // 是否正在朗读
    isSpeaking: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      isExpanded: false,
      collapsedHeight: '200rpx',
      expandedHeight: '60vh',
      // 用户选中的文本
      selectedText: '',
      // 已自动复制的全部文本
      copiedText: '',
      // 是否已完成自动复制
      hasCopied: false
    };
  },
  watch: {
    // 监听内容变化，自动复制
    content: {
      handler(newVal) {
        if (newVal) {
          this.autoCopyContent();
        }
      },
      immediate: true
    }
  },
  computed: {
    contentHeight() {
      return this.isExpanded ? this.expandedHeight : this.collapsedHeight;
    },
    // 清理HTML标签的内容
    cleanedContent() {
      if (!this.content) return '暂无逐字稿内容';
      return this.content
        .replace(/<[^>]*>/g, '')
        .replace(/&nbsp;/g, ' ')
        .replace(/&lt;/g, '<')
        .replace(/&gt;/g, '>')
        .replace(/&amp;/g, '&')
        .replace(/&quot;/g, '"')
        .trim();
    }
  },
  methods: {
    toggleExpand() {
      this.isExpanded = !this.isExpanded;
      this.$emit('toggle', this.isExpanded);
    },
    
    /**
     * 自动复制全部内容（页面加载时自动执行）
     */
    autoCopyContent() {
      const text = this.cleanedContent;
      
      if (!text || text === '暂无逐字稿内容') {
        return;
      }
      
      // 保存到组件状态
      this.copiedText = text;
      
      // 复制到剪贴板
      // #ifdef H5
      if (navigator.clipboard) {
        navigator.clipboard.writeText(text).then(() => {
          this.hasCopied = true;
          console.log('[ScriptDrawer] 已自动复制全部内容');
        }).catch((err) => {
          console.log('[ScriptDrawer] 自动复制失败（需要用户交互）:', err);
          // 即使剪贴板API失败，copiedText仍然可用
          this.hasCopied = true;
        });
      } else {
        this.hasCopied = true;
      }
      // #endif
      
      // #ifndef H5
      uni.setClipboardData({
        data: text,
        showToast: false,
        success: () => {
          this.hasCopied = true;
          console.log('[ScriptDrawer] 已自动复制全部内容');
        },
        fail: () => {
          this.hasCopied = true;
        }
      });
      // #endif
      
      this.$emit('copy', text);
    },
    
    /**
     * 监听文本选择事件
     */
    onTextSelect() {
      // 延迟获取选中文本，确保选择已完成
      setTimeout(() => {
        this.getSelectedText();
      }, 100);
    },
    
    /**
     * 获取用户选中的文本
     */
    getSelectedText() {
      // #ifdef H5
      try {
        const selection = window.getSelection();
        if (selection && selection.toString().trim()) {
          this.selectedText = selection.toString().trim();
          console.log('[ScriptDrawer] 选中文本:', this.selectedText.substring(0, 30) + '...');
        } else {
          // 如果没有选中文本，不清除之前的选择（避免误触导致清除）
        }
      } catch (err) {
        console.error('[ScriptDrawer] 获取选中文本失败:', err);
      }
      // #endif
      
      // #ifndef H5
      // 小程序环境暂不支持获取选中文本
      // #endif
    },
    
    /**
     * 复制选中的文本
     */
    copySelected() {
      if (!this.selectedText) {
        uni.showToast({
          title: '请先选中文本',
          icon: 'none'
        });
        return;
      }
      
      this.copyToClipboard(this.selectedText, '选中内容已复制');
    },
    
    /**
     * 朗读选中的文本
     */
    ttsSelected() {
      if (!this.selectedText) {
        uni.showToast({
          title: '请先选中文本',
          icon: 'none'
        });
        return;
      }
      
      console.log('[ScriptDrawer] 朗读选中文本:', this.selectedText);
      this.$emit('tts', this.selectedText);
      
      // 清除选中状态
      this.clearSelection();
    },
    
    /**
     * AI跟读 - 优先朗读选中文本，否则朗读已复制的全部内容
     */
    handleTTS() {
      // 如果正在朗读，则停止
      if (this.isSpeaking) {
        this.$emit('tts', '');
        return;
      }
      
      // 优先使用选中的文本
      if (this.selectedText) {
        console.log('[ScriptDrawer] 朗读选中文本:', this.selectedText);
        this.$emit('tts', this.selectedText);
        this.clearSelection();
        return;
      }
      
      // 使用已复制的全部文本
      const text = this.copiedText || this.cleanedContent;
      
      if (!text || text === '暂无逐字稿内容') {
        uni.showToast({
          title: '暂无内容可朗读',
          icon: 'none'
        });
        return;
      }
      
      console.log('[ScriptDrawer] 朗读全部文本');
      this.$emit('tts', text);
    },
    
    /**
     * 复制文本到剪贴板
     */
    copyToClipboard(text, successMsg) {
      // #ifdef H5
      navigator.clipboard.writeText(text).then(() => {
        uni.showToast({
          title: successMsg,
          icon: 'success'
        });
      }).catch(() => {
        uni.showToast({
          title: '复制失败',
          icon: 'none'
        });
      });
      // #endif
      
      // #ifndef H5
      uni.setClipboardData({
        data: text,
        success: () => {
          uni.showToast({
            title: successMsg,
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
      // #endif
    },
    
    /**
     * 清除选中状态
     */
    clearSelection() {
      this.selectedText = '';
      
      // #ifdef H5
      try {
        window.getSelection()?.removeAllRanges();
      } catch (err) {
        // ignore
      }
      // #endif
    }
  }
};
</script>

<style scoped>
.script-drawer {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: #FFFFFF;
  border-radius: 32rpx 32rpx 0 0;
  box-shadow: 0 -4rpx 20rpx rgba(0, 0, 0, 0.1);
  z-index: 100;
  transition: all 0.3s ease;
}

/* 拖拽手柄 */
.drawer-handle {
  padding: 20rpx 32rpx;
  border-bottom: 1rpx solid #F0F0F0;
}

.handle-bar {
  width: 80rpx;
  height: 8rpx;
  background: #E5E5E5;
  border-radius: 4rpx;
  margin: 0 auto 16rpx;
}

.handle-actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.handle-left {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.handle-title {
  font-size: 30rpx;
  font-weight: 500;
  color: #333333;
}

.copied-badge {
  font-size: 22rpx;
  color: #52C41A;
  background: rgba(82, 196, 26, 0.1);
  padding: 4rpx 12rpx;
  border-radius: 8rpx;
}

.handle-toggle {
  font-size: 26rpx;
  color: #4A90D9;
}

/* 内容区 */
.drawer-content {
  padding: 24rpx 32rpx;
  transition: height 0.3s ease;
}

.script-text {
  font-size: 28rpx;
  color: #333333;
  line-height: 1.8;
  white-space: pre-wrap;
  word-break: break-word;
  user-select: text;
  -webkit-user-select: text;
}

/* 选中文本提示条 */
.selection-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16rpx 32rpx;
  background: #E8F4FD;
  border-top: 1rpx solid #D0E8FA;
}

.selection-text {
  font-size: 26rpx;
  color: #4A90D9;
}

.selection-actions {
  display: flex;
  gap: 16rpx;
}

.selection-btn {
  padding: 12rpx 24rpx;
  background: #FFFFFF;
  border-radius: 8rpx;
  font-size: 24rpx;
  color: #333333;
  border: 1rpx solid #E0E0E0;
}

.selection-btn.primary {
  background: #4A90D9;
  color: #FFFFFF;
  border-color: #4A90D9;
}

.selection-btn:active {
  opacity: 0.8;
}

/* 底部操作栏 */
.drawer-actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20rpx 32rpx;
  padding-bottom: calc(20rpx + env(safe-area-inset-bottom));
  border-top: 1rpx solid #F0F0F0;
  background: #FFFFFF;
}

.action-tip {
  flex: 1;
}

.tip-text {
  font-size: 24rpx;
  color: #999999;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 12rpx;
  padding: 20rpx 40rpx;
  border-radius: 12rpx;
  transition: background 0.2s;
}

.action-btn:active {
  background: #F5F7FA;
}

.tts-btn {
  background: #E8F4FD;
}

.action-icon {
  font-size: 32rpx;
}

.action-text {
  font-size: 28rpx;
  color: #333333;
}

/* 展开状态 */
.script-drawer.expanded {
  /* 展开时增加高度 */
}
</style>
