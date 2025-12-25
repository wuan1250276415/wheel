<template>
  <view class="container">
    <!-- 用户信息卡片 -->
    <view class="user-card">
      <image
        v-if="userInfo?.avatar"
        :src="userInfo.avatar"
        class="avatar"
        mode="aspectFill"
        @click="changeAvatar"
      />
      <view v-else class="avatar-placeholder" @click="changeAvatar">
        <text>{{ userInfo?.nickname?.[0] || '用' }}</text>
      </view>

      <view class="user-info">
        <text class="nickname">{{ userInfo?.nickname || '未设置昵称' }}</text>
        <text class="phone">{{ formatPhone(userInfo?.phone) }}</text>
      </view>

      <button class="edit-btn" @click="showEditDialog = true">
        编辑
      </button>
    </view>

    <!-- 统计卡片 -->
    <view class="stats-card">
      <view class="stats-item">
        <text class="stats-value">{{ userStats.totalSpins || 0 }}</text>
        <text class="stats-label">总转盘次数</text>
      </view>

      <view class="stats-divider"></view>

      <view class="stats-item">
        <text class="stats-value">{{ userStats.consecutiveDays || 0 }}</text>
        <text class="stats-label">连续天数</text>
      </view>

      <view class="stats-divider"></view>

      <view class="stats-item">
        <text class="stats-value">{{ userStats.uniqueContentsSeen || 0 }}</text>
        <text class="stats-label">体验内容</text>
      </view>
    </view>

    <!-- 今日数据卡片 -->
    <view class="today-stats-card">
      <view class="today-stats-title">今日数据</view>
      <view class="today-stats-row">
        <view class="today-stat-item">
          <text class="today-stat-value">{{ userStats.todaySpins || 0 }}</text>
          <text class="today-stat-label">今日转盘</text>
        </view>
        <view class="today-stat-item">
          <text class="today-stat-value">{{ userStats.totalCoupleSpins || 0 }}</text>
          <text class="today-stat-label">情侣转盘</text>
        </view>
        <view class="today-stat-item">
          <text class="today-stat-value">{{ userStats.favoriteCategory || '暂无' }}</text>
          <text class="today-stat-label">最爱分类</text>
        </view>
      </view>
    </view>

    <!-- 功能列表 -->
    <view class="menu-list">
      <view class="menu-item highlight" @click="navigateTo('/pages/content/content')">
        <view class="menu-icon content-icon"></view>
        <view class="menu-text-group">
          <text class="menu-text">内容管理</text>
          <text class="menu-subtitle">管理我提交的转盘内容</text>
        </view>
        <text class="menu-arrow">></text>
      </view>

      <view class="menu-item" @click="navigateTo('/pages/couple/couple')">
        <view class="menu-icon couple-icon"></view>
        <text class="menu-text">情侣关系</text>
        <text class="menu-arrow">></text>
      </view>

      <view class="menu-item" @click="navigateTo('/pages/statistics/statistics')">
        <view class="menu-icon stats-icon"></view>
        <text class="menu-text">数据统计</text>
        <text class="menu-arrow">></text>
      </view>
    </view>

    <!-- 设置列表 -->
    <view class="menu-list">
      <view class="menu-item" @click="navigateTo('/pages/settings/settings')">
        <view class="menu-icon settings-icon"></view>
        <text class="menu-text">设置</text>
        <text class="menu-arrow">></text>
      </view>

      <view class="menu-item" @click="showLogoutConfirm">
        <view class="menu-icon logout-icon"></view>
        <text class="menu-text logout-text">退出登录</text>
        <text class="menu-arrow">></text>
      </view>
    </view>

    <!-- 编辑对话框 -->
    <uni-popup ref="editDialog" type="dialog">
      <uni-popup-dialog
        :show="showEditDialog"
        title="编辑个人信息"
        @close="showEditDialog = false"
      >
        <view class="edit-form">
          <view class="form-item">
            <text class="form-label">昵称</text>
            <input
              v-model="editForm.nickname"
              class="form-input"
              placeholder="请输入昵称"
              maxlength="20"
            />
          </view>
        </view>

        <template #footer>
          <view class="dialog-footer">
            <button class="dialog-btn cancel" @click="showEditDialog = false">
              取消
            </button>
            <button class="dialog-btn confirm" @click="saveProfile">
              保存
            </button>
          </view>
        </template>
      </uni-popup-dialog>
    </uni-popup>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { useUserStore } from '@/stores/user'
import * as statisticsApi from '@/api/statistics'

const userStore = useUserStore()

const userInfo = ref(userStore.userInfo)
const userStats = ref<any>({})
const showEditDialog = ref(false)
const editForm = ref({
  nickname: ''
})
const isLoading = ref(false)

// 监听userStore的userInfo变化
watch(() => userStore.userInfo, (newVal) => {
  userInfo.value = newVal
}, { deep: true })

// 页面加载
onMounted(() => {
  // 初始化用户Store
  userStore.init()
  initData()
})

// 初始化数据
async function initData() {
  if (!userStore.isLoggedIn) {
    uni.showToast({
      title: '请先登录',
      icon: 'none'
    })
    // 跳转到登录页
    setTimeout(() => {
      uni.redirectTo({
        url: '/pages/login/login'
      })
    }, 1500)
    return
  }

  // 更新用户信息引用
  userInfo.value = userStore.userInfo
  
  // 初始化编辑表单
  if (userInfo.value?.nickname) {
    editForm.value.nickname = userInfo.value.nickname
  }

  isLoading.value = true

  try {
    // 获取用户统计
    const statsRes = await statisticsApi.getUserStatistics()
    if (statsRes) {
      userStats.value = {
        totalSpins: statsRes.totalSpins || 0,
        todaySpins: statsRes.todaySpins || 0,
        consecutiveDays: statsRes.consecutiveDays || 0,
        uniqueContentsSeen: statsRes.uniqueContentsSeen || 0,
        totalCoupleSpins: statsRes.totalCoupleSpins || 0,
        favoriteCategory: statsRes.favoriteCategory || '暂无'
      }
    }
  } catch (error) {
    console.error('获取用户统计失败:', error)
  } finally {
    isLoading.value = false
  }
}

// 格式化手机号
function formatPhone(phone?: string) {
  if (!phone) return ''
  return phone.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2')
}

// 修改头像
function changeAvatar() {
  uni.chooseImage({
    count: 1,
    sizeType: ['compressed'],
    sourceType: ['album', 'camera'],
    success: (res) => {
      // 这里应该上传图片到服务器
      uni.showToast({
        title: '头像上传功能开发中',
        icon: 'none'
      })
    }
  })
}

// 显示退出确认
function showLogoutConfirm() {
  uni.showModal({
    title: '提示',
    content: '确定要退出登录吗？',
    success: (res) => {
      if (res.confirm) {
        handleLogout()
      }
    }
  })
}

// 处理退出登录
function handleLogout() {
  userStore.logout()

  uni.showToast({
    title: '已退出登录',
    icon: 'success'
  })

  // 跳转到首页
  setTimeout(() => {
    uni.reLaunch({
      url: '/pages/index/index'
    })
  }, 1500)
}

// 保存个人信息
async function saveProfile() {
  if (!editForm.value.nickname.trim()) {
    uni.showToast({
      title: '请输入昵称',
      icon: 'none'
    })
    return
  }

  const res = await userStore.updateProfile({
    nickname: editForm.value.nickname
  })

  if (res.success) {
    uni.showToast({
      title: '保存成功',
      icon: 'success'
    })
    showEditDialog.value = false

    // 更新本地用户信息
    if (userStore.userInfo) {
      userStore.userInfo.nickname = editForm.value.nickname
      userInfo.value = userStore.userInfo
    }
  } else {
    uni.showToast({
      title: res.message || '保存失败',
      icon: 'none'
    })
  }
}

// 导航到指定页面
function navigateTo(url: string) {
  uni.navigateTo({ url })
}
</script>

<style scoped>
.container {
  min-height: 100vh;
  background: linear-gradient(180deg, #FFF0F5 0%, #FFFFFF 100%);
  padding: 20rpx;
}

.user-card {
  background: linear-gradient(135deg, #FF69B4, #FF1493);
  border-radius: 20rpx;
  padding: 40rpx;
  display: flex;
  align-items: center;
  box-shadow: 0 8rpx 20rpx rgba(255, 105, 180, 0.3);
  margin-bottom: 30rpx;
}

.avatar {
  width: 120rpx;
  height: 120rpx;
  border-radius: 50%;
  border: 4rpx solid #fff;
}

.avatar-placeholder {
  width: 120rpx;
  height: 120rpx;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.3);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 48rpx;
  font-weight: bold;
  color: #fff;
  border: 4rpx solid #fff;
}

.user-info {
  flex: 1;
  margin-left: 30rpx;
}

.nickname {
  display: block;
  font-size: 36rpx;
  font-weight: bold;
  color: #fff;
  margin-bottom: 10rpx;
}

.phone {
  display: block;
  font-size: 28rpx;
  color: rgba(255, 255, 255, 0.9);
}

.edit-btn {
  width: 120rpx;
  height: 60rpx;
  background: rgba(255, 255, 255, 0.3);
  color: #fff;
  border: none;
  border-radius: 30rpx;
  font-size: 26rpx;
}

.stats-card {
  background: #fff;
  border-radius: 20rpx;
  padding: 40rpx;
  display: flex;
  justify-content: space-around;
  align-items: center;
  box-shadow: 0 4rpx 15rpx rgba(0, 0, 0, 0.05);
  margin-bottom: 30rpx;
}

.stats-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  flex: 1;
}

.stats-value {
  font-size: 40rpx;
  font-weight: bold;
  color: #FF1493;
  margin-bottom: 10rpx;
}

.stats-label {
  font-size: 24rpx;
  color: #999;
}

.stats-divider {
  width: 2rpx;
  height: 60rpx;
  background: #f0f0f0;
}

.today-stats-card {
  background: #fff;
  border-radius: 20rpx;
  padding: 30rpx;
  box-shadow: 0 4rpx 15rpx rgba(0, 0, 0, 0.05);
  margin-bottom: 30rpx;
}

.today-stats-title {
  font-size: 28rpx;
  font-weight: bold;
  color: #666;
  margin-bottom: 20rpx;
}

.today-stats-row {
  display: flex;
  justify-content: space-around;
}

.today-stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.today-stat-value {
  font-size: 32rpx;
  font-weight: bold;
  color: #FF69B4;
  margin-bottom: 8rpx;
}

.today-stat-label {
  font-size: 22rpx;
  color: #999;
}

.menu-list {
  background: #fff;
  border-radius: 20rpx;
  overflow: hidden;
  box-shadow: 0 4rpx 15rpx rgba(0, 0, 0, 0.05);
  margin-bottom: 30rpx;
}

.menu-item {
  display: flex;
  align-items: center;
  padding: 30rpx;
  border-bottom: 1rpx solid #f0f0f0;
}

.menu-item:last-child {
  border-bottom: none;
}

.menu-icon {
  width: 60rpx;
  height: 60rpx;
  border-radius: 50%;
  margin-right: 20rpx;
}

.couple-icon {
  background: linear-gradient(135deg, #FFA500, #FF6347);
}

.stats-icon {
  background: linear-gradient(135deg, #4169E1, #6495ED);
}

.content-icon {
  background: linear-gradient(135deg, #32CD32, #90EE90);
}

.settings-icon {
  background: linear-gradient(135deg, #808080, #A9A9A9);
}

.logout-icon {
  background: linear-gradient(135deg, #DC143C, #FF6347);
}

.menu-text {
  flex: 1;
  font-size: 30rpx;
  color: #333;
}

.menu-text-group {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.menu-subtitle {
  font-size: 24rpx;
  color: #999;
  margin-top: 5rpx;
}

.menu-item.highlight {
  background: linear-gradient(135deg, #FFF0F5, #FFFFFF);
  border: 2rpx solid #FF69B4;
}

.logout-text {
  color: #DC143C;
}

.menu-arrow {
  font-size: 28rpx;
  color: #ccc;
}

.edit-form {
  padding: 20rpx 0;
}

.form-item {
  margin-bottom: 30rpx;
}

.form-label {
  display: block;
  font-size: 28rpx;
  color: #666;
  margin-bottom: 15rpx;
}

.form-input {
  width: 100%;
  padding: 20rpx;
  background: #f5f5f5;
  border-radius: 10rpx;
  font-size: 28rpx;
  color: #333;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 20rpx;
  margin-top: 20rpx;
}

.dialog-btn {
  padding: 15rpx 40rpx;
  border-radius: 30rpx;
  font-size: 28rpx;
  border: none;
}

.cancel {
  background: #f5f5f5;
  color: #666;
}

.confirm {
  background: linear-gradient(135deg, #FF69B4, #FF1493);
  color: #fff;
}
</style>
