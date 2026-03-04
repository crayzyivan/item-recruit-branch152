package com.item.service.impl;

import com.item.convert.CrmModeConvert;
import com.item.dto.LeadsCustomerCompanyDTO;
import com.item.dto.crm.CrmAddCardPaymentAdapterReqDTO;
import com.item.dto.crm.CrmAddCardPaymentDTO;
import com.item.dto.crm.DeletePaymentAdapterDTO;
import com.item.dto.crm.DeletePaymentDTO;
import com.item.dto.crm.PaymentInformationResDTO;
import com.item.dto.crm.QueryPaymentAdapterReqDTO;
import com.item.dto.crm.QueryPaymentReqDTO;
import com.item.dto.crm.UpdatePaymentAdapterDTO;
import com.item.dto.crm.UpdatePaymentDTO;
import com.item.dto.iam.IamUserContextDTO;
import static com.item.framework.constant.CommonResponseCode.COMMON_CUSTOMER_IS_NULL;
import com.item.framework.error.BusinessException;
import com.item.service.CrmDomainService;
import com.item.service.LeadsCustomerCompanyService;
import com.item.service.client.adapter.CrmRpcAdapter;
import com.item.util.CommonUtils;
import com.item.util.UserContextUtil;
import com.item.vo.PaymentInformationResVO;
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
public class CrmDomainServiceImpl implements CrmDomainService {
    private final CrmRpcAdapter crmRpcAdapter;
    private final CrmModeConvert crmModeConvert;
    private final LeadsCustomerCompanyService leadsCustomerCompanyService;

    @Override
    public Boolean addCardPaymentInformation(CrmAddCardPaymentDTO crmAddCardPaymentDTO) {
        //获取customer code
        IamUserContextDTO currentUserNeedLogin = UserContextUtil.getCurrentUserRecruitNeedLogin();
        //获取companyCode 查询customerCode数据
        LeadsCustomerCompanyDTO leadsCustomerCompany = leadsCustomerCompanyService.getCustomerByCompanyCode(currentUserNeedLogin.getCompanyCode());
        //如果没有数据 表明不是通过注册进入的 并且 没有调用检查接口 直接返回
        if (leadsCustomerCompany == null || StringUtils.isBlank(leadsCustomerCompany.getCrmCustomerCode())) {
            throw BusinessException.of(COMMON_CUSTOMER_IS_NULL);
        }
        CrmAddCardPaymentAdapterReqDTO crmAddCardPaymentAdapterReqDTO = crmModeConvert.convert2Adapter(crmAddCardPaymentDTO);
        crmAddCardPaymentAdapterReqDTO.setCardNumber(crmAddCardPaymentAdapterReqDTO.getCardNumber().replaceAll("[^0-9]", ""));
        crmAddCardPaymentAdapterReqDTO.setCustomerCodeOrId(leadsCustomerCompany.getCrmCustomerCode());
        Boolean success = crmRpcAdapter.addCardPaymentInformation(crmAddCardPaymentAdapterReqDTO);
        log.info("addCardPaymentInformation crmAddCardPaymentDTO {}, {}, {}, {}, {}", success, CommonUtils.maskCardNumber(crmAddCardPaymentDTO.getCardNumber()), crmAddCardPaymentDTO.getCardHolderName(), crmAddCardPaymentDTO.getPaymentType(), crmAddCardPaymentDTO.getSubType());
        return success;
    }

    @Override
    public PaymentInformationResVO getPaymentInformationList(QueryPaymentReqDTO queryPaymentReqDTO) {
        //获取companyCode 查询customerCode数据
        IamUserContextDTO currentUserNeedLogin = UserContextUtil.getCurrentUserRecruitNeedLogin();
        LeadsCustomerCompanyDTO leadsCustomerCompany = leadsCustomerCompanyService.getCustomerByCompanyCode(currentUserNeedLogin.getCompanyCode());
        if (leadsCustomerCompany == null || StringUtils.isBlank(leadsCustomerCompany.getCrmCustomerCode())) {
            log.warn("getPaymentInformationList crmCustomerCode is null or empty {}", queryPaymentReqDTO);
            return PaymentInformationResVO.buildEmpty();
        }
        QueryPaymentAdapterReqDTO queryPaymentAdapterReqDTO = crmModeConvert.convertFromAdapterDTO(queryPaymentReqDTO);
        queryPaymentAdapterReqDTO.setCustomerCodeOrId(leadsCustomerCompany.getCrmCustomerCode());
        PaymentInformationResDTO paymentInformationList = crmRpcAdapter.getPaymentInformationList(queryPaymentAdapterReqDTO);
        return crmModeConvert.convert2VO(paymentInformationList);
    }

    @Override
    public Boolean deletePayment(DeletePaymentDTO deletePaymentDTO) {
        //获取companyCode 查询customerCode数据
        IamUserContextDTO currentUserNeedLogin = UserContextUtil.getCurrentUserRecruitNeedLogin();
        LeadsCustomerCompanyDTO leadsCustomerCompany = leadsCustomerCompanyService.getCustomerByCompanyCode(currentUserNeedLogin.getCompanyCode());
        if (leadsCustomerCompany == null || StringUtils.isBlank(leadsCustomerCompany.getCrmCustomerCode())) {
            log.warn("getPaymentInformationList crmCustomerCode is null or empty {}", deletePaymentDTO);
            throw BusinessException.of(COMMON_CUSTOMER_IS_NULL);
        }
        DeletePaymentAdapterDTO deletePaymentAdapterDTO = crmModeConvert.convert2Adapter(deletePaymentDTO);
        deletePaymentAdapterDTO.setCustomerCodeOrId(leadsCustomerCompany.getCrmCustomerCode());
        return crmRpcAdapter.deletePayment(deletePaymentAdapterDTO);
    }

    @Override
    public Boolean updatePayment(UpdatePaymentDTO updatePaymentDTO) {
        //获取companyCode 查询customerCode数据
        IamUserContextDTO currentUserNeedLogin = UserContextUtil.getCurrentUserRecruitNeedLogin();
        LeadsCustomerCompanyDTO leadsCustomerCompany = leadsCustomerCompanyService.getCustomerByCompanyCode(currentUserNeedLogin.getCompanyCode());
        if (leadsCustomerCompany == null || StringUtils.isBlank(leadsCustomerCompany.getCrmCustomerCode())) {
            log.warn("getPaymentInformationList crmCustomerCode is null or empty {}", updatePaymentDTO);
            throw BusinessException.of(COMMON_CUSTOMER_IS_NULL);
        }
        UpdatePaymentAdapterDTO updatePaymentAdapterDTO = crmModeConvert.convert2Adapter(updatePaymentDTO);
        updatePaymentAdapterDTO.setCardNumber(updatePaymentAdapterDTO.getCardNumber().replaceAll("[^0-9]", ""));
        updatePaymentAdapterDTO.setCustomerCodeOrId(leadsCustomerCompany.getCrmCustomerCode());
        return crmRpcAdapter.updatePayment(updatePaymentAdapterDTO);
    }
}
