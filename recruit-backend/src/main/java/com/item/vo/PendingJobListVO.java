package com.item.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Pending job approval list VO
 * Used specifically for displaying jobs pending approval
 *
 * @author system
 * @since 1.0.0
 */
@Data
public class PendingJobListVO implements Serializable {
    
    /**
     * Job ID
     */
    private Long jobId;
    
    /**
     * Job title
     */
    private String title;
    
    /**
     * Job status
     */
    private Integer jobStatus;
    
    /**
     * Job creation time
     */
    private LocalDateTime createTime;
    
    /**
     * Job creator name
     */
    private String createUser;
    
    /**
     * Location name
     */
    private String locationName;
    
    /**
     * Minimum salary
     */
    private Integer minSalary;
    
    /**
     * Maximum salary
     */
    private Integer maxSalary;
    
    /**
     * Company code
     */
    private String companyCode;
    
    /**
     * Company name
     */
    private String companyName;
    
    /**
     * Job category name
     */
    private String categoryName;
    
    /**
     * Job type name
     */
    private String typeName;
    
    /**
     * Job mode name (On-site, Remote, Hybrid, etc.)
     */
    private String modeName;
    
    /**
     * When job was submitted for approval
     */
    private LocalDateTime submittedForApprovalAt;
    
    /**
     * Number of openings
     */
    private Integer numberOpenings;
    
    /**
     * Salary type name
     */
    private String salaryTypeName;
    
    /**
     * Currency name
     */
    private String currencyName;
    
    /**
     * Approval comment (if any)
     */
    private String comment;
}
