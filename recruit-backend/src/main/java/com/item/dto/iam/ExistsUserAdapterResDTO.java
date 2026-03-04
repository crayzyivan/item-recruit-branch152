package com.item.dto.iam;

import lombok.Data;

/**
 * @author : lh
 */
@Data
public class ExistsUserAdapterResDTO {
    private Boolean exists;
    private String field;
    private String message;
}
