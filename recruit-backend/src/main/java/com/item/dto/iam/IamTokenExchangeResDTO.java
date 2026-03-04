package com.item.dto.iam;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * IAM token exchange response DTO for cross-system session sync
 * Contains the exchanged access tokens and related information
 * 
 * @author : lh
 */
@Data
public class IamTokenExchangeResDTO {
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
