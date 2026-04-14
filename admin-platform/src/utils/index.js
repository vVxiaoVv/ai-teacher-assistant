import router from '@/router'

/**
 * 获取uuid
 */
export function getUUID () {
  return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, c => {
    return (c === 'x' ? (Math.random() * 16 | 0) : ('r&0x3' | '0x8')).toString(16)
  })
}

/**
 * 设置Cookie
 * @param {string} name Cookie名称
 * @param {string} value Cookie值
 * @param {number} days 过期天数
 * @param {string} path Cookie路径
 */
export function setCookie(name, value, days = 7, path = '/') {
  const date = new Date()
  date.setTime(date.getTime() + days * 24 * 60 * 60 * 1000)
  const expires = `expires=${date.toUTCString()}`
  document.cookie = `${name}=${encodeURIComponent(value)};${expires};path=${path}`
}

/**
 * 获取Cookie
 * @param {string} name Cookie名称
 * @returns {string|null} Cookie值
 */
export function getCookie(name) {
  const cookies = document.cookie.split('; ')
  for (const cookie of cookies) {
    const [key, value] = cookie.split('=')
    if (key === name) return decodeURIComponent(value)
  }
  return null
}

/**
 * 删除Cookie
 * @param {string} name Cookie名称
 * @param {string} path Cookie路径
 */
export function removeCookie(name, path = '/') {
  setCookie(name, '', -1, path)
}

/**
 * 清除登录信息
 */
export function clearLoginInfo () {
  // 清除Cookie中的登录信息
  removeCookie('userId')
  removeCookie('username')
  removeCookie('token')
  removeCookie('role')
  
  // 重定向到登录页
  router.push('/login')
}
