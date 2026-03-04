package com.item.framework.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.StopWatch;
import org.springframework.util.StreamUtils;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

public class ResponseLoggingInterceptor implements ClientHttpRequestInterceptor {
    private static final Logger log = LoggerFactory.getLogger(ResponseLoggingInterceptor.class);
    
    // 文件下载相关的Content-Type列表
    private static final List<String> FILE_CONTENT_TYPES = Arrays.asList(
        "application/octet-stream",
        "application/pdf",
        "application/zip",
        "application/x-zip-compressed",
        "application/msword",
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
        "application/vnd.ms-excel",
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
        "image/jpeg",
        "image/png",
        "image/gif",
        "image/bmp",
        "text/csv",
        "video/",
        "audio/"
    );
    
    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body,
                                        ClientHttpRequestExecution execution) throws IOException {
        if (body != null && body.length > 0) {
            String requestBody = new String(body, StandardCharsets.UTF_8);
            log.info("request body: {}",requestBody);
        } else {
            log.info("request body null");
        }
        ObjectMapper objectMapper = new ObjectMapper();
        String headersStr = objectMapper.writeValueAsString(request.getHeaders().toSingleValueMap());
        // 创建计时器并开始计时
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        // 执行请求并获取响应
        ClientHttpResponse response = execution.execute(request, body);

        // 停止计时并计算响应时间
        stopWatch.stop();
        long responseTime = stopWatch.getTotalTimeMillis();
        
        // 判断是否为文件下载响应
        boolean isFileDownload = isFileDownloadResponse(request, response);
        
        if (isFileDownload) {
            // 对于文件下载，不输出响应体内容，只记录基本信息
            log.info("Request interface [{}],request headers : {}, response time {} ms, response status: {}, Content-Type: {}, File download - response body not logged",
                    request.getURI(),headersStr, responseTime, response.getStatusCode(), getContentType(response));
        } else {
            // 读取响应体内容
            String responseBody = StreamUtils.copyToString(response.getBody(), StandardCharsets.UTF_8);
            // 输出原始响应内容
            log.info("Request interface [{}],request headers : {}, response time {} ms, response status: {}, Original response content: {}",
                    request.getURI(),headersStr, responseTime, response.getStatusCode(), responseBody);
        }

        return response;
    }
    
    /**
     * 判断是否为文件下载响应
     * @param request HTTP请求
     * @param response HTTP响应
     * @return true表示是文件下载，false表示不是
     */
    private boolean isFileDownloadResponse(HttpRequest request, ClientHttpResponse response) {
        try {
            // 1. 检查Content-Disposition头部
            String contentDisposition = response.getHeaders().getFirst("Content-Disposition");
            if (contentDisposition != null && contentDisposition.toLowerCase().contains("attachment")) {
                return true;
            }
            
            // 2. 检查Content-Type头部
            String contentType = getContentType(response);
            if (contentType != null) {
                String lowerContentType = contentType.toLowerCase();
                for (String fileType : FILE_CONTENT_TYPES) {
                    if (lowerContentType.startsWith(fileType)) {
                        return true;
                    }
                }
            }
            
            // 3. 检查请求URL路径是否包含下载相关关键词
            String requestPath = request.getURI().getPath();
            if (requestPath != null) {
                String lowerPath = requestPath.toLowerCase();
                if (lowerPath.contains("/download") || 
                    lowerPath.contains("/export") || 
                    lowerPath.contains("/file") ||
                    lowerPath.endsWith(".pdf") ||
                    lowerPath.endsWith(".doc") ||
                    lowerPath.endsWith(".docx") ||
                    lowerPath.endsWith(".xls") ||
                    lowerPath.endsWith(".xlsx") ||
                    lowerPath.endsWith(".zip")) {
                    return true;
                }
            }
            
            // 4. 检查Content-Length（文件通常较大，这里设置为1MB阈值）
            long contentLength = response.getHeaders().getContentLength();
            if (contentLength > 1024 * 1024) { // 1MB
                return true;
            }
            
        } catch (Exception e) {
            log.warn("Error checking if response is file download: {}", e.getMessage());
        }
        
        return false;
    }
    
    /**
     * 获取响应的Content-Type
     * @param response HTTP响应
     * @return Content-Type字符串
     */
    private String getContentType(ClientHttpResponse response) {
        try {
            return response.getHeaders().getFirst("Content-Type");
        } catch (Exception e) {
            return null;
        }
    }
}
