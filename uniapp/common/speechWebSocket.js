/**
 * 语音识别 WebSocket 工具类
 * 通过后端代理连接语音服务（语音服务需要认证）
 */

// 后端 WebSocket 代理 URL
const WS_BASE_URL = 'ws://localhost:8080/ws/speech';
const APP_KEY = '300000775';  // 应用密钥

/**
 * 生成符合规范的ID（32位十六进制字符串，无中划线，小写）
 */
function generateId() {
  return 'xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx'.replace(/x/g, () => 
    Math.floor(Math.random() * 16).toString(16)
  );
}

/**
 * 语音识别 WebSocket 类
 */
class SpeechWebSocket {
  constructor(options = {}) {
    this.socketTask = null;
    this.isConnected = false;
    this.taskId = null;
    this.appKey = options.appKey || APP_KEY;
    
    // 回调函数
    this.onConnected = options.onConnected || (() => {});
    this.onDisconnected = options.onDisconnected || (() => {});
    this.onRecognitionStarted = options.onRecognitionStarted || (() => {});
    this.onRecognitionResult = options.onRecognitionResult || (() => {});
    this.onRecognitionCompleted = options.onRecognitionCompleted || (() => {});
    this.onError = options.onError || (() => {});
    
    // 重连配置
    this.reconnectAttempts = 0;
    this.maxReconnectAttempts = options.maxReconnectAttempts || 3;
    this.reconnectInterval = options.reconnectInterval || 2000;
    
    // Promise 相关
    this._connectResolve = null;
    this._connectReject = null;
    
    // 状态标记
    this.isStopping = false; // 是否正在停止识别
    this.isStarting = false; // 是否正在开始识别
  }
  
  /**
   * 连接 WebSocket
   */
  connect() {
    return new Promise((resolve, reject) => {
      if (this.isConnected) {
        console.log('[SpeechWS] 已连接，无需重复连接');
        resolve();
        return;
      }
      
      this._connectResolve = resolve;
      this._connectReject = reject;
      
      // 生成 taskId
      this.taskId = generateId();
      
      const url = WS_BASE_URL;
      console.log('[SpeechWS] 正在连接后端语音代理:', url);
      console.log('[SpeechWS] taskId:', this.taskId);
      
      try {
        this.socketTask = uni.connectSocket({
          url: url,
          header: {
            'content-type': 'application/json'
          },
          success: () => {
            console.log('[SpeechWS] WebSocket 连接请求发送成功');
          },
          fail: (err) => {
            console.error('[SpeechWS] WebSocket 连接请求失败:', err);
            this._connectReject && this._connectReject(err);
          }
        });
        
        // 绑定事件监听
        this._bindSocketEvents();
        
      } catch (e) {
        console.error('[SpeechWS] 创建 WebSocket 连接异常:', e);
        reject(e);
      }
      
      // 设置超时
      setTimeout(() => {
        if (!this.isConnected && this._connectReject) {
          console.error('[SpeechWS] 连接超时');
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
      console.error('[SpeechWS] socketTask 为空，尝试使用全局事件');
      this._bindGlobalSocketEvents();
      return;
    }
    
    // 监听连接打开
    this.socketTask.onOpen((res) => {
      console.log('[SpeechWS] WebSocket 连接已打开', res);
      this.isConnected = true;
      this.reconnectAttempts = 0;
      
      // 连接打开后，通知前端已连接
      const connectedData = {
        type: 'connected',
        taskId: this.taskId,
        message: '已连接到语音服务'
      };
      this.onConnected(connectedData);
      
      // resolve promise
      if (this._connectResolve) {
        this._connectResolve(connectedData);
        this._connectResolve = null;
        this._connectReject = null;
      }
    });
    
    // 监听消息
    this.socketTask.onMessage((res) => {
      console.log('[SpeechWS] 收到原始消息:', res.data);
      this.handleMessage(res.data);
    });
    
    // 监听关闭
    this.socketTask.onClose((res) => {
      console.log('[SpeechWS] WebSocket 连接已关闭:', res);
      this.isConnected = false;
      this.onDisconnected(res);
    });
    
    // 监听错误
    this.socketTask.onError((err) => {
      console.error('[SpeechWS] WebSocket 错误:', err);
      this.isConnected = false;
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
      console.log('[SpeechWS] [Global] WebSocket 连接已打开', res);
      this.isConnected = true;
      this.reconnectAttempts = 0;
      
      const connectedData = {
        type: 'connected',
        taskId: this.taskId,
        message: '已连接到语音服务'
      };
      this.onConnected(connectedData);
      
      if (this._connectResolve) {
        this._connectResolve(connectedData);
        this._connectResolve = null;
        this._connectReject = null;
      }
    });
    
    uni.onSocketMessage((res) => {
      console.log('[SpeechWS] [Global] 收到消息:', res.data);
      this.handleMessage(res.data);
    });
    
    uni.onSocketClose((res) => {
      console.log('[SpeechWS] [Global] WebSocket 连接已关闭:', res);
      this.isConnected = false;
      this.onDisconnected(res);
    });
    
    uni.onSocketError((err) => {
      console.error('[SpeechWS] [Global] WebSocket 错误:', err);
      this.isConnected = false;
      this.onError(err);
      if (this._connectReject) {
        this._connectReject(err);
        this._connectReject = null;
        this._connectResolve = null;
      }
    });
  }
  
  /**
   * 处理接收到的消息
   */
  handleMessage(data) {
    try {
      const message = typeof data === 'string' ? JSON.parse(data) : data;
      console.log('[SpeechWS] 解析消息:', message);
      
      // 后端代理返回的消息格式: { type, success, result, rawResult, statusText, message }
      const eventType = message.type;
      const success = message.success;
      // 优先使用 rawResult，如果不存在则使用 result
      const rawResult = message.rawResult || '';
      const result = message.result || '';
      const finalResult = (rawResult && rawResult.trim()) ? rawResult : result;
      const statusText = message.statusText || message.message || '';
      
      console.log('[SpeechWS] 事件类型:', eventType, '成功:', success);
      console.log('[SpeechWS] rawResult:', rawResult, 'result:', result, 'finalResult:', finalResult);
      
      switch (eventType) {
        case 'connected':
          console.log('[SpeechWS] 收到连接成功消息');
          // 连接成功消息会在 onOpen 中处理
          break;
          
        case 'disconnected':
          console.log('[SpeechWS] 收到断开连接消息');
          this.onDisconnected({ message: statusText });
          break;
          
        case 'RecognitionStarted':
          this.isStarting = false; // 重置开始状态
          this.isStopping = false; // 重置停止状态
          this.onRecognitionStarted({
            type: eventType,
            success: success,
            statusText: statusText
          });
          break;
          
        case 'RecognitionResultChanged':
        case 'SentenceBegin':
        case 'SentenceEnd':
          // 中间识别结果
          this.onRecognitionResult({
            type: eventType,
            result: finalResult,
            rawResult: rawResult,
            success: success
          });
          break;
          
        case 'RecognitionCompleted':
          this.isStopping = false; // 重置停止状态
          this.isStarting = false; // 重置开始状态
          this.onRecognitionCompleted({
            type: eventType,
            result: finalResult,
            rawResult: rawResult,
            success: success,
            statusText: statusText
          });
          break;
          
        case 'TaskFailed':
        case 'error':
          // 如果是任务状态错误（如重复停止），重置状态但不显示错误提示
          if (statusText && statusText.includes('TASK_STATE_ERROR')) {
            console.warn('[SpeechWS] 任务状态错误（可能是重复操作）:', statusText);
            this.isStopping = false;
            this.isStarting = false;
            // 不触发错误回调，避免干扰用户体验
            return;
          }
          
          this.isStopping = false; // 重置停止状态
          this.isStarting = false; // 重置开始状态
          this.onError({
            type: eventType,
            message: statusText || '任务失败'
          });
          break;
          
        default:
          console.log('[SpeechWS] 未知消息类型:', eventType, message);
      }
    } catch (e) {
      console.error('[SpeechWS] 解析消息失败:', e, data);
    }
  }
  
  /**
   * 构建 StartRecognition 指令
   */
  _buildStartRecognitionMessage() {
    return {
      header: {
        message_id: generateId(),
        task_id: this.taskId,
        namespace: 'SpeechRecognizer',
        name: 'StartRecognition',
        appkey: this.appKey
      },
      payload: {
        cluster: 'aliyun',
        format: 'pcm'
      },
      context: {
        sdk: {
          name: 'moke-uniapp-sdk',
          version: '1.0.0',
          language: 'javascript'
        },
        app: {},
        system: {},
        device: {},
        network: {},
        geography: {},
        bridge: {},
        custom: {}
      }
    };
  }
  
  /**
   * 构建 StopRecognition 指令
   */
  _buildStopRecognitionMessage() {
    return {
      header: {
        message_id: generateId(),
        task_id: this.taskId,
        namespace: 'SpeechRecognizer',
        name: 'StopRecognition',
        appkey: this.appKey
      },
      context: {
        sdk: {
          name: 'moke-uniapp-sdk',
          version: '1.0.0',
          language: 'javascript'
        }
      }
    };
  }
  
  /**
   * 发送消息
   */
  _send(data) {
    return new Promise((resolve, reject) => {
      if (!this.isConnected) {
        console.warn('[SpeechWS] WebSocket 未连接，无法发送消息');
        reject(new Error('WebSocket 未连接'));
        return;
      }
      
      const sendData = typeof data === 'string' ? data : JSON.stringify(data);
      console.log('[SpeechWS] 发送消息:', sendData);
      
      if (this.socketTask) {
        this.socketTask.send({
          data: sendData,
          success: () => {
            console.log('[SpeechWS] 消息发送成功');
            resolve();
          },
          fail: (err) => {
            console.error('[SpeechWS] 消息发送失败:', err);
            reject(err);
          }
        });
      } else {
        uni.sendSocketMessage({
          data: sendData,
          success: () => {
            console.log('[SpeechWS] [Global] 消息发送成功');
            resolve();
          },
          fail: (err) => {
            console.error('[SpeechWS] [Global] 消息发送失败:', err);
            reject(err);
          }
        });
      }
    });
  }
  
  /**
   * 发送开始识别指令
   */
  startRecognition() {
    if (!this.isConnected) {
      console.warn('[SpeechWS] WebSocket 未连接，无法开始识别');
      return false;
    }
    
    if (this.isStarting) {
      console.warn('[SpeechWS] 正在开始识别，避免重复发送');
      return false;
    }
    
    this.isStarting = true;
    
    // 发送简单的 action 消息给后端代理
    const message = { action: 'start' };
    console.log('[SpeechWS] 发送 start 指令:', message);
    
    this._send(message).then(() => {
      // 发送成功后，等待 RecognitionStarted 事件来重置状态
      setTimeout(() => {
        this.isStarting = false;
      }, 1000);
    }).catch(err => {
      console.error('[SpeechWS] 发送开始识别指令失败:', err);
      this.isStarting = false;
    });
    
    return true;
  }
  
  /**
   * 发送停止识别指令
   */
  stopRecognition() {
    if (!this.isConnected) {
      console.warn('[SpeechWS] WebSocket 未连接，无法停止识别');
      return false;
    }
    
    // 如果正在停止，避免重复发送
    if (this.isStopping) {
      console.warn('[SpeechWS] 正在停止识别，避免重复发送停止指令');
      return false;
    }
    
    this.isStopping = true;
    
    // 发送简单的 action 消息给后端代理
    const message = { action: 'stop' };
    console.log('[SpeechWS] 发送 stop 指令:', message);
    
    this._send(message).then(() => {
      // 发送成功后，等待 RecognitionCompleted 或错误事件来重置状态
      setTimeout(() => {
        this.isStopping = false;
      }, 2000);
    }).catch(err => {
      console.error('[SpeechWS] 发送停止识别指令失败:', err);
      this.isStopping = false;
    });
    
    return true;
  }
  
  /**
   * 发送音频数据
   * @param {ArrayBuffer} audioData PCM音频数据
   */
  sendAudioData(audioData) {
    if (!this.isConnected) {
      console.warn('[SpeechWS] WebSocket 未连接，无法发送音频');
      return false;
    }
    
    if (this.socketTask) {
      this.socketTask.send({
        data: audioData,
        success: () => {
          // 音频发送成功，不打印日志避免刷屏
        },
        fail: (err) => {
          console.error('[SpeechWS] 发送音频数据失败:', err);
        }
      });
    } else {
      uni.sendSocketMessage({
        data: audioData,
        success: () => {},
        fail: (err) => {
          console.error('[SpeechWS] [Global] 发送音频数据失败:', err);
        }
      });
    }
    
    return true;
  }
  
  /**
   * 关闭连接
   */
  close() {
    console.log('[SpeechWS] 关闭连接');
    
    if (this.socketTask) {
      this.socketTask.close({
        success: () => {
          console.log('[SpeechWS] WebSocket 关闭成功');
        },
        fail: (err) => {
          console.error('[SpeechWS] WebSocket 关闭失败:', err);
        }
      });
      this.socketTask = null;
    } else {
      uni.closeSocket({
        success: () => {
          console.log('[SpeechWS] [Global] WebSocket 关闭成功');
        },
        fail: (err) => {
          console.error('[SpeechWS] [Global] WebSocket 关闭失败:', err);
        }
      });
    }
    
    this.isConnected = false;
    this.taskId = null;
    this._connectResolve = null;
    this._connectReject = null;
  }
  
  /**
   * 重连
   */
  reconnect() {
    if (this.reconnectAttempts >= this.maxReconnectAttempts) {
      console.error('[SpeechWS] 达到最大重连次数');
      this.onError({ message: '无法连接到语音服务' });
      return;
    }
    
    this.reconnectAttempts++;
    console.log(`[SpeechWS] 尝试重连 (${this.reconnectAttempts}/${this.maxReconnectAttempts})...`);
    
    setTimeout(() => {
      this.connect().catch((err) => {
        console.error('[SpeechWS] 重连失败:', err);
        this.reconnect();
      });
    }, this.reconnectInterval);
  }
}

export { SpeechWebSocket, generateId, WS_BASE_URL, APP_KEY };
