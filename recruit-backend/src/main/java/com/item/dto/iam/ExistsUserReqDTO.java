package com.item.dto.iam;

import lombok.Data;

import java.util.Map;

/**
 * @author : lh
 */
@Data
public class ExistsUserReqDTO{
    /**
     * 必须
     */
    private String companyCode;
    /**
     * 必须
     */
    private String userName;
    private String rawPassword;
    /**
     * 必须
     */
    private String email;
    private String firstName;
    private String lastName;
    private String contactNumber;
    private String userType;
    private String userTags;
    private String origin;
    private Map<String, Object> externalInfo;
}
