package com.item.framework.config;

import com.item.dto.iam.FeignResponse;
import com.item.framework.error.BusinessException;
import com.item.util.JsonUtils;
import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * @author : lh
 */
@Slf4j
public class FeignErrorDecoder implements ErrorDecoder {
    private final ErrorDecoder defaultErrorDecoder = new Default();

    @Override
    public Exception decode(String method, Response response) {

        try {
            // 仅处理自定义业务异常（如HTTP 400/500状态码）
            if (response.status() >= 400 && response.body() != null) {
                String bodyStr;
                try (InputStream bodyStream = response.body().asInputStream()) {
                    bodyStr = new String(bodyStream.readAllBytes(), StandardCharsets.UTF_8);
                }
                log.warn("Feign call failed, status: {}, body: {}", response.status(), bodyStr);
                // 尝试解析为 JSON
                try {
                    FeignResponse errorResponse = JsonUtils.toObject(bodyStr, FeignResponse.class);
                    return BusinessException.of(errorResponse.getCode(), errorResponse.getMsg());
                } catch (Exception parseEx) {
                    // 不是 JSON，就直接把原始内容返回
                    log.warn("Feign call failed, status: {}, body: {}", response.status(), bodyStr);
                    return BusinessException.of(response.status(), bodyStr);
                }
            }
        } catch (IOException e) {
            // 解析失败时使用默认解码器
            return defaultErrorDecoder.decode(method, response);
        }
        // 默认处理其他异常
        return defaultErrorDecoder.decode(method, response);
    }
}
