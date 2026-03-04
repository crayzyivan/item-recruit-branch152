package com.item.dto.job;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class MasterAccountInfoDTO {
//    private Long id;
    private Integer jobCount;
//    private String companyName;
//    private String masterName;
    private Boolean accountExpired;
    private Integer points;
//    private Long customerId;
//    private String companyCode;
}
