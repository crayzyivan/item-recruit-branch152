package com.item.vo.ai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * 更新面试类型响应VO
 *
 * @author yarong.guo
 * @version 1.0
 * @since 2025-01-27
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class UpdateInterviewTypeResponseVO {

    @JsonProperty("id")
    private String id;

    @JsonProperty("created_at")
    private String createdAt;

    @JsonProperty("name")
    private String name;

    @JsonProperty("description")
    private String description;

    @JsonProperty("language")
    private String language;

    @JsonProperty("organization_id")
    private String organizationId;

    @JsonProperty("user_id")
    private String userId;

    @JsonProperty("interviewer_id")
    private Integer interviewerId;

    @JsonProperty("is_active")
    private Boolean isActive;

    @JsonProperty("is_anonymous")
    private Boolean isAnonymous;

    @JsonProperty("is_archived")
    private Boolean isArchived;

    @JsonProperty("logo_url")
    private String logoUrl;

    @JsonProperty("theme_color")
    private String themeColor;

    @JsonProperty("url")
    private String url;

    @JsonProperty("readable_slug")
    private String readableSlug;

    @JsonProperty("questions")
    private List<QuestionGroup> questions;

    @JsonProperty("quotes")
    private Object quotes;

    @JsonProperty("insights")
    private Object insights;

    @JsonProperty("respondents")
    private Object respondents;

    @JsonProperty("question_count")
    private Integer questionCount;

    @JsonProperty("response_count")
    private Integer responseCount;

    @JsonProperty("time_duration")
    private String timeDuration;

    @JsonProperty("agent_name")
    private String agentName;

    @JsonProperty("can_change_interview_language")
    private Boolean canChangeInterviewLanguage;

    @JsonProperty("written_question_ids")
    private Object writtenQuestionIds;

    @JsonProperty("enable_written_test")
    private Boolean enableWrittenTest;

    @JsonProperty("interview_type")
    private String interviewType;

    /**
     * 问题组
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class QuestionGroup {
        @JsonProperty("problems")
        private List<Problem> problems;

        @JsonProperty("dimension")
        private String dimension;
    }

    /**
     * 问题
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Problem {
        @JsonProperty("id")
        private String id;

        @JsonProperty("question")
        private String question;

        @JsonProperty("follow_up_count")
        private Integer followUpCount;
    }
}
