package com.item.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import static com.item.framework.constant.CommonConstants.LocalDateTimeConstant.LOCAL_DATE_TIME_SERIALIZER;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;

/**
 * @author : lh
 */
public class JsonUtils {

    private static ObjectMapper defaultObjectMapper = createDefaultMapper();
    private static ObjectMapper defaultObjectSkipNullMapper = createDefaultSkipNullMapper();

    private JsonUtils() {
        // 私有构造函数，防止实例化
    }

    /**
     * 创建默认配置的 ObjectMapper
     */
    private static ObjectMapper createDefaultMapper() {
        ObjectMapper mapper = new ObjectMapper();

        // 配置模块
        mapper.registerModule(new JavaTimeModule());

        // 序列化配置
        // 忽略 null 值
//        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        // 禁用时间戳格式
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        // 允许空 Bean
        mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);

        // 反序列化配置
        // 忽略未知属性
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        return mapper;
    }

    /**
     * 创建默认配置的 ObjectMapper 过滤空属性
     */
    private static ObjectMapper createDefaultSkipNullMapper() {
        ObjectMapper mapper = new ObjectMapper();
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        javaTimeModule.addSerializer(LocalDateTime.class, LOCAL_DATE_TIME_SERIALIZER);
//        javaTimeModule.addDeserializer(LocalDateTime.class, LOCAL_DATE_TIME_DESERIALIZER);

        // 配置模块
        mapper.registerModule(javaTimeModule);

        // 序列化配置
        // 忽略 null 值
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        // 禁用时间戳格式
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        // 允许空 Bean
        mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        // 反序列化配置
        // 忽略未知属性
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        return mapper;
    }

    /**
     * 获取当前使用的 ObjectMapper
     */
    public static ObjectMapper getObjectMapper() {
        return defaultObjectMapper;
    }
    public static ObjectMapper getObjectSkipNullMapper() {
        return defaultObjectSkipNullMapper;
    }

    /**
     * 设置自定义的 ObjectMapper
     */
    public static void setObjectMapper(ObjectMapper objectMapper) {
        JsonUtils.defaultObjectMapper = Objects.requireNonNull(objectMapper);
    }

    /**
     * 将对象序列化为 JSON 字符串
     */
    public static String toJson(Object object) {
        try {
            return defaultObjectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("对象序列化失败: " + object, e);
        }
    }

    /**
     * 将对象序列化为 JSON 字符串
     */
    public static String toSkipNullJson(Object object) {
        try {
            return getObjectSkipNullMapper().writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("对象序列化失败: " + object, e);
        }
    }

    public static <T> Map<String, Object> toMap(T entity) {
        try {
            return getObjectSkipNullMapper().convertValue(entity, Map.class);
        } catch (Exception e) {
            throw new IllegalArgumentException("实体转map失败: ", e);
        }
    }


    /**
     * Map转实体对象
     */
    public static <T> T convertToEntity(Map<String, Object> map, Class<T> entityClass) {
        try {
            return defaultObjectSkipNullMapper.convertValue(map, entityClass);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Map转换实体失败: " + e.getMessage());
        }
    }

    /**
     * Map转实体对象（泛型支持）
     */
    public static <T> T convertToEntity(Map<String, Object> map, TypeReference<T> typeReference) {
        try {
            return defaultObjectSkipNullMapper.convertValue(map, typeReference);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Map转换实体失败: " + e.getMessage());
        }
    }

    /**
     * 将 JSON 字符串反序列化为对象
     */
    public static <T> T toObject(String json, Class<T> clazz) {
        try {
            return defaultObjectMapper.readValue(json, clazz);
        } catch (IOException e) {
            throw new IllegalArgumentException("JSON 反序列化失败: " + json, e);
        }
    }


    /**
     * 将 JSON流 字符串反序列化为对象
     */
    public static <T> T toObject(InputStream jsonStream, Class<T> clazz) {
        try {
            return defaultObjectMapper.readValue(jsonStream, clazz);
        } catch (IOException e) {
            throw new IllegalArgumentException("JSON 反序列化失败: ", e);
        }
    }

    /**
     * 将 JSON 字符串反序列化为复杂类型对象（如 List<MyClass>）
     */
    public static <T> T toObject(String json, TypeReference<T> typeReference) {
        try {
            return defaultObjectMapper.readValue(json, typeReference);
        } catch (IOException e) {
            throw new IllegalArgumentException("JSON 反序列化失败: " + json, e);
        }
    }

    /**
     * 将 JSON 字符串反序列化为 List 对象
     * 
     * @param json JSON 字符串
     * @param clazz List 元素的类型
     * @return List 对象
     */
    public static <T> java.util.List<T> toList(String json, Class<T> clazz) {
        try {
            return defaultObjectMapper.readValue(json, 
                    defaultObjectMapper.getTypeFactory().constructCollectionType(java.util.List.class, clazz));
        } catch (IOException e) {
            throw new IllegalArgumentException("JSON 反序列化为 List 失败: " + json, e);
        }
    }

    /**
     * 美化 JSON 输出
     */
    public static String prettyPrint(String json) {
        try {
            Object jsonObject = defaultObjectMapper.readValue(json, Object.class);
            return defaultObjectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonObject);
        } catch (IOException e) {
            throw new IllegalArgumentException("JSON 格式化失败: " + json, e);
        }
    }

    /**
     * 根据nodeName获取节点内容
     * @param nodeName
     * @param json
     * @return
     * @throws IOException
     */
    public static JsonNode getNodeByName(String nodeName, String json) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();

        return objectMapper.readTree(json).findPath(nodeName);
    }

    /**
     * 美化对象的 JSON 输出
     */
    public static String prettyPrint(Object object) {
        try {
            return defaultObjectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("对象格式化失败: " + object, e);
        }
    }


}
