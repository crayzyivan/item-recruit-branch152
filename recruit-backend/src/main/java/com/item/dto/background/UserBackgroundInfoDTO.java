package com.item.dto.background;

import java.io.Serializable;

import lombok.Data;

/**
 * <p>
 * 用户背调信息
 * </p>
 *
 * @author liuyabin on 2025/8/3
 * @since 1.0.0
 */
@Data
public class UserBackgroundInfoDTO implements Serializable {
    private String userAccessCode;
    private String candidateId;
    private String jwtToken;
    private String uuidToken;
    //错误信息
    private String errorMessage;
}
