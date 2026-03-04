package com.item.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 地理位置模糊搜索请求DTO
 * 
 * @author yarong.guo
 * @version 1.0
 * @since 2025-01-27
 */
@Data
public class LocationSearchDTO {
    
    /**
     * 搜索关键词
     */
    @NotBlank(message = "Search keyword cannot be empty")
    @Size(max = 100, message = "Search keyword must not exceed 100 characters")
    @JsonProperty("keyword")
    private String keyword;
    
    /**
     * 页码，从0开始
     */
    @Min(value = 0, message = "Page number must be greater than or equal to 0")
    @JsonProperty("page")
    private Integer page = 0;
    
    /**
     * 每页大小
     */
    @Min(value = 1, message = "Page size must be greater than 0")
    @Max(value = 100, message = "Page size must not exceed 100")
    @JsonProperty("size")
    private Integer size = 20;
    
    /**
     * 是否只搜索城市
     */
    @JsonProperty("cityOnly")
    private Boolean cityOnly = false;
    
    /**
     * 是否只搜索省份
     */
    @JsonProperty("stateOnly")
    private Boolean stateOnly = false;
    
    /**
     * 是否只搜索国家
     */
    @JsonProperty("countryOnly")
    private Boolean countryOnly = false;
}
