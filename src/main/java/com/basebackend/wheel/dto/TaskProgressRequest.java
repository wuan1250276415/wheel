package com.basebackend.wheel.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TaskProgressRequest {
    @NotNull(message = "任务ID不能为空")
    private Long taskId;

    @NotNull(message = "进度增量不能为空")
    private Integer progressDelta;
}
