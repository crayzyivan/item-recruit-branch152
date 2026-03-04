package com.item.dto.crm;

import lombok.Data;

import java.util.List;

/**
 * @author : lh
 */
@Data
public class CreateLeadsInfoReqDTO {
    private Long id;
    private List<LeadsPropertiesDTO> properties;
    /**
     * '{
     *     "properties": [
     *         {
     *             "fieldName": "company_name",
     *             "value": "testcompany123"
     *         },
     *         {
     *             "fieldName": "domain_name",
     *             "value": ""
     *         },
     *         {
     *             "fieldName": "phone",
     *             "value": ""
     *         }
     *     ],
     *     "id": 0
     * }'
     */
}
