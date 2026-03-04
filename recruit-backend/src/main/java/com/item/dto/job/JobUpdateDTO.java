package com.item.dto.job;

import com.item.framework.constant.InterviewTypeEnum;
import com.item.framework.constant.JobResponseCode;
import com.item.framework.error.BusinessException;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;

/**
 * @author hua.liu
 */
@Data
public class JobUpdateDTO {
    private Long jobId;
//    private String title;
    private Double longitude;
    private Double latitude;
    private Integer locationId;
    private String locationName;
    private Integer needListed = 1;
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
    /**
     * 地点
     */
    private List<LocationValDTO> locations;
    /**
     * 面试时长
     */
    private Integer interviewLength;

    /**
     * 面试类型 (0:video, 1:audio)
     */
    private Integer interviewType;

    /**
     * 是否开启笔试
     */
    private Boolean enableWrittenTest;

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

}