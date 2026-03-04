package com.item.service;

import com.item.dto.IamCreateSubUserReqDTO;
import com.item.dto.IamSignUpDTO;
import com.item.dto.UserTenantSwitchReqDTO;
import com.item.dto.iam.ExistsCompanyDTO;
import com.item.dto.iam.ExistsUserDTO;
import com.item.vo.IamCreateSubUserVO;
import com.item.vo.IamSignUpVO;
import com.item.vo.LeadsCustomerInfoVO;
import com.item.vo.UserInfoResVO;
import com.item.vo.UserTenantSwitchVO;
import com.item.vo.UserTenantVO;
import com.item.vo.iam.BackendLogoutReqVO;
import com.item.vo.iam.BackendLogoutResVO;
import com.item.vo.iam.ExistsCompanyResVO;
import com.item.vo.iam.ExistsUserResVO;
import com.item.vo.iam.IamTicketExchangeReqVO;
import com.item.vo.iam.IamTicketResVO;
import com.item.vo.iam.IamTokenExchangeResVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * @author : lh
 */
public interface IamAccountDomainService {
    IamSignUpVO signUp(IamSignUpDTO signUpDTO);

    IamCreateSubUserVO createSubUser(IamCreateSubUserReqDTO createSubUser);

    LeadsCustomerInfoVO checkOrCreateLeadsCustomerCompany();

    ExistsCompanyResVO existCompany(ExistsCompanyDTO existsCompanyDTO);

    ExistsUserResVO existUser(ExistsUserDTO existsUserDTO);

    UserInfoResVO getCurrentUserInfo();

    /**
     * 签发票据用于跨系统会话同步
     *
     * @return 票据响应
     */
    IamTicketResVO issueTicket();

    /**
     * 交换票据获取访问令牌
     *
     * @param request 票据交换请求
     * @return 令牌交换响应
     */
    IamTokenExchangeResVO exchangeTicket(IamTicketExchangeReqVO request);

    /**
     * 用户登出  IAM会回调这个方法 达到登出状态同步的目的 recruit在当前版本没有维护这个状态直接 响应成功
     *
     * @param accessToken token
     * @param request 登出请求
     * @return 登出响应
     */
    BackendLogoutResVO logoutClient(String accessToken, BackendLogoutReqVO request);

    /**
     * 获取当前登录人的租户列表和上次选择的租户信息
     *
     * @return
     */
    UserTenantVO getUserTenants();

    /**
     * 切换当前登录人的租户
     *
     * @param reqDTO
     * @param response
     * @return
     */
    UserTenantSwitchVO userTenantSwitch(UserTenantSwitchReqDTO reqDTO, HttpServletRequest request, HttpServletResponse response);

}
