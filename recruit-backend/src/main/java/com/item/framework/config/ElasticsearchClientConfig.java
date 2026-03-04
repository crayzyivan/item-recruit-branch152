package com.item.framework.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import static com.item.framework.constant.CommonConstants.LocalDateTimeConstant.LOCAL_DATE_TIME_FORMATTER;
import static com.item.framework.constant.CommonConstants.LocalDateTimeConstant.LOCAL_DATE_TIME_SERIALIZER;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;
import java.util.Arrays;

/**
 * @author hua.liu
 */
@Configuration
public class ElasticsearchClientConfig {

    @Value("${spring.elasticsearch.uris:}")
    private String uris;

    @Value("${spring.elasticsearch.username:}")
    private String username;

    @Value("${spring.elasticsearch.password:}")
    private String password;

    @Value("${spring.elasticsearch.connection-timeout:5000}")
    private int connectTimeout;

    @Value("${spring.elasticsearch.read-timeout:30000}")
    private int readTimeout;

    @Value("${spring.elasticsearch.connection-pool.max-connections:100}")
    private int maxConnections;

    @Value("${spring.elasticsearch.connection-pool.max-connections-per-route:10}")
    private int maxConnectionsPerRoute;

    @Value("${spring.elasticsearch.ssl-verification:false}")
    private boolean sslVerification;

    @Bean
    public ElasticsearchClient elasticsearchClient(ObjectMapper objectMapper) {
        // 支持多个节点
        String[] uriArr = uris.split(",|;");
        HttpHost[] httpHosts = Arrays.stream(uriArr)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(u -> {
                    String noProto = u.replace("http://", "").replace("https://", "");
                    String[] parts = noProto.split(":");
                    String host = parts[0];
                    int port = Integer.parseInt(parts[1]);
                    boolean https = u.startsWith("https://");
                    return new HttpHost(host, port, https ? "https" : "http");
                })
                .toArray(HttpHost[]::new);

        RestClientBuilder builder = RestClient.builder(httpHosts)
                .setRequestConfigCallback(requestConfigBuilder -> requestConfigBuilder
                        .setConnectTimeout(connectTimeout)
                        .setSocketTimeout(readTimeout)
                        .setConnectionRequestTimeout(2000)
                )
                .setHttpClientConfigCallback(httpClientBuilder -> {
                    httpClientBuilder.setDefaultIOReactorConfig(
                            IOReactorConfig.custom()
                                    .setIoThreadCount(Runtime.getRuntime().availableProcessors() * 2)
                                    .build()
                    );
                    httpClientBuilder.setMaxConnTotal(maxConnections);
                    httpClientBuilder.setMaxConnPerRoute(maxConnectionsPerRoute);
                    // 账号密码认证
                    if (username != null && !username.isEmpty()) {
                        BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
                        credentialsProvider.setCredentials(AuthScope.ANY,
                                new UsernamePasswordCredentials(username, password));
                        httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
                    }
                    // SSL配置
                    if (!sslVerification) {
                        httpClientBuilder.setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE);
                    }
                    return httpClientBuilder;
                });

        RestClient restClient = builder.build();
        RestClientTransport transport = new RestClientTransport(restClient, new JacksonJsonpMapper(elasticsearchObjectMapper()));
        return new ElasticsearchClient(transport);
    }

    private ObjectMapper elasticsearchObjectMapper() {

        ObjectMapper objectMapper = new ObjectMapper();

        // Create JavaTimeModule with custom serializers/deserializers
        JavaTimeModule javaTimeModule = new JavaTimeModule();

        // Configure LocalDateTime serialization/deserialization
        javaTimeModule.addSerializer(LocalDateTime.class, LOCAL_DATE_TIME_SERIALIZER);
        javaTimeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(LOCAL_DATE_TIME_FORMATTER));

        // Register the module
        objectMapper.registerModule(javaTimeModule);
        return objectMapper;
    }
} 