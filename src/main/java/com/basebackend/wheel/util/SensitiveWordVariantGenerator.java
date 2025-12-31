package com.basebackend.wheel.util;

import lombok.extern.slf4j.Slf4j;

import java.util.*;

/**
 * 敏感词变体生成器
 * 用于生成敏感词的各种变体形式，包括：
 * 1. 拼音变体
 * 2. 谐音变体
 * 3. 符号分隔变体
 * 4. 数字替换变体
 *
 * @author wheel-api
 * @since 2025-02-01
 */
@Slf4j
public class SensitiveWordVariantGenerator {

    /**
     * 常见汉字到拼音的映射（简化版，实际项目可使用pinyin4j等库）
     */
    private static final Map<Character, String> PINYIN_MAP = new HashMap<>();

    /**
     * 谐音字映射（常见的谐音替换）
     */
    private static final Map<Character, List<Character>> HOMOPHONE_MAP = new HashMap<>();

    /**
     * 数字形似字母映射
     */
    private static final Map<Character, List<Character>> NUMBER_LETTER_MAP = new HashMap<>();

    static {
        // 初始化数字-字母映射
        NUMBER_LETTER_MAP.put('0', Arrays.asList('o', 'O'));
        NUMBER_LETTER_MAP.put('1', Arrays.asList('l', 'i', 'I', 'L'));
        NUMBER_LETTER_MAP.put('2', Arrays.asList('z', 'Z'));
        NUMBER_LETTER_MAP.put('3', Arrays.asList('e', 'E'));
        NUMBER_LETTER_MAP.put('4', Arrays.asList('a', 'A'));
        NUMBER_LETTER_MAP.put('5', Arrays.asList('s', 'S'));
        NUMBER_LETTER_MAP.put('6', Arrays.asList('b', 'G'));
        NUMBER_LETTER_MAP.put('7', Arrays.asList('t', 'T'));
        NUMBER_LETTER_MAP.put('8', Arrays.asList('b', 'B'));
        NUMBER_LETTER_MAP.put('9', Arrays.asList('g', 'q'));

        // 初始化常见谐音字映射
        initHomophoneMap();
    }

    private static void initHomophoneMap() {
        // 常见谐音替换（示例，实际应更完整）
        HOMOPHONE_MAP.put('操', Arrays.asList('草', '艹', '肏'));
        HOMOPHONE_MAP.put('日', Arrays.asList('曰', '入'));
        HOMOPHONE_MAP.put('死', Arrays.asList('屎', '4', '四'));
        HOMOPHONE_MAP.put('妈', Arrays.asList('马', '吗', '骂'));
        HOMOPHONE_MAP.put('逼', Arrays.asList('比', 'B', 'b', '币'));
        HOMOPHONE_MAP.put('傻', Arrays.asList('沙', '煞', '杀'));
        HOMOPHONE_MAP.put('蛋', Arrays.asList('淡', '弹'));
        HOMOPHONE_MAP.put('滚', Arrays.asList('棍'));
        HOMOPHONE_MAP.put('屁', Arrays.asList('P', 'p', '批'));
        HOMOPHONE_MAP.put('鸡', Arrays.asList('机', '基', 'J', 'j'));
        HOMOPHONE_MAP.put('色', Arrays.asList('涩'));
        HOMOPHONE_MAP.put('情', Arrays.asList('青', '晴'));
        HOMOPHONE_MAP.put('黄', Arrays.asList('煌', '皇'));
        HOMOPHONE_MAP.put('赌', Arrays.asList('堵', '杜'));
        HOMOPHONE_MAP.put('博', Arrays.asList('搏', '伯'));
        HOMOPHONE_MAP.put('毒', Arrays.asList('独', '读', '度'));
        HOMOPHONE_MAP.put('枪', Arrays.asList('抢', '腔'));
        HOMOPHONE_MAP.put('杀', Arrays.asList('沙', '煞', '刹'));
    }

    /**
     * 生成敏感词的所有变体
     *
     * @param word 原始敏感词
     * @return 变体列表
     */
    public static List<String> generateVariants(String word) {
        if (word == null || word.isEmpty()) {
            return Collections.emptyList();
        }

        Set<String> variants = new LinkedHashSet<>();
        
        // 1. 添加原词的大小写变体
        variants.add(word.toLowerCase());
        variants.add(word.toUpperCase());
        
        // 2. 生成符号分隔变体
        variants.addAll(generateSymbolSeparatedVariants(word));
        
        // 3. 生成谐音变体
        variants.addAll(generateHomophoneVariants(word));
        
        // 4. 生成数字替换变体
        variants.addAll(generateNumberVariants(word));
        
        // 移除原词
        variants.remove(word);
        
        return new ArrayList<>(variants);
    }

    /**
     * 生成符号分隔变体
     * 例如：敏感词 -> 敏*感*词, 敏.感.词
     */
    private static List<String> generateSymbolSeparatedVariants(String word) {
        List<String> variants = new ArrayList<>();
        char[] separators = {'*', '.', '_', '-', ' ', '·'};
        
        for (char sep : separators) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < word.length(); i++) {
                sb.append(word.charAt(i));
                if (i < word.length() - 1) {
                    sb.append(sep);
                }
            }
            variants.add(sb.toString());
        }
        
        return variants;
    }

    /**
     * 生成谐音变体
     */
    private static List<String> generateHomophoneVariants(String word) {
        List<String> variants = new ArrayList<>();
        
        // 对每个字符尝试谐音替换
        for (int i = 0; i < word.length(); i++) {
            char c = word.charAt(i);
            List<Character> homophones = HOMOPHONE_MAP.get(c);
            
            if (homophones != null && !homophones.isEmpty()) {
                for (Character homophone : homophones) {
                    String variant = word.substring(0, i) + homophone + word.substring(i + 1);
                    variants.add(variant);
                }
            }
        }
        
        return variants;
    }

    /**
     * 生成数字替换变体（针对英文敏感词）
     */
    private static List<String> generateNumberVariants(String word) {
        List<String> variants = new ArrayList<>();
        
        // 字母替换为形似数字
        for (int i = 0; i < word.length(); i++) {
            char c = Character.toLowerCase(word.charAt(i));
            
            for (Map.Entry<Character, List<Character>> entry : NUMBER_LETTER_MAP.entrySet()) {
                if (entry.getValue().contains(c)) {
                    String variant = word.substring(0, i) + entry.getKey() + word.substring(i + 1);
                    variants.add(variant);
                }
            }
        }
        
        return variants;
    }

    /**
     * 标准化文本（用于检测前的预处理）
     * 移除常见的干扰字符，统一大小写
     *
     * @param text 原始文本
     * @return 标准化后的文本
     */
    public static String normalizeText(String text) {
        if (text == null) {
            return "";
        }
        
        StringBuilder sb = new StringBuilder();
        for (char c : text.toCharArray()) {
            // 跳过常见干扰字符
            if (isNoiseChar(c)) {
                continue;
            }
            
            // 全角转半角
            if (c >= '！' && c <= '～') {
                c = (char) (c - 65248);
            }
            
            // 转小写
            if (c >= 'A' && c <= 'Z') {
                c = (char) (c + 32);
            }
            
            sb.append(c);
        }
        
        return sb.toString();
    }

    /**
     * 判断是否为干扰字符
     */
    private static boolean isNoiseChar(char c) {
        return c == '*' || c == '_' || c == '-' || c == '.' || 
               c == ' ' || c == '\t' || c == '\n' || c == '\r' ||
               c == '·' || c == '•' || c == '@' || c == '#' ||
               c == '$' || c == '%' || c == '^' || c == '&' ||
               c == '!' || c == '~' || c == '`' || c == '|';
    }

    /**
     * 检测文本中是否包含敏感词的变体形式
     *
     * @param text 待检测文本
     * @param sensitiveWord 敏感词
     * @return 是否包含
     */
    public static boolean containsVariant(String text, String sensitiveWord) {
        if (text == null || sensitiveWord == null) {
            return false;
        }
        
        String normalizedText = normalizeText(text);
        String normalizedWord = normalizeText(sensitiveWord);
        
        return normalizedText.contains(normalizedWord);
    }
}
