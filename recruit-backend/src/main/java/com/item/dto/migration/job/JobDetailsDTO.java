package com.item.dto.migration.job;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Job Details DTO
 * 
 * 用于解析 PostgreSQL jobs 表中的 details JSONB 字段。
 * 对应 JIRA 评论中提到的 JSON 结构。
 *
 * @author system
 * @since 2025-09-30
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class JobDetailsDTO {

    /**
     * 职位概述
     */
    @JsonProperty("overview")
    private String overview;

    /**
     * 技能要求列表
     */
    @JsonProperty("skills")
    private List<String> skills;

    /**
     * 福利待遇列表
     */
    @JsonProperty("benefits")
    private List<String> benefits;

    /**
     * 职责描述列表
     */
    @JsonProperty("responsibilities")
    private List<String> responsibilities;

    /**
     * 职位要求
     */
    @JsonProperty("requirements")
    private RequirementsDTO requirements;

    /**
     * 职位要求内部类
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RequirementsDTO {

        /**
         * 最低要求列表
         */
        @JsonProperty("minimum")
        private List<String> minimum;

        /**
         * 优先要求列表
         */
        @JsonProperty("preferred")
        private List<String> preferred;
    }
}
