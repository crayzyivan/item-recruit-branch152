package com.item.dto.ayrshare;

import lombok.Data;

/**
 * Ayrshare成功发布的帖子ID信息DTO
 * 
 * @author lh
 * @since 1.0.0
 */
@Data
public class AyrSharePostIdDTO {
    
    /**
     * 状态
     */
    private String status;
    
    /**
     * 帖子ID
     */
    private String id;
    
    /**
     * 帖子URL
     */
    private String postUrl;
    
    /**
     * 平台名称
     */
    private String platform;
}
