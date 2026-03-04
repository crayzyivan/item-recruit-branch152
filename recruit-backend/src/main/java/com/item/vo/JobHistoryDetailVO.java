package com.item.vo;

import com.item.dto.job.LocationDTO;
import lombok.Data;

import java.util.List;

/**
 * 历史职位详情响应VO
 *
 * @author hua.liu
 * @since 2025-08-26
 */
@Data
public class JobHistoryDetailVO {

    /**
     * 职位标题
     */
    private String title;

    /**
     * 工作模式ID
     */
    private Integer modeId;

    /**
     * 工作模式名称
     */
    private String modeName;

    /**
     * 最小薪资
     */
    private Integer minSalary;

    /**
     * 职位类别ID
     */
    private Integer categoryId;

    /**
     *
     */
    private String categoryName;

    /**
     * 最大薪资
     */
    private Integer maxSalary;

    /**
     * 职位类型ID
     */
    private Integer typeId;

    /**
     * 职位类型名称
     */
    private String typeName;

    /**
     * 职位详情
     */
    private String jobDetail;

    /**
     * 主要职责
     */
    private List<String> mainDuty;

    /**
     * 最低要求
     */
    private List<String> minimumJobRequirement;

    /**
     * 优选要求
     */
    private List<String> preferredJobRequirement;

    /**
     * 技能要求
     */
    private List<String> skills;

    /**
     * 福利待遇
     */
    private List<String> benefits;

    /**
     * 薪资类型
     */
    private Integer salaryType;

    /**
     * 薪资类型名称
     */
    private String salaryTypeName;

    /**
     * 货币类型
     */
    private Integer currency;

    /**
     * 货币简称
     */
    private String currencySimpleDesc;

    /**
     * 自定义问题
     */
    private List<String> customQuestions;


    /**
     * 地点
     */
    private List<LocationDTO> locations;

    /**
     * 是否开启笔试
     */
    private Boolean enableWrittenTest;

    private Integer numberOpenings;

    /**
     * 扩展字段1
     */
    private String ext1;
    /**
     * 扩展字段2
     */
    private String ext2;

    /**
     * 工作地点 必须
     */
    private Integer locationId;

    /**
     * 工作地点 必须
     */
    private String locationName;
    /**
     * 面试时长
     */
    private Integer interviewLength;
    /**
     * 面试类型 (0:video, 1:audio, 2:phone)
     */
    private Integer interviewType;

    /**
     * 智能判定开关
     * true: 启用智能评估
     * false/null: 禁用智能评估
     */
    private Boolean intelligenceSwitch;

    /**
     * 智能评分规则列表
     * 存储各阶段评估维度的权重和阈值配置
     */
    private List<IntelligenceScoreRuleVO> scoreRules;

    /**
     * 是否开启5S试题测试  默认false
     */
    private Boolean enableQuestion5STest;

    /**
     * 检查时间点功能开关（仅视频面试）
     * true: 启用检查时间点评估
     * false/null: 禁用检查时间点评估
     */
    private Boolean checkpointEnabled;

    /**
     * 检查时间点（分钟）
     * 面试进行到此时间点时，AI侧进行中期评估
     * 必须 <= interviewLength
     */
    private Integer checkpointTimeMinutes;

    /**
     * 检查时间点分数阈值（0-100）
     * 候选人在检查时间点的综合得分需达到此阈值才能继续面试
     * 默认值：60
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
}
