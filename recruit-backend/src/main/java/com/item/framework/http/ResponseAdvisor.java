package com.item.framework.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.item.controller.XmlController;
import com.item.iam.oauth2.controller.LoginController;
import com.item.vo.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.core.io.FileUrlResource;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@Slf4j
@RestControllerAdvice(basePackages = "com.item")
public class ResponseAdvisor implements ResponseBodyAdvice<Object>{
    private final ObjectMapper objectMapper;

    public ResponseAdvisor(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean supports(MethodParameter methodParameter, Class<? extends HttpMessageConverter<?>> aClass) {
        if(methodParameter.getExecutable().getName().contains("error")) return false;
        //不处理iam oauth 包下controller相关的返回结构体 按照oauth本身结构返回
        if (LoginController.class.equals(methodParameter.getContainingClass())) {
            return false;
        }
        if (XmlController.class.equals(methodParameter.getContainingClass())) {
            return false;
        }
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object o, MethodParameter methodParameter, MediaType mediaType, Class<? extends HttpMessageConverter<?>> aClass, ServerHttpRequest serverHttpRequest, ServerHttpResponse serverHttpResponse) {

        if(o instanceof Result){
            return o;
        }
        if(o instanceof FileUrlResource){
            return o;
        }
        if(o instanceof Boolean){
            boolean result = (boolean)o;
            return new Result<Boolean>(result);
        }

        if(o instanceof String || methodParameter.getParameterType().equals(String.class)){
            Result<Object> result = new Result<>(o);

            try {

                String response = objectMapper.writeValueAsString(result);

                serverHttpResponse.getHeaders().setContentType(MediaType.APPLICATION_JSON);
                serverHttpResponse.getBody().write(response.getBytes());

                return null;
            } catch (Exception e) {
                log.error("ResponseAdvisor beforeBodyWrite fail", e);
            }
        }
        return new Result<>(o);
    }
}
