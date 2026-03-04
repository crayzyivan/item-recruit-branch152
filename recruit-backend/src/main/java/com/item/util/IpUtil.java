package com.item.util;

import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;


import java.util.List;

/**
 * IP 工具类
 * 用于 IP 白名单校验，支持精确、通配符及范围匹配
 */
public class IpUtil {

    private static final String IP_SPLIT_REGEX = "\\.";
    private static final String WILDCARD = "*";
    private static final String RANGE_SEPARATOR = "~";

    /**
     * 检测 IP 是否允许访问
     * <p>
     * 支持的规则格式：
     * 1. 精确匹配：如 176.2.148.11
     * 2. 任意匹配：如 176.*.148.11 (* 代表 0-255)
     * 3. 范围匹配：如 176.2.148.2~10 (代表 2 到 10)
     * <p>
     * 说明：任意值(*)和范围值(~)对 IP 的 4 个段都生效。
     *
     * @param clientIp     客户端 IP (IPv4)
     * @param whitelistIps 白名单规则列表
     * @return boolean     是否匹配（若白名单为空，默认返回 true）
     */
    public static boolean isIpAllowed(String clientIp, List<String> whitelistIps) {
        // 如果白名单为空，默认允许所有
        if (CollectionUtils.isEmpty(whitelistIps)) {
            return true;
        }
        if (!StringUtils.hasText(clientIp)) {
            return false;
        }

        String[] clientIpSegments = clientIp.split(IP_SPLIT_REGEX);
        // 仅支持 IPv4，必须为 4 段
        if (clientIpSegments.length != 4) {
            return false;
        }

        for (String rule : whitelistIps) {
            if (isRuleMatched(clientIpSegments, rule)) {
                return true;
            }
        }

        return false;
    }

    /**
     * 判断单个 IP 是否匹配单条规则
     */
    private static boolean isRuleMatched(String[] clientIpSegments, String rule) {
        if (!StringUtils.hasText(rule)) {
            return false;
        }

        String[] ruleSegments = rule.trim().split(IP_SPLIT_REGEX);
        if (ruleSegments.length != 4) {
            return false;
        }

        for (int i = 0; i < 4; i++) {
            if (!matchIpSegment(clientIpSegments[i], ruleSegments[i])) {
                return false;
            }
        }
        return true;
    }

    /**
     * 匹配单个 IP 段
     *
     * @param ipSegmentStr   客户端 IP 段字符串
     * @param ruleSegmentStr 规则 IP 段字符串
     * @return boolean
     */
    private static boolean matchIpSegment(String ipSegmentStr, String ruleSegmentStr) {
        ruleSegmentStr = ruleSegmentStr.trim();
        ipSegmentStr = ipSegmentStr.trim();

        // 1. 任意值匹配 (*)
        if (WILDCARD.equals(ruleSegmentStr)) {
            return true;
        }

        // 解析客户端 IP 段为整数（确保是有效数字）
        Integer ipValue = parseIpSegment(ipSegmentStr);
        if (ipValue == null) {
            return false;
        }

        // 2. 范围匹配 (Start~End)
        if (ruleSegmentStr.contains(RANGE_SEPARATOR)) {
            String[] range = ruleSegmentStr.split(RANGE_SEPARATOR);
            if (range.length != 2) {
                return false;
            }
            Integer start = parseIpSegment(range[0]);
            Integer end = parseIpSegment(range[1]);

            // 范围解析失败
            if (start == null || end == null) {
                return false;
            }
            // 自动处理 start > end 的情况，如配置了 10~2
            return ipValue >= Math.min(start, end) && ipValue <= Math.max(start, end);
        }

        // 3. 精确匹配
        Integer ruleValue = parseIpSegment(ruleSegmentStr);
        return ruleValue != null && ruleValue.equals(ipValue);
    }

    /**
     * 解析 IP 段字符串为整数
     * 校验范围 [0, 255]
     */
    private static Integer parseIpSegment(String segment) {
        try {
            if (!StringUtils.hasText(segment)) {
                return null;
            }
            int value = Integer.parseInt(segment.trim());
            if (value < 0 || value > 255) {
                return null;
            }
            return value;
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
