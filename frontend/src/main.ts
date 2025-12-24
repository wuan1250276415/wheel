import { createSSRApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'

// 创建Vue应用实例
export function createApp() {
  console.log('应用正在初始化...')
  const app = createSSRApp(App)

  // 注册Pinia状态管理
  const pinia = createPinia()
  app.use(pinia)

  // 全局错误处理
  app.config.errorHandler = (err, vm, info) => {
    console.error('全局错误:', err, info)

    // 显示错误提示
    uni.showToast({
      title: '程序出现错误',
      icon: 'none'
    })
  }

  // 捕获未处理的 Promise 拒绝
  window.addEventListener('unhandledrejection', (event) => {
    console.error('未处理的 Promise 拒绝:', event.reason);
    // 阻止默认处理（例如控制台打印错误）
    // event.preventDefault(); 
  });

  return {
    app
  }
}
