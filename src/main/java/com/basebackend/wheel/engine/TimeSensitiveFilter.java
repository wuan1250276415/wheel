package com.basebackend.wheel.engine;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

/**
 * 时间敏感内容过滤器
 *
 * @author wheel-api
 * @since 2025-12-16
 */
@Slf4j
@Component
public class TimeSensitiveFilter {

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 检查内容在指定时间是否可用
     *
     * @param tagsJson 标签JSON字符串
     * @param currentTime 当前时间
     * @return 是否可用
     */
    public boolean isContentAvailable(String tagsJson, LocalDateTime currentTime) {
        if (tagsJson == null || tagsJson.isEmpty()) {
            return true; // 没有时间限制，默认可用
        }

        try {
            // 解析标签JSON
            Map<String, Object> tags = objectMapper.readValue(tagsJson, new TypeReference<Map<String, Object>>() {});

            // 检查时间规则
            if (tags.containsKey("timeRules")) {
                Map<String, Object> timeRules = (Map<String, Object>) tags.get("timeRules");

                // 检查工作日限制
                if (timeRules.containsKey("weekdays")) {
                    List<Integer> weekdays = (List<Integer>) timeRules.get("weekdays");
                    int currentDayOfWeek = currentTime.getDayOfWeek().getValue(); // 1-7 (周一到周日)
                    if (!weekdays.contains(currentDayOfWeek)) {
                        log.debug("内容不在工作日范围内: tags={}, currentDay={}", tagsJson, currentDayOfWeek);
                        return false;
                    }
                }

                // 检查时间段限制
                if (timeRules.containsKey("timeRange")) {
                    Map<String, String> timeRange = (Map<String, String>) timeRules.get("timeRange");
                    String startTimeStr = timeRange.get("start");
                    String endTimeStr = timeRange.get("end");

                    if (startTimeStr != null && endTimeStr != null) {
                        LocalTime startTime = LocalTime.parse(startTimeStr);
                        LocalTime endTime = LocalTime.parse(endTimeStr);
                        LocalTime currentTimeOnly = currentTime.toLocalTime();

                        if (!isTimeInRange(currentTimeOnly, startTime, endTime)) {
                            log.debug("内容不在指定时间段内: tags={}, currentTime={}, range={}-{}",
                                    tagsJson, currentTimeOnly, startTime, endTime);
                            return false;
                        }
                    }
                }

                // 检查日期范围限制
                if (timeRules.containsKey("dateRange")) {
                    Map<String, String> dateRange = (Map<String, String>) timeRules.get("dateRange");
                    String startDateStr = dateRange.get("start");
                    String endDateStr = dateRange.get("end");

                    if (startDateStr != null && endDateStr != null) {
                        // TODO: 实现日期范围检查
                        log.debug("日期范围限制暂未实现: tags={}", tagsJson);
                    }
                }
            }

            return true;
        } catch (Exception e) {
            log.warn("解析时间规则失败，使用默认可用: tags={}, error={}", tagsJson, e.getMessage());
            return true; // 解析失败时默认可用
        }
    }

    /**
     * 检查时间是否在指定范围内（支持跨天）
     *
     * @param currentTime 当前时间
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 是否在范围内
     */
    private boolean isTimeInRange(LocalTime currentTime, LocalTime startTime, LocalTime endTime) {
        if (startTime.isBefore(endTime)) {
            // 不跨天
            return !currentTime.isBefore(startTime) && !currentTime.isAfter(endTime);
        } else if (startTime.isAfter(endTime)) {
            // 跨天（例如 22:00-06:00）
            return !currentTime.isBefore(startTime) || !currentTime.isAfter(endTime);
        } else {
            // 开始时间等于结束时间，全天可用
            return true;
        }
    }

    /**
     * 获取当前时间适合的内容分类权重调整
     *
     * @param categoryName 分类名称
     * @param currentTime 当前时间
     * @return 权重调整系数
     */
    public double getCategoryWeightAdjustment(String categoryName, LocalDateTime currentTime) {
        DayOfWeek dayOfWeek = currentTime.getDayOfWeek();
        int hour = currentTime.getHour();

        // 根据时间和分类调整权重
        switch (categoryName) {
            case "聊天话题":
                // 晚上聊天话题权重更高
                if (hour >= 20 || hour <= 23) {
                    return 1.2;
                }
                break;
            case "运动健身":
                // 早晨和晚上运动健身权重更高
                if ((hour >= 6 && hour <= 9) || (hour >= 18 && hour <= 21)) {
                    return 1.3;
                }
                break;
            case "美食探索":
                // 饭点美食探索权重更高
                if ((hour >= 11 && hour <= 13) || (hour >= 17 && hour <= 19)) {
                    return 1.4;
                }
                break;
            case "约会建议":
                // 周末约会建议权重更高
                if (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY) {
                    return 1.1;
                }
                break;
            default:
                break;
        }

        return 1.0; // 默认无调整
    }
}
