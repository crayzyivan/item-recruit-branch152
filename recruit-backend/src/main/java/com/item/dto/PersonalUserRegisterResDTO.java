package com.item.dto;

import lombok.Data;

/**
 * Personal user registration response DTO
 * Contains IDs from both IAM and recruit systems
 * 
 * @author lh
 */
@Data
public class PersonalUserRegisterResDTO {
    
    /**
     * Recruit system candidate ID (auto-generated primary key) r_candidate.id
     */
    private Long candidateId;
    
    /**
     * IAM system user ID (returned from IAM createUser API)
     */
    private String iamId;
}

