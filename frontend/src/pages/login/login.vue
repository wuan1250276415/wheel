<template>
  <view class="login-container">
    <!-- Logo区域 -->
    <view class="logo-section">
      <view class="logo-circle">
        <text class="logo-text">💕</text>
      </view>
      <text class="app-title">情侣转盘</text>
      <text class="app-subtitle">和TA一起享受甜蜜时光</text>
    </view>

    <!-- 登录表单 -->
    <view class="form-section">
      <view class="input-group">
        <view class="input-wrapper" :class="{ 'input-focus': usernameFocus, 'input-error': usernameError }">
          <text class="input-icon">👤</text>
          <input
            v-model="username"
            type="text"
            placeholder="请输入用户名"
            @focus="usernameFocus = true"
            @blur="handleUsernameBlur"
            @input="clearUsernameError"
          />
        </view>
        <text v-if="usernameError" class="error-text">{{ usernameError }}</text>
      </view>

      <view class="input-group">
        <view class="input-wrapper" :class="{ 'input-focus': passwordFocus, 'input-error': passwordError }">
          <text class="input-icon">🔒</text>
          <input
            v-model="password"
            :type="showPassword ? 'text' : 'password'"
            placeholder="请输入密码"
            @focus="passwordFocus = true"
            @blur="handlePasswordBlur"
            @input="clearPasswordError"
          />
          <text class="toggle-password" @click="showPassword = !showPassword">
            {{ showPassword ? '🙈' : '👁️' }}
          </text>
        </view>
        <text v-if="passwordError" class="error-text">{{ passwordError }}</text>
      </view>

      <!-- 登录按钮 -->
      <button 
        class="login-btn" 
        :class="{ 'btn-loading': loading }"
        :disabled="loading"
        @click="handleLogin"
      >
        <text v-if="loading" class="loading-spinner">⏳</text>
        <text>{{ loading ? '登录中...' : '登 录' }}</text>
      </button>

      <!-- 错误提示 -->
      <view v-if="loginError" class="login-error">
        <text>{{ loginError }}</text>
      </view>
    </view>

    <!-- 底部提示 -->
    <view class="footer-section">
      <text class="footer-text">登录即表示同意</text>
      <text class="link-text">《用户协议》</text>
      <text class="footer-text">和</text>
      <text class="link-text">《隐私政策》</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

// 表单数据
const username = ref('')
const password = ref('')
const showPassword = ref(false)

// 焦点状态
const usernameFocus = ref(false)
const passwordFocus = ref(false)

// 错误状态
const usernameError = ref('')
const passwordError = ref('')
const loginError = ref('')

// 加载状态
const loading = ref(false)

// 清除用户名错误
function clearUsernameError() {
  usernameError.value = ''
  loginError.value = ''
}

// 清除密码错误
function clearPasswordError() {
  passwordError.value = ''
  loginError.value = ''
}

// 处理用户名失焦
function handleUsernameBlur() {
  usernameFocus.value = false
  validateUsername()
}

// 处理密码失焦
function handlePasswordBlur() {
  passwordFocus.value = false
  validatePassword()
}

// 验证用户名
function validateUsername(): boolean {
  const value = username.value.trim()
  if (!value) {
    usernameError.value = '请输入用户名'
    return false
  }
  usernameError.value = ''
  return true
}

// 验证密码
function validatePassword(): boolean {
  const value = password.value.trim()
  if (!value) {
    passwordError.value = '请输入密码'
    return false
  }
  passwordError.value = ''
  return true
}

// 表单验证
function validateForm(): boolean {
  const isUsernameValid = validateUsername()
  const isPasswordValid = validatePassword()
  return isUsernameValid && isPasswordValid
}

// 处理登录
async function handleLogin() {
  // 清除之前的登录错误
  loginError.value = ''

  // 表单验证
  if (!validateForm()) {
    return
  }

  loading.value = true

  try {
    const result = await userStore.login(username.value.trim(), password.value.trim())

    if (result.success) {
      // 登录成功，跳转到首页
      uni.showToast({
        title: '登录成功',
        icon: 'success',
        duration: 1500
      })

      setTimeout(() => {
        uni.reLaunch({
          url: '/pages/index/index'
        })
      }, 1500)
    } else {
      // 登录失败，显示错误信息
      loginError.value = result.message || '登录失败，请重试'
    }
  } catch (error: any) {
    loginError.value = error.message || '网络错误，请稍后重试'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  background: linear-gradient(180deg, #FFF0F5 0%, #FFFFFF 50%, #FFF0F5 100%);
  display: flex;
  flex-direction: column;
  padding: 60rpx 40rpx;
}

.logo-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-top: 80rpx;
  margin-bottom: 80rpx;
}

.logo-circle {
  width: 160rpx;
  height: 160rpx;
  border-radius: 50%;
  background: linear-gradient(135deg, #FF69B4, #FF1493);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 10rpx 30rpx rgba(255, 105, 180, 0.4);
  margin-bottom: 30rpx;
}

.logo-text {
  font-size: 80rpx;
}

.app-title {
  font-size: 48rpx;
  font-weight: bold;
  color: #FF1493;
  margin-bottom: 15rpx;
}

.app-subtitle {
  font-size: 28rpx;
  color: #999;
}

.form-section {
  flex: 1;
  padding: 0 20rpx;
}

.input-group {
  margin-bottom: 40rpx;
}

.input-wrapper {
  display: flex;
  align-items: center;
  background: #fff;
  border-radius: 50rpx;
  padding: 0 30rpx;
  height: 100rpx;
  border: 2rpx solid #eee;
  transition: all 0.3s;
}

.input-wrapper.input-focus {
  border-color: #FF69B4;
  box-shadow: 0 4rpx 15rpx rgba(255, 105, 180, 0.2);
}

.input-wrapper.input-error {
  border-color: #ff4d4f;
}

.input-icon {
  font-size: 36rpx;
  margin-right: 20rpx;
}

.input-wrapper input {
  flex: 1;
  height: 100%;
  font-size: 32rpx;
  color: #333;
}

.toggle-password {
  font-size: 36rpx;
  padding: 10rpx;
}

.error-text {
  display: block;
  color: #ff4d4f;
  font-size: 24rpx;
  margin-top: 10rpx;
  padding-left: 30rpx;
}

.login-btn {
  width: 100%;
  height: 100rpx;
  background: linear-gradient(135deg, #FF69B4, #FF1493);
  border-radius: 50rpx;
  border: none;
  color: #fff;
  font-size: 36rpx;
  font-weight: bold;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-top: 40rpx;
  box-shadow: 0 8rpx 20rpx rgba(255, 105, 180, 0.4);
  transition: all 0.3s;
}

.login-btn:active {
  transform: scale(0.98);
  opacity: 0.9;
}

.login-btn.btn-loading {
  opacity: 0.7;
}

.loading-spinner {
  margin-right: 10rpx;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.login-error {
  margin-top: 30rpx;
  padding: 20rpx;
  background: #fff2f0;
  border: 1rpx solid #ffccc7;
  border-radius: 10rpx;
  text-align: center;
}

.login-error text {
  color: #ff4d4f;
  font-size: 28rpx;
}

.footer-section {
  display: flex;
  justify-content: center;
  align-items: center;
  flex-wrap: wrap;
  padding: 40rpx 0;
}

.footer-text {
  font-size: 24rpx;
  color: #999;
}

.link-text {
  font-size: 24rpx;
  color: #FF69B4;
}
</style>
