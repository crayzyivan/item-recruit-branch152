package com.item.framework.constant;

import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;

import java.text.MessageFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * @author : lh
 */

public interface CommonConstants {
    interface LocalDateTimeConstant {

        String LOCAL_DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
        String LOCAL_DATE_TIME_FORMAT_T_Z = "yyyy-MM-dd'T'HH:mm:ss'Z'";
        String LOCAL_DATE_FORMAT = "yyyy-MM-dd";
        String LOCAL_TIME_FORMAT = "HH:mm:ss";
        String LOCAL_YEAR_MONTH_FORMAT = "MM/yyyy";

        DateTimeFormatter LOCAL_DATE_TIME_FORMATTER  = DateTimeFormatter.ofPattern(LOCAL_DATE_TIME_FORMAT);
        DateTimeFormatter LOCAL_DATE_TIME_FORMATTER_T_Z = DateTimeFormatter.ofPattern(LOCAL_DATE_TIME_FORMAT_T_Z);
        DateTimeFormatter LOCAL_DATE_FORMATTER  = DateTimeFormatter.ofPattern(LOCAL_DATE_FORMAT);
        DateTimeFormatter LOCAL_TIME_FORMATTER  = DateTimeFormatter.ofPattern(LOCAL_TIME_FORMAT);
        DateTimeFormatter LOCAL_YEAR_MONTH_FORMAT_FORMATTER = DateTimeFormatter.ofPattern(LOCAL_YEAR_MONTH_FORMAT);

        LocalDateTimeSerializer LOCAL_DATE_TIME_SERIALIZER = new LocalDateTimeSerializer(CommonConstants.LocalDateTimeConstant.LOCAL_DATE_TIME_FORMATTER);
        LocalDateTimeSerializer LOCAL_DATE_TIME_SERIALIZER_T_Z = new LocalDateTimeSerializer(CommonConstants.LocalDateTimeConstant.LOCAL_DATE_TIME_FORMATTER_T_Z);
        LocalDateTimeDeserializer LOCAL_DATE_TIME_DESERIALIZER = new LocalDateTimeDeserializer(CommonConstants.LocalDateTimeConstant.LOCAL_DATE_TIME_FORMATTER);
        LocalDateSerializer LOCAL_DATE_SERIALIZER = new LocalDateSerializer(CommonConstants.LocalDateTimeConstant.LOCAL_DATE_FORMATTER);
        LocalTimeSerializer LOCAL_TIME_SERIALIZER = new LocalTimeSerializer(CommonConstants.LocalDateTimeConstant.LOCAL_TIME_FORMATTER);
    }

    interface StrConstants {
        MessageFormat FORMAT = new MessageFormat(JobResponseCode.JOB_FIELD_VALIDATED_FAIL.getMsg());
        String GRANT_TYPE_CLIENT_CREDENTIALS = "client_credentials";
        String GRANT_TYPE = "grant_type";
        String SCOPE = "scope";
        String OAUTH2_TOKEN = "/oauth2/token";
        String TICKET_EXCHANGE_TOKEN_URL ="/ticket/exchange";
        String AUTHORIZATION_HEADER = "Authorization";
        String REFRESH_TOKEN_HEADER = "X-Refresh-Token";
        String NEW_TOKEN_HEADER = "X-New-Access-Token";
        String NEW_REFRESH_TOKEN_HEADER = "X-New-Refresh-Token";
        String TENANT_ID_HEADER = "X-Tenant-Id";
        String AUTHORIZATION_BEARER_HEADER = "Bearer ";
        String JOB_BY_DELETED = "Job by deleted";
        String APP_CODE = "recruit_saas";
        String SELF_APP_CODE = "recruit_saas";
        String EVENT_CODE_REGISTER = "REGISTER";
        String EVENT_CODE_CREATE_SUB = "CREATE_SUB";
        String SYSTEM = "SYSTEM";
        String CRM_CLIENT_HEADERS_X_TENANT_ID= "x-tenant-id";
        String CRM_LEADS_MODULE_3_COMPANY_NAME = "company_name";
        String CRM_LEADS_MODULE_3_DOMAIN_NAME = "domain_name";
        String CRM_LEADS_MODULE_3_PHONE = "phone";
        String TRACE_ID_HEADER = "Trace-Id-Log";
        String ES_FIELD_KEY_WORD = ".keyword";
        String ASTERISK_SYMBOL = "*";
        String HTTP = "http://";
        String HTTPS = "https://";
        String ERROR = "error";
        String SUCCESS = "success";
        String ALL = "all";
        String REDDIT = "reddit";
        String YOUTUBE = "youtube";
        String TWITTER = "twitter";
        String INSTAGRAM = "instagram";
        String PINTEREST = "pinterest";
        String QUESTION_5S = "question5S";
    }

    interface NetConstants {
        String ACCEPT_LANGUAGE = "accept-language";
    }

    interface NumConstants {
        Integer SECONDS_MILL = 1000;
        int JOB_FIELD_LENGTH_LIMIT = 512;
        int LIMIT_PRE_2 = 2;
        int LIMIT_SUFFIX_2 = 2;
        int LIMIT = LIMIT_PRE_2 + LIMIT_SUFFIX_2;
        int TWITTER_LENGTH_LIMIT = 280;
    }


    //招聘状态变更锁前缀
    public static final String JOB_STATUS_LOCK_PREFIX = "jobApplyStatus:lock:";
    //招聘流程状态流转错误
    public static final String EXCEPTION_SYSTEM_IS_BUSY = "System is busy. Please try again later.";
    public static final String EXCEPTION_INVALID_TRANSITION_EVENT = "Invalid Transition Event";

    //Redis缓存Cacheable value值
    String REDIS_CACHE_JOB_CATEGORY = "job:category:v2";
    String REDIS_CACHE_JOB_MODE = "job:mode:v2";
    String REDIS_CACHE_JOB_TYPE = "job:type:v2";
    String REDIS_CACHE_DICTIONARY_BY_TYPE = "dictionary:by:type:v3";
    String REDIS_CACHE_LOCATION_COUNTRIES = "location:countries:v3";
    String REDIS_CACHE_LOCATION_STATES = "location:states:by:country:v3";
    String REDIS_CACHE_LOCATION_CITIES = "location:cities:by:state:v3";
    String REDIS_CACHE_COMPANY_INFO = "simple:info";
    String REDIS_CACHE_USER_PRIMARY_BY_COMPANY = "user:primary:by:company";

    //发送面试链接的最低分数
    public static final int MIN_INTERVIEW_MAIL_SCORE = 80;
    //充值币种
    public static final String TOP_UP_CURRENCY_CODE = "USD";
    //ai面试结果软技能 技能名称
    public static final String AI_VETTED_SOFT_SKILL_NAME = "communication";
    //AI面试结果中性格评测
    List<String> AI_VETTED_PERSONALITY_TEST_NAME = List.of("Personality Test", "性格测试", "性格テスト", "Test de Personalidad");
    //geo 搜索默认范围
    String DEFAULT_SEARCH_RADIUS = "50km";
}
