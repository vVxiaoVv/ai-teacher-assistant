# Cookie 调试指南

## 检查浏览器 Cookie 设置

### 1. 打开浏览器开发者工具
- 按 `F12` 或 `Cmd+Option+I` (Mac) / `Ctrl+Shift+I` (Windows)
- 切换到 **Application** 标签（Chrome/Edge）或 **Storage** 标签（Firefox）

### 2. 检查 Cookie
- 左侧导航栏找到 **Cookies**
- 展开 `http://localhost:5173` 或 `http://localhost:8080`
- 查看是否有以下 Cookie：
  - `token` - 应该存在且值不为空
  - `userId` - 应该存在且值不为空
  - `username` - 应该存在
  - `role` - 应该存在

### 3. 检查 Cookie 属性
每个 Cookie 应该有以下属性：
- **Name**: token / userId / username / role
- **Value**: 对应的值
- **Domain**: localhost
- **Path**: /
- **Expires**: 应该显示过期时间（24小时后）
- **HttpOnly**: token 应该是 true，userId 应该是 false
- **SameSite**: Lax

### 4. 检查浏览器控制台
打开 **Console** 标签，查看登录时的日志：
- `登录响应完整数据:` - 查看后端返回的数据
- `设置token Cookie` - 确认是否尝试设置
- `立即检查token` / `延迟检查token` - 查看 Cookie 是否可读
- `当前所有Cookie:` - 查看所有 Cookie 内容

## 常见问题

### 问题1: Cookie 被阻止
**原因**: 浏览器安全策略阻止了 Cookie 设置

**解决方案**:
1. 检查浏览器是否启用了"阻止第三方 Cookie"
   - Chrome: 设置 → 隐私和安全 → 第三方 Cookie
   - 开发环境应该允许 localhost 的 Cookie

2. 检查浏览器控制台是否有 Cookie 相关错误
   - 查看 Console 标签中的错误信息

3. 尝试使用无痕模式测试
   - 排除浏览器扩展的影响

### 问题2: SameSite 属性问题
**原因**: 现代浏览器对 SameSite 有严格要求

**解决方案**:
- 后端已设置 `SameSite=Lax`，适合开发环境
- 生产环境使用 HTTPS 时，可以设置为 `SameSite=None; Secure`

### 问题3: CORS 问题
**原因**: 跨域请求未正确配置

**解决方案**:
- 检查后端 CORS 配置是否包含 `allowCredentials: true`
- 检查前端请求是否包含 `withCredentials: true`

## 测试步骤

1. **清除所有 Cookie**
   - 在 Application → Cookies 中，右键删除所有 Cookie
   - 或使用浏览器设置清除 Cookie

2. **重新登录**
   - 输入用户名和密码
   - 点击登录按钮

3. **查看控制台输出**
   - 检查是否有错误信息
   - 查看 Cookie 设置日志

4. **验证 Cookie**
   - 在 Application → Cookies 中查看
   - 确认 token 和 userId 已设置

5. **测试跳转**
   - 登录成功后应该自动跳转到主页
   - 如果未跳转，查看控制台的路由守卫日志

## 手动测试 Cookie

在浏览器控制台执行以下代码：

```javascript
// 检查所有 Cookie
console.log('所有Cookie:', document.cookie)

// 检查特定 Cookie
function getCookie(name) {
  const cookies = document.cookie.split('; ')
  for (const cookie of cookies) {
    const [key, value] = cookie.split('=')
    if (key === name) return decodeURIComponent(value)
  }
  return null
}

console.log('token:', getCookie('token'))
console.log('userId:', getCookie('userId'))
console.log('username:', getCookie('username'))
console.log('role:', getCookie('role'))

// 手动设置 Cookie 测试
document.cookie = "test=123; path=/; max-age=3600"
console.log('测试Cookie:', getCookie('test'))
```

## 如果 Cookie 仍然无法设置

1. **检查后端日志**
   - 查看后端控制台，确认 Cookie 是否被添加到响应头
   - 检查响应头中是否有 `Set-Cookie` 字段

2. **检查网络请求**
   - 在 Network 标签中查看登录请求
   - 检查响应头中的 `Set-Cookie` 字段
   - 确认 Cookie 是否在响应中

3. **检查浏览器设置**
   - 确保浏览器允许 Cookie
   - 检查是否有隐私扩展阻止了 Cookie

