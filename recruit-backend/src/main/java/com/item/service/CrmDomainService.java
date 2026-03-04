package com.item.service;

import com.item.dto.crm.CrmAddCardPaymentDTO;
import com.item.dto.crm.DeletePaymentDTO;
import com.item.dto.crm.QueryPaymentReqDTO;
import com.item.dto.crm.UpdatePaymentDTO;
import com.item.vo.PaymentInformationResVO;

/**
 * @author : lh
 */
public interface CrmDomainService {
    Boolean addCardPaymentInformation(CrmAddCardPaymentDTO crmAddCardPaymentDTO);

    PaymentInformationResVO getPaymentInformationList(QueryPaymentReqDTO queryPaymentReqDTO);

    Boolean deletePayment(DeletePaymentDTO deletePaymentDTO);

    Boolean updatePayment(UpdatePaymentDTO updatePaymentDTO);
}
