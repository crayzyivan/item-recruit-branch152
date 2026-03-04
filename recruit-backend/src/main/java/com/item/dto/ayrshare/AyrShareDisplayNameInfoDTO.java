package com.item.dto.ayrshare;

import lombok.Data;

/**
 * Ayrshare显示名称信息DTO
 *
 * @author lh
 */
@Data
public class AyrShareDisplayNameInfoDTO {
    
    /**
     * 创建时间
     */
    private String created;
    
    /**
     * 显示名称
     */
    private String displayName;
    
    /**
     * ID
     */
    private String id;
    
    /**
     * 页面名称
     */
    private String pageName;
    
    /**
     * 平台
     */
    private String platform;
    
    /**
     * 个人资料URL
     */
    private String profileUrl;
    
    /**
     * 用户ID
     */
    private String userId;
    
    /**
     * 用户头像
     */
    private String userImage;
}
