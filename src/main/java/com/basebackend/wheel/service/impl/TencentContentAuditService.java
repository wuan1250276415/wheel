package com.basebackend.wheel.service.impl;

import com.basebackend.wheel.config.AiAuditProperties;
import com.basebackend.wheel.dto.AiAuditResult;
import com.basebackend.wheel.dto.RiskDetail;
import com.basebackend.wheel.enums.AuditDecision;
import com.basebackend.wheel.service.AiContentAuditService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 腾讯云天御内容安全服务实现
 * 
 * 集成腾讯云天御API进行文本和图片审核
 *
 * @author wheel-api
 * @since 2025-02-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "wheel.ai-audit.tencent", name = "enabled", havingValue = "true")
public class TencentContentAuditService implements AiContentAuditService {

    private final AiAuditProperties aiAuditProperties;
    private final ObjectMapper objectMapper;

    private static final String PROVIDER_NAME = AiAuditResult.PROVIDER_TENCENT;
    private static final String ALGORITHM = "TC3-HMAC-SHA256";
    private static final String SERVICE_TEXT = "tms";
    private static final String SERVICE_IMAGE = "ims";
    private static final String TEXT_ACTION = "TextModeration";
    private static final String IMAGE_ACTION = "ImageModeration";
    private static final String API_VERSION = "2020-12-29";

    private HttpClient httpClient;

    private HttpClient getHttpClient() {
        if (httpClient == null) {
            httpClient = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofMillis(aiAuditProperties.getConnectTimeout()))
                    .build();
        }
        return httpClient;
    }

    @Override
    public AiAuditResult auditText(String text) {
        long startTime = System.currentTimeMillis();
        
        if (text == null || text.trim().isEmpty()) {
            return AiAuditResult.pass(PROVIDER_NAME, System.currentTimeMillis() - startTime);
        }

        try {
            AiAuditProperties.TencentConfig config = aiAuditProperties.getTencent();
            
            // 构建请求体
            ObjectNode requestBody = objectMapper.createObjectNode();
            // 将文本转为Base64编码
            String base64Content = Base64.getEncoder().encodeToString(text.getBytes(StandardCharsets.UTF_8));
            requestBody.put("Content", base64Content);

            // 发送请求
            String response = sendRequest(SERVICE_TEXT, TEXT_ACTION, requestBody.toString(), config);
            
            // 解析响应
            return parseTextAuditResponse(response, startTime);
            
        } catch (Exception e) {
            log.error("腾讯云文本审核失败: {}", e.getMessage(), e);
            return AiAuditResult.error(PROVIDER_NAME, e.getMessage(), System.currentTimeMillis() - startTime);
        }
    }

    @Override
    public AiAuditResult auditImage(String imageUrl) {
        long startTime = System.currentTimeMillis();
        
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            return AiAuditResult.pass(PROVIDER_NAME, System.currentTimeMillis() - startTime);
        }

        try {
            AiAuditProperties.TencentConfig config = aiAuditProperties.getTencent();
            
            // 构建请求体
            ObjectNode requestBody = objectMapper.createObjectNode();
            requestBody.put("FileUrl", imageUrl);

            // 发送请求
            String response = sendRequest(SERVICE_IMAGE, IMAGE_ACTION, requestBody.toString(), config);
            
            // 解析响应
            return parseImageAuditResponse(response, startTime);
            
        } catch (Exception e) {
            log.error("腾讯云图片审核失败: {}", e.getMessage(), e);
            return AiAuditResult.error(PROVIDER_NAME, e.getMessage(), System.currentTimeMillis() - startTime);
        }
    }

    @Override
    public List<AiAuditResult> batchAuditText(List<String> texts) {
        if (texts == null || texts.isEmpty()) {
            return Collections.emptyList();
        }
        
        return texts.stream()
                .map(this::auditText)
                .collect(Collectors.toList());
    }

    @Override
    public List<AiAuditResult> batchAuditImage(List<String> imageUrls) {
        if (imageUrls == null || imageUrls.isEmpty()) {
            return Collections.emptyList();
        }
        
        return imageUrls.stream()
                .map(this::auditImage)
                .collect(Collectors.toList());
    }

    @Override
    public String getProviderName() {
        return PROVIDER_NAME;
    }

    @Override
    public boolean isAvailable() {
        AiAuditProperties.TencentConfig config = aiAuditProperties.getTencent();
        return config.isEnabled() 
                && config.getSecretId() != null 
                && !config.getSecretId().isEmpty()
                && config.getSecretKey() != null 
                && !config.getSecretKey().isEmpty();
    }

    @Override
    public int getPriority() {
        return 2; // 腾讯云优先级次于阿里云
    }

    /**
     * 发送HTTP请求到腾讯云API
     */
    private String sendRequest(String service, String action, String body, AiAuditProperties.TencentConfig config) throws Exception {
        String host = service.equals(SERVICE_TEXT) ? config.getEndpoint() : config.getImageEndpoint();
        String url = "https://" + host + "/";
        
        // 获取时间戳
        long timestamp = Instant.now().getEpochSecond();
        String date = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                .withZone(ZoneOffset.UTC)
                .format(Instant.ofEpochSecond(timestamp));
        
        // 计算签名
        String authorization = calculateAuthorization(service, action, body, host, timestamp, date, config);
        
        // 构建HTTP请求
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofMillis(aiAuditProperties.getReadTimeout()))
                .header("Authorization", authorization)
                .header("Content-Type", "application/json; charset=utf-8")
                .header("Host", host)
                .header("X-TC-Action", action)
                .header("X-TC-Timestamp", String.valueOf(timestamp))
                .header("X-TC-Version", API_VERSION)
                .header("X-TC-Region", config.getRegion())
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        
        // 发送请求
        HttpResponse<String> response = getHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() != 200) {
            throw new RuntimeException("腾讯云API调用失败，状态码: " + response.statusCode() + ", 响应: " + response.body());
        }
        
        return response.body();
    }

    /**
     * 计算腾讯云API签名
     */
    private String calculateAuthorization(String service, String action, String body, String host, 
                                          long timestamp, String date, AiAuditProperties.TencentConfig config) throws Exception {
        // 步骤1：拼接规范请求串
        String httpRequestMethod = "POST";
        String canonicalUri = "/";
        String canonicalQueryString = "";
        String canonicalHeaders = "content-type:application/json; charset=utf-8\n" + "host:" + host + "\n";
        String signedHeaders = "content-type;host";
        String hashedRequestPayload = sha256Hex(body);
        
        String canonicalRequest = httpRequestMethod + "\n" 
                + canonicalUri + "\n" 
                + canonicalQueryString + "\n"
                + canonicalHeaders + "\n" 
                + signedHeaders + "\n" 
                + hashedRequestPayload;
        
        // 步骤2：拼接待签名字符串
        String credentialScope = date + "/" + service + "/tc3_request";
        String hashedCanonicalRequest = sha256Hex(canonicalRequest);
        String stringToSign = ALGORITHM + "\n" 
                + timestamp + "\n" 
                + credentialScope + "\n" 
                + hashedCanonicalRequest;
        
        // 步骤3：计算签名
        byte[] secretDate = hmacSha256(("TC3" + config.getSecretKey()).getBytes(StandardCharsets.UTF_8), date);
        byte[] secretService = hmacSha256(secretDate, service);
        byte[] secretSigning = hmacSha256(secretService, "tc3_request");
        String signature = bytesToHex(hmacSha256(secretSigning, stringToSign));
        
        // 步骤4：拼接Authorization
        return ALGORITHM + " " 
                + "Credential=" + config.getSecretId() + "/" + credentialScope + ", "
                + "SignedHeaders=" + signedHeaders + ", "
                + "Signature=" + signature;
    }

    /**
     * SHA256哈希
     */
    private String sha256Hex(String data) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(data.getBytes(StandardCharsets.UTF_8));
        return bytesToHex(hash);
    }

    /**
     * HMAC-SHA256签名
     */
    private byte[] hmacSha256(byte[] key, String data) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(key, "HmacSHA256"));
        return mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 字节数组转十六进制字符串
     */
    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    /**
     * 解析文本审核响应
     */
    private AiAuditResult parseTextAuditResponse(String response, long startTime) {
        try {
            JsonNode root = objectMapper.readTree(response);
            long auditTime = System.currentTimeMillis() - startTime;
            
            // 获取Response节点
            JsonNode responseNode = root.get("Response");
            if (responseNode == null) {
                return AiAuditResult.error(PROVIDER_NAME, "响应格式错误", auditTime);
            }
            
            // 检查是否有错误
            if (responseNode.has("Error")) {
                JsonNode error = responseNode.get("Error");
                String message = error.has("Message") ? error.get("Message").asText() : "未知错误";
                return AiAuditResult.error(PROVIDER_NAME, message, auditTime);
            }
            
            String requestId = responseNode.has("RequestId") ? responseNode.get("RequestId").asText() : null;
            
            // 获取审核建议
            String suggestion = responseNode.has("Suggestion") ? responseNode.get("Suggestion").asText() : "Pass";
            int score = responseNode.has("Score") ? responseNode.get("Score").asInt() : 0;
            
            List<RiskDetail> risks = new ArrayList<>();
            double riskScore = score;
            
            // 解析详细结果
            if (responseNode.has("DetailResults") && responseNode.get("DetailResults").isArray()) {
                for (JsonNode detail : responseNode.get("DetailResults")) {
                    String detailLabel = detail.has("Label") ? detail.get("Label").asText() : "";
                    int detailScore = detail.has("Score") ? detail.get("Score").asInt() : 0;
                    String detailSuggestion = detail.has("Suggestion") ? detail.get("Suggestion").asText() : "";
                    
                    if (!"Normal".equals(detailLabel) && detailScore > 50) {
                        RiskDetail risk = RiskDetail.builder()
                                .riskType(mapTencentLabelToRiskType(detailLabel))
                                .riskLabel(detailLabel)
                                .confidence(detailScore / 100.0)
                                .suggestion(mapTencentSuggestion(detailSuggestion))
                                .riskLevel(detailScore > 80 ? 3 : 2)
                                .build();
                        
                        // 获取关键词
                        if (detail.has("Keywords") && detail.get("Keywords").isArray()) {
                            StringBuilder keywords = new StringBuilder();
                            for (JsonNode keyword : detail.get("Keywords")) {
                                if (keywords.length() > 0) keywords.append(", ");
                                keywords.append(keyword.asText());
                            }
                            risk.setHitContent(keywords.toString());
                        }
                        
                        risks.add(risk);
                        riskScore = Math.max(riskScore, detailScore);
                    }
                }
            }
            
            // 根据建议决定审核结果
            AuditDecision decision = mapTencentSuggestionToDecision(suggestion, riskScore);
            
            return AiAuditResult.builder()
                    .pass(decision == AuditDecision.PASS)
                    .decision(decision)
                    .riskScore(riskScore)
                    .risks(risks)
                    .provider(PROVIDER_NAME)
                    .auditTimeMs(auditTime)
                    .requestId(requestId)
                    .build();
                    
        } catch (Exception e) {
            log.error("解析腾讯云文本审核响应失败: {}", e.getMessage(), e);
            return AiAuditResult.error(PROVIDER_NAME, "响应解析失败: " + e.getMessage(), 
                    System.currentTimeMillis() - startTime);
        }
    }

    /**
     * 解析图片审核响应
     */
    private AiAuditResult parseImageAuditResponse(String response, long startTime) {
        try {
            JsonNode root = objectMapper.readTree(response);
            long auditTime = System.currentTimeMillis() - startTime;
            
            // 获取Response节点
            JsonNode responseNode = root.get("Response");
            if (responseNode == null) {
                return AiAuditResult.error(PROVIDER_NAME, "响应格式错误", auditTime);
            }
            
            // 检查是否有错误
            if (responseNode.has("Error")) {
                JsonNode error = responseNode.get("Error");
                String message = error.has("Message") ? error.get("Message").asText() : "未知错误";
                return AiAuditResult.error(PROVIDER_NAME, message, auditTime);
            }
            
            String requestId = responseNode.has("RequestId") ? responseNode.get("RequestId").asText() : null;
            
            // 获取审核建议
            String suggestion = responseNode.has("Suggestion") ? responseNode.get("Suggestion").asText() : "Pass";
            int score = responseNode.has("Score") ? responseNode.get("Score").asInt() : 0;
            
            List<RiskDetail> risks = new ArrayList<>();
            double riskScore = score;
            
            // 解析标签结果
            String[] labelTypes = {"LabelResults", "ObjectResults", "OcrResults"};
            for (String labelType : labelTypes) {
                if (responseNode.has(labelType) && responseNode.get(labelType).isArray()) {
                    for (JsonNode result : responseNode.get(labelType)) {
                        String resultLabel = result.has("Label") ? result.get("Label").asText() : "";
                        int resultScore = result.has("Score") ? result.get("Score").asInt() : 0;
                        String resultSuggestion = result.has("Suggestion") ? result.get("Suggestion").asText() : "";
                        
                        if (!"Normal".equals(resultLabel) && resultScore > 50) {
                            RiskDetail risk = RiskDetail.builder()
                                    .riskType(mapTencentLabelToRiskType(resultLabel))
                                    .riskLabel(resultLabel)
                                    .confidence(resultScore / 100.0)
                                    .suggestion(mapTencentSuggestion(resultSuggestion))
                                    .riskLevel(resultScore > 80 ? 3 : 2)
                                    .build();
                            risks.add(risk);
                            riskScore = Math.max(riskScore, resultScore);
                        }
                    }
                }
            }
            
            // 根据建议决定审核结果
            AuditDecision decision = mapTencentSuggestionToDecision(suggestion, riskScore);
            
            return AiAuditResult.builder()
                    .pass(decision == AuditDecision.PASS)
                    .decision(decision)
                    .riskScore(riskScore)
                    .risks(risks)
                    .provider(PROVIDER_NAME)
                    .auditTimeMs(auditTime)
                    .requestId(requestId)
                    .build();
                    
        } catch (Exception e) {
            log.error("解析腾讯云图片审核响应失败: {}", e.getMessage(), e);
            return AiAuditResult.error(PROVIDER_NAME, "响应解析失败: " + e.getMessage(), 
                    System.currentTimeMillis() - startTime);
        }
    }

    /**
     * 将腾讯云标签映射为风险类型
     */
    private String mapTencentLabelToRiskType(String label) {
        if (label == null) {
            return RiskDetail.RISK_TYPE_OTHER;
        }
        
        switch (label.toLowerCase()) {
            case "porn":
            case "sexy":
            case "vulgar":
                return RiskDetail.RISK_TYPE_PORN;
            case "violence":
            case "bloody":
            case "terror":
                return RiskDetail.RISK_TYPE_VIOLENCE;
            case "politics":
            case "polity":
                return RiskDetail.RISK_TYPE_POLITICS;
            case "ad":
            case "advertisement":
            case "qrcode":
                return RiskDetail.RISK_TYPE_AD;
            case "abuse":
            case "insult":
                return RiskDetail.RISK_TYPE_ABUSE;
            case "terrorism":
                return RiskDetail.RISK_TYPE_TERRORISM;
            case "contraband":
                return RiskDetail.RISK_TYPE_CONTRABAND;
            case "spam":
                return RiskDetail.RISK_TYPE_SPAM;
            default:
                return RiskDetail.RISK_TYPE_OTHER;
        }
    }

    /**
     * 映射腾讯云建议到标准建议
     */
    private String mapTencentSuggestion(String suggestion) {
        if (suggestion == null) {
            return RiskDetail.SUGGESTION_REVIEW;
        }
        
        switch (suggestion.toLowerCase()) {
            case "pass":
                return RiskDetail.SUGGESTION_PASS;
            case "block":
                return RiskDetail.SUGGESTION_BLOCK;
            case "review":
            default:
                return RiskDetail.SUGGESTION_REVIEW;
        }
    }

    /**
     * 将腾讯云建议映射为审核决策
     */
    private AuditDecision mapTencentSuggestionToDecision(String suggestion, double riskScore) {
        if (suggestion == null) {
            return AuditDecision.REVIEW;
        }
        
        switch (suggestion.toLowerCase()) {
            case "pass":
                return AuditDecision.PASS;
            case "block":
                return AuditDecision.REJECT;
            case "review":
            default:
                // 根据分数进一步判断
                if (riskScore < 30) {
                    return AuditDecision.PASS;
                } else if (riskScore >= 80) {
                    return AuditDecision.REJECT;
                }
                return AuditDecision.REVIEW;
        }
    }
}
