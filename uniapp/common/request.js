// 根据运行环境自动选择BASE_URL
// 在H5环境下使用localhost，在移动端使用本机IP
// 如果需要手动配置，请修改下面的配置
const getBaseUrl = () => {
  // 优先使用环境变量或本地存储的配置
  const storedUrl = uni.getStorageSync('API_BASE_URL');
  if (storedUrl) {
    return storedUrl;
  }
  
  // 检测运行平台
  // #ifdef H5
  return 'http://localhost:8080';
  // #endif
  
  // #ifndef H5
  // 移动端使用本机IP（请根据实际情况修改）
  // 开发环境：使用本机IP，例如：http://10.8.21.26:8080
  // 生产环境：使用实际的后端服务器地址
  return 'http://10.8.21.26:8080'; // 请根据实际情况修改此IP
  // #endif
};

const BASE_URL = getBaseUrl();

/**
 * 获取请求头，自动添加 userId 和 token
 */
function getRequestHeaders(customHeaders = {}) {
  // 获取存储的用户ID和token
  const userId = uni.getStorageSync('userId');
  const token = uni.getStorageSync('token');
  
  // 构建请求头
  const headers = {
    'Content-Type': 'application/json',
    ...customHeaders
  };
  
  // 如果存在 userId，添加到请求头
  if (userId) {
    headers['X-User-Id'] = String(userId);
  }
  
  // 如果存在 token，添加到请求头（Authorization）
  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }
  
  return headers;
}

/**
 * 封装的请求函数
 */
export function request(options) {
  return new Promise((resolve, reject) => {
    uni.request({
      url: BASE_URL + options.url,
      method: options.method || 'GET',
      data: options.data || {},
      header: getRequestHeaders(options.header),
      success: (res) => {
        resolve(res);
      },
      fail: (err) => {
        reject(err);
      }
    });
  });
}

/**
 * 封装的 uni.request，自动添加 userId 和 token
 * 用于替换直接使用 uni.request 的地方
 */
export function uniRequest(options) {
  // 自动添加请求头
  const headers = getRequestHeaders(options.header);
  
  return new Promise((resolve, reject) => {
    uni.request({
      ...options,
      url: options.url.startsWith('http') ? options.url : BASE_URL + options.url,
      header: headers,
      timeout: options.timeout || 300000, // 默认5分钟超时（AI生成可能需要较长时间）
      success: (res) => {
        resolve(res);
      },
      fail: (err) => {
        reject(err);
      }
    });
  });
}

export const API_BASE_URL = BASE_URL;


