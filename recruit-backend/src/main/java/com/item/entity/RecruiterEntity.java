package com.item.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("r_recruiter")
public class RecruiterEntity {
    @TableId
    private Long id;
    private String recruiterName;
    private String recruiterEmail;
    private String password;
    private String companyName;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

}