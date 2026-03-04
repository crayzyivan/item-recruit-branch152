package com.item.framework.constant;

/**
 * 数据迁移业务类型枚举
 * 
 * 定义PostgreSQL到MySQL数据迁移过程中支持的业务表类型，
 * 用于标识不同业务数据的映射关系。
 *
 * @author system
 * @version 1.0
 * @since 2025-09-30
 */
public enum MigrationBusTypeEnum {
    
    /**
     * 职位表 (r_job)
     */
    JOB(1, "Job Table"),
    
    /**
     * 候选人表 (r_candidate)
     */
    CANDIDATE(2, "Candidate Table"),
    
    /**
     * 候选人职位关联表 (r_candidate_job)
     */
    CANDIDATE_JOB(3, "Candidate Job Table"),
    
    /**
     * 工作经历表 (r_employment_history)
     */
    EMPLOYMENT_HISTORY(4, "Employment History Table"),
    
    /**
     * 候选人教育背景表 (r_candidate_education)
     */
    CANDIDATE_EDUCATION(5, "Candidate Education Table"),

    /**
     * 面试报告表 (application_interview_reports)
     */
    INTERVIEW_REPORTS(6, "Interview Reports Table"),
    
    /**
     * 背景调查数据表 (r_background_data)
     */
    BACKGROUND_DATA(7, "Background Data Table"),

    /**
     * 公司数据 (companies.company_data)
     */
    COMPANY_DATA(8, "company data"),

    /**
     * 公司 ProfileKey 数据 (companies.company_data ayrshare_profile_key & linkedin_company_id)
     */
    COMPANY_PROFILE_KEY(9, "company profile key data"),

    /**
     * B端租户
     */
    RECRUIT(10, "recruit"),

    /**
     * ai筛选结果 (candidates.application_screening_reports)
     */
    SCREENING_REPORTS(11, "Screening Reports Table");

    ;
    private final Integer code;
    private final String description;

    /**
     * 构造函数
     *
     * @param code        业务类型编码
     * @param description 业务类型描述
     */
    MigrationBusTypeEnum(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 获取业务类型编码
     *
     * @return 业务类型编码
     */
    public Integer getCode() {
        return code;
    }

    /**
     * 获取业务类型描述
     *
     * @return 业务类型描述
     */
    public String getDescription() {
        return description;
    }

    /**
     * 根据编码获取枚举值
     *
     * @param code 业务类型编码
     * @return 对应的枚举值
     * @throws IllegalArgumentException 当编码无效时抛出异常
     */
    public static MigrationBusTypeEnum fromCode(Integer code) {
        if (code == null) {
            throw new IllegalArgumentException("MigrationBusType code cannot be null");
        }
        
        for (MigrationBusTypeEnum busType : values()) {
            if (busType.code.equals(code)) {
                return busType;
            }
        }
        
        throw new IllegalArgumentException("Invalid MigrationBusType code: " + code);
    }

    /**
     * 检查编码是否有效
     *
     * @param code 业务类型编码
     * @return 如果编码有效返回true，否则返回false
     */
    public static boolean isValidCode(Integer code) {
        if (code == null) {
            return false;
        }
        
        for (MigrationBusTypeEnum busType : values()) {
            if (busType.code.equals(code)) {
                return true;
            }
        }
        
        return false;
    }
}
