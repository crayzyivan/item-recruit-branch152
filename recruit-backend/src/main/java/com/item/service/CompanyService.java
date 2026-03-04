package com.item.service;

import com.item.dto.CompanyInfoSimpleDTO;

/**
 * @author : lh
 */
public interface CompanyService {
    CompanyInfoSimpleDTO getCompanyInfoByCode(String companyCode);
}
