package com.item.service.impl;

import com.item.dto.CompanyInfoDTO;
import com.item.dto.CompanyInfoSimpleDTO;
import com.item.dto.iam.IamUserContextDTO;
import com.item.service.CompanyDomainService;
import com.item.service.CompanyService;
import com.item.util.UserContextUtil;
import com.item.vo.CompanyInfoVO;
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
public class CompanyDomainServiceImpl implements CompanyDomainService {

    private final CompanyService companyService;

    @Override
    public CompanyInfoDTO getCompanyInfo() {
        CompanyInfoDTO info = new CompanyInfoDTO();
        IamUserContextDTO currentUserNeedLogin = UserContextUtil.getCurrentUserRecruitNeedLogin();
        CompanyInfoSimpleDTO simpleInfo = companyService.getCompanyInfoByCode(currentUserNeedLogin.getCompanyCode());
        info.setLogoPath(simpleInfo.getLogo());
        return info;
    }

    @Override
    public CompanyInfoVO getCompanyInfoByCode(String companyCode) {
        if (StringUtils.isBlank(companyCode)) {
            return new CompanyInfoVO();
        }
        CompanyInfoSimpleDTO simpleInfo = companyService.getCompanyInfoByCode(companyCode);
        if (simpleInfo == null) {
            return new CompanyInfoVO();
        }
        CompanyInfoVO companyInfoVO = new CompanyInfoVO();
        companyInfoVO.setName(simpleInfo.getName());
        companyInfoVO.setWebsite(simpleInfo.getWebsite());
        companyInfoVO.setLogo(simpleInfo.getLogo());
        return companyInfoVO;
    }

}
