package com.item.vo.iam;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * IAM token exchange response VO for cross-system session sync
 * Used in Controller layer for API output
 * 
 * @author : lh
 */
@Data
public class IamTokenExchangeResVO {
    /**
     * The access token for API authentication
     */
    @JsonProperty("access_token")
    private String accessToken;
    
    /**
     * The type of token (typically "Bearer")
     */
    @JsonProperty("token_type")
    private String tokenType;
    
    /**
     * Token expiration time in seconds
     */
    @JsonProperty("expires_in")
    private Long expiresIn;
    
    /**
     * The refresh token for token renewal
     */
    @JsonProperty("refresh_token")
    private String refreshToken;
    
    /**
     * The scope of the access token (optional)
     */
    private String scope;
}
