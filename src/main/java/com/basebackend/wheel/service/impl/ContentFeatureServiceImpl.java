package com.basebackend.wheel.service.impl;

import com.basebackend.wheel.dto.ContentFeatureDTO;
import com.basebackend.wheel.entity.WheelContent;
import com.basebackend.wheel.enums.DifficultyLevel;
import com.basebackend.wheel.mapper.WheelContentMapper;
import com.basebackend.wheel.mapper.WheelSpinRecordMapper;
import com.basebackend.wheel.service.ContentFeatureService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 内容特征服务实现类
 * 负责提取和维护内容特征数据，支持推荐系统
 *
 * @author wheel-api
 * @since 2025-01-31
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ContentFeatureServiceImpl implements ContentFeatureService {

    private final WheelContentMapper wheelContentMapper;
    private final WheelSpinRecordMapper wheelSpinRecordMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    // 缓存键前缀
    private static final String FEATURE_CACHE_PREFIX = "recommendation:content_feature:";
    
    // 缓存过期时间（秒）
    private static final long FEATURE_CACHE_TTL = 1800; // 30分钟
    
    // 热度计算时间窗口（天）
    private static final int POPULARITY_WINDOW_DAYS = 7;
    
    // 时间衰减因子
    private static final double TIME_DECAY_FACTOR = 0.9;
    
    // 难度分类关键词
    private static final Set<String> EASY_KEYWORDS = Set.of(
            "简单", "轻松", "基础", "入门", "日常", "普通", "常见", "基本"
    );
    
    private static final Set<String> HARD_KEYWORDS = Set.of(
            "挑战", "困难", "高级", "极限", "冒险", "刺激", "大胆", "疯狂", "极端"
    );


    @Override
    public ContentFeatureDTO extractFeatures(WheelContent content) {
        if (content == null || content.getId() == null) {
            log.warn("提取内容特征失败：内容为空");
            return null;
        }

        log.info("开始提取内容特征: contentId={}", content.getId());

        ContentFeatureDTO feature = new ContentFeatureDTO();
        feature.setContentId(content.getId());

        // 1. 提取标签
        List<String> tags = extractTags(content);
        feature.setTags(tags);

        // 2. 计算热度分数
        double popularityScore = calculatePopularityScore(content.getId());
        feature.setPopularityScore(popularityScore);

        // 3. 分类难度等级
        DifficultyLevel difficultyLevel = categorizeContent(content);
        feature.setDifficultyLevel(difficultyLevel.getCode());
        feature.setDifficultyDesc(difficultyLevel.getDescription());

        // 4. 生成特征向量
        double[] featureVector = generateFeatureVector(content, tags, popularityScore, difficultyLevel);
        feature.setFeatureVector(featureVector);

        // 5. 更新数据库中的特征信息
        updateContentFeatureInDb(content.getId(), popularityScore, difficultyLevel, featureVector);

        // 6. 缓存特征
        cacheFeature(feature);

        log.info("内容特征提取完成: contentId={}, tags={}, popularity={}, difficulty={}", 
                content.getId(), tags.size(), popularityScore, difficultyLevel);

        return feature;
    }

    @Override
    public double calculatePopularityScore(Long contentId) {
        if (contentId == null) {
            return 0.0;
        }

        try {
            // 1. 获取总转盘次数
            int totalSpins = wheelSpinRecordMapper.countByContentId(contentId);
            
            // 2. 获取最近7天的转盘次数
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime startTime = now.minusDays(POPULARITY_WINDOW_DAYS);
            int recentSpins = wheelSpinRecordMapper.countByContentIdInRange(contentId, startTime, now);
            
            // 3. 获取最近一次转盘时间
            LocalDateTime lastSpinTime = wheelSpinRecordMapper.getLastSpinTimeByContentId(contentId);
            
            // 4. 计算时间衰减
            double timeDecay = calculateTimeDecay(lastSpinTime);
            
            // 5. 综合计算热度分数
            // 公式: score = (recentSpins * 0.7 + totalSpins * 0.3) * timeDecay / normalizer
            double rawScore = (recentSpins * 0.7 + totalSpins * 0.3) * timeDecay;
            
            // 归一化到0-1范围（假设最大转盘次数为1000）
            double normalizedScore = Math.min(1.0, rawScore / 100.0);
            
            log.debug("计算热度分数: contentId={}, totalSpins={}, recentSpins={}, timeDecay={}, score={}", 
                    contentId, totalSpins, recentSpins, timeDecay, normalizedScore);
            
            return normalizedScore;
        } catch (Exception e) {
            log.error("计算热度分数失败: contentId={}, error={}", contentId, e.getMessage());
            return 0.0;
        }
    }

    @Override
    public DifficultyLevel categorizeContent(WheelContent content) {
        if (content == null || !StringUtils.hasText(content.getContentText())) {
            return DifficultyLevel.MEDIUM; // 默认中等难度
        }

        String text = content.getContentText().toLowerCase();
        
        // 1. 检查是否包含困难关键词
        for (String keyword : HARD_KEYWORDS) {
            if (text.contains(keyword)) {
                log.debug("内容包含困难关键词: contentId={}, keyword={}", content.getId(), keyword);
                return DifficultyLevel.HARD;
            }
        }
        
        // 2. 检查是否包含简单关键词
        for (String keyword : EASY_KEYWORDS) {
            if (text.contains(keyword)) {
                log.debug("内容包含简单关键词: contentId={}, keyword={}", content.getId(), keyword);
                return DifficultyLevel.EASY;
            }
        }
        
        // 3. 基于文本长度判断
        int textLength = text.length();
        if (textLength < 10) {
            return DifficultyLevel.EASY;
        } else if (textLength > 50) {
            return DifficultyLevel.HARD;
        }
        
        // 4. 默认返回中等难度
        return DifficultyLevel.MEDIUM;
    }

    @Override
    @Transactional
    public void batchUpdatePopularityScores() {
        log.info("开始批量更新内容热度分数");
        
        try {
            // 1. 获取所有已审核通过且启用的内容
            List<WheelContent> contents = wheelContentMapper.selectAllApprovedAndEnabled();
            if (contents == null || contents.isEmpty()) {
                log.info("没有需要更新热度的内容");
                return;
            }

            log.info("需要更新热度的内容数量: {}", contents.size());

            // 2. 批量获取转盘统计
            List<Long> contentIds = contents.stream()
                    .map(WheelContent::getId)
                    .collect(Collectors.toList());
            
            Map<Long, Integer> spinCounts = getSpinCountsMap(contentIds);

            // 3. 计算并更新每个内容的热度分数
            int updatedCount = 0;
            for (WheelContent content : contents) {
                try {
                    double score = calculatePopularityScoreWithCache(content.getId(), spinCounts);
                    
                    // 更新数据库
                    content.setPopularityScore(score);
                    wheelContentMapper.updateById(content);
                    
                    // 清除缓存
                    invalidateFeatureCache(content.getId());
                    
                    updatedCount++;
                } catch (Exception e) {
                    log.error("更新内容热度失败: contentId={}, error={}", content.getId(), e.getMessage());
                }
            }

            log.info("批量更新热度分数完成: 总数={}, 成功={}", contents.size(), updatedCount);
        } catch (Exception e) {
            log.error("批量更新热度分数失败: error={}", e.getMessage(), e);
        }
    }


    @Override
    public ContentFeatureDTO getContentFeature(Long contentId) {
        if (contentId == null) {
            return null;
        }

        // 1. 尝试从缓存获取
        ContentFeatureDTO cached = getFeatureFromCache(contentId);
        if (cached != null) {
            return cached;
        }

        // 2. 从数据库加载内容
        WheelContent content = wheelContentMapper.selectById(contentId);
        if (content == null) {
            log.warn("内容不存在: contentId={}", contentId);
            return null;
        }

        // 3. 构建特征DTO
        ContentFeatureDTO feature = buildFeatureDTO(content);

        // 4. 缓存特征
        cacheFeature(feature);

        return feature;
    }

    @Override
    public List<ContentFeatureDTO> getContentFeatures(List<Long> contentIds) {
        if (contentIds == null || contentIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<ContentFeatureDTO> features = new ArrayList<>();
        List<Long> missingIds = new ArrayList<>();

        // 1. 尝试从缓存批量获取
        for (Long contentId : contentIds) {
            ContentFeatureDTO cached = getFeatureFromCache(contentId);
            if (cached != null) {
                features.add(cached);
            } else {
                missingIds.add(contentId);
            }
        }

        // 2. 从数据库加载缺失的内容
        if (!missingIds.isEmpty()) {
            List<WheelContent> contents = wheelContentMapper.selectBatchIds(missingIds);
            for (WheelContent content : contents) {
                ContentFeatureDTO feature = buildFeatureDTO(content);
                cacheFeature(feature);
                features.add(feature);
            }
        }

        return features;
    }

    @Override
    @Transactional
    public void updateContentFeature(WheelContent content) {
        if (content == null || content.getId() == null) {
            return;
        }

        log.info("更新内容特征: contentId={}", content.getId());

        // 1. 重新提取特征
        extractFeatures(content);

        // 2. 清除旧缓存
        invalidateFeatureCache(content.getId());
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 提取内容标签
     */
    private List<String> extractTags(WheelContent content) {
        List<String> tags = new ArrayList<>();

        // 1. 从现有tags字段解析
        if (StringUtils.hasText(content.getTags())) {
            try {
                List<String> existingTags = objectMapper.readValue(
                        content.getTags(), 
                        new TypeReference<List<String>>() {}
                );
                if (existingTags != null) {
                    tags.addAll(existingTags);
                }
            } catch (JsonProcessingException e) {
                log.warn("解析内容标签失败: contentId={}, tags={}", content.getId(), content.getTags());
            }
        }

        // 2. 从内容文本提取关键词作为标签
        if (StringUtils.hasText(content.getContentText())) {
            List<String> extractedTags = extractKeywordsFromText(content.getContentText());
            for (String tag : extractedTags) {
                if (!tags.contains(tag)) {
                    tags.add(tag);
                }
            }
        }

        return tags;
    }

    /**
     * 从文本中提取关键词
     */
    private List<String> extractKeywordsFromText(String text) {
        List<String> keywords = new ArrayList<>();
        
        // 简单的关键词提取：基于预定义的关键词列表
        Set<String> allKeywords = new HashSet<>();
        allKeywords.addAll(EASY_KEYWORDS);
        allKeywords.addAll(HARD_KEYWORDS);
        allKeywords.addAll(Set.of("浪漫", "温馨", "甜蜜", "有趣", "搞笑", "创意", "惊喜", "感动"));
        
        String lowerText = text.toLowerCase();
        for (String keyword : allKeywords) {
            if (lowerText.contains(keyword)) {
                keywords.add(keyword);
            }
        }
        
        return keywords;
    }

    /**
     * 计算时间衰减因子
     */
    private double calculateTimeDecay(LocalDateTime lastSpinTime) {
        if (lastSpinTime == null) {
            return 0.1; // 从未被转过，给一个很小的基础值
        }

        long daysSinceLastSpin = ChronoUnit.DAYS.between(lastSpinTime, LocalDateTime.now());
        
        // 使用指数衰减: decay = factor ^ days
        // 7天后衰减到约0.48，14天后约0.23，30天后约0.04
        return Math.pow(TIME_DECAY_FACTOR, daysSinceLastSpin);
    }

    /**
     * 生成特征向量
     * 特征向量包含：[热度分数, 难度等级归一化, 标签数量归一化, 文本长度归一化]
     */
    private double[] generateFeatureVector(WheelContent content, List<String> tags, 
                                           double popularityScore, DifficultyLevel difficultyLevel) {
        double[] vector = new double[4];
        
        // 特征1: 热度分数 (已归一化到0-1)
        vector[0] = popularityScore;
        
        // 特征2: 难度等级归一化 (1-3 -> 0-1)
        vector[1] = (difficultyLevel.getCode() - 1) / 2.0;
        
        // 特征3: 标签数量归一化 (假设最大10个标签)
        vector[2] = Math.min(1.0, tags.size() / 10.0);
        
        // 特征4: 文本长度归一化 (假设最大200字符)
        int textLength = content.getContentText() != null ? content.getContentText().length() : 0;
        vector[3] = Math.min(1.0, textLength / 200.0);
        
        return vector;
    }

    /**
     * 更新数据库中的内容特征
     */
    private void updateContentFeatureInDb(Long contentId, double popularityScore, 
                                          DifficultyLevel difficultyLevel, double[] featureVector) {
        try {
            String featureVectorJson = objectMapper.writeValueAsString(
                    Arrays.stream(featureVector).boxed().collect(Collectors.toList())
            );
            wheelContentMapper.updateContentFeatures(
                    contentId, 
                    popularityScore, 
                    difficultyLevel.getCode(), 
                    featureVectorJson
            );
        } catch (JsonProcessingException e) {
            log.error("序列化特征向量失败: contentId={}, error={}", contentId, e.getMessage());
        }
    }

    /**
     * 构建特征DTO
     */
    private ContentFeatureDTO buildFeatureDTO(WheelContent content) {
        ContentFeatureDTO feature = new ContentFeatureDTO();
        feature.setContentId(content.getId());
        
        // 解析标签
        feature.setTags(extractTags(content));
        
        // 热度分数
        feature.setPopularityScore(content.getPopularityScore() != null ? content.getPopularityScore() : 0.0);
        
        // 难度等级
        int difficultyCode = content.getDifficultyLevel() != null ? content.getDifficultyLevel() : 2;
        DifficultyLevel level = DifficultyLevel.fromCode(difficultyCode);
        feature.setDifficultyLevel(level.getCode());
        feature.setDifficultyDesc(level.getDescription());
        
        // 特征向量
        if (content.getFeatureVector() != null && !content.getFeatureVector().isEmpty()) {
            feature.setFeatureVector(content.getFeatureVector().stream()
                    .mapToDouble(Double::doubleValue)
                    .toArray());
        }
        
        return feature;
    }


    /**
     * 从缓存获取特征
     */
    private ContentFeatureDTO getFeatureFromCache(Long contentId) {
        try {
            String cacheKey = FEATURE_CACHE_PREFIX + contentId;
            Object cached = redisTemplate.opsForValue().get(cacheKey);
            if (cached != null) {
                return objectMapper.convertValue(cached, ContentFeatureDTO.class);
            }
        } catch (Exception e) {
            log.warn("从缓存获取内容特征失败: contentId={}, error={}", contentId, e.getMessage());
        }
        return null;
    }

    /**
     * 缓存特征
     */
    private void cacheFeature(ContentFeatureDTO feature) {
        if (feature == null || feature.getContentId() == null) {
            return;
        }
        try {
            String cacheKey = FEATURE_CACHE_PREFIX + feature.getContentId();
            redisTemplate.opsForValue().set(cacheKey, feature, FEATURE_CACHE_TTL, TimeUnit.SECONDS);
            log.debug("缓存内容特征: contentId={}", feature.getContentId());
        } catch (Exception e) {
            log.warn("缓存内容特征失败: contentId={}, error={}", feature.getContentId(), e.getMessage());
        }
    }

    /**
     * 清除特征缓存
     */
    private void invalidateFeatureCache(Long contentId) {
        try {
            String cacheKey = FEATURE_CACHE_PREFIX + contentId;
            redisTemplate.delete(cacheKey);
            log.debug("清除内容特征缓存: contentId={}", contentId);
        } catch (Exception e) {
            log.warn("清除内容特征缓存失败: contentId={}, error={}", contentId, e.getMessage());
        }
    }

    /**
     * 获取转盘次数映射
     */
    private Map<Long, Integer> getSpinCountsMap(List<Long> contentIds) {
        Map<Long, Integer> spinCounts = new HashMap<>();
        
        try {
            List<Map<String, Object>> results = wheelSpinRecordMapper.batchCountByContentIds(contentIds);
            if (results != null) {
                for (Map<String, Object> result : results) {
                    Long contentId = ((Number) result.get("contentId")).longValue();
                    Integer count = ((Number) result.get("spinCount")).intValue();
                    spinCounts.put(contentId, count);
                }
            }
        } catch (Exception e) {
            log.error("批量获取转盘次数失败: error={}", e.getMessage());
        }
        
        // 为没有记录的内容设置默认值0
        for (Long contentId : contentIds) {
            spinCounts.putIfAbsent(contentId, 0);
        }
        
        return spinCounts;
    }

    /**
     * 使用缓存的转盘次数计算热度分数
     */
    private double calculatePopularityScoreWithCache(Long contentId, Map<Long, Integer> spinCounts) {
        int totalSpins = spinCounts.getOrDefault(contentId, 0);
        
        // 获取最近一次转盘时间
        LocalDateTime lastSpinTime = wheelSpinRecordMapper.getLastSpinTimeByContentId(contentId);
        
        // 计算时间衰减
        double timeDecay = calculateTimeDecay(lastSpinTime);
        
        // 简化计算（因为批量更新时不需要区分最近7天）
        double rawScore = totalSpins * timeDecay;
        
        // 归一化到0-1范围
        return Math.min(1.0, rawScore / 100.0);
    }
}
