<template>
  <view class="page">
    <view class="page-bg-layer"></view>
    
    <!-- 调试按钮 -->
    <view class="debug-bar" style="position: fixed; top: 50px; left: 10px; right: 10px; z-index: 9999; display: flex; gap: 10px;">
      <button size="mini" type="primary" @click="debugTest">调试测试</button>
      <button size="mini" type="warn" @click="debugConnect">测试连接</button>
    </view>
    
    <!-- 头部区域 -->
    <view class="header">
      <view class="header-content">
        <view class="title">语音识别</view>
        <view class="subtitle">实时语音转文字</view>
      </view>
    </view>
    
    <!-- 状态指示 -->
    <view class="status-bar">
      <view class="status-indicator" :class="statusClass">
        <view class="status-dot"></view>
        <text class="status-text">{{ statusText }}</text>
      </view>
    </view>
    
    <!-- 识别结果区域 -->
    <view class="result-container">
      <view class="result-card">
        <view class="result-header">
          <text class="result-title">识别结果</text>
          <view v-if="finalResult" class="copy-btn" @tap="copyResult">
            <text>复制</text>
          </view>
        </view>
        
        <scroll-view class="result-content" scroll-y>
          <!-- 最终结果 -->
          <view v-if="finalResult" class="final-result">
            <text>{{ finalResult }}</text>
          </view>
          
          <!-- 中间结果（实时显示） -->
          <view v-if="intermediateResult" class="intermediate-result">
            <text>{{ intermediateResult }}</text>
            <view class="typing-indicator">
              <view class="dot"></view>
              <view class="dot"></view>
              <view class="dot"></view>
            </view>
          </view>
          
          <!-- 空状态 -->
          <view v-if="!finalResult && !intermediateResult" class="empty-state">
            <text class="empty-icon">🎙️</text>
            <text class="empty-text">按住下方按钮开始说话</text>
          </view>
        </scroll-view>
      </view>
    </view>
    
    <!-- 历史记录 -->
    <view v-if="historyList.length > 0" class="history-container">
      <view class="history-header">
        <text class="history-title">历史记录</text>
        <view class="clear-btn" @tap="clearHistory">
          <text>清空</text>
        </view>
      </view>
      <scroll-view class="history-list" scroll-y>
        <view 
          v-for="(item, index) in historyList" 
          :key="index" 
          class="history-item"
          @tap="copyText(item.text)"
        >
          <text class="history-text">{{ item.text }}</text>
          <text class="history-time">{{ item.time }}</text>
        </view>
      </scroll-view>
    </view>
    
    <!-- 录音按钮区域 -->
    <view class="record-container">
      <view 
        class="record-btn"
        :class="{ 'recording': isRecording, 'connecting': isConnecting }"
        @touchstart.prevent="onTouchStart"
        @touchend.prevent="onTouchEnd"
        @touchcancel.prevent="onTouchEnd"
        @mousedown.prevent="onTouchStart"
        @mouseup.prevent="onTouchEnd"
        @mouseleave="onTouchEnd"
        @tap="onTap"
      >
        <view class="record-btn-inner">
          <view v-if="isConnecting" class="loading-icon">
            <view class="spinner"></view>
          </view>
          <text v-else class="record-icon">{{ isRecording ? '🔴' : '🎤' }}</text>
        </view>
        <view class="ripple" v-if="isRecording"></view>
        <view class="ripple delay" v-if="isRecording"></view>
      </view>
      <text class="record-tip">{{ recordTip }}</text>
    </view>
  </view>
</template>

<script>
import { SpeechWebSocket } from '../../common/speechWebSocket.js';

export default {
  data() {
    return {
      // 状态
      status: 'idle', // idle, connecting, connected, recording, recognizing
      isRecording: false,
      isConnecting: false,
      
      // 识别结果
      finalResult: '',
      intermediateResult: '',
      
      // 历史记录
      historyList: [],
      
      // WebSocket
      speechWS: null,
      
      // 录音管理器
      recorderManager: null,
      
      // H5 录音相关
      h5AudioContext: null,
      h5MediaStream: null,
      h5ScriptProcessor: null,
      h5OnStartCallback: null,
      h5OnStopCallback: null,
      h5OnFrameCallback: null,
      h5OnErrorCallback: null,
    };
  },
  
  computed: {
    statusClass() {
      return {
        'status-idle': this.status === 'idle',
        'status-connecting': this.status === 'connecting',
        'status-connected': this.status === 'connected',
        'status-recording': this.status === 'recording',
        'status-recognizing': this.status === 'recognizing',
      };
    },
    
    statusText() {
      const statusMap = {
        'idle': '等待连接',
        'connecting': '正在连接...',
        'connected': '已连接，可以开始说话',
        'recording': '正在录音...',
        'recognizing': '正在识别...',
      };
      return statusMap[this.status] || '未知状态';
    },
    
    recordTip() {
      if (this.isConnecting) {
        return '正在连接语音服务...';
      }
      if (this.isRecording) {
        return '点击或松开结束录音';
      }
      return '按住或点击说话';
    }
  },
  
  onLoad() {
    console.log('[Speech] ========================================');
    console.log('[Speech] 语音识别页面 onLoad 被调用');
    console.log('[Speech] ========================================');
    this.initRecorderManager();
    this.loadHistory();
  },
  
  onShow() {
    console.log('[Speech] 页面 onShow 被调用');
  },
  
  onReady() {
    console.log('[Speech] 页面 onReady 被调用');
  },
  
  onUnload() {
    console.log('[Speech] 页面 onUnload 被调用');
    this.cleanup();
  },
  
  methods: {
    /**
     * 调试测试 - 验证按钮点击事件是否正常
     */
    debugTest() {
      console.log('[Speech] ========================================');
      console.log('[Speech] debugTest 按钮被点击!');
      console.log('[Speech] 当前状态:', this.status);
      console.log('[Speech] isRecording:', this.isRecording);
      console.log('[Speech] isConnecting:', this.isConnecting);
      console.log('[Speech] speechWS:', this.speechWS);
      console.log('[Speech] recorderManager:', this.recorderManager);
      console.log('[Speech] ========================================');
      
      uni.showToast({
        title: '调试测试成功!',
        icon: 'success'
      });
    },
    
    /**
     * 调试连接 - 测试 WebSocket 连接
     */
    async debugConnect() {
      console.log('[Speech] ========================================');
      console.log('[Speech] debugConnect 按钮被点击!');
      console.log('[Speech] 开始测试 WebSocket 连接...');
      console.log('[Speech] ========================================');
      
      uni.showLoading({ title: '连接中...' });
      
      try {
        await this.initWebSocket();
        console.log('[Speech] WebSocket 连接成功!');
        uni.hideLoading();
        uni.showToast({
          title: '连接成功!',
          icon: 'success'
        });
      } catch (err) {
        console.error('[Speech] WebSocket 连接失败:', err);
        uni.hideLoading();
        uni.showToast({
          title: '连接失败: ' + (err.message || err),
          icon: 'none'
        });
      }
    },
    
    /**
     * 初始化录音管理器
     */
    initRecorderManager() {
      console.log('[Speech] initRecorderManager 开始初始化');
      
      // #ifdef H5
      // H5 环境使用 Web Audio API
      this.initH5Recorder();
      // #endif
      
      // #ifndef H5
      // 小程序环境使用 uni.getRecorderManager
      this.recorderManager = uni.getRecorderManager();
      console.log('[Speech] recorderManager 创建完成:', this.recorderManager);
      // #endif
      
      // 注册录音回调（H5 和小程序通用）
      // 录音开始
      this.recorderManager.onStart(() => {
        console.log('[Speech] 录音开始回调触发');
        this.isRecording = true;
        this.status = 'recording';
      });
      
      // 录音帧数据回调（用于实时发送音频）
      this.recorderManager.onFrameRecorded((res) => {
        const { frameBuffer } = res;
        console.log('[Speech] onFrameRecorded, buffer size:', frameBuffer ? frameBuffer.byteLength : 0);
        if (this.speechWS && frameBuffer) {
          this.speechWS.sendAudioData(frameBuffer);
        }
      });
      
      // 录音停止
      this.recorderManager.onStop((res) => {
        console.log('[Speech] 录音停止回调触发', res);
        this.isRecording = false;
        this.status = 'recognizing';
        
        // 发送停止识别指令（只在这里发送，避免重复）
        // 检查状态，确保只在录音状态下发送停止指令
        if (this.speechWS && (this.status === 'recording' || this.status === 'recognizing')) {
          this.speechWS.stopRecognition();
        }
      });
      
      // 录音错误
      this.recorderManager.onError((err) => {
        console.error('[Speech] 录音错误', err);
        this.isRecording = false;
        this.status = 'connected';
        uni.showToast({
          title: '录音失败: ' + err.errMsg,
          icon: 'none'
        });
      });
    },
    
    /**
     * H5 环境初始化录音器
     */
    initH5Recorder() {
      console.log('[Speech] 初始化 H5 录音器');
      this.h5AudioContext = null;
      this.h5MediaStream = null;
      this.h5ScriptProcessor = null;
      
      // 创建模拟的 recorderManager 对象
      this.recorderManager = {
        start: (options) => {
          console.log('[Speech] H5 录音开始', options);
          this.startH5Recording(options);
        },
        stop: () => {
          console.log('[Speech] H5 录音停止');
          this.stopH5Recording();
        },
        onStart: (callback) => {
          this.h5OnStartCallback = callback;
        },
        onStop: (callback) => {
          this.h5OnStopCallback = callback;
        },
        onFrameRecorded: (callback) => {
          this.h5OnFrameCallback = callback;
        },
        onError: (callback) => {
          this.h5OnErrorCallback = callback;
        }
      };
    },
    
    /**
     * H5 开始录音
     */
    async startH5Recording(options) {
      try {
        const stream = await navigator.mediaDevices.getUserMedia({ 
          audio: {
            sampleRate: options.sampleRate || 16000,
            channelCount: 1,
            echoCancellation: true,
            noiseSuppression: true
          } 
        });
        
        this.h5MediaStream = stream;
        this.h5AudioContext = new (window.AudioContext || window.webkitAudioContext)({
          sampleRate: options.sampleRate || 16000
        });
        
        const source = this.h5AudioContext.createMediaStreamSource(stream);
        
        // 使用 ScriptProcessorNode 获取 PCM 数据
        const bufferSize = 4096;
        this.h5ScriptProcessor = this.h5AudioContext.createScriptProcessor(bufferSize, 1, 1);
        
        this.h5ScriptProcessor.onaudioprocess = (e) => {
          if (!this.isRecording) return;
          
          const inputData = e.inputBuffer.getChannelData(0);
          // 转换为 16-bit PCM
          const pcmData = new Int16Array(inputData.length);
          for (let i = 0; i < inputData.length; i++) {
            const s = Math.max(-1, Math.min(1, inputData[i]));
            pcmData[i] = s < 0 ? s * 0x8000 : s * 0x7FFF;
          }
          
          // 调用帧回调
          if (this.h5OnFrameCallback) {
            this.h5OnFrameCallback({ frameBuffer: pcmData.buffer });
          }
        };
        
        source.connect(this.h5ScriptProcessor);
        this.h5ScriptProcessor.connect(this.h5AudioContext.destination);
        
        this.isRecording = true;
        this.status = 'recording';
        
        // 触发开始回调
        if (this.h5OnStartCallback) {
          this.h5OnStartCallback();
        }
        
        console.log('[Speech] H5 录音已启动');
      } catch (err) {
        console.error('[Speech] H5 录音启动失败:', err);
        if (this.h5OnErrorCallback) {
          this.h5OnErrorCallback({ errMsg: err.message });
        }
      }
    },
    
    /**
     * H5 停止录音
     */
    stopH5Recording() {
      if (this.h5ScriptProcessor) {
        this.h5ScriptProcessor.disconnect();
        this.h5ScriptProcessor = null;
      }
      
      if (this.h5AudioContext) {
        this.h5AudioContext.close();
        this.h5AudioContext = null;
      }
      
      if (this.h5MediaStream) {
        this.h5MediaStream.getTracks().forEach(track => track.stop());
        this.h5MediaStream = null;
      }
      
      this.isRecording = false;
      this.status = 'recognizing';
      
      // 触发停止回调（回调中会处理停止识别指令，避免重复调用）
      if (this.h5OnStopCallback) {
        this.h5OnStopCallback({ tempFilePath: '' });
      }
      
      console.log('[Speech] H5 录音已停止');
    },
    
    /**
     * 初始化 WebSocket 连接
     */
    async initWebSocket() {
      console.log('[Speech] initWebSocket 开始');
      
      this.speechWS = new SpeechWebSocket({
        onConnected: (data) => {
          console.log('[Speech] onConnected 回调触发:', data);
          this.status = 'connected';
          this.isConnecting = false;
        },
        
        onDisconnected: (data) => {
          console.log('[Speech] onDisconnected 回调触发:', data);
          this.status = 'idle';
        },
        
        onRecognitionStarted: (data) => {
          console.log('[Speech] onRecognitionStarted 回调触发:', data);
          this.status = 'recording';
        },
        
        onRecognitionResult: (data) => {
          console.log('[Speech] onRecognitionResult 回调触发:', data);
          if (data.result) {
            // 累积中间结果，而不是替换
            // 如果当前中间结果不为空，追加新结果；否则直接设置
            if (this.intermediateResult && this.intermediateResult.trim()) {
              // 检查新结果是否是当前结果的扩展（避免重复）
              if (!data.result.includes(this.intermediateResult)) {
                this.intermediateResult = this.intermediateResult + ' ' + data.result;
              } else {
                // 如果新结果包含旧结果，说明是更新，直接替换
                this.intermediateResult = data.result;
              }
            } else {
              this.intermediateResult = data.result;
            }
            console.log('[Speech] 更新中间结果:', this.intermediateResult);
          }
        },
        
        onRecognitionCompleted: (data) => {
          console.log('[Speech] onRecognitionCompleted 回调触发:', data);
          this.status = 'connected';
          
          // 优先使用累积的中间结果，如果最终结果不为空且更完整则使用最终结果
          let completedResult = '';
          if (data.result) {
            // 如果有累积的中间结果，比较两者，选择更完整的
            if (this.intermediateResult && this.intermediateResult.trim()) {
              // 如果中间结果包含最终结果，使用中间结果；否则合并两者
              if (this.intermediateResult.includes(data.result)) {
                completedResult = this.intermediateResult;
              } else if (data.result.includes(this.intermediateResult)) {
                completedResult = data.result;
              } else {
                // 两者不同，合并（中间结果在前）
                completedResult = this.intermediateResult + ' ' + data.result;
              }
            } else {
              completedResult = data.result;
            }
          } else if (this.intermediateResult && this.intermediateResult.trim()) {
            // 如果最终结果为空，使用中间结果
            completedResult = this.intermediateResult;
          }
          
          if (completedResult) {
            // 将结果合并到最终结果
            this.finalResult = this.finalResult 
              ? this.finalResult + ' ' + completedResult 
              : completedResult;
            this.intermediateResult = '';
            
            // 添加到历史记录
            this.addToHistory(completedResult);
            console.log('[Speech] 识别完成，最终结果:', completedResult);
          }
        },
        
        onError: (err) => {
          console.error('[Speech] onError 回调触发:', err);
          
          // 如果是任务状态错误（重复操作），不显示错误提示
          if (err.message && err.message.includes('TASK_STATE_ERROR')) {
            console.warn('[Speech] 任务状态错误（可能是重复操作），已忽略');
            return;
          }
          
          this.isConnecting = false;
          this.isRecording = false;
          this.status = 'idle';
          
          uni.showToast({
            title: err.message || '语音服务错误',
            icon: 'none'
          });
        }
      });
      
      try {
        console.log('[Speech] 准备调用 speechWS.connect()');
        this.isConnecting = true;
        this.status = 'connecting';
        await this.speechWS.connect();
        console.log('[Speech] speechWS.connect() 完成，状态:', this.status);
      } catch (err) {
        console.error('[Speech] speechWS.connect() 失败:', err);
        this.isConnecting = false;
        this.status = 'idle';
        throw err;
      }
    },
    
    /**
     * 开始录音
     */
    async startRecord() {
      console.log('[Speech] startRecord 被调用, 当前状态:', this.status);
      
      try {
        // 检查录音权限
        console.log('[Speech] 检查录音权限...');
        const authSetting = await this.checkRecordAuth();
        if (!authSetting) {
          console.log('[Speech] 录音权限检查未通过');
          return;
        }
        console.log('[Speech] 录音权限检查通过');
        
        // 如果未连接，先连接
        if (!this.speechWS || this.status === 'idle') {
          console.log('[Speech] 开始初始化 WebSocket 连接...');
          uni.showToast({
            title: '正在连接服务...',
            icon: 'loading',
            duration: 10000
          });
          
          try {
            await this.initWebSocket();
            console.log('[Speech] WebSocket 连接成功, 当前状态:', this.status);
            uni.hideToast();
          } catch (wsErr) {
            console.error('[Speech] WebSocket 连接失败:', wsErr);
            uni.showToast({
              title: '连接服务失败，请重试',
              icon: 'none'
            });
            return;
          }
        }
        
        // 等待连接就绪
        if (this.status !== 'connected') {
          console.log('[Speech] 连接未就绪, 状态:', this.status);
          uni.showToast({
            title: '正在连接，请稍候...',
            icon: 'none'
          });
          return;
        }
        
        console.log('[Speech] 开始录音流程...');
        
        // 清空中间结果
        this.intermediateResult = '';
        
        // 发送开始识别指令
        console.log('[Speech] 发送开始识别指令');
        this.speechWS.startRecognition();
        
        // 开始录音
        console.log('[Speech] 启动录音管理器');
        this.recorderManager.start({
          format: 'PCM',
          sampleRate: 16000,
          numberOfChannels: 1,
          encodeBitRate: 48000,
          frameSize: 1  // 每1KB触发一次onFrameRecorded
        });
        
      } catch (err) {
        console.error('[Speech] 开始录音失败:', err);
        uni.showToast({
          title: '开始录音失败: ' + (err.message || err),
          icon: 'none'
        });
      }
    },
    
    /**
     * 触摸/鼠标按下事件
     */
    onTouchStart(e) {
      console.log('[Speech] onTouchStart 事件触发');
      this.startRecord();
    },
    
    /**
     * 触摸/鼠标抬起事件
     */
    onTouchEnd(e) {
      console.log('[Speech] onTouchEnd 事件触发');
      this.stopRecord();
    },
    
    /**
     * 点击事件（兼容某些平台）
     */
    onTap(e) {
      console.log('[Speech] onTap 事件触发, isRecording:', this.isRecording);
      // 如果是点击模式，切换录音状态
      if (!this.isRecording) {
        this.startRecord();
      } else {
        this.stopRecord();
      }
    },
    
    /**
     * 停止录音
     */
    stopRecord() {
      console.log('[Speech] stopRecord 被调用, isRecording:', this.isRecording);
      if (this.isRecording) {
        this.recorderManager.stop();
      }
    },
    
    /**
     * 检查录音权限
     */
    checkRecordAuth() {
      return new Promise((resolve) => {
        // #ifdef H5
        // H5 环境使用浏览器 API 请求录音权限
        if (navigator.mediaDevices && navigator.mediaDevices.getUserMedia) {
          navigator.mediaDevices.getUserMedia({ audio: true })
            .then((stream) => {
              // 获取权限成功，停止流
              stream.getTracks().forEach(track => track.stop());
              resolve(true);
            })
            .catch((err) => {
              console.error('[Speech] 录音权限请求失败:', err);
              uni.showToast({
                title: '需要录音权限',
                icon: 'none'
              });
              resolve(false);
            });
        } else {
          // 浏览器不支持
          uni.showToast({
            title: '浏览器不支持录音功能',
            icon: 'none'
          });
          resolve(false);
        }
        // #endif
        
        // #ifndef H5
        // 小程序环境使用 uni.getSetting
        uni.getSetting({
          success: (res) => {
            if (res.authSetting['scope.record'] === false) {
              // 用户之前拒绝过，引导重新授权
              uni.showModal({
                title: '提示',
                content: '需要录音权限才能使用语音识别功能',
                confirmText: '去设置',
                success: (modalRes) => {
                  if (modalRes.confirm) {
                    uni.openSetting();
                  }
                }
              });
              resolve(false);
            } else if (res.authSetting['scope.record'] === undefined) {
              // 首次使用，触发授权弹窗
              uni.authorize({
                scope: 'scope.record',
                success: () => {
                  resolve(true);
                },
                fail: () => {
                  uni.showToast({
                    title: '需要录音权限',
                    icon: 'none'
                  });
                  resolve(false);
                }
              });
            } else {
              resolve(true);
            }
          }
        });
        // #endif
      });
    },
    
    /**
     * 添加到历史记录
     */
    addToHistory(text) {
      const now = new Date();
      const timeStr = `${now.getHours().toString().padStart(2, '0')}:${now.getMinutes().toString().padStart(2, '0')}`;
      
      this.historyList.unshift({
        text: text,
        time: timeStr
      });
      
      // 最多保留20条
      if (this.historyList.length > 20) {
        this.historyList.pop();
      }
      
      // 保存到本地存储
      this.saveHistory();
    },
    
    /**
     * 保存历史记录
     */
    saveHistory() {
      uni.setStorageSync('speechHistory', this.historyList);
    },
    
    /**
     * 加载历史记录
     */
    loadHistory() {
      const history = uni.getStorageSync('speechHistory');
      if (history && Array.isArray(history)) {
        this.historyList = history;
      }
    },
    
    /**
     * 清空历史记录
     */
    clearHistory() {
      uni.showModal({
        title: '提示',
        content: '确定要清空历史记录吗？',
        success: (res) => {
          if (res.confirm) {
            this.historyList = [];
            uni.removeStorageSync('speechHistory');
          }
        }
      });
    },
    
    /**
     * 复制文本
     */
    copyText(text) {
      uni.setClipboardData({
        data: text,
        success: () => {
          uni.showToast({
            title: '已复制',
            icon: 'success'
          });
        }
      });
    },
    
    /**
     * 复制识别结果
     */
    copyResult() {
      if (this.finalResult) {
        this.copyText(this.finalResult);
      }
    },
    
    /**
     * 清理资源
     */
    cleanup() {
      if (this.speechWS) {
        this.speechWS.close();
        this.speechWS = null;
      }
      if (this.recorderManager) {
        this.recorderManager.stop();
      }
    }
  }
};
</script>

<style>
.page {
  min-height: 100vh;
  background: linear-gradient(180deg, #667eea 0%, #764ba2 100%);
  position: relative;
  display: flex;
  flex-direction: column;
}

.page-bg-layer {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(180deg, rgba(102, 126, 234, 0.9) 0%, rgba(118, 75, 162, 0.9) 100%);
  pointer-events: none;
  z-index: 0;
}

/* 头部 */
.header {
  padding: 40rpx 32rpx 20rpx;
  position: relative;
  z-index: 1;
}

.header-content {
  color: #ffffff;
}

.title {
  font-size: 44rpx;
  font-weight: 600;
  margin-bottom: 8rpx;
}

.subtitle {
  font-size: 28rpx;
  opacity: 0.8;
}

/* 状态栏 */
.status-bar {
  padding: 0 32rpx 20rpx;
  position: relative;
  z-index: 1;
}

.status-indicator {
  display: inline-flex;
  align-items: center;
  padding: 12rpx 24rpx;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 30rpx;
  backdrop-filter: blur(10rpx);
}

.status-dot {
  width: 16rpx;
  height: 16rpx;
  border-radius: 50%;
  margin-right: 12rpx;
  background: #ccc;
}

.status-idle .status-dot {
  background: #999;
}

.status-connecting .status-dot {
  background: #f0ad4e;
  animation: blink 1s infinite;
}

.status-connected .status-dot {
  background: #5cb85c;
}

.status-recording .status-dot {
  background: #d9534f;
  animation: pulse 0.5s infinite;
}

.status-recognizing .status-dot {
  background: #5bc0de;
  animation: blink 0.5s infinite;
}

.status-text {
  font-size: 26rpx;
  color: #ffffff;
}

@keyframes blink {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.3; }
}

@keyframes pulse {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.3); }
}

/* 结果区域 */
.result-container {
  flex: 1;
  padding: 0 32rpx;
  position: relative;
  z-index: 1;
}

.result-card {
  background: #ffffff;
  border-radius: 24rpx;
  padding: 32rpx;
  min-height: 300rpx;
  max-height: 400rpx;
  display: flex;
  flex-direction: column;
  box-shadow: 0 8rpx 32rpx rgba(0, 0, 0, 0.1);
}

.result-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
  padding-bottom: 16rpx;
  border-bottom: 1rpx solid #f0f0f0;
}

.result-title {
  font-size: 32rpx;
  font-weight: 600;
  color: #333;
}

.copy-btn {
  padding: 8rpx 24rpx;
  background: #667eea;
  border-radius: 20rpx;
}

.copy-btn text {
  font-size: 24rpx;
  color: #ffffff;
}

.result-content {
  flex: 1;
  overflow: hidden;
}

.final-result {
  font-size: 32rpx;
  color: #333;
  line-height: 1.6;
  word-break: break-all;
}

.intermediate-result {
  display: flex;
  align-items: center;
  font-size: 32rpx;
  color: #999;
  line-height: 1.6;
}

.typing-indicator {
  display: flex;
  align-items: center;
  margin-left: 10rpx;
}

.typing-indicator .dot {
  width: 12rpx;
  height: 12rpx;
  border-radius: 50%;
  background: #667eea;
  margin: 0 4rpx;
  animation: typing 1.4s infinite ease-in-out both;
}

.typing-indicator .dot:nth-child(1) {
  animation-delay: -0.32s;
}

.typing-indicator .dot:nth-child(2) {
  animation-delay: -0.16s;
}

@keyframes typing {
  0%, 80%, 100% {
    transform: scale(0);
  }
  40% {
    transform: scale(1);
  }
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 200rpx;
}

.empty-icon {
  font-size: 80rpx;
  margin-bottom: 20rpx;
}

.empty-text {
  font-size: 28rpx;
  color: #999;
}

/* 历史记录 */
.history-container {
  padding: 20rpx 32rpx;
  position: relative;
  z-index: 1;
}

.history-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16rpx;
}

.history-title {
  font-size: 28rpx;
  color: rgba(255, 255, 255, 0.8);
}

.clear-btn text {
  font-size: 24rpx;
  color: rgba(255, 255, 255, 0.6);
}

.history-list {
  max-height: 200rpx;
}

.history-item {
  background: rgba(255, 255, 255, 0.15);
  border-radius: 12rpx;
  padding: 16rpx 20rpx;
  margin-bottom: 12rpx;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.history-text {
  font-size: 26rpx;
  color: #ffffff;
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  margin-right: 20rpx;
}

.history-time {
  font-size: 22rpx;
  color: rgba(255, 255, 255, 0.6);
  flex-shrink: 0;
}

/* 录音按钮区域 */
.record-container {
  padding: 40rpx 32rpx 60rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  position: relative;
  z-index: 1;
}

.record-btn {
  width: 160rpx;
  height: 160rpx;
  border-radius: 50%;
  background: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  box-shadow: 0 8rpx 32rpx rgba(0, 0, 0, 0.2);
  transition: transform 0.2s;
}

.record-btn:active {
  transform: scale(0.95);
}

.record-btn.recording {
  background: #ff4757;
}

.record-btn.connecting {
  background: #f0f0f0;
}

.record-btn-inner {
  display: flex;
  align-items: center;
  justify-content: center;
}

.record-icon {
  font-size: 64rpx;
}

.loading-icon {
  width: 60rpx;
  height: 60rpx;
}

.spinner {
  width: 100%;
  height: 100%;
  border: 6rpx solid #e0e0e0;
  border-top: 6rpx solid #667eea;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.ripple {
  position: absolute;
  width: 100%;
  height: 100%;
  border-radius: 50%;
  border: 4rpx solid rgba(255, 255, 255, 0.5);
  animation: ripple 1.5s infinite ease-out;
}

.ripple.delay {
  animation-delay: 0.75s;
}

@keyframes ripple {
  0% {
    transform: scale(1);
    opacity: 1;
  }
  100% {
    transform: scale(2);
    opacity: 0;
  }
}

.record-tip {
  margin-top: 24rpx;
  font-size: 28rpx;
  color: rgba(255, 255, 255, 0.8);
}
</style>

