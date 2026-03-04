package com.item.framework.config;

import com.item.dto.AuthorizationCodeTokenDto;
import com.item.framework.constant.CommonConstants;
import static com.item.framework.constant.CommonConstants.StrConstants.AUTHORIZATION_BEARER_HEADER;
import static com.item.framework.constant.CommonConstants.StrConstants.GRANT_TYPE;
import static com.item.framework.constant.CommonConstants.StrConstants.SCOPE;
import static com.item.framework.constant.CommonConstants.StrConstants.TICKET_EXCHANGE_TOKEN_URL;
import static com.item.framework.constant.CommonConstants.StrConstants.TRACE_ID_HEADER;
import com.item.framework.utils.TraceIdUtils;
import com.item.iam.oauth2.config.OAuth2Properties;
import com.item.util.UserContextUtil;
import feign.RequestInterceptor;
import feign.codec.ErrorDecoder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Set;

/**
 * @author : lh
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class IamFeignClientConfig {

    private final RestTemplate restTemplate;
    private final OAuth2Properties oAuth2Properties;
    private final IamPathConfig iamPathConfig;
    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    static {
        // 配置路径匹配器
        // 不区分大小写
        PATH_MATCHER.setCaseSensitive(false);
        // 去除空格
        PATH_MATCHER.setTrimTokens(true);
        // 设置路径分隔符
        PATH_MATCHER.setPathSeparator("/");

    }

    @Bean
    public RequestInterceptor iamClientHeaderInterceptor() {
        return template -> {
            // 从当前线程的MDC中获取traceId
            String traceId = TraceIdUtils.getTraceId();
            // 如果存在traceId，添加到请求头中
            if (StringUtils.isNotBlank(traceId)) {
                template.header(TRACE_ID_HEADER, traceId);
            }
            String path = template.path();
            // 如果是获取rpc的请求token 添加这个请求
            if (oAuth2Properties.getTokenEndpoint().equals(path)) {
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
                headers.setBasicAuth(
                        oAuth2Properties.getClientId(),
                        oAuth2Properties.getClientSecret());
                headers.forEach(template::header);
                return;
            }
            // 通过ticket置换token的rpc的请求
            if (TICKET_EXCHANGE_TOKEN_URL.equals(path)) {
                HttpHeaders headers = new HttpHeaders();
                headers.setBasicAuth(
                        oAuth2Properties.getClientId(),
                        oAuth2Properties.getClientSecret());
                headers.forEach(template::header);
                return;
            }
            // 在需要使用accessToken鉴权的接口调用时需要使用， 比如跨系统登录态保持需求 增加了一个获取tickets数据的接口 需要这中token ； /tickets/issue
            if (isSimulateFrontRequest(path)) {
                template.header(HttpHeaders.AUTHORIZATION, AUTHORIZATION_BEARER_HEADER + UserContextUtil.getCurrentUserNeedLogin().getAuthorization());
                return;
            }
            String accessToken = getAccessToken();
//            log.info("iamClientHeaderInterceptor accessToken:{}", accessToken);
            template.header(HttpHeaders.AUTHORIZATION, AUTHORIZATION_BEARER_HEADER + accessToken);
        };
    }

    @Bean
    public ErrorDecoder iamErrorDecoder() {
        return new FeignErrorDecoder();
    }

    /**
     * 获取IAM系统的OAuth2访问令牌
     */
    private String getAccessToken() {
        try {
            // 准备请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.setBasicAuth(
                    oAuth2Properties.getClientId(),
                    oAuth2Properties.getClientSecret());

            // 准备请求体
            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add(GRANT_TYPE, CommonConstants.StrConstants.GRANT_TYPE_CLIENT_CREDENTIALS);
            body.add(SCOPE, oAuth2Properties.getScope());

            // 创建HTTP请求实体
            HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(body, headers);

            // 使用专用的RestTemplate实例发送请求
            ResponseEntity<AuthorizationCodeTokenDto> response = restTemplate.exchange(
                    iamPathConfig.getTokenBase() + oAuth2Properties.getTokenEndpoint(),
                    HttpMethod.POST,
                    entity,
                    AuthorizationCodeTokenDto.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
//                log.info("成功获取IAM OAuth2访问令牌");
                return response.getBody().getAccessToken();
            } else {
                log.error("获取IAM OAuth2访问令牌失败: {}", response);
                return null;
            }
        } catch (Exception e) {
            log.error("获取IAM OAuth2访问令牌异常", e);
            return null;
        }
    }

    private boolean isSimulateFrontRequest(String path) {
        IamPathConfig.MethodPathConfig simulateFrontRequest = iamPathConfig.getSimulateFrontRequest();
        if (simulateFrontRequest == null) {
            return false;
        }
        if (CollectionUtils.isEmpty(simulateFrontRequest.getPath())) {
            return false;
        }
        boolean isSimulate = simulateFrontRequest.getPath().contains(path);
        if (isSimulate) {
            return true;
        }
        Set<String> pathConfig = simulateFrontRequest.getPath();
        for (String pathTemp : pathConfig) {
            // 检查路径匹配
            if (PATH_MATCHER.match(pathTemp, path)) {
                return true;
            }
        }
        return false;
    }

}