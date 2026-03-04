package com.item.service;

import com.item.dto.CompanyInfoDTO;
import com.item.vo.CompanyInfoVO;

/**
 * @author : lh
 */
public interface CompanyDomainService {

    CompanyInfoDTO getCompanyInfo();

    CompanyInfoVO getCompanyInfoByCode(String companyCode);
}
