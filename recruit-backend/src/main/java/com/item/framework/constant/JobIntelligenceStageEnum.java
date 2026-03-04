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
public enum JobIntelligenceStageEnum {
    /**
     * SCREENED, AI_VETTED
     */
    SCREENED("SCREENED", "Screened stage"),
    AI_VETTED("AI_VETTED", "AI vetted stage"),

    ;

    private final String code;
    private final String description;

    JobIntelligenceStageEnum(String code, String description) {
        this.code = code;
        this.description = description;
    }
    private static final class Holder {
        private static final Map<String, JobIntelligenceStageEnum> MAP = Arrays.stream(values())
                .collect(Collectors.toUnmodifiableMap(JobIntelligenceStageEnum::getCode, Function.identity()));
    }

    /**
     * 通过 code 获取枚举
     */
    public static JobIntelligenceStageEnum getByCode(String code) {
        if (code == null) {
            return null;
        }
        return JobIntelligenceStageEnum.Holder.MAP.get(code);
    }
}
