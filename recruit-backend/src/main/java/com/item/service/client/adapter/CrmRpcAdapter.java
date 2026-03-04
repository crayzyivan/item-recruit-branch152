package com.item.service.client.adapter;

import com.item.convert.CrmModeConvert;
import com.item.dto.crm.Convert2CustomerAdapterResDTO;
import com.item.dto.crm.Convert2CustomerResDTO;
import com.item.dto.crm.CreateLeadsInfoAdapterReqDTO;
import com.item.dto.crm.CreateLeadsInfoReqDTO;
import com.item.dto.crm.CrmAddCardPaymentAdapterReqDTO;
import com.item.dto.crm.CrmAddCardPaymentReqDTO;
import com.item.dto.crm.CrmFeignResponse;
import com.item.dto.crm.DeletePaymentAdapterDTO;
import com.item.dto.crm.PaymentInformationResDTO;
import com.item.dto.crm.QueryPaymentAdapterReqDTO;
import com.item.dto.crm.QueryPaymentRequestDTO;
import com.item.dto.crm.UpdatePaymentAdapterDTO;
import com.item.dto.crm.UpdatePaymentReqDTO;
import com.item.framework.constant.CommonResponseCode;
import com.item.framework.error.BusinessException;
import com.item.service.client.CrmFeignClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

/**
 * @author : lh
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CrmRpcAdapter {
    private final CrmFeignClient crmClient;
    private final CrmModeConvert crmModeConvert;

    public PaymentInformationResDTO getPaymentInformationList(QueryPaymentAdapterReqDTO queryPaymentAdapterResDTO) {
        if (StringUtils.isBlank(queryPaymentAdapterResDTO.getCustomerCodeOrId())) {
            throw BusinessException.of(CommonResponseCode.COMMON_CUSTOMER_CODE_OR_ID_IS_NULL);
        }
        QueryPaymentRequestDTO queryPaymentResDTO = crmModeConvert.convertFromAdapterDTO(queryPaymentAdapterResDTO);
        CrmFeignResponse<PaymentInformationResDTO> singleBusinessDataDetails = crmClient.getPaymentInformationList(queryPaymentAdapterResDTO.getCustomerCodeOrId(),
                queryPaymentResDTO.getSearchValue(), queryPaymentResDTO.getPaymentType(), queryPaymentResDTO.getSubType(), queryPaymentResDTO.getPageIndex(), queryPaymentResDTO.getPageSize());
        return getData(singleBusinessDataDetails);
    }

    public Convert2CustomerAdapterResDTO convert2Customer(String leadsIdOrCode) {
        CrmFeignResponse<Convert2CustomerResDTO> singleBusinessDataDetails = crmClient.convert2Customer(leadsIdOrCode);
        Convert2CustomerResDTO data = getData(singleBusinessDataDetails);
        return crmModeConvert.convert2Adapter(data);
    }

    public Boolean addCardPaymentInformation(CrmAddCardPaymentAdapterReqDTO addCardPayment) {
        if (StringUtils.isBlank(addCardPayment.getCustomerCodeOrId())) {
            throw BusinessException.of(CommonResponseCode.COMMON_CUSTOMER_CODE_OR_ID_IS_NULL);
        }
        CrmAddCardPaymentReqDTO crmAddCardPaymentReqDTO = crmModeConvert.convertFromAdapterDTO(addCardPayment);
        CrmFeignResponse<Boolean> response = crmClient.addCardPaymentInformation(addCardPayment.getCustomerCodeOrId(), crmAddCardPaymentReqDTO);
        return getData(response);
    }


    public Long createLeadsInfo(CreateLeadsInfoAdapterReqDTO createLeadsInfo) {
        if (createLeadsInfo.getModule() == null) {
            throw BusinessException.of(CommonResponseCode.COMMON_MODULE_IS_NULL);
        }
        CreateLeadsInfoReqDTO createLeadsInfoReqDTO = crmModeConvert.convertFromAdapterDTO(createLeadsInfo);
        CrmFeignResponse<Long> response = crmClient.createLeadsInfo(createLeadsInfo.getModule(), createLeadsInfoReqDTO);
        return getData(response);
    }

    public Boolean deletePayment(DeletePaymentAdapterDTO deletePaymentAdapterDTO){
        if (deletePaymentAdapterDTO.getMethodId() == null) {
            throw BusinessException.of(CommonResponseCode.COMMON_MODULE_IS_NULL);
        }
        if (deletePaymentAdapterDTO.getPaymentId() == null) {
            throw BusinessException.of(CommonResponseCode.COMMON_PAYMENT_ID_IS_NULL);
        }
        if (StringUtils.isBlank(deletePaymentAdapterDTO.getCustomerCodeOrId())) {
            throw BusinessException.of(CommonResponseCode.COMMON_CUSTOMER_CODE_OR_ID_IS_NULL);
        }
        CrmFeignResponse<Boolean> response = crmClient.deletePayment(deletePaymentAdapterDTO.getCustomerCodeOrId(), deletePaymentAdapterDTO.getPaymentId(), deletePaymentAdapterDTO.getMethodId());
        return getData(response);
    }

    public Boolean updatePayment(UpdatePaymentAdapterDTO updatePaymentAdapterDTO){
        if (updatePaymentAdapterDTO.getMethodId() == null) {
            throw BusinessException.of(CommonResponseCode.COMMON_MODULE_IS_NULL);
        }
        if (StringUtils.isBlank(updatePaymentAdapterDTO.getCustomerCodeOrId())) {
            throw BusinessException.of(CommonResponseCode.COMMON_CUSTOMER_CODE_OR_ID_IS_NULL);
        }
        UpdatePaymentReqDTO updatePaymentReqDTO = crmModeConvert.convertFromAdapterDTO(updatePaymentAdapterDTO);
        CrmFeignResponse<Boolean> response = crmClient.updatePayment(updatePaymentAdapterDTO.getCustomerCodeOrId(), updatePaymentAdapterDTO.getMethodId(), updatePaymentReqDTO);
        return getData(response);
    }


    /**
     * 解析响应体
     *
     * @param crmResponse
     * @param <T>
     * @return
     */
    private <T> T getData(CrmFeignResponse<T> crmResponse) {
        log.info("CrmFeignResponse crm {}", crmResponse);
        if (crmResponse.getSuccess() == null || !crmResponse.getSuccess()) {
            log.error("get user info failed {}", crmResponse);
            return null;
        }
        if (crmResponse.getData() == null) {
            log.warn("get user info data is null {} ", crmResponse);
            return null;
        }
        return crmResponse.getData();
    }
}
