package com.item.dto.iam;

import lombok.Data;

/**
 * DTO representing IAM company information
 * @author hua.liu
 */
@Data
public class IamCompanyDTO {
    private String companyCode;
    private String companyName;
    private String website;
    private String tax;
    private String businessCategory;
    private String address1;
    private String city;
    private String state;
    private String zipcode;
    private String country;
    private String telephone;
} 