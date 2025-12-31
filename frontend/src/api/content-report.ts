/**
 * 内容举报API模块
 * 提供增强的举报功能，支持多类型举报和证据上传
 */
import request from './request'

/**
 * 举报类型枚举
 */
export enum ReportType {
  /** 内容举报 */
  CONTENT = 0,
  /** 用户举报 */
  USER = 1,
  /** 评论举报 */
  COMMENT = 2
}

/**
 * 举报原因枚举
 */
export enum ReportReason {
  /** 色情低俗 */
  PORN = 0,
  /** 暴力血腥 */
  VIOLENCE = 1,
  /** 政治敏感 */
  POLITICAL = 2,
  /** 广告骚扰 */
  AD = 3,
  /** 侵权内容 */
  INFRINGEMENT = 4,
  /** 其他 */
  OTHER = 5
}

/**
 * 内容举报请求
 */
export interface ContentReportRequest {
  /** 内容ID */
  contentId: number
  /** 举报类型：0-内容 1-用户 2-评论 */
  reportType?: number
  /** 举报原因：0-色情低俗，1-暴力血腥，2-政治敏感，3-广告骚扰，4-侵权内容，5-其他 */
  reportReason: number
  /** 详细描述 */
  description?: string
  /** 证据截图URL列表 */
  evidenceUrls?: string[]
}

/**
 * 举报结果
 */
export interface ContentReportResult {
  /** 举报ID */
  reportId: number
  /** 是否成功 */
  success: boolean
  /** 消息 */
  message: string
}

/**
 * 举报原因选项
 */
export const REPORT_REASON_OPTIONS = [
  { value: ReportReason.PORN, label: '色情低俗', icon: '🔞' },
  { value: ReportReason.VIOLENCE, label: '暴力血腥', icon: '⚠️' },
  { value: ReportReason.POLITICAL, label: '政治敏感', icon: '🚫' },
  { value: ReportReason.AD, label: '广告骚扰', icon: '📢' },
  { value: ReportReason.INFRINGEMENT, label: '侵权内容', icon: '©️' },
  { value: ReportReason.OTHER, label: '其他', icon: '❓' }
]

/**
 * 举报类型选项
 */
export const REPORT_TYPE_OPTIONS = [
  { value: ReportType.CONTENT, label: '内容', icon: '📄' },
  { value: ReportType.USER, label: '用户', icon: '👤' },
  { value: ReportType.COMMENT, label: '评论', icon: '💬' }
]

/**
 * 提交内容举报
 * POST /api/content/report/submit
 * Requirements: 3.1, 3.2, 3.4
 */
export function submitContentReport(data: ContentReportRequest): Promise<ContentReportResult> {
  return request<ContentReportResult>({
    url: '/api/content/report/submit',
    method: 'POST',
    data
  })
}

/**
 * 检查是否已举报
 * GET /api/content/report/check
 * Requirements: 3.6
 */
export function checkReported(contentId: number): Promise<{ reported: boolean }> {
  return request<{ reported: boolean }>({
    url: '/api/content/report/check',
    method: 'GET',
    params: { contentId }
  })
}

/**
 * 获取举报原因文本
 */
export function getReportReasonText(reason: number): string {
  const option = REPORT_REASON_OPTIONS.find(o => o.value === reason)
  return option?.label || '未知原因'
}

/**
 * 获取举报类型文本
 */
export function getReportTypeText(type: number): string {
  const option = REPORT_TYPE_OPTIONS.find(o => o.value === type)
  return option?.label || '未知类型'
}
