package com.basebackend.wheel.service.impl;

import cn.jiguang.common.resp.APIConnectionException;
import cn.jiguang.common.resp.APIRequestException;
import cn.jpush.api.JPushClient;
import cn.jpush.api.push.PushResult;
import cn.jpush.api.push.model.Options;
import cn.jpush.api.push.model.Platform;
import cn.jpush.api.push.model.PushPayload;
import cn.jpush.api.push.model.audience.Audience;
import cn.jpush.api.push.model.notification.AndroidNotification;
import cn.jpush.api.push.model.notification.IosNotification;
import cn.jpush.api.push.model.notification.Notification;
import com.basebackend.wheel.config.JPushProperties;
import com.basebackend.wheel.entity.ContentReport;
import com.basebackend.wheel.service.ReportNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * 举报通知服务实现
 *
 * @author wheel-api
 * @since 2025-02-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReportNotificationServiceImpl implements ReportNotificationService {

    private final ObjectProvider<JPushClient> jPushClientProvider;
    private final JPushProperties jPushProperties;

    /**
     * 举报原因描述映射
     */
    private static final Map<Integer, String> REPORT_REASON_DESC = Map.of(
            0, "色情低俗",
            1, "暴力血腥",
            2, "政治敏感",
            3, "广告骚扰",
            4, "侵权内容",
            5, "其他"
    );

    @Override
    @Async
    public void notifyReporter(ContentReport report, String handleResult, boolean isValid) {
        if (report == null || report.getReporterUserId() == null) {
            return;
        }

        String alert;
        if (isValid) {
            alert = "您的举报已处理，感谢您对平台环境的维护！";
        } else {
            alert = "您的举报已处理，经核实该内容未违规。";
        }

        if (StringUtils.hasText(handleResult)) {
            alert = alert + " 处理结果：" + handleResult;
        }

        Map<String, String> extras = new HashMap<>();
        extras.put("event", "report_result");
        extras.put("reportId", String.valueOf(report.getId()));
        extras.put("isValid", String.valueOf(isValid));

        sendNotification(report.getReporterUserId(), alert, extras);
        log.info("已通知举报人 {} 举报处理结果，举报ID: {}", report.getReporterUserId(), report.getId());
    }

    @Override
    @Async
    public void notifyContentAuthor(Long contentId, Long authorId, Integer reportReason) {
        if (authorId == null) {
            return;
        }

        String reasonDesc = REPORT_REASON_DESC.getOrDefault(reportReason, "违规内容");
        String alert = String.format("您发布的内容因涉嫌「%s」被举报，请注意遵守平台规范。", reasonDesc);

        Map<String, String> extras = new HashMap<>();
        extras.put("event", "content_reported");
        extras.put("contentId", String.valueOf(contentId));
        extras.put("reportReason", String.valueOf(reportReason));

        sendNotification(authorId, alert, extras);
        log.info("已通知内容作者 {} 内容被举报，内容ID: {}", authorId, contentId);
    }

    @Override
    @Async
    public void notifyUserBlocked(Long userId, String reason) {
        if (userId == null) {
            return;
        }

        String alert = "您的账号已被限制，原因：" + (StringUtils.hasText(reason) ? reason : "违反平台规定");
        alert += "。如有疑问，请联系客服申诉。";

        Map<String, String> extras = new HashMap<>();
        extras.put("event", "user_blocked");
        extras.put("reason", reason != null ? reason : "");

        sendNotification(userId, alert, extras);
        log.info("已通知用户 {} 账号被封禁", userId);
    }

    /**
     * 发送推送通知
     */
    private void sendNotification(Long userId, String alert, Map<String, String> extras) {
        if (!isPushEnabled()) {
            log.debug("推送服务未启用，跳过通知: {}", alert);
            return;
        }

        JPushClient client = jPushClientProvider.getIfAvailable();
        if (client == null) {
            log.debug("JPushClient未配置，跳过推送: {}", alert);
            return;
        }

        PushPayload payload = PushPayload.newBuilder()
                .setPlatform(Platform.all())
                .setAudience(Audience.alias(String.valueOf(userId)))
                .setNotification(buildNotification(alert, extras))
                .setOptions(Options.newBuilder()
                        .setApnsProduction(jPushProperties.isApnsProduction())
                        .build())
                .build();

        try {
            PushResult result = client.sendPush(payload);
            log.debug("推送通知成功 userId={}, msgId={}", userId, result.msg_id);
        } catch (APIConnectionException | APIRequestException ex) {
            log.warn("推送通知失败 userId={}, error={}", userId, ex.getMessage());
        }
    }

    /**
     * 检查推送服务是否启用
     */
    private boolean isPushEnabled() {
        return jPushProperties.isEnabled()
                && StringUtils.hasText(jPushProperties.getAppKey())
                && StringUtils.hasText(jPushProperties.getMasterSecret());
    }

    /**
     * 构建通知内容
     */
    private Notification buildNotification(String alert, Map<String, String> extras) {
        AndroidNotification.Builder android = AndroidNotification.newBuilder()
                .setTitle(jPushProperties.getTitle())
                .setAlert(alert);
        extras.forEach(android::addExtra);

        IosNotification.Builder ios = IosNotification.newBuilder()
                .setAlert(alert);
        extras.forEach(ios::addExtra);

        return Notification.newBuilder()
                .setAlert(alert)
                .addPlatformNotification(android.build())
                .addPlatformNotification(ios.build())
                .build();
    }
}
