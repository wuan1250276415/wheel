// 备份记录
export interface BackupRecord {
    id: number
    backupType: number
    backupTypeName: string
    filename: string
    fileSize: number
    fileSizeFormatted: string
    isEncrypted: boolean
    status: number
    statusName: string
    description?: string
    durationMs: number
    durationFormatted: string
    errorMessage?: string
    createdBy?: number
    createdByName?: string
    createdTime: string
}

// 恢复记录
export interface RestoreRecord {
    id: number
    backupId?: number
    preRestoreBackupId?: number
    sourceType: number
    sourceFilename?: string
    status: number
    statusName: string
    durationMs: number
    errorMessage?: string
    createdBy: number
    createdByName?: string
    createdTime: string
}

// 备份配置
export interface BackupConfig {
    dailyCron: string
    dailyCronDescription?: string
    weeklyCron: string
    weeklyCronDescription?: string
    cleanupCron: string
    cleanupCronDescription?: string
    healthCheckCron: string
    healthCheckCronDescription?: string
    retentionDays: number
    minBackupCount: number
    storagePath: string
    encryptionEnabled: boolean
    alertThresholdHours: number
    storageWarningThresholdGb: number
    lastUpdatedTime?: string
    lastUpdatedBy?: string
}

// 备份统计
export interface BackupStatistics {
    lastBackupTime?: string
    lastBackupSize?: number
    totalBackupCount: number
    successCount: number
    failureCount: number
    successRate: number
    totalStorageUsed: number
    totalStorageUsedFormatted: string
    healthStatus: 'HEALTHY' | 'WARNING' | 'CRITICAL'
    healthMessage?: string
}

// 备份进度
export interface BackupProgress {
    backupId: number
    progress: number
    stage: string
    estimatedRemainingMs?: number
}

// 恢复进度
export interface RestoreProgress {
    restoreId: number
    progress: number
    stage: string
    estimatedRemainingMs?: number
}

// 备份结果
export interface BackupResult {
    backupId: number
    filename: string
    message: string
    success: boolean
}

// 恢复结果
export interface RestoreResult {
    restoreId: number
    message: string
    success: boolean
    preRestoreBackupId?: number
}

// 备份查询参数
export interface BackupQueryDTO {
    pageNum?: number
    pageSize?: number
    backupType?: number
    status?: number
    startTime?: string
    endTime?: string
}

// 手动备份请求
export interface ManualBackupDTO {
    description?: string
}

// 备份配置更新请求
export interface BackupConfigDTO {
    dailyCron?: string
    weeklyCron?: string
    retentionDays?: number
    minBackupCount?: number
    storagePath?: string
    encryptionEnabled?: boolean
    alertThresholdHours?: number
    storageWarningThresholdGb?: number
}

// 备份类型枚举
export const BackupType = {
    DAILY: 1,
    WEEKLY: 2,
    MANUAL: 3
} as const

export const BackupTypeLabels: Record<number, string> = {
    1: '每日备份',
    2: '每周备份',
    3: '手动备份'
}

// 备份状态枚举
export const BackupStatus = {
    IN_PROGRESS: 0,
    SUCCESS: 1,
    FAILED: 2
} as const

export const BackupStatusLabels: Record<number, string> = {
    0: '进行中',
    1: '成功',
    2: '失败'
}

// 恢复状态枚举
export const RestoreStatus = {
    IN_PROGRESS: 0,
    SUCCESS: 1,
    FAILED: 2,
    ROLLED_BACK: 3
} as const

export const RestoreStatusLabels: Record<number, string> = {
    0: '进行中',
    1: '成功',
    2: '失败',
    3: '已回滚'
}

// 健康状态枚举
export const HealthStatus = {
    HEALTHY: 'HEALTHY',
    WARNING: 'WARNING',
    CRITICAL: 'CRITICAL'
} as const

export const HealthStatusLabels: Record<string, string> = {
    HEALTHY: '健康',
    WARNING: '警告',
    CRITICAL: '危险'
}
