package com.item.dto.crm;

import com.google.common.collect.Lists;
import static com.item.framework.constant.CommonConstants.StrConstants.CRM_LEADS_MODULE_3_COMPANY_NAME;
import static com.item.framework.constant.CommonConstants.StrConstants.CRM_LEADS_MODULE_3_DOMAIN_NAME;
import static com.item.framework.constant.CommonConstants.StrConstants.CRM_LEADS_MODULE_3_PHONE;
import com.item.framework.constant.CrmModuleEnum;
import lombok.Data;

import java.util.List;

/**
 * @author : lh
 */
@Data
public class CreateLeadsInfoAdapterReqDTO {
    private Long id;
    private List<LeadsPropertiesDTO> properties;
    private Integer module;

    public static CreateLeadsInfoAdapterReqDTO buildLeadsCompany(String companyName){
        LeadsPropertiesDTO companyNameProperty = LeadsPropertiesDTO.builder().fieldName(CRM_LEADS_MODULE_3_COMPANY_NAME).value(companyName)
                .build();
        LeadsPropertiesDTO domainNameProperty = LeadsPropertiesDTO.builder().fieldName(CRM_LEADS_MODULE_3_DOMAIN_NAME).value("")
                .build();
        LeadsPropertiesDTO phoneProperty = LeadsPropertiesDTO.builder().fieldName(CRM_LEADS_MODULE_3_PHONE).value("")
                .build();
        CreateLeadsInfoAdapterReqDTO reqDTO = new CreateLeadsInfoAdapterReqDTO();
        reqDTO.setProperties(Lists.newArrayList(companyNameProperty, domainNameProperty, phoneProperty));
        reqDTO.setModule(CrmModuleEnum.COMPANY.getModule());
        return reqDTO;
    }
}
