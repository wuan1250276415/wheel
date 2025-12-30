import request from './request'

// ==================== 类型定义 ====================

/**
 * 广告类型
 */
export enum AdType {
  IMAGE = 1,
  VIDEO = 2,
  TEXT = 3
}

/**
 * 展示类型
 */
export enum ImpressionType {
  VIEW = 1,
  CLICK = 2
}

/**
 * 广告VO
 */
export interface AdVO {
  id: number
  adType: AdType
  contentUrl?: string
  contentText?: string
  linkUrl?: string
  placementKey: string
}

// ==================== API 函数 ====================

/**
 * 获取广告
 * GET /api/ads/{placementKey}
 * VIP用户自动返回空列表
 * @param placementKey 广告位标识
 */
export function getAds(placementKey: string) {
  return request<AdVO[]>({
    url: `/api/ads/${placementKey}`,
    method: 'GET'
  })
}

/**
 * 记录广告展示
 * POST /api/ads/impression
 * @param adId 广告ID
 * @param placementKey 广告位标识
 * @param type 展示类型
 */
export function recordImpression(
  adId: number,
  placementKey: string,
  type: ImpressionType
) {
  return request<void>({
    url: '/api/ads/impression',
    method: 'POST',
    params: {
      adId,
      placementKey,
      type
    }
  })
}
