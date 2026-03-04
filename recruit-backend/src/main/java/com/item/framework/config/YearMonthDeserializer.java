package com.item.framework.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.item.framework.constant.CommonConstants;

import java.io.IOException;
import java.time.YearMonth;

/**
 * @author : lh
 */
public class YearMonthDeserializer extends JsonDeserializer<YearMonth> {

    @Override
    public YearMonth deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        return YearMonth.parse(p.getText(), CommonConstants.LocalDateTimeConstant.LOCAL_YEAR_MONTH_FORMAT_FORMATTER);
    }
}
