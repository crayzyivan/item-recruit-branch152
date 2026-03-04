package com.item.dto.iam;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * DTO for initializing IAM points account
 * @author hua.liu
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class IamPointsInitAccountDTO extends IamUserPointsBaseDTO {

    /**
     * User name required
     */
    private String userName;

    /**
     * User email required
     */
    private String userEmail;

    /**
     * Created by
     */
    private String createdBy;
} 