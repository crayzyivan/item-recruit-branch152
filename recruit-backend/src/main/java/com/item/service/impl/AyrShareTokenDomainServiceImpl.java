package com.item.service.impl;

import com.item.convert.AyrShareConverter;
import com.item.convert.AyrshareCompanyConfigConvert;
import com.item.dto.ayrshare.AyrShareCreateUserProfileReqDTO;
import com.item.dto.ayrshare.AyrShareCreateUserProfileResDTO;
import com.item.dto.ayrshare.AyrShareGenerateJwtDTO;
import com.item.dto.ayrshare.AyrShareJwtResponseDTO;
import com.item.dto.ayrshare.AyrShareUserResDTO;
import com.item.dto.ayrshare.AyrshareTokenDTO;
import com.item.dto.iam.IamCompanyDetailDTO;
import com.item.dto.iam.IamUserContextDTO;
import com.item.entity.AyrshareCompanyConfigEntity;
import com.item.framework.config.AyrShareConfig;
import static com.item.framework.constant.CommonConstants.StrConstants.ERROR;
import static com.item.framework.constant.CommonConstants.StrConstants.SUCCESS;
import com.item.framework.constant.CommonResponseCode;
import static com.item.framework.constant.CommonResponseCode.COMMON_OPERATION_FAILED;
import com.item.framework.error.BusinessException;
import com.item.service.AyrShareService;
import com.item.service.AyrShareTokenDomainService;
import com.item.service.AyrshareCompanyConfigService;
import com.item.service.client.adapter.IamRpcAdapter;
import com.item.util.RedisKeyUtil;
import com.item.util.UserContextUtil;
import com.item.vo.ayrshare.AyrShareJwtResponseVO;
import com.item.vo.ayrshare.AyrShareUserPlatformsVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * AyrShare Token领域服务实现类
 *
 * @author hua.liu
 * @since 2025-08-27
 */
@Slf4j
@Service
@RefreshScope
@RequiredArgsConstructor
public class AyrShareTokenDomainServiceImpl implements AyrShareTokenDomainService {

    private final AyrShareConfig ayrShareConfig;
    private final AyrShareConverter ayrShareConverter;
    private final AyrShareService ayrShareService;
    private final IamRpcAdapter iamRpcAdapter;
    private final RedissonClient redissonClient;
    private final AyrshareCompanyConfigService ayrshareCompanyConfigService;

    private final AyrshareCompanyConfigConvert ayrshareCompanyConfigConvert;

    @Override
    public AyrShareJwtResponseVO generateJwt() {
        IamUserContextDTO currentUserRecruit = UserContextUtil.getCurrentUserRecruitNeedLogin();
        AyrshareTokenDTO tokenInfo = ayrshareCompanyConfigService.getTokenInfoByCompanyCode(currentUserRecruit.getCompanyCode());
        log.info("generateJwt tokenInfo: {}", tokenInfo);
        if (tokenInfo == null || StringUtils.isBlank(tokenInfo.getAyrshareApiKey())) {
            String companyCode = currentUserRecruit.getCompanyCode();
            // 业务验证 暂时不需要 已经存在的配置
//            Boolean validateApiKey = ayrshareCompanyConfigService.validateApiKey(ayrShareConfig.getApiKey());
//            if (!validateApiKey) {
//                throw BusinessException.of(CommonResponseCode.COMMON_AYRSHARE_APIKEY_VALIDATE_FAIL);
//            }
//            checkApiKey(ayrShareConfig.getApiKey(), null);
            IamCompanyDetailDTO companyDetailByCode = iamRpcAdapter.getCompanyDetailByCode(companyCode);

            // 使用分布式锁确保并发安全
            String lockKey = RedisKeyUtil.getLockAyrshareConfigKey(companyCode);
            RLock lock = redissonClient.getLock(lockKey);
            Long userId = Long.parseLong(currentUserRecruit.getId());
            boolean locked = false;
            try {
                locked = lock.tryLock();
                if (locked) {
                    // 查询现有配置 二次查询是否存在
                    AyrshareCompanyConfigEntity existingEntity = ayrshareCompanyConfigService.findByCompanyCode(companyCode);
                    log.info("allowHotListSaveConfig existingEntity: {}", existingEntity);
                    //不存在 新增后转换
                    if (existingEntity == null) {
                        AyrshareCompanyConfigEntity entity = new AyrshareCompanyConfigEntity();
                        // 插入新记录
                        log.info("Creating new Ayrshare config for company: {}", companyCode);

                        entity.setCompanyCode(companyCode);
                        entity.setAyrshareApiKey(ayrShareConfig.getApiKey());
                        // 需要调用ayrsahre创建
                        String profileKey = createProfileKey(ayrShareConfig.getApiKey(), companyDetailByCode);
                        entity.setAyrshareProfileKey(profileKey);
                        entity.setAyrshareSubreddit(ayrShareConfig.getSubreddit());
                        entity.setAyrshareDomain(ayrShareConfig.getDomain());
                        entity.setCreateById(userId);
                        entity.setUpdateById(userId);
                        entity.setCreateByName(currentUserRecruit.getUserName());
                        entity.setUpdateByName(currentUserRecruit.getUserName());
                        ayrshareCompanyConfigService.saveAyrShareConfig(entity);
                        tokenInfo = ayrshareCompanyConfigConvert.entityToDTO(entity);
                    } else {
                        //存在直接转换
                        tokenInfo = ayrshareCompanyConfigConvert.entityToDTO(existingEntity);
                    }
                } else {
                    log.warn("Failed to acquire lock for company: {}", companyCode);
                    throw BusinessException.of(CommonResponseCode.COMMON_FREQUENT_OPERATION_PLEASE_TRY_AGAIN_LATER);
                }
            } catch (Exception e) {
                log.error("Failed to acquire lock for company: {}", companyCode, e);
                throw BusinessException.of(COMMON_OPERATION_FAILED);
            } finally {
                if (locked) {
                    lock.unlock();
                }
            }
        }
        log.info("generateJwt tokenInfo real: {}", tokenInfo);
        AyrShareGenerateJwtDTO ayrShareGenerateJwtDTO = new AyrShareGenerateJwtDTO();
        ayrShareGenerateJwtDTO.setDomain(tokenInfo.getAyrshareDomain());
        ayrShareGenerateJwtDTO.setPrivateKey(ayrShareConfig.getPrivateKey());
        ayrShareGenerateJwtDTO.setProfileKey(tokenInfo.getAyrshareProfileKey());

        AyrShareJwtResponseDTO ayrShareJwtResponseDTO = ayrShareService.generateJWT(ayrShareGenerateJwtDTO);
        log.info("generateJwt tokenInfo res {}", ayrShareJwtResponseDTO);
        return ayrShareConverter.toJwtVO(ayrShareJwtResponseDTO);
    }

    @Override
    public AyrShareUserPlatformsVO getProfileDetails() {
        AyrShareUserResDTO userProfileDetails = ayrShareService.getUserProfileDetails();
        AyrShareUserPlatformsVO ayrShareUserPlatformsVO = new AyrShareUserPlatformsVO();
        ayrShareUserPlatformsVO.setPlatforms(userProfileDetails.getActiveSocialAccounts());
        return ayrShareUserPlatformsVO;
    }

    @Override
    public Boolean allowHotList() {
        IamUserContextDTO currentUserRecruit = UserContextUtil.getCurrentUserRecruitNeedLogin();
        AyrshareTokenDTO tokenInfo = ayrshareCompanyConfigService.getTokenInfoByCompanyCode(currentUserRecruit.getCompanyCode());
        log.info("allowHotList tokenInfo: {} {}", tokenInfo, currentUserRecruit.getCompanyCode());
        if (tokenInfo == null || StringUtils.isBlank(tokenInfo.getAyrshareApiKey())) {
            throw BusinessException.of(CommonResponseCode.COMMON_AYRSHARE_APIKEY_NOT_FOUND);
        }
        AyrShareUserResDTO userProfileDetails = ayrShareService.getUserProfileDetails(tokenInfo.getAyrshareApiKey(), tokenInfo.getAyrshareProfileKey());
        return Objects.nonNull(userProfileDetails) && CollectionUtils.isNotEmpty(userProfileDetails.getActiveSocialAccounts());
    }

    @Override
    public Boolean allowHotListSaveConfig() {
        IamUserContextDTO currentUserRecruit = UserContextUtil.getCurrentUserRecruitNeedLogin();
        AyrshareTokenDTO tokenInfo = ayrshareCompanyConfigService.getTokenInfoByCompanyCode(currentUserRecruit.getCompanyCode());
        log.info("allowHotListSaveConfig tokenInfo: {} {}", tokenInfo, currentUserRecruit.getCompanyCode());
        if (tokenInfo == null || StringUtils.isBlank(tokenInfo.getAyrshareApiKey())) {
            return false;
        }
        //使用当前companyCode的数据 判断是否已经配置了相关平台
        AyrShareUserResDTO userProfileDetails = ayrShareService.getUserProfileDetails(tokenInfo.getAyrshareApiKey(), tokenInfo.getAyrshareProfileKey());
        return Objects.nonNull(userProfileDetails) && CollectionUtils.isNotEmpty(userProfileDetails.getActiveSocialAccounts());
    }

    private void checkApiKey(String apiKey, String profileKey) {
        AyrShareUserResDTO userProfileDetails = ayrShareService.getUserProfileDetails(apiKey, profileKey);
        if (StringUtils.isBlank(userProfileDetails.getStatus()) || SUCCESS.equalsIgnoreCase(userProfileDetails.getStatus())) {
            return;
        }
        log.warn("check api key {}", userProfileDetails);
        throw BusinessException.of(CommonResponseCode.COMMON_AYRSHARE_APIKEY_VALIDATE_FAIL);
    }


    private String createProfileKey(String apiKey, IamCompanyDetailDTO companyDetailByCode) {
        AyrShareCreateUserProfileReqDTO ayrShareCreateUserProfileReqDTO = new AyrShareCreateUserProfileReqDTO();
        ayrShareCreateUserProfileReqDTO.setTitle(companyDetailByCode.getCompanyName());
        AyrShareCreateUserProfileResDTO userProfile = ayrShareService.createUserProfile(apiKey, ayrShareCreateUserProfileReqDTO);
        if (userProfile.getStatus() != null && ERROR.equalsIgnoreCase(userProfile.getStatus())) {
            log.error("createUserProfile fail {}", userProfile);
            throw BusinessException.of(COMMON_OPERATION_FAILED);
        }
        return userProfile.getProfileKey();
    }

}
