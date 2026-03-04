package com.item.client;

import com.item.service.IamAccountDomainService;
import com.item.vo.iam.BackendLogoutReqVO;
import com.item.vo.iam.BackendLogoutResVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Client logout controller for cross-system session sync
 * Handles logout related client interfaces
 *
 * @author : lh
 */
@RestController
@RequestMapping("/client/")
@RequiredArgsConstructor
public class ClientLogoutController {
    private final IamAccountDomainService iamAccountDomainService;

    /**
     * Backend logout for cross-system session sync
     * 后端登出接口，用于跨系统会话同步
     *
     * @param request logout request containing userId
     * @return logout response containing the logout ticket
     */
    @PostMapping("logout")
    public BackendLogoutResVO backendLogout(@RequestHeader("Authorization") String token, @RequestBody BackendLogoutReqVO request) {
        return iamAccountDomainService.logoutClient(token, request);
    }
}
