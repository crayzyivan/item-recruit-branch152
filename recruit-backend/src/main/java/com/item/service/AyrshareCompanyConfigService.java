package com.item.service;

import com.item.dto.ayrshare.AyrshareTokenDTO;
import com.item.entity.AyrshareCompanyConfigEntity;

/**
 * Ayrshare公司配置服务接口
 *
 * @author lh
 * @since 2025-08-28
 */
public interface AyrshareCompanyConfigService {
    /**
     * 根据公司代码获取Token信息
     * @param companyCode 公司代码
     * @return Token业务DTO
     */
    AyrshareTokenDTO getTokenInfoByCompanyCode(String companyCode);

    AyrshareCompanyConfigEntity findByCompanyCode(String companyCode);

    void saveAyrShareConfig(AyrshareCompanyConfigEntity ayrshareCompanyConfigEntity);

    void updateAyrShareConfig(AyrshareCompanyConfigEntity ayrshareCompanyConfigEntity);


    Boolean validateApiKey(String apiKey);

    void checkAllowHotList(String companyCode);
}
