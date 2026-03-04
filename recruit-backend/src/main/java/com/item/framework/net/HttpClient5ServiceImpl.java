package com.item.framework.net;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.item.framework.utils.MDCThreadPoolExecutor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author hua.liu
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HttpClient5ServiceImpl implements HttpClient5Service {
    private static final ThreadPoolExecutor THREAD_POOL_EXECUTOR = new MDCThreadPoolExecutor(8, 12, 60,
            TimeUnit.SECONDS, new LinkedBlockingQueue<>(100), new ThreadFactoryBuilder()
            .setNameFormat("httpclient5-%d")
            .build());
    private final CloseableHttpClient httpClient;

    /**
     * 同步POST请求
     */
    public String doPost(String url, String body, Map<String, String> headers) {
        // 创建POST请求
        HttpPost httpPost = new HttpPost(url);
        String result = null;
        try {
            // 设置请求体
            StringEntity entity = new StringEntity(body, StandardCharsets.UTF_8);
            httpPost.setEntity(entity);

            // 设置请求头
            if (headers != null) {
                headers.forEach(httpPost::addHeader);
            }

            // 定义响应处理器：自动处理响应和资源释放
            HttpClientResponseHandler<String> responseHandler = response -> {
                int statusCode = response.getCode();
                // 这里可以根据状态码和响应内容进行处理
                log.info("response code {}", statusCode);
                HttpEntity responseEntity = response.getEntity();
                // 示例：返回响应体内容（实际使用时需根据需求处理）
                return EntityUtils.toString(responseEntity, StandardCharsets.UTF_8);
            };

            // 执行请求并获取处理结果（推荐用法）
            result = httpClient.execute(httpPost, responseHandler);
            log.info("HttpClient5Service doPost result {}", result);
        } catch (Exception e) {
            log.error("HttpClient5Service doPost exception ", e);
        } finally {
            // HttpPost会自动释放连接，显式释放
            httpPost.reset();
        }
        return result;
    }

    @Override
    public void doPostAsync(String url, String body, Map<String, String> headers) {
        THREAD_POOL_EXECUTOR.execute(() -> {
            doPost(url, body, headers);
        });
    }

    /**
     * 同步GET请求
     */
    @Override
    public String doGet(String url, Map<String, String> headers) {
        // 创建GET请求
        HttpGet httpGet = new HttpGet(url);
        String result = null;
        try {
            // 设置请求头
            if (headers != null) {
                headers.forEach(httpGet::addHeader);
            }

            // 定义响应处理器：自动处理响应和资源释放
            HttpClientResponseHandler<String> responseHandler = response -> {
                int statusCode = response.getCode();
                // 这里可以根据状态码和响应内容进行处理
                log.info("response code {}", statusCode);
                HttpEntity responseEntity = response.getEntity();
                // 示例：返回响应体内容（实际使用时需根据需求处理）
                return EntityUtils.toString(responseEntity, StandardCharsets.UTF_8);
            };

            // 执行请求并获取处理结果（推荐用法）
            result = httpClient.execute(httpGet, responseHandler);
            log.info("HttpClient5Service doGet result {}", result);
        } catch (Exception e) {
            log.error("HttpClient5Service doGet exception ", e);
        } finally {
            // HttpGet会自动释放连接，显式释放
            httpGet.reset();
        }
        return result;
    }
}