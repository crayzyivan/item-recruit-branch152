package com.item.dto.ayrshare;

import lombok.Data;

/**
 * Ayrshare错误信息DTO
 * 
 * @author lh
 * @since 1.0.0
 */
@Data
public class AyrShareErrorDTO {
    
    /**
     * 操作类型
     */
    private String action;
    
    /**
     * 状态
     */
    private String status;
    
    /**
     * 错误代码
     */
    private Integer code;
    
    /**
     * 错误消息
     */
    private String message;
    
    /**
     * 平台名称
     */
    private String platform;
}
