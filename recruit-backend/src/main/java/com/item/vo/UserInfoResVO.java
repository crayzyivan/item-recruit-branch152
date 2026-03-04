package com.item.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author : lh
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserInfoResVO {
    /**
     * 1：应聘者； 2：招聘者
     */
    private Integer userIdentityType;

    private String userName;

    private String userEmail;

    private Long candidateId;

    /**
     * 1：candidate 应聘者； 2：招聘者 主账号 recruit master 3: 招聘者 子账号 recruit sub
     */
    private Integer userIdentityTypeV1;

    /**
     * 永久邮箱
     */
    private String permanentEmail;

    /**
     * 0, "NOTHING View"
     * 1, "All View"
     * 2, "XML Setting"
     */
    private Integer viewDisplay;

    private String userId;
}
