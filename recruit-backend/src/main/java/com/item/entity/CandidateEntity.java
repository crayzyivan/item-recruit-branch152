package com.item.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 候选人实体类
 * 对应数据库表：r_candidate
 */
@Data
@TableName("r_candidate")
public class CandidateEntity {
    
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 候选人姓名（全名）
     */
    private String candidateName;
    
    /**
     * 候选人邮箱
     */
    private String candidateEmail;

    /**
     * 候选人永久邮箱（不可变）
     */
    private String candidatePermanentEmail;
    
    /**
     * 密码
     */
    private String password;
    private String firstName;
    private String lastName;

    /**
     * 简历URL
     */
    @TableField(updateStrategy =FieldStrategy.ALWAYS)
    private String resumeUrl;
    
    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    /**
     * 电话号码
     */
    private String phoneNumber;
    
    /**
     * 扩展字段1
     */
    @TableField("ext_1")
    private String ext1;
    
    /**
     * 扩展字段2
     */
    @TableField("ext_2")
    private String ext2;
    
    /**
     * 逻辑删除标识（0：未删除，1：已删除）
     */
    @TableLogic
    private Integer deleted;
    private String middleName;
    private String gender;
    private LocalDate dateOfBirth;
    private String streetAddress;
    private String apartmentOrSuite;
    private Long countryId;
    private Long stateId;
    private Long cityId;
    private String countryName;
    private String stateName;
    private String cityName;
    private String postalCode;
    private Long educationId;
    private Long employmentId;
    private Long currencyTypeId;
    private Integer expectedSalary;
    private Long salaryTypeId;
    private LocalDate availableFrom;
    private Long candidateId;
    private Integer uploadStatus;
    
    /**
     * 密码是否已告知(0:未告知,1:已告知)
     */
    @TableField("is_password_told")
    private Integer isPasswordTold;
}