package com.item.framework.handler;

import com.fasterxml.jackson.core.exc.InputCoercionException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.item.framework.constant.CommonResponseCode;
import com.item.framework.constant.GlobalStatusCode;
import com.item.framework.error.BusinessException;
import com.item.framework.error.LoginException;
import com.item.vo.Result;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public Result<String> handleRuntimeException(RuntimeException ex) {

        Result<String> result;
        if (ex instanceof LoginException) {
            log.warn("login exception ", ex);
            result = Result.fail(((LoginException) ex).getCode(), ex.getMessage());
        } else if (ex instanceof BusinessException bex) {
            log.warn("business process exception ", ex);
            result = Result.fail(bex.getCode(), bex.getMessage());
        } else {
            log.error("request exception ", ex);
            result = Result.fail(GlobalStatusCode.FAIL, GlobalStatusCode.MESSAGE_INTERNAL_SERVER_ERROR);
        }

        return result;
    }

    /**
     * 处理参数验证失败异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, Object> errorDetails = new HashMap<>();
        Map<String, String> fieldErrors = new HashMap<>();
        List<String> globalErrors = new ArrayList<>();

        // 收集字段级别的错误
        ex.getBindingResult().getFieldErrors().forEach(error -> {
            String fieldName = error.getField();
            String errorMessage = error.getDefaultMessage();
            fieldErrors.put(fieldName, errorMessage);
        });

        // 收集全局级别的错误
        ex.getBindingResult().getGlobalErrors().forEach(error -> {
            globalErrors.add(error.getDefaultMessage());
        });

        // 构建详细错误信息
        errorDetails.put("fieldErrors", fieldErrors);
//        errorDetails.put("globalErrors", globalErrors);
        errorDetails.put("errorCount", fieldErrors.size() + globalErrors.size());

        // 生成友好的错误消息
//        StringBuilder sb = new StringBuilder(32);
        String msgs = fieldErrors.values().stream().distinct().collect(Collectors.joining(" and "));
        String msg = "field invalid: " + msgs;
        //返回了所有校验没通过的字段 返回的是一个结构体 不是简单字符串 使用单独code
        return Result.fail(CommonResponseCode.INTERFACE_REQUEST_PARAM_VALIDATION.getCode(), msg, errorDetails);
    }

    /**
     * 处理手动校验失败异常
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public Result<Map<String, Object>> handleValidationExceptions(ConstraintViolationException ex) {
        Map<String, Object> errorDetails = new HashMap<>();
        Map<String, String> fieldErrors = new HashMap<>();
        List<String> globalErrors = new ArrayList<>();

        // 收集字段级别的错误
        ex.getConstraintViolations().forEach(violation -> {
            String fieldName = violation.getPropertyPath().toString();
            String errorMessage = violation.getMessage();
            fieldErrors.put(fieldName, errorMessage);
        });

        // 构建详细错误信息
        errorDetails.put("fieldErrors", fieldErrors);
        errorDetails.put("errorCount", fieldErrors.size() + globalErrors.size());

        // 生成友好的错误消息
        String msgs = fieldErrors.values().stream().distinct().collect(Collectors.joining(" and "));
        String msg = "field invalid: " + msgs;
        //返回了所有校验没通过的字段 返回的是一个结构体 不是简单字符串 使用单独code
        return Result.fail(CommonResponseCode.INTERFACE_REQUEST_PARAM_VALIDATION.getCode(), msg, errorDetails);
    }

    @ExceptionHandler(Exception.class)
    public Result<String> handleException(Exception ex) {
        log.error("request fail ", ex);
        return Result.fail(GlobalStatusCode.SYSTEM_ERROR, GlobalStatusCode.MESSAGE_INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Result<String> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        Throwable cause = ex.getCause();
        //细化异常 便于排错 模糊返回防止暴力请求
        if (cause instanceof JsonMappingException jsonEx) {
            if (cause.getCause() instanceof InputCoercionException icex) {
                //处理数值范围错误
                log.warn("request param InputCoercionException ", icex);
                try {
                    String field;
                    if ((field = icex.getProcessor().getParsingContext().getCurrentName()) != null) {
                        return Result.fail(CommonResponseCode.COMMON_REQUEST_PARAM_FORMAT_FAIL.getCode(), CommonResponseCode.COMMON_REQUEST_PARAM_FORMAT_FAIL.getMsg() + " " + field);
                    }
                } finally {

                }
                return Result.fail(CommonResponseCode.COMMON_REQUEST_PARAM_FORMAT_FAIL);
            } else if (cause instanceof InvalidFormatException invalidEx) {
                // 处理格式错误
                log.warn("request param InvalidFormatException ", invalidEx);
                try {
                    String field;
                    if ((field = invalidEx.getPath().getFirst().getFieldName()) != null) {
                        return Result.fail(CommonResponseCode.COMMON_REQUEST_PARAM_FORMAT_FAIL.getCode(), CommonResponseCode.COMMON_REQUEST_PARAM_FORMAT_FAIL.getMsg() + " " + field);
                    }
                } finally {

                }
                return Result.fail(CommonResponseCode.COMMON_REQUEST_PARAM_FORMAT_FAIL);
            }
            log.warn("request param JsonMappingException ", jsonEx);
            return Result.fail(CommonResponseCode.COMMON_REQUEST_PARAM_FORMAT_FAIL);
        }
        log.warn("request param HttpMessageNotReadableException ", ex);
        return Result.fail(CommonResponseCode.COMMON_REQUEST_PARAM_FORMAT_FAIL);
    }

}
