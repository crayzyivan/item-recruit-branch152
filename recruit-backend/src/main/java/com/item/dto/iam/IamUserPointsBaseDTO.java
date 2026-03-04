package com.item.dto.iam;

import static com.item.framework.constant.CommonConstants.StrConstants.APP_CODE;
import lombok.Data;

/**
 * @author : lh
 */
@Data
public class IamUserPointsBaseDTO {
    /**
     * required
     */
    private Long userId;
    /**
     * required
     */
    private String appCode = APP_CODE;
}
