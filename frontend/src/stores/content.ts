import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import * as contentApi from '@/api/content'
import type { WheelContent, ContentSubmitDTO, PageResponse } from '@/api/content'

export const useContentStore = defineStore('content', () => {
  // 状态
  const myContents = ref<WheelContent[]>([])
  const currentPage = ref<number>(1)
  const pageSize = ref<number>(10)
  const total = ref<number>(0)
  const loading = ref<boolean>(false)

  // 计算属性
  const hasMore = computed(() => myContents.value.length < total.value)
  
  const pendingContents = computed(() => 
    myContents.value.filter(c => c.status === 0)
  )
  
  const approvedContents = computed(() => 
    myContents.value.filter(c => c.status === 1)
  )
  
  const rejectedContents = computed(() => 
    myContents.value.filter(c => c.status === 2)
  )

  /**
   * 获取我的内容列表
   * Requirements: 3.2
   */
  async function fetchMyContents(pageNum: number = 1, size: number = 10) {
    try {
      loading.value = true
      const res = await contentApi.getMyContents(pageNum, size)
      
      if (pageNum === 1) {
        myContents.value = res.list
      } else {
        myContents.value.push(...res.list)
      }
      
      currentPage.value = pageNum
      pageSize.value = size
      total.value = res.total
      
      return { success: true, data: res }
    } catch (error: any) {
      return { success: false, message: error.message || '获取内容列表失败' }
    } finally {
      loading.value = false
    }
  }

  /**
   * 加载更多内容
   */
  async function loadMore() {
    if (!hasMore.value || loading.value) {
      return { success: false, message: '没有更多内容' }
    }
    return fetchMyContents(currentPage.value + 1, pageSize.value)
  }

  /**
   * 刷新内容列表
   */
  async function refresh() {
    return fetchMyContents(1, pageSize.value)
  }

  /**
   * 提交新内容
   * Requirements: 3.1
   */
  async function submitContent(data: ContentSubmitDTO) {
    try {
      loading.value = true
      const res = await contentApi.submitContent(data)
      
      // 刷新列表以显示新内容
      await refresh()
      
      return { success: true, data: res }
    } catch (error: any) {
      return { success: false, message: error.message || '提交内容失败' }
    } finally {
      loading.value = false
    }
  }

  /**
   * 更新内容
   * Requirements: 3.3
   */
  async function updateContent(contentId: number, data: ContentSubmitDTO) {
    try {
      loading.value = true
      await contentApi.updateContent(contentId, data)
      
      // 更新本地状态
      const index = myContents.value.findIndex(c => c.id === contentId)
      if (index !== -1) {
        myContents.value[index] = {
          ...myContents.value[index],
          contentText: data.contentText,
          categoryId: data.categoryId,
          weight: data.weight ?? myContents.value[index].weight,
          updatedAt: new Date().toISOString()
        }
      }
      
      return { success: true }
    } catch (error: any) {
      return { success: false, message: error.message || '更新内容失败' }
    } finally {
      loading.value = false
    }
  }

  /**
   * 删除内容
   * Requirements: 3.4
   */
  async function deleteContent(contentId: number) {
    try {
      loading.value = true
      await contentApi.deleteContent(contentId)
      
      // 从本地状态中移除
      myContents.value = myContents.value.filter(c => c.id !== contentId)
      total.value = Math.max(0, total.value - 1)
      
      return { success: true }
    } catch (error: any) {
      return { success: false, message: error.message || '删除内容失败' }
    } finally {
      loading.value = false
    }
  }

  /**
   * 举报内容
   * Requirements: 3.5
   */
  async function reportContent(contentId: number, reason: number, description?: string) {
    try {
      await contentApi.reportContent(contentId, reason, description)
      return { success: true }
    } catch (error: any) {
      return { success: false, message: error.message || '举报失败' }
    }
  }

  /**
   * 获取审核状态
   * Requirements: 3.6
   */
  async function getAuditStatus(contentId: number) {
    try {
      const res = await contentApi.getAuditStatus(contentId)
      return { success: true, data: res }
    } catch (error: any) {
      return { success: false, message: error.message || '获取审核状态失败' }
    }
  }

  /**
   * 重置状态
   */
  function reset() {
    myContents.value = []
    currentPage.value = 1
    total.value = 0
    loading.value = false
  }

  return {
    // 状态
    myContents,
    currentPage,
    pageSize,
    total,
    loading,
    // 计算属性
    hasMore,
    pendingContents,
    approvedContents,
    rejectedContents,
    // 方法
    fetchMyContents,
    loadMore,
    refresh,
    submitContent,
    updateContent,
    deleteContent,
    reportContent,
    getAuditStatus,
    reset
  }
})
