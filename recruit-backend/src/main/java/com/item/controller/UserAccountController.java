package com.item.controller;

import com.item.dto.UserTenantSwitchReqDTO;
import com.item.service.IamAccountDomainService;
import com.item.vo.UserInfoResVO;
import com.item.vo.UserTenantSwitchVO;
import com.item.vo.UserTenantVO;
import com.item.vo.iam.IamTicketExchangeReqVO;
import com.item.vo.iam.IamTicketResVO;
import com.item.vo.iam.IamTokenExchangeResVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

/**
 * @author : lh
 */
@RestController
@RequestMapping("/user/account/")
@RequiredArgsConstructor
public class UserAccountController {
    private final IamAccountDomainService iamAccountDomainService;

    /**
     * 获取当前登录信息
     * <p>
     * 其中有建立candidate与iamuser关联关系的处理
     *
     * @return
     */
    @GetMapping(value = "current/user-info")
    public UserInfoResVO getCurrentUserInfo() {
        return iamAccountDomainService.getCurrentUserInfo();
    }

    @GetMapping(value = "login/general-auth-ticket")
    public String getGeneralAuthTicketIssue() {
        IamTicketResVO iamTicketResVO = iamAccountDomainService.issueTicket();
        return Optional.ofNullable(iamTicketResVO).map(IamTicketResVO::getTicket).orElse(null);
    }

    @PostMapping(value = "login/oauth-code-token/by-ticket")
    public IamTokenExchangeResVO getOauthCodeTokenIssue(@RequestBody @Validated IamTicketExchangeReqVO iamTicketExchangeReqVO) {
        return iamAccountDomainService.exchangeTicket(iamTicketExchangeReqVO);
    }

    /**
     * 获取租户列表
     *
     * @return
     */
    @GetMapping(value = "tenants")
    public UserTenantVO getUserTenants() {
        return iamAccountDomainService.getUserTenants();
    }

    /**
     * 切换租户
     * 响应头返回内容
     * X-New-Access-Token
     * X-New-Refresh-Token
     * X-Tenant-Id
     *
     * @param reqDTO
     * @param response
     * @return
     */
    @PutMapping(value = "tenant/switch")
    public UserTenantSwitchVO userTenantSwitch(@RequestBody @Validated UserTenantSwitchReqDTO reqDTO, HttpServletRequest request, HttpServletResponse response) {
        return iamAccountDomainService.userTenantSwitch(reqDTO, request, response);
    }
}
