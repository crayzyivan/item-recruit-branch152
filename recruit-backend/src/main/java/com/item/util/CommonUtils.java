package com.item.util;

import com.google.common.base.CharMatcher;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.item.dto.iam.FeignResponse;
import com.item.dto.iam.IamUserContextDTO;
import com.item.framework.config.RecruitCommonNacosConfig;
import static com.item.framework.constant.CommonConstants.NumConstants.LIMIT;
import static com.item.framework.constant.CommonConstants.NumConstants.LIMIT_PRE_2;
import static com.item.framework.constant.CommonConstants.NumConstants.LIMIT_SUFFIX_2;
import static com.item.framework.constant.CommonConstants.StrConstants.ASTERISK_SYMBOL;
import static com.item.framework.constant.CommonConstants.StrConstants.REFRESH_TOKEN_HEADER;
import static com.item.framework.constant.CommonConstants.StrConstants.TENANT_ID_HEADER;
import com.item.framework.constant.CommonResponseCode;
import com.item.framework.constant.RecruitAccountTypeEnum;
import com.item.framework.constant.UserIdentifyTypeEnum;
import com.item.framework.constant.UserViewEnum;
import com.item.framework.error.BusinessException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class CommonUtils {
    public static final String MID_SCORE = "-";
    public static final String COMMA_SCORE = ",";
    public static final String INCLINED_ROD = "/";
    public static final char MID_SCORE_CHAR = '-';
    private static final Joiner JOINER_MID_SCORE = Joiner.on(MID_SCORE).skipNulls();
    private static final Joiner JOINER_INCLINED_ROD = Joiner.on(INCLINED_ROD).skipNulls();
    private static final Joiner JOINER_COMMA_SCORE = Joiner.on(COMMA_SCORE).skipNulls();
    private static final Splitter MID_SCORE_SPLITTER = Splitter.on(MID_SCORE_CHAR).trimResults().omitEmptyStrings();
    // 需要转义的特殊字符（顺序不能乱，\需要放在最前面）
    private static final String[] SPECIAL_CHARS = {"\\", "*", "?"};
    // 转义后的替换字符
    private static final String[] ESCAPED_CHARS = {"\\\\", "\\*", "\\?"};


    // 支持的图片格式
    private static final Set<String> SUPPORTED_IMAGE_TYPES = Set.of(
            "image/png",
            "image/jpeg",
            "image/jpg",
            "image/gif"
    );

    // 默认最大文件大小 (256K)
    private static final long MAX_FILE_SIZE = 256 * 1024L;

    private CommonUtils() {
    }

    /**
     * 验证文件格式和大小
     * @param file MultipartFile文件
     * @param maxSize 最大文件大小（字节）
     * @return 文件大小（字节）
     * @throws BusinessException 如果文件格式不支持或大小超出限制
     */
    public static long validateFileFormatAndSize(MultipartFile file, long maxSize) {
        if (file == null || file.isEmpty()) {
            throw BusinessException.of(CommonResponseCode.COMMON_COMPANY_LOGO_UPLOAD_FILE_EMPTY);
        }

        // 获取文件大小
        long fileSize = file.getSize();
        
        // 检查文件大小
        if (fileSize > maxSize) {
            throw BusinessException.of(CommonResponseCode.COMMON_COMPANY_LOGO_UPLOAD_FILE_SIZE);
        }

        // 获取文件类型
        String contentType = file.getContentType();
        if (contentType == null || !SUPPORTED_IMAGE_TYPES.contains(contentType.toLowerCase())) {
            throw BusinessException.of(CommonResponseCode.COMMON_COMPANY_LOGO_UPLOAD_FILE_FORMAT);
        }

        return fileSize;
    }

    /**
     * 验证文件格式和大小（使用默认256KB大小限制）
     * @param file MultipartFile文件
     * @return 文件大小（字节）
     * @throws BusinessException 如果文件格式不支持或大小超出限制
     */
    public static long validateFileFormatAndSize(MultipartFile file) {
        return validateFileFormatAndSize(file, MAX_FILE_SIZE);
    }

    public static String join(Long... args) {
        return JOINER_MID_SCORE.join(args).trim();
    }

    public static String join(String... args) {
        return JOINER_MID_SCORE.join(args).trim();
    }

    public static String joinComma(String... args) {
        return JOINER_COMMA_SCORE.join(args).trim();
    }

    public static String joinInclinedRod(String... args) {
        return joinInclinedRod(true, args);
    }

    /**
     * 使用“/”连接多个字符串
     * @param skipBlank 是否跳过空白字符串
     * @param args 需要连接的字符串
     * @return
     */
    public static String joinInclinedRod(boolean skipBlank, String... args) {
        if (!skipBlank) {
            return JOINER_INCLINED_ROD.join(args).trim();
        }
        return JOINER_INCLINED_ROD.join(Arrays.stream(args).filter(StringUtils::isNotBlank).toArray(String[]::new)).trim();
    }


    /**
     * 对input清除并替换指定replace字符 使用“-”连接并且合并连续的“-” 转小写
     *
     * @param input
     * @param replace
     * @return
     */
    public static String cleanInputReplace(String input, String replace) {
        if (StringUtils.isBlank(input)) {
            return input;
        }
        CharMatcher charMatcher = CharMatcher.whitespace().or(CharMatcher.javaIsoControl());
        if (StringUtils.isNotBlank(replace)) {
            charMatcher = charMatcher.or(CharMatcher.anyOf(replace));
        }
        String replaceAfter = charMatcher.replaceFrom(input, MID_SCORE).trim();
        String collapseAfter = CharMatcher.is(MID_SCORE_CHAR).collapseFrom(replaceAfter, MID_SCORE_CHAR);
        return CharMatcher.is(MID_SCORE_CHAR).trimFrom(collapseAfter).toLowerCase();
    }

    /**
     * 分割字符串 根据“-”
     *
     * @param input
     * @return
     */
    public static List<String> splitterStr(String input) {
        if (StringUtils.isBlank(input)) {
            return Collections.emptyList();
        }
        return MID_SCORE_SPLITTER.splitToList(input);
    }

    /**
     * 获取分割后的最后一个字符串
     *
     * @param input
     * @return
     */
    public static String splitterStrGetLast(String input) {
        if (StringUtils.isBlank(input)) {
            return null;
        }
        List<String> splitterList = splitterStr(input);
        return splitterList.get(splitterList.size() - 1);
    }


    public static  <T> T getData(FeignResponse<T> iamResponse) {
        if (iamResponse.getSuccess() == null || !iamResponse.getSuccess()) {
            log.warn("get user info failed {}", iamResponse);
            return null;
        }
        if (iamResponse.getData() == null) {
            log.warn("get user info data is null {} ", iamResponse);
            return null;
        }
        return iamResponse.getData();
    }

    public static String subStr(String input, int maxSize) {
        if (StringUtils.isBlank(input)) {
            return input;
        }
        if (input.length() > maxSize) {
            log.info("subStr input {}", input);
            return StringUtils.substring(input, 0, maxSize);
        }
        return input;
    }

    /**
     * 脱敏卡号 保留前三位后四位 中间星号替换
     * @param input 原始卡号
     * @return 脱敏后的卡号
     */
    public static String maskCardNumber(String input) {
        if (input == null || input.length() < 7) {
            return "**";
        }

        int length = input.length();
        StringBuilder masked = new StringBuilder(length);

        // 保留前三位
        masked.append(input, 0, 3);

        // 中间用星号替换
        for (int i = 3; i < length - 4; i++) {
            masked.append('*');
        }

        // 保留后四位
        masked.append(input, length - 4, length);

        return masked.toString();
    }

    /**
     * 脱敏Ayrshare Token 保留前2位后2位 中间10个星号替换
     * @param token 原始Token
     * @return 脱敏后的Token
     */
    public static String maskAyrshareToken(String token) {
        if (token == null || token.length() < LIMIT) {
            return "**********";
        }

        int length = token.length();

        // 保留前2位

        return token.substring(0, LIMIT_PRE_2) +
                // 中间用10个星号替换
                "**********" +
                // 保留后2位
                token.substring(Math.max(length - LIMIT_SUFFIX_2, LIMIT_SUFFIX_2), length);
    }


    /**
     * 获取地址名称
     * @param countryName 国家名称
     * @param stateName 省份名称
     * @param cityName 城市名称
     * @return
     */
    public static String getLocationName(String countryName, String stateName, String cityName) {
        return Stream.of(cityName, stateName, countryName) // 按顺序拼接
                .filter(s -> s != null && !s.isBlank())    // 过滤掉 null 或空字符串
                .collect(Collectors.joining(","));
    }

    public static <T1, T2> boolean isCollectionSizeEquals(Collection<T1> l1, Collection<T2> l2) {
        if (l1 == null && l2 == null) {
            return true;
        }
        if (l1 == null) {
            return false;
        }
        if (l2 == null) {
            return false;
        }
        return l1.size() == l2.size();
    }

    /**
     * Get user identity type with additional candidate company codes support
     * 1 判断当前用户的companyCode是否在C端租户的CompanyCode中  在其中
     *   1.1 判断是否开启 C端租户主账号判断逻辑  开启了 判断当前租户的primaryUser属性 true 返回主账号 false 应聘者
     *   1.2 未开启 C端租户主账号判断逻辑  直接返回应聘者
     * 2 不在 C端租户的CompanyCode中 那就是B端用户
     *   2.1 判断primaryUser属性 true返回 主账号
     *   2.2 判断扩张属性 recruitAccountType == primary 返回主账号
     *   2.3 其他 返回 子账号
     * @param context user context
     * @param  candidateUserIdentify set of company codes that should be identified as candidates
     * @return user identity type
     */
    public static UserIdentifyTypeEnum getUserIdentify(IamUserContextDTO context, RecruitCommonNacosConfig.CandidateUserIdentify candidateUserIdentify) {
        //companyCode in candidateCompanyCodes return candidate
        if (context == null) {
            log.warn("getUserIdentify context is null");
            throw BusinessException.of(CommonResponseCode.CURRENT_USER_INFO_EXCEPTION);
        }
        if (StringUtils.isBlank(context.getCompanyCode())) {
            log.warn("getUserIdentify context companyCode is blank {}", context);
            throw BusinessException.of(CommonResponseCode.CURRENT_USER_INFO_EXCEPTION);
        }
        log.debug("getUserIdentify context {} candidateUserIdentify config {}", JsonUtils.toJson(context), candidateUserIdentify);
        
        // 如果存在身份配置数据 并且 当前companyCode在配置的companyCodes中
        if (candidateUserIdentify != null &&
                CollectionUtils.isNotEmpty(candidateUserIdentify.getCandidateCompanyCodes()) &&
                context.getCompanyCode() != null &&
                candidateUserIdentify.getCandidateCompanyCodes().contains(context.getCompanyCode())) {
            // 如果没有启用(默认是启用状态)校验iam的PrimaryUser属性 直接返回身份为应聘者
            if (!candidateUserIdentify.isCandidatePrimaryUserEnable()) {
                log.debug("getUserIdentify current candidate hit candidateCompanyCodes {} and isCandidatePrimaryUserEnable is false", 
                          context.getCompanyCode());
                return UserIdentifyTypeEnum.CANDIDATE;
            }
            
            // 只有当primaryUser明确为true时才返回主账号身份,其余情况(false或null)返回应聘者身份
            if (Boolean.TRUE.equals(context.getPrimaryUser())) {
                log.debug("getUserIdentify current master recruit hit candidateCompanyCodes {} and primaryUser is true", 
                          context.getCompanyCode());
                return UserIdentifyTypeEnum.MASTER_RECRUIT;
            }
            
            // primaryUser为false或null时,返回应聘者身份
            log.debug("getUserIdentify current candidate hit candidateCompanyCodes {} and primaryUser is {} (false or null)", 
                      context.getCompanyCode(), context.getPrimaryUser());
            return UserIdentifyTypeEnum.CANDIDATE;
        }
        
        // Step 4: B-end user - check primaryUser OR recruitAccountType
        return determineBEndUserIdentity(context);
    }

    public static int getCurrentAccountView(IamUserContextDTO context, UserIdentifyTypeEnum userIdentify) {
        if (userIdentify == null) {
            return UserViewEnum.NOTHING_VIEW.getCode();
        }
        if (userIdentify == UserIdentifyTypeEnum.MASTER_RECRUIT) {
            return UserViewEnum.ALL_VIEW.getCode();
        }
        if (userIdentify == UserIdentifyTypeEnum.CANDIDATE) {
            return UserViewEnum.NOTHING_VIEW.getCode();
        }
        // Check recruitAccountType == PRIMARY && userIdentify
        if (context.getExternalInfo() != null) {
            RecruitAccountTypeEnum recruitType = RecruitAccountTypeEnum.getByCode(
                    context.getExternalInfo().getRecruitAccountType());
            log.debug("processRecruitExternal sub user context {}", context);
            if (recruitType.isPrimary()) {
                return UserViewEnum.XML_SETTING_VIEW.getCode();
            }
        }
        return UserViewEnum.NOTHING_VIEW.getCode();
    }

    /**
     * Determine B-end user identity based on primaryUser or recruitAccountType
     * Uses OR logic: returns MASTER_RECRUIT if either condition is met
     * 
     * @param context user context containing primaryUser and externalInfo
     * @return MASTER_RECRUIT if primaryUser=true OR recruitAccountType=PRIMARY, otherwise SUB_RECRUIT
     */
    private static UserIdentifyTypeEnum determineBEndUserIdentity(IamUserContextDTO context) {
        log.debug("getUserIdentify determineBEndUserIdentity context {}", context);
        // Check primaryUser == true
        if (Boolean.TRUE.equals(context.getPrimaryUser())) {
            log.debug("getUserIdentify B-end user companyCode {} with primaryUser=true, return MASTER_RECRUIT", 
                      context.getCompanyCode());
            return UserIdentifyTypeEnum.MASTER_RECRUIT;
        }
        
        // Check recruitAccountType == PRIMARY
//        if (context.getExternalInfo() != null) {
//            RecruitAccountTypeEnum recruitType = RecruitAccountTypeEnum.getByCode(
//                context.getExternalInfo().getRecruitAccountType());
//            if (recruitType.isPrimary()) {
//                log.debug("getUserIdentify B-end user companyCode {} with recruitAccountType=PRIMARY, return MASTER_RECRUIT",
//                          context.getCompanyCode());
//                return UserIdentifyTypeEnum.MASTER_RECRUIT;
//            }
//        }
        
        // Otherwise, return SUB_RECRUIT
        log.debug("getUserIdentify B-end user companyCode {}, return SUB_RECRUIT", 
                  context.getCompanyCode());
        return UserIdentifyTypeEnum.SUB_RECRUIT;
    }


    /**
     * 获取ai面试冻结积分
     * centExchangeRate 每分对应积分
     * interviewFreezeMinutes 分钟数
     * interviewCostPerMinute 每分钟费用
     */
    public static Integer  getInterviewFreezePoints(Integer centExchangeRate,Integer interviewFreezeMinutes,BigDecimal interviewCostPerMinute){
        // 1. 计算总费用（元）
        BigDecimal totalCostYuan = interviewCostPerMinute.multiply(BigDecimal.valueOf(interviewFreezeMinutes));
        // 2. 换算成分（元 × 100）
        BigDecimal totalCostFen = totalCostYuan.multiply(BigDecimal.valueOf(100));
        // 3. 转成整数（分），四舍五入
        int totalFen = totalCostFen.setScale(0, RoundingMode.HALF_UP).intValue();
        // 4. 计算积分（分 × 每分对应积分比例）
        int totalPoints = totalFen * centExchangeRate;
        return totalPoints;
    }

    /**
     * 转义wildcard查询中的特殊字符
     * @param input 用户输入的原始字符串
     * @return 转义后的字符串（若输入为null则返回null）
     */
    public static String escape(String input) {
        if (StringUtils.isBlank(input)) {
            return input;
        }
        String result = input;
        // 替换特殊字符为转义后的值
        for (int i = 0; i < SPECIAL_CHARS.length; i++) {
            result = result.replace(SPECIAL_CHARS[i], ESCAPED_CHARS[i]);
        }
        return result;
    }

    /**
     * 构建带前后通配符的查询字符串（常用于模糊匹配）
     * @param input 用户输入的原始字符串
     * @return 转义后并拼接通配符的字符串（格式：*转义后的值*）
     */
    public static String buildWildcardPattern(String input) {
        String escaped = escape(input);
        return StringUtils.isBlank(escaped) ? "" : "*" + escaped + "*";
    }

    public static String getAllLikeEscapeKeyWord(String keyword){
        if(StringUtils.isEmpty(keyword)) {
            return keyword;
        }
        String escape = escape(keyword);
        return ASTERISK_SYMBOL+escape+ASTERISK_SYMBOL;
    }

    /**
     * 判断字符串是否为数字
     * @param str
     * @return
     */
    public static boolean isLong(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }
        try {
            Long.parseLong(str);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 从文件路径中提取简历文件名
     * @param path 文件路径
     * @return 文件名
     */
    public static String extractResumeName(String path) {
        if (path == null || path.isEmpty()) {
            return null;
        }
        // 找到UUID后面的第一个 "_"
        int index = path.indexOf("_");
        String encodedName;
        if (index == -1) {
            encodedName = path.substring(path.lastIndexOf("/") + 1);
        } else {
            encodedName = path.substring(index + 1);
        }

        // URL 解码
        String decodedName;
        try {
            decodedName = URLDecoder.decode(encodedName, "UTF-8");
        } catch (Exception e) {
            decodedName = encodedName;
        }

//        // 取最终文件名（去掉前面的路径）
//        int lastSlash = Math.max(decodedName.lastIndexOf("/"), decodedName.lastIndexOf("\\"));
//        if (lastSlash != -1) {
//            return decodedName.substring(lastSlash + 1);
//        }
        return decodedName;
    }


    /**
     * 拆分关键词（处理连续空格、前后空格）
     * 优化：过滤停用词和短词，提升匹配精度
     */
    public static List<String> splitKeywords(String keywords) {
        if (keywords == null || keywords.trim().isEmpty()) {
            return new ArrayList<>();
        }

        // 按空格拆分，过滤空字符串
        List<String> basicKeywords = Arrays.stream(keywords.trim().split("\\s+"))
                .filter(k -> !k.isEmpty())
                .toList();

        // 进一步处理：移除常见的停用词，保留重要关键词
        List<String> filteredKeywords = basicKeywords.stream()
                .filter(keyword -> !isStopWord(keyword))
                .filter(keyword -> keyword.length() >= 2) // 过滤太短的词
                .toList();

        // 如果过滤后没有关键词，返回原始关键词
        return filteredKeywords.isEmpty() ? basicKeywords : filteredKeywords;
    }

    /**
     * 判断是否为停用词
     */
    private static boolean isStopWord(String word) {
        Set<String> stopWords = Set.of(
                "the", "a", "an", "and", "or", "but", "in", "on", "at", "to", "for", "of", "with", "by",
                "is", "are", "was", "were", "be", "been", "being", "have", "has", "had", "do", "does", "did",
                "will", "would", "could", "should", "may", "might", "can", "must", "shall"
        );
        return stopWords.contains(word.toLowerCase());
    }


    public static Long getSafeValue(Supplier<Long> supplier, Long defaultValue) {
        return supplier.get() == null ? defaultValue : supplier.get();
    }

    public static Long getSafeValueDefault0(Supplier<Long> supplier) {
        return getSafeValue(supplier, 0L);
    }

    public static Long getSafeValueDefault0FormInt(Supplier<Integer> supplier) {
        return supplier.get() == null ? 0L : supplier.get().longValue();
    }

    public static <T> T getSafeFirstFromList(List<T> list, T defaultValue) {
        if (CollectionUtils.isEmpty(list)) {
            return defaultValue;
        }
        return list.getFirst();
    }

    public static String getSafeValue(Supplier<String> supplier, String defaultValue) {
        return supplier.get() == null ? defaultValue : supplier.get();
    }

    public static String parseRefreshJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader(REFRESH_TOKEN_HEADER);
        if (StringUtils.isNotBlank(headerAuth)) {
            return headerAuth;
        }
        return null;
    }

    public static String parseRequestHeaderTenantId(HttpServletRequest request) {
        String headerTenantId = request.getHeader(TENANT_ID_HEADER);
        if (StringUtils.isNotBlank(headerTenantId)) {
            return headerTenantId;
        }
        return null;
    }

    public static String firstNonBlank(String first, String second, String... other) {
        log.info("firstNonBlank: first {}, second {} other {}", first, second, other);
        if (StringUtils.isNotBlank(first)) {
            return first;
        }
        if (StringUtils.isNotBlank(second)) {
            return second;
        }
        if (other == null) {
            return null;
        }
        for (String o : other) {
            if (StringUtils.isNotBlank(o)) {
                return o;
            }
        }
        return null;
    }
}
