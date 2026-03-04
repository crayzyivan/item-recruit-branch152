package com.item.vo;

import com.item.dto.job.LocationValDTO;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class JobVO {

    private Long jobId;

    /**
     * 职位名称 必须
     */
    private String title;

    /**
     * 客户id 必须
     */
    private Long customerId;

    /**
     * 主账号id 必须
     */
    private Long masterAccountId;

    /**
     * 经度
     */
    private Double longitude;

    /**
     * 维度
     */
    private Double latitude;

    /**
     * 工作地点 必须
     */
    private String location;

    /**
     * 职位状态（必填）
     */
    private Integer jobStatus;

    /**
     * 是否需要显示在列表（必填）
     */
    private Integer needListed;

    /**
     * URL编码
     */
    private String urlCode;
    /**
     * 最小薪资
     */
    private Integer minSalary;
    /**
     * 最大薪资
     */
    private Integer maxSalary;
    /**
     * 职位类别ID
     */
    private Integer categoryId;
    /**
     * 职位类型ID
     */
    private Integer typeId;

    /**
     * 类型ID
     */
    private Integer modeId;

    /**
     * 创建人ID（必填）
     */
    private Long createBy;
    /**
     * 创建人（必填）
     */
    private String createUser;
    /**
     * 更新人ID
     */
    private Long updateBy;
    /**
     * 扩展字段1
     */
    private String ext1;
    /**
     * 扩展字段2
     */
    private String ext2;

    // 新增字段
    /**
     * 职位详情（必填）
     */
    private String jobDetail;
    /**
     * 职位要求
     */
    private List<String> minimumJobRequirement;

    /**
     * 职位要求
     */
    private List<String> preferredJobRequirement;
    /**
     * 主要职责（必填）
     */
    private List<String> mainDuty;
    /**
     * 技能（必填）
     */
    private List<String> skills;

    private LocalDateTime createTime;

    private long applicationCount;

    private String locationName;

    private Integer hotList;

    private String typeName;

    private String modeName;
    private List<LocationValDTO> locations;
    
    /**
     * Ayrshare分享状态：0-不需要分享，1-分享中，2-分享成功，3-部分成功，-1-分享失败
     */
    private Integer ayrshareStatus;

    /**
     * 服务器当前时间
     */
    private LocalDateTime currentDateTime = LocalDateTime.now();

    private String companyName;

    /**
     * 面试时长
     */
    private Integer interviewLength;

    /**
     * 面试类型 (0:video, 1:audio)
     */
    private Integer interviewType;

    private Long createUserId;

}