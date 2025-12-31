package com.basebackend.wheel.service.impl;

import com.basebackend.wheel.dto.MatchedWord;
import com.basebackend.wheel.dto.SensitiveWordResult;
import com.basebackend.wheel.entity.SensitiveWord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * DFA敏感词过滤器单元测试
 *
 * @author wheel-api
 * @since 2025-02-01
 */
@DisplayName("DFA敏感词过滤器测试")
class DfaSensitiveWordFilterTest {

    private DfaSensitiveWordFilter filter;

    @BeforeEach
    void setUp() {
        filter = new DfaSensitiveWordFilter();
    }

    private SensitiveWord createSensitiveWord(String word, int category, int level) {
        SensitiveWord sw = new SensitiveWord();
        sw.setId((long) word.hashCode());
        sw.setWord(word);
        sw.setCategory(category);
        sw.setLevel(level);
        sw.setStatus(SensitiveWord.STATUS_ENABLED);
        return sw;
    }

    @Test
    @DisplayName("初始化后应正确设置状态")
    void testInitialize() {
        List<SensitiveWord> words = Arrays.asList(
            createSensitiveWord("敏感词", SensitiveWord.CATEGORY_OTHER, SensitiveWord.LEVEL_MEDIUM),
            createSensitiveWord("违禁词", SensitiveWord.CATEGORY_POLITICS, SensitiveWord.LEVEL_HIGH)
        );

        filter.initialize(words);

        assertTrue(filter.isInitialized());
        assertEquals(2, filter.getWordCount());
    }

    @Test
    @DisplayName("检测包含敏感词的文本")
    void testDetectWithSensitiveWord() {
        List<SensitiveWord> words = Arrays.asList(
            createSensitiveWord("敏感词", SensitiveWord.CATEGORY_OTHER, SensitiveWord.LEVEL_MEDIUM)
        );
        filter.initialize(words);

        SensitiveWordResult result = filter.detect("这是一段包含敏感词的文本");

        assertTrue(result.isHasSensitiveWord());
        assertEquals(1, result.getMatchedWords().size());
        assertEquals("敏感词", result.getMatchedWords().get(0).getWord());
    }

    @Test
    @DisplayName("检测不包含敏感词的文本")
    void testDetectWithoutSensitiveWord() {
        List<SensitiveWord> words = Arrays.asList(
            createSensitiveWord("敏感词", SensitiveWord.CATEGORY_OTHER, SensitiveWord.LEVEL_MEDIUM)
        );
        filter.initialize(words);

        SensitiveWordResult result = filter.detect("这是一段正常的文本");

        assertFalse(result.isHasSensitiveWord());
        assertTrue(result.getMatchedWords().isEmpty());
    }


    @Test
    @DisplayName("检测多个敏感词")
    void testDetectMultipleSensitiveWords() {
        List<SensitiveWord> words = Arrays.asList(
            createSensitiveWord("敏感词", SensitiveWord.CATEGORY_OTHER, SensitiveWord.LEVEL_MEDIUM),
            createSensitiveWord("违禁词", SensitiveWord.CATEGORY_POLITICS, SensitiveWord.LEVEL_HIGH)
        );
        filter.initialize(words);

        SensitiveWordResult result = filter.detect("这段文本包含敏感词和违禁词");

        assertTrue(result.isHasSensitiveWord());
        assertEquals(2, result.getMatchedWords().size());
    }

    @Test
    @DisplayName("检测敏感词位置正确性")
    void testDetectSensitiveWordPosition() {
        List<SensitiveWord> words = Arrays.asList(
            createSensitiveWord("敏感词", SensitiveWord.CATEGORY_OTHER, SensitiveWord.LEVEL_MEDIUM)
        );
        filter.initialize(words);

        String text = "前缀敏感词后缀";
        SensitiveWordResult result = filter.detect(text);

        assertTrue(result.isHasSensitiveWord());
        MatchedWord matched = result.getMatchedWords().get(0);
        assertEquals(2, matched.getStartIndex());
        assertEquals(5, matched.getEndIndex());
        assertEquals("敏感词", text.substring(matched.getStartIndex(), matched.getEndIndex()));
    }

    @Test
    @DisplayName("检测敏感词分类正确性")
    void testDetectSensitiveWordCategory() {
        List<SensitiveWord> words = Arrays.asList(
            createSensitiveWord("色情词", SensitiveWord.CATEGORY_PORN, SensitiveWord.LEVEL_HIGH),
            createSensitiveWord("暴力词", SensitiveWord.CATEGORY_VIOLENCE, SensitiveWord.LEVEL_MEDIUM)
        );
        filter.initialize(words);

        SensitiveWordResult result = filter.detect("包含色情词和暴力词");

        assertTrue(result.isHasSensitiveWord());
        assertEquals(2, result.getMatchedWords().size());
        
        // 验证分类
        boolean hasPorn = result.getMatchedWords().stream()
            .anyMatch(w -> w.getCategory() == SensitiveWord.CATEGORY_PORN);
        boolean hasViolence = result.getMatchedWords().stream()
            .anyMatch(w -> w.getCategory() == SensitiveWord.CATEGORY_VIOLENCE);
        
        assertTrue(hasPorn);
        assertTrue(hasViolence);
    }

    @Test
    @DisplayName("替换敏感词")
    void testReplaceSensitiveWord() {
        List<SensitiveWord> words = Arrays.asList(
            createSensitiveWord("敏感词", SensitiveWord.CATEGORY_OTHER, SensitiveWord.LEVEL_MEDIUM)
        );
        filter.initialize(words);

        String result = filter.replace("这是一段包含敏感词的文本", '*');

        assertEquals("这是一段包含***的文本", result);
    }

    @Test
    @DisplayName("替换多个敏感词")
    void testReplaceMultipleSensitiveWords() {
        List<SensitiveWord> words = Arrays.asList(
            createSensitiveWord("敏感词", SensitiveWord.CATEGORY_OTHER, SensitiveWord.LEVEL_MEDIUM),
            createSensitiveWord("违禁词", SensitiveWord.CATEGORY_POLITICS, SensitiveWord.LEVEL_HIGH)
        );
        filter.initialize(words);

        String result = filter.replace("这段文本包含敏感词和违禁词", '*');

        assertEquals("这段文本包含***和***", result);
    }

    @Test
    @DisplayName("containsSensitiveWord方法测试")
    void testContainsSensitiveWord() {
        List<SensitiveWord> words = Arrays.asList(
            createSensitiveWord("敏感词", SensitiveWord.CATEGORY_OTHER, SensitiveWord.LEVEL_MEDIUM)
        );
        filter.initialize(words);

        assertTrue(filter.containsSensitiveWord("包含敏感词"));
        assertFalse(filter.containsSensitiveWord("正常文本"));
    }

    @Test
    @DisplayName("热更新敏感词库")
    void testReload() {
        // 初始化
        List<SensitiveWord> words1 = Arrays.asList(
            createSensitiveWord("敏感词", SensitiveWord.CATEGORY_OTHER, SensitiveWord.LEVEL_MEDIUM)
        );
        filter.initialize(words1);
        assertEquals(1, filter.getWordCount());

        // 热更新
        List<SensitiveWord> words2 = Arrays.asList(
            createSensitiveWord("新敏感词", SensitiveWord.CATEGORY_OTHER, SensitiveWord.LEVEL_MEDIUM),
            createSensitiveWord("另一个词", SensitiveWord.CATEGORY_POLITICS, SensitiveWord.LEVEL_HIGH)
        );
        filter.reload(words2);

        assertEquals(2, filter.getWordCount());
        assertTrue(filter.containsSensitiveWord("包含新敏感词"));
        assertFalse(filter.containsSensitiveWord("包含敏感词")); // 旧词应该不存在了
    }

    @Test
    @DisplayName("空文本检测")
    void testDetectEmptyText() {
        List<SensitiveWord> words = Arrays.asList(
            createSensitiveWord("敏感词", SensitiveWord.CATEGORY_OTHER, SensitiveWord.LEVEL_MEDIUM)
        );
        filter.initialize(words);

        SensitiveWordResult result1 = filter.detect("");
        SensitiveWordResult result2 = filter.detect(null);

        assertFalse(result1.isHasSensitiveWord());
        assertFalse(result2.isHasSensitiveWord());
    }

    @Test
    @DisplayName("空敏感词库初始化")
    void testInitializeWithEmptyList() {
        filter.initialize(new ArrayList<>());

        assertTrue(filter.isInitialized());
        assertEquals(0, filter.getWordCount());
        assertFalse(filter.containsSensitiveWord("任何文本"));
    }

    @Test
    @DisplayName("大小写不敏感检测")
    void testCaseInsensitiveDetection() {
        List<SensitiveWord> words = Arrays.asList(
            createSensitiveWord("BadWord", SensitiveWord.CATEGORY_OTHER, SensitiveWord.LEVEL_MEDIUM)
        );
        filter.initialize(words);

        assertTrue(filter.containsSensitiveWord("contains badword here"));
        assertTrue(filter.containsSensitiveWord("contains BADWORD here"));
        assertTrue(filter.containsSensitiveWord("contains BadWord here"));
    }

    @Test
    @DisplayName("符号分隔变体检测")
    void testSymbolSeparatedVariantDetection() {
        SensitiveWord sw = createSensitiveWord("敏感词", SensitiveWord.CATEGORY_OTHER, SensitiveWord.LEVEL_MEDIUM);
        sw.setVariants(Arrays.asList("敏*感*词", "敏.感.词"));
        
        List<SensitiveWord> words = Arrays.asList(sw);
        filter.initialize(words);

        // 原词应该能检测到
        assertTrue(filter.containsSensitiveWord("包含敏感词"));
        // 变体词也应该能检测到
        assertTrue(filter.containsSensitiveWord("包含敏*感*词"));
    }

    @Test
    @DisplayName("检测性能测试 - 50ms内完成")
    void testDetectionPerformance() {
        // 构建较大的敏感词库
        List<SensitiveWord> words = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            words.add(createSensitiveWord("敏感词" + i, SensitiveWord.CATEGORY_OTHER, SensitiveWord.LEVEL_MEDIUM));
        }
        filter.initialize(words);

        // 构建较长的测试文本
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 100; i++) {
            sb.append("这是一段正常的测试文本，用于测试敏感词检测性能。");
        }
        String text = sb.toString();

        // 执行检测并验证时间
        SensitiveWordResult result = filter.detect(text);

        assertTrue(result.getDetectTimeMs() < 50, 
            "检测时间应小于50ms，实际: " + result.getDetectTimeMs() + "ms");
    }

    @Test
    @DisplayName("风险评分计算")
    void testRiskScoreCalculation() {
        List<SensitiveWord> words = Arrays.asList(
            createSensitiveWord("低风险词", SensitiveWord.CATEGORY_OTHER, SensitiveWord.LEVEL_LOW),
            createSensitiveWord("高风险词", SensitiveWord.CATEGORY_POLITICS, SensitiveWord.LEVEL_HIGH)
        );
        filter.initialize(words);

        SensitiveWordResult result = filter.detect("包含低风险词和高风险词");

        assertTrue(result.isHasSensitiveWord());
        assertTrue(result.getRiskScore() > 0);
        // 低风险10分 + 高风险50分 = 60分
        assertEquals(60.0, result.getRiskScore(), 0.01);
    }
}
