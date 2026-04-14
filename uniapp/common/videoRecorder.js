/**
 * 视频录制工具类
 * 支持 H5 和 原生小程序 双平台
 */

class VideoRecorder {
  constructor(options = {}) {
    this.options = options;
    this.isRecording = false;
    this.isPaused = false;
    this.recordedChunks = [];
    this.startTime = null;
    this.duration = 0;
    this.durationTimer = null;
    
    // H5 相关
    this.mediaStream = null;
    this.mediaRecorder = null;
    this.videoElement = null;
    
    // 小程序相关
    this.cameraContext = null;
    this.tempFilePath = null;
    
    // 回调函数
    this.onStart = options.onStart || (() => {});
    this.onStop = options.onStop || (() => {});
    this.onPause = options.onPause || (() => {});
    this.onResume = options.onResume || (() => {});
    this.onError = options.onError || (() => {});
    this.onDurationUpdate = options.onDurationUpdate || (() => {});
  }
  
  /**
   * 初始化 H5 摄像头预览
   * @param {HTMLElement|string} containerOrId - 容器元素或容器ID
   * @param {string} facingMode - 摄像头方向: 'user'(前置) 或 'environment'(后置)
   */
  async initH5Camera(containerOrId, facingMode = 'user') {
    try {
      // 获取容器元素
      let container;
      if (typeof containerOrId === 'string') {
        container = document.getElementById(containerOrId);
      } else {
        container = containerOrId;
      }
      
      if (!container) {
        throw new Error('找不到视频容器元素');
      }
      
      // 创建原生 video 元素
      this.videoElement = document.createElement('video');
      this.videoElement.setAttribute('autoplay', '');
      this.videoElement.setAttribute('playsinline', '');
      this.videoElement.setAttribute('muted', '');
      this.videoElement.style.cssText = 'width: 100%; height: 100%; object-fit: cover; background: #000;';
      
      // 清空容器并添加 video 元素
      container.innerHTML = '';
      container.appendChild(this.videoElement);
      
      const constraints = {
        video: {
          facingMode: facingMode,
          width: { ideal: 1280 },
          height: { ideal: 720 }
        },
        audio: true
      };
      
      console.log('[VideoRecorder] 正在请求摄像头权限...');
      this.mediaStream = await navigator.mediaDevices.getUserMedia(constraints);
      console.log('[VideoRecorder] 获取到媒体流:', this.mediaStream);
      
      this.videoElement.srcObject = this.mediaStream;
      
      // 使用 Promise 包装 play
      await new Promise((resolve, reject) => {
        this.videoElement.onloadedmetadata = () => {
          this.videoElement.play()
            .then(resolve)
            .catch(reject);
        };
        this.videoElement.onerror = reject;
        // 超时处理
        setTimeout(() => resolve(), 3000);
      });
      
      console.log('[VideoRecorder] H5 摄像头初始化成功');
      return true;
    } catch (err) {
      console.error('[VideoRecorder] H5 摄像头初始化失败:', err);
      this.onError({ type: 'init', message: err.message || '摄像头初始化失败' });
      return false;
    }
  }
  
  /**
   * 切换 H5 摄像头
   * @param {string} containerId - 容器ID
   * @param {string} facingMode - 摄像头方向
   */
  async switchH5Camera(containerId, facingMode) {
    if (this.isRecording) {
      console.warn('[VideoRecorder] 录制中无法切换摄像头');
      return false;
    }
    
    // 停止当前流
    this.stopH5Stream();
    
    // 重新初始化
    return await this.initH5Camera(containerId, facingMode);
  }
  
  /**
   * 停止 H5 媒体流
   */
  stopH5Stream() {
    if (this.mediaStream) {
      this.mediaStream.getTracks().forEach(track => track.stop());
      this.mediaStream = null;
    }
  }
  
  /**
   * 开始 H5 录制
   */
  startH5Recording() {
    if (!this.mediaStream) {
      console.error('[VideoRecorder] 媒体流未初始化');
      return false;
    }
    
    try {
      // 确定支持的 MIME 类型
      const mimeTypes = [
        'video/webm;codecs=vp9,opus',
        'video/webm;codecs=vp8,opus',
        'video/webm',
        'video/mp4'
      ];
      
      let selectedMimeType = '';
      for (const mimeType of mimeTypes) {
        if (MediaRecorder.isTypeSupported(mimeType)) {
          selectedMimeType = mimeType;
          break;
        }
      }
      
      if (!selectedMimeType) {
        throw new Error('浏览器不支持视频录制');
      }
      
      this.recordedChunks = [];
      this.mediaRecorder = new MediaRecorder(this.mediaStream, {
        mimeType: selectedMimeType
      });
      
      this.mediaRecorder.ondataavailable = (event) => {
        if (event.data && event.data.size > 0) {
          this.recordedChunks.push(event.data);
        }
      };
      
      this.mediaRecorder.onstop = () => {
        this._stopDurationTimer();
        this.isRecording = false;
        
        // 创建视频 Blob
        const blob = new Blob(this.recordedChunks, { type: selectedMimeType });
        const url = URL.createObjectURL(blob);
        
        this.onStop({
          tempFilePath: url,
          duration: this.duration,
          size: blob.size,
          blob: blob
        });
      };
      
      this.mediaRecorder.onerror = (event) => {
        console.error('[VideoRecorder] 录制错误:', event);
        this.onError({ type: 'recording', message: '录制失败' });
      };
      
      this.mediaRecorder.start(1000); // 每秒收集一次数据
      this.isRecording = true;
      this.isPaused = false;
      this.startTime = Date.now();
      this._startDurationTimer();
      
      this.onStart();
      console.log('[VideoRecorder] H5 录制开始');
      return true;
    } catch (err) {
      console.error('[VideoRecorder] 开始录制失败:', err);
      this.onError({ type: 'start', message: err.message || '开始录制失败' });
      return false;
    }
  }
  
  /**
   * 停止 H5 录制
   */
  stopH5Recording() {
    if (this.mediaRecorder && this.isRecording) {
      this.mediaRecorder.stop();
      console.log('[VideoRecorder] H5 录制停止');
    }
  }
  
  /**
   * 暂停 H5 录制
   */
  pauseH5Recording() {
    if (this.mediaRecorder && this.isRecording && !this.isPaused) {
      this.mediaRecorder.pause();
      this.isPaused = true;
      this._stopDurationTimer();
      this.onPause();
      console.log('[VideoRecorder] H5 录制暂停');
    }
  }
  
  /**
   * 恢复 H5 录制
   */
  resumeH5Recording() {
    if (this.mediaRecorder && this.isPaused) {
      this.mediaRecorder.resume();
      this.isPaused = false;
      this._startDurationTimer();
      this.onResume();
      console.log('[VideoRecorder] H5 录制恢复');
    }
  }
  
  /**
   * 初始化小程序相机
   * @param {string} cameraId - camera 组件的 id
   */
  initMiniProgramCamera(cameraId) {
    // #ifndef H5
    this.cameraContext = uni.createCameraContext();
    console.log('[VideoRecorder] 小程序相机初始化成功');
    return true;
    // #endif
    // #ifdef H5
    return false;
    // #endif
  }
  
  /**
   * 开始小程序录制
   */
  startMiniProgramRecording() {
    // #ifndef H5
    if (!this.cameraContext) {
      console.error('[VideoRecorder] 相机上下文未初始化');
      return false;
    }
    
    this.cameraContext.startRecord({
      timeoutCallback: (res) => {
        // 录制超过最大时长
        this.onStop({
          tempFilePath: res.tempVideoPath,
          tempThumbPath: res.tempThumbPath,
          duration: this.duration
        });
      },
      success: () => {
        this.isRecording = true;
        this.isPaused = false;
        this.startTime = Date.now();
        this._startDurationTimer();
        this.onStart();
        console.log('[VideoRecorder] 小程序录制开始');
      },
      fail: (err) => {
        console.error('[VideoRecorder] 小程序录制失败:', err);
        this.onError({ type: 'start', message: err.errMsg || '开始录制失败' });
      }
    });
    return true;
    // #endif
    // #ifdef H5
    return false;
    // #endif
  }
  
  /**
   * 停止小程序录制
   */
  stopMiniProgramRecording() {
    // #ifndef H5
    if (this.cameraContext && this.isRecording) {
      this.cameraContext.stopRecord({
        success: (res) => {
          this._stopDurationTimer();
          this.isRecording = false;
          this.tempFilePath = res.tempVideoPath;
          this.onStop({
            tempFilePath: res.tempVideoPath,
            tempThumbPath: res.tempThumbPath,
            duration: this.duration
          });
          console.log('[VideoRecorder] 小程序录制停止');
        },
        fail: (err) => {
          console.error('[VideoRecorder] 停止录制失败:', err);
          this.onError({ type: 'stop', message: err.errMsg || '停止录制失败' });
        }
      });
    }
    // #endif
  }
  
  /**
   * 开始计时
   */
  _startDurationTimer() {
    this.durationTimer = setInterval(() => {
      this.duration = Math.floor((Date.now() - this.startTime) / 1000);
      this.onDurationUpdate(this.duration);
    }, 1000);
  }
  
  /**
   * 停止计时
   */
  _stopDurationTimer() {
    if (this.durationTimer) {
      clearInterval(this.durationTimer);
      this.durationTimer = null;
    }
  }
  
  /**
   * 格式化时长
   * @param {number} seconds - 秒数
   */
  static formatDuration(seconds) {
    const mins = Math.floor(seconds / 60);
    const secs = seconds % 60;
    return `${String(mins).padStart(2, '0')}:${String(secs).padStart(2, '0')}`;
  }
  
  /**
   * 保存视频到相册（小程序）
   * @param {string} filePath - 视频文件路径
   */
  static saveToAlbum(filePath) {
    return new Promise((resolve, reject) => {
      // #ifndef H5
      uni.saveVideoToPhotosAlbum({
        filePath: filePath,
        success: () => {
          uni.showToast({
            title: '已保存到相册',
            icon: 'success'
          });
          resolve(true);
        },
        fail: (err) => {
          console.error('[VideoRecorder] 保存失败:', err);
          if (err.errMsg && err.errMsg.includes('auth deny')) {
            uni.showToast({
              title: '请授权相册权限',
              icon: 'none'
            });
          } else {
            uni.showToast({
              title: '保存失败',
              icon: 'none'
            });
          }
          reject(err);
        }
      });
      // #endif
      // #ifdef H5
      // H5 使用下载方式
      const a = document.createElement('a');
      a.href = filePath;
      a.download = `virtual_classroom_${Date.now()}.webm`;
      document.body.appendChild(a);
      a.click();
      document.body.removeChild(a);
      resolve(true);
      // #endif
    });
  }
  
  /**
   * 清理资源
   */
  destroy() {
    this._stopDurationTimer();
    this.stopH5Stream();
    
    if (this.mediaRecorder) {
      if (this.isRecording) {
        this.mediaRecorder.stop();
      }
      this.mediaRecorder = null;
    }
    
    this.recordedChunks = [];
    this.isRecording = false;
    this.isPaused = false;
    this.duration = 0;
    
    console.log('[VideoRecorder] 资源已清理');
  }
}

export { VideoRecorder };

