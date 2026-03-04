package com.item.dto.iam;

import lombok.Data;

/**
 * Standard response structure for IAM API calls
 * @author hua.liu
 */
@Data
public class FeignResponse<T> {
    private Integer code;
    private T data;
    private String msg;
    private Boolean success;

} 