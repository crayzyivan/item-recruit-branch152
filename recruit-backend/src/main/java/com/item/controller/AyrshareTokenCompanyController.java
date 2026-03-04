package com.item.controller;

import com.item.dto.ayrshare.AyrshareTokenSaveDTO;
import com.item.framework.annotation.Auth;
import com.item.framework.constant.RoleType;
import com.item.service.AyrshareTokenCompanyConfigDomainService;
import com.item.vo.ayrshare.AyrshareTokenVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Ayrshare Token管理控制器
 *
 * @author lh
 * @since 2025-08-28
 */
@Slf4j
@RestController
@RequestMapping("ayrshare/config")
@RequiredArgsConstructor
public class AyrshareTokenCompanyController {

    private final AyrshareTokenCompanyConfigDomainService ayrshareTokenCompanyConfigDomainService;

    /**
     * 保存或更新Ayrshare Token配置
     *
     * @param saveDTO Token保存DTO
     * @return Token视图VO（掩码格式）
     */
    @Auth(roleType = RoleType.SUB_USER)
    @PostMapping
    public Boolean saveOrUpdateToken(@Validated @RequestBody AyrshareTokenSaveDTO saveDTO) {
        return ayrshareTokenCompanyConfigDomainService.saveToken(saveDTO);
    }

    /**
     * 获取Ayrshare Token配置信息
     *
     * @return Token视图VO（掩码格式）
     */
    @Auth(roleType = RoleType.SUB_USER)
    @GetMapping
    public AyrshareTokenVO getTokenInfo() {
        return ayrshareTokenCompanyConfigDomainService.getTokenInfo();
    }
}
