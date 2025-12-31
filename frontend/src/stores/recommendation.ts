import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import * as recommendationApi from '@/api/recommendation'
import type {
  RecommendationVO,
  CoupleRecommendationVO,
  HomepageRecommendationVO,
  CategoryRankVO,
  RecommendationFeedbackDTO,
  PreferenceSurveyDTO
} from '@/types/recommendation'

// 缓存有效期（毫秒）- 6小时
const CACHE_DURATION = 6 * 60 * 60 * 1000

// 缓存键
const CACHE_KEYS = {
  HOMEPAGE: 'recommendation_homepage',
  HOMEPAGE_TIME: 'recommendation_homepage_time',
  PERSONAL: 'recommendation_personal',
  PERSONAL_TIME: 'recommendation_personal_time',
  COUPLE: 'recommendation_couple',
  COUPLE_TIME: 'recommendation_couple_time',
  SURVEY_NEEDED: 'recommendation_survey_needed'
}

export const useRecommendationStore = defineStore('recommendation', () => {
  // 状态
  const homepageData = ref<HomepageRecommendationVO | null>(null)
  const personalRecommendations = ref<RecommendationVO[]>([])
  const coupleRecommendations = ref<CoupleRecommendationVO[]>([])
  const personalTotal = ref(0)
  const personalPageNum = ref(1)
  const isLoading = ref(false)
  const surveyNeeded = ref<boolean | null>(null)
  const sessionId = ref<string>(generateSessionId())

  // 计算属性
  const categoryRanking = computed<CategoryRankVO[]>(() => {
    return homepageData.value?.categoryRanking || []
  })

  const recommendedForYou = computed<RecommendationVO[]>(() => {
    return homepageData.value?.recommendedForYou || []
  })

  const couplePicks = computed<RecommendationVO[]>(() => {
    return homepageData.value?.couplePicks || []
  })

  const hasCouplePicks = computed(() => {
    return couplePicks.value.length > 0
  })

  const hasMorePersonal = computed(() => {
    return personalRecommendations.value.length < personalTotal.value
  })

  // 生成会话ID
  function generateSessionId(): string {
    return `${Date.now()}-${Math.random().toString(36).substring(2, 9)}`
  }

  // 检查缓存是否有效
  function isCacheValid(cacheTimeKey: string): boolean {
    const cacheTime = uni.getStorageSync(cacheTimeKey)
    if (!cacheTime) return false
    return Date.now() - cacheTime < CACHE_DURATION
  }

  // 从缓存加载数据
  function loadFromCache<T>(cacheKey: string, cacheTimeKey: string): T | null {
    if (!isCacheValid(cacheTimeKey)) return null
    const cached = uni.getStorageSync(cacheKey)
    return cached || null
  }

  // 保存到缓存
  function saveToCache<T>(cacheKey: string, cacheTimeKey: string, data: T): void {
    uni.setStorageSync(cacheKey, data)
    uni.setStorageSync(cacheTimeKey, Date.now())
  }

  // 清除缓存
  function clearCache(): void {
    Object.values(CACHE_KEYS).forEach(key => {
      uni.removeStorageSync(key)
    })
  }

  /**
   * 获取首页推荐数据
   * @param forceRefresh 是否强制刷新
   */
  async function fetchHomepageRecommendations(forceRefresh: boolean = false) {
    // 尝试从缓存加载
    if (!forceRefresh) {
      const cached = loadFromCache<HomepageRecommendationVO>(
        CACHE_KEYS.HOMEPAGE,
        CACHE_KEYS.HOMEPAGE_TIME
      )
      if (cached) {
        homepageData.value = cached
        return { success: true, data: cached, fromCache: true }
      }
    }

    try {
      isLoading.value = true
      const data = forceRefresh
        ? await recommendationApi.refreshHomepageRecommendations()
        : await recommendationApi.getHomepageRecommendations()
      
      homepageData.value = data
      saveToCache(CACHE_KEYS.HOMEPAGE, CACHE_KEYS.HOMEPAGE_TIME, data)
      
      return { success: true, data, fromCache: false }
    } catch (error: any) {
      return { success: false, message: error.message }
    } finally {
      isLoading.value = false
    }
  }

  /**
   * 获取个性化推荐
   * @param pageNum 页码
   * @param pageSize 每页数量
   * @param append 是否追加到现有列表
   */
  async function fetchPersonalRecommendations(
    pageNum: number = 1,
    pageSize: number = 10,
    append: boolean = false
  ) {
    // 首页尝试从缓存加载
    if (pageNum === 1 && !append) {
      const cached = loadFromCache<{ records: RecommendationVO[]; total: number }>(
        CACHE_KEYS.PERSONAL,
        CACHE_KEYS.PERSONAL_TIME
      )
      if (cached) {
        personalRecommendations.value = cached.records
        personalTotal.value = cached.total
        personalPageNum.value = 1
        return { success: true, data: cached.records, fromCache: true }
      }
    }

    try {
      isLoading.value = true
      const result = await recommendationApi.getPersonalRecommendations(pageNum, pageSize)
      
      if (append && pageNum > 1) {
        personalRecommendations.value.push(...result.records)
      } else {
        personalRecommendations.value = result.records
        // 缓存首页数据
        saveToCache(CACHE_KEYS.PERSONAL, CACHE_KEYS.PERSONAL_TIME, {
          records: result.records,
          total: result.total
        })
      }
      
      personalTotal.value = result.total
      personalPageNum.value = pageNum
      
      return { success: true, data: result.records, fromCache: false }
    } catch (error: any) {
      return { success: false, message: error.message }
    } finally {
      isLoading.value = false
    }
  }

  /**
   * 加载更多个性化推荐
   */
  async function loadMorePersonalRecommendations(pageSize: number = 10) {
    if (!hasMorePersonal.value) {
      return { success: true, data: [], hasMore: false }
    }
    
    const nextPage = personalPageNum.value + 1
    const result = await fetchPersonalRecommendations(nextPage, pageSize, true)
    
    return {
      ...result,
      hasMore: hasMorePersonal.value
    }
  }

  /**
   * 获取情侣推荐
   * @param forceRefresh 是否强制刷新
   */
  async function fetchCoupleRecommendations(forceRefresh: boolean = false) {
    // 尝试从缓存加载
    if (!forceRefresh) {
      const cached = loadFromCache<CoupleRecommendationVO[]>(
        CACHE_KEYS.COUPLE,
        CACHE_KEYS.COUPLE_TIME
      )
      if (cached) {
        coupleRecommendations.value = cached
        return { success: true, data: cached, fromCache: true }
      }
    }

    try {
      isLoading.value = true
      const data = await recommendationApi.getCoupleRecommendations()
      
      coupleRecommendations.value = data
      saveToCache(CACHE_KEYS.COUPLE, CACHE_KEYS.COUPLE_TIME, data)
      
      return { success: true, data, fromCache: false }
    } catch (error: any) {
      return { success: false, message: error.message }
    } finally {
      isLoading.value = false
    }
  }

  /**
   * 记录推荐反馈
   * @param contentId 内容ID
   * @param feedbackType 反馈类型
   */
  async function recordFeedback(
    contentId: number,
    feedbackType: RecommendationFeedbackDTO['feedbackType']
  ) {
    try {
      await recommendationApi.submitFeedback({
        contentId,
        feedbackType,
        sessionId: sessionId.value
      })
      return { success: true }
    } catch (error: any) {
      return { success: false, message: error.message }
    }
  }

  /**
   * 记录点击反馈
   */
  async function recordClick(contentId: number) {
    return recordFeedback(contentId, 'CLICK')
  }

  /**
   * 记录忽略反馈
   */
  async function recordIgnore(contentId: number) {
    return recordFeedback(contentId, 'IGNORE')
  }

  /**
   * 记录喜欢反馈
   */
  async function recordLike(contentId: number) {
    return recordFeedback(contentId, 'LIKE')
  }

  /**
   * 记录不喜欢反馈
   */
  async function recordDislike(contentId: number) {
    return recordFeedback(contentId, 'DISLIKE')
  }

  /**
   * 提交偏好调查
   * @param selectedCategoryIds 选择的分类ID列表
   */
  async function submitPreferenceSurvey(selectedCategoryIds: number[]) {
    try {
      await recommendationApi.submitSurvey({
        selectedCategoryIds,
        skipped: false
      })
      surveyNeeded.value = false
      uni.setStorageSync(CACHE_KEYS.SURVEY_NEEDED, false)
      // 清除推荐缓存，以便获取新的个性化推荐
      clearRecommendationCache()
      return { success: true }
    } catch (error: any) {
      return { success: false, message: error.message }
    }
  }

  /**
   * 跳过偏好调查
   */
  async function skipPreferenceSurvey() {
    try {
      await recommendationApi.submitSurvey({
        skipped: true
      })
      surveyNeeded.value = false
      uni.setStorageSync(CACHE_KEYS.SURVEY_NEEDED, false)
      return { success: true }
    } catch (error: any) {
      return { success: false, message: error.message }
    }
  }

  /**
   * 检查是否需要偏好调查
   */
  async function checkSurveyNeeded() {
    // 先检查本地缓存
    const cached = uni.getStorageSync(CACHE_KEYS.SURVEY_NEEDED)
    if (cached === false) {
      surveyNeeded.value = false
      return { success: true, needed: false }
    }

    try {
      const needed = await recommendationApi.checkSurveyNeeded()
      surveyNeeded.value = needed
      if (!needed) {
        uni.setStorageSync(CACHE_KEYS.SURVEY_NEEDED, false)
      }
      return { success: true, needed }
    } catch (error: any) {
      return { success: false, message: error.message }
    }
  }

  /**
   * 清除推荐缓存（不包括调查状态）
   */
  function clearRecommendationCache() {
    uni.removeStorageSync(CACHE_KEYS.HOMEPAGE)
    uni.removeStorageSync(CACHE_KEYS.HOMEPAGE_TIME)
    uni.removeStorageSync(CACHE_KEYS.PERSONAL)
    uni.removeStorageSync(CACHE_KEYS.PERSONAL_TIME)
    uni.removeStorageSync(CACHE_KEYS.COUPLE)
    uni.removeStorageSync(CACHE_KEYS.COUPLE_TIME)
  }

  /**
   * 刷新首页推荐
   */
  async function refreshHomepage() {
    return fetchHomepageRecommendations(true)
  }

  /**
   * 重置会话
   */
  function resetSession() {
    sessionId.value = generateSessionId()
  }

  /**
   * 重置所有状态
   */
  function reset() {
    homepageData.value = null
    personalRecommendations.value = []
    coupleRecommendations.value = []
    personalTotal.value = 0
    personalPageNum.value = 1
    surveyNeeded.value = null
    sessionId.value = generateSessionId()
    clearCache()
  }

  return {
    // 状态
    homepageData,
    personalRecommendations,
    coupleRecommendations,
    personalTotal,
    personalPageNum,
    isLoading,
    surveyNeeded,
    sessionId,
    // 计算属性
    categoryRanking,
    recommendedForYou,
    couplePicks,
    hasCouplePicks,
    hasMorePersonal,
    // 方法
    fetchHomepageRecommendations,
    fetchPersonalRecommendations,
    loadMorePersonalRecommendations,
    fetchCoupleRecommendations,
    recordFeedback,
    recordClick,
    recordIgnore,
    recordLike,
    recordDislike,
    submitPreferenceSurvey,
    skipPreferenceSurvey,
    checkSurveyNeeded,
    clearRecommendationCache,
    refreshHomepage,
    resetSession,
    reset
  }
})
