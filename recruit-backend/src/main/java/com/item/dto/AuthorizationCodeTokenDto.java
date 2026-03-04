package com.item.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * @author : lh
 */
@Data
public class AuthorizationCodeTokenDto {

    @JsonProperty("access_token")
    private String accessToken;
    @JsonProperty("refresh_token")
    private String refreshToken;
    @JsonProperty("token_type")
    private String tokenType;
    @JsonProperty("expires_in")
    private Long expiresIn;
    @JsonProperty("scope")
    private String scope;
    @JsonProperty("id_token")
    private String idToken;
}
