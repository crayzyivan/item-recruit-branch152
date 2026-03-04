package com.item.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.item.convert.IamModeConvert;
import com.item.convert.LeadsCustomerCompanyConvert;
import com.item.dto.IamCreateSubUserReqDTO;
import com.item.dto.IamSignUpDTO;
import com.item.dto.LeadsCustomerCompanyDTO;
import com.item.dto.UserTenantSwitchReqDTO;
import com.item.dto.crm.Convert2CustomerAdapterResDTO;
import com.item.dto.crm.CreateLeadsInfoAdapterReqDTO;
import com.item.dto.iam.ExistsCompanyAdapterReqDTO;
import com.item.dto.iam.ExistsCompanyAdapterResDTO;
import com.item.dto.iam.ExistsCompanyDTO;
import com.item.dto.iam.ExistsUserAdapterReqDTO;
import com.item.dto.iam.ExistsUserAdapterResDTO;
import com.item.dto.iam.ExistsUserDTO;
import com.item.dto.iam.IamCompanyDetailDTO;
import com.item.dto.iam.IamCompanySimpleDTO;
import com.item.dto.iam.IamSignUpAdapterDTO;
import com.item.dto.iam.IamSignUpAdapterResDTO;
import com.item.dto.iam.IamTicketResDTO;
import com.item.dto.iam.IamTokenExchangeResDTO;
import com.item.dto.iam.IamUserContextDTO;
import com.item.dto.iam.IamUserDetailResponseDTO;
import com.item.entity.CandidateEntity;
import com.item.entity.LeadsCustomerCompanyEntity;
import static com.item.framework.constant.CommonConstants.StrConstants.NEW_REFRESH_TOKEN_HEADER;
import static com.item.framework.constant.CommonConstants.StrConstants.NEW_TOKEN_HEADER;
import static com.item.framework.constant.CommonConstants.StrConstants.TENANT_ID_HEADER;
import com.item.framework.constant.CommonResponseCode;
import static com.item.framework.constant.CommonResponseCode.COMMON_CREATE_LEADS_DUPLICATE;
import static com.item.framework.constant.CommonResponseCode.COMMON_DATA_NOT_INVALID;
import static com.item.framework.constant.CommonResponseCode.COMMON_INTERFACE_UPGRADE_MAINTENANCE;
import static com.item.framework.constant.CommonResponseCode.CURRENT_USER_SWITCH_COMPANY;
import com.item.framework.constant.GlobalStatusCode;
import com.item.framework.constant.LeadsCompanyOriginEnum;
import com.item.framework.constant.UserIdentifyTypeEnum;
import com.item.framework.error.BusinessException;
import com.item.iam.oauth2.config.OAuth2Properties;
import com.item.iam.oauth2.model.TokenResponse;
import com.item.service.CandidateService;
import com.item.service.IamAccountDomainService;
import com.item.service.LeadsCustomerCompanyService;
import com.item.service.PointService;
import com.item.service.client.adapter.CrmRpcAdapter;
import com.item.service.client.adapter.IamManagerCompanyRpcAdapter;
import com.item.service.client.adapter.IamRpcAdapter;
import com.item.util.CommonUtils;
import com.item.util.RedisKeyUtil;
import com.item.util.UserContextUtil;
import com.item.vo.IamCreateSubUserVO;
import com.item.vo.IamSignUpVO;
import com.item.vo.LeadsCustomerInfoVO;
import com.item.vo.UserInfoResVO;
import com.item.vo.UserTenantInfoDTO;
import com.item.vo.UserTenantSwitchVO;
import com.item.vo.UserTenantVO;
import com.item.vo.iam.BackendLogoutReqVO;
import com.item.vo.iam.BackendLogoutResVO;
import com.item.vo.iam.ExistsCompanyResVO;
import com.item.vo.iam.ExistsUserResVO;
import com.item.vo.iam.IamTicketExchangeReqVO;
import com.item.vo.iam.IamTicketResVO;
import com.item.vo.iam.IamTokenExchangeResVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * @author : lh
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class IamAccountDomainServiceImpl implements IamAccountDomainService {
    private final IamManagerCompanyRpcAdapter iamManagerCompanyRpcAdapter;
    private final IamModeConvert iamModeConvert;
    private final LeadsCustomerCompanyService leadsCustomerCompanyService;
    private final LeadsCustomerCompanyConvert leadsCustomerCompanyConvert;
    private final CrmRpcAdapter crmRpcAdapter;
    private final IamRpcAdapter iamRpcAdapter;
    private final OAuth2Properties oAuth2Properties;
    private final RedissonClient redissonClient;
    private final PointService pointService;
    private final CandidateService candidateService;

    @Override
    public IamSignUpVO signUp(IamSignUpDTO signUpDTO) {
        checkCompanyUserValid(signUpDTO.getCompanyName(), null, null);
        IamSignUpAdapterDTO iamSignUpAdapterDTO = iamModeConvert.convert2AdapterDTO(signUpDTO);
        //创建账号
        IamSignUpAdapterResDTO iamSignUpAdapterResDTO = iamManagerCompanyRpcAdapter.signUpManagerCompany(iamSignUpAdapterDTO);
        String leadCompanyId = iamSignUpAdapterResDTO.getLeadCompanyId();
        //lead 转 customer
        Convert2CustomerAdapterResDTO customer = crmRpcAdapter.convert2Customer(leadCompanyId);
        //保存映射 或者 更新映射
        LeadsCustomerCompanyDTO leadsCustomerCompany = null;
        RLock lock = redissonClient.getLock(RedisKeyUtil.getCustomerCompanyKey(iamSignUpAdapterResDTO.getCompanyCode()));
        boolean locked = false;
        try {
            locked = lock.tryLock();
            if (locked) {
                leadsCustomerCompany = leadsCustomerCompanyService.getCustomerByCompanyCode(iamSignUpAdapterResDTO.getCompanyCode());
                if (leadsCustomerCompany != null) {
                    LeadsCustomerCompanyDTO leadsCustomerCompanyNew = LeadsCustomerCompanyDTO.fillField(customer, iamSignUpAdapterResDTO);
                    LeadsCustomerCompanyEntity leadsCustomerCompanyEntity = leadsCustomerCompanyConvert.dtoToEntity(leadsCustomerCompanyNew);
                    leadsCustomerCompanyEntity.setId(leadsCustomerCompany.getId());
                    log.warn("sign up user and company already exist leadsCustomerCompany {} new {}", leadsCustomerCompany, leadsCustomerCompanyEntity);
                    leadsCustomerCompanyService.updateById(leadsCustomerCompanyEntity);
                } else {
                    leadsCustomerCompany = LeadsCustomerCompanyDTO.fillField(customer, iamSignUpAdapterResDTO);
                    leadsCustomerCompany.setOrigin(LeadsCompanyOriginEnum.SIGN_UP.getOrigin());
                    leadsCustomerCompanyService.addLeadsCustomerCompany(leadsCustomerCompany);
                }
            }
        } catch (Exception e) {
            log.error("leads convert customer fail {}", leadsCustomerCompany, e);
        } finally {
            if (locked) {
                lock.unlock();
            }
        }

        //初始化账号积分
        initAccountInitPoints(iamSignUpAdapterResDTO);
        IamSignUpVO iamSignUpVO = iamModeConvert.convert2VO(iamSignUpAdapterResDTO);
        iamSignUpVO.setCustomerCode(Optional.ofNullable(customer).map(Convert2CustomerAdapterResDTO::getCustomerCode).orElse(null));
        return iamSignUpVO;
    }

    @Override
    public IamCreateSubUserVO createSubUser(IamCreateSubUserReqDTO createSubUser) {
        throw BusinessException.of(COMMON_INTERFACE_UPGRADE_MAINTENANCE);
//        IamCreateSubUserAdapterDTO iamCreateSubUserAdapterDTO = iamModeConvert.convert2AdapterDTO(createSubUser);
//        IamUserContextDTO currentUserNeedLogin = UserContextUtil.getCurrentUserNeedLogin();
//        iamCreateSubUserAdapterDTO.setCompanyCode(currentUserNeedLogin.getCompanyCode());
////        iamCreateSubUserAdapterDTO.setOrigin(CommonConstants.StrConstants.SYSTEM);
//        IamCreateSubUserAdapterResDTO subUser = iamManagerCompanyRpcAdapter.createSubUser(iamCreateSubUserAdapterDTO);
//        return iamModeConvert.convert2VO(subUser);
    }

    @Override
    public LeadsCustomerInfoVO checkOrCreateLeadsCustomerCompany() {
        IamUserContextDTO currentUserNeedLogin = UserContextUtil.getCurrentUserRecruitNeedLogin();
        LeadsCustomerCompanyDTO leadsCustomerCompany = leadsCustomerCompanyService.getCustomerByCompanyCode(currentUserNeedLogin.getCompanyCode());
        if (leadsCustomerCompany != null && StringUtils.isNotBlank(leadsCustomerCompany.getCrmCustomerCode())) {
            //隐藏关键信息只返回是否通过 可以继续下一步操作
            return LeadsCustomerInfoVO.buildAccess();
        }
        //获取company信息
        IamCompanyDetailDTO companyDetailByCode = iamRpcAdapter.getCompanyDetailByCode(currentUserNeedLogin.getCompanyCode());

        RLock lock = redissonClient.getLock(RedisKeyUtil.getCustomerCompanyKey(currentUserNeedLogin.getCompanyCode()));
        boolean locked = false;
        try {
            locked = lock.tryLock();
            if (locked) {
                leadsCustomerCompany = leadsCustomerCompanyService.getCustomerByCompanyCode(currentUserNeedLogin.getCompanyCode());
                if (leadsCustomerCompany != null && StringUtils.isNotBlank(leadsCustomerCompany.getCrmCustomerCode())) {
                    return LeadsCustomerInfoVO.buildAccess();
                }
                //创建leads
                CreateLeadsInfoAdapterReqDTO createLeadsInfo = CreateLeadsInfoAdapterReqDTO.buildLeadsCompany(companyDetailByCode.getCompanyName());
                Long leadsId = crmRpcAdapter.createLeadsInfo(createLeadsInfo);

                //lead 转 customer
                Convert2CustomerAdapterResDTO customer = crmRpcAdapter.convert2Customer(leadsId.toString());
                //封装映射关系
                LeadsCustomerCompanyDTO leadsCustomerCompanyNew = LeadsCustomerCompanyDTO.fillField(customer, null);
                leadsCustomerCompanyNew.setCentralLeadCompanyId(leadsId);
                leadsCustomerCompanyNew.setCentralManagerId(companyDetailByCode.getManagerId());
                leadsCustomerCompanyNew.setCentralCompanyCode(companyDetailByCode.getCompanyCode());
                leadsCustomerCompanyNew.setCentralCompanyId(companyDetailByCode.getId());
                leadsCustomerCompanyNew.setOrigin(LeadsCompanyOriginEnum.ADD_PAYMENT.getOrigin());
                leadsCustomerCompanyService.addLeadsCustomerCompany(leadsCustomerCompanyNew);
                return LeadsCustomerInfoVO.buildAccess();
            } else {
                throw BusinessException.of(COMMON_CREATE_LEADS_DUPLICATE);
            }
        } catch (BusinessException busex) {
            log.warn("checkOrCreateLeadsCustomerCompany duplicate userId {} companyCode {}", currentUserNeedLogin.getCompanyCode(), currentUserNeedLogin.getId());
            throw busex;
        } catch (Exception e) {
            log.error("checkOrCreateLeadsCustomerCompany fail userId {} companyCode {}", currentUserNeedLogin.getCompanyCode(), currentUserNeedLogin.getId(), e);
        } finally {
            if (locked) {
                lock.unlock();
            }
        }
        return LeadsCustomerInfoVO.buildFail();
    }

    @Override
    public ExistsCompanyResVO existCompany(ExistsCompanyDTO existsCompanyDTO) {
        ExistsCompanyAdapterReqDTO existsCompanyAdapterReqDTO = iamModeConvert.convert2AdapterDTO(existsCompanyDTO);
        ExistsCompanyAdapterResDTO existsCompanyAdapterResDTO = iamManagerCompanyRpcAdapter.existCompany(existsCompanyAdapterReqDTO);
        return iamModeConvert.convert2VO(existsCompanyAdapterResDTO);
    }

    @Override
    public ExistsUserResVO existUser(ExistsUserDTO existsUserDTO) {
        ExistsUserAdapterReqDTO existsUserAdapterReqDTO = iamModeConvert.convert2AdapterDTO(existsUserDTO);
        ExistsUserAdapterResDTO existUser = iamManagerCompanyRpcAdapter.existUser(existsUserAdapterReqDTO);
        return iamModeConvert.convert2VO(existUser);
    }

    @Override
    public UserInfoResVO getCurrentUserInfo() {
        IamUserContextDTO currentUserNeedLogin = UserContextUtil.getCurrentUserNeedLogin();
//        UserIdentifyTypeEnum userIdentifyTypeEnum = (currentUserNeedLogin.getPrimaryUser() != null && currentUserNeedLogin.getPrimaryUser()) ? UserIdentifyTypeEnum.MASTER_RECRUIT : UserIdentifyTypeEnum.CANDIDATE;
        UserIdentifyTypeEnum userIdentifyTypeEnum = UserIdentifyTypeEnum.getByCode(currentUserNeedLogin.getUserIdentifyCode());
        int currentAccountView = CommonUtils.getCurrentAccountView(currentUserNeedLogin, userIdentifyTypeEnum);
        if (StringUtils.isBlank(currentUserNeedLogin.getId())) {
            log.warn("getCurrentUserInfo accessToken {}", currentUserNeedLogin);
            throw BusinessException.of(GlobalStatusCode.TOKEN_INVALID, "token invalid");
        }
        if (userIdentifyTypeEnum == UserIdentifyTypeEnum.MASTER_RECRUIT || userIdentifyTypeEnum == UserIdentifyTypeEnum.SUB_RECRUIT) {
            return UserInfoResVO.builder()
                    .userName(currentUserNeedLogin.getUserName())
                    .userEmail(currentUserNeedLogin.getEmail())
                    .permanentEmail(currentUserNeedLogin.getEmail())
                    .userIdentityType(UserIdentifyTypeEnum.MASTER_RECRUIT.getCode())
                    .userIdentityTypeV1(userIdentifyTypeEnum.getCode())
                    .viewDisplay(currentAccountView)
                    .userId(currentUserNeedLogin.getId())
                    .build();
        }
        RLock lock = redissonClient.getLock(RedisKeyUtil.getLockPreLoginCheckKey(currentUserNeedLogin.getId()));
        boolean locked = false;
        try {
            locked = lock.tryLock();
            if (locked) {
                QueryWrapper<CandidateEntity> queryWrapperCandidateId = new QueryWrapper<>();
                queryWrapperCandidateId.lambda().eq(CandidateEntity::getCandidateId, Long.parseLong(currentUserNeedLogin.getId()));
                CandidateEntity rCandidateId = candidateService.getOne(queryWrapperCandidateId);
                if (Objects.nonNull(rCandidateId)) {
                    String candidatePermanentEmail = rCandidateId.getCandidatePermanentEmail();
                    // 如果邮箱在iam中被修改了 这里也需要更新一下永久邮箱字段值
                    if (!candidatePermanentEmail.equals(currentUserNeedLogin.getEmail())) {
                        CandidateEntity candidate = new CandidateEntity();
                        candidate.setId(rCandidateId.getId());
                        candidate.setCandidatePermanentEmail(currentUserNeedLogin.getEmail());
                        log.info("registerCandidate currentUserNeedLogin {} rCandidateId {} candidate {}", currentUserNeedLogin, rCandidateId, candidate);
                        candidateService.updateById(candidate);
                        candidatePermanentEmail = currentUserNeedLogin.getEmail();
                    }
                    log.warn("registerCandidate rCandidateId {} ", rCandidateId);
                    // 如果已经存储应聘者信息 直接返回
                    return UserInfoResVO
                            .builder()
                            .userName(rCandidateId.getCandidateName())
                            .userEmail(rCandidateId.getCandidateEmail())
                            .userIdentityType(userIdentifyTypeEnum.getCode())
                            .candidateId(rCandidateId.getId())
                            .userIdentityTypeV1(userIdentifyTypeEnum.getCode())
                            .permanentEmail(candidatePermanentEmail)
                            .viewDisplay(currentAccountView)
                            .userId(currentUserNeedLogin.getId())
                            .build();
                }
                // 没有应聘者信息 保存应聘者基本信息
                CandidateEntity candidate = new CandidateEntity();
                candidate.setCandidateName(currentUserNeedLogin.getUserName());
                candidate.setCandidateEmail(currentUserNeedLogin.getEmail());
                candidate.setCandidatePermanentEmail(currentUserNeedLogin.getEmail());
                candidate.setPhoneNumber(currentUserNeedLogin.getContactNumber());
                candidate.setCandidateId(Long.parseLong(currentUserNeedLogin.getId()));
                candidate.setFirstName(currentUserNeedLogin.getFirstName());
                candidate.setLastName(currentUserNeedLogin.getLastName());
                log.info("registerCandidate candidate {} iamUserRegisterResDTO {}", candidate, currentUserNeedLogin);
                candidateService.save(candidate);
                return  UserInfoResVO.builder()
                        .userName(currentUserNeedLogin.getUserName())
                        .userEmail(currentUserNeedLogin.getEmail())
                        .candidateId(candidate.getId())
                        .userIdentityType(userIdentifyTypeEnum.getCode())
                        .userIdentityTypeV1(userIdentifyTypeEnum.getCode())
                        .permanentEmail(currentUserNeedLogin.getEmail())
                        .viewDisplay(currentAccountView)
                        .userId(currentUserNeedLogin.getId())
                        .build();
            } else {
                throw BusinessException.of(CommonResponseCode.COMMON_FREQUENT_OPERATION_PLEASE_TRY_AGAIN_LATER);
            }
        } catch (BusinessException e) {
            log.warn("registerCandidate accessToken BusinessException", e);
            throw e;
        } catch (Exception e) {
            log.warn("registerCandidate accessToken Exception", e);
            throw BusinessException.of(GlobalStatusCode.TOKEN_INVALID, "token invalid");
        } finally {
            if (locked) {
                lock.unlock();
            }
        }
    }

    private void checkCompanyUserValid(String companyName, String userName, String email) {
        // 校验公司是否有效
        ExistsCompanyAdapterReqDTO existsCompanyAdapterReqDTO = new ExistsCompanyAdapterReqDTO();
        existsCompanyAdapterReqDTO.setCompanyName(companyName);
        ExistsCompanyAdapterResDTO existsCompanyAdapterResDTO = iamManagerCompanyRpcAdapter.existCompany(existsCompanyAdapterReqDTO);
        if (existsCompanyAdapterResDTO != null && existsCompanyAdapterResDTO.getExists()) {
            throw BusinessException.of(COMMON_DATA_NOT_INVALID.getCode(), existsCompanyAdapterResDTO.getMessage());
        }
//        ExistsUserAdapterReqDTO existsUserAdapterReqDTO = new ExistsUserAdapterReqDTO();
//        ExistsUserAdapterResDTO existUser = iamManagerCompanyRpcAdapter.existUser(existsUserAdapterReqDTO);

    }

    /**
     * 注册账号的时候 需要初始化该账号的积分
     *
     * @param iamSignUpAdapterResDTO
     */
    private void initAccountInitPoints(IamSignUpAdapterResDTO iamSignUpAdapterResDTO) {
        try {
            if (iamSignUpAdapterResDTO == null) {
                log.warn("initAccountInitPoints iamSignUpAdapterResDTO is null");
                return;
            }
            Long managerId = iamSignUpAdapterResDTO.getManagerId();
            String managerUserEmail = iamSignUpAdapterResDTO.getManagerUserEmail();
            String managerUserName = iamSignUpAdapterResDTO.getManagerUserName();
            if (StringUtils.isAnyBlank(managerUserEmail, managerUserName) || managerId == null) {
                log.warn("initAccountInitPoints iamSignUpAdapterResDTO info is null or blank {}", iamSignUpAdapterResDTO);
                return;
            }
            Boolean initSuccess = pointService.initPoints(managerId, managerUserEmail, managerUserName);
            log.info("init account points {} {}", iamSignUpAdapterResDTO, initSuccess);
        } catch (Exception e) {
            log.error("init account points fail iamSignUpAdapterResDTO {}", iamSignUpAdapterResDTO);
        }
    }

    @Override
    public IamTicketResVO issueTicket() {
        IamUserContextDTO currentUserNeedLogin = UserContextUtil.getCurrentUserNeedLogin();
        String id = currentUserNeedLogin.getId();
        log.info("Domain service issueTicket: {}", id);
        IamTicketResDTO result = iamRpcAdapter.issueTicket(id, oAuth2Properties.getClientId());

        IamTicketResVO response = new IamTicketResVO();
        if (result == null) {
            log.warn("issueTicket result is null for request id {}, client id {}", id, oAuth2Properties.getClientId());
            return response;
        }

        response.setTicket(result.getTicket());
        return response;
    }

    @Override
    public IamTokenExchangeResVO exchangeTicket(IamTicketExchangeReqVO request) {
        log.info("Domain service exchangeTicket: {}", request);

        IamTokenExchangeResDTO result = iamRpcAdapter.exchangeTicket(request.getAuthTicket());

        if (result == null) {
            log.warn("exchangeTicket result is null for request: {}", request);
            return null;
        }

        IamTokenExchangeResVO response = new IamTokenExchangeResVO();
        response.setAccessToken(result.getAccessToken());
        response.setTokenType(result.getTokenType());
        response.setExpiresIn(result.getExpiresIn());
        response.setRefreshToken(result.getRefreshToken());
//        response.setScope(result.getScope());
//        response.setIdToken(result.getIdToken());

        return response;
    }

    @Override
    public BackendLogoutResVO logoutClient(String accessToken, BackendLogoutReqVO request) {

        log.info("Domain service accessToken {} logout: {}", accessToken, request);

        BackendLogoutResVO backendLogoutResVO = new BackendLogoutResVO();

        if (request == null || StringUtils.isBlank(request.getUserId())) {
            log.warn("logout request is invalid: {}", request);
            return backendLogoutResVO;
        }
        backendLogoutResVO.setUserId(request.getUserId());
        return backendLogoutResVO;
    }

    @Override
    public UserTenantVO getUserTenants() {
        IamUserContextDTO currentUserNeedLogin = UserContextUtil.getCurrentUserNeedLogin();
        IamUserDetailResponseDTO userDetailByIdentifier = iamRpcAdapter.getUserDetailByIdentifier(currentUserNeedLogin.getId());
        if (userDetailByIdentifier == null || CollectionUtils.isEmpty(userDetailByIdentifier.getCompanies())) {
            log.warn("getUserTenants not found user company  {} userDetailByIdentifier {}", currentUserNeedLogin, userDetailByIdentifier);
            return null;
        }
        List<IamCompanySimpleDTO> companies = userDetailByIdentifier.getCompanies();
        List<UserTenantInfoDTO> companyList = companies.stream().map(c -> {
            UserTenantInfoDTO userTenantInfoDTO = new UserTenantInfoDTO();
            userTenantInfoDTO.setTenantId(c.getCompanyCode());
            userTenantInfoDTO.setTenantName(c.getCompanyName());
            userTenantInfoDTO.setCurrentTenant(c.getIsCurrentTenant() != null && c.getIsCurrentTenant());
            return userTenantInfoDTO;
        }).toList();
        UserTenantVO vo = new UserTenantVO();
        vo.setTenants(companyList);
        UserTenantInfoDTO userTenantInfoDTO = companyList.stream().filter(UserTenantInfoDTO::isCurrentTenant).findFirst().orElse(null);
        vo.setCurrentTenant(userTenantInfoDTO);
        return vo;
    }

    @Override
    public UserTenantSwitchVO userTenantSwitch(UserTenantSwitchReqDTO reqDTO, HttpServletRequest request, HttpServletResponse response) {
        IamUserContextDTO currentUserNeedLogin = UserContextUtil.getCurrentUserNeedLogin();
        IamUserDetailResponseDTO userDetailByIdentifier = iamRpcAdapter.getUserDetailByIdentifier(currentUserNeedLogin.getId());
        if (userDetailByIdentifier == null || CollectionUtils.isEmpty(userDetailByIdentifier.getCompanies())) {
            log.warn("userTenantSwitch not found user company not execute switch {} userDetailByIdentifier {}", currentUserNeedLogin, userDetailByIdentifier);
            return null;
        }
        // 校验是否允许切换
        List<IamCompanySimpleDTO> companies = userDetailByIdentifier.getCompanies();
        boolean matchCompanies = companies.stream().anyMatch(c -> c.getCompanyCode().equals(reqDTO.getSwitchTargetTenantId()));
        if (!matchCompanies) {
            log.warn("userTenantSwitch not allowed to {} companies {}", reqDTO, companies);
            throw BusinessException.of(CURRENT_USER_SWITCH_COMPANY);
        }
        iamRpcAdapter.switchUserTenant(Long.parseLong(currentUserNeedLogin.getId()), reqDTO.getSwitchTargetTenantId());
        UserTenantSwitchVO userTenantSwitchVO = new UserTenantSwitchVO();
        String refreshJwt = CommonUtils.parseRefreshJwt(request);
        if (StringUtils.isNotBlank(refreshJwt)) {
            TokenResponse tokenResponse = iamRpcAdapter.refreshToken(refreshJwt);
            userTenantSwitchVO.setRefreshToken(tokenResponse.getRefreshToken());
            userTenantSwitchVO.setAccessToken(tokenResponse.getAccessToken());
            response.setHeader(NEW_TOKEN_HEADER, tokenResponse.getAccessToken());
            response.setHeader(NEW_REFRESH_TOKEN_HEADER, tokenResponse.getRefreshToken());
        }
        userTenantSwitchVO.setSwitchTargetTenantId(reqDTO.getSwitchTargetTenantId());
        response.setHeader(TENANT_ID_HEADER, reqDTO.getSwitchTargetTenantId());
        return userTenantSwitchVO;
    }

}
