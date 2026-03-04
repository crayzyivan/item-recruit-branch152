package com.item.convert;

import com.item.dto.crm.Convert2CustomerAdapterResDTO;
import com.item.dto.crm.Convert2CustomerResDTO;
import com.item.dto.crm.CreateLeadsInfoAdapterReqDTO;
import com.item.dto.crm.CreateLeadsInfoReqDTO;
import com.item.dto.crm.CrmAddCardPaymentAdapterReqDTO;
import com.item.dto.crm.CrmAddCardPaymentDTO;
import com.item.dto.crm.CrmAddCardPaymentReqDTO;
import com.item.dto.crm.DeletePaymentAdapterDTO;
import com.item.dto.crm.DeletePaymentDTO;
import com.item.dto.crm.PaymentInformationResDTO;
import com.item.dto.crm.QueryPaymentAdapterReqDTO;
import com.item.dto.crm.QueryPaymentReqDTO;
import com.item.dto.crm.QueryPaymentRequestDTO;
import com.item.dto.crm.UpdatePaymentAdapterDTO;
import com.item.dto.crm.UpdatePaymentDTO;
import com.item.dto.crm.UpdatePaymentReqDTO;
import com.item.vo.PaymentInformationResVO;
import org.mapstruct.Mapper;

/**
 * @author : lh
 */
@Mapper(componentModel = "spring")
public interface CrmModeConvert {
    Convert2CustomerAdapterResDTO convert2Adapter(Convert2CustomerResDTO resDTO);

    CrmAddCardPaymentAdapterReqDTO convert2Adapter(CrmAddCardPaymentDTO reqDTO);

    UpdatePaymentAdapterDTO convert2Adapter(UpdatePaymentDTO reqDTO);

    DeletePaymentAdapterDTO convert2Adapter(DeletePaymentDTO reqDTO);

    QueryPaymentAdapterReqDTO convertFromAdapterDTO(QueryPaymentReqDTO queryPaymentReqDTO);
    QueryPaymentRequestDTO convertFromAdapterDTO(QueryPaymentAdapterReqDTO queryPaymentAdapterResDTO);
    CrmAddCardPaymentReqDTO convertFromAdapterDTO(CrmAddCardPaymentAdapterReqDTO adapterReqDTO);
    CreateLeadsInfoReqDTO convertFromAdapterDTO(CreateLeadsInfoAdapterReqDTO createLeads);
    UpdatePaymentReqDTO convertFromAdapterDTO(UpdatePaymentAdapterDTO updatePayment);

    PaymentInformationResVO convert2VO(PaymentInformationResDTO resDTO);
}
