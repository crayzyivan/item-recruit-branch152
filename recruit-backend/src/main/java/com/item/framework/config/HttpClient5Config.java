package com.item.framework.config;


import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author hua.liu
 */
@Configuration
public class HttpClient5Config {
    // 最大连接数
    @Value("${httpclient.max.total:200}")
    private int maxTotal;

    // 每个路由最大连接数
    @Value("${httpclient.max.perRoute:50}")
    private int maxPerRoute;

    // 连接超时时间（毫秒）
    @Value("${httpclient.connectTimeout:30000}")
    private int connectTimeout;

    // 从连接池获取连接超时时间（毫秒）
    @Value("${httpclient.requestTimeout:30000}")
    private int requestTimeout;

    @Value("${httpclient.responseTimeout:30000}")
    private int responseTimeout;
    // 读取超时时间（毫秒）
    @Value("${httpclient.socketTimeout:30000}")
    private int socketTimeout;

    // 空闲连接存活时间（秒）
    @Value("${httpclient.idleTimeout:60}")
    private int idleTimeout;

    /**
     * 同步HttpClient连接池管理器
     *
     * @return PoolingHttpClientConnectionManager
     */
    @Bean(destroyMethod = "close")
    public PoolingHttpClientConnectionManager httpClientConnectionManager() {
        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
        //连接超时配置
        ConnectionConfig build = ConnectionConfig.custom()
                .setSocketTimeout(Timeout.ofMilliseconds(socketTimeout))
                .setConnectTimeout(Timeout.ofMilliseconds(connectTimeout)).build();
        // 最大连接数
        cm.setMaxTotal(maxTotal);
        // 每个路由最大连接数
        cm.setDefaultMaxPerRoute(maxPerRoute);
        // 空闲连接超时时间
        cm.closeIdle(TimeValue.ofSeconds(idleTimeout));
        cm.setDefaultConnectionConfig(build);
        return cm;
    }

    /**
     * HttpClient请求参数配置
     *
     * @return RequestConfig
     */
    @Bean
    public RequestConfig requestConfig() {
        return RequestConfig.custom()
                // 从连接池获取连接超时
                .setConnectionRequestTimeout(Timeout.ofMilliseconds(requestTimeout))
                // 读取超时
                .setResponseTimeout(Timeout.ofMilliseconds(responseTimeout))
                .build();
    }

    /**
     * 同步HttpClient客户端
     *
     * @param cm            连接池管理器
     * @param requestConfig 请求参数配置
     * @return CloseableHttpClient
     */
    @Bean(destroyMethod = "close")
    public CloseableHttpClient httpClient(PoolingHttpClientConnectionManager cm, RequestConfig requestConfig) {
        return HttpClients.custom()
                .setConnectionManager(cm)
                .setDefaultRequestConfig(requestConfig)
                // 定期清理空闲连接
                .evictExpiredConnections()
                .evictIdleConnections(TimeValue.ofSeconds(idleTimeout))
                .build();
    }
}