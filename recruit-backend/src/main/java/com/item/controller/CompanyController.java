package com.item.controller;

import com.item.dto.CompanyInfoDTO;
import com.item.service.CompanyDomainService;
import com.item.vo.CompanyInfoVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author : lh
 * 公司相关接口
 */
@RestController
@RequestMapping("/company")
@RequiredArgsConstructor
public class CompanyController {
    private final CompanyDomainService companyDomainService;

    /**
     * 获取当前登录人公司信息
     * @return
     */
    @Deprecated
    @GetMapping(value = "/info")
    public CompanyInfoDTO getInfo() {
        return companyDomainService.getCompanyInfo();
    }

    /**
     * 非登录态 根据companyCode获取和company信息
     * @return
     */
    @GetMapping(value = "/name/{companyCode}")
    public CompanyInfoVO getCompanyName(@PathVariable String companyCode) {
        return companyDomainService.getCompanyInfoByCode(companyCode);
    }

}
