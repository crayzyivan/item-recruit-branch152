package com.item.util;

import com.google.common.hash.Hashing;
import org.apache.commons.lang3.StringUtils;

import java.nio.charset.StandardCharsets;

/**
 * Hash utility class for generating hash values
 * Uses Guava's Murmur3 implementation for high performance and reliability
 */
public class HashUtils {

    private HashUtils() {
        // Utility class, prevent instantiation
    }

    /**
     * Generate Murmur3 hash for the given string using Guava implementation
     * 
     * @param input the input string to hash
     * @return the hash value as a string
     */
    public static String murmur3Hash(String input) {
        if (StringUtils.isBlank(input)) {
            return null;
        }
        
        return Hashing.murmur3_128()
                .hashString(input, StandardCharsets.UTF_8)
                .toString();
    }

    /**
     * Generate Murmur3 hash for concatenated strings using Guava implementation
     * 
     * @param strings the strings to concatenate and hash
     * @return the hash value as a string
     */
    public static String murmur3Hash(String... strings) {
        if (strings == null || strings.length == 0) {
            return null;
        }
        
        StringBuilder sb = new StringBuilder();
        for (String str : strings) {
            if (StringUtils.isNotBlank(str)) {
                sb.append(str);
            }
        }
        
        return murmur3Hash(sb.toString());
    }

    /**
     * Generate Murmur3 hash with custom seed using Guava implementation
     * 
     * @param input the input string to hash
     * @param seed the seed value for the hash function
     * @return the hash value as a string
     */
    public static String murmur3Hash(String input, int seed) {
        if (StringUtils.isBlank(input)) {
            return null;
        }
        
        return Hashing.murmur3_128(seed)
                .hashString(input, StandardCharsets.UTF_8)
                .toString();
    }

    /**
     * Generate Murmur3 hash for concatenated strings with custom seed
     * 
     * @param seed the seed value for the hash function
     * @param strings the strings to concatenate and hash
     * @return the hash value as a string
     */
    public static String murmur3Hash(int seed, String... strings) {
        if (strings == null || strings.length == 0) {
            return null;
        }
        
        StringBuilder sb = new StringBuilder();
        for (String str : strings) {
            if (StringUtils.isNotBlank(str)) {
                sb.append(str);
            }
        }
        
        return murmur3Hash(sb.toString(), seed);
    }
} 