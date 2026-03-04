package com.item.framework.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Data
@Component
@RefreshScope
@ConfigurationProperties(prefix = "recruit.interview.openapi.whitelist")
public class IpWhitelistProperties {
    /**
     * 是否启用白名单检查
     */
    private boolean enabled = false;

    /**
     * 白名单 IP 列表
     */
    private List<String> ips = new ArrayList<>();
}
