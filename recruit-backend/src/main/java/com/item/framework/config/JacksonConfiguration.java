package com.item.framework.config;

import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.item.framework.constant.CommonConstants;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * @author : lh
 * 序列化Long2String 防止前端精度丢失
 */
@Configuration
public class JacksonConfiguration {

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jackson2ObjectMapperBuilderCustomizer() {
        return builder -> {
            // 把 Long 类型序列化为 String
            builder.serializerByType(Long.class, ToStringSerializer.instance);
            builder.serializerByType(Long.TYPE, ToStringSerializer.instance);
            //设置时间格式化  yyyy-MM-dd HH:mm:ss； yyyy-MM-dd ； HH:mm:ss
            //在此时这次修改格式改为 yyyy-MM-dd'T'HH:mm:ss'Z' 和 yabin和zhaoxuan确认
            builder.serializerByType(LocalDateTime.class, CommonConstants.LocalDateTimeConstant.LOCAL_DATE_TIME_SERIALIZER_T_Z);
            builder.serializerByType(LocalDate.class, CommonConstants.LocalDateTimeConstant.LOCAL_DATE_SERIALIZER);
            builder.serializerByType(LocalTime.class, CommonConstants.LocalDateTimeConstant.LOCAL_TIME_SERIALIZER);
        };
    }
}
