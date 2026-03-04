package com.item.vo.background;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Date;

@Data
public class BackgroundGetTokenResponse {
    @JsonProperty("jwt")
    private String jwt;
    
    @JsonProperty("token")
    private String token;
    
    @JsonProperty("expiresOn")
    private Date expiresOn;

}