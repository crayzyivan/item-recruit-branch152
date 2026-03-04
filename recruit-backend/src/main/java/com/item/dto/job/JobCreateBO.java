package com.item.dto.job;

import com.item.framework.constant.CurrencyType;
import com.item.framework.constant.SalaryType;
import com.item.util.CommonUtils;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class JobCreateBO {
    /**
     * 职位名称 必须
     */

    private String title;

    /**
     * 客户id 必须
     */

    private Long customerId;

    /**
     * 客户name 必须
     */

    private String customerName;

    private String companyCode;

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

    /**
     * 好处 非必须 和页面上相关属性对应
     */
    private List<String> benefits;

    /**
     * 类型 必须 (1, "按小时"),
     * (2, "按天"),
     * (3, "按周"),
     * (4, "按月"),
     * (5, "按年"),
     * (6, "按提成"),
     * (7, "其他");
     * {@link SalaryType}
     */
    private Integer salaryType;

    /**
     * 货币 必须
     * <p>
     * (1, "USD", "US Dollar"),
     * (2, "GBP", "British Pound"),
     * (3, "EUR", "Euro"),
     * (4, "CAD", "Canadian Dollar"),
     * (5, "CNY", "Chinese Yuan"),
     * (6, "PHP", "Philippine Peso"),
     * {@link CurrencyType}
     */
    private Integer currency;

    /**
     * 空缺数量  非必须
     */
    private Integer numberOpenings;

    private List<String> customQuestions;

    private Integer hotList;

    private String updateUser;

    private String locationName;
    private String jobStatusName;
    private String typeName;
    private String categoryName;
    private String modeName;

    /**
     * Company name and title hash for uniqueness validation
     * Used to optimize uniqueness check performance
     */
    private String companyTitleHash;

    /**
     * ai面试id 更为唯一
     */
    private String interviewUrlId;

    private String logoPath;

    private String currencyName;

    private String salaryTypeName;

    /**
     * 地点
     */
    private List<LocationValDTO> locations;

    /**
     * 是否开启笔试
     */
    private Boolean enableWrittenTest;

    /**
     * 面试时长
     */
    private Integer interviewLength;

    private Integer ayrshareStatus;

    /**
     * 二次确认保存
     */
    private boolean confirmSave;

    /**
     * 面试类型 (0:video, 1:audio, 2: AI phone)
     */
    private Integer interviewType;
    
    /**
     * When job was submitted for approval
     */
    private LocalDateTime submittedForApprovalAt;

    /**
     * When job was approved
     */
    private LocalDateTime approvedAt;

    /**
     * Who approved the job
     */
    private Long approvedBy;

    /**
     * When job was denied
     */
    private LocalDateTime deniedAt;

    /**
     * Who denied the job
     */
    private Long deniedBy;

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
     * 检查时间点功能开关（仅视频面试可用）
     */
    private Boolean checkpointEnabled;

    /**
     * 检查时间点（分钟），必须 <= interviewLength
     */
    private Integer checkpointTimeMinutes;

    /**
     * 检查时间点分数阈值（0-100），默认60
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

}
