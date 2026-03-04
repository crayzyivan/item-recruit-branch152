package com.item.dto.iam;

import lombok.Data;

/**
 * DTO for IAM user points information
 * @author hua.liu
 */
@Data
public class IamUserPointsDTO {
    /**
     * User ID
     */
    private Long userId;

    /**
     * Application code
     */
    private String appCode;

    /**
     * Total points
     */
    private Integer totalPoints;

    /**
     * Gifted points
     */
    private Integer giftedPoints;

    /**
     * Paid points
     */
    private Integer paidPoints;

    /**
     * Frozen points
     */
    private Integer frozenPoints;

    /**
     * Available points
     */
    private Integer availablePoints;
} 