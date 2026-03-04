package com.item.service.client.back;

import com.item.service.client.CrmFeignClient;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

/**
 * @author : lh
 * IAM服务调用异常回调工厂
 */
@Component
public class CrmFeignClientFallbackFactory implements FallbackFactory<CrmFeignClient> {
    @Override
    public CrmFeignClient create(Throwable cause) {
        return new CrmFeignClientFallback(cause);
    }
}
