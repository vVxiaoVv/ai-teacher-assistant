<template>
  <view class="speech-synthesis">
    <!-- 标题 -->
    <view class="section-header">
      <text class="section-title">语音合成</text>
      <text class="section-subtitle">将文字转换为语音</text>
    </view>
    
    <!-- 语音角色选择 -->
    <view class="voice-selector">
      <text class="label">选择语音：</text>
      <picker :range="voiceOptions" range-key="name" :value="selectedVoiceIndex" @change="onVoiceChange">
        <view class="picker-content">
          <text class="picker-text">{{ currentVoice.name }}</text>
          <text class="picker-arrow">▼</text>
        </view>
      </picker>
    </view>
    
    <!-- 文本输入区域 -->
    <view class="text-input-section">
      <textarea 
        v-model="inputText" 
        class="text-input"
        :placeholder="placeholder"
        :maxlength="maxLength"
        :disabled="isSynthesizing"
      />
      <view class="text-counter">
        <text>{{ inputText.length }} / {{ maxLength }}</text>
      </view>
    </view>
    
    <!-- 操作按钮 -->
    <view class="action-buttons">
      <button 
        class="btn-synthesize" 
        :class="{ 'btn-disabled': !canSynthesize, 'btn-loading': isSynthesizing }"
        :disabled="!canSynthesize"
        @click="startSynthesis"
      >
        <text v-if="isSynthesizing" class="btn-icon">⏳</text>
        <text v-else class="btn-icon">🔊</text>
        <text>{{ synthesizeButtonText }}</text>
      </button>
      
      <button 
        v-if="hasAudio" 
        class="btn-clear"
        @click="clearAudio"
      >
        <text class="btn-icon">🗑️</text>
        <text>清除</text>
      </button>
    </view>
    
    <!-- 音频播放控制 -->
    <view v-if="hasAudio" class="audio-player">
      <view class="player-controls">
        <button class="btn-play" @click="togglePlay">
          <text class="play-icon">{{ isPlaying ? '⏸️' : '▶️' }}</text>
        </button>
        
        <view class="progress-container">
          <slider 
            :value="playProgress" 
            :max="100" 
            :step="1"
            activeColor="#4a90d9"
            backgroundColor="#e0e0e0"
            block-size="16"
            @change="onProgressChange"
          />
          <view class="time-display">
            <text>{{ formatTime(currentTime) }}</text>
            <text>{{ formatTime(duration) }}</text>
          </view>
        </view>
      </view>
    </view>
    
    <!-- 实时字幕显示 -->
    <view v-if="showSubtitle && currentSubtitle" class="subtitle-container">
      <text class="subtitle-text">{{ currentSubtitle }}</text>
    </view>
    
    <!-- 状态提示 -->
    <view v-if="statusMessage" class="status-message" :class="statusType">
      <text>{{ statusMessage }}</text>
    </view>
  </view>
</template>

<script>
import { SpeechSynthesisWebSocket } from '@/common/speechSynthesisWebSocket.js';

// 预置语音角色
const VOICE_OPTIONS = [
  { id: 'azure:zh-CN-XiaoxiaoMultilingualNeural', name: '晓晓（女声-多语言）' },
  { id: 'azure:zh-CN-YunxiMultilingualNeural', name: '云希（男声-多语言）' },
  { id: 'azure:zh-CN-XiaoyiNeural', name: '晓依（女声）' },
  { id: 'azure:zh-CN-YunjianNeural', name: '云健（男声）' },
  { id: 'azure:zh-CN-XiaochenNeural', name: '晓辰（女声）' },
  { id: 'azure:zh-CN-YunyangNeural', name: '云扬（男声-新闻）' }
];

export default {
  name: 'SpeechSynthesis',
  
  props: {
    // 默认文本
    defaultText: {
      type: String,
      default: ''
    },
    // 占位符
    placeholder: {
      type: String,
      default: '请输入需要转换为语音的文字...'
    },
    // 最大字符数
    maxLength: {
      type: Number,
      default: 500
    },
    // 是否显示字幕
    showSubtitle: {
      type: Boolean,
      default: true
    },
    // 默认语音角色索引
    defaultVoiceIndex: {
      type: Number,
      default: 0
    }
  },
  
  data() {
    return {
      // 输入文本
      inputText: '',
      
      // 语音选择
      voiceOptions: VOICE_OPTIONS,
      selectedVoiceIndex: 0,
      
      // WebSocket
      ttsWS: null,
      isConnected: false,
      isSynthesizing: false,
      
      // 音频数据
      audioChunks: [],
      audioBlob: null,
      audioUrl: null,
      
      // 播放状态
      isPlaying: false,
      currentTime: 0,
      duration: 0,
      playProgress: 0,
      
      // H5 音频播放
      h5Audio: null,
      h5AudioContext: null,
      
      // 小程序音频播放
      innerAudioContext: null,
      
      // 字幕
      wordBoundaries: [],
      currentSubtitle: '',
      subtitleTimer: null,
      
      // 状态
      statusMessage: '',
      statusType: ''
    };
  },
  
  computed: {
    currentVoice() {
      return this.voiceOptions[this.selectedVoiceIndex] || this.voiceOptions[0];
    },
    
    canSynthesize() {
      return this.inputText.trim().length > 0 && !this.isSynthesizing;
    },
    
    synthesizeButtonText() {
      if (this.isSynthesizing) {
        return '合成中...';
      }
      return '开始合成';
    },
    
    hasAudio() {
      return this.audioUrl !== null || this.audioChunks.length > 0;
    }
  },
  
  watch: {
    defaultText: {
      immediate: true,
      handler(val) {
        if (val) {
          this.inputText = val;
        }
      }
    },
    
    defaultVoiceIndex: {
      immediate: true,
      handler(val) {
        if (val >= 0 && val < this.voiceOptions.length) {
          this.selectedVoiceIndex = val;
        }
      }
    }
  },
  
  mounted() {
    this.initAudioPlayer();
  },
  
  beforeDestroy() {
    this.cleanup();
  },
  
  methods: {
    /**
     * 初始化音频播放器
     */
    initAudioPlayer() {
      // #ifdef H5
      // H5 使用 Audio 对象
      this.h5Audio = new Audio();
      this.h5Audio.addEventListener('timeupdate', this.onTimeUpdate);
      this.h5Audio.addEventListener('ended', this.onPlayEnded);
      this.h5Audio.addEventListener('loadedmetadata', this.onLoadedMetadata);
      // #endif
      
      // #ifndef H5
      // 小程序使用 innerAudioContext
      this.innerAudioContext = uni.createInnerAudioContext();
      this.innerAudioContext.onTimeUpdate(() => {
        this.currentTime = this.innerAudioContext.currentTime;
        this.duration = this.innerAudioContext.duration || 0;
        if (this.duration > 0) {
          this.playProgress = (this.currentTime / this.duration) * 100;
        }
        this.updateSubtitle();
      });
      this.innerAudioContext.onEnded(() => {
        this.isPlaying = false;
        this.playProgress = 0;
        this.currentTime = 0;
        this.currentSubtitle = '';
      });
      // #endif
    },
    
    /**
     * 语音选择变化
     */
    onVoiceChange(e) {
      this.selectedVoiceIndex = e.detail.value;
    },
    
    /**
     * 开始语音合成
     */
    async startSynthesis() {
      if (!this.canSynthesize) return;
      
      this.showStatus('正在连接语音服务...', 'info');
      this.clearAudio();
      this.audioChunks = [];
      
      try {
        // 初始化 WebSocket
        await this.initWebSocket();
        
        // 发送合成请求
        this.showStatus('正在合成语音...', 'info');
        const success = this.ttsWS.synthesize(
          this.inputText,
          this.currentVoice.id,
          this.inputText.length > 200 // 超过200字使用长文本模式
        );
        
        if (!success) {
          this.showStatus('发送合成请求失败', 'error');
          this.isSynthesizing = false;
        }
        
      } catch (err) {
        console.error('[TTS] 合成失败:', err);
        this.showStatus('合成失败: ' + (err.message || err), 'error');
        this.isSynthesizing = false;
      }
    },
    
    /**
     * 初始化 WebSocket 连接
     */
    async initWebSocket() {
      if (this.ttsWS && this.isConnected) {
        return;
      }
      
      this.ttsWS = new SpeechSynthesisWebSocket({
        onConnected: (data) => {
          console.log('[TTS] 连接成功:', data);
          this.isConnected = true;
        },
        
        onDisconnected: (data) => {
          console.log('[TTS] 连接断开:', data);
          this.isConnected = false;
          this.isSynthesizing = false;
        },
        
        onAudioPlay: (data) => {
          console.log('[TTS] AudioPlay事件:', data);
          // 音频参数，可用于初始化播放器
        },
        
        onAudioData: (audioData) => {
          console.log('[TTS] 收到音频数据, 大小:', audioData.byteLength);
          // 收集音频数据
          this.audioChunks.push(audioData);
        },
        
        onMetaInfo: (data) => {
          console.log('[TTS] MetaInfo事件:', data);
          // 保存字词边界信息用于字幕显示
          if (data.wordBoundary) {
            this.wordBoundaries = this.wordBoundaries.concat(data.wordBoundary);
          }
        },
        
        onCompleted: (data) => {
          console.log('[TTS] 合成完成:', data);
          this.isSynthesizing = false;
          this.showStatus('合成完成', 'success');
          
          // 合并音频数据并创建播放 URL
          this.createAudioFromChunks();
          
          // 触发完成事件
          this.$emit('synthesis-complete', {
            text: this.inputText,
            voice: this.currentVoice,
            audioUrl: this.audioUrl
          });
        },
        
        onError: (err) => {
          console.error('[TTS] 错误:', err);
          this.isSynthesizing = false;
          this.showStatus('合成失败: ' + (err.message || '未知错误'), 'error');
          
          // 触发错误事件
          this.$emit('synthesis-error', err);
        }
      });
      
      this.isSynthesizing = true;
      await this.ttsWS.connect();
    },
    
    /**
     * 从音频块创建可播放的音频
     */
    createAudioFromChunks() {
      if (this.audioChunks.length === 0) {
        console.warn('[TTS] 没有音频数据');
        return;
      }
      
      // 合并所有音频块
      const totalLength = this.audioChunks.reduce((acc, chunk) => acc + chunk.byteLength, 0);
      const audioData = new Uint8Array(totalLength);
      let offset = 0;
      for (const chunk of this.audioChunks) {
        audioData.set(new Uint8Array(chunk), offset);
        offset += chunk.byteLength;
      }
      
      console.log('[TTS] 合并音频数据, 总大小:', totalLength);
      
      // #ifdef H5
      // H5: 创建 Blob URL
      this.audioBlob = new Blob([audioData], { type: 'audio/mpeg' });
      this.audioUrl = URL.createObjectURL(this.audioBlob);
      this.h5Audio.src = this.audioUrl;
      console.log('[TTS] H5 音频URL已创建:', this.audioUrl);
      // #endif
      
      // #ifndef H5
      // 小程序: 保存到临时文件
      const fs = uni.getFileSystemManager();
      const tempPath = `${wx.env.USER_DATA_PATH}/tts_audio_${Date.now()}.mp3`;
      
      fs.writeFile({
        filePath: tempPath,
        data: audioData.buffer,
        encoding: 'binary',
        success: () => {
          console.log('[TTS] 音频文件已保存:', tempPath);
          this.audioUrl = tempPath;
          this.innerAudioContext.src = tempPath;
        },
        fail: (err) => {
          console.error('[TTS] 保存音频文件失败:', err);
        }
      });
      // #endif
    },
    
    /**
     * 切换播放/暂停
     */
    togglePlay() {
      if (!this.audioUrl) return;
      
      if (this.isPlaying) {
        this.pauseAudio();
      } else {
        this.playAudio();
      }
    },
    
    /**
     * 播放音频
     */
    playAudio() {
      // #ifdef H5
      this.h5Audio.play().then(() => {
        this.isPlaying = true;
      }).catch(err => {
        console.error('[TTS] H5播放失败:', err);
      });
      // #endif
      
      // #ifndef H5
      this.innerAudioContext.play();
      this.isPlaying = true;
      // #endif
    },
    
    /**
     * 暂停音频
     */
    pauseAudio() {
      // #ifdef H5
      this.h5Audio.pause();
      // #endif
      
      // #ifndef H5
      this.innerAudioContext.pause();
      // #endif
      
      this.isPlaying = false;
    },
    
    /**
     * 进度条变化
     */
    onProgressChange(e) {
      const progress = e.detail.value;
      const newTime = (progress / 100) * this.duration;
      
      // #ifdef H5
      this.h5Audio.currentTime = newTime;
      // #endif
      
      // #ifndef H5
      this.innerAudioContext.seek(newTime);
      // #endif
      
      this.currentTime = newTime;
      this.playProgress = progress;
    },
    
    /**
     * H5 时间更新回调
     */
    onTimeUpdate() {
      if (this.h5Audio) {
        this.currentTime = this.h5Audio.currentTime;
        this.duration = this.h5Audio.duration || 0;
        if (this.duration > 0) {
          this.playProgress = (this.currentTime / this.duration) * 100;
        }
        this.updateSubtitle();
      }
    },
    
    /**
     * H5 播放结束回调
     */
    onPlayEnded() {
      this.isPlaying = false;
      this.playProgress = 0;
      this.currentTime = 0;
      this.currentSubtitle = '';
    },
    
    /**
     * H5 元数据加载完成
     */
    onLoadedMetadata() {
      if (this.h5Audio) {
        this.duration = this.h5Audio.duration || 0;
      }
    },
    
    /**
     * 更新字幕
     */
    updateSubtitle() {
      if (!this.showSubtitle || this.wordBoundaries.length === 0) return;
      
      // 根据当前播放时间查找对应的字词
      const currentWord = this.wordBoundaries.find(wb => 
        this.currentTime >= wb.start_time && this.currentTime <= wb.end_time
      );
      
      if (currentWord) {
        this.currentSubtitle = currentWord.word;
      }
    },
    
    /**
     * 清除音频
     */
    clearAudio() {
      this.pauseAudio();
      
      // #ifdef H5
      if (this.audioUrl) {
        URL.revokeObjectURL(this.audioUrl);
      }
      if (this.h5Audio) {
        this.h5Audio.src = '';
      }
      // #endif
      
      // #ifndef H5
      if (this.innerAudioContext) {
        this.innerAudioContext.stop();
        this.innerAudioContext.src = '';
      }
      // 删除临时文件
      if (this.audioUrl) {
        const fs = uni.getFileSystemManager();
        fs.unlink({
          filePath: this.audioUrl,
          fail: () => {} // 忽略删除失败
        });
      }
      // #endif
      
      this.audioChunks = [];
      this.audioBlob = null;
      this.audioUrl = null;
      this.wordBoundaries = [];
      this.currentSubtitle = '';
      this.currentTime = 0;
      this.duration = 0;
      this.playProgress = 0;
      this.statusMessage = '';
    },
    
    /**
     * 显示状态消息
     */
    showStatus(message, type = 'info') {
      this.statusMessage = message;
      this.statusType = type;
      
      // 成功和错误消息3秒后自动隐藏
      if (type === 'success' || type === 'error') {
        setTimeout(() => {
          if (this.statusMessage === message) {
            this.statusMessage = '';
          }
        }, 3000);
      }
    },
    
    /**
     * 格式化时间
     */
    formatTime(seconds) {
      if (!seconds || isNaN(seconds)) return '0:00';
      const mins = Math.floor(seconds / 60);
      const secs = Math.floor(seconds % 60);
      return `${mins}:${secs.toString().padStart(2, '0')}`;
    },
    
    /**
     * 清理资源
     */
    cleanup() {
      this.clearAudio();
      
      if (this.ttsWS) {
        this.ttsWS.close();
        this.ttsWS = null;
      }
      
      // #ifdef H5
      if (this.h5Audio) {
        this.h5Audio.removeEventListener('timeupdate', this.onTimeUpdate);
        this.h5Audio.removeEventListener('ended', this.onPlayEnded);
        this.h5Audio.removeEventListener('loadedmetadata', this.onLoadedMetadata);
        this.h5Audio = null;
      }
      // #endif
      
      // #ifndef H5
      if (this.innerAudioContext) {
        this.innerAudioContext.destroy();
        this.innerAudioContext = null;
      }
      // #endif
    }
  }
};
</script>

<style scoped>
.speech-synthesis {
  padding: 20rpx;
  background: #ffffff;
  border-radius: 16rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.08);
}

/* 标题区域 */
.section-header {
  margin-bottom: 24rpx;
}

.section-title {
  font-size: 36rpx;
  font-weight: 600;
  color: #333;
  display: block;
}

.section-subtitle {
  font-size: 24rpx;
  color: #999;
  margin-top: 8rpx;
  display: block;
}

/* 语音选择器 */
.voice-selector {
  display: flex;
  align-items: center;
  margin-bottom: 20rpx;
  padding: 16rpx 20rpx;
  background: #f5f7fa;
  border-radius: 12rpx;
}

.label {
  font-size: 28rpx;
  color: #666;
  margin-right: 16rpx;
}

.picker-content {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8rpx 16rpx;
  background: #fff;
  border-radius: 8rpx;
  border: 1rpx solid #e0e0e0;
}

.picker-text {
  font-size: 28rpx;
  color: #333;
}

.picker-arrow {
  font-size: 20rpx;
  color: #999;
}

/* 文本输入区域 */
.text-input-section {
  margin-bottom: 20rpx;
}

.text-input {
  width: 100%;
  height: 200rpx;
  padding: 20rpx;
  font-size: 28rpx;
  color: #333;
  background: #f5f7fa;
  border-radius: 12rpx;
  border: 1rpx solid #e0e0e0;
  box-sizing: border-box;
}

.text-input:focus {
  border-color: #4a90d9;
  background: #fff;
}

.text-counter {
  text-align: right;
  margin-top: 8rpx;
}

.text-counter text {
  font-size: 24rpx;
  color: #999;
}

/* 操作按钮 */
.action-buttons {
  display: flex;
  gap: 20rpx;
  margin-bottom: 20rpx;
}

.btn-synthesize {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  height: 88rpx;
  background: linear-gradient(135deg, #4a90d9, #6ab0f3);
  color: #fff;
  font-size: 30rpx;
  font-weight: 500;
  border-radius: 12rpx;
  border: none;
}

.btn-synthesize:active {
  opacity: 0.9;
}

.btn-synthesize.btn-disabled {
  background: #ccc;
  opacity: 0.6;
}

.btn-synthesize.btn-loading {
  background: linear-gradient(135deg, #7ab3e8, #9cc8f5);
}

.btn-clear {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 160rpx;
  height: 88rpx;
  background: #f5f5f5;
  color: #666;
  font-size: 28rpx;
  border-radius: 12rpx;
  border: none;
}

.btn-icon {
  margin-right: 8rpx;
}

/* 音频播放器 */
.audio-player {
  padding: 20rpx;
  background: #f5f7fa;
  border-radius: 12rpx;
  margin-bottom: 20rpx;
}

.player-controls {
  display: flex;
  align-items: center;
  gap: 20rpx;
}

.btn-play {
  width: 80rpx;
  height: 80rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #4a90d9, #6ab0f3);
  border-radius: 50%;
  border: none;
  padding: 0;
}

.play-icon {
  font-size: 36rpx;
}

.progress-container {
  flex: 1;
}

.time-display {
  display: flex;
  justify-content: space-between;
  margin-top: 8rpx;
}

.time-display text {
  font-size: 22rpx;
  color: #999;
}

/* 字幕容器 */
.subtitle-container {
  padding: 20rpx;
  background: rgba(74, 144, 217, 0.1);
  border-radius: 12rpx;
  text-align: center;
  margin-bottom: 20rpx;
}

.subtitle-text {
  font-size: 32rpx;
  color: #4a90d9;
  font-weight: 500;
}

/* 状态消息 */
.status-message {
  padding: 16rpx 20rpx;
  border-radius: 8rpx;
  text-align: center;
}

.status-message.info {
  background: #e6f7ff;
  color: #1890ff;
}

.status-message.success {
  background: #f6ffed;
  color: #52c41a;
}

.status-message.error {
  background: #fff2f0;
  color: #ff4d4f;
}

.status-message text {
  font-size: 26rpx;
}
</style>




