package com.item.service;

import com.item.dto.iam.IamUserDetailResponseDTO;

/**
 * IAM用户详细信息服务接口
 * 提供用户详细信息查询相关的业务逻辑
 *
 * @author lh
 * @version 1.0
 */
public interface IamUserDetailService {
    
    /**
     * 根据用户ID获取用户完整详细信息
     * 包含用户基本信息、角色、权限、应用、公司等完整数据
     * 
     * @param userId 用户ID
     * @return 用户完整详细信息，如果用户不存在则返回null
     */
    IamUserDetailResponseDTO getUserDetailById(String userId);
    
    /**
     * 根据邮箱获取用户完整详细信息
     * 先通过邮箱查询用户ID，再获取完整详细信息
     * 
     * @param email 用户邮箱
     * @return 用户完整详细信息，如果用户不存在则返回null
     */
    IamUserDetailResponseDTO getUserDetailByEmail(String email);
    
    /**
     * 根据用户名获取用户完整详细信息
     * 先通过用户名查询用户ID，再获取完整详细信息
     * 
     * @param userName 用户名
     * @return 用户完整详细信息，如果用户不存在则返回null
     */
    IamUserDetailResponseDTO getUserDetailByUserName(String userName);
}
