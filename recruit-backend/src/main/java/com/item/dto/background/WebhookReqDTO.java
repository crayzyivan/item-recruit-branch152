package com.item.dto.background;

import java.io.Serializable;

import lombok.Data;

/**
 * <p>
 *
 * </p>
 *
 * @author liuyabin on 2025/8/3
 * @since 1.0.0
 */
@Data
public class WebhookReqDTO implements Serializable {
    private String companyAccessCode;
    private String webhookUrl;
}
