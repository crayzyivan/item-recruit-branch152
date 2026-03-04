package com.item.vo;

import com.item.dto.crm.PaymentInformationDataDTO;
import lombok.Data;

import java.util.Collections;
import java.util.List;

/**
 * <p>
 * Payment information response DTO
 * </p>
 *
 * @author lh
 */
@Data
public class PaymentInformationResVO {
    
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

    public static PaymentInformationResVO buildEmpty() {
        PaymentInformationResVO res = new PaymentInformationResVO();
        res.setData(Collections.emptyList());
        res.setTotal(0);
        res.setDataCount(0);
        res.setPageCount(0);
        res.setTotalPage(0);
        return res;
    }
}