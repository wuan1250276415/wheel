<template>
  <view class="wheel-wrapper">
    <view class="wheel-container">
      <view class="wheel-canvas-container">
        <canvas
          canvas-id="wheelCanvas"
          class="wheel-canvas"
          :style="{ width: canvasSize + 'px', height: canvasSize + 'px' }"
        ></canvas>
      </view>

      <!-- 指针（固定在顶部） -->
      <view class="wheel-pointer">
        <view class="pointer-arrow"></view>
      </view>

      <!-- 开始按钮（中心） -->
      <button
        class="spin-button"
        :class="{ spinning: isSpinning }"
        @click="handleSpin"
        :disabled="isSpinning || contents.length === 0"
      >
        {{ isSpinning ? '...' : '开始' }}
      </button>
    </view>

    <!-- 结果弹窗 -->
    <view v-if="showResultPopup" class="result-popup" @click="closeResultPopup">
      <view class="result-popup-content" @click.stop>
        <view class="result-icon">🎯</view>
        <text class="result-title">本次结果</text>
        <text class="result-text">{{ result?.contentText || '' }}</text>
        <button class="result-close-btn" @click="closeResultPopup">确定</button>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted, watch, nextTick } from 'vue'
import { useWheelStore, type WheelContent } from '@/stores/wheel'

interface Props {
  size?: number
  contents?: WheelContent[]
  isSpinning?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  size: 320,
  contents: () => [],
  isSpinning: false
})

const emit = defineEmits<{
  (e: 'spin'): void
  (e: 'result', content: WheelContent): void
}>()

const wheelStore = useWheelStore()
const canvasSize = ref(props.size)
const canvasContext = ref<UniApp.CanvasContext | null>(null)
const currentRotation = ref(0)
const animationFrame = ref<number | null>(null)
const result = ref<WheelContent | null>(null)
const showResultPopup = ref(false)

// 初始化画布
function initCanvas() {
  setTimeout(() => {
    // #ifdef H5
    const canvasEl = document.querySelector('.wheel-canvas') as HTMLElement
    if (canvasEl) {
      const canvas = canvasEl.querySelector('canvas') as HTMLCanvasElement
      if (canvas) {
        const ctx = canvas.getContext('2d')
        if (ctx) {
          // 处理 High DPI
          const dpr = window.devicePixelRatio || 1
          canvas.width = canvasSize.value * dpr
          canvas.height = canvasSize.value * dpr
          canvas.style.width = `${canvasSize.value}px`
          canvas.style.height = `${canvasSize.value}px`

          canvasContext.value = ctx as unknown as UniApp.CanvasContext
          drawWheel()
          return
        }
      }
    }
    // #endif

    // #ifndef H5
    const query = uni.createSelectorQuery()
    query
      .select('.wheel-canvas')
      .fields({ node: true, size: true }, (res: any) => {
        if (res && res.node) {
          const canvas = res.node as HTMLCanvasElement
          const ctx = canvas.getContext('2d')
          if (ctx) {
            canvas.width = canvasSize.value
            canvas.height = canvasSize.value
            canvasContext.value = ctx as unknown as UniApp.CanvasContext
            drawWheel()
          }
        } else {
          try {
            const mpCtx = uni.createCanvasContext('wheelCanvas')
            if (mpCtx) {
              canvasContext.value = mpCtx
              drawWheel()
            }
          } catch (e) {
            console.error('Failed to create canvas context:', e)
          }
        }
      })
      .exec()
    // #endif
  }, 150)
}

// 绘制转盘
function drawWheel() {
  if (!canvasContext.value || props.contents.length === 0) return

  const ctx = canvasContext.value
  const size = canvasSize.value
  const centerX = size / 2
  const centerY = size / 2

  // 1. 获取 DPR
  let dpr = 1
  // #ifdef H5
  dpr = window.devicePixelRatio || 1
  // #endif

  // 2. 重置上下文状态 (仅 H5)
  // #ifdef H5
  if (ctx instanceof CanvasRenderingContext2D) {
    ctx.setTransform(1, 0, 0, 1, 0, 0)
    ctx.shadowBlur = 0
    ctx.shadowOffsetX = 0
    ctx.shadowOffsetY = 0
  }
  // #endif

  // 3. 清空画布
  // #ifdef H5
  const canvasEl = document.querySelector('.wheel-canvas canvas') as HTMLCanvasElement
  if (canvasEl) {
    ctx.clearRect(0, 0, size * dpr, size * dpr)
  } else {
    ctx.clearRect(0, 0, size, size)
  }
  // #endif
  // #ifndef H5
  ctx.clearRect(0, 0, size, size)
  // #endif

  // // 4. 绘制最外层装饰圈 (Rim) - 改为环形描边
  // ctx.save()
  // // #ifdef H5
  // if (dpr !== 1) {
  //   ctx.scale(dpr, dpr)
  // }
  // // #endif
  //
  // // 使用实际可用的转盘半径
  // const wheelRadius = size / 2 - 2 // 留出 2px 边距
  // const rimThickness = 10
  //
  // // 绘制环形外边框
  // ctx.beginPath()
  // ctx.arc(centerX, centerY, wheelRadius, 0, 2 * Math.PI)
  // ctx.strokeStyle = '#FF1493'
  // ctx.lineWidth = rimThickness
  // ctx.stroke()
  //
  // // 绘制边框内圈细线（装饰）
  // ctx.beginPath()
  // ctx.arc(centerX, centerY, wheelRadius - rimThickness - 2, 0, 2 * Math.PI)
  // ctx.strokeStyle = 'rgba(255, 255, 255, 0.4)'
  // ctx.lineWidth = 2
  // ctx.stroke()
  //
  // // 绘制边框上的装饰点 (Lights)
  // const lightCount = 16
  // const lightRadius = wheelRadius - rimThickness / 2
  //
  // for (let i = 0; i < lightCount; i++) {
  //   const angle = (i * 2 * Math.PI) / lightCount
  //   const lightX = centerX + lightRadius * Math.cos(angle)
  //   const lightY = centerY + lightRadius * Math.sin(angle)
  //
  //   ctx.beginPath()
  //   ctx.arc(lightX, lightY, 3, 0, 2 * Math.PI)
  //   const isLit = i % 2 === 0
  //   ctx.fillStyle = isLit ? '#FFFFFF' : '#FFD700'
  //   ctx.shadowColor = isLit ? 'rgba(255, 255, 255, 0.8)' : 'rgba(255, 215, 0, 0.6)'
  //   ctx.shadowBlur = 4
  //   ctx.fill()
  //   ctx.shadowBlur = 0
  // }
  // ctx.restore()

  // 扇形绘制区域半径 = 整个转盘
  const contentRadiusToUse = size / 2 - 5

  // 5. 绘制扇形区域
  const totalWeight = props.contents.reduce((sum, item) => sum + (item.weight || 1), 0)
  if (totalWeight === 0) return

  const rotationRad = (currentRotation.value * Math.PI) / 180
  let currentAngle = -Math.PI / 2 + rotationRad

  props.contents.forEach((item, index) => {
    const sliceAngle = ((item.weight || 1) / totalWeight) * 2 * Math.PI

    // 扇形背景
    const hue = (index * 360 / props.contents.length + 330) % 360
    ctx.beginPath()
    ctx.moveTo(centerX, centerY)
    ctx.arc(centerX, centerY, contentRadiusToUse, currentAngle, currentAngle + sliceAngle)
    ctx.closePath()

    // 使用渐变增加质感
    try {
      const gradient = (ctx as any).createRadialGradient(centerX, centerY, 0, centerX, centerY, contentRadiusToUse)
      if (gradient) {
        gradient.addColorStop(0, `hsl(${hue}, 85%, 70%)`)
        gradient.addColorStop(1, `hsl(${hue}, 85%, 55%)`)
        ctx.fillStyle = gradient
      } else {
        ctx.fillStyle = `hsl(${hue}, 85%, 60%)`
      }
    } catch (e) {
      ctx.fillStyle = `hsl(${hue}, 85%, 60%)`
    }
    ctx.fill()

    // 扇形边框
    ctx.strokeStyle = 'rgba(255, 255, 255, 0.5)'
    ctx.lineWidth = 1
    ctx.stroke()

    // 绘制径向文本
    if (item.contentText) {
      ctx.save()
      ctx.translate(centerX, centerY)

      // 旋转到扇形中心
      const textAngle = currentAngle + sliceAngle / 2
      ctx.rotate(textAngle)

      const canvasCtx = ctx as any
      // 设置文本样式
      if (typeof canvasCtx.setTextAlign === 'function') {
        canvasCtx.setTextAlign('right')
      } else {
        canvasCtx.textAlign = 'right'
      }

      if (typeof canvasCtx.setTextBaseline === 'function') {
        canvasCtx.setTextBaseline('middle')
      } else {
        canvasCtx.textBaseline = 'middle'
      }

      ctx.fillStyle = '#ffffff'

      // 根据长度计算字号
      const textLength = item.contentText.length
      let fontSize = 14
      if (textLength > 8) fontSize = 11
      else if (textLength > 5) fontSize = 13

      ctx.font = `bold ${fontSize}px sans-serif`

      // 绘制文本
      ctx.fillText(item.contentText, contentRadiusToUse - 15, 0)

      ctx.restore()
    }

    currentAngle += sliceAngle
  })

  // 6. 绘制中心按钮基座
  ctx.save()
  ctx.beginPath()
  ctx.arc(centerX, centerY, 38, 0, 2 * Math.PI)
  ctx.fillStyle = '#FFFFFF'
  ctx.fill()
  ctx.restore()

  // #ifndef H5
  ctx.draw()
  // #endif
}

// 执行转盘动画 - ease-out 缓动
function animateTo(targetRotation: number, duration: number) {
  const startTime = Date.now()
  const startRotation = currentRotation.value

  // 确保至少旋转 3 圈（1080度）
  const minTarget = currentRotation.value + 1080
  let finalTarget = targetRotation
  while (finalTarget < minTarget) {
    finalTarget += 360
  }

  function animate() {
    const elapsed = Date.now() - startTime
    const progress = Math.min(elapsed / duration, 1)

    // ease-out-quart 缓动函数
    const easeProgress = 1 - Math.pow(1 - progress, 4)
    currentRotation.value = startRotation + (finalTarget - startRotation) * easeProgress

    // 重新绘制转盘
    drawWheel()

    if (progress < 1) {
      // #ifdef H5
      animationFrame.value = requestAnimationFrame(animate)
      // #endif
      // #ifndef H5
      setTimeout(animate, 1000 / 60)
      // #endif
    } else {
      // 动画完成
      currentRotation.value = finalTarget
      drawWheel()

      // 显示结果弹窗
      if (result.value) {
        showResultPopup.value = true
        emit('result', result.value)
      }
    }
  }

  animate()
}

// 动画配置接口
interface AnimationConfig {
  content: WheelContent
  rotationAngle: number
  spinDuration: number
}

// 暴露给父组件的方法：开始旋转动画
const playAnimation = (config: AnimationConfig) => {
  if (props.contents.length === 0) return

  result.value = config.content
  const targetRotation = config.rotationAngle
  const duration = config.spinDuration || 4000

  animateTo(targetRotation, duration)
}

// 处理转盘初始点击
function handleSpin() {
  if (props.isSpinning || props.contents.length === 0) return
  showResultPopup.value = false
  emit('spin')
  result.value = null
}

// 关闭结果弹窗
function closeResultPopup() {
  showResultPopup.value = false
}

// 监听内容变化，重绘转盘
watch(
  () => props.contents,
  (newContents) => {
    if (newContents && newContents.length > 0) {
      nextTick(() => {
        setTimeout(() => {
          if (!canvasContext.value) {
            initCanvas()
          } else {
            drawWheel()
          }
        }, 100)
      })
    }
  },
  { deep: true }
)

onMounted(() => {
  setTimeout(() => {
    initCanvas()
  }, 300)
})

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
  padding: 30rpx;
}

.wheel-container {
  position: relative;
  display: flex;
  justify-content: center;
  align-items: center;
}

.wheel-canvas-container {
  position: relative;
  display: flex;
  justify-content: center;
  align-items: center;
}

.wheel-canvas {
  display: block;
}

/* 指针样式 - 固定在顶部中央 */
.wheel-pointer {
  position: absolute;
  top: -5rpx;
  left: 50%;
  transform: translateX(-50%);
  z-index: 20;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.pointer-arrow {
  width: 0;
  height: 0;
  border-left: 24rpx solid transparent;
  border-right: 24rpx solid transparent;
  border-top: 50rpx solid #FF1493;
  filter: drop-shadow(0 4rpx 8rpx rgba(255, 20, 147, 0.4));
}

/* 开始按钮 - 固定在中心 */
.spin-button {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 100rpx;
  height: 100rpx;
  background: linear-gradient(135deg, #FF69B4, #FF1493);
  color: white;
  border: 6rpx solid #ffffff;
  border-radius: 50%;
  font-size: 24rpx;
  font-weight: bold;
  box-shadow: 0 6rpx 20rpx rgba(255, 20, 147, 0.5);
  transition: all 0.3s ease;
  z-index: 15;
  display: flex;
  justify-content: center;
  align-items: center;
}

.spin-button:active {
  transform: translate(-50%, -50%) scale(0.92);
}

.spin-button:disabled {
  opacity: 0.7;
  transform: translate(-50%, -50%);
}

.spin-button.spinning {
  animation: pulse 0.5s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% { box-shadow: 0 0 0 0 rgba(255, 105, 180, 0.6); }
  50% { box-shadow: 0 0 0 15rpx rgba(255, 105, 180, 0); }
}

/* 结果弹窗 */
.result-popup {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

.result-popup-content {
  background: linear-gradient(135deg, #ffffff 0%, #fff5f8 100%);
  border-radius: 30rpx;
  padding: 50rpx 40rpx;
  width: 80%;
  max-width: 500rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  box-shadow: 0 20rpx 60rpx rgba(255, 105, 180, 0.3);
  animation: slideUp 0.4s ease;
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(60rpx);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.result-icon {
  font-size: 80rpx;
  margin-bottom: 20rpx;
}

.result-title {
  font-size: 32rpx;
  color: #999;
  margin-bottom: 30rpx;
}

.result-text {
  font-size: 36rpx;
  font-weight: bold;
  color: #FF1493;
  text-align: center;
  line-height: 1.5;
  margin-bottom: 40rpx;
  max-width: 100%;
}

.result-close-btn {
  width: 100%;
  height: 80rpx;
  background: linear-gradient(135deg, #FF69B4, #FF1493);
  color: white;
  border: none;
  border-radius: 40rpx;
  font-size: 30rpx;
  font-weight: bold;
  box-shadow: 0 6rpx 20rpx rgba(255, 20, 147, 0.4);
}

.result-close-btn:active {
  transform: scale(0.98);
}
</style>
