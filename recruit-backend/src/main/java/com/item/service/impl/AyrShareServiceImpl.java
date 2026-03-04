package com.item.service.impl;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.item.dto.AyrShareRequestDTO;
import com.item.dto.ayrshare.AyrShareCreateUserProfileReqDTO;
import com.item.dto.ayrshare.AyrShareCreateUserProfileResDTO;
import com.item.dto.ayrshare.AyrShareGenerateJwtDTO;
import com.item.dto.ayrshare.AyrShareJwtResponseDTO;
import com.item.dto.ayrshare.AyrSharePostDTO;
import com.item.dto.ayrshare.AyrSharePostResDTO;
import com.item.dto.ayrshare.AyrShareUserResDTO;
import com.item.dto.ayrshare.AyrshareTokenDTO;
import com.item.framework.config.AyrShareConfig;
import com.item.framework.constant.AyrshareStatus;
import static com.item.framework.constant.CommonConstants.StrConstants.ALL;
import static com.item.framework.constant.CommonConstants.StrConstants.ERROR;
import static com.item.framework.constant.CommonConstants.StrConstants.SUCCESS;
import com.item.framework.constant.CommonResponseCode;
import com.item.framework.error.BusinessException;
import com.item.framework.net.HttpClient5Service;
import com.item.framework.utils.MDCThreadPoolExecutor;
import com.item.service.AyrShareService;
import com.item.service.AyrshareCompanyConfigService;
import com.item.service.JobDoubleDataSourceService;
import com.item.util.JsonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author hua.liu
 */
@Slf4j
@Service
@RefreshScope
@RequiredArgsConstructor
public class AyrShareServiceImpl implements AyrShareService {
    private static final ThreadPoolExecutor AYRSHARE_POOL_EXECUTOR = new MDCThreadPoolExecutor(20, 40, 60,
            TimeUnit.SECONDS, new LinkedBlockingQueue<>(300), new ThreadFactoryBuilder()
            .setNameFormat("ayrshare-%d")
            .build());

    private final HttpClient5Service httpClient5Service;
    private final AyrShareConfig ayrShareConfig;
    private final AyrshareCompanyConfigService ayrshareCompanyConfigService;
    private final JobDoubleDataSourceService jobDoubleDataSourceService;
    @Value("${ayrshare.api.url:}")
    private String apiUrl;

    @Value("${ayrshare.api.key}")
    private String apiKey;

    @Override
    public String sendToAyrShare(AyrShareRequestDTO jsonBody) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("Authorization", "Bearer " + apiKey);
        log.info("sendToAyrShare headers {}", headers);
        return httpClient5Service.doPost(apiUrl, JsonUtils.toJson(jsonBody), headers);
    }

    @Override
    public void asyncSendToAyrShare(AyrShareRequestDTO jsonBody) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("Authorization", "Bearer " + apiKey);
        if (StringUtils.isNotBlank(ayrShareConfig.getUserProfileKey())) {
            headers.put("Profile-Key", ayrShareConfig.getUserProfileKey());
        }
        log.info("asyncSendToAyrShare headers {}", headers);
        httpClient5Service.doPostAsync(apiUrl, JsonUtils.toJson(jsonBody), headers);

    }

    @Override
    public AyrShareJwtResponseDTO generateJWT(AyrShareGenerateJwtDTO jsonBody) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("Authorization", "Bearer " + apiKey);
        log.info("generateJWT headers {}", headers);
        String json = JsonUtils.toJson(jsonBody);
        String res = httpClient5Service.doPost(ayrShareConfig.assemblyGenerateJwtUrl(), json, headers);
        log.info("generateJWT jsonBody {} res:{}", json, res);
        if (StringUtils.isBlank(res)) {
            return new AyrShareJwtResponseDTO();
        }
        AyrShareJwtResponseDTO resDTO = JsonUtils.toObject(res, AyrShareJwtResponseDTO.class);
        return resDTO;
    }

    @Override
    public AyrShareUserResDTO getUserProfileDetails() {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("Authorization", "Bearer " + apiKey);
        if (StringUtils.isNotBlank(ayrShareConfig.getUserProfileKey())) {
            headers.put("Profile-Key", ayrShareConfig.getUserProfileKey());
        }
        log.info("getUserProfileDetails headers {}", headers);
        String res = httpClient5Service.doGet(ayrShareConfig.assemblyGetUserUrl(), headers);
        log.info("getUserProfileDetails res:{}", res);
        return JsonUtils.toObject(res, AyrShareUserResDTO.class);
    }

    @Override
    public AyrShareCreateUserProfileResDTO createUserProfile(AyrShareCreateUserProfileReqDTO reqDTO) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("Authorization", "Bearer " + apiKey);
        String json = JsonUtils.toJson(reqDTO);
        log.info("createUserProfile headers {}", headers);
        String res = httpClient5Service.doPost(ayrShareConfig.assemblyCreateUserProfileUrl(), JsonUtils.toJson(reqDTO), headers);
        log.info("generateJWT jsonBody {} res:{}", json, res);
        return JsonUtils.toObject(res, AyrShareCreateUserProfileResDTO.class);
    }

    @Override
    public AyrShareUserResDTO getUserProfileDetails(String apiKey, String profileKey) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("Authorization", "Bearer " + apiKey);
        if (StringUtils.isNotBlank(profileKey)) {
            headers.put("Profile-Key", profileKey);
        }
        log.info("getUserProfileDetails headers {}", headers);
        String res = httpClient5Service.doGet(ayrShareConfig.assemblyGetUserUrl(), headers);
        log.info("getUserProfileDetails jsonBody {} res:{}", res, res);
        return JsonUtils.toObject(res, AyrShareUserResDTO.class);
    }

    @Override
    public AyrShareCreateUserProfileResDTO createUserProfile(String apiKey, AyrShareCreateUserProfileReqDTO req) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("Authorization", "Bearer " + apiKey);
        String json = JsonUtils.toJson(req);
        log.info("createUserProfile headers {}", headers);
        String res = httpClient5Service.doPost(ayrShareConfig.assemblyCreateUserProfileUrl(), JsonUtils.toJson(req), headers);
        log.info("createUserProfileByAllParam jsonBody {} res:{}", json, res);
        return JsonUtils.toObject(res, AyrShareCreateUserProfileResDTO.class);
    }

    @Override
    public CompletableFuture<Void> asyncSendToAyrShareWithConfig(AyrSharePostDTO postDTO) {
        log.info("asyncSendToAyrShareWithConfig starting for postDTO: {}", postDTO);
        Long jobId = postDTO.getJobId();
        String companyCode = postDTO.getCompanyCode();
        return CompletableFuture
                // 第一步：更新状态为分享中
                .supplyAsync(() -> {
                    log.info("Step 1: Updating job {} status to SHARING", jobId);
                    jobDoubleDataSourceService.updateJobAyrShareStatus(jobId, AyrshareStatus.SHARING.getCode());
                    return jobId;
                }, AYRSHARE_POOL_EXECUTOR)
                // 第二步：从数据库获取配置信息
                .thenCompose(id -> CompletableFuture.supplyAsync(() -> {
                    return sendToAyrShare(postDTO, id, companyCode, jobId);
                }, AYRSHARE_POOL_EXECUTOR))
                // 第四步：根据返回结果更新状态
                .thenAcceptAsync(response -> {
                    updateAyrShareStatus(response, jobId);
                }, AYRSHARE_POOL_EXECUTOR)
                // 异常处理
                .exceptionally(throwable -> {
                    log.error("CompletableFuture chain failed for postDTO: {}", postDTO, throwable);
                    jobDoubleDataSourceService.updateJobAyrShareStatus(jobId, AyrshareStatus.SHARE_FAILED.getCode());
                    return null;
                });
    }

    private void updateAyrShareStatus(List<String> response, Long jobId) {
        log.info("Step 4: Processing response {} and updating job {} status", response, jobId);
        try {
            //合并多个发送结果
            List<AyrSharePostResDTO> list = response.stream().map(rStr -> JsonUtils.toObject(rStr, AyrSharePostResDTO.class)).toList();
            AyrshareStatus ayrshareStatus = AyrSharePostResDTO.checkResponse(list);
            jobDoubleDataSourceService.updateJobAyrShareStatus(jobId, ayrshareStatus.getCode());
            log.info("updateAyrShareStatus ayrshareStatus {}", ayrshareStatus);
            //失败了
//            if (isAllError(res)) {
//                jobDoubleDataSourceService.updateJobAyrShareStatus(jobId, AyrshareStatus.SHARE_FAILED.getCode());
//                log.warn("Fail response from Ayrshare for job {} response {} ", jobId, response);
//                return;
//            }
//            //成功了
//            if (isAllSuccess(res)) {
//                jobDoubleDataSourceService.updateJobAyrShareStatus(jobId, AyrshareStatus.SHARE_SUCCESS.getCode());
//                log.info("Successfully shared job {} to Ayrshare response {} ", jobId, response);
//                return;
//            }
//            //部分成功
//            if (isPartialSuccess(res)) {
//                jobDoubleDataSourceService.updateJobAyrShareStatus(jobId, AyrshareStatus.PARTIAL_SUCCESS.getCode());
//                log.warn("Empty response from Ayrshare for job {} response {}", jobId, response);
//            }
        } catch (Exception e) {
            log.error("Error processing Ayrshare response for job: {}", jobId, e);
            jobDoubleDataSourceService.updateJobAyrShareStatus(jobId, AyrshareStatus.SHARE_FAILED.getCode());
        }
    }

    private List<String> sendToAyrShare(AyrSharePostDTO postDTO, Long id, String companyCode, Long jobId) {
        log.info("Step 2: Getting Ayrshare config for company: {}", companyCode);
        AyrshareTokenDTO tokenConfig = ayrshareCompanyConfigService.getTokenInfoByCompanyCode(companyCode);
        if (tokenConfig == null || StringUtils.isBlank(tokenConfig.getAyrshareApiKey())) {
            log.warn("No Ayrshare config found for company: {}, updating job status to failed {}", companyCode, tokenConfig);
            jobDoubleDataSourceService.updateJobAyrShareStatus(id, AyrshareStatus.SHARE_FAILED.getCode());
            throw BusinessException.of(CommonResponseCode.COMMON_AYRSHARE_APIKEY_NOT_FOUND);
        }
        log.info("Step 3: Calling Ayrshare API for job: {}", jobId);
        // 第三步：调用Ayrshare接口
        try {
            // 构建请求头
            Map<String, String> headers = new HashMap<>();
            headers.put("Content-Type", "application/json");
            headers.put("Authorization", "Bearer " + tokenConfig.getAyrshareApiKey());
            if (StringUtils.isNotBlank(tokenConfig.getAyrshareProfileKey())) {
                headers.put("Profile-Key", tokenConfig.getAyrshareProfileKey());
            }
            AyrShareRequestDTO jsonBody = null;
            List<String> platforms = List.of(ALL);
            if (ayrShareConfig.getPlatform() == 1) {
                AyrShareUserResDTO userProfileDetails = getUserProfileDetails(tokenConfig.getAyrshareApiKey(), tokenConfig.getAyrshareProfileKey());
                if (CollectionUtils.isNotEmpty(userProfileDetails.getActiveSocialAccounts())) {
                    platforms = userProfileDetails.getActiveSocialAccounts();
                }
                jsonBody = AyrShareRequestDTO.build(platforms, postDTO.getPublishUrl(), tokenConfig.getAyrshareSubreddit(),
                        postDTO.getCompanyName(), postDTO.getJobTitle(), postDTO.getJobPublishShareCase(), postDTO.getJobPublishShareTitleCase());
            } else {
                jsonBody = AyrShareRequestDTO.build(platforms, postDTO.getPublishUrl(), tokenConfig.getAyrshareSubreddit(),
                        postDTO.getCompanyName(), postDTO.getJobTitle(), postDTO.getJobPublishShareCase(), postDTO.getJobPublishShareTitleCase());
            }
            List<String> result = new ArrayList<>(4);
            //发送需要图片
            AyrShareRequestDTO mediaUrl = jsonBody.fillMediaUrls(ayrShareConfig.getImageUrl());
            if (mediaUrl != null) {
                log.info("Calling Ayrshare API mediaUrl with headers for mediaUrl {}: {}", mediaUrl, headers);
                String response = httpClient5Service.doPost(apiUrl, JsonUtils.toSkipNullJson(mediaUrl), headers);
                log.info("Ayrshare API mediaUrl response for job {}: {}", jobId, response);
                //获取结果
                result.add(response);
                // 清除一下平台防止重复发送
                jsonBody.cleanNeedMediaUrlsPlatform();
            }
            //twitter单独发送 长度限制
            if (jsonBody.getTwitterCustomerOptions() != null) {
                //构造twitter发送结构
                AyrShareRequestDTO ayrShareRequestDTO = jsonBody.convertTwitter();
                log.info("Calling Ayrshare API Twitter with headers for ayrShareRequestDTO {}: {}", ayrShareRequestDTO, headers);
                String response = httpClient5Service.doPost(apiUrl, JsonUtils.toSkipNullJson(ayrShareRequestDTO), headers);
                log.info("Ayrshare API Twitter response for job {}: {}", jobId, response);
                //获取结果
                result.add(response);
            }
            // 发送非twitter平台的其他平台
            AyrShareRequestDTO ayrShareRequestDTO = jsonBody.convertRemoveTwitter();
            if (CollectionUtils.isEmpty(ayrShareRequestDTO.getPlatforms())) {
                return result;
            }
            log.info("Calling Ayrshare API with headers for ayrShareRequestDTO {}: {}", ayrShareRequestDTO, headers);
            String response = httpClient5Service.doPost(apiUrl, JsonUtils.toSkipNullJson(ayrShareRequestDTO), headers);
            log.info("Ayrshare API response for job {}: {}", jobId, response);
            // 获取结果
            result.add(response);
            return result;
        } catch (Exception e) {
            log.error("Error calling Ayrshare API for job: {}", jobId, e);
            throw BusinessException.of(CommonResponseCode.COMMON_AYRSHARE_API_CALL_FAIL);
        }
    }

    @Override
    public boolean isAllError(AyrSharePostResDTO res) {
        if (res == null) {
            return true;
        }
        if (ERROR.equalsIgnoreCase(res.getStatus()) && CollectionUtils.isEmpty(res.getPostIds())) {
            return true;
        }
        return false;
    }

    @Override
    public boolean isAllSuccess(AyrSharePostResDTO res) {
        if (res == null) {
            return false;
        }
        if (SUCCESS.equalsIgnoreCase(res.getStatus())) {
            return true;
        }
        return false;
    }

    @Override
    public boolean isPartialSuccess(AyrSharePostResDTO res) {
        if (res == null) {
            return false;
        }
        if (ERROR.equalsIgnoreCase(res.getStatus()) && CollectionUtils.isNotEmpty(res.getPostIds())) {
            return true;
        }
        return false;
    }
}