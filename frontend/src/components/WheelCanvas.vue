<template>
  <view class="wheel-wrapper">
    <view class="wheel-container">
      <!-- 浪漫背景光晕 -->
      <view class="glow-bg" :class="{ 'glow-active': isVisualSpinning }"></view>
      
      <!-- 主圆盘 (纯装饰) -->
      <view class="mystery-wheel" :class="{ 'wheel-spinning': isVisualSpinning }">
        <view class="wheel-ring outer-ring"></view>
        <view class="wheel-ring inner-ring"></view>
        
        <!-- 装饰性粒子/星星 -->
        <view class="star star-1"></view>
        <view class="star star-2"></view>
        <view class="star star-3"></view>
        <view class="star star-4"></view>
        
        <!-- 中心装饰文字 -->
        <view class="center-decoration">
          <text class="decoration-text">Touch</text>
        </view>
      </view>

      <!-- 开始按钮（中心悬浮） -->
      <button
        class="spin-btn"
        :class="{ 'btn-disabled': isSpinning || isVisualSpinning || wheelStore.contents.length === 0 }"
        @click="handleSpin"
        :disabled="isSpinning || isVisualSpinning "
      >
        <view class="heart-icon">❤</view>
      </button>
    </view>

    <!-- 结果弹窗 -->
    <view v-if="showResultPopup" class="result-popup" @click="closeResultPopup">
      <view class="result-card" @click.stop>
        <view class="card-header">
           <text class="card-icon">✨</text>
        </view>
        <text class="result-title">命运的指示</text>
        <view class="result-content-box">
          <text class="result-text">{{ result?.contentText || '...' }}</text>
        </view>
        <button class="result-close-btn" @click="closeResultPopup">收下</button>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useWheelStore, type WheelContent } from '@/stores/wheel'

interface Props {
  size?: number
  isSpinning?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  size: 320,
  isSpinning: false
})

const emit = defineEmits<{
  (e: 'spin'): void
  (e: 'result', content: WheelContent): void
}>()

const wheelStore = useWheelStore()
const result = ref<WheelContent | null>(null)
const showResultPopup = ref(false)
const isVisualSpinning = ref(false)

// 动画配置接口
interface AnimationConfig {
  content: WheelContent
  spinDuration: number
}

// 暴露给父组件的方法：开始旋转动画
const playAnimation = (config: AnimationConfig) => {
  result.value = config.content
  isVisualSpinning.value = true
  
  // 这里的动画完全由CSS类 'wheel-spinning' 和 'glow-active' 控制
  // JS只需要负责定时结束
  const duration = config.spinDuration || 3000
  
  setTimeout(() => {
    // 动画结束
    isVisualSpinning.value = false
    showResultPopup.value = true
    emit('result', config.content)
  }, duration)
}

// 处理转盘初始点击
function handleSpin() {
  if (props.isSpinning || isVisualSpinning.value) return
  showResultPopup.value = false
  emit('spin')
  result.value = null
}

// 关闭结果弹窗
function closeResultPopup() {
  showResultPopup.value = false
}

// 暴露方法
defineExpose({
  playAnimation
})
</script>

<style scoped>
.wheel-wrapper {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 40rpx;
  position: relative;
}

.wheel-container {
  position: relative;
  width: 600rpx;
  height: 600rpx;
  display: flex;
  justify-content: center;
  align-items: center;
}

/* 背景光晕 */
.glow-bg {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 500rpx;
  height: 500rpx;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(255, 182, 193, 0.4) 0%, rgba(255, 105, 180, 0) 70%);
  filter: blur(20px);
  z-index: 1;
  animation: breathe 4s ease-in-out infinite;
}

.glow-active {
  background: radial-gradient(circle, rgba(255, 20, 147, 0.6) 0%, rgba(255, 105, 180, 0.2) 80%);
  animation: pulse-fast 1s ease-in-out infinite;
}

/* 神秘转盘主体 */
.mystery-wheel {
  position: relative;
  width: 480rpx;
  height: 480rpx;
  border-radius: 50%;
  /* 梦幻渐变背景 */
  background: linear-gradient(135deg, #ffd1ff 0%, #fad0c4 100%);
  box-shadow: 
    0 10rpx 30rpx rgba(255, 182, 193, 0.4),
    inset 0 0 40rpx rgba(255, 255, 255, 0.8);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 5;
  border: 4rpx solid rgba(255, 255, 255, 0.6);
  animation: rotate-slow 20s linear infinite;
}

.wheel-spinning {
  animation: rotate-fast 0.6s linear infinite;
}

/* 装饰环 */
.wheel-ring {
  position: absolute;
  border-radius: 50%;
  border: 2rpx dashed rgba(255, 255, 255, 0.6);
}

.outer-ring {
  width: 440rpx;
  height: 440rpx;
  animation: rotate-reverse 30s linear infinite;
}

.inner-ring {
  width: 300rpx;
  height: 300rpx;
  border-style: dotted;
  border-color: rgba(255, 255, 255, 0.8);
  border-width: 4rpx;
}

/* 装饰星星 */
.star {
  position: absolute;
  width: 10rpx;
  height: 10rpx;
  background: white;
  border-radius: 50%;
  box-shadow: 0 0 10rpx white;
}
.star-1 { top: 40rpx; left: 50%; }
.star-2 { bottom: 40rpx; left: 50%; }
.star-3 { top: 50%; left: 40rpx; }
.star-4 { top: 50%; right: 40rpx; }

/* 中心装饰 */
.center-decoration {
  width: 200rpx;
  height: 200rpx;
  border-radius: 50%;
  border: 2rpx solid rgba(255, 255, 255, 0.4);
  display: flex;
  justify-content: center;
  align-items: center;
}

.decoration-text {
  font-size: 24rpx;
  color: #fff;
  letter-spacing: 4rpx;
  opacity: 0.8;
  font-family: 'Courier New', Courier, monospace;
}

/* 按钮 - 悬浮在最上层 */
.spin-btn {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 140rpx;
  height: 140rpx;
  border-radius: 50%;
  background: white;
  border: none;
  box-shadow: 0 8rpx 20rpx rgba(255, 105, 180, 0.3);
  z-index: 10;
  display: flex;
  justify-content: center;
  align-items: center;
  transition: all 0.3s ease;
}

.spin-btn:active {
  transform: translate(-50%, -50%) scale(0.9);
}

.btn-disabled {
  opacity: 0.8;
  /* pointer-events: none; */
}

.heart-icon {
  font-size: 48rpx;
  color: #ff69b4;
  animation: heartbeat 1.5s ease-in-out infinite;
}

/* 结果弹窗 */
.result-popup {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.4);
  backdrop-filter: blur(4px);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
  animation: fadeIn 0.4s ease;
}

.result-card {
  background: rgba(255, 255, 255, 0.95);
  border-radius: 40rpx;
  padding: 60rpx 50rpx;
  width: 80%;
  max-width: 560rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  box-shadow: 0 20rpx 60rpx rgba(0, 0, 0, 0.1);
  animation: slideUp 0.4s cubic-bezier(0.16, 1, 0.3, 1);
}

.card-header {
  margin-bottom: 30rpx;
}
.card-icon {
  font-size: 60rpx;
}

.result-title {
  font-size: 32rpx;
  color: #888;
  margin-bottom: 40rpx;
  letter-spacing: 2rpx;
}

.result-content-box {
  margin-bottom: 50rpx;
  padding: 20rpx;
  border-bottom: 2rpx solid #ffe4e1;
  width: 100%;
  text-align: center;
}

.result-text {
  font-size: 44rpx;
  font-weight: bold;
  color: #d63384; 
  line-height: 1.4;
}

.result-close-btn {
  width: 100%;
  height: 90rpx;
  background: linear-gradient(135deg, #ff9a9e 0%, #ff6a88 100%);
  color: white;
  border-radius: 45rpx;
  font-size: 32rpx;
  font-weight: 500;
  letter-spacing: 4rpx;
  border: none;
  box-shadow: 0 10rpx 20rpx rgba(255, 106, 136, 0.3);
}

/* 动画关键帧 */

@keyframes breathe {
  0%, 100% { transform: translate(-50%, -50%) scale(1); opacity: 0.6; }
  50% { transform: translate(-50%, -50%) scale(1.1); opacity: 0.8; }
}

@keyframes pulse-fast {
  0%, 100% { transform: translate(-50%, -50%) scale(1); opacity: 0.8; }
  50% { transform: translate(-50%, -50%) scale(1.2); opacity: 1; }
}

@keyframes rotate-slow {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

@keyframes rotate-fast {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

@keyframes rotate-reverse {
  from { transform: rotate(360deg); }
  to { transform: rotate(0deg); }
}

@keyframes heartbeat {
  0%, 100% { transform: scale(1); }
  15% { transform: scale(1.15); }
  30% { transform: scale(1); }
  45% { transform: scale(1.15); }
  60% { transform: scale(1); }
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

@keyframes slideUp {
  from { opacity: 0; transform: translateY(40rpx); }
  to { opacity: 1; transform: translateY(0); }
}
</style>
