/**
 * 情侣报告状态管理
 * Requirements: 7.1
 */
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import * as reportApi from '@/api/report'
import type {
  CoupleReportVO,
  ReportQueryDTO,
  AnniversaryVO,
  AnniversaryCreateDTO,
  AnniversaryUpdateDTO,
  ShareImageVO,
  ReportType
} from '@/types/report'

export const useReportStore = defineStore('report', () => {
  // ==================== 状态 ====================
  
  /** 当前报告 */
  const currentReport = ref<CoupleReportVO | null>(null)
  
  /** 历史报告列表 */
  const reportHistory = ref<CoupleReportVO[]>([])
  
  /** 纪念日列表 */
  const anniversaries = ref<AnniversaryVO[]>([])
  
  /** 当前选中的报告类型 */
  const selectedReportType = ref<number>(1)
  
  /** 分页信息 */
  const currentPage = ref<number>(1)
  const pageSize = ref<number>(10)
  const total = ref<number>(0)
  
  /** 加载状态 */
  const loading = ref<boolean>(false)
  
  /** 分享图片数据 */
  const shareImage = ref<ShareImageVO | null>(null)

  // ==================== 计算属性 ====================
  
  /** 是否有更多历史报告 */
  const hasMore = computed(() => reportHistory.value.length < total.value)
  
  /** 即将到来的纪念日（7天内） */
  const upcomingAnniversaries = computed(() => 
    anniversaries.value.filter(a => a.daysUntil >= 0 && a.daysUntil <= 7)
  )
  
  /** 报告类型名称映射 */
  const reportTypeName = computed(() => {
    const names: Record<number, string> = {
      1: '周报',
      2: '月报',
      3: '年报'
    }
    return names[selectedReportType.value] || '周报'
  })

  // ==================== 报告相关方法 ====================

  /**
   * 获取当前报告
   * @param type 报告类型: 1-周报, 2-月报, 3-年报
   * Requirements: 1.1, 1.2
   */
  async function fetchCurrentReport(type: number = 1) {
    try {
      loading.value = true
      selectedReportType.value = type
      const data = await reportApi.getCurrentReport(type)
      currentReport.value = data
      return { success: true, data }
    } catch (error: any) {
      currentReport.value = null
      return { success: false, message: error.message || '获取报告失败' }
    } finally {
      loading.value = false
    }
  }

  /**
   * 获取历史报告列表
   * @param query 查询参数
   * Requirements: 7.1, 7.2, 7.3
   */
  async function fetchReportHistory(query?: ReportQueryDTO) {
    try {
      loading.value = true
      const params: ReportQueryDTO = {
        pageNum: query?.pageNum || 1,
        pageSize: query?.pageSize || pageSize.value,
        reportType: query?.reportType,
        startDate: query?.startDate,
        endDate: query?.endDate
      }
      
      const data = await reportApi.getReportHistory(params)
      
      if (params.pageNum === 1) {
        reportHistory.value = data.records
      } else {
        reportHistory.value.push(...data.records)
      }
      
      currentPage.value = data.current
      total.value = data.total
      
      return { success: true, data }
    } catch (error: any) {
      return { success: false, message: error.message || '获取历史报告失败' }
    } finally {
      loading.value = false
    }
  }

  /**
   * 加载更多历史报告
   */
  async function loadMoreHistory() {
    if (!hasMore.value || loading.value) {
      return { success: false, message: '没有更多报告' }
    }
    return fetchReportHistory({ pageNum: currentPage.value + 1 })
  }

  /**
   * 获取报告详情
   * @param reportId 报告ID
   * Requirements: 7.4
   */
  async function fetchReportById(reportId: number) {
    try {
      loading.value = true
      const data = await reportApi.getReportById(reportId)
      return { success: true, data }
    } catch (error: any) {
      return { success: false, message: error.message || '获取报告详情失败' }
    } finally {
      loading.value = false
    }
  }

  // ==================== 分享相关方法 ====================

  /**
   * 生成分享图片
   * @param reportId 报告ID
   * @param theme 主题
   * Requirements: 6.1, 6.3
   */
  async function generateShareImage(reportId: number, theme: string = 'default') {
    try {
      loading.value = true
      const data = await reportApi.generateShareImage(reportId, theme)
      shareImage.value = data
      return { success: true, data }
    } catch (error: any) {
      return { success: false, message: error.message || '生成分享图片失败' }
    } finally {
      loading.value = false
    }
  }

  // ==================== 纪念日相关方法 ====================

  /**
   * 获取纪念日列表
   * Requirements: 5.2
   */
  async function fetchAnniversaries() {
    try {
      loading.value = true
      const data = await reportApi.getAnniversaries()
      anniversaries.value = data
      return { success: true, data }
    } catch (error: any) {
      return { success: false, message: error.message || '获取纪念日列表失败' }
    } finally {
      loading.value = false
    }
  }

  /**
   * 添加纪念日
   * @param data 纪念日创建参数
   * Requirements: 5.2
   */
  async function addAnniversary(data: AnniversaryCreateDTO) {
    try {
      loading.value = true
      const result = await reportApi.addAnniversary(data)
      // 刷新列表
      await fetchAnniversaries()
      return { success: true, data: result }
    } catch (error: any) {
      return { success: false, message: error.message || '添加纪念日失败' }
    } finally {
      loading.value = false
    }
  }

  /**
   * 更新纪念日
   * @param id 纪念日ID
   * @param data 纪念日更新参数
   * Requirements: 5.2
   */
  async function updateAnniversary(id: number, data: AnniversaryUpdateDTO) {
    try {
      loading.value = true
      const result = await reportApi.updateAnniversary(id, data)
      // 更新本地状态
      const index = anniversaries.value.findIndex(a => a.id === id)
      if (index !== -1) {
        anniversaries.value[index] = result
      }
      return { success: true, data: result }
    } catch (error: any) {
      return { success: false, message: error.message || '更新纪念日失败' }
    } finally {
      loading.value = false
    }
  }

  /**
   * 删除纪念日
   * @param id 纪念日ID
   * Requirements: 5.6
   */
  async function deleteAnniversary(id: number) {
    try {
      loading.value = true
      await reportApi.deleteAnniversary(id)
      // 从本地状态中移除
      anniversaries.value = anniversaries.value.filter(a => a.id !== id)
      return { success: true }
    } catch (error: any) {
      return { success: false, message: error.message || '删除纪念日失败' }
    } finally {
      loading.value = false
    }
  }

  // ==================== 工具方法 ====================

  /**
   * 切换报告类型
   * @param type 报告类型
   */
  async function switchReportType(type: number) {
    selectedReportType.value = type
    return fetchCurrentReport(type)
  }

  /**
   * 刷新当前报告
   */
  async function refreshCurrentReport() {
    return fetchCurrentReport(selectedReportType.value)
  }

  /**
   * 重置状态
   */
  function reset() {
    currentReport.value = null
    reportHistory.value = []
    anniversaries.value = []
    selectedReportType.value = 1
    currentPage.value = 1
    total.value = 0
    loading.value = false
    shareImage.value = null
  }

  return {
    // 状态
    currentReport,
    reportHistory,
    anniversaries,
    selectedReportType,
    currentPage,
    pageSize,
    total,
    loading,
    shareImage,
    // 计算属性
    hasMore,
    upcomingAnniversaries,
    reportTypeName,
    // 报告方法
    fetchCurrentReport,
    fetchReportHistory,
    loadMoreHistory,
    fetchReportById,
    // 分享方法
    generateShareImage,
    // 纪念日方法
    fetchAnniversaries,
    addAnniversary,
    updateAnniversary,
    deleteAnniversary,
    // 工具方法
    switchReportType,
    refreshCurrentReport,
    reset
  }
})
