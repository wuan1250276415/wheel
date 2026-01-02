import request from '@/utils/request'
import type { ApiResponse, PageResponse } from '@/types/api'
import type {
    BackupRecord,
    BackupConfig,
    BackupStatistics,
    BackupProgress,
    RestoreProgress,
    BackupResult,
    RestoreResult,
    BackupQueryDTO,
    ManualBackupDTO,
    BackupConfigDTO
} from '@/types/backup'

/**
 * 分页查询备份列表
 */
export function listBackups(params: BackupQueryDTO) {
    return request.get<any, ApiResponse<PageResponse<BackupRecord>>>('/admin/backup/list', { params })
}

/**
 * 手动触发备份
 */
export function triggerBackup(data: ManualBackupDTO) {
    return request.post<any, ApiResponse<BackupResult>>('/admin/backup/trigger', data)
}

/**
 * 获取备份进度
 */
export function getBackupProgress(backupId: number) {
    return request.get<any, ApiResponse<BackupProgress>>(`/admin/backup/progress/${backupId}`)
}

/**
 * 下载备份文件
 */
export function downloadBackup(backupId: number) {
    return request.get<any, Blob>(`/admin/backup/download/${backupId}`, {
        responseType: 'blob'
    })
}

/**
 * 删除备份
 */
export function deleteBackup(backupId: number) {
    return request.delete<any, ApiResponse<void>>(`/admin/backup/${backupId}`)
}

/**
 * 从备份恢复
 */
export function restoreFromBackup(backupId: number) {
    return request.post<any, ApiResponse<RestoreResult>>(`/admin/backup/restore/${backupId}`)
}

/**
 * 上传备份文件并恢复
 */
export function restoreFromUpload(file: File) {
    const formData = new FormData()
    formData.append('file', file)
    return request.post<any, ApiResponse<RestoreResult>>('/admin/backup/restore/upload', formData, {
        headers: {
            'Content-Type': 'multipart/form-data'
        }
    })
}

/**
 * 获取恢复进度
 */
export function getRestoreProgress(restoreId: number) {
    return request.get<any, ApiResponse<RestoreProgress>>(`/admin/backup/restore/progress/${restoreId}`)
}

/**
 * 获取备份配置
 */
export function getBackupConfig() {
    return request.get<any, ApiResponse<BackupConfig>>('/admin/backup/config')
}

/**
 * 更新备份配置
 */
export function updateBackupConfig(data: BackupConfigDTO) {
    return request.put<any, ApiResponse<void>>('/admin/backup/config', data)
}

/**
 * 获取备份统计信息
 */
export function getBackupStatistics() {
    return request.get<any, ApiResponse<BackupStatistics>>('/admin/backup/statistics')
}
