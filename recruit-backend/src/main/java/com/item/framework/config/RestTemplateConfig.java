package com.item.framework.config;

import com.item.framework.interceptor.ResponseLoggingInterceptor;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.socket.ConnectionSocketFactory;
import org.apache.hc.client5.http.socket.PlainConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.core5.http.config.Registry;
import org.apache.hc.core5.http.config.RegistryBuilder;
import org.apache.hc.core5.ssl.SSLContexts;
import org.apache.hc.core5.ssl.TrustStrategy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.xml.MappingJackson2XmlHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;
import java.nio.charset.StandardCharsets;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.List;

@Configuration
public class RestTemplateConfig {
    @Value("${iam.token.connect.time.out.second:60000}")
    private int connectTimeOut;
    @Value("${iam.token.read.time.out:60000}")
    private int readTimeOut;

    /**
     * 注册RestTemplate Bean
     */
    @Bean
    public RestTemplate restTemplate() {
        try {
            TrustStrategy acceptingTrustStrategy = (X509Certificate[] chain, String authType) -> true;
            SSLContext sslContext = SSLContexts.custom()
                    .loadTrustMaterial(null, acceptingTrustStrategy)
                    .build();

            SSLConnectionSocketFactory csf = new SSLConnectionSocketFactory(sslContext);

            Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create()
                    .register("http", new PlainConnectionSocketFactory())
                    .register("https", csf)
                    .build();

            PoolingHttpClientConnectionManager pccm = new PoolingHttpClientConnectionManager(registry);
            // 连接池最大并发连接数
            pccm.setMaxTotal(200);
            // 单路由最大并发数
            pccm.setDefaultMaxPerRoute(100);
            CloseableHttpClient httpClient = HttpClients.custom()
                    .setConnectionManager(pccm)
                    .evictExpiredConnections()
                    .build();
            HttpComponentsClientHttpRequestFactory requestFactory =
                    new HttpComponentsClientHttpRequestFactory();
            requestFactory.setHttpClient(httpClient);
            requestFactory.setConnectionRequestTimeout(connectTimeOut);
            requestFactory.setConnectTimeout(connectTimeOut);

            BufferingClientHttpRequestFactory bufferingFactory =
                    new BufferingClientHttpRequestFactory(requestFactory);
            //加上中文编码集
            RestTemplate restTemplate = new RestTemplate(bufferingFactory);
            List<HttpMessageConverter<?>> list = restTemplate.getMessageConverters();
            for (HttpMessageConverter<?> httpMessageConverter : list) {
                if (httpMessageConverter instanceof StringHttpMessageConverter) {
                    ((StringHttpMessageConverter) httpMessageConverter).setDefaultCharset(StandardCharsets.UTF_8);
                    break;
                }
            }
            //去掉XML转换器
            restTemplate.getMessageConverters().removeIf(
                    c -> c instanceof MappingJackson2XmlHttpMessageConverter
            );
            // 添加自定义拦截器
            restTemplate.setInterceptors(
                    Collections.singletonList(new ResponseLoggingInterceptor())
            );
            return restTemplate;
        } catch (Exception e) {
            return null;
        }
    }

    @Bean
    public ClientHttpRequestFactory simpleClientHttpRequestFactory() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(15000);
        factory.setReadTimeout(5000);
        return factory;
    }
}