package com.item.convert;

import co.elastic.clients.elasticsearch._types.GeoLocation;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;

/**
 * @author : lh
 */
public class GeoLocationDeserializer extends JsonDeserializer<GeoLocation> {

    @Override
    public GeoLocation deserialize(JsonParser p, DeserializationContext ctxt)
            throws IOException {
        JsonNode node = p.getCodec().readTree(p);

        // 处理ES支持的多种地理位置格式
        if (node.has("lat") && node.has("lon")) {
            // 格式1: { "lat": 40.7128, "lon": -74.0060 }
            double lat = node.get("lat").asDouble();
            double lon = node.get("lon").asDouble();
            return GeoLocation.of(g -> g.latlon(l -> l.lat(lat).lon(lon)));
        }
        else if (node.isArray() && node.size() == 2) {
            // 格式2: [lon, lat] (注意顺序是经度在前，纬度在后)
            double lon = node.get(0).asDouble();
            double lat = node.get(1).asDouble();
            return GeoLocation.of(g -> g.latlon(l -> l.lat(lat).lon(lon)));
        }
        else if (node.isTextual()) {
            String value = node.asText();
            // 格式3: "lat,lon" 字符串形式
            if (value.contains(",")) {
                String[] parts = value.split(",");
                if (parts.length == 2) {
                    double lat = Double.parseDouble(parts[0].trim());
                    double lon = Double.parseDouble(parts[1].trim());
                    return GeoLocation.of(g -> g.latlon(l -> l.lat(lat).lon(lon)));
                }
            }
            // 格式4: GeoHash字符串
            return GeoLocation.of(g -> g.geohash(l -> l.geohash(value)));
        }
        // 如果所有格式都不匹配，抛出异常
        throw new IOException("无法解析GeoLocation，不支持的格式: " + node.toString());
    }
}
