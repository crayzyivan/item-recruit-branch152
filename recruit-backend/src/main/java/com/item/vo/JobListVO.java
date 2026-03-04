package com.item.vo;

import com.item.dto.FullLocationDTO;
import com.item.dto.job.LocationValDTO;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * job列表页单条数据对象
 * </p>
 *
 * @author liuyabin on 2025/7/22
 * @since 1.0.0
 */
@Data
public class JobListVO implements Serializable {
    private Long jobId;
    private String title;
    private Integer hotList;
    private Long jobStatus;
    private Long applicationCounts;
    private LocalDateTime createTime;
    private String createUser;
    private String locationName;
    private String modeName;
    private LocalDateTime updateTime;
    private String categoryName;
    private String typeName;
    private String customerName;
    private String logoUrl;
    private Integer minSalary;
    private Integer maxSalary;
    private String salaryTypeName;
    private Integer salaryType;
    private String currencyName;
    private FullLocationDTO fullLocationDTO;
    private List<LocationValDTO> locations;

    private String companyName;
    private String companyWebsite;

    private String jobDetail;
    private Integer categoryId;
    private Integer typeId;
    private Integer modeId;
    private Integer currency;
    
    /**
     * Ayrshare分享状态：0-不需要分享，1-分享中，2-分享成功，3-部分成功，-1-分享失败
     */
    private Integer ayrshareStatus;

    /**
     * 服务器当前时间
     */
    private LocalDateTime currentDateTime = LocalDateTime.now();

    /**
     * 面试时长
     */
    private Integer interviewLength;

    /**
     * 面试类型 (0:video, 1:audio)
     */
    private Integer interviewType;

    /**
     * 智能判定开关
     */
    private Boolean intelligenceSwitch;

    /**
     * 距离用户位置的距离（单位：公里）
     * 仅在使用地理位置查询时返回
     * 格式: 保留2位小数，如 "3.45"
     */
    private String distance;

    /**
     * 距离单位
     * 默认: "km"
     */
    private String distanceUnit;

    private Long createUserId;

}
