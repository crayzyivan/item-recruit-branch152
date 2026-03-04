package com.item.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.item.framework.annotation.Xss;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

/**
 * 邀请面试教育经历VO
 * 用于接收前端调用"/resume-parsing"接口后的教育经历解析结果
 *
 * @since 2025-11-13
 */
@Data
public class InviteInterviewEducationVO {
    
    /**
     * 机构类型ID（可选）
     */
    @JsonProperty("institutionType")
    private Long institutionTypeId;
    
    /**
     * 机构名称（可选）
     */
    @Size(max = 200, message = "Institution name length must be less than or equal to 200")
    @Xss(message = "Institution name some special characters, such as \"<\", \">\", \"<a>\", etc.")
    private String institutionName;
    
    /**
     * 是否毕业（可选）
     */
    private Integer graduated;
    
    /**
     * 开始日期（可选）
     */
    private LocalDate startDate;
    
    /**
     * 结束日期（可选）
     */
    private LocalDate endDate;
    
    /**
     * 学位ID（可选）
     */
    @JsonProperty("degree")
    private Long degreeId;
    
    /**
     * 专业（可选）
     */
    @Size(max = 200, message = "Major length must be less than or equal to 200")
    @Xss(message = "Major some special characters, such as \"<\", \">\", \"<a>\", etc.")
    private String major;
    
    /**
     * 辅修（可选）
     */
    @Size(max = 200, message = "Minor length must be less than or equal to 200")
    @Xss(message = "Minor some special characters, such as \"<\", \">\", \"<a>\", etc.")
    private String minor;
}

