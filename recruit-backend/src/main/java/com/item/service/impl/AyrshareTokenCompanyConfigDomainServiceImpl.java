package com.item.service.impl;

import com.item.convert.AyrshareCompanyConfigConvert;
import com.item.dto.ayrshare.AyrShareCreateUserProfileReqDTO;
import com.item.dto.ayrshare.AyrShareCreateUserProfileResDTO;
import com.item.dto.ayrshare.AyrShareUserResDTO;
import com.item.dto.ayrshare.AyrshareTokenSaveDTO;
import com.item.dto.iam.IamCompanyDetailDTO;
import static com.item.framework.constant.CommonConstants.StrConstants.ERROR;
import static com.item.framework.constant.CommonConstants.StrConstants.SUCCESS;
import com.item.framework.constant.CommonResponseCode;
import static com.item.framework.constant.CommonResponseCode.COMMON_OPERATION_FAILED;
import com.item.framework.error.BusinessException;
import com.item.service.AyrShareService;
import com.item.service.AyrshareCompanyConfigService;
import com.item.service.AyrshareTokenCompanyConfigDomainService;
import com.item.service.client.adapter.IamRpcAdapter;
import com.item.vo.ayrshare.AyrshareTokenVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RedissonClient;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;

/**
 * Ayrshare Token领域服务实现
 *
 * @author lh
 * @since 2025-08-28
 */
@Slf4j
@Service
@RefreshScope
@RequiredArgsConstructor
public class AyrshareTokenCompanyConfigDomainServiceImpl implements AyrshareTokenCompanyConfigDomainService {

    private final RedissonClient redissonClient;
    private final AyrshareCompanyConfigService ayrshareCompanyConfigService;
    private final IamRpcAdapter iamRpcAdapter;
    private final AyrShareService ayrShareService;
    private final AyrshareCompanyConfigConvert ayrshareCompanyConfigConvert;

    @Override
    public Boolean saveToken(AyrshareTokenSaveDTO saveDTO) {
        log.info("Saving Ayrshare token configuration {}", saveDTO);
//        IamUserContextDTO currentUser = UserContextUtil.getCurrentUserRecruitNeedLogin();
//        String companyCode = currentUser.getCompanyCode();
//
//        // 业务验证
//        Boolean validateApiKey = ayrshareCompanyConfigService.validateApiKey(saveDTO.getAyrshareApiKey());
//        if (!validateApiKey) {
//            throw BusinessException.of(CommonResponseCode.COMMON_AYRSHARE_APIKEY_VALIDATE_FAIL);
//        }
//        checkApiKey(saveDTO.getAyrshareApiKey(), null);
//        IamCompanyDetailDTO companyDetailByCode = iamRpcAdapter.getCompanyDetailByCode(companyCode);
//
//        // 使用分布式锁确保并发安全
//        String lockKey = RedisKeyUtil.getLockAyrshareConfigKey(companyCode);
//        RLock lock = redissonClient.getLock(lockKey);
//        Long userId = Long.parseLong(currentUser.getId());
//
//        try {
//            if (lock.tryLock()) {
//                // 查询现有配置
//                AyrshareCompanyConfigEntity existingEntity = ayrshareCompanyConfigService.findByCompanyCode(companyCode);
//
//                AyrshareCompanyConfigEntity entity = new AyrshareCompanyConfigEntity();
//                if (existingEntity != null) {
//                    // 检查API Key是否不同，如果不同则更新
//                    if (!saveDTO.getAyrshareApiKey().equals(existingEntity.getAyrshareApiKey())) {
//                        log.info("Updating existing Ayrshare config for company: {}, config: {}",
//                                companyCode, existingEntity);
//                        //创建
//                        String profileKey = createProfileKey(saveDTO, companyDetailByCode);
//                        entity.setAyrshareProfileKey(profileKey);
//                        entity.setAyrshareApiKey(saveDTO.getAyrshareApiKey());
//                        entity.setId(existingEntity.getId());
//                        entity.setUpdateByName(currentUser.getUserName());
//                        entity.setUpdateById(userId);
//                        ayrshareCompanyConfigService.updateAyrShareConfig(entity);
//                        return true;
//                    } else {
//                        log.info("Ayrshare API Key unchanged for existingEntity: {}, {} skipping update", existingEntity, saveDTO);
//                        return true;
//                    }
//                } else {
//                    // 插入新记录
//                    log.info("Creating new Ayrshare config for company: {}", companyCode);
//
//                    entity.setCompanyCode(companyCode);
//                    entity.setAyrshareApiKey(saveDTO.getAyrshareApiKey());
//                    // 需要调用ayrsahre创建
//                    String profileKey = createProfileKey(saveDTO, companyDetailByCode);
//                    entity.setAyrshareProfileKey(profileKey);
//                    entity.setCreateById(userId);
//                    entity.setUpdateById(userId);
//                    entity.setCreateByName(currentUser.getUserName());
//                    entity.setUpdateByName(currentUser.getUserName());
//                    ayrshareCompanyConfigService.saveAyrShareConfig(entity);
//                    return true;
//                }
//            } else {
//                log.warn("Failed to acquire lock for company: {}", companyCode);
//                throw BusinessException.of(CommonResponseCode.COMMON_FREQUENT_OPERATION_PLEASE_TRY_AGAIN_LATER);
//            }
//        } catch (Exception e) {
//            log.error("Failed to acquire lock for company: {}", companyCode, e);
//            throw BusinessException.of(COMMON_OPERATION_FAILED);
//        } finally {
//            lock.unlock();
//        }
        return true;
    }

    @Override
    public AyrshareTokenVO getTokenInfo() {
//        IamUserContextDTO currentUserRecruitNeedLogin = UserContextUtil.getCurrentUserRecruitNeedLogin();
//        // 调用Service查询
//        AyrshareTokenDTO tokenDTO = ayrshareCompanyConfigService.getTokenInfoByCompanyCode(currentUserRecruitNeedLogin.getCompanyCode());
//        AyrshareTokenVO ayrshareTokenVO = new AyrshareTokenVO();
//        ayrshareTokenVO.setHotListStatus(AyrshareHotListStatus.AYRSHARE_APIKEY_NOT_FOUND.getCode());
//        if (tokenDTO == null) {
//            log.info("No Ayrshare token configuration found {}", currentUserRecruitNeedLogin.getCompanyCode());
//            return ayrshareTokenVO;
//        }
//        // 转换为VO并应用掩码逻辑
//        AyrshareTokenVO tokenVO = ayrshareCompanyConfigConvert.dtoToVO(tokenDTO);
//        tokenVO.setHotListStatus(AyrshareHotListStatus.AYRSHARE_APIKEY_BUT_NOT_LINKED.getCode());
//        AyrShareUserResDTO userProfileDetails = ayrShareService.getUserProfileDetails(tokenDTO.getAyrshareApiKey(), tokenDTO.getAyrshareProfileKey());
//        if (Objects.nonNull(userProfileDetails) && CollectionUtils.isNotEmpty(userProfileDetails.getActiveSocialAccounts())) {
//            tokenVO.setHotListStatus(AyrshareHotListStatus.AYRSHARE_ENABLE_HOTLIST.getCode());
//        }
//        log.info("Successfully retrieved Ayrshare token configuration for company: {} {} ", tokenDTO.getCompanyCode(), tokenVO);
//        return tokenVO;
        return new AyrshareTokenVO();
    }

    private void checkApiKey(String apiKey, String profileKey) {
        AyrShareUserResDTO userProfileDetails = ayrShareService.getUserProfileDetails(apiKey, profileKey);
        if (StringUtils.isBlank(userProfileDetails.getStatus()) || SUCCESS.equalsIgnoreCase(userProfileDetails.getStatus())) {
            return;
        }
        log.warn("check api key {}", userProfileDetails);
        throw BusinessException.of(CommonResponseCode.COMMON_AYRSHARE_APIKEY_VALIDATE_FAIL);
    }


    private String createProfileKey(AyrshareTokenSaveDTO saveDTO, IamCompanyDetailDTO companyDetailByCode) {
        AyrShareCreateUserProfileReqDTO ayrShareCreateUserProfileReqDTO = new AyrShareCreateUserProfileReqDTO();
        ayrShareCreateUserProfileReqDTO.setTitle(companyDetailByCode.getCompanyName());
        AyrShareCreateUserProfileResDTO userProfile = ayrShareService.createUserProfile(saveDTO.getAyrshareApiKey(), ayrShareCreateUserProfileReqDTO);
        if (userProfile.getStatus() != null && ERROR.equalsIgnoreCase(userProfile.getStatus())) {
            log.error("createUserProfile fail {}", userProfile);
            throw BusinessException.of(COMMON_OPERATION_FAILED);
        }
        return userProfile.getProfileKey();
    }
}
