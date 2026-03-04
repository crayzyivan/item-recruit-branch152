package com.item.framework.config;

import com.item.dto.crm.CrmFeignResponse;
import com.item.framework.constant.CrmConvertResponseCode;
import com.item.framework.constant.CrmResponseCode;
import com.item.framework.error.BusinessException;
import com.item.util.JsonUtils;
import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author : lh
 */
@Slf4j
public class CrmFeignErrorDecoder implements ErrorDecoder {
    private final ErrorDecoder defaultErrorDecoder = new Default();

    @Override
    public Exception decode(String method, Response response) {

        try {
            // 仅处理自定义业务异常（如HTTP 400/500状态码）
            if (response.status() >= 400 && response.body() != null) {
                try (InputStream bodyStream = response.body().asInputStream()) {
                    CrmFeignResponse errorResponse = JsonUtils.toObject(bodyStream, CrmFeignResponse.class);
                    log.warn("crm feign fail {}", errorResponse);
                    CrmResponseCode byCode = CrmConvertResponseCode.getByCode(errorResponse.getCode());
                    return BusinessException.of(CrmResponseCode.CRM_FAIL.getCode(), parseMsg(errorResponse.getMsg()));
                }
            }
        } catch (IOException e) {
            // 解析失败时使用默认解码器
            return defaultErrorDecoder.decode(method, response);
        }
        // 默认处理其他异常
        return defaultErrorDecoder.decode(method, response);
    }

    public String parseMsg(Object msgObj) {
        if (msgObj instanceof String msg) {
            return msg;
        }
        return JsonUtils.toJson(msgObj);
    }
}
