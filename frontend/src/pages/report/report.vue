<template>
  <view class="container">
    <!-- 加载中 -->
    <view v-if="loading" class="loading-state">
      <text class="loading-text">加载中...</text>
    </view>

    <!-- 无情侣关系提示 -->
    <view v-else-if="!hasCouple" class="no-couple-card">
      <view class="no-couple-icon">💕</view>
      <text class="no-couple-title">还没有绑定情侣关系</text>
      <text class="no-couple-desc">绑定情侣后即可查看专属报告</text>
      <button class="go-couple-btn" @click="goToCouple">去绑定</button>
    </view>

    <!-- 报告内容 -->
    <view v-else>
      <!-- 报告类型切换 -->
      <view class="type-tabs">
        <view 
          v-for="tab in reportTabs" 
          :key="tab.type"
          class="tab-item"
          :class="{ active: selectedType === tab.type }"
          @click="switchReportType(tab.type)"
        >
          <text class="tab-icon">{{ tab.icon }}</text>
          <text class="tab-name">{{ tab.name }}</text>
        </view>
      </view>

      <!-- 报告卡片 -->
      <view v-if="currentReport" class="report-card">
        <!-- 报告头部 -->
        <view class="report-header">
          <view class="report-title">
            <text class="title-text">{{ reportTypeName }}</text>
            <text class="date-range">{{ formatDateRange }}</text>
          </view>
          <button class="share-btn" @click="showShareModal">
            <text class="share-icon">📤</text>
            <text>分享</text>
          </button>
        </view>

        <!-- 认识天数 -->
        <view class="days-together-section">
          <view class="days-number">{{ reportData.daysTogether || 0 }}</view>
          <view class="days-label">我们已经在一起的天数</view>
        </view>

        <!-- 默契度评分 -->
        <view class="compatibility-section">
          <view class="score-circle">
            <view class="score-value">{{ reportData.compatibilityScore || 0 }}</view>
            <view class="score-label">默契度</view>
          </view>
          <view class="score-desc">{{ reportData.compatibilityDesc || '继续加油' }}</view>
        </view>

        <!-- 互动统计 -->
        <view class="stats-section">
          <view class="section-title">📊 互动统计</view>
          <view class="stats-grid">
            <view class="stat-item">
              <text class="stat-value">{{ reportData.totalSpins || 0 }}</text>
              <text class="stat-label">总转盘次数</text>
            </view>
            <view class="stat-item">
              <text class="stat-value">{{ reportData.chatMessageCount || 0 }}</text>
              <text class="stat-label">聊天消息</text>
            </view>
            <view class="stat-item">
              <text class="stat-value">{{ reportData.momentCount || 0 }}</text>
              <text class="stat-label">动态数量</text>
            </view>
            <view class="stat-item">
              <text class="stat-value">{{ reportData.favoriteCategory || '-' }}</text>
              <text class="stat-label">最爱分类</text>
            </view>
          </view>
        </view>

        <!-- 转盘对比 -->
        <view class="spin-compare-section">
          <view class="section-title">🎡 转盘对比</view>
          <view class="compare-bars">
            <view class="compare-item">
              <text class="compare-label">我</text>
              <view class="compare-bar-container">
                <view 
                  class="compare-bar bar-me" 
                  :style="{ width: user1SpinPercent + '%' }"
                ></view>
              </view>
              <text class="compare-value">{{ reportData.user1Spins || 0 }}</text>
            </view>
            <view class="compare-item">
              <text class="compare-label">TA</text>
              <view class="compare-bar-container">
                <view 
                  class="compare-bar bar-partner" 
                  :style="{ width: user2SpinPercent + '%' }"
                ></view>
              </view>
              <text class="compare-value">{{ reportData.user2Spins || 0 }}</text>
            </view>
          </view>
        </view>

        <!-- 活跃时间分布 -->
        <view class="active-time-section">
          <view class="section-title">⏰ 活跃时间</view>
          <view class="time-chart">
            <view 
              v-for="hour in 24" 
              :key="hour - 1"
              class="time-bar-wrapper"
            >
              <view 
                class="time-bar"
                :style="{ height: getTimeBarHeight(hour - 1) + '%' }"
              ></view>
              <text v-if="(hour - 1) % 6 === 0" class="time-label">{{ hour - 1 }}时</text>
            </view>
          </view>
        </view>

        <!-- 即将到来的纪念日 -->
        <view v-if="upcomingAnniversaries.length > 0" class="upcoming-section">
          <view class="section-title">🎂 即将到来的纪念日</view>
          <view class="anniversary-list">
            <view 
              v-for="item in upcomingAnniversaries" 
              :key="item.id"
              class="anniversary-item upcoming"
            >
              <view class="anniversary-icon">{{ getAnniversaryIcon(item.anniversaryType) }}</view>
              <view class="anniversary-info">
                <text class="anniversary-name">{{ item.name }}</text>
                <text class="anniversary-date">{{ item.anniversaryDate }}</text>
              </view>
              <view class="days-until">
                <text class="days-number-small">{{ item.daysUntil }}</text>
                <text class="days-text">天后</text>
              </view>
            </view>
          </view>
        </view>
      </view>

      <!-- 无报告数据 -->
      <view v-else class="no-report-card">
        <view class="no-report-icon">📋</view>
        <text class="no-report-title">暂无{{ reportTypeName }}数据</text>
        <text class="no-report-desc">报告将在有互动数据后自动生成</text>
      </view>

      <!-- 纪念日管理入口 -->
      <view class="anniversary-entry" @click="showAnniversaryPanel = true">
        <view class="entry-left">
          <text class="entry-icon">📅</text>
          <text class="entry-title">纪念日管理</text>
        </view>
        <view class="entry-right">
          <text class="entry-count">{{ anniversaries.length }}个纪念日</text>
          <text class="entry-arrow">›</text>
        </view>
      </view>

      <!-- 历史报告入口 -->
      <view class="history-entry" @click="goToHistory">
        <view class="entry-left">
          <text class="entry-icon">📚</text>
          <text class="entry-title">历史报告</text>
        </view>
        <view class="entry-right">
          <text class="entry-arrow">›</text>
        </view>
      </view>
    </view>

    <!-- 纪念日管理面板 -->
    <view v-if="showAnniversaryPanel" class="anniversary-panel">
      <view class="panel-mask" @click="showAnniversaryPanel = false"></view>
      <view class="panel-content">
        <view class="panel-header">
          <text class="panel-title">纪念日管理</text>
          <text class="panel-close" @click="showAnniversaryPanel = false">×</text>
        </view>
        
        <!-- 纪念日列表 -->
        <scroll-view class="anniversary-scroll" scroll-y>
          <view 
            v-for="item in anniversaries" 
            :key="item.id"
            class="anniversary-item"
          >
            <view class="anniversary-icon">{{ getAnniversaryIcon(item.anniversaryType) }}</view>
            <view class="anniversary-info">
              <text class="anniversary-name">{{ item.name }}</text>
              <text class="anniversary-date">{{ item.anniversaryDate }}</text>
              <text class="anniversary-remind">提前{{ item.remindDays }}天提醒</text>
            </view>
            <view class="anniversary-actions">
              <button class="action-btn edit-btn" @click="editAnniversary(item)">编辑</button>
              <button class="action-btn delete-btn" @click="confirmDeleteAnniversary(item)">删除</button>
            </view>
          </view>
          
          <view v-if="anniversaries.length === 0" class="empty-list">
            <text>暂无纪念日</text>
          </view>
        </scroll-view>
        
        <!-- 添加纪念日按钮 -->
        <button class="add-anniversary-btn" @click="showAddForm">
          <text class="add-icon">+</text>
          <text>添加纪念日</text>
        </button>
      </view>
    </view>

    <!-- 添加/编辑纪念日表单 -->
    <view v-if="showAnniversaryForm" class="form-panel">
      <view class="panel-mask" @click="closeAnniversaryForm"></view>
      <view class="panel-content form-content">
        <view class="panel-header">
          <text class="panel-title">{{ editingAnniversary ? '编辑纪念日' : '添加纪念日' }}</text>
          <text class="panel-close" @click="closeAnniversaryForm">×</text>
        </view>
        
        <view class="form-body">
          <!-- 纪念日类型 -->
          <view class="form-item">
            <text class="form-label">类型</text>
            <view class="type-selector">
              <view 
                v-for="type in anniversaryTypes" 
                :key="type.value"
                class="type-option"
                :class="{ active: formData.anniversaryType === type.value }"
                @click="formData.anniversaryType = type.value"
              >
                <text class="type-icon">{{ type.icon }}</text>
                <text class="type-name">{{ type.name }}</text>
              </view>
            </view>
          </view>
          
          <!-- 纪念日名称 -->
          <view class="form-item">
            <text class="form-label">名称</text>
            <input 
              v-model="formData.name" 
              class="form-input"
              placeholder="请输入纪念日名称"
              maxlength="100"
            />
          </view>
          
          <!-- 纪念日日期 -->
          <view class="form-item">
            <text class="form-label">日期</text>
            <picker 
              mode="date" 
              :value="formData.anniversaryDate"
              @change="onDateChange"
            >
              <view class="date-picker-display">
                <text>{{ formData.anniversaryDate || '请选择日期' }}</text>
                <text class="picker-arrow">›</text>
              </view>
            </picker>
          </view>
          
          <!-- 提醒天数 -->
          <view class="form-item">
            <text class="form-label">提前提醒</text>
            <picker 
              mode="selector" 
              :range="remindDaysOptions"
              :value="remindDaysIndex"
              @change="onRemindDaysChange"
            >
              <view class="date-picker-display">
                <text>提前{{ formData.remindDays }}天</text>
                <text class="picker-arrow">›</text>
              </view>
            </picker>
          </view>
        </view>
        
        <view class="form-actions">
          <button class="cancel-btn" @click="closeAnniversaryForm">取消</button>
          <button class="submit-btn" @click="submitAnniversary" :disabled="!isFormValid">
            {{ editingAnniversary ? '保存' : '添加' }}
          </button>
        </view>
      </view>
    </view>

    <!-- 分享弹窗 -->
    <view v-if="showShare" class="share-panel">
      <view class="panel-mask" @click="showShare = false"></view>
      <view class="panel-content share-content">
        <view class="panel-header">
          <text class="panel-title">分享报告</text>
          <text class="panel-close" @click="showShare = false">×</text>
        </view>
        
        <!-- 分享图片预览 -->
        <view class="share-preview">
          <image 
            v-if="shareImageData" 
            :src="'data:image/png;base64,' + shareImageData"
            class="share-image"
            mode="widthFix"
          />
          <view v-else class="share-loading">
            <text>生成中...</text>
          </view>
        </view>
        
        <!-- 主题选择 -->
        <view class="theme-selector">
          <view 
            v-for="theme in shareThemes" 
            :key="theme.value"
            class="theme-option"
            :class="{ active: selectedTheme === theme.value }"
            @click="changeShareTheme(theme.value)"
          >
            <view class="theme-color" :style="{ background: theme.color }"></view>
            <text class="theme-name">{{ theme.name }}</text>
          </view>
        </view>
        
        <!-- 分享按钮 -->
        <view class="share-actions">
          <button class="share-action-btn" @click="saveShareImage">
            <text class="action-icon">💾</text>
            <text>保存图片</text>
          </button>
          <button class="share-action-btn wechat" open-type="share">
            <text class="action-icon">💬</text>
            <text>分享好友</text>
          </button>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { onShareAppMessage } from '@dcloudio/uni-app'
import { useReportStore } from '@/stores/report'
import { getCoupleStatus } from '@/api/couple'
import type { AnniversaryVO, AnniversaryCreateDTO, AnniversaryUpdateDTO, ReportData } from '@/types/report'
import { AnniversaryType, ReportType } from '@/types/report'

const reportStore = useReportStore()

// 状态
const loading = ref(true)
const hasCouple = ref(false)
const showAnniversaryPanel = ref(false)
const showAnniversaryForm = ref(false)
const showShare = ref(false)
const editingAnniversary = ref<AnniversaryVO | null>(null)
const shareImageData = ref<string>('')
const selectedTheme = ref('default')
const selectedType = ref(1)

// 报告类型选项
const reportTabs = [
  { type: 1, name: '周报', icon: '📅' },
  { type: 2, name: '月报', icon: '📆' },
  { type: 3, name: '年报', icon: '🗓️' }
]

// 纪念日类型选项
const anniversaryTypes = [
  { value: 1, name: '恋爱纪念日', icon: '💕' },
  { value: 2, name: '生日', icon: '🎂' },
  { value: 3, name: '自定义', icon: '⭐' }
]

// 提醒天数选项
const remindDaysOptions = ['1天', '3天', '7天', '14天', '30天']
const remindDaysValues = [1, 3, 7, 14, 30]

// 分享主题
const shareThemes = [
  { value: 'default', name: '默认', color: 'linear-gradient(135deg, #FF69B4, #FF1493)' },
  { value: 'blue', name: '蓝色', color: 'linear-gradient(135deg, #4169E1, #6495ED)' },
  { value: 'purple', name: '紫色', color: 'linear-gradient(135deg, #9b59b6, #8e44ad)' }
]

// 表单数据
const formData = ref<AnniversaryCreateDTO>({
  anniversaryType: 3,
  anniversaryDate: '',
  name: '',
  remindDays: 7
})

// 计算属性
const currentReport = computed(() => reportStore.currentReport)
const reportData = computed<ReportData>(() => currentReport.value?.reportData || {} as ReportData)
const anniversaries = computed(() => reportStore.anniversaries)
const upcomingAnniversaries = computed(() => reportStore.upcomingAnniversaries)
const reportTypeName = computed(() => reportStore.reportTypeName)

const formatDateRange = computed(() => {
  if (!currentReport.value) return ''
  return `${currentReport.value.startDate} ~ ${currentReport.value.endDate}`
})

const user1SpinPercent = computed(() => {
  const total = (reportData.value.user1Spins || 0) + (reportData.value.user2Spins || 0)
  if (total === 0) return 50
  return Math.round((reportData.value.user1Spins || 0) / total * 100)
})

const user2SpinPercent = computed(() => {
  return 100 - user1SpinPercent.value
})

const remindDaysIndex = computed(() => {
  const idx = remindDaysValues.indexOf(formData.value.remindDays || 7)
  return idx >= 0 ? idx : 2
})

const isFormValid = computed(() => {
  return formData.value.name?.trim() && formData.value.anniversaryDate
})

// 页面加载
onMounted(async () => {
  await checkCoupleStatus()
  if (hasCouple.value) {
    await loadData()
  }
  loading.value = false
})

// 检查情侣状态
async function checkCoupleStatus() {
  try {
    const status = await getCoupleStatus()
    hasCouple.value = status.status === 2
  } catch (error) {
    hasCouple.value = false
  }
}

// 加载数据
async function loadData() {
  await Promise.all([
    reportStore.fetchCurrentReport(selectedType.value),
    reportStore.fetchAnniversaries()
  ])
}

// 切换报告类型
async function switchReportType(type: number) {
  selectedType.value = type
  await reportStore.switchReportType(type)
}

// 获取时间柱高度
function getTimeBarHeight(hour: number): number {
  const distribution = reportData.value.activeTimeDistribution || {}
  const maxCount = Math.max(...Object.values(distribution), 1)
  const count = distribution[hour] || 0
  return Math.round(count / maxCount * 100)
}

// 获取纪念日图标
function getAnniversaryIcon(type: number): string {
  const icons: Record<number, string> = {
    1: '💕',
    2: '🎂',
    3: '⭐'
  }
  return icons[type] || '⭐'
}

// 显示添加表单
function showAddForm() {
  editingAnniversary.value = null
  formData.value = {
    anniversaryType: 3,
    anniversaryDate: getTodayString(),
    name: '',
    remindDays: 7
  }
  showAnniversaryForm.value = true
}

// 编辑纪念日
function editAnniversary(item: AnniversaryVO) {
  editingAnniversary.value = item
  formData.value = {
    anniversaryType: item.anniversaryType,
    anniversaryDate: item.anniversaryDate,
    name: item.name,
    remindDays: item.remindDays
  }
  showAnniversaryForm.value = true
}

// 关闭表单
function closeAnniversaryForm() {
  showAnniversaryForm.value = false
  editingAnniversary.value = null
}

// 日期选择
function onDateChange(e: any) {
  formData.value.anniversaryDate = e.detail.value
}

// 提醒天数选择
function onRemindDaysChange(e: any) {
  formData.value.remindDays = remindDaysValues[e.detail.value]
}

// 提交纪念日
async function submitAnniversary() {
  if (!isFormValid.value) return
  
  try {
    if (editingAnniversary.value) {
      const updateData: AnniversaryUpdateDTO = {
        anniversaryType: formData.value.anniversaryType,
        anniversaryDate: formData.value.anniversaryDate,
        name: formData.value.name,
        remindDays: formData.value.remindDays
      }
      await reportStore.updateAnniversary(editingAnniversary.value.id, updateData)
      uni.showToast({ title: '更新成功', icon: 'success' })
    } else {
      await reportStore.addAnniversary(formData.value)
      uni.showToast({ title: '添加成功', icon: 'success' })
    }
    closeAnniversaryForm()
  } catch (error: any) {
    uni.showToast({ title: error.message || '操作失败', icon: 'none' })
  }
}

// 确认删除纪念日
function confirmDeleteAnniversary(item: AnniversaryVO) {
  uni.showModal({
    title: '删除纪念日',
    content: `确定要删除"${item.name}"吗？`,
    confirmColor: '#DC143C',
    success: async (res) => {
      if (res.confirm) {
        await deleteAnniversary(item.id)
      }
    }
  })
}

// 删除纪念日
async function deleteAnniversary(id: number) {
  try {
    await reportStore.deleteAnniversary(id)
    uni.showToast({ title: '删除成功', icon: 'success' })
  } catch (error: any) {
    uni.showToast({ title: error.message || '删除失败', icon: 'none' })
  }
}

// 显示分享弹窗
async function showShareModal() {
  if (!currentReport.value) return
  showShare.value = true
  await generateShareImage()
}

// 生成分享图片
async function generateShareImage() {
  if (!currentReport.value) return
  
  shareImageData.value = ''
  const result = await reportStore.generateShareImage(currentReport.value.id, selectedTheme.value)
  if (result.success && result.data) {
    shareImageData.value = result.data.imageBase64
  }
}

// 切换分享主题
async function changeShareTheme(theme: string) {
  selectedTheme.value = theme
  await generateShareImage()
}

// 保存分享图片
function saveShareImage() {
  if (!shareImageData.value) {
    uni.showToast({ title: '图片未生成', icon: 'none' })
    return
  }
  
  // #ifdef MP-WEIXIN
  const fsm = uni.getFileSystemManager()
  const filePath = `${wx.env.USER_DATA_PATH}/report_${Date.now()}.png`
  
  fsm.writeFile({
    filePath,
    data: shareImageData.value,
    encoding: 'base64',
    success: () => {
      uni.saveImageToPhotosAlbum({
        filePath,
        success: () => {
          uni.showToast({ title: '保存成功', icon: 'success' })
        },
        fail: () => {
          uni.showToast({ title: '保存失败', icon: 'none' })
        }
      })
    },
    fail: () => {
      uni.showToast({ title: '保存失败', icon: 'none' })
    }
  })
  // #endif
  
  // #ifdef H5
  const link = document.createElement('a')
  link.href = 'data:image/png;base64,' + shareImageData.value
  link.download = `couple_report_${Date.now()}.png`
  link.click()
  uni.showToast({ title: '下载成功', icon: 'success' })
  // #endif
}

// 跳转到情侣页面
function goToCouple() {
  uni.navigateTo({ url: '/pages/couple/couple' })
}

// 跳转到历史报告
function goToHistory() {
  uni.showToast({ title: '功能开发中', icon: 'none' })
}

// 获取今天日期字符串
function getTodayString(): string {
  const date = new Date()
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}

// 微信分享配置
onShareAppMessage(() => {
  return {
    title: '我们的情侣报告',
    path: '/pages/report/report',
    imageUrl: shareImageData.value ? 'data:image/png;base64,' + shareImageData.value : ''
  }
})
</script>

<style scoped>
.container {
  min-height: 100vh;
  background: linear-gradient(180deg, #FFF0F5 0%, #FFFFFF 100%);
  padding: 20rpx;
  padding-bottom: 40rpx;
}

.loading-state {
  display: flex;
  justify-content: center;
  padding-top: 200rpx;
}

.loading-text {
  color: #999;
  font-size: 28rpx;
}

/* 无情侣关系 */
.no-couple-card {
  background: #fff;
  border-radius: 20rpx;
  padding: 80rpx 40rpx;
  margin-top: 100rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  box-shadow: 0 4rpx 15rpx rgba(0, 0, 0, 0.05);
}

.no-couple-icon {
  font-size: 100rpx;
  margin-bottom: 30rpx;
}

.no-couple-title {
  font-size: 36rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 15rpx;
}

.no-couple-desc {
  font-size: 28rpx;
  color: #999;
  margin-bottom: 40rpx;
}

.go-couple-btn {
  width: 300rpx;
  height: 80rpx;
  background: linear-gradient(135deg, #FF69B4, #FF1493);
  color: #fff;
  border: none;
  border-radius: 40rpx;
  font-size: 30rpx;
  font-weight: bold;
}

/* 报告类型切换 */
.type-tabs {
  display: flex;
  background: #fff;
  border-radius: 20rpx;
  padding: 10rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 4rpx 15rpx rgba(0, 0, 0, 0.05);
}

.tab-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20rpx;
  border-radius: 15rpx;
  transition: all 0.3s;
}

.tab-item.active {
  background: linear-gradient(135deg, #FF69B4, #FF1493);
}

.tab-icon {
  font-size: 36rpx;
  margin-bottom: 8rpx;
}

.tab-name {
  font-size: 26rpx;
  color: #666;
}

.tab-item.active .tab-name {
  color: #fff;
  font-weight: bold;
}

/* 报告卡片 */
.report-card {
  background: #fff;
  border-radius: 20rpx;
  padding: 30rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 4rpx 15rpx rgba(0, 0, 0, 0.05);
}

.report-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 30rpx;
}

.report-title {
  display: flex;
  flex-direction: column;
}

.title-text {
  font-size: 36rpx;
  font-weight: bold;
  color: #333;
}

.date-range {
  font-size: 24rpx;
  color: #999;
  margin-top: 8rpx;
}

.share-btn {
  display: flex;
  align-items: center;
  padding: 12rpx 24rpx;
  background: linear-gradient(135deg, #FF69B4, #FF1493);
  color: #fff;
  border: none;
  border-radius: 30rpx;
  font-size: 26rpx;
}

.share-icon {
  margin-right: 8rpx;
}

/* 认识天数 */
.days-together-section {
  text-align: center;
  padding: 40rpx 0;
  border-bottom: 1rpx solid #f0f0f0;
}

.days-number {
  font-size: 100rpx;
  font-weight: bold;
  color: #FF1493;
  line-height: 1;
}

.days-label {
  font-size: 28rpx;
  color: #999;
  margin-top: 15rpx;
}

/* 默契度 */
.compatibility-section {
  display: flex;
  align-items: center;
  padding: 40rpx 0;
  border-bottom: 1rpx solid #f0f0f0;
}

.score-circle {
  width: 160rpx;
  height: 160rpx;
  border-radius: 50%;
  background: linear-gradient(135deg, #FFD700, #FFA500);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  margin-right: 30rpx;
}

.score-value {
  font-size: 48rpx;
  font-weight: bold;
  color: #fff;
}

.score-label {
  font-size: 22rpx;
  color: #fff;
  opacity: 0.9;
}

.score-desc {
  flex: 1;
  font-size: 32rpx;
  color: #333;
  font-weight: bold;
}

/* 统计区域 */
.stats-section, .spin-compare-section, .active-time-section, .upcoming-section {
  padding: 30rpx 0;
  border-bottom: 1rpx solid #f0f0f0;
}

.section-title {
  font-size: 30rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 20rpx;
}

.stats-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20rpx;
}

.stat-item {
  background: #FFF0F5;
  border-radius: 15rpx;
  padding: 20rpx;
  text-align: center;
}

.stat-value {
  display: block;
  font-size: 36rpx;
  font-weight: bold;
  color: #FF1493;
  margin-bottom: 8rpx;
}

.stat-label {
  font-size: 24rpx;
  color: #999;
}

/* 转盘对比 */
.compare-bars {
  display: flex;
  flex-direction: column;
  gap: 15rpx;
}

.compare-item {
  display: flex;
  align-items: center;
}

.compare-label {
  width: 60rpx;
  font-size: 26rpx;
  color: #666;
}

.compare-bar-container {
  flex: 1;
  height: 30rpx;
  background: #f5f5f5;
  border-radius: 15rpx;
  overflow: hidden;
  margin: 0 15rpx;
}

.compare-bar {
  height: 100%;
  border-radius: 15rpx;
  transition: width 0.3s;
}

.bar-me {
  background: linear-gradient(90deg, #FF69B4, #FF1493);
}

.bar-partner {
  background: linear-gradient(90deg, #4169E1, #6495ED);
}

.compare-value {
  width: 80rpx;
  text-align: right;
  font-size: 26rpx;
  color: #666;
}

/* 活跃时间 */
.time-chart {
  display: flex;
  align-items: flex-end;
  height: 150rpx;
  padding-top: 20rpx;
}

.time-bar-wrapper {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  height: 100%;
}

.time-bar {
  width: 80%;
  background: linear-gradient(180deg, #FF69B4, #FF1493);
  border-radius: 4rpx 4rpx 0 0;
  min-height: 4rpx;
  margin-top: auto;
}

.time-label {
  font-size: 18rpx;
  color: #999;
  margin-top: 8rpx;
}

/* 即将到来的纪念日 */
.anniversary-list {
  display: flex;
  flex-direction: column;
  gap: 15rpx;
}

.anniversary-item {
  display: flex;
  align-items: center;
  padding: 20rpx;
  background: #FFF0F5;
  border-radius: 15rpx;
}

.anniversary-item.upcoming {
  background: linear-gradient(135deg, #FFF0F5, #FFE4E1);
  border: 2rpx solid #FF69B4;
}

.anniversary-icon {
  font-size: 40rpx;
  margin-right: 20rpx;
}

.anniversary-info {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.anniversary-name {
  font-size: 28rpx;
  color: #333;
  font-weight: bold;
}

.anniversary-date {
  font-size: 24rpx;
  color: #999;
  margin-top: 4rpx;
}

.anniversary-remind {
  font-size: 22rpx;
  color: #FF69B4;
  margin-top: 4rpx;
}

.days-until {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.days-number-small {
  font-size: 36rpx;
  font-weight: bold;
  color: #FF1493;
}

.days-text {
  font-size: 22rpx;
  color: #999;
}

/* 无报告 */
.no-report-card {
  background: #fff;
  border-radius: 20rpx;
  padding: 80rpx 40rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  box-shadow: 0 4rpx 15rpx rgba(0, 0, 0, 0.05);
  margin-bottom: 20rpx;
}

.no-report-icon {
  font-size: 80rpx;
  margin-bottom: 20rpx;
}

.no-report-title {
  font-size: 32rpx;
  color: #333;
  margin-bottom: 10rpx;
}

.no-report-desc {
  font-size: 26rpx;
  color: #999;
}

/* 入口卡片 */
.anniversary-entry, .history-entry {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #fff;
  border-radius: 20rpx;
  padding: 30rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 4rpx 15rpx rgba(0, 0, 0, 0.05);
}

.entry-left {
  display: flex;
  align-items: center;
}

.entry-icon {
  font-size: 36rpx;
  margin-right: 15rpx;
}

.entry-title {
  font-size: 30rpx;
  color: #333;
}

.entry-right {
  display: flex;
  align-items: center;
}

.entry-count {
  font-size: 26rpx;
  color: #999;
  margin-right: 10rpx;
}

.entry-arrow {
  font-size: 32rpx;
  color: #ccc;
}

/* 面板通用样式 */
.anniversary-panel, .form-panel, .share-panel {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 1000;
}

.panel-mask {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
}

.panel-content {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  background: #fff;
  border-radius: 30rpx 30rpx 0 0;
  max-height: 80vh;
  display: flex;
  flex-direction: column;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 30rpx;
  border-bottom: 1rpx solid #f0f0f0;
}

.panel-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
}

.panel-close {
  font-size: 48rpx;
  color: #999;
  line-height: 1;
}

/* 纪念日面板 */
.anniversary-scroll {
  flex: 1;
  padding: 20rpx 30rpx;
  max-height: 50vh;
}

.anniversary-actions {
  display: flex;
  gap: 15rpx;
}

.action-btn {
  padding: 8rpx 20rpx;
  font-size: 24rpx;
  border-radius: 20rpx;
  border: none;
}

.edit-btn {
  background: #f5f5f5;
  color: #666;
}

.delete-btn {
  background: #FFE4E1;
  color: #DC143C;
}

.empty-list {
  text-align: center;
  padding: 60rpx 0;
  color: #999;
  font-size: 28rpx;
}

.add-anniversary-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 20rpx 30rpx 40rpx;
  height: 90rpx;
  background: linear-gradient(135deg, #FF69B4, #FF1493);
  color: #fff;
  border: none;
  border-radius: 45rpx;
  font-size: 30rpx;
  font-weight: bold;
}

.add-icon {
  font-size: 36rpx;
  margin-right: 10rpx;
}

/* 表单面板 */
.form-content {
  max-height: 70vh;
}

.form-body {
  padding: 30rpx;
  flex: 1;
  overflow-y: auto;
}

.form-item {
  margin-bottom: 30rpx;
}

.form-label {
  display: block;
  font-size: 28rpx;
  color: #333;
  margin-bottom: 15rpx;
}

.type-selector {
  display: flex;
  gap: 15rpx;
}

.type-option {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20rpx;
  background: #f5f5f5;
  border-radius: 15rpx;
  border: 2rpx solid transparent;
}

.type-option.active {
  background: #FFF0F5;
  border-color: #FF69B4;
}

.type-icon {
  font-size: 36rpx;
  margin-bottom: 8rpx;
}

.type-name {
  font-size: 24rpx;
  color: #666;
}

.type-option.active .type-name {
  color: #FF1493;
}

.form-input {
  width: 100%;
  height: 80rpx;
  padding: 0 20rpx;
  background: #f5f5f5;
  border-radius: 15rpx;
  font-size: 28rpx;
}

.date-picker-display {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 80rpx;
  padding: 0 20rpx;
  background: #f5f5f5;
  border-radius: 15rpx;
  font-size: 28rpx;
  color: #333;
}

.picker-arrow {
  font-size: 32rpx;
  color: #ccc;
}

.form-actions {
  display: flex;
  gap: 20rpx;
  padding: 20rpx 30rpx 40rpx;
  border-top: 1rpx solid #f0f0f0;
}

.cancel-btn {
  flex: 1;
  height: 90rpx;
  background: #f5f5f5;
  color: #666;
  border: none;
  border-radius: 45rpx;
  font-size: 30rpx;
}

.submit-btn {
  flex: 1;
  height: 90rpx;
  background: linear-gradient(135deg, #FF69B4, #FF1493);
  color: #fff;
  border: none;
  border-radius: 45rpx;
  font-size: 30rpx;
  font-weight: bold;
}

.submit-btn:disabled {
  opacity: 0.6;
}

/* 分享面板 */
.share-content {
  max-height: 85vh;
}

.share-preview {
  padding: 30rpx;
  display: flex;
  justify-content: center;
  background: #f5f5f5;
}

.share-image {
  width: 500rpx;
  border-radius: 20rpx;
  box-shadow: 0 8rpx 20rpx rgba(0, 0, 0, 0.1);
}

.share-loading {
  width: 500rpx;
  height: 600rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fff;
  border-radius: 20rpx;
  color: #999;
}

.theme-selector {
  display: flex;
  justify-content: center;
  gap: 30rpx;
  padding: 30rpx;
}

.theme-option {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 15rpx;
  border-radius: 15rpx;
  border: 2rpx solid transparent;
}

.theme-option.active {
  border-color: #FF69B4;
  background: #FFF0F5;
}

.theme-color {
  width: 60rpx;
  height: 60rpx;
  border-radius: 50%;
  margin-bottom: 10rpx;
}

.theme-name {
  font-size: 24rpx;
  color: #666;
}

.share-actions {
  display: flex;
  gap: 20rpx;
  padding: 20rpx 30rpx 40rpx;
}

.share-action-btn {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  height: 90rpx;
  background: #f5f5f5;
  color: #333;
  border: none;
  border-radius: 45rpx;
  font-size: 28rpx;
}

.share-action-btn.wechat {
  background: #07C160;
  color: #fff;
}

.action-icon {
  font-size: 32rpx;
  margin-right: 10rpx;
}
</style>
