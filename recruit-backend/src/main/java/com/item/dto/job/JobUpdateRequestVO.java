package com.item.dto.job;

import com.item.framework.annotation.StringList;
import com.item.framework.annotation.Xss;
import static com.item.framework.constant.CommonConstants.NumConstants.JOB_FIELD_LENGTH_LIMIT;
import com.item.vo.IntelligenceScoreRuleRequestDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * @author hua.liu
 */
@Data
public class JobUpdateRequestVO {
    private static final int LIMIT = JOB_FIELD_LENGTH_LIMIT;

    @NotNull(message = "Job ID is required")
    @Min(value = 1, message = "Job ID must be greater than 0")
    private Long jobId;

    //title不支持修改
//    @NotBlank(message = "Job title is required")
//    @Size(max = 200, message = "Job title must not exceed 200 characters")
//    private String title;

//    @NotNull(message = "Customer ID is required")
//    private Long customerId;

//    @NotNull(message = "Master account ID is required")
//    private Long masterAccountId;

    private Double longitude;
    private Double latitude;

//    @NotNull(message = "Location ID is required")
//    @Min(value = 1, message = "Location ID must be greater than 0")
    private Integer locationId;

//    @NotNull(message = "Location name is required")
    @Xss(message = "Location name some special characters, such as \"<\", \">\", \"<a>\", etc.")
    private String locationName;

    /**
     * 地点 必须 上限调整15
     */
    @Valid
    @NotNull(message = "Locations must not be null")
    @Size(min = 1, max = 15, message = "Locations size must be between {min} and {max}")
    private List<LocationValDTO> locations;

//    @NotNull(message = "Need listed flag is required")
//    private Integer needListed;

    @NotNull(message = "Min salary must not be null")
    @Min(value = 1, message = "Min salary must be greater than or equal to {value}")
    @Max(value = Integer.MAX_VALUE, message = "Min salary must be less than or equal to {value}")
    private Integer minSalary;
    @NotNull(message = "Max salary must not be null")
    @Min(value = 1, message = "Max salary must be greater than or equal to {value}")
    @Max(value = Integer.MAX_VALUE, message = "Max salary must be less than or equal to {value}")
    private Integer maxSalary;

    @NotNull(message = "Category ID is required")
    private Integer categoryId;

    @NotNull(message = "Type ID is required")
    private Integer typeId;

//    @Min(value = 1, message = "Mode ID must be at least 1")
//    @Max(value = 4, message = "Mode ID must not exceed 4")
    @NotNull(message = "Mode ID is required")
    private Integer modeId;

//    @NotNull(message = "Update ID is required")
//    private Long updateBy;
    @Xss(message = "Ext1 some special characters, such as \"<\", \">\", \"<a>\", etc.")
    private String ext1;
    @Xss(message = "Ext2 some special characters, such as \"<\", \">\", \"<a>\", etc.")
    private String ext2;

    @NotBlank(message = "Job overview is required")
    @Size(min = 100, max = 1500, message = "Job overview must be between {min} and {max} characters")
    @Xss(message = "Job overview some special characters, such as \"<\", \">\", \"<a>\", etc.")
    private String jobDetail;

//    @Size(max = 6, message = "Minimum job requirements must not exceed 6 items")
    @StringList(maxSize = 6, sizeMessage = "Minimum job requirements must not exceed 6 items",
            minLength = 1, maxLength = LIMIT,
            lengthMessage = "Minimum job requirements element length must be between {minLength} and {maxLength}")
    @Xss(message = "Minimum job requirements some special characters, such as \"<\", \">\", \"<a>\", etc.")
    private List<String> minimumJobRequirement;

//    @Size(max = 6, message = "Preferred job requirements must not exceed 6 items")
    @StringList(maxSize = 6, sizeMessage = "Preferred job requirements must not exceed 6 items",
            minLength = 1, maxLength = LIMIT,
            lengthMessage = "Preferred job requirements element length must be between {minLength} and {maxLength}")
    @Xss(message = "Preferred job requirements some special characters, such as \"<\", \">\", \"<a>\", etc.")
    private List<String> preferredJobRequirement;

    @NotNull(message = "Responsibilities are required")
    @StringList(minSize = 1, maxSize = 20, sizeMessage = "Responsibilities must contain between 1 and 20 items",
            minLength = 1, maxLength = LIMIT,
            lengthMessage = "Responsibilities element length must be between {minLength} and {maxLength}")
    @Xss(message = "Responsibilities some special characters, such as \"<\", \">\", \"<a>\", etc.")
    private List<String> mainDuty;

    @NotNull(message = "Skills are required")
//    @Size(min = 1, max = 5, message = "Skills must contain between 1 and 5 items")
    @StringList(minSize = 1, maxSize = 5, sizeMessage = "Skills must contain between 1 and 5 items",
            minLength = 1, maxLength = LIMIT,
            lengthMessage = "Skills element length must be between {minLength} and {maxLength}")
    @Xss(message = "Skills some special characters, such as \"<\", \">\", \"<a>\", etc.")
    private List<String> skills;

//    @Size(max = 5, message = "Benefits must not exceed 5 items")
    @StringList(maxSize = 5, sizeMessage = "Benefits must not exceed 5 items",
        minLength = 1, maxLength = LIMIT,
        lengthMessage = "Benefits element length must be between {minLength} and {maxLength}")
    @Xss(message = "Benefits some special characters, such as \"<\", \">\", \"<a>\", etc.")
    private List<String> benefits;

    @NotNull(message = "Salary type is required")
    private Integer salaryType;

//    @Min(value = 1, message = "Currency must be at least 1")
//    @Max(value = 7, message = "Currency must not exceed 7")
    private Integer currency;

    private Integer numberOpenings;

//    @Size(max = 5, message = "Custom questions must not exceed 5 items")
    @StringList(maxSize = 20, sizeMessage = "Custom questions must not exceed 20 items",
            minLength = 1, maxLength = LIMIT,
            lengthMessage = "Custom questions element length must be between {minLength} and {maxLength}")
    @Xss(message = "Custom questions some special characters, such as \"<\", \">\", \"<a>\", etc.")
    private List<String> customQuestions;

    /**
     * 面试时长  单位分钟
     */
    @Min(value = 3, message = "Interview length must be greater than or equal to {value}")
    @Max(value = 60, message = "Interview length must be less than or equal to {value}")
    @NotNull(message = "Interview length must not be null")
    private Integer interviewLength;

    /**
     * 面试类型 (0:video, 1:audio, 2:AI phone)
     */
    //@NotNull(message = "Interview type must not be null")
    @Min(value = 0, message = "Interview type must be 0 , 1 or 2")
    @Max(value = 2, message = "Interview type must be 0 , 1 or 2")
    private Integer interviewType;

    /**
     * 是否开启笔试
     */
    private Boolean enableWrittenTest;
//    @NotNull(message = "Hot list flag is required")
//    @Min(value = 0, message = "Hot list flag must be 0 or 1")
//    @Max(value = 1, message = "Hot list flag must be 0 or 1")
//    private Integer hotList;

    /**
     * confirm save
     */
    private boolean confirmSave;

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