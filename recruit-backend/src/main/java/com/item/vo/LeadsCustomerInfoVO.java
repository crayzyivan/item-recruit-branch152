package com.item.vo;

import lombok.Data;

/**
 * @author : lh
 */
@Data
public class LeadsCustomerInfoVO {
    private Long leadCompanyId;
    private String customerCode;
    private Long customerId;
    /**
     * 检查是否通过 true可以继续下一步操作
     */
    private Boolean checkAccess;

    public static LeadsCustomerInfoVO buildAccess(){
        LeadsCustomerInfoVO vo = new LeadsCustomerInfoVO();
        vo.setCheckAccess(true);
        return vo;
    }

    public static LeadsCustomerInfoVO buildFail(){
        LeadsCustomerInfoVO vo = new LeadsCustomerInfoVO();
        vo.setCheckAccess(false);
        return vo;
    }
}
