package com.basebackend.wheel.service.impl;

import com.basebackend.wheel.config.AiAuditProperties;
import com.basebackend.wheel.dto.AiAuditResult;
import com.basebackend.wheel.dto.RiskDetail;
import com.basebackend.wheel.enums.AuditDecision;
import com.basebackend.wheel.service.AiContentAuditService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
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
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 阿里云内容安全服务实现
 * 
 * 集成阿里云内容安全API进行文本和图片审核
 *
 * @author wheel-api
 * @since 2025-02-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "wheel.ai-audit.aliyun", name = "enabled", havingValue = "true")
public class AliyunContentAuditService implements AiContentAuditService {

    private final AiAuditProperties aiAuditProperties;
    private final ObjectMapper objectMapper;

    private static final String PROVIDER_NAME = AiAuditResult.PROVIDER_ALIYUN;
    private static final String ALGORITHM = "ACS3-HMAC-SHA256";
    private static final String TEXT_MODERATION_ACTION = "TextModeration";
    private static final String IMAGE_MODERATION_ACTION = "ImageModeration";
    private static final String API_VERSION = "2022-03-02";

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
            AiAuditProperties.AliyunConfig config = aiAuditProperties.getAliyun();
            
            // 构建请求体
            ObjectNode requestBody = objectMapper.createObjectNode();
            requestBody.put("Service", "chat_detection");
            requestBody.put("ServiceParameters", objectMapper.writeValueAsString(
                    Map.of("content", text)
            ));

            // 发送请求
            String response = sendRequest(TEXT_MODERATION_ACTION, requestBody.toString(), config);
            
            // 解析响应
            return parseTextAuditResponse(response, startTime);
            
        } catch (Exception e) {
            log.error("阿里云文本审核失败: {}", e.getMessage(), e);
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
            AiAuditProperties.AliyunConfig config = aiAuditProperties.getAliyun();
            
            // 构建请求体
            ObjectNode requestBody = objectMapper.createObjectNode();
            requestBody.put("Service", "baselineCheck");
            requestBody.put("ServiceParameters", objectMapper.writeValueAsString(
                    Map.of("imageUrl", imageUrl)
            ));

            // 发送请求
            String response = sendRequest(IMAGE_MODERATION_ACTION, requestBody.toString(), config);
            
            // 解析响应
            return parseImageAuditResponse(response, startTime);
            
        } catch (Exception e) {
            log.error("阿里云图片审核失败: {}", e.getMessage(), e);
            return AiAuditResult.error(PROVIDER_NAME, e.getMessage(), System.currentTimeMillis() - startTime);
        }
    }

    @Override
    public List<AiAuditResult> batchAuditText(List<String> texts) {
        if (texts == null || texts.isEmpty()) {
            return Collections.emptyList();
        }
        
        // 阿里云支持批量，但为简化实现，这里逐个处理
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
        AiAuditProperties.AliyunConfig config = aiAuditProperties.getAliyun();
        return config.isEnabled() 
                && config.getAccessKeyId() != null 
                && !config.getAccessKeyId().isEmpty()
                && config.getAccessKeySecret() != null 
                && !config.getAccessKeySecret().isEmpty();
    }

    @Override
    public int getPriority() {
        return 1; // 阿里云优先级最高
    }

    /**
     * 发送HTTP请求到阿里云API
     */
    private String sendRequest(String action, String body, AiAuditProperties.AliyunConfig config) throws Exception {
        String host = config.getEndpoint();
        String url = "https://" + host + "/";
        
        // 生成签名所需的时间戳
        String timestamp = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
                .format(new Date());
        String nonce = UUID.randomUUID().toString();
        
        // 计算请求体的哈希
        String bodyHash = sha256Hex(body);
        
        // 构建规范请求
        Map<String, String> headers = new LinkedHashMap<>();
        headers.put("host", host);
        headers.put("x-acs-action", action);
        headers.put("x-acs-version", API_VERSION);
        headers.put("x-acs-date", timestamp);
        headers.put("x-acs-signature-nonce", nonce);
        headers.put("x-acs-content-sha256", bodyHash);
        headers.put("content-type", "application/json");
        
        // 计算签名
        String signature = calculateSignature(headers, body, config);
        
        // 构建Authorization头
        String signedHeaders = headers.keySet().stream()
                .sorted()
                .collect(Collectors.joining(";"));
        String authorization = ALGORITHM + " Credential=" + config.getAccessKeyId() 
                + ",SignedHeaders=" + signedHeaders 
                + ",Signature=" + signature;
        
        // 构建HTTP请求
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofMillis(aiAuditProperties.getReadTimeout()))
                .header("Authorization", authorization)
                .POST(HttpRequest.BodyPublishers.ofString(body));
        
        headers.forEach(requestBuilder::header);
        
        HttpRequest request = requestBuilder.build();
        
        // 发送请求
        HttpResponse<String> response = getHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() != 200) {
            throw new RuntimeException("阿里云API调用失败，状态码: " + response.statusCode() + ", 响应: " + response.body());
        }
        
        return response.body();
    }

    /**
     * 计算签名
     */
    private String calculateSignature(Map<String, String> headers, String body, AiAuditProperties.AliyunConfig config) throws Exception {
        // 构建规范请求字符串
        StringBuilder canonicalRequest = new StringBuilder();
        canonicalRequest.append("POST\n");
        canonicalRequest.append("/\n");
        canonicalRequest.append("\n"); // 查询字符串为空
        
        // 规范化头部
        String canonicalHeaders = headers.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> e.getKey().toLowerCase() + ":" + e.getValue().trim())
                .collect(Collectors.joining("\n")) + "\n";
        canonicalRequest.append(canonicalHeaders);
        canonicalRequest.append("\n");
        
        // 签名头部列表
        String signedHeaders = headers.keySet().stream()
                .sorted()
                .map(String::toLowerCase)
                .collect(Collectors.joining(";"));
        canonicalRequest.append(signedHeaders);
        canonicalRequest.append("\n");
        
        // 请求体哈希
        canonicalRequest.append(sha256Hex(body));
        
        // 计算规范请求的哈希
        String hashedCanonicalRequest = sha256Hex(canonicalRequest.toString());
        
        // 构建待签名字符串
        String stringToSign = ALGORITHM + "\n" + hashedCanonicalRequest;
        
        // 使用HMAC-SHA256计算签名
        return hmacSha256Hex(config.getAccessKeySecret(), stringToSign);
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
    private String hmacSha256Hex(String key, String data) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return bytesToHex(hash);
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
            
            // 检查是否有错误
            if (root.has("Code") && !"200".equals(root.get("Code").asText())) {
                String message = root.has("Message") ? root.get("Message").asText() : "未知错误";
                return AiAuditResult.error(PROVIDER_NAME, message, auditTime);
            }
            
            // 解析Data节点
            JsonNode data = root.get("Data");
            if (data == null) {
                return AiAuditResult.pass(PROVIDER_NAME, auditTime);
            }
            
            // 获取标签和风险分数
            String labels = data.has("Labels") ? data.get("Labels").asText() : "";
            String reason = data.has("Reason") ? data.get("Reason").asText() : "";
            
            List<RiskDetail> risks = new ArrayList<>();
            double riskScore = 0.0;
            
            if (labels != null && !labels.isEmpty()) {
                // 解析标签
                String[] labelArray = labels.split(",");
                for (String label : labelArray) {
                    RiskDetail risk = mapLabelToRiskDetail(label.trim(), reason);
                    if (risk != null) {
                        risks.add(risk);
                        riskScore = Math.max(riskScore, risk.getConfidence() * 100);
                    }
                }
            }
            
            // 根据风险分数决定审核结果
            AuditDecision decision = determineDecision(riskScore, risks);
            
            return AiAuditResult.builder()
                    .pass(decision == AuditDecision.PASS)
                    .decision(decision)
                    .riskScore(riskScore)
                    .risks(risks)
                    .provider(PROVIDER_NAME)
                    .auditTimeMs(auditTime)
                    .requestId(root.has("RequestId") ? root.get("RequestId").asText() : null)
                    .build();
                    
        } catch (Exception e) {
            log.error("解析阿里云文本审核响应失败: {}", e.getMessage(), e);
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
            
            // 检查是否有错误
            if (root.has("Code") && !"200".equals(root.get("Code").asText())) {
                String message = root.has("Message") ? root.get("Message").asText() : "未知错误";
                return AiAuditResult.error(PROVIDER_NAME, message, auditTime);
            }
            
            // 解析Data节点
            JsonNode data = root.get("Data");
            if (data == null) {
                return AiAuditResult.pass(PROVIDER_NAME, auditTime);
            }
            
            List<RiskDetail> risks = new ArrayList<>();
            double maxRiskScore = 0.0;
            
            // 解析Result数组
            if (data.has("Result") && data.get("Result").isArray()) {
                ArrayNode results = (ArrayNode) data.get("Result");
                for (JsonNode result : results) {
                    String label = result.has("Label") ? result.get("Label").asText() : "";
                    double confidence = result.has("Confidence") ? result.get("Confidence").asDouble() : 0.0;
                    
                    if (!"nonLabel".equals(label) && confidence > 0.5) {
                        RiskDetail risk = RiskDetail.builder()
                                .riskType(mapImageLabelToRiskType(label))
                                .riskLabel(label)
                                .confidence(confidence)
                                .suggestion(confidence > 0.8 ? RiskDetail.SUGGESTION_BLOCK : RiskDetail.SUGGESTION_REVIEW)
                                .riskLevel(confidence > 0.8 ? 3 : 2)
                                .build();
                        risks.add(risk);
                        maxRiskScore = Math.max(maxRiskScore, confidence * 100);
                    }
                }
            }
            
            // 根据风险分数决定审核结果
            AuditDecision decision = determineDecision(maxRiskScore, risks);
            
            return AiAuditResult.builder()
                    .pass(decision == AuditDecision.PASS)
                    .decision(decision)
                    .riskScore(maxRiskScore)
                    .risks(risks)
                    .provider(PROVIDER_NAME)
                    .auditTimeMs(auditTime)
                    .requestId(root.has("RequestId") ? root.get("RequestId").asText() : null)
                    .build();
                    
        } catch (Exception e) {
            log.error("解析阿里云图片审核响应失败: {}", e.getMessage(), e);
            return AiAuditResult.error(PROVIDER_NAME, "响应解析失败: " + e.getMessage(), 
                    System.currentTimeMillis() - startTime);
        }
    }

    /**
     * 将标签映射为风险详情
     */
    private RiskDetail mapLabelToRiskDetail(String label, String reason) {
        if (label == null || label.isEmpty()) {
            return null;
        }
        
        String riskType;
        String riskLabel;
        double confidence = 0.9; // 默认置信度
        
        switch (label.toLowerCase()) {
            case "porn":
            case "sexy":
                riskType = RiskDetail.RISK_TYPE_PORN;
                riskLabel = "色情内容";
                break;
            case "violence":
            case "bloody":
                riskType = RiskDetail.RISK_TYPE_VIOLENCE;
                riskLabel = "暴力血腥";
                break;
            case "politics":
            case "political":
                riskType = RiskDetail.RISK_TYPE_POLITICS;
                riskLabel = "政治敏感";
                break;
            case "ad":
            case "advertisement":
            case "spam":
                riskType = RiskDetail.RISK_TYPE_AD;
                riskLabel = "广告推广";
                break;
            case "abuse":
            case "insult":
                riskType = RiskDetail.RISK_TYPE_ABUSE;
                riskLabel = "辱骂内容";
                break;
            case "terrorism":
            case "terror":
                riskType = RiskDetail.RISK_TYPE_TERRORISM;
                riskLabel = "恐怖主义";
                break;
            case "contraband":
                riskType = RiskDetail.RISK_TYPE_CONTRABAND;
                riskLabel = "违禁品";
                break;
            default:
                riskType = RiskDetail.RISK_TYPE_OTHER;
                riskLabel = label;
        }
        
        return RiskDetail.builder()
                .riskType(riskType)
                .riskLabel(riskLabel)
                .confidence(confidence)
                .suggestion(confidence > 0.8 ? RiskDetail.SUGGESTION_BLOCK : RiskDetail.SUGGESTION_REVIEW)
                .riskLevel(confidence > 0.8 ? 3 : 2)
                .hitContent(reason)
                .build();
    }

    /**
     * 将图片标签映射为风险类型
     */
    private String mapImageLabelToRiskType(String label) {
        if (label == null) {
            return RiskDetail.RISK_TYPE_OTHER;
        }
        
        switch (label.toLowerCase()) {
            case "porn":
            case "sexy":
                return RiskDetail.RISK_TYPE_PORN;
            case "terrorism":
            case "bloody":
                return RiskDetail.RISK_TYPE_TERRORISM;
            case "ad":
            case "qrcode":
                return RiskDetail.RISK_TYPE_AD;
            case "politics":
                return RiskDetail.RISK_TYPE_POLITICS;
            default:
                return RiskDetail.RISK_TYPE_OTHER;
        }
    }

    /**
     * 根据风险分数和风险详情决定审核结果
     */
    private AuditDecision determineDecision(double riskScore, List<RiskDetail> risks) {
        if (risks.isEmpty() || riskScore < 30) {
            return AuditDecision.PASS;
        } else if (riskScore >= 80) {
            return AuditDecision.REJECT;
        } else {
            return AuditDecision.REVIEW;
        }
    }
}
