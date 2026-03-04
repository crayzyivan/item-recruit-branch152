package com.item.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RecruiterDTO {
    private Long id;
    private String recruiterName;
    private String recruiterEmail;
    private String password;
    private String companyName;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

}