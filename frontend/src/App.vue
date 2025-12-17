<template>
  <view class="app">
    <!-- 应用入口组件 -->
  </view>
</template>

<script setup lang="ts">
import { onLaunch, onShow, onHide } from '@dcloudio/uni-app'
import { useUserStore } from '@/stores/user'

// 初始化用户状态
const userStore = useUserStore()

// 应用启动
onLaunch(() => {
  console.log('应用启动')

  // 初始化用户状态
  userStore.init()

  // 检查更新
  checkUpdate()
})

// 应用显示
onShow(() => {
  console.log('应用显示')
})

// 应用隐藏
onHide(() => {
  console.log('应用隐藏')
})

// 检查应用更新
function checkUpdate() {
  // #ifdef MP-WEIXIN
  const updateManager = uni.getUpdateManager()

  updateManager.onCheckForUpdate((res) => {
    console.log('检查更新结果:', res.hasUpdate)
  })

  updateManager.onUpdateReady(() => {
    uni.showModal({
      title: '更新提示',
      content: '新版本已经准备好，是否重启应用？',
      success: (res) => {
        if (res.confirm) {
          updateManager.applyUpdate()
        }
      }
    })
  })

  updateManager.onUpdateFailed(() => {
    uni.showToast({
      title: '更新失败',
      icon: 'none'
    })
  })
  #endif
}
</script>

<style>
/* 全局样式 */
page {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'Roboto', 'Oxygen', 'Ubuntu', 'Cantarell', 'Fira Sans', 'Droid Sans', 'Helvetica Neue', sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

.app {
  width: 100%;
  height: 100%;
}

/* 全局滚动条样式 */
::-webkit-scrollbar {
  width: 0;
  height: 0;
  background: transparent;
}
</style>
