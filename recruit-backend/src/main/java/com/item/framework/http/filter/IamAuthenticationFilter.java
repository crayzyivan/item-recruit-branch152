package com.item.framework.http.filter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.item.dto.iam.FeignResponse;
import com.item.dto.iam.IamCompanySimpleDTO;
import com.item.dto.iam.IamUserContextDTO;
import com.item.dto.iam.IamUserDetailResponseDTO;
import com.item.entity.CandidateEntity;
import com.item.framework.config.IamPathConfig;
import com.item.framework.config.RecruitCommonNacosConfig;
import static com.item.framework.constant.CommonConstants.StrConstants.AUTHORIZATION_BEARER_HEADER;
import static com.item.framework.constant.CommonConstants.StrConstants.AUTHORIZATION_HEADER;
import static com.item.framework.constant.CommonConstants.StrConstants.NEW_REFRESH_TOKEN_HEADER;
import static com.item.framework.constant.CommonConstants.StrConstants.NEW_TOKEN_HEADER;
import static com.item.framework.constant.CommonConstants.StrConstants.TENANT_ID_HEADER;
import com.item.framework.constant.UserIdentifyTypeEnum;
import com.item.iam.oauth2.model.TokenResponse;
import com.item.service.CandidateService;
import com.item.service.client.adapter.IamRpcAdapter;
import com.item.util.CommonUtils;
import static com.item.util.CommonUtils.parseRefreshJwt;
import static com.item.util.CommonUtils.parseRequestHeaderTenantId;
import com.item.util.JsonUtils;
import com.item.util.UserContextUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author : lh
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 100)
@RequiredArgsConstructor
public class IamAuthenticationFilter extends OncePerRequestFilter {

    private static final TypeReference<FeignResponse<IamUserContextDTO>> RESPONSE_TYPE= new TypeReference<FeignResponse<IamUserContextDTO>>() {};
    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();
    private final IamRpcAdapter iamRpcAdapter;
    private final IamPathConfig iamPathConfig;
    private final CandidateService candidateService;
    private final RecruitCommonNacosConfig recruitCommonNacosConfig;

    static {
        // 配置路径匹配器
        // 不区分大小写
        PATH_MATCHER.setCaseSensitive(false);
        // 去除空格
        PATH_MATCHER.setTrimTokens(true);
        // 设置路径分隔符
        PATH_MATCHER.setPathSeparator("/");

    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        try {
            String accessToken = parseJwt(request);
            if (StringUtils.isNotBlank(accessToken)) {
                // 获取请求头中的租户id
                String headerTenantId = parseRequestHeaderTenantId(request);
                Map<String, Object> userIdByAccessToken = iamRpcAdapter.getUserIdByAccessToken(accessToken);
                log.info("iam get user info by access token source {} headerTenantId {}", userIdByAccessToken, headerTenantId);
                FeignResponse<IamUserContextDTO> iamUserResponse = JsonUtils.convertToEntity(userIdByAccessToken, RESPONSE_TYPE);
                // 401 token过期 需要自动刷新
                if (iamUserResponse != null && iamUserResponse.getCode() != null && iamUserResponse.getCode() == 401) {
                    // 解析刷新token
                    String refreshToken = parseRefreshJwt(request);
                    log.info("filter iam iamUserResponse {} refreshToken {}", iamUserResponse, refreshToken);
                    if (refreshToken != null) {
                        // 通过刷新token获取新的token access refresh
                        TokenResponse tokenResponse = iamRpcAdapter.refreshToken(refreshToken);
                        log.info("filter iam iamUserResponse {} refreshToken {} tokenResponse {}", iamUserResponse, refreshToken, tokenResponse);
                        if (tokenResponse != null && StringUtils.isNotBlank(tokenResponse.getAccessToken())) {
                            // 校验通过 获取新的userinfo数据
                            accessToken = tokenResponse.getAccessToken();
                            userIdByAccessToken = iamRpcAdapter.getUserIdByAccessToken(accessToken);
                            iamUserResponse = JsonUtils.convertToEntity(userIdByAccessToken, RESPONSE_TYPE);
                            // 将新的token写入响应头 access 和 refresh
                            response.setHeader(NEW_TOKEN_HEADER, accessToken);
                            response.setHeader(NEW_REFRESH_TOKEN_HEADER, tokenResponse.getRefreshToken());
                            //按照请求头 > token解析 的优先级设置响应头租户id
                            response.setHeader(TENANT_ID_HEADER, CommonUtils.firstNonBlank(headerTenantId, Optional.ofNullable(getData(iamUserResponse)).map(IamUserContextDTO::getCompanyCode).orElse(null)));
                        }
                    }
                }

                //获取userinfo数据
                IamUserContextDTO data = getData(iamUserResponse);
                // 验证请求头信息是否合法 不合法重新登录
                if (data != null && StringUtils.isNotBlank(headerTenantId) && !Strings.CS.equals(headerTenantId, data.getCompanyCode())) {
                    log.info("request headerTenantId {} iam tenantId {}", headerTenantId,  data.getCompanyCode());
                    IamUserDetailResponseDTO userDetailByIdentifier = iamRpcAdapter.getUserDetailByIdentifier(data.getId());
                    Set<String> flowCompanies = Optional.ofNullable(userDetailByIdentifier)
                            .map(IamUserDetailResponseDTO::getCompanies)
                            .stream()
                            .flatMap(Collection::stream)
                            .map(IamCompanySimpleDTO::getCompanyCode)
                            .collect(Collectors.toSet());

                    if (!flowCompanies.contains(headerTenantId)) {
                        log.warn("iam tenant id is invalid headerTenantId {} flowCompanies {}", headerTenantId, flowCompanies);
                        data = null;
                    }
                }

                if (data != null) {
                    UserIdentifyTypeEnum userIdentify = CommonUtils.getUserIdentify(data, recruitCommonNacosConfig.getCandidateUserIdentify());
                    data.setUserIdentifyCode(userIdentify.getCode());
                    // 如果是应聘者 填充recruit系统中的candidate表的主键ID
                    if (userIdentify == UserIdentifyTypeEnum.CANDIDATE) {
                        List<CandidateEntity> candidates = candidateService.getByCandidateId(Long.parseLong(data.getId()));
                        if (CollectionUtils.isNotEmpty(candidates) && candidates.size() == 1) {
                            data.setCandidateOneselfId(candidates.stream().findFirst().map(CandidateEntity::getId).orElse(null));
                        } else {
                            log.warn("iam userId get candidate exception {} data {}", candidates, data);
                        }
                    }
                    // 在需要使用accessToken访问的接口调用时需要使用，比如在使用accessToken置换ticket时候使用
                    data.setAuthorization(accessToken);
                    //重置租户id  按照请求头 > token解析 的优先级设置响应头租户id
                    String companyCode = CommonUtils.firstNonBlank(headerTenantId, data.getCompanyCode());
                    data.setCompanyCode(companyCode);
                }
                UserContextUtil.setCurrentUser(data);
                log.info("iam get user info by access token {}", data);
            }
        } catch (Exception e) {
            log.error("Unexpected authentication error: {}", e.getMessage(), e);
            // 清除上下文
            UserContextUtil.clear();
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            //清除上下文
            UserContextUtil.clear();
        }
    }


    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        String method = request.getMethod();
        // 定制化过滤逻辑 记录请求信息
        boolean isExcluded = isPathExcluded(path) || isExcludedMethod(method);
        log.info("Checking request - Path: {}, Method: {} isExcluded {} ", path, method, isExcluded);

        return isExcluded;
    }

    private String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.isNotBlank(headerAuth) && headerAuth.startsWith(AUTHORIZATION_BEARER_HEADER)) {
            return headerAuth.substring(7);
        }
        return null;
    }


    /**
     * 检查路径是否应该被排除
     */
    private boolean isPathExcluded(String path) {
        Set<String> pathConfig = iamPathConfig.getExcluded().getPath();
        for (String pathTemp : pathConfig) {
            // 检查路径匹配
            if (!PATH_MATCHER.match(pathTemp, path)) {
                continue;
            }
            return true;
        }

        return false;
    }

    private boolean isExcludedMethod(String method) {
        // 排除特定HTTP方法
        return iamPathConfig.getExcluded().getMethod().contains(method);
    }

    private <T> T getData(FeignResponse<T> iamResponse) {
        if (iamResponse == null || iamResponse.getSuccess() == null || !iamResponse.getSuccess()) {
            log.warn("get user info by access token failed {}", iamResponse);
            return null;
        }
        if (iamResponse.getData() == null) {
            log.warn("get user info by access token data is null  {} ", iamResponse);
            return null;
        }
        return iamResponse.getData();
    }
}
