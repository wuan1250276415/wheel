package com.basebackend.wheel.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.basebackend.common.model.Result;
import com.basebackend.security.annotation.RequiresPermission;
import com.basebackend.wheel.dto.*;
import com.basebackend.wheel.service.BackupConfigService;
import com.basebackend.wheel.service.BackupService;
import com.basebackend.wheel.service.BackupStorageService;
import com.basebackend.wheel.service.RestoreService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 备份管理控制器
 * 提供数据库备份、恢复、配置和统计的 REST API
 *
 * @author wheel-api
 * @since 2025-02-02
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/backup")
@Tag(name = "管理端-备份管理", description = "管理员进行数据库备份和恢复操作")
public class BackupController {

    @Autowired
    private BackupService backupService;

    @Autowired
    private RestoreService restoreService;

    @Autowired
    private BackupConfigService backupConfigService;

    @Autowired
    private BackupStorageService backupStorageService;

    // ==================== 备份管理接口 ====================

    /**
     * 分页查询备份列表
     */
    @Operation(summary = "分页查询备份列表")
    @GetMapping("/list")
    @RequiresPermission("backup:list:view")
    public Result<IPage<BackupRecordVO>> listBackups(BackupQueryDTO queryDTO) {
        try {
            IPage<BackupRecordVO> page = backupService.listBackups(queryDTO);
            return Result.success(page);
        } catch (Exception e) {
            log.error("查询备份列表失败: error={}", e.getMessage(), e);
            return Result.error(500, "查询备份列表失败: " + e.getMessage());
        }
    }

    /**
     * 手动触发备份
     */
    @Operation(summary = "手动触发备份")
    @PostMapping("/trigger")
    @RequiresPermission("backup:trigger")
    public Result<BackupResultVO> triggerBackup(@RequestBody ManualBackupDTO dto) {
        try {
            // 检查是否有备份正在进行
            if (backupService.isBackupInProgress()) {
                return Result.error(409, "已有备份任务正在进行中，请稍后再试");
            }

            BackupResultVO result = backupService.triggerManualBackup(dto);
            log.info("手动触发备份成功: backupId={}", result.getBackupId());
            return Result.success(result);
        } catch (Exception e) {
            log.error("触发备份失败: error={}", e.getMessage(), e);
            return Result.error(500, "触发备份失败: " + e.getMessage());
        }
    }

    /**
     * 获取备份进度
     */
    @Operation(summary = "获取备份进度")
    @GetMapping("/progress/{backupId}")
    @RequiresPermission("backup:list:view")
    public Result<BackupProgressVO> getBackupProgress(@PathVariable Long backupId) {
        try {
            BackupProgressVO progress = backupService.getBackupProgress(backupId);
            if (progress == null) {
                return Result.error(404, "备份进度信息不存在");
            }
            return Result.success(progress);
        } catch (Exception e) {
            log.error("获取备份进度失败: backupId={}, error={}", backupId, e.getMessage(), e);
            return Result.error(500, "获取备份进度失败: " + e.getMessage());
        }
    }

    /**
     * 下载备份文件
     */
    @Operation(summary = "下载备份文件")
    @GetMapping("/download/{backupId}")
    @RequiresPermission("backup:download")
    public ResponseEntity<Resource> downloadBackup(@PathVariable Long backupId) {
        try {
            BackupRecordVO backup = backupService.getBackupById(backupId);
            if (backup == null) {
                return ResponseEntity.notFound().build();
            }

            Resource resource = backupStorageService.getBackupFileResource(backup.getFilename());

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + backup.getFilename() + "\"")
                    .body(resource);
        } catch (Exception e) {
            log.error("下载备份文件失败: backupId={}, error={}", backupId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 删除备份
     */
    @Operation(summary = "删除备份")
    @DeleteMapping("/{backupId}")
    @RequiresPermission("backup:delete")
    public Result<Void> deleteBackup(@PathVariable Long backupId) {
        try {
            BackupRecordVO backup = backupService.getBackupById(backupId);
            if (backup == null) {
                return Result.error(404, "备份记录不存在");
            }

            backupService.deleteBackup(backupId);
            log.info("删除备份成功: backupId={}", backupId);
            return Result.success(null);
        } catch (Exception e) {
            log.error("删除备份失败: backupId={}, error={}", backupId, e.getMessage(), e);
            return Result.error(500, "删除备份失败: " + e.getMessage());
        }
    }

    // ==================== 恢复管理接口 ====================

    /**
     * 从备份恢复
     */
    @Operation(summary = "从备份恢复数据库")
    @PostMapping("/restore/{backupId}")
    @RequiresPermission("backup:restore")
    public Result<RestoreResultVO> restoreFromBackup(@PathVariable Long backupId) {
        try {
            // 检查是否有恢复正在进行
            if (restoreService.isRestoreInProgress()) {
                return Result.error(409, "已有恢复任务正在进行中，请稍后再试");
            }

            // 检查是否有备份正在进行
            if (backupService.isBackupInProgress()) {
                return Result.error(409, "有备份任务正在进行中，请稍后再试");
            }

            // 验证备份文件
            if (!restoreService.validateBackupById(backupId)) {
                return Result.error(400, "备份文件无效或已损坏");
            }

            RestoreResultVO result = restoreService.restoreFromBackup(backupId);
            log.info("从备份恢复成功: backupId={}, restoreId={}", backupId, result.getRestoreId());
            return Result.success(result);
        } catch (Exception e) {
            log.error("从备份恢复失败: backupId={}, error={}", backupId, e.getMessage(), e);
            return Result.error(500, "恢复失败: " + e.getMessage());
        }
    }

    /**
     * 上传备份文件并恢复
     */
    @Operation(summary = "上传备份文件并恢复")
    @PostMapping("/restore/upload")
    @RequiresPermission("backup:restore")
    public Result<RestoreResultVO> restoreFromUpload(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return Result.error(400, "请选择要上传的备份文件");
            }

            // 检查是否有恢复正在进行
            if (restoreService.isRestoreInProgress()) {
                return Result.error(409, "已有恢复任务正在进行中，请稍后再试");
            }

            // 检查是否有备份正在进行
            if (backupService.isBackupInProgress()) {
                return Result.error(409, "有备份任务正在进行中，请稍后再试");
            }

            RestoreResultVO result = restoreService.restoreFromUpload(file);
            log.info("从上传文件恢复成功: filename={}, restoreId={}",
                    file.getOriginalFilename(), result.getRestoreId());
            return Result.success(result);
        } catch (Exception e) {
            log.error("从上传文件恢复失败: filename={}, error={}",
                    file.getOriginalFilename(), e.getMessage(), e);
            return Result.error(500, "恢复失败: " + e.getMessage());
        }
    }

    /**
     * 获取恢复进度
     */
    @Operation(summary = "获取恢复进度")
    @GetMapping("/restore/progress/{restoreId}")
    @RequiresPermission("backup:restore")
    public Result<RestoreProgressVO> getRestoreProgress(@PathVariable Long restoreId) {
        try {
            RestoreProgressVO progress = restoreService.getRestoreProgress(restoreId);
            if (progress == null) {
                return Result.error(404, "恢复进度信息不存在");
            }
            return Result.success(progress);
        } catch (Exception e) {
            log.error("获取恢复进度失败: restoreId={}, error={}", restoreId, e.getMessage(), e);
            return Result.error(500, "获取恢复进度失败: " + e.getMessage());
        }
    }

    // ==================== 配置管理接口 ====================

    /**
     * 获取备份配置
     */
    @Operation(summary = "获取备份配置")
    @GetMapping("/config")
    @RequiresPermission("backup:config:view")
    public Result<BackupConfigVO> getBackupConfig() {
        try {
            BackupConfigVO config = backupConfigService.getConfigVO();
            return Result.success(config);
        } catch (Exception e) {
            log.error("获取备份配置失败: error={}", e.getMessage(), e);
            return Result.error(500, "获取备份配置失败: " + e.getMessage());
        }
    }

    /**
     * 更新备份配置
     */
    @Operation(summary = "更新备份配置")
    @PutMapping("/config")
    @RequiresPermission("backup:config:update")
    public Result<Void> updateBackupConfig(@RequestBody BackupConfigDTO configDTO) {
        try {
            // 验证配置
            String validationError = backupConfigService.validateConfig(configDTO);
            if (validationError != null) {
                return Result.error(400, validationError);
            }

            backupConfigService.updateConfigs(configDTO);
            log.info("更新备份配置成功");
            return Result.success(null);
        } catch (Exception e) {
            log.error("更新备份配置失败: error={}", e.getMessage(), e);
            return Result.error(500, "更新备份配置失败: " + e.getMessage());
        }
    }

    // ==================== 统计信息接口 ====================

    /**
     * 获取备份统计信息
     */
    @Operation(summary = "获取备份统计信息")
    @GetMapping("/statistics")
    @RequiresPermission("backup:statistics:view")
    public Result<BackupStatisticsVO> getBackupStatistics() {
        try {
            BackupStatisticsVO statistics = backupService.getBackupStatistics();
            return Result.success(statistics);
        } catch (Exception e) {
            log.error("获取备份统计失败: error={}", e.getMessage(), e);
            return Result.error(500, "获取备份统计失败: " + e.getMessage());
        }
    }
}
