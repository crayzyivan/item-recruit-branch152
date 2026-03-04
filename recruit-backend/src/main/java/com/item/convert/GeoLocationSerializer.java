package com.item.convert;

import co.elastic.clients.elasticsearch._types.GeoLocation;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

/**
 * @author : lh
 */
public class GeoLocationSerializer extends JsonSerializer<GeoLocation> {

    @Override
    public void serialize(GeoLocation value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        // 开始写入对象
        gen.writeStartObject();

        // 从GeoLocation中提取经纬度信息
        // 注意：GeoLocation可能有多种格式，这里处理最常见的lat/lon类型
        if (value.latlon() != null) {
            double lat = value.latlon().lat();
            double lon = value.latlon().lon();

            // 写入lat和lon字段，符合ES的地理位置格式要求
            gen.writeNumberField("lat", lat);
            gen.writeNumberField("lon", lon);
        } else if (value.geohash() != null) {
            // 处理geohash格式
            gen.writeStringField("geohash", value.geohash().geohash());
        } else {
            // 处理其他不常见格式
            throw new IOException("不支持的GeoLocation格式: " + value);
        }
        // 结束对象写入
        gen.writeEndObject();
    }
}
