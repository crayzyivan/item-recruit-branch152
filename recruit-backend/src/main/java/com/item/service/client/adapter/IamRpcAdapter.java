package com.item.service.client.adapter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.item.dto.iam.*;

import com.item.framework.config.IamCommonConfig;
import com.item.framework.constant.IGlobalStatusCode;
import com.item.framework.error.BusinessException;
import com.item.iam.oauth2.model.TokenResponse;
import com.item.iam.oauth2.service.OAuth2Service;
import com.item.service.client.IamFeignClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.item.framework.constant.FeignClientResponseCode.*;

/**
 * @author : lh
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class IamRpcAdapter {
    private static final TypeReference<FeignResponse<IamUserContextDTO>> RESPONSE_TYPE= new TypeReference<FeignResponse<IamUserContextDTO>>() {};


    private final IamFeignClient iamFeignClient;
    private final OAuth2Service oAuth2Service;
    private final IamCommonConfig iamCommonConfig;

    /**
     * 通过用户id获取用户信息
     *
     * @param userId
     * @return
     */
    public IamUserDTO getUserInfo(Long userId) {
        log.info("getUserInfo userId={}", userId);
        FeignResponse<IamUserDTO> iamUserDTOIamResponse = iamFeignClient.userInfo(userId);
        return getData(iamUserDTOIamResponse);
    }

    /**
     * 通过公司code获取公司信息
     *
     * @param companyCode
     * @return
     */
    public IamCompanyDTO getCompanyInfoByCode(String companyCode) {
        log.info("getCompanyInfoByCode companyCode={}", companyCode);
        FeignResponse<IamCompanyDTO> companyInfoByCode = iamFeignClient.getCompanyInfoByCode(companyCode);
        return getData(companyInfoByCode);
    }

    public IamCompanyDetailDTO getCompanyDetailByCode(String companyCode) {
        log.info("getCompanyDetailByCode companyCode={}", companyCode);
        FeignResponse<IamCompanyDetailDTO> companyDetailDTO = iamFeignClient.getCompanyDetailCode(companyCode);
        return getData(companyDetailDTO);
    }

    public Map<String , Object> getUserIdByAccessToken(String accessToken) {
        log.info("getUserIdByAccessToken  accessToken={}", accessToken);
        Map<String, Object> userInfo = Map.of();
        try {
            userInfo = oAuth2Service.getUserInfo(accessToken);
        } catch (Exception e) {
            log.error("getUserIdByAccessToken  error accessToken={}", accessToken, e);
            /* 检查嵌套异常  目前底层抛出异常内容 如下 目前解析这个错误信息 包含401 错误码 认为失效 需要刷新
             * java.lang.RuntimeException: Failed to fetch user info
             * 	at com.item.iam.oauth2.service.impl.OAuth2ServiceImpl.getUserInfo(OAuth2ServiceImpl.java:111) ~[iam-oauth2-starter-0.1.1-20250806.022505-1.jar:na]
             * 	........
             * 	at java.base/java.lang.Thread.run(Thread.java:1583) ~[na:na]
             * Caused by: java.io.IOException: Server returned HTTP response code: 401 for URL: https://id-dev.item.pub/user-info
             * 	at java.base/sun.net.www.protocol.http.HttpURLConnection.getInputStream0(HttpURLConnection.java:2014) ~[na:na]
             * 	....
             */
            Throwable cause = e;

            //防止死循环或者递归深度过大引起问题 限制一下
            int depth = 20;
            while (cause != null && depth > 0) {
                String causeMessage = cause.getMessage();
                if (causeMessage != null &&
                        (causeMessage.contains("HTTP response code: 401") ||causeMessage.contains(" 401 ") ||
                                causeMessage.contains(HttpStatus.UNAUTHORIZED.getReasonPhrase()))) {
                    return Map.of("code", 401);
                }
                cause = cause.getCause();
                depth--;
            }

            return userInfo;
        }
        return userInfo;
    }

    public IamUserRegisterResDTO iamUserRegister(IamUserRegisterReqDTO registerReqDTO){
        log.info("iamUserRegister registerReqDTO={}", registerReqDTO);
        FeignResponse<IamUserRegisterResDTO> iamUserRegister = iamFeignClient.iamUserRegister(registerReqDTO);
        return getData(iamUserRegister);
    }

    /**
     * Create user via IAM OpenAPI
     *
     * @param createUserReq create user request
     * @return create user response
     */
    public IamCreateUserResDTO createUser(IamCreateUserReqDTO createUserReq) {
        log.info("createUser createUserReq={}", createUserReq);
        FeignResponse<IamCreateUserResDTO> response = iamFeignClient.createUser(createUserReq);
        return getDataOrThrowsBusException(response, IAM_CREATE_USER_FAIL);
    }

    /**
     * 签发票据用于跨系统会话同步
     *
     * @param userId 用户ID
     * @param clientId 客户端ID
     * @return 票据响应
     */
    public IamTicketResDTO issueTicket(String userId, String clientId) {
        log.debug("issueTicket userId={}, clientId={}", userId, clientId);
        IamTicketReqDTO request = new IamTicketReqDTO();
        if (StringUtils.isBlank(userId) || StringUtils.isBlank(clientId)) {
            return null;
        }

        request.setUserId(userId);
        request.setClientId(clientId);

        FeignResponse<IamTicketResDTO> response = iamFeignClient.issueTicket(request);
        return getData(response);
    }

    /**
     * 交换票据获取访问令牌
     *
     * @param ticket 待交换的票据
     * @return 令牌交换响应
     */
    public IamTokenExchangeResDTO exchangeTicket(String ticket) {
        log.debug("exchangeTicket ticket {}", ticket);

        if (StringUtils.isBlank(ticket)) {
            log.warn("exchangeTicket ticket is blank");
            return null;
        }

        IamTicketExchangeReqDTO request = new IamTicketExchangeReqDTO();
        request.setTicket(ticket);

        FeignResponse<IamTokenExchangeResDTO> response = iamFeignClient.exchangeTicket(request);
        return getData(response);
    }

    public TokenResponse refreshToken(String refreshToken) {
        return oAuth2Service.refreshToken(refreshToken);
    }

    /**
     * 根据邮箱或用户名获取IAM用户信息
     * 
     * @param identifier 用户邮箱或用户名
     * @return IamUserDetailDTO 用户详细信息
     */
    public IamUserDetailDTO getUserByIdentifier(String identifier) {
        log.info("getUserByIdentifier identifier={}", identifier);
        FeignResponse<IamUserDetailDTO> response = iamFeignClient.queryUserByIdentifier(identifier);
        return getData(response);
    }

    /**
     * 根据邮箱从IAM系统获取用户信息并映射到IamCreateUserResDTO
     * 
     * @param email 用户邮箱
     * @return IamCreateUserResDTO 用户信息
     */
    public IamCreateUserResDTO getUserByEmail(String email) {
        log.info("getUserByEmail email={}", email);
        IamUserDetailDTO userDetail = getUserByIdentifier(email);
        if (userDetail == null) {
            log.warn("User not found by email: {}", email);
            return null;
        }
        
        // 将IamUserDetailDTO映射到IamCreateUserResDTO
        IamCreateUserResDTO iamCreateUserResDTO = new IamCreateUserResDTO();
        iamCreateUserResDTO.setId(userDetail.getId());
        iamCreateUserResDTO.setUserName(userDetail.getUserName());
        iamCreateUserResDTO.setEmail(userDetail.getEmail());
        iamCreateUserResDTO.setFirstName(userDetail.getFirstName());
        iamCreateUserResDTO.setLastName(userDetail.getLastName());
        iamCreateUserResDTO.setContactNumber(userDetail.getContactNumber());
        iamCreateUserResDTO.setCompanyCode(userDetail.getCompanyCode());
        iamCreateUserResDTO.setUserStatus(userDetail.getUserStatus());
        iamCreateUserResDTO.setUserType(userDetail.getUserType());
        iamCreateUserResDTO.setPrimaryUser(userDetail.getPrimaryUser());
        
        log.info("Successfully retrieved IAM user info: id={}, userName={}, email={}", 
                iamCreateUserResDTO.getId(), iamCreateUserResDTO.getUserName(), iamCreateUserResDTO.getEmail());
        
        return iamCreateUserResDTO;
    }

    /**
     * 根据用户ID获取用户完整详细信息
     * 包含用户基本信息、角色、权限、应用、公司等完整数据
     * 
     * 接口文档: https://s.apifox.cn/2d98fc39-2f6e-4821-a468-2f8a4f52246e/api-375428661
     * 
     * @param identifier 用户ID
     * @return IamUserDetailResponseDTO 用户完整详细信息，如果用户不存在则返回null
     */
    public IamUserDetailResponseDTO getUserDetailByIdentifier(String identifier) {
        log.info("getUserDetailById identifier={}", identifier);

        if (StringUtils.isBlank(identifier)) {
            log.warn("getUserDetailById userId is blank");
            return null;
        }

        FeignResponse<IamUserDetailResponseDTO> response = iamFeignClient.getUserDetailByIdentifier(identifier);
        IamUserDetailResponseDTO userDetail = getDataOrThrowsBusException(response, FEIGN_CLIENT_FAIL);
        Set<String> excludeCompanies = iamCommonConfig.getCompanyConfig().getSwitchTenantExcludeCompany();
        // 过滤掉排除列表中的公司
        if  (CollectionUtils.isNotEmpty(excludeCompanies) && userDetail != null) {
            List<IamCompanySimpleDTO> companies = Optional.ofNullable(userDetail.getCompanies()).stream().flatMap(Collection::stream)
                    .filter(Objects::nonNull)
                    .filter(s -> !excludeCompanies.contains(s.getCompanyCode())).collect(Collectors.toList());
            userDetail.setCompanies(companies);
        }
        
        log.info("Successfully retrieved user detail: userDetail {}", userDetail);
        return userDetail;
    }

    /**
     * 切换用户的活动租户
     * 
     * 接口文档: https://s.apifox.cn/2d98fc39-2f6e-4821-a468-2f8a4f52246e/api-221431159
     * 
     * @param userId 用户ID
     * @param tenantId 租户ID
     * @return 切换成功返回true，失败抛出BusinessException
     */
    public boolean switchUserTenant(Long userId, String tenantId) {
        log.info("switchUserTenant userId={}, tenantId={}", userId, tenantId);

        if (userId == null || StringUtils.isBlank(tenantId)) {
            log.warn("switchUserTenant invalid parameters: userId={}, tenantId={}", userId, tenantId);
            return false;
        }

        FeignResponse<Void> response = iamFeignClient.switchUserTenant(userId, tenantId);
        getDataOrThrowsBusException(response, FEIGN_CLIENT_FAIL);

        log.info("Successfully switched tenant: userId={}, tenantId={}", userId, tenantId);
        return true;
    }

    private <T> T getData(FeignResponse<T> iamResponse) {
        if (iamResponse.getSuccess() == null || !iamResponse.getSuccess()) {
            log.warn("get user info failed {}", iamResponse);
            return null;
        }
        if (iamResponse.getData() == null) {
            log.warn("get user info data is null {} ", iamResponse);
            return null;
        }
        return iamResponse.getData();
    }

    private <T> T getDataOrThrowsBusException(FeignResponse<T> iamResponse, IGlobalStatusCode code) throws BusinessException {
        if (iamResponse == null) {
            log.warn("get user info failed response is null");
            throw BusinessException.of(IAM_HAVE_NOT_RESPONSE);
        }
        if (iamResponse.getSuccess() == null || !iamResponse.getSuccess()) {
            log.warn("get user info failed {}", iamResponse);
            if (iamResponse.getCode()!= null) {
                throw BusinessException.of(iamResponse.getCode(),  iamResponse.getMsg());
            }
            throw BusinessException.of(code);
        }
        if (iamResponse.getData() == null) {
            log.warn("get user info data is null {} ", iamResponse);
            return null;
        }
        return iamResponse.getData();
    }

    public IamAdminDetailDTO getAdminByCompanyCode(String companyCode) {
        log.info("getAdminByCompanyCode companyCode={}", companyCode);
        FeignResponse<IamAdminDetailDTO> response = iamFeignClient.queryAdminByCompanyCode(companyCode);
        return getData(response);
    }
}
