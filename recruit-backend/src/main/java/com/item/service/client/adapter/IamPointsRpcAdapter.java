package com.item.service.client.adapter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.item.dto.iam.CurrencyToPointsRuleDTO;
import com.item.dto.iam.FeignResponse;
import com.item.dto.iam.IamPointsAddDTO;
import com.item.dto.iam.IamPointsCancelFreezeDTO;
import com.item.dto.iam.IamPointsConfirmFreezeDTO;
import com.item.dto.iam.IamPointsDeductDTO;
import com.item.dto.iam.IamPointsFreezeDTO;
import com.item.dto.iam.IamPointsInitAccountDTO;
import com.item.dto.iam.IamPointsMixedConsumeDTO;
import com.item.dto.iam.IamPointsTopUpDTO;
import com.item.dto.iam.IamPointsTransactionReqDTO;
import com.item.dto.iam.IamPointsTransactionResDTO;
import com.item.dto.iam.IamPointsTransferDTO;
import com.item.dto.iam.IamUserPointsBaseDTO;
import com.item.dto.iam.IamUserPointsDTO;
import com.item.dto.iam.IamUserPrimaryDTO;
import com.item.dto.iam.PagedResult;
import com.item.dto.iam.PointRuleDTO;
import com.item.dto.iam.SubscriptionTrialDTO;
import com.item.service.client.IamFeignClient;
import com.item.util.CommonUtils;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * @author : lh
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class IamPointsRpcAdapter {
    private final IamFeignClient iamFeignClient;

    /**
     * Query user points
     * @param userPointsBaseDTO
     * @return
     */
    public IamUserPointsDTO getCreditCenterPoints(IamUserPointsBaseDTO userPointsBaseDTO) {
        log.info("getCreditCenterPoints userPointsBaseDTO={}", userPointsBaseDTO);
        FeignResponse<IamUserPointsDTO> iamResponse = iamFeignClient.getCreditCenterPoints(userPointsBaseDTO);
        return CommonUtils.getData(iamResponse);
    }

    /**
     * Transfer points
     * @param pointsTransferDTO
     * @return
     */
    public FeignResponse<Object> transferCreditCenterPoints(IamPointsTransferDTO pointsTransferDTO) {
        log.info("transferCreditCenterPoints pointsTransferDTO={}", pointsTransferDTO);
        try{
            return iamFeignClient.transferCreditCenterPoints(pointsTransferDTO);
        }catch (FeignException e){
            return handlerFeignException(e, new TypeReference<FeignResponse<Object>>(){});
        }
    }

    /**
     * Top up points
     * @param pointsTopUpDTO
     * @return
     */
    public FeignResponse<Boolean> topUpCreditCenterPoints(IamPointsTopUpDTO pointsTopUpDTO) {
        log.info("topUpCreditCenterPoints pointsTopUpDTO={}", pointsTopUpDTO);
        try{
            return iamFeignClient.topUpCreditCenterPoints(pointsTopUpDTO);
        }catch (FeignException e){
            return handlerFeignException(e, new TypeReference<FeignResponse<Boolean>>(){});
        }
    }

    /**
     * Mixed consume points
     * @param mixedConsumePointsDTO
     * @return
     */
    public FeignResponse<Object> mixConsumeCreditCenterPoints(IamPointsMixedConsumeDTO mixedConsumePointsDTO) {
        log.info("mixConsumeCreditCenterPoints pointsTransferDTO={}", mixedConsumePointsDTO);
        try{
            return iamFeignClient.mixConsumeCreditCenterPoints(mixedConsumePointsDTO);
        }catch (FeignException e){
            return handlerFeignException(e, new TypeReference<FeignResponse<Object>>(){});
        }
    }

    /**
     * Add points
     * @param pointsAddDTO
     * @return
     */
    public FeignResponse<Object> addCreditCenterPoints(IamPointsAddDTO pointsAddDTO){
        log.info("addCreditCenterPoints pointsAddDTO={}", pointsAddDTO);
        try{
            return iamFeignClient.addCreditCenterPoints(pointsAddDTO);
        }catch (FeignException e){
            return handlerFeignException(e, new TypeReference<FeignResponse<Object>>(){});
        }
    }

    /**
     * Initialize point account
     * @param pointsInitAccountDTO
     * @return
     */
    public FeignResponse<Object> initCreditCenterPoints(IamPointsInitAccountDTO pointsInitAccountDTO){
        log.info("addCreditCenterPoints pointsInitAccountDTO={}", pointsInitAccountDTO);
        try{
            return iamFeignClient.initCreditCenterPoints(pointsInitAccountDTO);
        }catch (FeignException e){
            return handlerFeignException(e, new TypeReference<FeignResponse<Object>>(){});
        }
    }

    /**
     *  Freeze points
     * @param pointsFreezeDTO
     * @return
     */
    public FeignResponse<Object> freezeCreditCenterPoints(IamPointsFreezeDTO pointsFreezeDTO){
        log.info("freezeCreditCenterPoints pointsFreezeDTO={}", pointsFreezeDTO);
        try{
            return iamFeignClient.freezeCreditCenterPoints(pointsFreezeDTO);
        }catch (FeignException e){
            return handlerFeignException(e, new TypeReference<FeignResponse<Object>>(){});
        }
    }

    /**
     * Consume points
     * One-time consumption of points
     * @param pointsDeductDTO
     * @return
     */
    public FeignResponse<Object> deductCreditCenterPoints(IamPointsDeductDTO pointsDeductDTO){
        log.info("deductCreditCenterPoints pointsDeductDTO={}", pointsDeductDTO);
        try{
            return iamFeignClient.deductCreditCenterPoints(pointsDeductDTO);
        }catch (FeignException e){
            return handlerFeignException(e, new TypeReference<FeignResponse<Object>>(){});
        }
    }

    /**
     * Confirm frozen points
     * @param pointsConfirmFreezeDTO
     * @return
     */
    public FeignResponse<Object> confirmFreezeCreditCenterPoints(IamPointsConfirmFreezeDTO pointsConfirmFreezeDTO){
        log.info("confirmFreezeCreditCenterPoints pointsConfirmFreezeDTO={}", pointsConfirmFreezeDTO);
        try{
            return iamFeignClient.confirmFreezeCreditCenterPoints(pointsConfirmFreezeDTO);
        }catch (FeignException e){
            return handlerFeignException(e, new TypeReference<FeignResponse<Object>>(){});
        }
    }

    /**
     * Cancel frozen points
     * @param pointsCancelFreezeDTO
     * @return
     */
    public FeignResponse<Object> cancelFreezeCreditCenterPoints(IamPointsCancelFreezeDTO pointsCancelFreezeDTO){
        log.info("confirmFreezeCreditCenterPoints pointsCancelFreezeDTO={}", pointsCancelFreezeDTO);
        try{
            return iamFeignClient.cancelFreezeCreditCenterPoints(pointsCancelFreezeDTO);
        }catch (FeignException e){
            return  handlerFeignException(e, new TypeReference<FeignResponse<Object>>(){});
        }
    }


    /**
     * 查询积分规则
     * @return
     */
    public FeignResponse<PagedResult<CurrencyToPointsRuleDTO>> pointExchangeRulePage(PointRuleDTO pointRuleDTO){
        try{
            return iamFeignClient.pointExchangeRulePage(pointRuleDTO.getApplicationCode(), pointRuleDTO.getCurrencyCode(), pointRuleDTO.getStatus(), pointRuleDTO.getPage(), pointRuleDTO.getSize());
        }catch (FeignException e){
            return  handlerFeignException(e, new TypeReference<FeignResponse<PagedResult<CurrencyToPointsRuleDTO>>>(){});
        }
    }

    /**
     * 查询积分消费列表
     * @return
     */
    public PagedResult<IamPointsTransactionResDTO> pagePointsTransaction(IamPointsTransactionReqDTO pointsTransactionReqDTO){
        FeignResponse<PagedResult<IamPointsTransactionResDTO>> response = iamFeignClient.pagePointsTransaction(pointsTransactionReqDTO);
        return CommonUtils.getData(response);
    }

    /**
     * 获取计费模式
     * @param companyCode
     * @return
     */
    public SubscriptionTrialDTO getSubscriptionTrial(String companyCode){
        FeignResponse<SubscriptionTrialDTO> subscriptionTrial = iamFeignClient.getSubscriptionTrial(companyCode);
        return CommonUtils.getData(subscriptionTrial);
    }

    /**
     * 获取主用户
     * @param companyCode
     * @return
     */
    public IamUserPrimaryDTO queryUserPrimary(String companyCode){
        FeignResponse<IamUserPrimaryDTO> userPrimary = iamFeignClient.queryUserPrimary(companyCode);
        return CommonUtils.getData(userPrimary);
    }


    /**
     * 处理Feign调用异常
     * @param e Feign异常
     * @return 标准响应对象
     */
    public <T> FeignResponse<T> handlerFeignException(FeignException e, TypeReference<FeignResponse<T>> typeRef) {
        FeignResponse<T> errorResponse = new FeignResponse<>();
        errorResponse.setSuccess(false);
        errorResponse.setMsg("Service invocation failed");

        String content = e.contentUTF8();
        if (StringUtils.isBlank(content)) {
            errorResponse.setMsg(e.getMessage());
            return errorResponse;
        }

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            FeignResponse<T> originalResponse = objectMapper.readValue(content, typeRef);

            Optional.ofNullable(originalResponse.getCode()).ifPresent(errorResponse::setCode);
            if (StringUtils.isNotBlank(originalResponse.getMsg())) {
                errorResponse.setMsg(originalResponse.getMsg());
            }

        } catch (JsonProcessingException ex) {
            log.error("Failed to parse Feign response content: {}", content, ex);
            errorResponse.setMsg(StringUtils.defaultIfBlank(e.getMessage(), "Invalid service response format"));
        }

        return errorResponse;
    }


}
