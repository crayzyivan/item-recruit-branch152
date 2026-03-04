package com.item.service.impl;

import com.item.dto.iam.IamUserDetailDTO;
import com.item.dto.iam.IamUserDetailResponseDTO;
import com.item.service.IamUserDetailService;
import com.item.service.client.adapter.IamRpcAdapter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/**
 * IAM用户详细信息服务实现类
 * 提供用户详细信息查询相关的业务逻辑实现
 *
 * @author lh
 * @version 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class IamUserDetailServiceImpl implements IamUserDetailService {
    
    private final IamRpcAdapter iamRpcAdapter;
    
    /**
     * 根据用户ID获取用户完整详细信息
     * 
     * @param userId 用户ID
     * @return 用户完整详细信息，如果用户不存在则返回null
     */
    @Override
    public IamUserDetailResponseDTO getUserDetailById(String userId) {
        log.info("getUserDetailById userId={}", userId);
        
        if (StringUtils.isBlank(userId)) {
            log.warn("getUserDetailById userId is blank");
            return null;
        }
        
        IamUserDetailResponseDTO userDetail = iamRpcAdapter.getUserDetailByIdentifier(userId);
        
        if (userDetail == null) {
            log.warn("User detail not found for userId: {}", userId);
            return null;
        }
        
        log.info("Successfully retrieved user detail: userId={}, userName={}, email={}", 
                userDetail.getId(), userDetail.getUserName(), userDetail.getEmail());
        
        return userDetail;
    }
    
    /**
     * 根据邮箱获取用户完整详细信息
     * 先通过邮箱查询用户基本信息获取用户ID，再获取完整详细信息
     * 
     * @param email 用户邮箱
     * @return 用户完整详细信息，如果用户不存在则返回null
     */
    @Override
    public IamUserDetailResponseDTO getUserDetailByEmail(String email) {
        log.info("getUserDetailByEmail email={}", email);
        
        if (StringUtils.isBlank(email)) {
            log.warn("getUserDetailByEmail email is blank");
            return null;
        }
        
        // 先通过邮箱查询用户基本信息
        IamUserDetailDTO userBasicInfo = iamRpcAdapter.getUserByIdentifier(email);
        if (userBasicInfo == null) {
            log.warn("User not found by email: {}", email);
            return null;
        }
        
        // 再通过用户ID获取完整详细信息
        String userId = userBasicInfo.getId();
        if (StringUtils.isBlank(userId)) {
            log.warn("User ID is blank for email: {}", email);
            return null;
        }
        
        return getUserDetailById(userId);
    }
    
    /**
     * 根据用户名获取用户完整详细信息
     * 先通过用户名查询用户基本信息获取用户ID，再获取完整详细信息
     * 
     * @param userName 用户名
     * @return 用户完整详细信息，如果用户不存在则返回null
     */
    @Override
    public IamUserDetailResponseDTO getUserDetailByUserName(String userName) {
        log.info("getUserDetailByUserName userName={}", userName);
        
        if (StringUtils.isBlank(userName)) {
            log.warn("getUserDetailByUserName userName is blank");
            return null;
        }
        
        // 先通过用户名查询用户基本信息
        IamUserDetailDTO userBasicInfo = iamRpcAdapter.getUserByIdentifier(userName);
        if (userBasicInfo == null) {
            log.warn("User not found by userName: {}", userName);
            return null;
        }
        
        // 再通过用户ID获取完整详细信息
        String userId = userBasicInfo.getId();
        if (StringUtils.isBlank(userId)) {
            log.warn("User ID is blank for userName: {}", userName);
            return null;
        }
        
        return getUserDetailById(userId);
    }
}
