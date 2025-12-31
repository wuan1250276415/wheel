// 敏感词
export interface SensitiveWord {
  id: number
  word: string
  category: number
  categoryName?: string
  level: number
  levelName?: string
  variants?: string[]
  status: number
  createTime: string
  updateTime: string
}

// 敏感词查询
export interface SensitiveWordQueryDTO {
  keyword?: string
  category?: number
  level?: number
  status?: number
  current?: number
  size?: number
}

// 敏感词创建/编辑
export interface SensitiveWordCreateDTO {
  word: string
  category: number
  level: number
  variants?: string[]
  status?: number
}

// 黑名单
export interface Blacklist {
  id: number
  type: number
  typeName?: string
  targetId: string
  targetName?: string
  reason: string
  duration: number
  expireAt?: string
  operatorId: number
  operatorName?: string
  status: number
  statusName?: string
  appealStatus: number
  appealStatusName?: string
  appealReason?: string
  appealTime?: string
  appealHandleTime?: string
  appealHandleResult?: string
  createTime: string
  updateTime: string
}

// 黑名单查询
export interface BlacklistQueryDTO {
  type?: number
  targetId?: string
  status?: number
  appealStatus?: number
  current?: number
  size?: number
}

// 黑名单创建
export interface BlacklistCreateDTO {
  type: number
  targetId: string
  reason: string
  duration: number // 0表示永久
}

// 举报记录
export interface ContentReport {
  id: number
  contentId: number
  contentType: number
  contentTypeName?: string
  contentTitle?: string
  contentPreview?: string
  reporterId: number
  reporterName?: string
  reportType: number
  reportTypeName?: string
  description?: string
  evidenceUrls?: string[]
  reporterCredibility: number
  priority: number
  priorityName?: string
  status: number
  statusName?: string
  handleResult?: string
  handlerId?: number
  handlerName?: string
  handleTime?: string
  createTime: string
  updateTime: string
}

// 举报查询
export interface ContentReportQueryDTO {
  contentType?: number
  reportType?: number
  status?: number
  priority?: number
  dateRange?: string[]
  current?: number
  size?: number
}

// 举报统计
export interface ReportStatistics {
  todayCount: number
  pendingCount: number
  handledCount: number
  validRate: number
  typeDistribution: { type: string; count: number }[]
  trendData: { date: string; count: number }[]
}

// 审核配置
export interface AuditConfig {
  id: number
  configKey: string
  configValue: string
  description: string
  createTime: string
  updateTime: string
}

// 敏感词分类枚举
export const SensitiveWordCategoryMap: Record<number, string> = {
  1: '色情',
  2: '暴力',
  3: '政治',
  4: '广告',
  5: '其他'
}

// 敏感词风险等级枚举
export const SensitiveWordLevelMap: Record<number, string> = {
  1: '低',
  2: '中',
  3: '高'
}

// 黑名单类型枚举
export const BlacklistTypeMap: Record<number, string> = {
  1: '用户',
  2: 'IP',
  3: '设备'
}

// 黑名单状态枚举
export const BlacklistStatusMap: Record<number, string> = {
  0: '已解除',
  1: '生效中'
}

// 申诉状态枚举
export const AppealStatusMap: Record<number, string> = {
  0: '未申诉',
  1: '申诉中',
  2: '申诉通过',
  3: '申诉驳回'
}

// 举报类型枚举
export const ReportTypeMap: Record<number, string> = {
  1: '色情低俗',
  2: '暴力血腥',
  3: '政治敏感',
  4: '广告骚扰',
  5: '侵权内容',
  6: '其他'
}

// 举报状态枚举
export const ReportStatusMap: Record<number, string> = {
  0: '待处理',
  1: '已处理-有效',
  2: '已处理-无效',
  3: '已忽略'
}

// 举报优先级枚举
export const ReportPriorityMap: Record<number, string> = {
  1: '普通',
  2: '高',
  3: '紧急'
}
