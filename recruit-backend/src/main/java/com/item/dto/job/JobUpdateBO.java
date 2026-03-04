package com.item.dto.job;

import com.item.entity.JobAuditHistoryEntity;
import com.item.util.CommonUtils;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author hua.liu
 */
@Data
public class JobUpdateBO {
    private Long jobId;
//    private String title;
    private Double longitude;
    private Double latitude;
    private Integer locationId;
    private Integer jobStatus;
    private Integer needListed;
    private Integer minSalary;
    private Integer maxSalary;
    private Integer categoryId;
    private Integer typeId;
    private Integer modeId;
    private Long updateBy;
    private String ext1;
    private String ext2;
    private String jobDetail;
    private List<String> minimumJobRequirement;
    private List<String> preferredJobRequirement;
    private List<String> mainDuty;
    private List<String> skills;
    private List<String> benefits;
    private Integer salaryType;
    private Integer currency;
    private Integer numberOpenings;
    private List<String> customQuestions;
    private Integer hotList;
    private String updateUser;
    private String locationName;
    private String jobStatusName;
    private String typeName;
    private String categoryName;
    private String modeName;
    private String companyTitleHash;
    private String customerName;
    private String urlCode;
    private String companyCode;
    private String logoPath;

    private String currencyName;
    private String salaryTypeName;
    /**
     * 面试时长
     */
    private Integer interviewLength;
    /**
     * 面试类型 (0:video, 1:audio, 2: AI phone)
     */
    private Integer interviewType;
    /**
     * 是否开启笔试
     */
    private Boolean enableWrittenTest;

    private String interviewUrlId;

    /**
     * 地点
     */
    private List<LocationValDTO> locations;

    /**
     * confirm save
     */
    private boolean confirmSave;

    /**
     * 智能判定开关
     */
    private Boolean intelligenceSwitch;

    /**
     * 智能评分规则列表
     */
    private List<IntelligenceScoreRuleDTO> scoreRules;

    private Boolean enableQuestion5STest;

    /**
     * 检查时间点功能开关
     */
    private Boolean checkpointEnabled;

    /**
     * 检查时间点（分钟）
     */
    private Integer checkpointTimeMinutes;

    /**
     * 检查时间点分数阈值
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

    public void fillLocationName(){
        if (CollectionUtils.isNotEmpty(this.getLocations())) {
            this.getLocations().forEach(l -> {
                if(StringUtils.isBlank(l.getLocationName())){
                    l.setLocationName(CommonUtils.getLocationName(l.getCountryName(), l.getStateName(), l.getCityName()));
                }
            });
        }
    }

    private LocalDateTime submittedForApprovalAt;

    private LocalDateTime approvedAt;

    private Long approvedBy;

    private LocalDateTime deniedAt;

    private Long deniedBy;

    private JobAuditHistoryBO jobAuditHistory;
}