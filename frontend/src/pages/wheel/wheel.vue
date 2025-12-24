<template>
  <view class="container">
    <!-- 分类选择 -->
    <view class="category-selector">
      <scroll-view class="category-list" scroll-x>
        <view
          v-for="category in categories"
          :key="category.id"
          class="category-item"
          :class="{ active: selectedCategoryIds.includes(category.id) }"
          :style="{ borderColor: selectedCategoryIds.includes(category.id) ? category.themeColor : 'transparent' }"
          @click="toggleCategory(category.id)"
        >
          <text>{{ category.categoryName }}</text>
        </view>
      </scroll-view>
      
      <view class="category-actions">
        <button class="select-all-btn" @click="selectAllCategories">全选</button>
        <button class="confirm-btn" @click="confirmSelection" :disabled="selectedCategoryIds.length === 0">
          确认选择 ({{ selectedCategoryIds.length }})
        </button>
      </view>
    </view>

    <!-- 转盘区域 -->
    <view class="wheel-section" v-if="wheelStore.contents.length > 0">
      <WheelCanvas
        :key="wheelKey"
        ref="wheelCanvasRef"
        :size="350"
        :contents="wheelStore.contents"
        :isSpinning="wheelStore.isSpinning"
        @spin="handleSpin"
        @result="handleResult"
      />

      <!-- 转盘配置 -->
      <view class="config-section" v-if="showConfig">
        <view class="config-title">转盘配置</view>

        <view class="config-item">
          <text class="config-label">半径</text>
          <slider
            :value="config.radius"
            min="100"
            max="200"
            step="10"
            @change="updateRadius"
            class="config-slider"
          />
          <text class="config-value">{{ config.radius }}px</text>
        </view>

        <view class="config-item">
          <text class="config-label">动画时长</text>
          <slider
            :value="config.animationDuration"
            min="2000"
            max="6000"
            step="500"
            @change="updateAnimationDuration"
            class="config-slider"
          />
          <text class="config-value">{{ config.animationDuration / 1000 }}s</text>
        </view>

        <button class="save-config-btn" @click="saveConfig">保存配置</button>
      </view>

      <button class="config-toggle-btn" @click="showConfig = !showConfig">
        {{ showConfig ? '隐藏配置' : '显示配置' }}
      </button>
    </view>

    <!-- 未选择分类提示 -->
    <view class="empty-tip" v-else>
      <text>请选择分类后点击"确认选择"加载转盘内容</text>
    </view>

    <!-- 历史记录 -->
    <view class="history-section" v-if="wheelStore.history.length > 0">
      <view class="section-title">
        <text>转盘历史</text>
      </view>

      <scroll-view class="history-list" scroll-y>
        <view
          class="history-item"
          v-for="item in wheelStore.history"
          :key="item.id"
        >
          <view class="history-content">
            <text class="history-text">{{ item.resultText }}</text>
            <text class="history-category">{{ getCategoryName(item.categoryId) }}</text>
          </view>
          <text class="history-time">{{ formatTime(item.spinTime) }}</text>
        </view>
      </scroll-view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useWheelStore } from '@/stores/wheel'
import WheelCanvas from '@/components/WheelCanvas.vue'

const wheelStore = useWheelStore()

const showConfig = ref(false)
const selectedCategoryIds = ref<string[]>([])
const config = ref({
  radius: 175,
  animationDuration: 3000
})

// 使用 key 强制组件重新创建，确保内容更新时 Canvas 重新渲染
const wheelKey = ref(0)

// 计算属性
const categories = computed(() => wheelStore.availableCategories)

// 页面加载
onMounted(async () => {
  await loadData()
})

// 监听 contents 变化，强制重新渲染 WheelCanvas
watch(
  () => wheelStore.contents.length,
  () => {
    // 当内容数量变化时，强制重新渲染组件
    wheelKey.value++
  }
)

// 加载数据
async function loadData() {
  // 获取分类列表
  await wheelStore.fetchCategories()

  // 获取历史记录
  await wheelStore.fetchHistory()
}

// 切换分类选择
function toggleCategory(categoryId: string) {
  const index = selectedCategoryIds.value.indexOf(categoryId)
  if (index > -1) {
    selectedCategoryIds.value.splice(index, 1)
  } else {
    selectedCategoryIds.value.push(categoryId)
  }
}

// 全选分类
function selectAllCategories() {
  if (selectedCategoryIds.value.length === categories.value.length) {
    selectedCategoryIds.value = []
  } else {
    selectedCategoryIds.value = categories.value.map(c => c.id)
  }
}

// 确认选择，加载转盘内容
async function confirmSelection() {
  if (selectedCategoryIds.value.length === 0) {
    uni.showToast({
      title: '请至少选择一个分类',
      icon: 'none'
    })
    return
  }
  
  const res = await wheelStore.fetchContents(selectedCategoryIds.value)
  if (!res.success) {
    uni.showToast({
      title: res.message || '加载内容失败',
      icon: 'none'
    })
  }
}

// 处理转盘开始
const wheelCanvasRef = ref()

async function handleSpin() {
  const res = await wheelStore.spin(selectedCategoryIds.value, {
    animationDuration: config.value.animationDuration,
    radius: config.value.radius
  })

  if (res.success && res.data) {
    // 后端 rotationAngle 计算假设转盘从 0° 开始绘制
    // 但前端 Canvas 从 -90° 开始绘制
    // 需要补偿 90° 的坐标系差异
    const adjustedAngle = res.data.angle - 90

    wheelCanvasRef.value?.playAnimation({
      content: res.data.content,
      rotationAngle: adjustedAngle,
      spinDuration: Number(res.data.duration) || config.value.animationDuration
    })
  } else {
    uni.showToast({
      title: res.message || '转盘失败',
      icon: 'none'
    })
  }
}

// 处理转盘结果 - 动画完成后调用接口获取历史记录
async function handleResult(result: any) {
  // 调用接口获取最新的历史记录
  await wheelStore.fetchHistory(1, 20)

  // 显示结果弹窗（由 WheelCanvas 组件处理）
  playSuccessSound()
}

// 播放成功音效
function playSuccessSound() {
  const audioContext = uni.createInnerAudioContext()
  audioContext.src = '/static/sounds/success.mp3'
  audioContext.play()
}

// 更新半径
function updateRadius(e: any) {
  config.value.radius = e.detail.value
}

// 更新动画时长
function updateAnimationDuration(e: any) {
  config.value.animationDuration = e.detail.value
}

// 保存配置（本地保存）
function saveConfig() {
  uni.showToast({
    title: '配置已保存',
    icon: 'success'
  })
}

// 格式化时间
function formatTime(timeStr: string) {
  const date = new Date(timeStr)
  const now = new Date()
  const diff = now.getTime() - date.getTime()

  if (diff < 60 * 1000) {
    return '刚刚'
  } else if (diff < 60 * 60 * 1000) {
    return `${Math.floor(diff / 60 / 1000)}分钟前`
  } else if (diff < 24 * 60 * 60 * 1000) {
    return `${Math.floor(diff / 60 / 60 / 1000)}小时前`
  } else {
    return date.toLocaleDateString()
  }
}

// 获取分类名称
function getCategoryName(categoryId: string) {
  const category = categories.value.find(c => c.id === categoryId)
  return category ? category.categoryName : '未知分类'
}
</script>

<style scoped>
.container {
  min-height: 100vh;
  background: linear-gradient(180deg, #FFF0F5 0%, #FFFFFF 100%);
  padding: 20rpx;
}

.category-selector {
  margin-bottom: 20rpx;
}

.category-list {
  white-space: nowrap;
  margin-bottom: 20rpx;
}

.category-item {
  display: inline-block;
  padding: 15rpx 30rpx;
  margin-right: 15rpx;
  background: #fff;
  border-radius: 30rpx;
  font-size: 28rpx;
  color: #666;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.05);
  transition: all 0.3s;
}

.category-item.active {
  background: linear-gradient(135deg, #FF69B4, #FF1493);
  color: #fff;
}

.category-actions {
  display: flex;
  gap: 20rpx;
  padding: 0 10rpx;
}

.select-all-btn {
  flex: 1;
  height: 70rpx;
  background: #fff;
  color: #FF69B4;
  border: 2rpx solid #FF69B4;
  border-radius: 35rpx;
  font-size: 28rpx;
}

.confirm-btn {
  flex: 2;
  height: 70rpx;
  background: linear-gradient(135deg, #FF69B4, #FF1493);
  color: #fff;
  border: none;
  border-radius: 35rpx;
  font-size: 28rpx;
}

.confirm-btn[disabled] {
  background: #ccc;
  color: #999;
}

.empty-tip {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 400rpx;
  color: #999;
  font-size: 28rpx;
}

.wheel-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 30rpx;
}

.config-section {
  width: 100%;
  background: #fff;
  border-radius: 20rpx;
  padding: 30rpx;
  margin-top: 30rpx;
  box-shadow: 0 4rpx 15rpx rgba(0, 0, 0, 0.05);
}

.config-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 30rpx;
  text-align: center;
}

.config-item {
  display: flex;
  align-items: center;
  margin-bottom: 30rpx;
}

.config-label {
  width: 150rpx;
  font-size: 28rpx;
  color: #666;
}

.config-slider {
  flex: 1;
  margin: 0 20rpx;
}

.config-value {
  width: 100rpx;
  text-align: right;
  font-size: 28rpx;
  color: #FF1493;
}

.save-config-btn {
  width: 100%;
  height: 80rpx;
  background: linear-gradient(135deg, #FF69B4, #FF1493);
  color: white;
  border: none;
  border-radius: 40rpx;
  font-size: 30rpx;
  font-weight: bold;
  margin-top: 20rpx;
}

.config-toggle-btn {
  width: 200rpx;
  height: 60rpx;
  background: #fff;
  color: #FF69B4;
  border: 2rpx solid #FF69B4;
  border-radius: 30rpx;
  font-size: 26rpx;
  margin-top: 20rpx;
}

.history-section {
  background: #fff;
  border-radius: 20rpx;
  padding: 30rpx;
  box-shadow: 0 4rpx 15rpx rgba(0, 0, 0, 0.05);
}

.section-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 20rpx;
}

.history-list {
  max-height: 500rpx;
}

.history-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20rpx;
  background: #FFF0F5;
  border-radius: 15rpx;
  margin-bottom: 15rpx;
}

.history-content {
  flex: 1;
  margin-right: 20rpx;
}

.history-text {
  display: block;
  font-size: 28rpx;
  color: #333;
  margin-bottom: 8rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.history-category {
  display: block;
  font-size: 24rpx;
  color: #999;
}

.history-time {
  font-size: 24rpx;
  color: #999;
  white-space: nowrap;
}
</style>
