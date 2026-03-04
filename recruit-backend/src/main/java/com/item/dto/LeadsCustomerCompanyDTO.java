package com.item.dto;

import com.item.dto.crm.Convert2CustomerAdapterResDTO;
import com.item.dto.iam.IamSignUpAdapterResDTO;
import com.item.framework.constant.CrmLeadsCustomerStatus;
import com.item.util.CommonUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;

/**
 * <p>
 * lead customer company 映射关系DTO
 * </p>
 *
 * @author lh on 2025/7/29
 * @since 1.0.0
 */
@Slf4j
@Data
public class LeadsCustomerCompanyDTO implements Serializable {
    
    private Long id;
    
    private Long crmLeadsId;
    
    private Long crmCustomerId;
    
    private String crmCustomerCode;
    
    private Integer crmLeadsCustomerStatus;
    
    private String centralCompanyCode;
    
    private Long centralLeadCompanyId;
    
    private Long centralManagerId;

    private Long centralCompanyId;
    
    private String responseMsg;

    private Integer origin;

    public static LeadsCustomerCompanyDTO fillField(Convert2CustomerAdapterResDTO customer, IamSignUpAdapterResDTO iam) {
        LeadsCustomerCompanyDTO dto = new LeadsCustomerCompanyDTO();
        if (customer != null) {
            dto.setCrmCustomerId(customer.getCustomerId());
            dto.setCrmCustomerCode(customer.getCustomerCode());
            dto.setResponseMsg(CommonUtils.subStr(customer.getErrorMessage(), 512));
            if (StringUtils.isNotBlank(dto.getResponseMsg())) {
                log.error("LeadsCustomerCompanyDTO fillField {}", customer);
                dto.setCrmLeadsCustomerStatus(CrmLeadsCustomerStatus.CONVERT_ERROR.getCode());
            } else {
                dto.setCrmLeadsCustomerStatus(CrmLeadsCustomerStatus.CONVERT_FINISH.getCode());
            }
        } else {
            dto.setCrmLeadsCustomerStatus(CrmLeadsCustomerStatus.INIT.getCode());
        }
        if (iam != null) {
            dto.setCrmLeadsId(Long.parseLong(iam.getLeadCompanyId()));
            dto.setCentralLeadCompanyId(Long.parseLong(iam.getLeadCompanyId()));
            dto.setCentralManagerId(iam.getManagerId());
            dto.setCentralCompanyCode(iam.getCompanyCode());
            dto.setCentralCompanyId(iam.getCompanyId());
        }
        return dto;
    }
}