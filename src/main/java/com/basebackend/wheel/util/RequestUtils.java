package com.basebackend.wheel.util;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * HTTP请求工具类
 */
public final class RequestUtils {

    private static final String[] IP_HEADERS = {
            "X-Forwarded-For",
            "X-Real-IP",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_CLIENT_IP",
            "HTTP_X_FORWARDED_FOR"
    };

    private static final String DEVICE_ID_HEADER = "X-Device-ID";
    private static final String UNKNOWN = "unknown";

    private RequestUtils() {
    }

    public static String getClientIp() {
        HttpServletRequest request = getCurrentRequest();
        return request != null ? getClientIp(request) : "0.0.0.0";
    }

    public static String getClientIp(HttpServletRequest request) {
        if (request == null) {
            return "0.0.0.0";
        }

        for (String header : IP_HEADERS) {
            String ip = request.getHeader(header);
            if (isValidIp(ip)) {
                return ip.contains(",") ? ip.split(",")[0].trim() : ip;
            }
        }

        String remoteAddr = request.getRemoteAddr();
        return remoteAddr != null ? remoteAddr : "0.0.0.0";
    }

    public static String getDeviceId() {
        HttpServletRequest request = getCurrentRequest();
        return request != null ? getDeviceId(request) : null;
    }

    public static ClientInfo getClientInfo() {
        HttpServletRequest request = getCurrentRequest();
        return request != null ? getClientInfo(request) : new ClientInfo("0.0.0.0", null);
    }

    public static ClientInfo getClientInfo(HttpServletRequest request) {
        if (request == null) {
            return new ClientInfo("0.0.0.0", null);
        }
        return new ClientInfo(getClientIp(request), getDeviceId(request));
    }

    public static String getDeviceId(HttpServletRequest request) {
        if (request == null) {
            return null;
        }

        String deviceId = request.getHeader(DEVICE_ID_HEADER);
        if (deviceId != null && !deviceId.isBlank()) {
            return deviceId;
        }

        String userAgent = request.getHeader("User-Agent");
        String sessionId = request.getSession(false) != null ? request.getSession().getId() : null;

        if (userAgent != null && sessionId != null) {
            return Integer.toHexString((userAgent + sessionId).hashCode());
        }

        return userAgent != null ? Integer.toHexString(userAgent.hashCode()) : null;
    }

    private static boolean isValidIp(String ip) {
        return ip != null && !ip.isBlank() && !UNKNOWN.equalsIgnoreCase(ip);
    }

    private static HttpServletRequest getCurrentRequest() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attrs != null ? attrs.getRequest() : null;
    }

    public static final class ClientInfo {
        private final String ipAddress;
        private final String deviceId;

        public ClientInfo(String ipAddress, String deviceId) {
            this.ipAddress = ipAddress;
            this.deviceId = deviceId;
        }

        public String getIpAddress() {
            return ipAddress;
        }

        public String getDeviceId() {
            return deviceId;
        }
    }
}
