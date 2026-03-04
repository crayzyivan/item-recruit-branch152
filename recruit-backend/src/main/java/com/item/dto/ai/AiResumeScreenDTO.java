package com.item.dto.ai;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class AiResumeScreenDTO {
    /** 职位名称 */
    private String jobTitle;

    /** 技能列表 */
    private List<String> skills;

    /** 最低学历要求 */
    private List<String> minimumRequirements;

    /** 岗位职责 */
    private List<String> responsibilities;

    /** 工作经验（年限） */
    private String experience;

    /** 工作地点 */
    private String location;

    /** 最低薪资 */
    private String minimumSalary;

    /** 最高薪资 */
    private String maximumSalary;

    /** 语言（如：en、zh、es等） */
    private String language;

    /** 简历文本内容 */
    private String resume;
}
