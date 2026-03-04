package com.item.controller;

import com.item.framework.annotation.Auth;
import com.item.framework.constant.RoleType;
import com.item.service.AyrShareTokenDomainService;
import com.item.vo.ayrshare.AyrShareJwtResponseVO;
import com.item.vo.ayrshare.AyrShareUserPlatformsVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * AyrShare控制器
 *
 * @author hua.liu
 * @since 2025-08-27
 */
@Slf4j
@RestController
@RequestMapping("/ayrshare")
@RequiredArgsConstructor
public class AyrShareTokenController {

    private final AyrShareTokenDomainService ayrShareTokenDomainService;

    /**
     * 生成JWT Token以及链接
     *
     * @return JWT响应信息
     */
    @Auth(roleType = RoleType.SUB_USER)
    @GetMapping("/generate-jwt")
    public AyrShareJwtResponseVO generateJwt() {
        return ayrShareTokenDomainService.generateJwt();
    }

    /**
     * 获取平台详情信息
     *
     * @return 平台信息详情
     */
    @Auth(roleType = RoleType.SUB_USER)
    @GetMapping("/profile-platforms")
    public AyrShareUserPlatformsVO getProfileDetails() {
        
        return ayrShareTokenDomainService.getProfileDetails();
    }

    /**
     * 是否允许使用hotlist功能  在ayrshare没有linked任何平台时 不允许 返回false
     *
     * @return true 允许分享 false不允许分享
     */
    @Auth(roleType = RoleType.SUB_USER)
    @GetMapping("/allow-hot-list")
    public Boolean allowHotList() {
        return ayrShareTokenDomainService.allowHotListSaveConfig();
    }
}
