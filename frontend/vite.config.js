import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
  },
  // react-leaflet 4 + Vite cần optimizeDeps include rõ ràng để tránh
  // 'render2 is not a function' khi dev mode pre-bundle các CJS module.
  optimizeDeps: {
    include: ['leaflet', 'react-leaflet', '@react-leaflet/core'],
  },
  test: {
    environment: 'jsdom',
    globals: true,
    setupFiles: ['./src/test/setup.js'],
    css: false,
    exclude: ['node_modules', 'dist', 'demo'],
  },
})
