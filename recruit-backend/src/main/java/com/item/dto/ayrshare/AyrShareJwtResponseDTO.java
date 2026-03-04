package com.item.dto.ayrshare;

import lombok.Data;

/**
 * AyrShare JWT生成响应DTO
 *
 * @author hua.liu
 * @since 2025-08-27
 */
@Data
public class AyrShareJwtResponseDTO {

    private String status;
    private String title;
    private String token;
    private String url;
    private Boolean emailSent;
    private String expiresIn;
}
