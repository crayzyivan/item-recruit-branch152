package com.item.dto.crm;

import lombok.Data;

import java.util.List;

/**
 * <p>
 * Payment information response DTO
 * </p>
 *
 * @author lh
 */
@Data
public class PaymentInformationResDTO {
    
    /**
     * Total number of pages
     */
    private Integer totalPage;
    
    /**
     * Number of items per page
     */
    private Integer pageCount;
    
    /**
     * Current page index
     */
    private Integer pageIndex;
    
    /**
     * Page size
     */
    private Integer pageSize;
    
    /**
     * Total number of records
     */
    private Integer total;
    
    /**
     * Number of data items in current page
     */
    private Integer dataCount;
    
    /**
     * Payment information data list
     */
    private List<PaymentInformationDataDTO> data;
}