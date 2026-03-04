package com.item.convert;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.data.convert.PropertyValueConverter;
import org.springframework.data.convert.ValueConversionContext;

/**
 * <p>
 *
 * </p>
 *
 * @author liuyabin on 2025/7/22
 * @since 1.0.0
 */
public class LocaDateTimeEsConverter implements PropertyValueConverter {

    @Override
    public Object read(Object value, ValueConversionContext context) {
        String dateTimeString = value.toString();
        return LocalDateTime.parse(dateTimeString, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    @Override
    public Object write(Object value, ValueConversionContext context) {
        if(value instanceof LocalDateTime) {

            LocalDateTime localDateTime = (LocalDateTime)value;

            return DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(localDateTime);
        }
        return  value;
    }
}
