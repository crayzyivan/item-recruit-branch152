package com.item.dto.crm;

import lombok.Data;

/**
 * Standard response structure for IAM API calls
 * @author hua.liu
 */
@Data
public class CrmFeignResponse<T> {
    private String code;
    private T data;
    private Object msg;
    private Boolean success;

} 