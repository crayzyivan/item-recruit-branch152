package com.item.framework.constant;

import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author : lh
 */
@Getter
public enum JobIntelligenceSubStageEnum {
    /**
     *ASSESSMENT_SCORE, INTERVIEW, PROCTORING, TECHNICAL_SKILLS, SOFT_SKILLS, OVERALL
     */
    ASSESSMENT_SCORE("ASSESSMENT_SCORE", "Assessment Score stage", JobIntelligenceStageEnum.SCREENED),
    INTERVIEW("INTERVIEW", "Interview stage", JobIntelligenceStageEnum.AI_VETTED),
    PROCTORING("PROCTORING", "Proctoring stage", JobIntelligenceStageEnum.AI_VETTED),
    TECHNICAL_SKILLS("TECHNICAL_SKILLS", "Technical Skills stage", JobIntelligenceStageEnum.AI_VETTED),
    SOFT_SKILLS("SOFT_SKILLS", "Soft Skills stage", JobIntelligenceStageEnum.AI_VETTED),
    OVERALL("OVERALL", "Overall stage", JobIntelligenceStageEnum.AI_VETTED),

    ;

    private final String code;
    private final String description;
    private final JobIntelligenceStageEnum stage;

    JobIntelligenceSubStageEnum(String code, String description, JobIntelligenceStageEnum stage) {
        this.code = code;
        this.description = description;
        this.stage = stage;
    }
    private static final class Holder {
        private static final Map<String, JobIntelligenceSubStageEnum> MAP = Arrays.stream(values())
                .collect(Collectors.toUnmodifiableMap(JobIntelligenceSubStageEnum::getCode, Function.identity()));
    }

    /**
     * 通过 code 获取枚举
     */
    public static JobIntelligenceSubStageEnum getByCode(String code) {
        if (code == null) {
            return null;
        }
        return JobIntelligenceSubStageEnum.Holder.MAP.get(code);
    }
}
