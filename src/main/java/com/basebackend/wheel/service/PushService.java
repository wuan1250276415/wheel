package com.basebackend.wheel.service;

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
import com.basebackend.wheel.entity.ChatMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class PushService {

    private final ObjectProvider<JPushClient> jPushClientProvider;
    private final JPushProperties jPushProperties;

    public void sendOfflineMessageNotification(ChatMessage message, String senderNickname) {
        if (!isPushEnabled(message)) {
            return;
        }
        Map<String, String> extras = buildCommonExtras(message);
        extras.put("event", "message");
        String alert = String.format("%s: %s", resolveDisplayName(senderNickname), previewContent(message));
        sendPayload(message.getReceiverId(), alert, extras);
    }

    public void sendRecallNotification(ChatMessage message, String senderNickname) {
        if (!isPushEnabled(message)) {
            return;
        }
        Map<String, String> extras = buildCommonExtras(message);
        extras.put("event", "recall");
        String alert = String.format("%s撤回了一条消息", resolveDisplayName(senderNickname));
        sendPayload(message.getReceiverId(), alert, extras);
    }

    private boolean isPushEnabled(ChatMessage message) {
        return jPushProperties.isEnabled()
                && StringUtils.hasText(jPushProperties.getAppKey())
                && StringUtils.hasText(jPushProperties.getMasterSecret())
                && message != null
                && message.getReceiverId() != null;
    }

    private Map<String, String> buildCommonExtras(ChatMessage message) {
        Map<String, String> extras = new HashMap<>();
        if (message.getId() != null) {
            extras.put("messageId", String.valueOf(message.getId()));
        }
        if (message.getCoupleId() != null) {
            extras.put("coupleId", String.valueOf(message.getCoupleId()));
        }
        if (message.getMessageType() != null) {
            extras.put("messageType", String.valueOf(message.getMessageType()));
        }
        return extras;
    }

    private void sendPayload(Long receiverId, String alert, Map<String, String> extras) {
        JPushClient client = jPushClientProvider.getIfAvailable();
        if (client == null) {
            log.debug("JPushClient未配置，跳过推送: {}", alert);
            return;
        }

        PushPayload payload = PushPayload.newBuilder()
                .setPlatform(Platform.all())
                .setAudience(Audience.alias(String.valueOf(receiverId)))
                .setNotification(buildNotification(alert, extras))
                .setOptions(Options.newBuilder().setApnsProduction(jPushProperties.isApnsProduction()).build())
                .build();

        try {
            PushResult result = client.sendPush(payload);
            log.debug("离线推送成功 alias={}, msgId={}", receiverId, result.msg_id);
        } catch (APIConnectionException | APIRequestException ex) {
            log.warn("离线推送失败 alias={}, error={}", receiverId, ex.getMessage());
        }
    }

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

    private String resolveDisplayName(String senderNickname) {
        return StringUtils.hasText(senderNickname) ? senderNickname : "你的伴侣";
    }

    private String previewContent(ChatMessage message) {
        Integer type = message.getMessageType();
        if (type == null || type == 1) {
            String content = message.getContent();
            if (StringUtils.hasText(content)) {
                return content.length() > 50 ? content.substring(0, 50) + "..." : content;
            }
            return "发来了一条消息";
        }
        return switch (type) {
            case 2 -> "[图片]";
            case 3 -> "[转盘结果]";
            case 4 -> "[系统消息]";
            default -> "[消息]";
        };
    }
}
