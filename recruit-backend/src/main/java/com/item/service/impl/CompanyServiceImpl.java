package com.item.service.impl;

import com.item.dto.CompanyInfoSimpleDTO;
import com.item.dto.iam.IamCompanyDetailDTO;
import static com.item.framework.constant.CommonConstants.REDIS_CACHE_COMPANY_INFO;
import com.item.service.CompanyService;
import com.item.service.client.adapter.IamRpcAdapter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * @author : lh
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CompanyServiceImpl implements CompanyService {

    private final IamRpcAdapter iamRpcAdapter;

    @Override
    @Cacheable(cacheManager = "companyInfoCacheManager", value = REDIS_CACHE_COMPANY_INFO, key = "#companyCode")
    public CompanyInfoSimpleDTO getCompanyInfoByCode(String companyCode) {
        if (companyCode == null) {
            return new CompanyInfoSimpleDTO();
        }
        IamCompanyDetailDTO companyDetailByCode = iamRpcAdapter.getCompanyDetailByCode(companyCode);
        CompanyInfoSimpleDTO companyInfo = new CompanyInfoSimpleDTO();
        companyInfo.setName(companyDetailByCode.getCompanyName());
        companyInfo.setWebsite(companyDetailByCode.getWebsite());
        companyInfo.setLogo(companyDetailByCode.getLogopath());
        companyInfo.setEmail(companyDetailByCode.getEmail());
        return companyInfo;
    }
}
