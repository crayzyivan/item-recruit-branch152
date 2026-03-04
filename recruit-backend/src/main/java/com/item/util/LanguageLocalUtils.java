package com.item.util;

import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.item.dto.cache.CityCacheDTO;
import com.item.dto.cache.CountryCacheDTO;
import com.item.dto.cache.DictionaryCacheDTO;
import com.item.dto.cache.StateCacheDTO;
import com.item.entity.CityEntity;
import com.item.entity.CountryEntity;
import com.item.entity.DictionaryEntity;
import com.item.entity.JobCategoryEntity;
import com.item.entity.JobModeEntity;
import com.item.entity.JobTypeEntity;
import com.item.entity.StateEntity;
import static com.item.framework.constant.CommonConstants.NetConstants.ACCEPT_LANGUAGE;
import com.item.framework.constant.JobResponseCode;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.Locale;
import java.util.Map;
import java.util.function.Function;

/**
 * @author : lh
 */
@Slf4j
public class LanguageLocalUtils {
    private LanguageLocalUtils() {
    }

    /**
     * 西班牙语
     */
    public static final Locale SPANISH =  Locale.forLanguageTag("es");
    /**
     * 西班牙语 西班牙
     */
    public static final Locale SPAIN =  Locale.forLanguageTag("es-ES");

    // ==================== 多语言函数映射 Maps ====================
    
    /**
     * DictionaryEntity 多语言字段映射
     * 映射语言代码到对应的getter方法引用
     */
    private static final Map<String, Function<DictionaryEntity, String>> DICTIONARY_FUNCTION_MAP =
            Map.of(
                    Locale.CHINA.getLanguage(), DictionaryEntity::getChineseName,
                    Locale.JAPAN.getLanguage(), DictionaryEntity::getJapaneseName,
                    SPAIN.getLanguage(), DictionaryEntity::getSpanishName
            );

    /**
     * DictionaryCacheDTO 多语言字段映射
     */
    private static final Map<String, Function<DictionaryCacheDTO, String>> DICTIONARY_CACHE_FUNCTION_MAP =
            Map.of(
                    Locale.CHINA.getLanguage(), DictionaryCacheDTO::getChineseName,
                    Locale.JAPAN.getLanguage(), DictionaryCacheDTO::getJapaneseName,
                    SPAIN.getLanguage(), DictionaryCacheDTO::getSpanishName
            );

    /**
     * JobCategoryEntity 多语言字段映射
     */
    private static final Map<String, Function<JobCategoryEntity, String>> JOB_CATEGORY_FUNCTION_MAP =
            Map.of(
                    Locale.CHINA.getLanguage(), JobCategoryEntity::getChineseName,
                    Locale.JAPAN.getLanguage(), JobCategoryEntity::getJapaneseName,
                    SPAIN.getLanguage(), JobCategoryEntity::getSpanishName
            );

    /**
     * JobModeEntity 多语言字段映射
     */
    private static final Map<String, Function<JobModeEntity, String>> JOB_MODE_FUNCTION_MAP =
            Map.of(
                    Locale.CHINA.getLanguage(), JobModeEntity::getChineseName,
                    Locale.JAPAN.getLanguage(), JobModeEntity::getJapaneseName,
                    SPAIN.getLanguage(), JobModeEntity::getSpanishName
            );

    /**
     * JobTypeEntity 多语言字段映射
     */
    private static final Map<String, Function<JobTypeEntity, String>> JOB_TYPE_FUNCTION_MAP =
            Map.of(
                    Locale.CHINA.getLanguage(), JobTypeEntity::getChineseName,
                    Locale.JAPAN.getLanguage(), JobTypeEntity::getJapaneseName,
                    SPAIN.getLanguage(), JobTypeEntity::getSpanishName
            );

    /**
     * CityEntity 多语言字段映射（仅支持中文）
     */
    private static final Map<String, Function<CityEntity, String>> CITY_FUNCTION_MAP =
            Map.of(
                    Locale.CHINA.getLanguage(), CityEntity::getChineseName,
                    Locale.JAPAN.getLanguage(), CityEntity::getJapaneseName,
                    SPAIN.getLanguage(), CityEntity::getSpanishName
            );

    /**
     * StateEntity 多语言字段映射（仅支持中文）
     */
    private static final Map<String, Function<StateEntity, String>> STATE_FUNCTION_MAP =
            Map.of(
                    Locale.CHINA.getLanguage(), StateEntity::getChineseName,
                    Locale.JAPAN.getLanguage(), StateEntity::getJapaneseName,
                    SPAIN.getLanguage(), StateEntity::getSpanishName
            );

    /**
     * CountryEntity 多语言字段映射（仅支持中文）
     */
    private static final Map<String, Function<CountryEntity, String>> COUNTRY_FUNCTION_MAP =
            Map.of(
                    Locale.CHINA.getLanguage(), CountryEntity::getChineseName,
                    Locale.JAPAN.getLanguage(), CountryEntity::getJapaneseName,
                    SPAIN.getLanguage(), CountryEntity::getSpanishName
            );

    /**
     * CityCacheDTO 多语言字段映射（仅支持中文）
     */
    private static final Map<String, Function<CityCacheDTO, String>> CITY_CACHE_FUNCTION_MAP =
            Map.of(
                    Locale.CHINA.getLanguage(), CityCacheDTO::getChineseName,
                    Locale.JAPAN.getLanguage(), CityCacheDTO::getJapaneseName,
                    SPAIN.getLanguage(), CityCacheDTO::getSpanishName
            );

    /**
     * StateCacheDTO 多语言字段映射（仅支持中文）
     */
    private static final Map<String, Function<StateCacheDTO, String>> STATE_CACHE_FUNCTION_MAP =
            Map.of(
                    Locale.CHINA.getLanguage(), StateCacheDTO::getChineseName,
                    Locale.JAPAN.getLanguage(), StateCacheDTO::getJapaneseName,
                    SPAIN.getLanguage(), StateCacheDTO::getSpanishName
            );

    /**
     * CountryCacheDTO 多语言字段映射（仅支持中文）
     */
    private static final Map<String, Function<CountryCacheDTO, String>> COUNTRY_CACHE_FUNCTION_MAP =
            Map.of(
                    Locale.CHINA.getLanguage(), CountryCacheDTO::getChineseName,
                    Locale.JAPAN.getLanguage(), CountryCacheDTO::getJapaneseName,
                    SPAIN.getLanguage(), CountryCacheDTO::getSpanishName
            );

    /**
     * CityEntity SFunction 多语言字段映射（仅支持中文）
     * 用于 MyBatis-Plus 查询
     */
    private static final Map<String, SFunction<CityEntity, String>> CITY_SFUNCTION_MAP =
            Map.of(Locale.CHINA.getLanguage(), CityEntity::getChineseName,
                    Locale.JAPAN.getLanguage(), CityEntity::getJapaneseName,
                    SPAIN.getLanguage(), CityEntity::getSpanishName
            );

    /**
     * StateEntity SFunction 多语言字段映射（仅支持中文）
     * 用于 MyBatis-Plus 查询
     */
    private static final Map<String, SFunction<StateEntity, String>> STATE_SFUNCTION_MAP =
            Map.of(Locale.CHINA.getLanguage(), StateEntity::getChineseName,
                    Locale.JAPAN.getLanguage(), StateEntity::getJapaneseName,
                    SPAIN.getLanguage(), StateEntity::getSpanishName
            );

    /**
     * CountryEntity SFunction 多语言字段映射（仅支持中文）
     * 用于 MyBatis-Plus 查询
     */
    private static final Map<String, SFunction<CountryEntity, String>> COUNTRY_SFUNCTION_MAP =
            Map.of(Locale.CHINA.getLanguage(), CountryEntity::getChineseName,
                    Locale.JAPAN.getLanguage(), CountryEntity::getJapaneseName,
                    SPAIN.getLanguage(), CountryEntity::getSpanishName
            );

    public static Locale getLocale(HttpServletRequest request) {
        String languageLocale = request.getHeader(ACCEPT_LANGUAGE);
        log.info("HttpServletRequest Locale is {}", languageLocale);
        if (StringUtils.isBlank(languageLocale)) {
            log.warn("HttpServletRequest Locale is null");
            return Locale.ENGLISH;
        }
        Locale locale = Locale.forLanguageTag(languageLocale);
        if (locale == null) {
            log.warn("HttpServletRequest Locale is null");
            return Locale.ENGLISH;
        }

        if (isUnSupportLocale(locale)) {
            log.warn("HttpServletRequest Locale is unSupport locale, current is {}", locale);
            return Locale.ENGLISH;
        }
        return locale;
    }

    /**
     * 是否支持的语言
     * 支持英文 中文 日语 西班牙语
     *
     * @param locale 当前语言
     * @return 是否支持 true 不支持 false 支持
     */
    public static boolean isUnSupportLocale(Locale locale) {
        return (locale == null) || (locale != Locale.ENGLISH && locale != Locale.US &&
                locale != Locale.CHINESE && locale != Locale.SIMPLIFIED_CHINESE && locale != Locale.CHINA &&
                locale != Locale.JAPAN && locale != Locale.JAPANESE &&
                !LanguageLocalUtils.SPANISH.equals(locale) && !LanguageLocalUtils.SPAIN.equals(locale) &&
                !LanguageLocalUtils.SPAIN.getLanguage().equals(locale.getLanguage()));
    }

    public static boolean isChinese(Locale locale) {
        return locale != null && Locale.CHINA.getLanguage().equals(locale.getLanguage());
    }

    public static boolean isEnglishLanguage(Locale locale) {
        return locale != null && Locale.ENGLISH.getLanguage().equals(locale.getLanguage());
    }

    public static String getChineseLanguageCode() {
        return Locale.CHINA.getLanguage();
    }

    public static boolean isJapanLanguage(Locale locale) {
        return locale != null && Locale.JAPAN.getLanguage().equals(locale.getLanguage());
    }

    public static String getJapanLanguageCode() {
        return Locale.JAPAN.getLanguage();
    }

    public static boolean isSpanishLanguage(Locale locale) {
        return locale != null && LanguageLocalUtils.SPAIN.getLanguage().equals(locale.getLanguage());
    }

    public static String getSpanishLanguageCode() {
        return LanguageLocalUtils.SPAIN.getLanguage();
    }

    public static String getReapplyForInvitationEmailSubject(Locale locale) {
//        if (isJapanLanguage(locale)) {
//            return "ふたたび しょうたい を もうしこむ - ";
//        }
//        if (isSpanishLanguage(locale)) {
//            return "Reenviar la solicitud de invitación - ";
//        }
        if (isChinese(locale)) {
            return  "重新申请邀请 - ";
        }
        return "Reapply Invitation - ";
    }

    //  关于字典的多语言获取
    public static Function<DictionaryEntity, String> getFunctionDictionaryValue(Locale locale) {
        log.debug("getFunctionDictionaryValue called with locale: {}", locale);
        if (locale == null) {
            return DictionaryEntity::getValue;
        }
        return DICTIONARY_FUNCTION_MAP.getOrDefault(
                locale.getLanguage(),
                DictionaryEntity::getValue
        );
    }

    public static Function<DictionaryCacheDTO, String> getFunctionDictionaryCacheValue(Locale locale) {
        log.debug("getFunctionDictionaryCacheValue called with locale: {}", locale);
        if (locale == null) {
            return DictionaryCacheDTO::getValue;
        }
        return DICTIONARY_CACHE_FUNCTION_MAP.getOrDefault(
                locale.getLanguage(),
                DictionaryCacheDTO::getValue
        );
    }

    // 关于categories多语言获取
    public static Function<JobCategoryEntity, String> getFunctionJobCategoryName(Locale locale) {
        log.debug("getFunctionJobCategoryName called with locale: {}", locale);
        if (locale == null) {
            return JobCategoryEntity::getName;
        }
        return JOB_CATEGORY_FUNCTION_MAP.getOrDefault(
                locale.getLanguage(),
                JobCategoryEntity::getName
        );
    }

    // 关于jobMode多语言获取
    public static Function<JobModeEntity, String> getFunctionJobModeName(Locale locale) {
        log.debug("getFunctionJobModeName called with locale: {}", locale);
        if (locale == null) {
            return JobModeEntity::getModeName;
        }
        return JOB_MODE_FUNCTION_MAP.getOrDefault(
                locale.getLanguage(),
                JobModeEntity::getModeName
        );
    }

    // 关于jobType多语言获取
    public static Function<JobTypeEntity, String> getFunctionJobTypeName(Locale locale) {
        log.debug("getFunctionJobTypeName called with locale: {}", locale);
        if (locale == null) {
            return JobTypeEntity::getName;
        }
        return JOB_TYPE_FUNCTION_MAP.getOrDefault(
                locale.getLanguage(),
                JobTypeEntity::getName
        );
    }

    //  关于地点 国家 省 市 区多语言获取
    public static Function<CityEntity, String> getFunctionCityName(Locale locale) {
        log.debug("getFunctionCityName called with locale: {}", locale);
        if (locale == null) {
            return CityEntity::getName;
        }
        return CITY_FUNCTION_MAP.getOrDefault(
                locale.getLanguage(),
                CityEntity::getName
        );
    }

    public static Function<StateEntity, String> getFunctionStateName(Locale locale) {
        log.debug("getFunctionStateName called with locale: {}", locale);
        if (locale == null) {
            return StateEntity::getName;
        }
        return STATE_FUNCTION_MAP.getOrDefault(
                locale.getLanguage(),
                StateEntity::getName
        );
    }

    public static Function<CountryEntity, String> getFunctionCountryName(Locale locale) {
        log.debug("getFunctionCountryName called with locale: {}", locale);
        if (locale == null) {
            return CountryEntity::getName;
        }
        return COUNTRY_FUNCTION_MAP.getOrDefault(
                locale.getLanguage(),
                CountryEntity::getName
        );
    }

    public static Function<CityCacheDTO, String> getFunctionCityCacheName(Locale locale) {
        log.debug("getFunctionCityCacheName called with locale: {}", locale);
        if (locale == null) {
            return CityCacheDTO::getName;
        }
        return CITY_CACHE_FUNCTION_MAP.getOrDefault(
                locale.getLanguage(),
                CityCacheDTO::getName
        );
    }

    public static Function<StateCacheDTO, String> getFunctionStateCacheName(Locale locale) {
        log.debug("getFunctionStateCacheName called with locale: {}", locale);
        if (locale == null) {
            return StateCacheDTO::getName;
        }
        return STATE_CACHE_FUNCTION_MAP.getOrDefault(
                locale.getLanguage(),
                StateCacheDTO::getName
        );
    }

    public static Function<CountryCacheDTO, String> getFunctionCountryCacheName(Locale locale) {
        log.debug("getFunctionCountryCacheName called with locale: {}", locale);
        if (locale == null) {
            return CountryCacheDTO::getName;
        }
        return COUNTRY_CACHE_FUNCTION_MAP.getOrDefault(
                locale.getLanguage(),
                CountryCacheDTO::getName
        );
    }

    public static SFunction<CityEntity, String> getSFunctionCityName(Locale locale) {
        log.debug("getSFunctionCityName called with locale: {}", locale);
        if (locale == null) {
            return CityEntity::getName;
        }
        return CITY_SFUNCTION_MAP.getOrDefault(
                locale.getLanguage(),
                CityEntity::getName
        );
    }

    public static SFunction<StateEntity, String> getSFunctionStateName(Locale locale) {
        log.debug("getSFunctionStateName called with locale: {}", locale);
        if (locale == null) {
            return StateEntity::getName;
        }
        return STATE_SFUNCTION_MAP.getOrDefault(
                locale.getLanguage(),
                StateEntity::getName
        );
    }

    public static SFunction<CountryEntity, String> getSFunctionCountryName(Locale locale) {
        log.debug("getSFunctionCountryName called with locale: {}", locale);
        if (locale == null) {
            return CountryEntity::getName;
        }
        return COUNTRY_SFUNCTION_MAP.getOrDefault(
                locale.getLanguage(),
                CountryEntity::getName
        );
    }

    /**
     * 根据Locale获取错误消息（中英文切换）
     * 
     * @param englishMessage 英文错误消息
     * @param chineseMessage 中文错误消息
     * @return 根据Locale返回对应的错误消息
     */
    public static String getErrorMessage(String englishMessage, String chineseMessage) {
        Locale locale = UserContextUtil.getLanguageLocal();
        boolean chinese = isChinese(locale);
        return chinese ? chineseMessage : englishMessage;
    }

    /**
     * 根据Locale获取JobResponseCode的错误消息（中英文切换）
     *
     * @param jobResponseCode JobResponseCode枚举值
     * @return 根据Locale返回对应的错误消息
     */
    public static String getJobResponseCodeMessage(JobResponseCode jobResponseCode) {
        Locale locale = UserContextUtil.getLanguageLocal();
        boolean chinese = isChinese(locale);

        if (chinese) {
            switch (jobResponseCode) {
                case JOB_NOT_FOUND:
                    return "未找到职位。";
                case JOB_STATUS_NOT_ACTIVE:
                    return "当前职位状态未开放。";
                default:
                    return jobResponseCode.getMsg();
            }
        } else {
            return jobResponseCode.getMsg();
        }
    }

    /**
     * 根据Locale获取邮件模板名称（支持国际化）
     * 如果是中文环境，返回中文模板名称（-zh后缀），否则返回英文模板名称
     *
     * @param templateName 基础模板名称（不含语言后缀）
     * @return 根据Locale返回对应的模板名称
     */
    public static String getEmailTemplateName(String templateName) {
        Locale locale = UserContextUtil.getLanguageLocal();
        boolean chinese = isChinese(locale);

        if (chinese) {
            // 如果模板名称已经包含扩展名，需要插入-zh
            if (templateName.endsWith(".html")) {
                return templateName.replace(".html", "-zh.html");
            } else {
                return templateName + "-zh";
            }
        } else {
            return templateName;
        }
    }

//    public static void main(String[] args) {
//
//        String str1 = "zh-CN";
//        String str2 = "en";
//        String str3 = "zh";
//        String str4 = "ja";
//        String str5 = "ja-JP";
//        String str6 = "es";
//        String str7 = "es-ES";
//        String str8 = "es-MX";
//
//        Locale locale = Locale.forLanguageTag(str1);
//        Locale locale1 = Locale.forLanguageTag(str2);
//        Locale locale2 = Locale.forLanguageTag(str3);
//        Locale locale3 = Locale.forLanguageTag(str4);
//        Locale locale4 = Locale.forLanguageTag(str5);
//        Locale locale5 = Locale.forLanguageTag(str6);
//        Locale locale6 = Locale.forLanguageTag(str7);
//        Locale locale7 = Locale.forLanguageTag(str8);
//
//        log.info("{} {} {} {} {} {} {} {}", locale, locale1, locale2, locale3, locale4, locale5, locale6, locale7);
//        log.info("{} {} {}", Locale.CHINESE.getLanguage(), Locale.SIMPLIFIED_CHINESE.getLanguage(), Locale.CHINA.getLanguage());
//        log.info("{} {} {} {} {} {} {} {}", isUnSupportLocale(locale), isUnSupportLocale(locale1), isUnSupportLocale(locale2),
//                isUnSupportLocale(locale3), isUnSupportLocale(locale4), isUnSupportLocale(locale5), isUnSupportLocale(locale6),
//                isUnSupportLocale(locale7));
//
//    }
}
