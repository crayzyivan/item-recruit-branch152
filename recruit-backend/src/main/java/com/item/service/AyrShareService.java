package com.item.service;

import com.item.dto.AyrShareRequestDTO;
import com.item.dto.ayrshare.AyrShareCreateUserProfileReqDTO;
import com.item.dto.ayrshare.AyrShareCreateUserProfileResDTO;
import com.item.dto.ayrshare.AyrShareGenerateJwtDTO;
import com.item.dto.ayrshare.AyrShareJwtResponseDTO;
import com.item.dto.ayrshare.AyrSharePostDTO;
import com.item.dto.ayrshare.AyrSharePostResDTO;
import com.item.dto.ayrshare.AyrShareUserResDTO;

import java.util.concurrent.CompletableFuture;

public interface AyrShareService {
    /**
     * 发送内容到Ayrshare平台
     * @param jsonBody JSON格式的请求体
     * @return 平台响应
     */
    String sendToAyrShare(AyrShareRequestDTO jsonBody);
    void asyncSendToAyrShare(AyrShareRequestDTO jsonBody);

    AyrShareJwtResponseDTO generateJWT(AyrShareGenerateJwtDTO jsonBody);

    AyrShareUserResDTO getUserProfileDetails();

    AyrShareCreateUserProfileResDTO createUserProfile(AyrShareCreateUserProfileReqDTO req);

    AyrShareUserResDTO getUserProfileDetails(String apiKey, String profileKey);

    AyrShareCreateUserProfileResDTO createUserProfile(String apiKey, AyrShareCreateUserProfileReqDTO req);

    /**
     * 使用CompletableFuture异步发送内容到Ayrshare平台，并更新Job状态
     * @param postDTO
     * @return CompletableFuture<Void>
     */
    CompletableFuture<Void> asyncSendToAyrShareWithConfig(AyrSharePostDTO postDTO);

    boolean isAllError(AyrSharePostResDTO res);

    boolean isAllSuccess(AyrSharePostResDTO res);

    boolean isPartialSuccess(AyrSharePostResDTO res);
}