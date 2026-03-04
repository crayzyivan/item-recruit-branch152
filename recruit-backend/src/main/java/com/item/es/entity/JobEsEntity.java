package com.item.es.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.item.dto.job.IntelligenceScoreRuleDTO;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.item.dto.job.LocationValRecordDTO;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class JobEsEntity {
    private Long id;
    private Long masterAccountId;
    private String title;
    private String location;
    private String urlCode;
    
    /**
     * Company name and title hash for uniqueness validation
     * Used to optimize uniqueness check performance
     */
    private String companyTitleHash;
    private String companyCode;

    /**
     * 职位详情（必填）
     */

    private String jobDetail;
    /**
     * 职位要求（必填）
     */
    private List<String> minimumJobRequirement;

    /**
     * 职位要求（必填）
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

    /**
     * 其他ai提示问题
     */
    private List<String> customQuestions;

    private Long customerId;
    private String customerName;
    private java.math.BigDecimal longitude;
    private java.math.BigDecimal latitude;
    private Integer locationId;
    private String locationName;
    private Integer jobStatus;
    private String jobStatusName;
    private Integer needListed;
    private Integer typeId;
    private String typeName;
    private Integer categoryId;
    private String categoryName;
    private Integer modeId;
    private String modeName;
    private Long createBy;
    private Long updateBy;
    private String createUser;
    private String updateUser;
    private String ext1;
    private String ext2;
    private Integer salaryType;
    private String salaryTypeName;
    private Integer currency;
    private String currencyName;
    private Integer numberOpenings;
//    @ValueConverter(LocaDateTimeEsConverter.class)
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer minSalary;
    private Integer maxSalary;
    private Integer hotList;
    private Integer deleted;
    private String logoPath;
    /**
     * 面试时长
     */
    private Integer interviewLength;

    /**
     * 面试类型 (0:video, 1:audio)
     */
    private Integer interviewType;

    /**
     * 地点
     */
    private List<LocationValRecordDTO> locations;

    /**
     * Ayrshare分享状态：0-不需要分享，1-分享中，2-分享成功，3-部分成功，-1-分享失败
     */
    private Integer ayrshareStatus;

    /**
     * 智能判定开关
     * true: 启用智能评估
     * false/null: 禁用智能评估
     */
    private Boolean intelligenceSwitch;

    /**
     * 智能评分规则列表
     * 存储各阶段评估维度的权重和阈值配置
     */
    private List<IntelligenceScoreRuleDTO> scoreRules;

    /**
     * 是否需要做5S试题笔试
     */
    private Boolean enableQuestion5STest;

    /**
     * 检查时间点功能开关（仅视频面试）
     * true: 启用检查时间点评估
     * false/null: 禁用检查时间点评估
     */
    private Boolean checkpointEnabled;

    /**
     * 检查时间点（分钟）
     * 面试进行到此时间点时，AI侧进行中期评估
     * 必须 <= interviewLength
     */
    private Integer checkpointTimeMinutes;

    /**
     * 检查时间点分数阈值（0-100）
     * 候选人在检查时间点的综合得分需达到此阈值才能继续面试
     * 默认值：60
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

}