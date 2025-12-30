<template>
  <view class="wheel-wrapper" :style="cssVars">
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
        <view class="btn-icon"></view>
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
import { ref, computed } from 'vue'
import { useWheelStore, type WheelContent } from '@/stores/wheel'
import type { Theme } from '@/themes'
import { defaultTheme } from '@/themes'

interface Props {
  size?: number
  isSpinning?: boolean
  theme?: Theme | null
}

const props = withDefaults(defineProps<Props>(), {
  size: 320,
  isSpinning: false,
  theme: null
})

const emit = defineEmits<{
  (e: 'spin'): void
  (e: 'result', content: WheelContent): void
}>()

const wheelStore = useWheelStore()
const result = ref<WheelContent | null>(null)
const showResultPopup = ref(false)
const isVisualSpinning = ref(false)

// 将主题样式转换为CSS变量（使用fallback）
const cssVars = computed(() => {
  const theme = props.theme || defaultTheme
  const styles = theme.styles
  return {
    '--wheel-bg-gradient': styles.wheelBgGradient,
    '--wheel-border-color': styles.wheelBorderColor,
    '--wheel-box-shadow': styles.wheelBoxShadow,
    '--glow-bg-idle': styles.glowBgIdle,
    '--glow-bg-active': styles.glowBgActive,
    '--ring-outer-color': styles.ringOuterColor,
    '--ring-inner-color': styles.ringInnerColor,
    '--ring-inner-style': styles.ringInnerStyle || 'dotted',
    '--ring-inner-width': styles.ringInnerWidth || '4rpx',
    '--decorator-bg': styles.decoratorBg,
    '--decorator-shadow': styles.decoratorShadow,
    '--btn-bg': styles.btnBg,
    '--btn-shadow': styles.btnShadow,
    '--btn-icon-color': styles.btnIconColor,
    '--btn-icon-content': `"${styles.btnIconContent}"`,
    '--btn-icon-font-family': styles.btnIconFontFamily || 'inherit',
    '--center-deco-text-color': styles.centerDecoTextColor,
    '--center-deco-font-family': styles.centerDecoFontFamily,
    '--popup-bg': styles.popupBg,
    '--popup-card-bg': styles.popupCardBg,
    '--popup-title-color': styles.popupTitleColor,
    '--popup-result-text-color': styles.popupResultTextColor,
    '--popup-result-border-color': styles.popupResultBorderColor,
    '--popup-close-btn-bg': styles.popupCloseBtnBg,
    '--popup-close-btn-shadow': styles.popupCloseBtnShadow
  }
})

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
  background: var(--glow-bg-idle);
  filter: blur(20px);
  z-index: 1;
  animation: breathe 4s ease-in-out infinite;
  transition: background 0.4s ease;
}

.glow-active {
  background: var(--glow-bg-active);
  animation: pulse-fast 1s ease-in-out infinite;
}

/* 神秘转盘主体 */
.mystery-wheel {
  position: relative;
  width: 480rpx;
  height: 480rpx;
  border-radius: 50%;
  background: var(--wheel-bg-gradient);
  box-shadow: var(--wheel-box-shadow);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 5;
  border: 4rpx solid var(--wheel-border-color);
  animation: rotate-slow 20s linear infinite;
  transition: background 0.4s ease, box-shadow 0.4s ease, border-color 0.4s ease;
}

.wheel-spinning {
  animation: rotate-fast 0.6s linear infinite;
}

/* 装饰环 */
.wheel-ring {
  position: absolute;
  border-radius: 50%;
  transition: border-color 0.4s ease;
}

.outer-ring {
  width: 440rpx;
  height: 440rpx;
  border: 2rpx dashed var(--ring-outer-color);
  animation: rotate-reverse 30s linear infinite;
}

.inner-ring {
  width: 300rpx;
  height: 300rpx;
  border-style: var(--ring-inner-style);
  border-color: var(--ring-inner-color);
  border-width: var(--ring-inner-width);
}

/* 装饰星星 */
.star {
  position: absolute;
  width: 10rpx;
  height: 10rpx;
  background: var(--decorator-bg);
  border-radius: 50%;
  box-shadow: var(--decorator-shadow);
  transition: background 0.4s ease, box-shadow 0.4s ease;
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
  border: 2rpx solid var(--wheel-border-color);
  display: flex;
  justify-content: center;
  align-items: center;
  transition: border-color 0.4s ease;
}

.decoration-text {
  font-size: 24rpx;
  color: var(--center-deco-text-color);
  letter-spacing: 4rpx;
  opacity: 0.8;
  font-family: var(--center-deco-font-family);
  transition: color 0.4s ease;
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
  background: var(--btn-bg);
  border: none;
  box-shadow: var(--btn-shadow);
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
}

.btn-icon {
  font-size: 48rpx;
  color: var(--btn-icon-color);
  animation: heartbeat 1.5s ease-in-out infinite;
  transition: color 0.4s ease;
  font-family: var(--btn-icon-font-family);
}

.btn-icon::before {
  content: var(--btn-icon-content);
}

/* 结果弹窗 */
.result-popup {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: var(--popup-bg);
  backdrop-filter: blur(4px);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
  animation: fadeIn 0.4s ease;
}

.result-card {
  background: var(--popup-card-bg);
  border-radius: 40rpx;
  padding: 60rpx 50rpx;
  width: 80%;
  max-width: 560rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  box-shadow: 0 20rpx 60rpx rgba(0, 0, 0, 0.1);
  animation: slideUp 0.4s cubic-bezier(0.16, 1, 0.3, 1);
  transition: background 0.4s ease;
}

.card-header {
  margin-bottom: 30rpx;
}
.card-icon {
  font-size: 60rpx;
}

.result-title {
  font-size: 32rpx;
  color: var(--popup-title-color);
  margin-bottom: 40rpx;
  letter-spacing: 2rpx;
  transition: color 0.4s ease;
}

.result-content-box {
  margin-bottom: 50rpx;
  padding: 20rpx;
  border-bottom: 2rpx solid var(--popup-result-border-color);
  width: 100%;
  text-align: center;
  transition: border-color 0.4s ease;
}

.result-text {
  font-size: 44rpx;
  font-weight: bold;
  color: var(--popup-result-text-color);
  line-height: 1.4;
  transition: color 0.4s ease;
}

.result-close-btn {
  width: 100%;
  height: 90rpx;
  background: var(--popup-close-btn-bg);
  color: white;
  border-radius: 45rpx;
  font-size: 32rpx;
  font-weight: 500;
  letter-spacing: 4rpx;
  border: none;
  box-shadow: var(--popup-close-btn-shadow);
  transition: background 0.4s ease, box-shadow 0.4s ease;
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
