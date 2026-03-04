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
public class BackgroundPdfOrderDetailsDTO implements Serializable {
    private String userAccessCode;
    private String status;
    private String reportUUID;
    private String reportLink;
    private String expires;
}
