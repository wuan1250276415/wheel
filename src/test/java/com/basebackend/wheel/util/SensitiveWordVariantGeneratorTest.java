package com.basebackend.wheel.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 敏感词变体生成器单元测试
 *
 * @author wheel-api
 * @since 2025-02-01
 */
@DisplayName("敏感词变体生成器测试")
class SensitiveWordVariantGeneratorTest {

    @Test
    @DisplayName("生成符号分隔变体")
    void testGenerateSymbolSeparatedVariants() {
        List<String> variants = SensitiveWordVariantGenerator.generateVariants("敏感词");

        assertNotNull(variants);
        assertFalse(variants.isEmpty());
        
        // 应该包含符号分隔变体
        assertTrue(variants.stream().anyMatch(v -> v.contains("*")));
        assertTrue(variants.stream().anyMatch(v -> v.contains(".")));
        assertTrue(variants.stream().anyMatch(v -> v.contains("_")));
    }

    @Test
    @DisplayName("生成大小写变体")
    void testGenerateCaseVariants() {
        List<String> variants = SensitiveWordVariantGenerator.generateVariants("BadWord");

        assertNotNull(variants);
        assertTrue(variants.contains("badword"));
        assertTrue(variants.contains("BADWORD"));
    }

    @Test
    @DisplayName("空输入处理")
    void testEmptyInput() {
        List<String> variants1 = SensitiveWordVariantGenerator.generateVariants("");
        List<String> variants2 = SensitiveWordVariantGenerator.generateVariants(null);

        assertTrue(variants1.isEmpty());
        assertTrue(variants2.isEmpty());
    }

    @Test
    @DisplayName("文本标准化")
    void testNormalizeText() {
        // 测试全角转半角
        String normalized = SensitiveWordVariantGenerator.normalizeText("ＡＢＣ");
        assertEquals("abc", normalized);

        // 测试大写转小写
        normalized = SensitiveWordVariantGenerator.normalizeText("ABC");
        assertEquals("abc", normalized);

        // 测试移除干扰字符
        normalized = SensitiveWordVariantGenerator.normalizeText("敏*感*词");
        assertEquals("敏感词", normalized);
    }

    @Test
    @DisplayName("检测变体包含")
    void testContainsVariant() {
        assertTrue(SensitiveWordVariantGenerator.containsVariant("包含敏*感*词", "敏感词"));
        assertTrue(SensitiveWordVariantGenerator.containsVariant("包含敏.感.词", "敏感词"));
        assertTrue(SensitiveWordVariantGenerator.containsVariant("包含BADWORD", "badword"));
        assertFalse(SensitiveWordVariantGenerator.containsVariant("正常文本", "敏感词"));
    }

    @Test
    @DisplayName("空输入检测变体")
    void testContainsVariantWithNullInput() {
        assertFalse(SensitiveWordVariantGenerator.containsVariant(null, "敏感词"));
        assertFalse(SensitiveWordVariantGenerator.containsVariant("文本", null));
        assertFalse(SensitiveWordVariantGenerator.containsVariant(null, null));
    }

    @Test
    @DisplayName("生成谐音变体")
    void testGenerateHomophoneVariants() {
        // 测试包含谐音映射的词
        List<String> variants = SensitiveWordVariantGenerator.generateVariants("傻");

        assertNotNull(variants);
        // 应该包含谐音变体
        assertTrue(variants.stream().anyMatch(v -> v.contains("沙") || v.contains("煞") || v.contains("杀")));
    }

    @Test
    @DisplayName("生成数字替换变体")
    void testGenerateNumberVariants() {
        List<String> variants = SensitiveWordVariantGenerator.generateVariants("test");

        assertNotNull(variants);
        // 应该包含数字替换变体 (t->7, e->3, s->5)
        assertTrue(variants.stream().anyMatch(v -> v.contains("7") || v.contains("3") || v.contains("5")));
    }
}
