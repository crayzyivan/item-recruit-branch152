package com.item.service.client;

import com.item.dto.crm.Convert2CustomerResDTO;
import com.item.dto.crm.CreateLeadsInfoReqDTO;
import com.item.dto.crm.CrmAddCardPaymentReqDTO;
import com.item.dto.crm.CrmFeignResponse;
import com.item.dto.crm.PaymentInformationResDTO;
import com.item.dto.crm.UpdatePaymentReqDTO;
import com.item.framework.config.CrmFeignClientConfig;
import com.item.service.client.back.CrmFeignClientFallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * @author : lh
 */
@FeignClient(name = "crm-api", configuration = CrmFeignClientConfig.class, fallbackFactory = CrmFeignClientFallbackFactory.class)
public interface CrmFeignClient {

    @GetMapping(value = "/crm/customers/v1/{customerCodeOrId}/payments")
    CrmFeignResponse<PaymentInformationResDTO> getPaymentInformationList(@PathVariable(value = "customerCodeOrId") String customerCodeOrId,
                                                                         @RequestParam(value = "SearchValue") String SearchValue,
                                                                         @RequestParam(value = "PaymentType") Integer PaymentType,
                                                                         @RequestParam(value = "SubType") List<Integer> SubType,
                                                                         @RequestParam(value = "PageIndex") Integer PageIndex,
                                                                         @RequestParam(value = "PageSize") Integer PageSize);

    @PostMapping(value = "/crm/leads/v2/{leadsIdOrCode}/convert-to-customer")
    CrmFeignResponse<Convert2CustomerResDTO> convert2Customer(@PathVariable(value = "leadsIdOrCode") String leadsIdOrCode);

    @PostMapping(value = "/crm/customers/v1/{customerCodeOrId}/payments/card")
    CrmFeignResponse<Boolean> addCardPaymentInformation(@PathVariable("customerCodeOrId") String customerCodeOrId, @RequestBody CrmAddCardPaymentReqDTO addCardPaymentReq);

    @PostMapping(value = "/crm/modules/v2/{module}/datas")
    CrmFeignResponse<Long> createLeadsInfo(@PathVariable(value = "module") Integer module, @RequestBody CreateLeadsInfoReqDTO createLeadsInfoReqDTO);

    @DeleteMapping(value = "/crm/customers/v1/{customerCodeOrId}/payments")
    CrmFeignResponse<Boolean> deletePayment(@PathVariable(value = "customerCodeOrId") String customerCodeOrId, @RequestParam("payment_id") Long payment_id, @RequestParam("method_id") Long method_id);

    @PutMapping(value = "/crm/customers/v1/{customerCodeOrId}/payments/card/{method_id}")
    CrmFeignResponse<Boolean> updatePayment(@PathVariable(value = "customerCodeOrId") String customerCodeOrId, @PathVariable(value = "method_id") Long method_id, @RequestBody UpdatePaymentReqDTO updatePaymentReqDTO);
}
