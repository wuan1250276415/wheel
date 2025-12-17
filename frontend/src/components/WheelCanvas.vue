<template>
  <view class="wheel-container">
    <canvas
      canvas-id="wheelCanvas"
      class="wheel-canvas"
      :style="{ width: canvasSize + 'px', height: canvasSize + 'px' }"
    ></canvas>

    <!-- 指针 -->
    <view class="wheel-pointer"></view>

    <!-- 开始按钮 -->
    <button
      class="spin-button"
      :class="{ spinning: isSpinning }"
      @click="handleSpin"
      :disabled="isSpinning || contents.length === 0"
    >
      {{ isSpinning ? '转盘中...' : '开始' }}
    </button>

    <!-- 结果显示 -->
    <view v-if="result" class="result-display">
      <text class="result-text">{{ result.contentText }}</text>
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
  size: 300,
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

// 初始化画布
function initCanvas() {
  const query = uni.createSelectorQuery()
  query
    .select('.wheel-canvas')
    .fields({ node: true, size: true })
    .exec((res) => {
      if (res[0]) {
        const canvas = res[0].node as HTMLCanvasElement
        const ctx = canvas.getContext('2d') as UniApp.CanvasContext

        // 设置画布大小
        canvas.width = canvasSize.value
        canvas.height = canvasSize.value

        canvasContext.value = ctx
        drawWheel()
      }
    })
}

// 绘制转盘
function drawWheel() {
  if (!canvasContext.value || props.contents.length === 0) return

  const ctx = canvasContext.value
  const centerX = canvasSize.value / 2
  const centerY = canvasSize.value / 2
  const radius = (canvasSize.value / 2) - 20

  // 清空画布
  ctx.clearRect(0, 0, canvasSize.value, canvasSize.value)

  // 计算总权重
  const totalWeight = props.contents.reduce((sum, item) => sum + item.weight, 0)

  let currentAngle = -Math.PI / 2 // 从顶部开始

  // 绘制扇形
  props.contents.forEach((item, index) => {
    const sliceAngle = (item.weight / totalWeight) * 2 * Math.PI

    // 扇形填充色（渐变色）
    const hue = (index * 360 / props.contents.length + currentRotation.value) % 360
    ctx.fillStyle = `hsl(${hue}, 70%, 60%)`
    ctx.beginPath()
    ctx.moveTo(centerX, centerY)
    ctx.arc(centerX, centerY, radius, currentAngle, currentAngle + sliceAngle)
    ctx.closePath()
    ctx.fill()

    // 扇形边框
    ctx.strokeStyle = '#fff'
    ctx.lineWidth = 2
    ctx.stroke()

    // 绘制文本
    ctx.save()
    ctx.translate(centerX, centerY)
    ctx.rotate(currentAngle + sliceAngle / 2)
    ctx.textAlign = 'center'
    ctx.fillStyle = '#fff'
    ctx.font = 'bold 14px Arial'

    const textRadius = radius * 0.7
    const text = item.contentText.length > 10
      ? item.contentText.substring(0, 10) + '...'
      : item.contentText

    ctx.fillText(text, textRadius, 0)
    ctx.restore()

    currentAngle += sliceAngle
  })

  // 绘制中心圆
  ctx.fillStyle = '#fff'
  ctx.beginPath()
  ctx.arc(centerX, centerY, 30, 0, 2 * Math.PI)
  ctx.fill()

  ctx.strokeStyle = '#FF69B4'
  ctx.lineWidth = 3
  ctx.stroke()
}

// 执行转盘动画
function animateTo(targetRotation: number, duration: number) {
  const startTime = Date.now()
  const startRotation = currentRotation.value
  const rotationDiff = targetRotation - startRotation

  function animate() {
    const elapsed = Date.now() - startTime
    const progress = Math.min(elapsed / duration, 1)

    // 使用缓动函数（ease-out）
    const easeProgress = 1 - Math.pow(1 - progress, 3)
    currentRotation.value = startRotation + rotationDiff * easeProgress

    // 重新绘制转盘
    drawWheel()

    if (progress < 1) {
      animationFrame.value = requestAnimationFrame(animate)
    } else {
      // 动画完成，显示结果
      showResult()
    }
  }

  animate()
}

// 显示转盘结果
function showResult() {
  if (props.contents.length === 0) return

  // 计算结果
  const totalWeight = props.contents.reduce((sum, item) => sum + item.weight, 0)
  let random = Math.random() * totalWeight
  let selectedItem = props.contents[0]

  for (const item of props.contents) {
    random -= item.weight
    if (random <= 0) {
      selectedItem = item
      break
    }
  }

  result.value = selectedItem
  emit('result', selectedItem)
}

// 处理转盘点击
function handleSpin() {
  if (props.isSpinning || props.contents.length === 0) return

  emit('spin')

  result.value = null

  // 计算随机旋转角度（至少3圈，最多5圈）
  const minRotation = 3 * 360
  const maxRotation = 5 * 360
  const targetRotation = currentRotation.value + Math.random() * (maxRotation - minRotation) + minRotation

  // 动画持续时间（3-5秒）
  const duration = Math.random() * 2000 + 3000

  animateTo(targetRotation, duration)
}

// 监听内容变化，重绘转盘
watch(
  () => props.contents,
  () => {
    nextTick(() => {
      drawWheel()
    })
  },
  { deep: true }
)

// 监听旋转角度变化，重绘转盘
watch(currentRotation, () => {
  nextTick(() => {
    drawWheel()
  })
})

onMounted(() => {
  initCanvas()
})
</script>

<style scoped>
.wheel-container {
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 40rpx;
}

.wheel-canvas {
  border-radius: 50%;
  box-shadow: 0 4rpx 20rpx rgba(255, 105, 180, 0.3);
}

.wheel-pointer {
  position: absolute;
  top: 30rpx;
  left: 50%;
  transform: translateX(-50%);
  width: 0;
  height: 0;
  border-left: 20rpx solid transparent;
  border-right: 20rpx solid transparent;
  border-bottom: 40rpx solid #FF1493;
  z-index: 10;
}

.spin-button {
  margin-top: 40rpx;
  width: 200rpx;
  height: 80rpx;
  background: linear-gradient(135deg, #FF69B4, #FF1493);
  color: white;
  border: none;
  border-radius: 40rpx;
  font-size: 32rpx;
  font-weight: bold;
  box-shadow: 0 4rpx 15rpx rgba(255, 20, 147, 0.4);
  transition: all 0.3s;
}

.spin-button:active {
  transform: scale(0.95);
}

.spin-button:disabled {
  opacity: 0.6;
  transform: none;
}

.spin-button.spinning {
  animation: spinning 1s linear infinite;
}

@keyframes spinning {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

.result-display {
  margin-top: 40rpx;
  padding: 30rpx;
  background: linear-gradient(135deg, #FFF0F5, #FFE4E1);
  border-radius: 20rpx;
  box-shadow: 0 4rpx 15rpx rgba(255, 105, 180, 0.2);
}

.result-text {
  font-size: 36rpx;
  font-weight: bold;
  color: #FF1493;
  text-align: center;
}
</style>
