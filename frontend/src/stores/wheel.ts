import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import * as wheelApi from '@/api/wheel'

export interface WheelContent {
  id: number
  contentText: string
  categoryId: number
  weight: number
}

export interface WheelConfig {
  id: number
  userId: number
  radius: number
  categories: number[]
  theme: string
  animationDuration: number
}

export const useWheelStore = defineStore('wheel', () => {
  // 状态
  const config = ref<WheelConfig | null>(null)
  const contents = ref<WheelContent[]>([])
  const isSpinning = ref(false)
  const lastResult = ref<WheelContent | null>(null)
  const history = ref<Array<{
    id: number
    resultText: string
    spinTime: string
    categoryName: string
  }>>([])

  // 计算属性
  const availableCategories = computed(() => {
    const categoryMap = new Map<number, string>()
    contents.value.forEach(content => {
      if (!categoryMap.has(content.categoryId)) {
        categoryMap.set(content.categoryId, `分类${content.categoryId}`)
      }
    })
    return Array.from(categoryMap.entries()).map(([id, name]) => ({
      id,
      name
    }))
  })

  const totalWeight = computed(() => {
    return contents.value.reduce((sum, content) => sum + content.weight, 0)
  })

  // 获取转盘配置
  async function fetchConfig() {
    try {
      const res = await wheelApi.getWheelConfig()

      if (res.code === 200) {
        config.value = res.data
        return { success: true }
      } else {
        return { success: false, message: res.message }
      }
    } catch (error: any) {
      return { success: false, message: error.message }
    }
  }

  // 保存转盘配置
  async function saveConfig(data: {
    radius?: number
    categories?: number[]
    theme?: string
    animationDuration?: number
  }) {
    try {
      const res = await wheelApi.saveWheelConfig(data)

      if (res.code === 200) {
        // 重新获取配置
        await fetchConfig()
        return { success: true }
      } else {
        return { success: false, message: res.message }
      }
    } catch (error: any) {
      return { success: false, message: error.message }
    }
  }

  // 获取转盘内容
  async function fetchContents(categoryId?: number) {
    try {
      const res = await wheelApi.getWheelContents(categoryId)

      if (res.code === 200) {
        contents.value = res.data
        return { success: true }
      } else {
        return { success: false, message: res.message }
      }
    } catch (error: any) {
      return { success: false, message: error.message }
    }
  }

  // 执行转盘
  async function spin(categoryIds?: number[]) {
    if (isSpinning.value) {
      return { success: false, message: '转盘正在运行中' }
    }

    try {
      isSpinning.value = true
      const res = await wheelApi.spinWheel({ categoryIds })

      if (res.code === 200) {
        lastResult.value = res.data.result

        // 将新记录添加到历史记录开头
        history.value.unshift({
          id: res.data.recordId,
          resultText: res.data.result.contentText,
          spinTime: new Date().toISOString(),
          categoryName: `分类${res.data.result.categoryId}`
        })

        // 限制历史记录数量
        if (history.value.length > 50) {
          history.value = history.value.slice(0, 50)
        }

        return {
          success: true,
          data: {
            result: res.data.result,
            angle: res.data.angle,
            duration: res.data.duration
          }
        }
      } else {
        return { success: false, message: res.message }
      }
    } catch (error: any) {
      return { success: false, message: error.message }
    } finally {
      isSpinning.value = false
    }
  }

  // 获取历史记录
  async function fetchHistory(page = 1, pageSize = 20) {
    try {
      const res = await wheelApi.getSpinHistory({ page, pageSize })

      if (res.code === 200) {
        if (page === 1) {
          history.value = res.data.list
        } else {
          history.value.push(...res.data.list)
        }

        return {
          success: true,
          data: {
            list: res.data.list,
            total: res.data.total,
            hasMore: history.value.length < res.data.total
          }
        }
      } else {
        return { success: false, message: res.message }
      }
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
    config,
    contents,
    isSpinning,
    lastResult,
    history,
    availableCategories,
    totalWeight,
    fetchConfig,
    saveConfig,
    fetchContents,
    spin,
    fetchHistory,
    reset
  }
})
