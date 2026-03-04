package com.item.dto.job;

import com.item.framework.annotation.StringList;
import com.item.framework.annotation.Xss;
import static com.item.framework.constant.CommonConstants.NumConstants.JOB_FIELD_LENGTH_LIMIT;
import com.item.framework.constant.CurrencyType;
import com.item.framework.constant.SalaryType;
import com.item.vo.IntelligenceScoreRuleRequestDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class JobCreateRequestVO {
    private static final int LIMIT = JOB_FIELD_LENGTH_LIMIT;
    /**
     * 职位名称 必须
     */
    @NotBlank(message = "Job title is required")
    @Size(max = 200, message = "Job title must not exceed 200 characters")
    @Xss(message = "Job title some special characters, such as \"<\", \">\", \"<a>\", etc.")
    private String title;

//    /**
//     * 公司id 必须
//     */
//    @NotNull(message = "Customer ID is required")
//    private Long customerId;
//
//    /**
//     * 主账号id 必须
//     */
//    @NotNull(message = "Master account ID is required")
//    private Long masterAccountId;

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
//    @NotNull(message = "Location ID is required")
//    @Min(value = 1, message = "Location ID must be greater than 0")
    private Integer locationId;

//    @NotBlank(message = "Location Name is required")
    @Xss(message = "Location name some special characters, such as \"<\", \">\", \"<a>\", etc.")
    private String locationName;

    /**
     * 地点 必须 上限调整15
     */
    @Valid
    @NotNull(message = "Locations must not be null")
    @Size(min = 1, max = 15, message = "Locations size must be between {min} and {max}")
    private List<LocationValDTO> locations;

    /**
     * 职位状态（必填）
     */
//    @NotNull(message = "Job status is required")
//    private Integer jobStatus;

    /**
     * 是否需要显示在列表（必填）
     */
//    @NotNull(message = "Need listed flag is required")
//    private Integer needListed;

    /**
     * URL编码
     */
    @Xss(message = "Url code some special characters, such as \"<\", \">\", \"<a>\", etc.")
    private String urlCode;
    /**
     * 最小薪资
     */
    @NotNull(message = "Min salary must not be null")
    @Min(value = 1, message = "Min salary must be greater than or equal to {value}")
    @Max(value = Integer.MAX_VALUE, message = "Min salary must be less than or equal to {value}")
    private Integer minSalary;
    /**
     * 最大薪资
     */
    @NotNull(message = "Max salary must not be null")
    @Min(value = 1, message = "Max salary must be greater than or equal to {value}")
    @Max(value = Integer.MAX_VALUE, message = "Max salary must be less than or equal to {value}")
    private Integer maxSalary;
    /**
     * 领域类别ID 必须
     */
    @NotNull(message = "Category ID is required")
    private Integer categoryId;
    /**
     * 职位类型ID 必须
     * Full Time
     * Part Time
     * Contract
     * Temporary
     * Internship
     * Other
     */
    @NotNull(message = "Type ID is required")
    private Integer typeId;

    /**
     * 地点类型 必须
     * (1, "On-site"),
     * (2, "Remote"),
     * (3, "Hybrid"),
     * (4, "Other");
     */
//    @Min(value = 1, message = "Mode ID must be at least 1")
//    @Max(value = 4, message = "Mode ID must not exceed 4")
    @NotNull(message = "Mode ID is required")
    private Integer modeId;

    /**
     * 创建人ID（必填）
     */
    @NotNull(message = "Creator ID is required")
    private Long createBy;
    /**
     * 更新人ID
     */
    private Long updateBy;
    /**
     * 扩展字段1
     */
    @Xss(message = "Ext1 some special characters, such as \"<\", \">\", \"<a>\", etc.")
    private String ext1;
    /**
     * 扩展字段2
     */
    @Xss(message = "Ext2 some special characters, such as \"<\", \">\", \"<a>\", etc.")
    private String ext2;

    // 新增字段
    /**
     * 职位详情（必填）
     */
    @NotBlank(message = "Job overview is required")
    @Size(min = 100, max = 1500, message = "Job overview must be between {min} and {max} characters")
    @Xss(message = "Job overview some special characters, such as \"<\", \">\", \"<a>\", etc.")
    private String jobDetail;
    /**
     * 职位要求
     */
//    @Size(max = 6, message = "Minimum job requirements must not exceed 6 items")
    @StringList(maxSize = 6, sizeMessage = "Minimum job requirements must not exceed 6 items",
            minLength = 1, maxLength = LIMIT,
            lengthMessage = "Minimum job requirements element length must be between {minLength} and {maxLength}")
    @Xss(message = "Minimum job requirements some special characters, such as \"<\", \">\", \"<a>\", etc.")
    private List<String> minimumJobRequirement;

    /**
     * 职位要求
     */
//    @Size(max = 6, message = "Preferred job requirements must not exceed 6 items")
    @StringList(maxSize = 6, sizeMessage = "Preferred job requirements must not exceed 6 items",
            minLength = 1, maxLength = LIMIT,
            lengthMessage = "Preferred job requirements element length must be between {minLength} and {maxLength}")
    @Xss(message = "Preferred job requirements some special characters, such as \"<\", \">\", \"<a>\", etc.")
    private List<String> preferredJobRequirement;
    /**
     * 主要职责（必填）
     */
    @NotNull(message = "Responsibilities are required")
//    @Size(min = 1, max = 20, message = "Responsibilities must contain between 1 and 20 items")
    @StringList(minSize = 1, maxSize = 20, sizeMessage = "Responsibilities must contain between 1 and 20 items",
            minLength = 1, maxLength = LIMIT,
            lengthMessage = "Responsibilities element length must be between {minLength} and {maxLength}")
    @Xss(message = "Responsibilities some special characters, such as \"<\", \">\", \"<a>\", etc.")
    private List<String> mainDuty;
    /**
     * 技能（必填）
     */
    @NotNull(message = "Skills are required")
//    @Size(min = 1, max = 5, message = "Skills must contain between 1 and 5 items")
    @StringList(minSize = 1, maxSize = 5, sizeMessage = "Skills must contain between 1 and 5 items",
            minLength = 1, maxLength = LIMIT,
            lengthMessage = "Skills element length must be between {minLength} and {maxLength}")
    @Xss(message = "Skills some special characters, such as \"<\", \">\", \"<a>\", etc.")
    private List<String> skills;

    /**
     * 好处 非必须 和页面上相关属性对应
     */
//    @Size(max = 5, message = "Benefits must not exceed 5 items")
    @StringList(maxSize = 5, sizeMessage = "Benefits must not exceed 5 items",
            minLength = 1, maxLength = LIMIT,
            lengthMessage = "Benefits element length must be between {minLength} and {maxLength}")
    @Xss(message = "Benefits some special characters, such as \"<\", \">\", \"<a>\", etc.")
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
    @NotNull(message = "Salary type is required")
//    @Min(value = 1, message = "Salary type must be at least 1")
//    @Max(value = 7, message = "Salary type must not exceed 7")
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
//    @NotNull(message = "Currency is required")
//    @Min(value = 5, message = "Currency must be at least 5")
    private Integer currency;

    /**
     * 空缺数量  非必须
     */
    private Integer numberOpenings;

//    @Size(max = 5, message = "Custom questions must not exceed 5 items")
    @StringList(maxSize = 20, sizeMessage = "Custom questions must not exceed 20 items",
            minLength = 1, maxLength = LIMIT,
            lengthMessage = "Custom questions element length must be between {minLength} and {maxLength}")
    @Xss(message = "Custom questions some special characters, such as \"<\", \">\", \"<a>\", etc.")
    private List<String> customQuestions;

    /**
     * 0false 1true
     */
    @NotNull(message = "Hot list flag is required")
    @Min(value = 0, message = "Hot list flag must be 0 or 1")
    @Max(value = 1, message = "Hot list flag must be 0 or 1")
    private Integer hotList;

    /**
     * 是否开启笔试
     */
    private Boolean enableWrittenTest;

    /**
     * 面试时长 单位分钟
     */
    @Min(value = 3, message = "Interview length must be greater than or equal to {value}")
    @Max(value = 60, message = "Interview length must be less than or equal to {value}")
    @NotNull(message = "Interview length must not be null")
    private Integer interviewLength;

    /**
     * confirm save
     */
    private boolean confirmSave;

    /**
     * 面试类型 (0:video, 1:audio, 2:AI phone)
     */
    //@NotNull(message = "Interview type must not be null")
    @Min(value = 0, message = "Interview type must be 0 , 1 or 2")
    @Max(value = 2, message = "Interview type must be 0 , 1 or 2")
    private Integer interviewType;

    /**
     * 智能判定开关
     */
    @NotNull(message = "Intelligence Switch is required")
    private Boolean intelligenceSwitch;

    /**
     * 智能评分规则列表
     */
    @Valid
    private List<IntelligenceScoreRuleRequestDTO> scoreRules;

    /**
     * 是否开启5S试题测试  默认false
     */
    private Boolean enableQuestion5STest;

    private Boolean checkpointEnabled;

    /**
     * 检查时间点（分钟），必须 <= interviewLength
     */
    @Min(value = 0, message = "Checkpoint time must greater than 0 minutes")
    private Integer checkpointTimeMinutes;

    /**
     * 检查时间点分数阈值（0-100），默认60
     */
    @Min(value = 0, message = "Checkpoint score threshold must between 0-100")
    @Max(value = 100, message = "Checkpoint score threshold must between 0-100")
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