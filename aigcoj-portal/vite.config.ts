import { fileURLToPath, URL } from 'node:url'
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import monacoEditorPlugin from 'vite-plugin-monaco-editor'

export default defineConfig({
  // 使用独立缓存目录，避免开发时清理默认缓存目录
  cacheDir: 'node_modules/.vite-dev',
  plugins: [
    vue(),
    ((monacoEditorPlugin as any).default ? (monacoEditorPlugin as any).default({}) : (monacoEditorPlugin as any)({})),
  ],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url)),
    },
  },
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://127.0.0.1:8121',
        changeOrigin: true,
      },
    },
  },
  build: {
    chunkSizeWarningLimit: 2000,
  },
})
