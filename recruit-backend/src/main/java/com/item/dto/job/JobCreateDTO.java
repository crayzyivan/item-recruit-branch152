package com.item.dto.job;

import com.item.framework.constant.CurrencyType;
import com.item.framework.constant.InterviewTypeEnum;
import com.item.framework.constant.JobResponseCode;
import com.item.framework.constant.SalaryType;
import com.item.framework.error.BusinessException;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;

@Data
public class JobCreateDTO {
    /**
     * 职位名称 必须
     */
    private String title;

    /**
     * 客户id 必须
     */
    private Long customerId = 0L;

    /**
     * 主账号id 必须
     */
    private Long masterAccountId = 0L;

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
    private Integer needListed = 1;

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
     * 职位类别ID 必须
     */
    private Integer categoryId;
    /**
     * 职位类型ID 必须
     */
    private Integer typeId;

    /**
     * mode ID 必须
     */
    private Integer modeId;

    /**
     * 创建人ID（必填）
     */
    private Long createBy;
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
    private String locationName;

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

    /**
     * 二次确认保存
     */
    private boolean confirmSave;

    /**
     * 面试类型 (0:video, 1:audio)
     */
    private Integer interviewType;

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
    private Integer checkpointScoreThreshold = 60;

    /**
     * 是否仅采用自定义问题
     */
    private Boolean customQuestionEnabled;

    /**
     * 是否启用性格测试
     */
    private Boolean personalityTestEnabled;


    public void checkJobRequirement() {
        if (CollectionUtils.isEmpty(minimumJobRequirement) && CollectionUtils.isEmpty(preferredJobRequirement)) {
            throw BusinessException.of(JobResponseCode.JOB_REQUIREMENT_EMPTY);
        }
        validateCheckpointConfig();
        validatePersonalityTestConfig();
        validateCustomQuestionConfig();
    }

    /**
     * 验证检查时间点配置
     */
    public void validateCheckpointConfig() {
        if (Boolean.TRUE.equals(checkpointEnabled)) {
            // 1. 仅视频面试支持
            if (interviewType != null && !interviewType.equals(InterviewTypeEnum.VIDEO.getCode())) {
                throw BusinessException.of(JobResponseCode.CHECKPOINT_ONLY_FOR_VIDEO);
            }

            // 2. 检查时间点必须 <= 面试总时长
            if (checkpointTimeMinutes == null || checkpointTimeMinutes > interviewLength || checkpointTimeMinutes <= 0) {
                throw BusinessException.of(JobResponseCode.CHECKPOINT_TIME_INVALID);
            }

            // 3. 分数阈值范围验证
            if (checkpointScoreThreshold == null
                || checkpointScoreThreshold < 0
                || checkpointScoreThreshold > 100) {
                throw BusinessException.of(JobResponseCode.CHECKPOINT_SCORE_INVALID);
            }
        }
    }

    /**
     * 目前仅 AI 视频面试支持性格测试
     */
    public void validatePersonalityTestConfig() {
        if (personalityTestEnabled != null && personalityTestEnabled && InterviewTypeEnum.VIDEO.getCode() != interviewType) {
            throw BusinessException.of(JobResponseCode.PERSONALITY_TEST_INVALID);
        }
    }

    /**
     * 全部启用自定义问题时，自定义问题不能为空
     */
    public void validateCustomQuestionConfig() {
        if (customQuestionEnabled != null && customQuestionEnabled && CollectionUtils.isEmpty(customQuestions)) {
            throw BusinessException.of(JobResponseCode.CUSTOM_QUESTION_INVALID);
        }
        if (customQuestionEnabled != null && customQuestionEnabled && customQuestions.size() > 20) {
            throw BusinessException.of(JobResponseCode.CUSTOM_QUESTION_SIZE_INVALID);
        }
    }

    public void trim() {
        this.setTitle(this.getTitle().trim());
    }
} 