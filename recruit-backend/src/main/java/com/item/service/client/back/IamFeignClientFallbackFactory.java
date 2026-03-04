package com.item.service.client.back;

import com.item.service.client.IamFeignClient;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

/**
 * @author : lh
 * IAM服务调用异常回调工厂
 */
@Component
public class IamFeignClientFallbackFactory implements FallbackFactory<IamFeignClient> {
    @Override
    public IamFeignClient create(Throwable cause) {
        return new IamFeignClientFallback(cause);
    }
}
