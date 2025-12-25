import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import * as wheelApi from '@/api/wheel'
import type { WheelCategory, WheelContent, WheelSpinRecord, UserStats } from '@/api/wheel'

export type { WheelCategory, WheelContent, WheelSpinRecord, UserStats }

export const useWheelStore = defineStore('wheel', () => {
  // 状态
  const categories = ref<WheelCategory[]>([])
  const contents = ref<WheelContent[]>([])
  const selectedCategoryIds = ref<number[]>([])
  const isSpinning = ref(false)
  const lastResult = ref<WheelContent | null>(null)
  const history = ref<WheelSpinRecord[]>([])
  const stats = ref<UserStats | null>(null)
  const historyTotal = ref(0)

  // 计算属性
  const availableCategories = computed(() => {
    return categories.value.sort((a, b) => a.sortOrder - b.sortOrder)
  })

  const totalWeight = computed(() => {
    return contents.value.reduce((sum, content) => sum + content.weight, 0)
  })

  const hasMoreHistory = computed(() => {
    return history.value.length < historyTotal.value
  })

  // 获取转盘分类
  async function fetchCategories() {
    try {
      const data = await wheelApi.getCategories()
      categories.value = data
      return { success: true, data }
    } catch (error: any) {
      return { success: false, message: error.message }
    }
  }

  /**
   * 获取转盘内容
   * @param categoryIds 分类ID数组，必传
   */
  async function fetchContents(categoryIds: number[]) {
    try {
      selectedCategoryIds.value = categoryIds
      const data = await wheelApi.getContents(categoryIds)
      contents.value = data
      return { success: true, data }
    } catch (error: any) {
      return { success: false, message: error.message }
    }
  }

  // 执行转盘
  async function spin(categoryIds?: number[], config?: { radius?: number; animationDuration?: number }) {
    if (isSpinning.value) {
      return { success: false, message: '转盘正在运行中' }
    }

    try {
      isSpinning.value = true
      const data = await wheelApi.spin({
        categoryIds: categoryIds || [],
        radius: config?.radius || 175,
        animationDuration: config?.animationDuration || 3000
      })

      const targetContent: WheelContent = {
        id: data.contentId,
        categoryId: data.categoryId,
        contentText: data.resultText,
        weight: 1,
        status: 1,
        isSystem: false
      }

      lastResult.value = targetContent

      return {
        success: true,
        data: {
          content: targetContent,
          angle: data.rotationAngle,
          duration: data.spinDuration
        }
      }
    } catch (error: any) {
      return { success: false, message: error.message }
    } finally {
      isSpinning.value = false
    }
  }

  // 获取历史记录（使用pageNum/pageSize）
  async function fetchHistory(pageNum: number = 1, pageSize: number = 20) {
    try {
      const data = await wheelApi.getHistory(pageNum, pageSize)

      if (pageNum === 1) {
        history.value = data.records
      } else {
        history.value.push(...data.records)
      }
      historyTotal.value = data.total

      return {
        success: true,
        data: {
          list: data.records,
          total: data.total,
          hasMore: history.value.length < data.total
        }
      }
    } catch (error: any) {
      return { success: false, message: error.message }
    }
  }

  // 获取用户统计
  async function fetchStats() {
    try {
      const data = await wheelApi.getStats()
      stats.value = data
      return { success: true, data }
    } catch (error: any) {
      return { success: false, message: error.message }
    }
  }

  // 重置状态
  function reset() {
    isSpinning.value = false
    lastResult.value = null
  }

  return {
    // 状态
    categories,
    contents,
    selectedCategoryIds,
    isSpinning,
    lastResult,
    history,
    stats,
    historyTotal,
    // 计算属性
    availableCategories,
    totalWeight,
    hasMoreHistory,
    // 方法
    fetchCategories,
    fetchContents,
    spin,
    fetchHistory,
    fetchStats,
    reset
  }
})
