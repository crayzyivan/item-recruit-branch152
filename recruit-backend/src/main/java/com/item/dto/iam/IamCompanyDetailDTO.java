package com.item.dto.iam;

import lombok.Data;
import java.time.OffsetDateTime;

/**
 * DTO representing detailed IAM company information
 * @author hua.liu
 */
@Data
public class IamCompanyDetailDTO {
    private Long id;
    private String companyName;
    private String companyaAlias;
    private String companyCode;
    private String companyNumber;
    private Integer companyStatus;
    private String companyType;
    private String description;
    private Integer companySource;
    private String businessCategory;
    private String address1;
    private String address2;
    private String city;
    private String state;
    private String zipcode;
    private String county;
    private String country;
    private String telephone;
    private String email;
    private String fax;
    private String notes;
    private Long parentId;
    private String website;
    private String federalId;
    private String logopath;
    private Long managerId;
    private OffsetDateTime createdAt;
    private String createdBy;
    private OffsetDateTime updatedAt;
    private String updatedBy;
} 