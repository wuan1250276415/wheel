import request from './request'
import type { PageResult } from '@/types/api'
import type {
  RecommendationVO,
  CoupleRecommendationVO,
  HomepageRecommendationVO,
  CategoryRankVO,
  RecommendationFeedbackDTO,
  PreferenceSurveyDTO
} from '@/types/recommendation'

// Re-export types for backward compatibility
export type {
  RecommendationVO,
  CoupleRecommendationVO,
  HomepageRecommendationVO,
  CategoryRankVO,
  RecommendationFeedbackDTO,
  PreferenceSurveyDTO
}

/**
 * 获取个性化推荐
 * @param pageNum 页码，默认1
 * @param pageSize 每页数量，默认10，最大50
 * @returns 分页的推荐结果
 */
export function getPersonalRecommendations(pageNum: number = 1, pageSize: number = 10) {
  return request<PageResult<RecommendationVO>>({
    url: '/api/recommendations/personal',
    method: 'GET',
    params: { pageNum, pageSize }
  })
}

/**
 * 获取情侣推荐
 * @returns 情侣推荐列表，按分类组织
 */
export function getCoupleRecommendations() {
  return request<CoupleRecommendationVO[]>({
    url: '/api/recommendations/couple',
    method: 'GET'
  })
}

/**
 * 获取首页推荐数据
 * @returns 首页推荐数据，包含分类排名、为你推荐、情侣精选等
 */
export function getHomepageRecommendations() {
  return request<HomepageRecommendationVO>({
    url: '/api/recommendations/homepage',
    method: 'GET'
  })
}

/**
 * 记录推荐反馈
 * @param data 反馈数据
 */
export function submitFeedback(data: RecommendationFeedbackDTO) {
  return request<void>({
    url: '/api/recommendations/feedback',
    method: 'POST',
    data
  })
}

/**
 * 提交偏好调查
 * @param data 调查数据
 */
export function submitSurvey(data: PreferenceSurveyDTO) {
  return request<void>({
    url: '/api/recommendations/survey',
    method: 'POST',
    data
  })
}

/**
 * 刷新首页推荐
 * @returns 刷新后的首页推荐数据
 */
export function refreshHomepageRecommendations() {
  return request<HomepageRecommendationVO>({
    url: '/api/recommendations/homepage/refresh',
    method: 'POST'
  })
}

/**
 * 检查是否需要偏好调查
 * @returns 是否需要调查
 */
export function checkSurveyNeeded() {
  return request<boolean>({
    url: '/api/recommendations/survey/check',
    method: 'GET'
  })
}
