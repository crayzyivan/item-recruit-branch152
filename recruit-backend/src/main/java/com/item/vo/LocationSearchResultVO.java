package com.item.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.item.entity.CityEntity;
import com.item.entity.CountryEntity;
import com.item.entity.StateEntity;
import lombok.Data;

import java.util.function.Function;

/**
 * 地理位置模糊搜索结果VO
 * 
 * @author yarong.guo
 * @version 1.0
 * @since 2025-01-27
 */
@Data
public class LocationSearchResultVO {
    
    /**
     * 地理位置ID
     */
    @JsonProperty("id")
    private Long id;
    
    /**
     * 地理位置名称
     */
    @JsonProperty("name")
    private String name;
    
    /**
     * 匹配类型：city/state/country
     */
    @JsonProperty("matchType")
    private String matchType;
    
    /**
     * 匹配类型名称
     */
    @JsonProperty("matchTypeName")
    private String matchTypeName;
    
    /**
     * 完整地址（城市,省份,国家）
     */
    @JsonProperty("fullAddress")
    private String fullAddress;
    
    /**
     * 城市ID（如果是城市匹配）
     */
    @JsonProperty("cityId")
    private Long cityId;
    
    /**
     * 城市名称（如果是城市匹配）
     */
    @JsonProperty("cityName")
    private String cityName;
    
    /**
     * 省份ID
     */
    @JsonProperty("stateId")
    private Long stateId;
    
    /**
     * 省份名称
     */
    @JsonProperty("stateName")
    private String stateName;
    
    /**
     * 国家ID
     */
    @JsonProperty("countryId")
    private Long countryId;
    
    /**
     * 国家名称
     */
    @JsonProperty("countryName")
    private String countryName;
    
    /**
     * 经度
     */
    @JsonProperty("longitude")
    private Double longitude;
    
    /**
     * 纬度
     */
    @JsonProperty("latitude")
    private Double latitude;

    private Long currency;

    /**
     * 对应 Google Map Place Id
     */
    private String placeId;

    /**
     * 对应 Google Map Description
     */
    private String placeName;

    public void fillCityNameByLanguage(Function<CityEntity, String> functionCityName, CityEntity cityEntity) {
        this.setCityName(functionCityName.apply(cityEntity));
    }

    public void fillStateNameByLanguage(Function<StateEntity, String> functionStateName, StateEntity stateEntity) {
        this.setStateName(functionStateName.apply(stateEntity));
    }

    public void fillCountryNameByLanguage(Function<CountryEntity, String> functionCountryName, CountryEntity countryEntity) {
        this.setCountryName(functionCountryName.apply(countryEntity));
    }
}
