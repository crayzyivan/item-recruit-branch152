package com.item.dto;

import com.item.framework.constant.AyrshareStatus;
import com.item.framework.constant.CommonConstants;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 *
 * </p>
 *
 * @author liuyabin on 2025/7/7
 * @since 1.0.0
 */
@Data
public class JobDto {
    private Long id;
    private String title;
    private Long customerId;
    private long masterAccountId;
    private BigDecimal longitude;
    private BigDecimal latitude;
    private Integer locationId;
    private String location;
    private Integer jobStatus;
    private List<Integer> qJobStatus;
    private Integer needListed;
    private String urlCode;
    private Integer typeId;
    private Integer categoryId;
    private long createBy;
    private String createUser;
    private long updateBy;
    private String ext1;
    private String ext2;
    private long applicationCount;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer jobTypeId;
    private List<Integer> qJobTypeIds;
    private JobTypeDto jobType;
    private Integer jobCategoryId;
    private JobCategoryDto jobCategory;
    private String logoUrl;
    private Integer modeId;
    private Boolean deleted;
    private Integer minSalary;
    private Integer maxSalary;
    private String locationName;
    //公司code
    private String companyCode;
    private Integer hotList;
    private String typeName;
    private String modeName;
    
    /**
     * Ayrshare分享状态：0-不需要分享，1-分享中，2-分享成功，3-部分成功，-1-分享失败
     */
    private Integer ayrshareStatus;
    
    /**
     * 薪资类型列表（用于多选查询）
     */
    private List<Integer> qSalaryTypes;
    
    /**
     * 职位类型ID列表（用于多选查询）- 新增支持
     */
    private List<Integer> qTypeIds;
    
    /**
     * 职位分类ID列表（用于多选查询）- 新增支持
     */
    private List<Integer> qCategoryIds;
    
    /**
     * 工作模式ID列表（用于多选查询）- 新增支持
     */
    private List<Integer> qModeIds;

    /**
     * 用户当前位置 - 纬度
     * 用于地理位置查询，范围: -90 到 90
     */
    private Double userLatitude;

    /**
     * 用户当前位置 - 经度
     * 用于地理位置查询，范围: -180 到 180
     */
    private Double userLongitude;

    /**
     * 搜索半径距离
     * 格式: 数字+单位，如 "10km", "5mi", "1000m"
     * 默认值: "50km"
     * 支持单位: km(千米), mi(英里), m(米)
     */
    private String searchRadius = CommonConstants.DEFAULT_SEARCH_RADIUS;

    /**
     * 是否按距离排序
     * true: 按距离从近到远排序
     * false: 使用默认排序（按 id 倒序）
     * 默认值: false
     */
    private Boolean sortByDistance = true;
}
