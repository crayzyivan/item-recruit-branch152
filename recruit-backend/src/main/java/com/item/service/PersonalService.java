package com.item.service;

import com.item.dto.PersonalUserRegisterReqDTO;
import com.item.dto.PersonalUserRegisterResDTO;
import com.item.framework.error.BusinessException;

/**
 * Personal C-end user service
 * Handles registration and management of candidate users
 * 
 * @author lh
 */
public interface PersonalService {
    
    /**
     * Register personal C-end user account
     * Automatically creates IAM account and local candidate record
     * 
     * @param request personal user registration request containing user information
     * @return PersonalUserRegisterResDTO containing candidateId and iamId
     * @throws BusinessException when registration fails (duplicate email, IAM error ,User name occupied, param exception, etc.) "code":"210010022","message":"User name occupied"
     */
    PersonalUserRegisterResDTO registerPersonalUser(PersonalUserRegisterReqDTO request) throws BusinessException;
}

