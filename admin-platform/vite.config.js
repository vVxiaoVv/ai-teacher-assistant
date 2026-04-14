import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'

// https://vite.dev/config/
export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src')
    }
  },
  server: {
    proxy: {
      // API代理配置
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        // 不重写路径，直接转发到后端 /api 路径
        // rewrite: (path) => path.replace(/^\/api/, '/api')
      }
    }
  }
})
