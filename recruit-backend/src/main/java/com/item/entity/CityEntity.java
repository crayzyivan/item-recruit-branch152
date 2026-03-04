package com.item.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.sql.Timestamp;
import java.math.BigDecimal;

/**
 * 地理位置 市
 */
@Data
@TableName("r_cities")
public class CityEntity {
    @TableId
    private Long id;
    private String name;
    private Long stateId;
    private String stateCode;
    private Long countryId;
    private String countryCode;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private Integer flag;
    @TableField("wikiDataId")
    private String wikiDataId;
    private String chineseName;
    private String spanishName;
    private String japaneseName;
} 