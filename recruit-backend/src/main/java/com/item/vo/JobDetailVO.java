package com.item.vo;

import com.item.dto.job.LocationDTO;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class JobDetailVO {

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
    private Integer locationId;

    /**
     * 工作地点 必须
     */
    private String locationName;

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
    private Integer salaryType;
    private Integer currency;
    private String currencySimpleDesc;
    private Integer numberOpenings;
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

    /**
     * 好处 非必须 和页面上相关属性对应
     */
    private List<String> benefits;

    private List<String> customQuestions;

    private String logoPath;

    /**
     * r_job_type中数据
     */
    private String typeName;

    /**
     * r_job_mode中数据
     */
    private String modeName;

    /**
     *
     */
    private String salaryTypeName;

    /**
     * r_job_category中数据
     */
    private String categoryName;

    /**
     * 地点
     */
    private List<LocationDTO> locations;

    /**
     * 面试时长
     */
    private Integer interviewLength;

    /**
     * 是否开启笔试
     */
    private Boolean enableWrittenTest;

    private String companyCode;

    private LocalDateTime createTime;

    private String slug;
    
    /**
     * Ayrshare分享状态：0-不需要分享，1-分享中，2-分享成功，3-部分成功，-1-分享失败
     */
    private Integer ayrshareStatus;

    /**
     * 服务器当前时间
     */
    private LocalDateTime currentDateTime = LocalDateTime.now();

    private Integer interviewType;

    /**
     * 智能判定开关
     */
    private Boolean intelligenceSwitch;

    /**
     * 智能评分规则列表
     */
    private List<IntelligenceScoreRuleVO> scoreRules;

    private Boolean enableQuestion5STest;

    private Boolean checkpointEnabled;

    /**
     * 检查时间点（分钟
     */
    private Integer checkpointTimeMinutes;

    /**
     * 检查时间点分数阈值（0-100）
     */
    private Integer checkpointScoreThreshold;

    /**
     * 是否仅采用自定义问题
     */
    private Boolean customQuestionEnabled;

    /**
     * 是否启用性格测试
     */
    private Boolean personalityTestEnabled;

    private LocalDateTime submittedForApprovalAt;

    private LocalDateTime approvedAt;

    private Long approvedBy;

    private LocalDateTime deniedAt;

    private Long deniedBy;

}