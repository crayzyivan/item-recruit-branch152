package com.item.util;

import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 提取关键字
 *
 * @author liyunlong
 * @version 1.0
 * @since 2025-10-24  09:50
 */
public class ExtractKeyWordUtils {

    /**
     * 提取技能中的名词关键词和普通关键词
     * 结合专业名词识别、通用关键词提取和TF-IDF算法
     */
    public static List<String> extractNounKeywords(List<String> skills,int size) {
        List<String> allKeywords = new ArrayList<>();

        // 常见的技术名词和技能关键词
        Set<String> techNouns = Set.of(
                "java", "python", "javascript", "typescript", "react", "angular", "vue", "nodejs", "spring", "hibernate",
                "mysql", "postgresql", "mongodb", "redis", "elasticsearch", "docker", "kubernetes", "aws", "azure", "gcp",
                "machine", "learning", "artificial", "intelligence", "data", "science", "analytics", "blockchain", "devops",
                "frontend", "backend", "fullstack", "mobile", "ios", "android", "swift", "kotlin", "flutter", "reactnative",
                "ui", "ux", "design", "testing", "qa", "automation", "api", "rest", "graphql", "microservices", "agile", "scrum",
                "sql", "nosql", "html", "css", "bootstrap", "jquery", "webpack", "babel", "git", "svn", "jenkins", "ci", "cd",
                "tensorflow", "pytorch", "pandas", "numpy", "scikit", "keras", "opencv", "nlp", "computer", "vision",
                "security", "encryption", "ssl", "tls", "oauth", "jwt", "firewall", "penetration", "vulnerability"
        );

        // 常见的职业名词
        Set<String> jobNouns = Set.of(
                "developer", "engineer", "programmer", "architect", "analyst", "manager", "director", "lead", "senior", "junior",
                "consultant", "specialist", "expert", "coordinator", "supervisor", "administrator", "technician", "designer",
                "advisor", "researcher", "scientist", "trainer", "instructor", "mentor", "coach", "facilitator"
        );

        // 常见的业务领域名词
        Set<String> businessNouns = Set.of(
                "finance", "banking", "insurance", "healthcare", "education", "retail", "ecommerce", "logistics", "manufacturing",
                "telecommunications", "media", "entertainment", "gaming", "sports", "travel", "hospitality", "real", "estate",
                "consulting", "advertising", "marketing", "sales", "customer", "service", "support", "operations", "strategy"
        );

        // 停用词（需要过滤的词）
        Set<String> stopWords = Set.of(
                "the", "a", "an", "and", "or", "but", "in", "on", "at", "to", "for", "of", "with", "by", "from", "up", "about",
                "is", "are", "was", "were", "be", "been", "being", "have", "has", "had", "do", "does", "did", "will", "would",
                "could", "should", "may", "might", "can", "must", "shall", "very", "much", "more", "most", "less", "least",
                "good", "bad", "new", "old", "big", "small", "high", "low", "long", "short", "first", "last", "next", "previous"
        );

        // 1. 传统方法提取关键词
        List<String> traditionalKeywords = extractTraditionalKeywords(skills, techNouns, jobNouns, businessNouns, stopWords,size);
        allKeywords.addAll(traditionalKeywords);
        if (allKeywords.size()>=size){
            return allKeywords;
        }

        // 2. TF-IDF方法提取关键词
        List<String> tfidfKeywords = extractKeywordsByTFIDF(skills, stopWords,size-traditionalKeywords.size());
        allKeywords.addAll(tfidfKeywords);

        // 去重并返回
        return allKeywords.stream()
                .distinct()
                .filter(keyword -> keyword.length() >= 2)
                .collect(Collectors.toList());
    }

    /**
     * 传统方法提取关键词
     */
    private static List<String> extractTraditionalKeywords(List<String> skills, Set<String> techNouns,
                                                    Set<String> jobNouns, Set<String> businessNouns, Set<String> stopWords,int size) {
        List<String> keywords = new ArrayList<>();

        for (String skill : skills) {
            if (StringUtils.hasText(skill)) {
                // 预处理：转换为小写，处理特殊字符
                String processedSkill = skill.toLowerCase()
                        .replaceAll("[^a-zA-Z0-9\\s\\-]", " ") // 保留字母、数字、空格和连字符
                        .replaceAll("\\s+", " ") // 合并多个空格
                        .trim();

                // 按空格分割
                String[] words = processedSkill.split("\\s+");

                for (String word : words) {
                    word = word.trim();

                    // 过滤条件
                    if (word.length() < 2 || stopWords.contains(word)) {
                        continue;
                    }

                    // 1. 专业名词识别（优先级最高）
                    if (techNouns.contains(word) || jobNouns.contains(word) || businessNouns.contains(word)) {
                        keywords.add(word);
                        if (keywords.size()>=size){
                            return  keywords;
                        }
                        continue;
                    }

                    // 2. 名词后缀识别
                    if (isNounSuffix(word)) {
                        keywords.add(word);
                        if (keywords.size()>=size){
                            return  keywords;
                        }
                        continue;
                    }

                    // 3. 复合词识别（如 "machine-learning", "data-science"）
                    if (word.contains("-")) {
                        String[] compoundWords = word.split("-");
                        for (String compoundWord : compoundWords) {
                            if (compoundWord.length() >= 2 && !stopWords.contains(compoundWord)) {
                                keywords.add(compoundWord);
                                if (keywords.size()>=size){
                                    return  keywords;
                                }
                            }
                        }
                        continue;
                    }

                    // 4. 普通关键词提取（长度适中，非停用词）
                    if (word.length() >= 3 && word.length() <= 15 && !isStopWord(word)) {
                        keywords.add(word);
                        if (keywords.size()>=size){
                            return  keywords;
                        }
                    }
                }
            }
        }

        return keywords;
    }

    /**
     * 基于TF-IDF的关键词提取
     */
    private static List<String> extractKeywordsByTFIDF(List<String> skills, Set<String> stopWords,int residueSize) {
        if (skills == null || skills.isEmpty()) {
            return new ArrayList<>();
        }

        // 1. 预处理所有技能文本
        List<String> processedSkills = skills.stream()
                .filter(StringUtils::hasText)
                .map(skill -> skill.toLowerCase()
                        .replaceAll("[^a-zA-Z0-9\\s\\-]", " ")
                        .replaceAll("\\s+", " ")
                        .trim())
                .collect(Collectors.toList());

        // 2. 构建词汇表
        Map<String, Integer> vocabulary = new HashMap<>();
        Map<String, Integer> documentFrequency = new HashMap<>();

        for (String skill : processedSkills) {
            String[] words = skill.split("\\s+");
            Set<String> uniqueWords = new HashSet<>();

            for (String word : words) {
                word = word.trim();
                if (word.length() >= 2 && !stopWords.contains(word)) {
                    // 计算词频
                    vocabulary.put(word, vocabulary.getOrDefault(word, 0) + 1);
                    uniqueWords.add(word);
                }
            }

            // 计算文档频率
            for (String word : uniqueWords) {
                documentFrequency.put(word, documentFrequency.getOrDefault(word, 0) + 1);
            }
        }

        // 3. 计算TF-IDF分数
        Map<String, Double> tfidfScores = new HashMap<>();
        int totalDocuments = processedSkills.size();

        for (Map.Entry<String, Integer> entry : vocabulary.entrySet()) {
            String word = entry.getKey();
            int termFrequency = entry.getValue();
            int docFrequency = documentFrequency.getOrDefault(word, 1);

            // TF (Term Frequency) = 词频 / 总词数
            double tf = (double) termFrequency / vocabulary.values().stream().mapToInt(Integer::intValue).sum();

            // IDF (Inverse Document Frequency) = log(总文档数 / 包含该词的文档数)
            double idf = Math.log((double) totalDocuments / docFrequency);

            // TF-IDF = TF * IDF
            double tfidf = tf * idf;
            tfidfScores.put(word, tfidf);
        }

        // 4. 按TF-IDF分数排序并选择top关键词
        return tfidfScores.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(Math.min(residueSize, tfidfScores.size())) // 取前20个关键词
                .filter(entry -> entry.getValue() > 0.01) // 过滤掉分数太低的关键词
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    /**
     * 计算TF-IDF分数的辅助方法
     */
    private static Map<String, Double> calculateTFIDF(List<String> documents, Set<String> stopWords) {
        Map<String, Integer> vocabulary = new HashMap<>();
        Map<String, Integer> documentFrequency = new HashMap<>();

        // 统计词频和文档频率
        for (String document : documents) {
            String[] words = document.toLowerCase()
                    .replaceAll("[^a-zA-Z0-9\\s]", " ")
                    .split("\\s+");

            Set<String> uniqueWords = new HashSet<>();
            for (String word : words) {
                word = word.trim();
                if (word.length() >= 2 && !stopWords.contains(word)) {
                    vocabulary.put(word, vocabulary.getOrDefault(word, 0) + 1);
                    uniqueWords.add(word);
                }
            }

            for (String word : uniqueWords) {
                documentFrequency.put(word, documentFrequency.getOrDefault(word, 0) + 1);
            }
        }

        // 计算TF-IDF分数
        Map<String, Double> tfidfScores = new HashMap<>();
        int totalDocuments = documents.size();
        int totalWords = vocabulary.values().stream().mapToInt(Integer::intValue).sum();

        for (Map.Entry<String, Integer> entry : vocabulary.entrySet()) {
            String word = entry.getKey();
            int termFrequency = entry.getValue();
            int docFrequency = documentFrequency.getOrDefault(word, 1);

            double tf = (double) termFrequency / totalWords;
            double idf = Math.log((double) totalDocuments / docFrequency);
            double tfidf = tf * idf;

            tfidfScores.put(word, tfidf);
        }

        return tfidfScores;
    }

    /**
     * 判断是否为名词后缀
     */
    private static boolean isNounSuffix(String word) {
        String[] nounSuffixes = {
                "ing", "tion", "sion", "ment", "ity", "ness", "ism", "ist", "er", "or", "ar", "ant", "ent",
                "ance", "ence", "age", "ade", "ure", "ture", "sure", "ry", "ty", "cy", "sy", "my", "ny"
        };

        for (String suffix : nounSuffixes) {
            if (word.endsWith(suffix) && word.length() > suffix.length()) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断是否为停用词（更严格的判断）
     */
    private static boolean isStopWord(String word) {
        Set<String> extendedStopWords = Set.of(
                "the", "a", "an", "and", "or", "but", "in", "on", "at", "to", "for", "of", "with", "by", "from", "up", "about",
                "is", "are", "was", "were", "be", "been", "being", "have", "has", "had", "do", "does", "did", "will", "would",
                "could", "should", "may", "might", "can", "must", "shall", "very", "much", "more", "most", "less", "least",
                "good", "bad", "new", "old", "big", "small", "high", "low", "long", "short", "first", "last", "next", "previous",
                "this", "that", "these", "those", "i", "you", "he", "she", "it", "we", "they", "me", "him", "her", "us", "them",
                "my", "your", "his", "its", "our", "their", "mine", "yours", "hers", "ours", "theirs"
        );
        return extendedStopWords.contains(word.toLowerCase());
    }
}