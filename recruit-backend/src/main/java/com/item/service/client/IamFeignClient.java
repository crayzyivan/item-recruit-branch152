package com.item.service.client;

import com.item.dto.AuthorizationCodeTokenDto;
import com.item.dto.iam.*;
import com.item.framework.config.IamFeignClientConfig;
import com.item.service.client.back.IamFeignClientFallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author : lh
 */
@FeignClient(name = "iam-app", configuration = IamFeignClientConfig.class, fallbackFactory = IamFeignClientFallbackFactory.class)
public interface IamFeignClient {
    @PostMapping(value = "/oauth2/token")
    AuthorizationCodeTokenDto getAuthorizationCodeToken(@RequestBody MultiValueMap<String, String> map);

    @GetMapping(value = "/v1/users/{userId}")
    FeignResponse<IamUserDTO> userInfo(@PathVariable(value = "userId") Long userId);

    @GetMapping(value = "/v1/company/{companyCode}")
    FeignResponse<IamCompanyDTO> getCompanyInfoByCode(@PathVariable(value = "companyCode") String companyCode);

    @GetMapping(value = "/company/code/{companyCode}")
    FeignResponse<IamCompanyDetailDTO> getCompanyDetailCode(@PathVariable(value = "companyCode") String companyCode);

    /**
     *  Creates a companny and create a manager user for the registered company
     *
     *  https://apifox.com/apidoc/shared/a35148ba-4cde-43e2-a450-7b245bf8462d/api-287034056
     */
    @PostMapping(value = "/v1/register/company")
    FeignResponse<IamSignUpManagerCompanyResDTO> signUpManagerCompany(@RequestBody IamSignUpManagerCompanyDTO managerCompany);

    /**
     * Creates a new sub user within the current company
     *
     * https://apifox.com/apidoc/shared/a35148ba-4cde-43e2-a450-7b245bf8462d/api-287034057
     */
    @PostMapping(value = "/v1/create/user")
    FeignResponse<IamCreateSubUserResDTO> createSubUser(@RequestBody IamCreateSubUserDTO createSubUser);

    @PostMapping(value = "/v1/company/exist")
    FeignResponse<ExistsCompanyResDTO> existCompany(@RequestBody ExistsCompanyReqDTO existsCompany);

    @PostMapping(value = "/v1/user/exist")
    FeignResponse<ExistsUserResDTO> existUser(@RequestBody ExistsUserReqDTO existsUser);

    /**
     * Query user points
     * /v1/creditcenter/point
     * https://apifox.com/apidoc/shared/d92a8b65-af9b-4122-a8d4-e34dcca67d33/api-325412045
     *
     * @param userPointsBaseDTO
     * @return
     */
    @PostMapping(value = "/v1/creditcenter/point")
    FeignResponse<IamUserPointsDTO> getCreditCenterPoints(@RequestBody IamUserPointsBaseDTO userPointsBaseDTO);

    /**
     * Transfer points
     * https://apifox.com/apidoc/shared/d92a8b65-af9b-4122-a8d4-e34dcca67d33/api-325412046
     *
     * @param pointsTransferDTO
     * @return
     */
    @PostMapping(value = "/v1/creditcenter/point/transfer")
    FeignResponse<Object> transferCreditCenterPoints(@RequestBody IamPointsTransferDTO pointsTransferDTO);

    /**
     * Top up points
     * https://apifox.com/apidoc/shared/d92a8b65-af9b-4122-a8d4-e34dcca67d33/api-325412047
     *
     * @param pointsTopUpDTO
     * @return
     */
    @PostMapping(value = "/v1/creditcenter/point/top-up")
    FeignResponse<Boolean> topUpCreditCenterPoints(@RequestBody IamPointsTopUpDTO pointsTopUpDTO);

    /**
     * Mixed consume points
     * https://apifox.com/apidoc/shared/d92a8b65-af9b-4122-a8d4-e34dcca67d33/api-325412048
     *
     * @param pointsMixedConsumeDTO
     * @return
     */
    @PostMapping(value = "/v1/creditcenter/point/mixConsume")
    FeignResponse<Object> mixConsumeCreditCenterPoints(@RequestBody IamPointsMixedConsumeDTO pointsMixedConsumeDTO);

    /**
     * Add points
     * https://apifox.com/apidoc/shared/d92a8b65-af9b-4122-a8d4-e34dcca67d33/api-325412054
     *
     * @param pointsAddDTO
     * @return
     */
    @PostMapping(value = "/v1/creditcenter/point/add")
    FeignResponse<Object> addCreditCenterPoints(@RequestBody IamPointsAddDTO pointsAddDTO);

    /**
     * Initialize point account
     * https://apifox.com/apidoc/shared/d92a8b65-af9b-4122-a8d4-e34dcca67d33/api-325412049
     *
     * @param pointsInitAccountDTO
     * @return
     */
    @PostMapping(value = "/v1/creditcenter/point/init")
    FeignResponse<Object> initCreditCenterPoints(@RequestBody IamPointsInitAccountDTO pointsInitAccountDTO);

    /**
     * Freeze points
     * https://apifox.com/apidoc/shared/d92a8b65-af9b-4122-a8d4-e34dcca67d33/api-325412050
     *
     * @param pointsFreezeDTO
     * @return
     */
    @PostMapping(value = "/v1/creditcenter/point/freeze")
    FeignResponse<Object> freezeCreditCenterPoints(@RequestBody IamPointsFreezeDTO pointsFreezeDTO);


    /**
     * Consume points
     * One-time consumption of points
     * https://apifox.com/apidoc/shared/d92a8b65-af9b-4122-a8d4-e34dcca67d33/api-325412051
     *
     * @param pointsDeductDTO
     * @return
     */
    @PostMapping(value = "/v1/creditcenter/point/deduct")
    FeignResponse<Object> deductCreditCenterPoints(@RequestBody IamPointsDeductDTO pointsDeductDTO);

    /**
     * Confirm frozen points
     * https://apifox.com/apidoc/shared/d92a8b65-af9b-4122-a8d4-e34dcca67d33/api-325412052
     *
     * @param pointsConfirmFreezeDTO
     * @return
     */
    @PostMapping(value = "/v1/creditcenter/point/confirmFreeze")
    FeignResponse<Object> confirmFreezeCreditCenterPoints(@RequestBody IamPointsConfirmFreezeDTO pointsConfirmFreezeDTO);

    /**
     * Cancel frozen points
     * https://apifox.com/apidoc/shared/d92a8b65-af9b-4122-a8d4-e34dcca67d33/api-325412053
     *
     * @param pointsCancelFreezeDTO
     * @return
     */
    @PostMapping(value = "/v1/creditcenter/point/cancelFreeze")
    FeignResponse<Object> cancelFreezeCreditCenterPoints(@RequestBody IamPointsCancelFreezeDTO pointsCancelFreezeDTO);


    /**
     * queryPointRulePage
     * https://s.apifox.cn/d92a8b65-af9b-4122-a8d4-e34dcca67d33/api-323367441
     * @param applicationCode
     * @param currencyCode
     * @param status
     * @param page
     * @param size
     * @return
     */
    @GetMapping(value = "/v1/creditcenter/point-exchange-rule/page")
    FeignResponse<PagedResult<CurrencyToPointsRuleDTO>>  pointExchangeRulePage(@RequestParam(value = "applicationCode") String applicationCode,
                                                                          @RequestParam(value = "currencyCode", required = false) String currencyCode,
                                                                          @RequestParam(value = "status", required = false) Integer status,
                                                                          @RequestParam(value = "page", required = false) Integer page,
                                                                          @RequestParam(value = "size", required = false) Integer size);

    /**
     * 获取消费记录列表
     *
     * @param pointsTransactionReqDTO
     * @return
     */
    @PostMapping(value = "/v1/creditcenter/point/transaction/page")
    FeignResponse<PagedResult<IamPointsTransactionResDTO>> pagePointsTransaction(@RequestBody IamPointsTransactionReqDTO pointsTransactionReqDTO);

    /**
     * https://s.apifox.cn/dac00fc5-4e64-4d8d-bd57-2d1f60d442ad
     * @param registerDTO
     * @return
     */
    @PostMapping(value = "/register/v1/register")
    FeignResponse<IamUserRegisterResDTO> iamUserRegister(@RequestBody IamUserRegisterReqDTO registerDTO);


    @GetMapping(value = "/v1/subscription/trial/{companyCode}")
    FeignResponse<SubscriptionTrialDTO> getSubscriptionTrial(@PathVariable(value = "companyCode") String companyCode);

    /**
     * Issue ticket for cross-system session sync
     * POST /tickets/issue
     * 
     * @param request ticket request containing userId and clientId
     * @return ticket response containing the issued ticket
     */
    @PostMapping(value = "/ticket/issue")
    FeignResponse<IamTicketResDTO> issueTicket(@RequestBody IamTicketReqDTO request);

    /**
     * Exchange ticket for access tokens
     * POST /tickets/exchange
     * 
     * @param request ticket exchange request containing the ticket to be exchanged
     * @return token exchange response containing access tokens and related information
     */
    @PostMapping(value = "/ticket/exchange")
    FeignResponse<IamTokenExchangeResDTO> exchangeTicket(@RequestBody IamTicketExchangeReqDTO request);

    /**
     * Create a new user via OpenAPI
     * POST /openapi/v1/users
     * 
     * 对外提供的用户创建接口，为外部系统创建新用户
     * 
     * @param createUserReq 创建用户请求参数
     * @return 创建用户响应结果
     */
    @PostMapping(value = "/openapi/v1/users")
    FeignResponse<IamCreateUserResDTO> createUser(@RequestBody IamCreateUserReqDTO createUserReq);

    /**
     * Get user by email or username
     * GET /openapi/v1/users/query
     * 
     * 通过邮箱或用户名获取用户信息
     * 对外提供的用户查询接口，支持通过邮箱或用户名查询
     * 
     * @param identifier 用户邮箱或用户名
     * @return 用户信息响应结果
     */
    @GetMapping(value = "/openapi/v1/users/query")
    FeignResponse<IamUserDetailDTO> queryUserByIdentifier(@RequestParam("identifier") String identifier);


    @GetMapping(value = "/platform/v1/users/primary")
    FeignResponse<IamUserPrimaryDTO> queryUserPrimary(@RequestParam("companyCode") String companyCode);
    /**
     * Get user by email or username or userId
     * GET /openapi/v1/users/full-info
     *
     * 通过用户ID获取用户完整详细信息
     * 对外提供的用户详情查询接口，返回包含应用、角色、公司等完整信息
     *
     * 接口文档: https://s.apifox.cn/2d98fc39-2f6e-4821-a468-2f8a4f52246e/api-375428661
     *
     * @param identifier 用户identifier
     * @return 用户完整详细信息响应结果
     */
    @GetMapping(value = "/openapi/v1/users/full-info")
    FeignResponse<IamUserDetailResponseDTO> getUserDetailByIdentifier(@RequestParam("identifier") String identifier);

    /**
     * Switch user's tenant
     * PUT /users/{userId}/tenants/{tenantId}/switch
     *
     * Changes the active tenant for a user
     * 切换用户的活动租户
     *
     * 接口文档: https://s.apifox.cn/2d98fc39-2f6e-4821-a468-2f8a4f52246e/api-221431159
     *
     * @param userId 用户ID
     * @param tenantId 租户ID
     * @return 切换结果响应
     */
    @PutMapping(value = "/users/{userId}/tenants/{tenantId}/switch")
    FeignResponse<Void> switchUserTenant(@PathVariable(value = "userId") Long userId,
                                         @PathVariable(value = "tenantId") String tenantId);

    @GetMapping(value = "/platform/v1/users/primary")
    FeignResponse<IamAdminDetailDTO> queryAdminByCompanyCode(@RequestParam("companyCode") String companyCode);

}
