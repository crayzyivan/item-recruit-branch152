package com.item.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.item.convert.AyrshareCompanyConfigConvert;
import com.item.dto.ayrshare.AyrShareUserResDTO;
import com.item.dto.ayrshare.AyrshareTokenDTO;
import com.item.entity.AyrshareCompanyConfigEntity;
import static com.item.framework.constant.CommonConstants.NumConstants.LIMIT;
import com.item.framework.constant.CommonResponseCode;
import com.item.framework.error.BusinessException;
import com.item.mapper.AyrshareCompanyConfigMapper;
import com.item.service.AyrShareService;
import com.item.service.AyrshareCompanyConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * Ayrshare公司配置服务实现
 *
 * @author lh
 * @since 2025-08-28
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AyrshareCompanyConfigServiceImpl extends ServiceImpl<AyrshareCompanyConfigMapper, AyrshareCompanyConfigEntity> implements AyrshareCompanyConfigService {

    private final AyrshareCompanyConfigConvert ayrshareCompanyConfigConvert;
    private final AyrShareService ayrShareService;

    @Override
    public AyrshareTokenDTO getTokenInfoByCompanyCode(String companyCode) {
        log.info("Getting Ayrshare token info for company: {}", companyCode);
        AyrshareCompanyConfigEntity entity = findByCompanyCode(companyCode);
        if (entity == null) {
            log.info("No Ayrshare config found for company: {}", companyCode);
            return null;
        }
        log.info("Found Ayrshare config for company: {}, config: {}", companyCode, entity);
        return ayrshareCompanyConfigConvert.entityToDTO(entity);
    }

    /**
     * 根据公司代码查询配置
     */
    @Override
    public AyrshareCompanyConfigEntity findByCompanyCode(String companyCode) {
        LambdaQueryWrapper<AyrshareCompanyConfigEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AyrshareCompanyConfigEntity::getCompanyCode, companyCode)
                .orderByDesc(AyrshareCompanyConfigEntity::getId).last("LIMIT 1");
        List<AyrshareCompanyConfigEntity> list = this.list(queryWrapper);
        if (CollectionUtils.isEmpty(list)) {
            return null;
        }
        return list.getFirst();
    }

    @Override
    public void saveAyrShareConfig(AyrshareCompanyConfigEntity ayrshareCompanyConfigEntity) {
        if (ayrshareCompanyConfigEntity == null) {
            return;
        }
        save(ayrshareCompanyConfigEntity);
    }

    @Override
    public void updateAyrShareConfig(AyrshareCompanyConfigEntity ayrshareCompanyConfigEntity) {
        this.updateById(ayrshareCompanyConfigEntity);
    }

    @Override
    public Boolean validateApiKey(String apiKey) {
        if (StringUtils.isBlank(apiKey)) {
            log.warn("apiKey validation failed: api key is blank");
            return false;
        }

        // 基本格式验证：Token长度应该大于等于4个字符
        if (apiKey.length() < LIMIT) {
            log.warn("apiKey validation failed:  api key length is less than 4 characters");
            return false;
        }

        if (Strings.CS.contains(apiKey, "*")) {
            log.warn("apiKey validation failed:  api key in * ");
            return false;
        }
        log.debug("apiKey validation passed");
        return true;
    }

    @Override
    public void checkAllowHotList(String companyCode) {
        AyrshareTokenDTO tokenInfo = getTokenInfoByCompanyCode(companyCode);
        log.info("checkAllowHotList tokenInfo: {} {}", tokenInfo, companyCode);
        if (tokenInfo == null || StringUtils.isBlank(tokenInfo.getAyrshareApiKey())) {
            throw BusinessException.of(CommonResponseCode.COMMON_AYRSHARE_APIKEY_NOT_FOUND);
        }
        AyrShareUserResDTO userProfileDetails = ayrShareService.getUserProfileDetails(tokenInfo.getAyrshareApiKey(), tokenInfo.getAyrshareProfileKey());
        if(Objects.nonNull(userProfileDetails) && CollectionUtils.isNotEmpty(userProfileDetails.getActiveSocialAccounts())){
            return;
        }
        throw BusinessException.of(CommonResponseCode.COMMON_AYRSHARE_NOT_LINKED_ANY_PLATFORM);
    }
}
