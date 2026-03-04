package com.item.vo.ayrshare;

import lombok.Data;

/**
 * AyrShare JWT生成响应VO
 *
 * @author hua.liu
 * @since 2025-08-27
 */
@Data
public class AyrShareJwtResponseVO {
    private String title;
    private String token;
    private String url;
    private Boolean emailSent;
    private String expiresIn;
}
