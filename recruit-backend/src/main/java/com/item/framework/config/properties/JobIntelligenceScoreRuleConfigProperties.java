package com.item.framework.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * @author : lh
 */
@Data
@RefreshScope
@Configuration
@ConfigurationProperties(prefix = "job.intelligence.score.rule")
public class JobIntelligenceScoreRuleConfigProperties {

    private List<ScoreRuleConfig> configs;
    private List<ScoreRuleMaxLimitConfig> maxLimitConfigs;

    @Data
    public static class ScoreRuleConfig {
        /**
         * 阶段代码 全局唯一
         * 如: SCREENED, AI_VETTED
         */
        private String stageCode;

        /**
         * 阶段顺序
         */
        private Integer stageOrder;

        /**
         * 子阶段代码 全局唯一
         * 如: ASSESSMENT_SCORE, INTERVIEW, PROCTORING, TECHNICAL_SKILLS, SOFT_SKILLS, OVERALL
         */
        private String subStageCode;

        /**
         * 子阶段顺序
         */
        private Integer subStageOrder;

        /**
         * 阈值最大限制
         */
        private Integer maxThresholdLimit;
    }


    @Data
    public static class ScoreRuleMaxLimitConfig {
        /**
         * 阶段代码
         * 如: SCREENED, AI_VETTED
         */
        private String stageCode;

        /**
         * 子阶段代码
         * 如: ASSESSMENT_SCORE, INTERVIEW, PROCTORING, TECHNICAL_SKILLS, SOFT_SKILLS, OVERALL
         */
        private String subStageCode;

        /**
         * 权重和限制
         */
        private Integer maxWeightSumLimit;
    }
}
