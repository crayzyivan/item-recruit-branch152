package com.item.vo.iam;

import lombok.Data;

/**
 * @author : lh
 */
@Data
public class ExistsUserResVO {
    private Boolean exists;
    private String field;
    private String message;
}
