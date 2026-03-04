package com.item.service.client.back;

import com.item.dto.AuthorizationCodeTokenDto;
import com.item.dto.iam.*;
import com.item.framework.constant.FeignClientResponseCode;
import com.item.service.client.IamFeignClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.MultiValueMap;

/**
 * @author : lh
 * IAM服务调用异常回调处理
 */
@Slf4j
public class IamFeignClientFallback implements IamFeignClient {
    private final Throwable cause;

    public IamFeignClientFallback(Throwable cause) {
        this.cause = cause;
    }

    @Override
    public AuthorizationCodeTokenDto getAuthorizationCodeToken(MultiValueMap<String, String> map) {
        log.error("Failed to get authorization token map {}", map, cause);
        return null;
    }

    @Override
    public FeignResponse<IamUserDTO> userInfo(Long userId) {
        log.error("Failed to get user info for userId: {}", userId, cause);
        return build();
    }

    @Override
    public FeignResponse<IamCompanyDTO> getCompanyInfoByCode(String companyCode) {
        log.error("Failed to get company info for companyCode: {}", companyCode, cause);
        return build();
    }

    @Override
    public FeignResponse<IamCompanyDetailDTO> getCompanyDetailCode(String companyCode) {
        log.error("Failed to get company detail for companyCode: {}", companyCode, cause);
        return build();
    }

    @Override
    public FeignResponse<IamUserPointsDTO> getCreditCenterPoints(IamUserPointsBaseDTO userPointsBaseDTO) {
        log.error("getCreditCenterPoints for userPointsBaseDTO: {}", userPointsBaseDTO, cause);
        return build();
    }

    @Override
    public FeignResponse<Object> transferCreditCenterPoints(IamPointsTransferDTO pointsTransferDTO) {
        log.error("transferCreditCenterPoints for pointsTransferDTO: {}", pointsTransferDTO, cause);
        return build();
    }

    @Override
    public FeignResponse<Boolean> topUpCreditCenterPoints(IamPointsTopUpDTO pointsTopUpDTO) {
        log.error("topUpCreditCenterPoints for pointsTopUpDTO: {}", pointsTopUpDTO, cause);
        return build();
    }

    @Override
    public FeignResponse<Object> mixConsumeCreditCenterPoints(IamPointsMixedConsumeDTO mixedConsumePointsDTO) {
        log.error("mixConsumeCreditCenterPoints for mixedConsumePointsDTO: {}", mixedConsumePointsDTO, cause);
        return build();
    }

    @Override
    public FeignResponse<Object> addCreditCenterPoints(IamPointsAddDTO pointsAddDTO) {
        log.error("addCreditCenterPoints for pointsAddDTO: {}", pointsAddDTO, cause);
        return build();
    }

    @Override
    public FeignResponse<Object> initCreditCenterPoints(IamPointsInitAccountDTO pointsInitAccountDTO) {
        log.error("initCreditCenterPoints for pointsInitAccountDTO: {}", pointsInitAccountDTO, cause);
        return build();
    }

    @Override
    public FeignResponse<Object> freezeCreditCenterPoints(IamPointsFreezeDTO pointsFreezeDTO) {
        log.error("freezeCreditCenterPoints for pointsFreezeDTO: {}", pointsFreezeDTO, cause);
        return build();
    }

    @Override
    public FeignResponse<Object> deductCreditCenterPoints(IamPointsDeductDTO pointsDeductDTO) {
        log.error("deductCreditCenterPoints for pointsDeductDTO: {}", pointsDeductDTO, cause);
        return build();
    }

    @Override
    public FeignResponse<Object> confirmFreezeCreditCenterPoints(IamPointsConfirmFreezeDTO pointsConfirmFreezeDTO) {
        log.error("confirmFreezeCreditCenterPoints for pointsConfirmFreezeDTO: {}", pointsConfirmFreezeDTO, cause);
        return build();
    }

    @Override
    public FeignResponse<Object> cancelFreezeCreditCenterPoints(IamPointsCancelFreezeDTO pointsCancelFreezeDTO) {
        log.error("cancelFreezeCreditCenterPoints for pointsCancelFreezeDTO: {}", pointsCancelFreezeDTO, cause);
        return build();
    }

    @Override
    public FeignResponse<PagedResult<CurrencyToPointsRuleDTO>> pointExchangeRulePage(String applicationCode, String currencyCode, Integer status, Integer page, Integer size) {
        log.error("queryPointRulePage for applicationCode:{},currencyCode:{},status:{},page:{},size:{}", applicationCode,currencyCode,status,page,size, cause);
        return build();
    }

    @Override
    public FeignResponse<PagedResult<IamPointsTransactionResDTO>> pagePointsTransaction(IamPointsTransactionReqDTO pointsTransactionReqDTO) {
        log.error("Failed pagePointsTransaction for pointsTransactionReqDTO:{}", pointsTransactionReqDTO, cause);
        return build();
    }

    @Override
    public FeignResponse<IamUserRegisterResDTO> iamUserRegister(IamUserRegisterReqDTO registerDTO) {
        log.error("Failed iamUserRegister for registerDTO:{}", registerDTO, cause);
        return build();
    }

    @Override
    public FeignResponse<IamSignUpManagerCompanyResDTO> signUpManagerCompany(IamSignUpManagerCompanyDTO managerCompany) {
        log.error("Failed to signUpManagerCompany : {}", managerCompany, cause);
        return build();
    }

    @Override
    public FeignResponse<IamCreateSubUserResDTO> createSubUser(IamCreateSubUserDTO createSubUser) {
        log.error("Failed to createSubUser : {}", createSubUser, cause);
        return build();
    }

    @Override
    public FeignResponse<ExistsCompanyResDTO> existCompany(ExistsCompanyReqDTO existsCompany) {
        log.error("Failed to existCompany : {}", existsCompany, cause);
        return build();
    }

    @Override
    public FeignResponse<ExistsUserResDTO> existUser(ExistsUserReqDTO existsUser) {
        log.error("Failed to existsUser : {}", existsUser, cause);
        return build();
    }

    @Override
    public FeignResponse<SubscriptionTrialDTO> getSubscriptionTrial(String companyCode) {
        log.error("Failed to getSubscriptionTrial : {}", companyCode, cause);
        return build();
    }

    @Override
    public FeignResponse<IamTicketResDTO> issueTicket(IamTicketReqDTO request) {
        log.error("Failed to issueTicket : {}", request, cause);
        return build();
    }

    @Override
    public FeignResponse<IamTokenExchangeResDTO> exchangeTicket(IamTicketExchangeReqDTO request) {
        log.error("Failed to exchangeTicket : {}", request, cause);
        return build();
    }

    @Override
    public FeignResponse<IamCreateUserResDTO> createUser(IamCreateUserReqDTO createUserReq) {
        log.error("Failed to createUser for userName: {}, companyCode: {}, email: {}", 
                createUserReq != null ? createUserReq.getUserName() : "null",
                createUserReq != null ? createUserReq.getCompanyCode() : "null", 
                createUserReq != null ? createUserReq.getEmail() : "null", cause);
        return build();
    }

    @Override
    public FeignResponse<IamUserDetailDTO> queryUserByIdentifier(String identifier) {
        log.error("Failed to queryUserByIdentifier for identifier: {}", identifier, cause);
        return build();
    }

    @Override
    public FeignResponse<IamUserPrimaryDTO> queryUserPrimary(String identifier) {
        log.error("Failed to queryUserPrimary for identifier: {}", identifier, cause);
        return build();
    }

    @Override
    public FeignResponse<IamUserDetailResponseDTO> getUserDetailByIdentifier(String identifier) {
        log.error("Failed to getUserDetailByIdentifier for identifier: {}", identifier, cause);
        return build();
    }

    @Override
    public FeignResponse<Void> switchUserTenant(Long userId, String tenantId) {
        log.error("Failed to switchUserTenant for userId: {} tenantId {}", userId, tenantId, cause);
        return build();
    }

    @Override
    public FeignResponse<IamAdminDetailDTO> queryAdminByCompanyCode(String companyCode) {
        log.error("Failed to queryAdminByCompanyCode for companyCode: {}", companyCode, cause);
        return build();
    }

    private <T> FeignResponse<T> build(boolean success, int code, String message, T data) {
        FeignResponse<T> objectIamResponse = new FeignResponse<>();
        objectIamResponse.setCode(code);
        objectIamResponse.setData(data);
        objectIamResponse.setSuccess(success);
        objectIamResponse.setMsg(message);
        return objectIamResponse;
    }

    private <T> FeignResponse<T> build(int code, String message, T data) {
        return build(false, code, message, data);
    }

    private <T> FeignResponse<T> build(T data) {
        return build(FeignClientResponseCode.FEIGN_CLIENT_FAIL.getCode(), FeignClientResponseCode.FEIGN_CLIENT_FAIL.getMsg(), data);
    }

    private <T> FeignResponse<T> build() {
        return build(null);
    }
}
