package com.item.service.impl;

import com.item.dto.PersonalUserRegisterReqDTO;
import com.item.dto.PersonalUserRegisterResDTO;
import com.item.dto.iam.IamCreateUserReqDTO;
import com.item.dto.iam.IamCreateUserResDTO;
import com.item.entity.CandidateEntity;
import com.item.framework.config.IamCommonConfig;
import com.item.framework.error.BusinessException;
import com.item.service.CandidateService;
import com.item.service.PersonalService;
import com.item.service.client.adapter.IamRpcAdapter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Personal C-end user service implementation
 * 
 * @author lh
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PersonalServiceImpl implements PersonalService {
    
    private final IamRpcAdapter iamRpcAdapter;
    private final CandidateService candidateService;
    private final IamCommonConfig iamCommonConfig;
    
    @Override
    public PersonalUserRegisterResDTO registerPersonalUser(PersonalUserRegisterReqDTO request) throws BusinessException {
        
        log.info("registerPersonalUser request", request);
        
        // Step 0: Validate required parameters
        request.validate();
        
        // Step 1: Build IAM create user request
        IamCreateUserReqDTO iamReq = new IamCreateUserReqDTO();
        iamReq.setUserName(request.getUserName());
        iamReq.setRawPassword(request.getRawPassword());
        iamReq.setFirstName(request.getFirstName());
        iamReq.setLastName(request.getLastName());
        iamReq.setEmail(request.getEmail());
        iamReq.setContactNumber(request.getContactNumber());
        
        // Step 2: Get configuration from nacos
        IamCommonConfig.RegisterCandidateConfig config = iamCommonConfig.getRegisterCandidate();
        iamReq.setCompanyCode(config.getBelong2CompanyCode());
        iamReq.setGrantedAppCodes(config.getGrantedAppCodes());
        
        log.info("registerPersonalUser calling IAM createUser with companyCode {} rantedAppCodes {}", config.getBelong2CompanyCode(), config.getGrantedAppCodes());
        
        // Step 3: Call IAM to create user
        IamCreateUserResDTO iamRes = iamRpcAdapter.createUser(iamReq);
        
        log.info("registerPersonalUser IAM user created successfully, iamId={}", iamRes.getId());
        
        // Step 4: Create CandidateEntity
        CandidateEntity candidate = new CandidateEntity();
        candidate.setCandidateName(request.getUserName());
        candidate.setCandidateEmail(request.getEmail());
        candidate.setCandidatePermanentEmail(request.getEmail());
        candidate.setPhoneNumber(request.getContactNumber());
        candidate.setCandidateId(Long.parseLong(iamRes.getId()));
        candidate.setFirstName(request.getFirstName());
        candidate.setLastName(request.getLastName());
        
        log.info("registerPersonalUser saving candidate={}", candidate);
        candidateService.save(candidate);
        
        // Step 5: Build response
        PersonalUserRegisterResDTO result = new PersonalUserRegisterResDTO();
        result.setCandidateId(candidate.getId());
        result.setIamId(iamRes.getId());
        
        log.info("registerPersonalUser completed successfully, candidateId={}, iamId={}", 
            result.getCandidateId(), result.getIamId());
        
        return result;
    }
}

