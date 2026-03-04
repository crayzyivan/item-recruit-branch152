package com.item.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.sql.Timestamp;
import java.math.BigDecimal;
/**
 * 地理位置 国家
 */
@Data
@TableName("r_countries")
public class CountryEntity {
    @TableId
    private Long id;
    private String name;
    private String iso3;
    private String numericCode;
    private String iso2;
    private String phonecode;
    private String capital;
    private String currency;
    private String currencyName;
    private String currencySymbol;
    private String tld;
    @TableField("native")
    private String nativeName;
    private String region;
    private Long regionId;
    private String subregion;
    private Long subregionId;
    private String nationality;
    private String timezones;
    private String translations;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String emoji;
    @TableField("emojiU")
    private String emojiU;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private Integer flag;
    @TableField("wikiDataId")
    private String wikiDataId;
    private String chineseName;
    private String spanishName;
    private String japaneseName;
} 