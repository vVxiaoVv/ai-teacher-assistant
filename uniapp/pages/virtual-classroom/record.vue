<template>
  <view class="page">
    <!-- 顶部状态栏 -->
    <view class="status-bar">
      <view class="back-btn" @tap="goBack">
        <text class="back-icon">‹</text>
        <text class="back-text">返回</text>
      </view>
      <view class="status-info">
        <view v-if="cameraInitializing" class="camera-status loading">
          <text>正在启动摄像头...</text>
        </view>
        <view v-else-if="!cameraReady" class="camera-status waiting">
          <text>等待启动摄像头</text>
        </view>
        <view v-else-if="isRecording" class="camera-status recording">
          <view class="recording-dot"></view>
          <text>录制中 {{ formattedDuration }}</text>
        </view>
        <view v-else class="camera-status ready">
          <text>摄像头就绪</text>
        </view>
      </view>
    </view>

    <!-- 摄像头预览区域 -->
    <view class="camera-section">
      <!-- H5 视频预览容器 -->
      <!-- #ifdef H5 -->
      <view id="videoContainer" class="camera-preview-container"></view>
      <!-- #endif -->
      
      <!-- 小程序相机组件 -->
      <!-- #ifndef H5 -->
      <camera
        id="myCamera"
        class="camera-preview"
        :device-position="facingMode === 'user' ? 'front' : 'back'"
        flash="off"
        @error="onCameraError"
      ></camera>
      <!-- #endif -->
      
      <!-- 摄像头未就绪时的占位 -->
      <view v-if="!cameraReady" class="camera-placeholder">
        <view class="placeholder-icon">📷</view>
        <text v-if="cameraInitializing" class="placeholder-text">正在启动摄像头...</text>
        <text v-else class="placeholder-text">点击下方按钮启动摄像头</text>
        <text v-if="cameraError" class="placeholder-error">{{ cameraError }}</text>
        <view v-if="!cameraInitializing" class="start-camera-btn" @tap="requestCameraPermission">
          <text class="start-camera-icon">📹</text>
          <text class="start-camera-text">启动摄像头</text>
        </view>
        <text class="placeholder-tip">需要摄像头和麦克风权限</text>
      </view>
      
      <!-- 开始录制按钮（左上角） -->
      <view 
        v-if="!isRecording && cameraReady" 
        class="start-recording-btn-top" 
        @tap="startRecording"
      >
        <view class="btn-icon-wrapper-top start">
          <text class="btn-icon-top">●</text>
        </view>
        <text class="btn-text-top">开始录制</text>
      </view>
      
      <!-- 录制中的控制按钮（左上角） -->
      <view v-if="isRecording" class="recording-controls-top">
        <view 
          class="recording-btn-top pause-btn-top" 
          @tap="togglePause"
        >
          <view class="btn-icon-wrapper-top" :class="isPaused ? 'resume' : 'pause'">
            <text class="btn-icon-top">{{ isPaused ? '▶' : '⏸' }}</text>
          </view>
          <text class="btn-text-top">{{ isPaused ? '继续' : '暂停' }}</text>
        </view>
        
        <view 
          class="recording-btn-top stop-btn-top" 
          @tap="stopRecording"
        >
          <view class="btn-icon-wrapper-top stop">
            <text class="btn-icon-top">■</text>
          </view>
          <text class="btn-text-top">结束</text>
        </view>
      </view>
      
      <!-- 切换摄像头按钮 -->
      <view v-if="cameraReady" class="switch-camera-btn" @tap="switchCamera">
        <text class="switch-icon">🔄</text>
      </view>
      
      <!-- 课程标题 -->
      <view class="lesson-title-bar">
        <text class="lesson-title">{{ lessonPlanTitle || '虚拟课堂录制' }}</text>
      </view>
    </view>

    <!-- 控制按钮区域 -->
    <view class="controls-section">
      <!-- 录制时长显示 -->
      <view v-if="isRecording" class="duration-display">
        <text class="duration-label">录制时长：</text>
        <text class="duration-value">{{ formattedDuration }}</text>
        <text v-if="isPaused" class="paused-label">（已暂停）</text>
      </view>
    </view>

    <!-- 逐字稿抽屉 -->
    <ScriptDrawer
      :content="scriptContent"
      :isSpeaking="isSpeaking"
      @copy="onScriptCopy"
      @tts="onTTS"
      @toggle="onDrawerToggle"
    />

    <!-- 录制完成弹窗 -->
    <view v-if="showExportModal" class="modal-overlay" @tap="closeExportModal">
      <view class="export-modal" @tap.stop>
        <view class="modal-header">
          <text class="modal-title">🎬 录制完成</text>
        </view>
        <view class="modal-content">
          <view class="export-info">
            <view class="info-row">
              <text class="info-label">录制时长：</text>
              <text class="info-value">{{ formattedRecordedDuration }}</text>
            </view>
            <view v-if="recordedVideoSize" class="info-row">
              <text class="info-label">文件大小：</text>
              <text class="info-value">{{ formatFileSize(recordedVideoSize) }}</text>
            </view>
          </view>
          <view class="modal-actions">
            <view class="modal-btn preview-btn" @tap="previewVideo">
              <text>▶ 预览视频</text>
            </view>
            <view class="modal-btn save-btn" @tap="saveVideo">
              <text>💾 保存到本地</text>
            </view>
          </view>
          <view class="modal-secondary-actions">
            <view class="secondary-btn" @tap="reRecord">
              <text>🔄 重新录制</text>
            </view>
            <view class="secondary-btn" @tap="goBack">
              <text>← 返回</text>
            </view>
          </view>
        </view>
      </view>
    </view>

    <!-- TTS 播放控制浮窗 -->
    <view v-if="isSpeaking" class="tts-control" @tap="stopTTS">
      <text class="tts-icon">🔊</text>
      <text class="tts-text">正在朗读...</text>
      <text class="tts-stop">点击停止</text>
    </view>
  </view>
</template>

<script>
import ScriptDrawer from '@/components/ScriptDrawer.vue';
import { VideoRecorder } from '@/common/videoRecorder.js';
import { SpeechSynthesisWebSocket } from '@/common/speechSynthesisWebSocket.js';

export default {
  components: {
    ScriptDrawer
  },
  data() {
    return {
      // 逐字稿数据
      lessonPlanId: null,
      lessonPlanTitle: '',
      scriptContent: '',
      
      // 录制状态
      isRecording: false,
      isPaused: false,
      duration: 0,
      recordedDuration: 0,
      recordedVideoPath: null,
      recordedVideoSize: 0,
      recordedVideoBlob: null,
      
      // 摄像头
      cameraReady: false,
      cameraInitializing: false,
      cameraError: '',
      facingMode: 'user', // 'user' 前置, 'environment' 后置
      videoRecorder: null,
      h5VideoElement: null,
      
      // TTS
      ttsWS: null,
      isSpeaking: false,
      audioChunks: [],
      h5Audio: null,
      
      // 弹窗
      showExportModal: false,
      isDrawerExpanded: false
    };
  },
  computed: {
    formattedDuration() {
      return VideoRecorder.formatDuration(this.duration);
    },
    formattedRecordedDuration() {
      return VideoRecorder.formatDuration(this.recordedDuration);
    }
  },
  onLoad() {
    // 通过 eventChannel 接收逐字稿数据
    const eventChannel = this.getOpenerEventChannel();
    if (eventChannel) {
      eventChannel.on('scriptData', (data) => {
        this.lessonPlanId = data.lessonPlanId;
        this.lessonPlanTitle = data.lessonPlanTitle;
        this.scriptContent = data.scriptContent;
      });
    }
  },
  onReady() {
    this.initCamera();
    this.initTTSAudio();
  },
  onUnload() {
    this.cleanup();
  },
  onHide() {
    // 页面隐藏时停止录制和TTS
    if (this.isRecording) {
      this.stopRecording();
    }
    this.stopTTS();
  },
  methods: {
    // 返回上一页
    goBack() {
      uni.navigateBack();
    },
    
    // 初始化录制器（不启动摄像头）
    initRecorder() {
      this.videoRecorder = new VideoRecorder({
        onStart: () => {
          this.isRecording = true;
          this.isPaused = false;
          console.log('[Record] 录制开始');
        },
        onStop: (result) => {
          this.isRecording = false;
          this.isPaused = false;
          this.recordedDuration = result.duration;
          this.recordedVideoPath = result.tempFilePath;
          this.recordedVideoSize = result.size || 0;
          this.recordedVideoBlob = result.blob || null;
          this.showExportModal = true;
          console.log('[Record] 录制停止', result);
        },
        onPause: () => {
          this.isPaused = true;
        },
        onResume: () => {
          this.isPaused = false;
        },
        onError: (err) => {
          console.error('[Record] 错误:', err);
          uni.showToast({
            title: err.message || '录制错误',
            icon: 'none'
          });
        },
        onDurationUpdate: (duration) => {
          this.duration = duration;
        }
      });
    },
    
    // 请求摄像头权限并启动（需要用户点击触发）
    async requestCameraPermission() {
      if (this.cameraInitializing) return;
      
      this.cameraInitializing = true;
      this.cameraError = '';
      
      // #ifdef H5
      try {
        // 等待 DOM 更新
        await new Promise(resolve => setTimeout(resolve, 100));
        
        // 使用容器 ID 初始化摄像头
        const success = await this.videoRecorder.initH5Camera('videoContainer', this.facingMode);
        if (success) {
          this.cameraReady = true;
          this.cameraError = '';
        } else {
          this.cameraError = '无法访问摄像头，请检查权限设置';
        }
      } catch (err) {
        console.error('[Record] 摄像头初始化失败:', err);
        if (err.name === 'NotAllowedError') {
          this.cameraError = '摄像头权限被拒绝，请在浏览器设置中允许';
        } else if (err.name === 'NotFoundError') {
          this.cameraError = '未找到摄像头设备';
        } else {
          this.cameraError = err.message || '摄像头启动失败';
        }
      } finally {
        this.cameraInitializing = false;
      }
      // #endif
      
      // #ifndef H5
      // 小程序环境
      this.videoRecorder.initMiniProgramCamera('myCamera');
      // 检查相机权限
      uni.getSetting({
        success: (res) => {
          if (res.authSetting['scope.camera']) {
            this.cameraReady = true;
            this.cameraInitializing = false;
          } else {
            uni.authorize({
              scope: 'scope.camera',
              success: () => {
                this.cameraReady = true;
                this.cameraInitializing = false;
              },
              fail: () => {
                this.cameraError = '请在设置中授权摄像头权限';
                this.cameraInitializing = false;
                // 引导用户去设置
                uni.showModal({
                  title: '需要摄像头权限',
                  content: '请在设置中开启摄像头权限',
                  confirmText: '去设置',
                  success: (modalRes) => {
                    if (modalRes.confirm) {
                      uni.openSetting();
                    }
                  }
                });
              }
            });
          }
        },
        fail: () => {
          this.cameraError = '获取权限状态失败';
          this.cameraInitializing = false;
        }
      });
      // #endif
    },
    
    // 初始化摄像头（兼容旧逻辑）
    async initCamera() {
      this.initRecorder();
      
      // #ifndef H5
      // 小程序环境自动请求权限
      this.requestCameraPermission();
      // #endif
      
      // H5 环境等待用户点击按钮
    },
    
    // 相机错误处理
    onCameraError(e) {
      console.error('[Record] 相机错误:', e);
      this.cameraReady = false;
      uni.showToast({
        title: '相机启动失败',
        icon: 'none'
      });
    },
    
    // 初始化 TTS 音频播放
    initTTSAudio() {
      // 使用运行时检测，确保在浏览器环境下初始化 Audio
      if (typeof window !== 'undefined' && typeof Audio !== 'undefined') {
        this.h5Audio = new Audio();
        this.h5Audio.addEventListener('ended', () => {
          this.isSpeaking = false;
        });
        this.h5Audio.addEventListener('error', (e) => {
          console.error('[TTS] 音频播放错误:', e);
          this.isSpeaking = false;
        });
        console.log('[TTS] H5 音频播放器初始化成功');
      }
    },
    
    // 切换摄像头
    async switchCamera() {
      if (this.isRecording) {
        uni.showToast({
          title: '录制中无法切换摄像头',
          icon: 'none'
        });
        return;
      }
      
      this.facingMode = this.facingMode === 'user' ? 'environment' : 'user';
      
      // #ifdef H5
      if (this.videoRecorder && this.cameraReady) {
        this.cameraReady = false;
        this.cameraInitializing = true;
        const success = await this.videoRecorder.initH5Camera('videoContainer', this.facingMode);
        this.cameraReady = success;
        this.cameraInitializing = false;
      }
      // #endif
      
      uni.showToast({
        title: this.facingMode === 'user' ? '已切换到前置摄像头' : '已切换到后置摄像头',
        icon: 'none'
      });
    },
    
    // 开始录制
    startRecording() {
      if (!this.cameraReady) {
        uni.showToast({
          title: '摄像头未就绪',
          icon: 'none'
        });
        return;
      }
      
      if (!this.videoRecorder) {
        uni.showToast({
          title: '录制器未初始化',
          icon: 'none'
        });
        return;
      }
      
      // #ifdef H5
      const success = this.videoRecorder.startH5Recording();
      if (!success) {
        uni.showToast({
          title: '开始录制失败',
          icon: 'none'
        });
      }
      // #endif
      
      // #ifndef H5
      this.videoRecorder.startMiniProgramRecording();
      // #endif
    },
    
    // 暂停/继续录制
    togglePause() {
      if (!this.videoRecorder || !this.isRecording) return;
      
      // #ifdef H5
      if (this.isPaused) {
        this.videoRecorder.resumeH5Recording();
        uni.showToast({
          title: '继续录制',
          icon: 'none',
          duration: 1000
        });
      } else {
        this.videoRecorder.pauseH5Recording();
        uni.showToast({
          title: '已暂停',
          icon: 'none',
          duration: 1000
        });
      }
      // #endif
      
      // #ifndef H5
      uni.showToast({
        title: '小程序暂不支持暂停',
        icon: 'none'
      });
      // #endif
    },
    
    // 停止录制
    stopRecording() {
      if (!this.videoRecorder || !this.isRecording) return;
      
      uni.showModal({
        title: '结束录制',
        content: '确定要结束本次录制吗？',
        confirmText: '结束',
        cancelText: '继续录制',
        success: (res) => {
          if (res.confirm) {
            // #ifdef H5
            this.videoRecorder.stopH5Recording();
            // #endif
            
            // #ifndef H5
            this.videoRecorder.stopMiniProgramRecording();
            // #endif
          }
        }
      });
    },
    
    // 逐字稿复制
    onScriptCopy(text) {
      console.log('[Record] 逐字稿已复制');
    },
    
    /**
     * AI跟读 - 语音合成播放剪贴板文本
     * @param {string} text - 从剪贴板读取的文本内容（空字符串表示停止播放）
     * @param {string} voice - 语音音色（可选，后期扩展用）
     */
    async onTTS(text, voice = null) {
      // 如果正在朗读，或者传入空文本，则停止
      if (this.isSpeaking || !text || text.trim() === '') {
        this.stopTTS();
        return;
      }
      
      // 限制文本长度（后端 TTS 服务限制最大300字符）
      const maxLength = 300;
      let ttsText = text;
      
      if (text.length > maxLength) {
        ttsText = text.substring(0, maxLength);
        uni.showToast({
          title: `文本过长，仅朗读前${maxLength}字`,
          icon: 'none',
          duration: 2000
        });
      }
      
      // 默认音色配置（后期可扩展为根据内容类型自动匹配）
      // 可选音色示例：
      // - azure:zh-CN-XiaoxiaoMultilingualNeural（女声，默认）
      // - azure:zh-CN-YunxiMultilingualNeural（男声）
      // - azure:zh-CN-XiaoyiNeural（年轻女声，适合学生）
      // - azure:zh-CN-YunjianNeural（年轻男声，适合学生）
      const defaultVoice = 'azure:zh-CN-XiaoxiaoMultilingualNeural';
      const selectedVoice = voice || defaultVoice;
      
      try {
        this.isSpeaking = true;
        this.audioChunks = [];
        
        console.log('[TTS] 开始语音合成, 文本长度:', ttsText.length, ', 音色:', selectedVoice);
        
        // 初始化 TTS WebSocket
        this.ttsWS = new SpeechSynthesisWebSocket({
          onConnected: () => {
            console.log('[TTS] 已连接');
          },
          onAudioData: (audioData) => {
            this.audioChunks.push(audioData);
          },
          onCompleted: () => {
            console.log('[TTS] 合成完成');
            this.playTTSAudio();
          },
          onError: (err) => {
            console.error('[TTS] 错误:', err);
            this.isSpeaking = false;
            uni.showToast({
              title: '语音合成失败',
              icon: 'none'
            });
          },
          onDisconnected: () => {
            console.log('[TTS] 已断开');
          }
        });
        
        await this.ttsWS.connect();
        this.ttsWS.synthesize(ttsText, selectedVoice, ttsText.length > 200);
        
      } catch (err) {
        console.error('[TTS] 初始化失败:', err);
        this.isSpeaking = false;
        uni.showToast({
          title: '语音服务连接失败',
          icon: 'none'
        });
      }
    },
    
    // 播放 TTS 音频
    playTTSAudio() {
      if (this.audioChunks.length === 0) {
        this.isSpeaking = false;
        return;
      }
      
      // 合并音频数据
      const totalLength = this.audioChunks.reduce((acc, chunk) => acc + chunk.byteLength, 0);
      const audioData = new Uint8Array(totalLength);
      let offset = 0;
      for (const chunk of this.audioChunks) {
        audioData.set(new Uint8Array(chunk), offset);
        offset += chunk.byteLength;
      }
      
      console.log('[TTS] 准备播放音频, 数据大小:', totalLength);
      
      // 使用运行时检测环境
      if (typeof window !== 'undefined' && typeof Blob !== 'undefined') {
        // H5/浏览器环境
        // 确保 h5Audio 已初始化
        if (!this.h5Audio) {
          this.h5Audio = new Audio();
          this.h5Audio.addEventListener('ended', () => {
            this.isSpeaking = false;
          });
          this.h5Audio.addEventListener('error', (e) => {
            console.error('[TTS] 音频播放错误:', e);
            this.isSpeaking = false;
          });
        }
        
        const blob = new Blob([audioData], { type: 'audio/mpeg' });
        const url = URL.createObjectURL(blob);
        this.h5Audio.src = url;
        this.h5Audio.play().then(() => {
          console.log('[TTS] 音频开始播放');
        }).catch(err => {
          console.error('[TTS] 播放失败:', err);
          this.isSpeaking = false;
          uni.showToast({
            title: '音频播放失败',
            icon: 'none'
          });
        });
      } else if (typeof uni !== 'undefined' && uni.getFileSystemManager) {
        // 小程序环境
        const fs = uni.getFileSystemManager();
        const tempPath = `${wx.env.USER_DATA_PATH}/tts_${Date.now()}.mp3`;
        fs.writeFile({
          filePath: tempPath,
          data: audioData.buffer,
          encoding: 'binary',
          success: () => {
            const innerAudio = uni.createInnerAudioContext();
            innerAudio.src = tempPath;
            innerAudio.onEnded(() => {
              this.isSpeaking = false;
              innerAudio.destroy();
            });
            innerAudio.play();
          },
          fail: () => {
            this.isSpeaking = false;
          }
        });
      } else {
        console.error('[TTS] 未知环境，无法播放音频');
        this.isSpeaking = false;
      }
    },
    
    // 停止 TTS
    stopTTS() {
      this.isSpeaking = false;
      
      if (this.ttsWS) {
        this.ttsWS.close();
        this.ttsWS = null;
      }
      
      // #ifdef H5
      if (this.h5Audio) {
        this.h5Audio.pause();
        this.h5Audio.currentTime = 0;
      }
      // #endif
    },
    
    // 抽屉展开/收起
    onDrawerToggle(expanded) {
      this.isDrawerExpanded = expanded;
    },
    
    // 预览视频
    previewVideo() {
      if (!this.recordedVideoPath) {
        uni.showToast({
          title: '视频文件不存在',
          icon: 'none'
        });
        return;
      }
      
      // #ifdef H5
      // H5 在新窗口打开视频
      window.open(this.recordedVideoPath, '_blank');
      // #endif
      
      // #ifndef H5
      // 小程序使用预览视频
      uni.previewMedia({
        sources: [{
          url: this.recordedVideoPath,
          type: 'video'
        }]
      });
      // #endif
    },
    
    // 保存视频
    async saveVideo() {
      if (!this.recordedVideoPath) {
        uni.showToast({
          title: '视频文件不存在',
          icon: 'none'
        });
        return;
      }
      
      try {
        await VideoRecorder.saveToAlbum(this.recordedVideoPath);
        uni.showToast({
          title: '保存成功',
          icon: 'success'
        });
        this.showExportModal = false;
      } catch (err) {
        console.error('[Record] 保存失败:', err);
      }
    },
    
    // 重新录制
    reRecord() {
      this.showExportModal = false;
      this.recordedVideoPath = null;
      this.recordedVideoSize = 0;
      this.recordedVideoBlob = null;
      this.recordedDuration = 0;
      this.duration = 0;
    },
    
    // 关闭导出弹窗
    closeExportModal() {
      this.showExportModal = false;
    },
    
    // 格式化文件大小
    formatFileSize(bytes) {
      if (bytes === 0) return '0 B';
      const k = 1024;
      const sizes = ['B', 'KB', 'MB', 'GB'];
      const i = Math.floor(Math.log(bytes) / Math.log(k));
      return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
    },
    
    // 清理资源
    cleanup() {
      if (this.videoRecorder) {
        this.videoRecorder.destroy();
        this.videoRecorder = null;
      }
      
      this.stopTTS();
      
      // #ifdef H5
      if (this.h5Audio) {
        this.h5Audio = null;
      }
      // #endif
    }
  }
};
</script>

<style scoped>
.page {
  min-height: 100vh;
  background: #000000;
  display: flex;
  flex-direction: column;
}

/* 顶部状态栏 */
.status-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20rpx 32rpx;
  padding-top: calc(env(safe-area-inset-top) + 20rpx);
  background: rgba(0, 0, 0, 0.8);
  z-index: 10;
}

.back-btn {
  display: flex;
  align-items: center;
  gap: 8rpx;
  padding: 12rpx 20rpx;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 20rpx;
}

.back-icon {
  font-size: 36rpx;
  color: #FFFFFF;
}

.back-text {
  font-size: 28rpx;
  color: #FFFFFF;
}

.status-info {
  flex: 1;
  display: flex;
  justify-content: center;
}

.camera-status {
  display: flex;
  align-items: center;
  gap: 12rpx;
  padding: 12rpx 24rpx;
  border-radius: 20rpx;
  font-size: 26rpx;
}

.camera-status.loading {
  background: rgba(255, 165, 0, 0.2);
  color: #FFA500;
}

.camera-status.waiting {
  background: rgba(150, 150, 150, 0.2);
  color: #AAAAAA;
}

.camera-status.ready {
  background: rgba(82, 196, 26, 0.2);
  color: #52C41A;
}

.camera-status.recording {
  background: rgba(255, 68, 68, 0.2);
  color: #FF4444;
}

.recording-dot {
  width: 16rpx;
  height: 16rpx;
  background: #FF4444;
  border-radius: 50%;
  animation: blink 1s infinite;
}

@keyframes blink {
  0%, 50% { opacity: 1; }
  51%, 100% { opacity: 0.3; }
}

/* 摄像头预览区域 */
.camera-section {
  position: relative;
  width: 100%;
  height: 55vh;
  background: #1A1A1A;
}

.camera-preview {
  width: 100%;
  height: 100%;
  object-fit: cover;
  background: #000000;
}

.camera-preview-container {
  width: 100%;
  height: 100%;
  background: #000000;
  position: relative;
}

/* 摄像头占位 */
.camera-placeholder {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: #1A1A1A;
  gap: 24rpx;
}

.placeholder-icon {
  font-size: 100rpx;
}

.placeholder-text {
  font-size: 32rpx;
  color: #FFFFFF;
}

.placeholder-tip {
  font-size: 26rpx;
  color: #888888;
}

.placeholder-error {
  font-size: 26rpx;
  color: #FF4444;
  margin-top: 16rpx;
}

.start-camera-btn {
  display: flex;
  align-items: center;
  gap: 16rpx;
  margin: 32rpx 0;
  padding: 24rpx 48rpx;
  background: linear-gradient(135deg, #4A90D9, #357ABD);
  border-radius: 48rpx;
  box-shadow: 0 8rpx 24rpx rgba(74, 144, 217, 0.4);
}

.start-camera-btn:active {
  transform: scale(0.95);
  opacity: 0.9;
}

.start-camera-icon {
  font-size: 40rpx;
}

.start-camera-text {
  font-size: 32rpx;
  color: #FFFFFF;
  font-weight: 500;
}

/* 开始录制按钮（左上角） */
.start-recording-btn-top {
  position: absolute;
  top: 40rpx;
  left: 40rpx;
  z-index: 20;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12rpx;
}

.btn-icon-wrapper-top {
  width: 100rpx;
  height: 100rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: transform 0.2s;
  border: 4rpx solid rgba(255, 255, 255, 0.3);
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.3);
}

.btn-icon-wrapper-top:active {
  transform: scale(0.95);
}

.btn-icon-wrapper-top.start {
  background: #FF4444;
  border-color: #FF6666;
}

.btn-icon-top {
  font-size: 48rpx;
  color: #FFFFFF;
  font-weight: bold;
}

.btn-text-top {
  font-size: 24rpx;
  color: #FFFFFF;
  background: rgba(0, 0, 0, 0.6);
  padding: 8rpx 16rpx;
  border-radius: 20rpx;
  white-space: nowrap;
  font-weight: 500;
}

/* 录制中的控制按钮（左上角） */
.recording-controls-top {
  position: absolute;
  top: 40rpx;
  left: 40rpx;
  z-index: 20;
  display: flex;
  gap: 24rpx;
  align-items: flex-start;
}

.recording-btn-top {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12rpx;
}

.btn-icon-wrapper-top.pause {
  background: #FFA500;
  border-color: #FFB833;
}

.btn-icon-wrapper-top.resume {
  background: #52C41A;
  border-color: #73D13D;
}

.btn-icon-wrapper-top.stop {
  background: #FF4444;
  border-color: #FF6666;
}

/* 切换摄像头按钮 */
.switch-camera-btn {
  position: absolute;
  top: 40rpx;
  right: 40rpx;
  width: 80rpx;
  height: 80rpx;
  background: rgba(0, 0, 0, 0.5);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 20;
}

.switch-icon {
  font-size: 40rpx;
}

/* 课程标题 */
.lesson-title-bar {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 24rpx 40rpx;
  background: linear-gradient(transparent, rgba(0, 0, 0, 0.7));
}

.lesson-title {
  font-size: 32rpx;
  color: #FFFFFF;
  font-weight: 500;
}

/* 控制按钮区域 */
.controls-section {
  padding: 32rpx;
  background: #1A1A1A;
}

.controls-row {
  display: flex;
  justify-content: center;
  align-items: center;
}

.recording-controls {
  display: flex;
  gap: 60rpx;
}

.control-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16rpx;
}

.control-btn.disabled {
  opacity: 0.5;
}

.btn-icon-wrapper {
  width: 120rpx;
  height: 120rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: transform 0.2s;
  border: 4rpx solid rgba(255, 255, 255, 0.3);
}

.btn-icon-wrapper:active {
  transform: scale(0.95);
}

.btn-icon-wrapper.start {
  background: #FF4444;
  border-color: #FF6666;
}

.btn-icon-wrapper.pause {
  background: #FFA500;
  border-color: #FFB833;
}

.btn-icon-wrapper.resume {
  background: #52C41A;
  border-color: #73D13D;
}

.btn-icon-wrapper.stop {
  background: #666666;
  border-color: #888888;
}

.btn-icon {
  font-size: 48rpx;
  color: #FFFFFF;
}

.btn-text {
  font-size: 26rpx;
  color: #FFFFFF;
}

/* 录制时长显示 */
.duration-display {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8rpx;
  margin-top: 24rpx;
  padding: 16rpx 32rpx;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 12rpx;
}

.duration-label {
  font-size: 26rpx;
  color: #AAAAAA;
}

.duration-value {
  font-size: 32rpx;
  color: #FFFFFF;
  font-weight: 600;
  font-family: monospace;
}

.paused-label {
  font-size: 26rpx;
  color: #FFA500;
}

/* TTS 播放控制 */
.tts-control {
  position: fixed;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16rpx;
  padding: 40rpx 60rpx;
  background: rgba(0, 0, 0, 0.9);
  border-radius: 24rpx;
  z-index: 200;
}

.tts-icon {
  font-size: 60rpx;
}

.tts-text {
  font-size: 28rpx;
  color: #FFFFFF;
}

.tts-stop {
  font-size: 24rpx;
  color: #AAAAAA;
}

/* 导出弹窗 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 300;
}

.export-modal {
  width: 85%;
  max-width: 600rpx;
  background: #FFFFFF;
  border-radius: 24rpx;
  overflow: hidden;
}

.modal-header {
  padding: 32rpx;
  text-align: center;
  border-bottom: 1rpx solid #F0F0F0;
  background: #F8F9FA;
}

.modal-title {
  font-size: 36rpx;
  font-weight: 600;
  color: #333333;
}

.modal-content {
  padding: 32rpx;
}

.export-info {
  margin-bottom: 32rpx;
  padding: 24rpx;
  background: #F8F9FA;
  border-radius: 12rpx;
}

.info-row {
  display: flex;
  justify-content: space-between;
  padding: 12rpx 0;
}

.info-label {
  font-size: 28rpx;
  color: #666666;
}

.info-value {
  font-size: 28rpx;
  color: #333333;
  font-weight: 500;
}

.modal-actions {
  display: flex;
  gap: 24rpx;
  margin-bottom: 24rpx;
}

.modal-btn {
  flex: 1;
  padding: 28rpx;
  border-radius: 12rpx;
  text-align: center;
  font-size: 28rpx;
  font-weight: 500;
}

.preview-btn {
  background: #F5F7FA;
  color: #333333;
}

.save-btn {
  background: #4A90D9;
  color: #FFFFFF;
}

.modal-secondary-actions {
  display: flex;
  justify-content: center;
  gap: 48rpx;
  padding-top: 16rpx;
  border-top: 1rpx solid #F0F0F0;
}

.secondary-btn {
  font-size: 26rpx;
  color: #666666;
  padding: 16rpx;
}
</style>
