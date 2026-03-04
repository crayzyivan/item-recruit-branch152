package com.item.framework.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;

/**
 * Google Map API 配置类
 * 
 * @author haibin.bian
 * @since 2026-02-05
 */
@Data
@RefreshScope
@Configuration
@ConfigurationProperties(prefix = "google.map")
public class GoogleMapConfig {
    /**
     * Google Map API Key
     */
    private String apiKey;
    
    /**
     * Google Map Autocomplete API URL
     */
    private String autocompleteUrl = "https://maps.googleapis.com/maps/api/place/autocomplete/json";
    
    /**
     * Google Map Place Details API URL
     */
    private String placeDetailsUrl = "https://maps.googleapis.com/maps/api/place/details/json";
}
