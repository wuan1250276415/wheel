package com.basebackend.wheel.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 恢复进度 VO
 * 用于展示恢复操作的实时进度
 *
 * @author wheel-api
 * @since 2025-02-02
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RestoreProgressVO {

    /**
     * 恢复记录ID
     */
    private Long restoreId;

    /**
     * 进度百分比 (0-100)
     */
    private Integer progress;

    /**
     * 当前阶段
     * 可能的值: 验证中/解密中/解压中/恢复中/完成/失败/回滚中
     */
    private String stage;

    /**
     * 阶段代码
     * 0-验证中 1-解密中 2-解压中 3-恢复中 4-完成 5-失败 6-回滚中
     */
    private Integer stageCode;

    /**
     * 预计剩余时间（毫秒）
     */
    private Long estimatedRemainingMs;

    /**
     * 预计剩余时间（格式化显示）
     */
    private String estimatedRemainingFormatted;

    /**
     * 开始时间戳
     */
    private Long startTimestamp;

    /**
     * 已耗时（毫秒）
     */
    private Long elapsedMs;

    /**
     * 已耗时（格式化显示）
     */
    private String elapsedFormatted;

    /**
     * 错误信息（如果失败）
     */
    private String errorMessage;

    /**
     * 是否已完成
     */
    private Boolean completed;

    /**
     * 是否成功
     */
    private Boolean success;

    /**
     * 源备份ID
     */
    private Long backupId;

    /**
     * 恢复前备份ID
     */
    private Long preRestoreBackupId;

    // ==================== 阶段常量 ====================

    public static final int STAGE_VALIDATING = 0;
    public static final int STAGE_DECRYPTING = 1;
    public static final int STAGE_DECOMPRESSING = 2;
    public static final int STAGE_RESTORING = 3;
    public static final int STAGE_COMPLETED = 4;
    public static final int STAGE_FAILED = 5;
    public static final int STAGE_ROLLING_BACK = 6;

    public static final String STAGE_NAME_VALIDATING = "验证中";
    public static final String STAGE_NAME_DECRYPTING = "解密中";
    public static final String STAGE_NAME_DECOMPRESSING = "解压中";
    public static final String STAGE_NAME_RESTORING = "恢复中";
    public static final String STAGE_NAME_COMPLETED = "完成";
    public static final String STAGE_NAME_FAILED = "失败";
    public static final String STAGE_NAME_ROLLING_BACK = "回滚中";

    /**
     * 获取阶段名称
     *
     * @param stageCode 阶段代码
     * @return 阶段名称
     */
    public static String getStageName(int stageCode) {
        switch (stageCode) {
            case STAGE_VALIDATING:
                return STAGE_NAME_VALIDATING;
            case STAGE_DECRYPTING:
                return STAGE_NAME_DECRYPTING;
            case STAGE_DECOMPRESSING:
                return STAGE_NAME_DECOMPRESSING;
            case STAGE_RESTORING:
                return STAGE_NAME_RESTORING;
            case STAGE_COMPLETED:
                return STAGE_NAME_COMPLETED;
            case STAGE_FAILED:
                return STAGE_NAME_FAILED;
            case STAGE_ROLLING_BACK:
                return STAGE_NAME_ROLLING_BACK;
            default:
                return "未知";
        }
    }

    /**
     * 创建初始进度
     *
     * @param restoreId 恢复ID
     * @return 初始进度VO
     */
    public static RestoreProgressVO initial(Long restoreId) {
        return RestoreProgressVO.builder()
                .restoreId(restoreId)
                .progress(0)
                .stage(STAGE_NAME_VALIDATING)
                .stageCode(STAGE_VALIDATING)
                .startTimestamp(System.currentTimeMillis())
                .elapsedMs(0L)
                .elapsedFormatted("0秒")
                .completed(false)
                .success(false)
                .build();
    }

    /**
     * 创建进行中进度
     *
     * @param restoreId 恢复ID
     * @param progress  进度百分比
     * @param stageCode 阶段代码
     * @param startTimestamp 开始时间戳
     * @return 进度VO
     */
    public static RestoreProgressVO inProgress(Long restoreId, int progress, int stageCode, long startTimestamp) {
        long elapsedMs = System.currentTimeMillis() - startTimestamp;
        return RestoreProgressVO.builder()
                .restoreId(restoreId)
                .progress(progress)
                .stage(getStageName(stageCode))
                .stageCode(stageCode)
                .startTimestamp(startTimestamp)
                .elapsedMs(elapsedMs)
                .elapsedFormatted(formatDuration(elapsedMs))
                .completed(false)
                .success(false)
                .build();
    }

    /**
     * 创建完成进度
     *
     * @param restoreId    恢复ID
     * @param success      是否成功
     * @param elapsedMs    耗时
     * @param errorMessage 错误信息
     * @return 完成进度VO
     */
    public static RestoreProgressVO completed(Long restoreId, boolean success, long elapsedMs, String errorMessage) {
        return RestoreProgressVO.builder()
                .restoreId(restoreId)
                .progress(100)
                .stage(success ? STAGE_NAME_COMPLETED : STAGE_NAME_FAILED)
                .stageCode(success ? STAGE_COMPLETED : STAGE_FAILED)
                .estimatedRemainingMs(0L)
                .estimatedRemainingFormatted("0秒")
                .elapsedMs(elapsedMs)
                .elapsedFormatted(formatDuration(elapsedMs))
                .errorMessage(errorMessage)
                .completed(true)
                .success(success)
                .build();
    }

    /**
     * 格式化时长
     *
     * @param ms 毫秒数
     * @return 格式化字符串
     */
    public static String formatDuration(long ms) {
        if (ms <= 0) {
            return "0秒";
        }
        long seconds = ms / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;

        if (hours > 0) {
            return String.format("%d小时%d分%d秒", hours, minutes % 60, seconds % 60);
        } else if (minutes > 0) {
            return String.format("%d分%d秒", minutes, seconds % 60);
        } else {
            return String.format("%d秒", seconds);
        }
    }
}
