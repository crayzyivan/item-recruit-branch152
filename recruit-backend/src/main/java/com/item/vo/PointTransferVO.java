package com.item.vo;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 积分转移请求参数
 *
 * @author liyunlong
 * @version 1.0
 * @since 2025-07-29  19:04
 */
@Data
public class PointTransferVO {

    @NotNull(message = "fromUserId ID is required")
    private Long fromUserId;
    @NotNull(message = "toUserId ID is required")
    private Long toUserId;
    @NotNull(message = "points is required")
    private Long points;
    //积分类型 1赠送积分 2充值积分
    @NotNull(message = "pointType is required")
    private Integer pointType;
    private String remark;
}