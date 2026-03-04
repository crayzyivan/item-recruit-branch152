package com.item.service.client.back;

import com.item.dto.crm.Convert2CustomerResDTO;
import com.item.dto.crm.CreateLeadsInfoReqDTO;
import com.item.dto.crm.CrmAddCardPaymentReqDTO;
import com.item.dto.crm.CrmFeignResponse;
import com.item.dto.crm.PaymentInformationResDTO;
import com.item.dto.crm.UpdatePaymentReqDTO;
import com.item.framework.constant.CrmConvertResponseCode;
import com.item.service.client.CrmFeignClient;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * @author : lh
 * IAM服务调用异常回调处理
 */
@Slf4j
public class CrmFeignClientFallback implements CrmFeignClient {
    private final Throwable cause;

    public CrmFeignClientFallback(Throwable cause) {
        this.cause = cause;
    }

    @Override
    public CrmFeignResponse<PaymentInformationResDTO> getPaymentInformationList(String customerCodeOrId, String SearchValue,
                                                                             Integer PaymentType, List<Integer> SubType,
                                                                             Integer PageIndex, Integer PageSize) {
        log.error("Failed to getSingleBusinessDataDetails: customerCodeOrId {} SearchValue {} PaymentType {} SubType {} PageIndex {} PageSize {}",
                customerCodeOrId, SearchValue, PaymentType, SubType, PageIndex, PageSize, cause);
        return build();
    }

    @Override
    public CrmFeignResponse<Convert2CustomerResDTO> convert2Customer(String leadsIdOrCode) {
        log.error("Failed to convert2Customer: leadsIdOrCode {}", leadsIdOrCode, cause);
        return build();
    }

    @Override
    public CrmFeignResponse<Boolean> addCardPaymentInformation(String customerCodeOrId, CrmAddCardPaymentReqDTO addCardPaymentReq) {
        log.error("Failed to addCardPaymentInformation: customerCodeOrId {} addCardPaymentReq {}", customerCodeOrId, addCardPaymentReq, cause);
        return build();
    }

    @Override
    public CrmFeignResponse<Long> createLeadsInfo(Integer module, CreateLeadsInfoReqDTO createLeadsInfoReqDTO) {
        log.error("Failed to createLeadsInfo: module {} createLeadsInfoReqDTO {}", module, createLeadsInfoReqDTO, cause);
        return build();
    }

    @Override
    public CrmFeignResponse<Boolean> deletePayment(String customerCodeOrId, Long payment_id, Long method_id) {
        log.error("Failed to deletePayment: customerCodeOrId {} payment_id {} method_id {}", customerCodeOrId, payment_id, method_id, cause);
        return build();
    }

    @Override
    public CrmFeignResponse<Boolean> updatePayment(String customerCodeOrId, Long method_id, UpdatePaymentReqDTO updatePaymentReqDTO) {
        log.error("Failed to addCardPaymentInformation: customerCodeOrId {} method_id {} updatePaymentReqDTO {}", customerCodeOrId, method_id, updatePaymentReqDTO, cause);
        return build();
    }


    private <T> CrmFeignResponse<T> build(boolean success, String code, String message, T data) {
        CrmFeignResponse<T> objectIamResponse = new CrmFeignResponse<>();
        objectIamResponse.setCode(code);
        objectIamResponse.setData(data);
        objectIamResponse.setSuccess(success);
        objectIamResponse.setMsg(message);
        return objectIamResponse;
    }

    private <T> CrmFeignResponse<T> build(String code, String message, T data) {
        return build(false, code, message, data);
    }

    private <T> CrmFeignResponse<T> build(T data) {
        return build(CrmConvertResponseCode.CODE_FAIL.getCrmCode(), CrmConvertResponseCode.CODE_FAIL.getCrmResponseCode().getMsg(), data);
    }

    private <T> CrmFeignResponse<T> build() {
        return build(null);
    }

}
