import { createSSRApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'

// 创建Vue应用实例
export function createApp() {
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

  return {
    app
  }
}
