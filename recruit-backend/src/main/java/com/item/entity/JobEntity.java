package com.item.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * <p>
 *
 * </p>
 *
 * @author liuyabin on 2025/7/7
 * @since 1.0.0
 */
@Data
@TableName(value = "r_job")
public class JobEntity implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;
    @TableField(value = "title")
    private String title;
    @TableField(value = "customer_id")
    private Long customerId;
    @TableField(value = "master_account_id")
    private Long masterAccountId;
    @TableField(value = "longitude")
    private BigDecimal longitude;
    @TableField(value = "latitude")
    private BigDecimal latitude;
    @TableField(value = "location_id")
    private Integer locationId;
    @TableField(value = "job_status")
    private Integer jobStatus;
    @TableField(value = "need_listed")
    private Integer needListed;
    @TableField(value = "url_code")
    private String urlCode;
    @TableField(value = "type_id")
    private Integer typeId;
    @TableField(value = "category_id")
    private Integer categoryId;
    @TableField(value = "mode_id")
    private Integer modeId;
    @TableField(value = "create_by")
    private Long createBy;
    @TableField(value = "update_by")
    private Long updateBy;
    @TableField(value = "create_user")
    private String createUser;
    @TableField(value = "ext_1")
    private String ext1;
    @TableField(value = "ext_2")
    private String ext2;
    @TableField(value = "salary_type")
    private Integer salaryType;
    @TableField(value = "currency_type")
    private Integer currency;
    @TableField(value = "number_openings")
    private Integer numberOpenings;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    private Integer minSalary;
    private Integer maxSalary;
    private Integer hotList;
    @TableField("deleted")
    @TableLogic
    private Boolean deleted;
    @TableField(exist = false)
    private JobTypeEntity jobType;
    @TableField(exist = false)
    private JobCategoryEntity jobCategory;
    @TableField(exist = false)
    private JobModeEntity jobMode;
    private String locationName;

    /**
     * Company name and title hash for uniqueness validation
     * Used to optimize uniqueness check performance
     */
    @TableField(value = "company_title_hash")
    private String companyTitleHash;
    /**
     * ai面试id 更为唯一
     */
    private String interviewUrlId;

    @TableField(value = "company_code")
    private String companyCode;

   /**
    * Naukri Job ID (nullable)
    */
   @TableField(value = "naukri_job_id")
   private String naukriJobId;

    /**
     * 是否开启笔试
     */
    private Boolean enableWrittenTest;

    /**
     * 面试时长
     */
    private Integer interviewLength;

    /**
     * interview type (0:video, 1:audio)
     */
    @TableField(value = "interview_type")
    private Integer interviewType;

    /**
     * Ayrshare分享状态：0-不需要分享，1-分享中，2-分享成功，3-部分成功，-1-分享失败
     */
    @TableField(value = "ayrshare_status")
    private Integer ayrshareStatus;
    
    @TableField(value = "submitted_for_approval_at")
    private LocalDateTime submittedForApprovalAt;
    
    @TableField(value = "approved_at")
    private LocalDateTime approvedAt;
    
    @TableField(value = "approved_by")
    private Long approvedBy;
    
    @TableField(value = "denied_at")
    private LocalDateTime deniedAt;
    
    @TableField(value = "denied_by")
    private Long deniedBy;

	
}
