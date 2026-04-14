/**
 * 语音合成 WebSocket 工具类
 * 通过后端代理连接语音服务，实现文本转语音功能
 */

// 后端 WebSocket TTS 代理 URL
const WS_TTS_URL = 'ws://localhost:8080/ws/tts';

/**
 * 生成符合规范的ID（32位十六进制字符串，无中划线，小写）
 */
function generateId() {
  return 'xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx'.replace(/x/g, () => 
    Math.floor(Math.random() * 16).toString(16)
  );
}

/**
 * 语音合成 WebSocket 类
 */
class SpeechSynthesisWebSocket {
  constructor(options = {}) {
    this.socketTask = null;
    this.isConnected = false;
    this.taskId = null;
    this.isSynthesizing = false;
    
    // 回调函数
    this.onConnected = options.onConnected || (() => {});
    this.onDisconnected = options.onDisconnected || (() => {});
    this.onAudioPlay = options.onAudioPlay || (() => {});       // 音频参数事件
    this.onAudioData = options.onAudioData || (() => {});       // 音频数据（二进制）
    this.onMetaInfo = options.onMetaInfo || (() => {});         // 时间戳/字幕信息
    this.onCompleted = options.onCompleted || (() => {});       // 合成完成
    this.onError = options.onError || (() => {});
    
    // 重连配置
    this.reconnectAttempts = 0;
    this.maxReconnectAttempts = options.maxReconnectAttempts || 3;
    this.reconnectInterval = options.reconnectInterval || 2000;
    
    // Promise 相关
    this._connectResolve = null;
    this._connectReject = null;
  }
  
  /**
   * 连接 WebSocket
   */
  connect() {
    return new Promise((resolve, reject) => {
      if (this.isConnected) {
        console.log('[TTS-WS] 已连接，无需重复连接');
        resolve();
        return;
      }
      
      this._connectResolve = resolve;
      this._connectReject = reject;
      
      // 生成 taskId
      this.taskId = generateId();
      
      const url = WS_TTS_URL;
      console.log('[TTS-WS] 正在连接后端TTS代理:', url);
      console.log('[TTS-WS] taskId:', this.taskId);
      
      try {
        this.socketTask = uni.connectSocket({
          url: url,
          header: {
            'content-type': 'application/json'
          },
          success: () => {
            console.log('[TTS-WS] WebSocket 连接请求发送成功');
          },
          fail: (err) => {
            console.error('[TTS-WS] WebSocket 连接请求失败:', err);
            this._connectReject && this._connectReject(err);
          }
        });
        
        // 绑定事件监听
        this._bindSocketEvents();
        
      } catch (e) {
        console.error('[TTS-WS] 创建 WebSocket 连接异常:', e);
        reject(e);
      }
      
      // 设置超时
      setTimeout(() => {
        if (!this.isConnected && this._connectReject) {
          console.error('[TTS-WS] 连接超时');
          this._connectReject(new Error('连接超时'));
          this._connectReject = null;
          this._connectResolve = null;
        }
      }, 15000);
    });
  }
  
  /**
   * 绑定 Socket 事件
   */
  _bindSocketEvents() {
    if (!this.socketTask) {
      console.error('[TTS-WS] socketTask 为空，尝试使用全局事件');
      this._bindGlobalSocketEvents();
      return;
    }
    
    // 监听连接打开
    this.socketTask.onOpen((res) => {
      console.log('[TTS-WS] WebSocket 连接已打开', res);
      this.isConnected = true;
      this.reconnectAttempts = 0;
      
      // 连接打开后，通知已连接
      const connectedData = {
        type: 'connected',
        taskId: this.taskId,
        message: '已连接到语音合成服务'
      };
      this.onConnected(connectedData);
      
      // resolve promise
      if (this._connectResolve) {
        this._connectResolve(connectedData);
        this._connectResolve = null;
        this._connectReject = null;
      }
    });
    
    // 监听文本消息
    this.socketTask.onMessage((res) => {
      // 判断是二进制还是文本
      if (res.data instanceof ArrayBuffer) {
        // 二进制音频数据
        console.log('[TTS-WS] 收到音频数据, 大小:', res.data.byteLength);
        this.onAudioData(res.data);
      } else {
        // 文本消息（事件）
        console.log('[TTS-WS] 收到文本消息:', res.data);
        this.handleMessage(res.data);
      }
    });
    
    // 监听关闭
    this.socketTask.onClose((res) => {
      console.log('[TTS-WS] WebSocket 连接已关闭:', res);
      this.isConnected = false;
      this.isSynthesizing = false;
      this.onDisconnected(res);
    });
    
    // 监听错误
    this.socketTask.onError((err) => {
      console.error('[TTS-WS] WebSocket 错误:', err);
      this.isConnected = false;
      this.isSynthesizing = false;
      this.onError(err);
      if (this._connectReject) {
        this._connectReject(err);
        this._connectReject = null;
        this._connectResolve = null;
      }
    });
  }
  
  /**
   * 使用全局事件监听（备选方案）
   */
  _bindGlobalSocketEvents() {
    uni.onSocketOpen((res) => {
      console.log('[TTS-WS] [Global] WebSocket 连接已打开', res);
      this.isConnected = true;
      this.reconnectAttempts = 0;
      
      const connectedData = {
        type: 'connected',
        taskId: this.taskId,
        message: '已连接到语音合成服务'
      };
      this.onConnected(connectedData);
      
      if (this._connectResolve) {
        this._connectResolve(connectedData);
        this._connectResolve = null;
        this._connectReject = null;
      }
    });
    
    uni.onSocketMessage((res) => {
      if (res.data instanceof ArrayBuffer) {
        console.log('[TTS-WS] [Global] 收到音频数据, 大小:', res.data.byteLength);
        this.onAudioData(res.data);
      } else {
        console.log('[TTS-WS] [Global] 收到文本消息:', res.data);
        this.handleMessage(res.data);
      }
    });
    
    uni.onSocketClose((res) => {
      console.log('[TTS-WS] [Global] WebSocket 连接已关闭:', res);
      this.isConnected = false;
      this.isSynthesizing = false;
      this.onDisconnected(res);
    });
    
    uni.onSocketError((err) => {
      console.error('[TTS-WS] [Global] WebSocket 错误:', err);
      this.isConnected = false;
      this.isSynthesizing = false;
      this.onError(err);
      if (this._connectReject) {
        this._connectReject(err);
        this._connectReject = null;
        this._connectResolve = null;
      }
    });
  }
  
  /**
   * 处理接收到的文本消息
   */
  handleMessage(data) {
    try {
      const message = typeof data === 'string' ? JSON.parse(data) : data;
      console.log('[TTS-WS] 解析消息:', message);
      
      const eventType = message.type;
      const success = message.success;
      
      console.log('[TTS-WS] 事件类型:', eventType, '成功:', success);
      
      switch (eventType) {
        case 'connected':
          console.log('[TTS-WS] 收到连接成功消息');
          break;
          
        case 'disconnected':
          console.log('[TTS-WS] 收到断开连接消息');
          this.isSynthesizing = false;
          this.onDisconnected({ message: message.statusText || message.message });
          break;
          
        case 'AudioPlay':
          // 音频参数事件（PCM首包）
          console.log('[TTS-WS] AudioPlay事件, format:', message.format, 'sampleRate:', message.sampleRate);
          this.onAudioPlay({
            type: eventType,
            format: message.format,
            sampleRate: message.sampleRate,
            success: success
          });
          break;
          
        case 'MetaInfo':
          // 时间戳/字幕信息
          console.log('[TTS-WS] MetaInfo事件, wordBoundary:', message.wordBoundary);
          this.onMetaInfo({
            type: eventType,
            wordBoundary: message.wordBoundary,
            success: success
          });
          break;
          
        case 'SynthesisCompleted':
          // 合成完成
          console.log('[TTS-WS] 合成完成');
          this.isSynthesizing = false;
          this.onCompleted({
            type: eventType,
            success: success,
            statusText: message.statusText
          });
          break;
          
        case 'TaskFailed':
        case 'error':
          // 合成失败
          console.error('[TTS-WS] 合成失败:', message.statusText || message.message);
          this.isSynthesizing = false;
          this.onError({
            type: eventType,
            message: message.statusText || message.message || '合成失败'
          });
          break;
          
        default:
          console.log('[TTS-WS] 未知消息类型:', eventType, message);
      }
    } catch (e) {
      console.error('[TTS-WS] 解析消息失败:', e, data);
    }
  }
  
  /**
   * 发送消息
   */
  _send(data) {
    return new Promise((resolve, reject) => {
      if (!this.isConnected) {
        console.warn('[TTS-WS] WebSocket 未连接，无法发送消息');
        reject(new Error('WebSocket 未连接'));
        return;
      }
      
      const sendData = typeof data === 'string' ? data : JSON.stringify(data);
      console.log('[TTS-WS] 发送消息:', sendData);
      
      if (this.socketTask) {
        this.socketTask.send({
          data: sendData,
          success: () => {
            console.log('[TTS-WS] 消息发送成功');
            resolve();
          },
          fail: (err) => {
            console.error('[TTS-WS] 消息发送失败:', err);
            reject(err);
          }
        });
      } else {
        uni.sendSocketMessage({
          data: sendData,
          success: () => {
            console.log('[TTS-WS] [Global] 消息发送成功');
            resolve();
          },
          fail: (err) => {
            console.error('[TTS-WS] [Global] 消息发送失败:', err);
            reject(err);
          }
        });
      }
    });
  }
  
  /**
   * 开始语音合成
   * @param {string} text 待合成的文本
   * @param {string} voice 语音角色（可选）
   * @param {boolean} longText 是否为长文本流式合成（可选）
   */
  synthesize(text, voice = null, longText = false) {
    if (!this.isConnected) {
      console.warn('[TTS-WS] WebSocket 未连接，无法开始合成');
      return false;
    }
    
    if (this.isSynthesizing) {
      console.warn('[TTS-WS] 正在合成中，请等待完成');
      return false;
    }
    
    if (!text || text.trim() === '') {
      console.warn('[TTS-WS] 合成文本不能为空');
      return false;
    }
    
    this.isSynthesizing = true;
    
    // 发送合成指令
    const message = {
      action: 'synthesize',
      text: text,
      voice: voice,
      longText: longText
    };
    console.log('[TTS-WS] 发送合成指令:', message);
    
    this._send(message).catch(err => {
      console.error('[TTS-WS] 发送合成指令失败:', err);
      this.isSynthesizing = false;
    });
    
    return true;
  }
  
  /**
   * 关闭连接
   */
  close() {
    console.log('[TTS-WS] 关闭连接');
    
    if (this.socketTask) {
      this.socketTask.close({
        success: () => {
          console.log('[TTS-WS] WebSocket 关闭成功');
        },
        fail: (err) => {
          console.error('[TTS-WS] WebSocket 关闭失败:', err);
        }
      });
      this.socketTask = null;
    } else {
      uni.closeSocket({
        success: () => {
          console.log('[TTS-WS] [Global] WebSocket 关闭成功');
        },
        fail: (err) => {
          console.error('[TTS-WS] [Global] WebSocket 关闭失败:', err);
        }
      });
    }
    
    this.isConnected = false;
    this.isSynthesizing = false;
    this.taskId = null;
    this._connectResolve = null;
    this._connectReject = null;
  }
  
  /**
   * 重连
   */
  reconnect() {
    if (this.reconnectAttempts >= this.maxReconnectAttempts) {
      console.error('[TTS-WS] 达到最大重连次数');
      this.onError({ message: '无法连接到语音合成服务' });
      return;
    }
    
    this.reconnectAttempts++;
    console.log(`[TTS-WS] 尝试重连 (${this.reconnectAttempts}/${this.maxReconnectAttempts})...`);
    
    setTimeout(() => {
      this.connect().catch((err) => {
        console.error('[TTS-WS] 重连失败:', err);
        this.reconnect();
      });
    }, this.reconnectInterval);
  }
}

export { SpeechSynthesisWebSocket, generateId, WS_TTS_URL };




