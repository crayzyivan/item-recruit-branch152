package com.item.controller;

import com.item.dto.iam.ExistsCompanyDTO;
import com.item.dto.iam.ExistsUserDTO;
import com.item.framework.annotation.Auth;
import com.item.framework.constant.RoleType;
import com.item.service.CrmDomainService;
import com.item.service.IamAccountDomainService;
import com.item.service.PointService;
import com.item.vo.UserInfoResVO;
import com.item.vo.iam.ExistsCompanyResVO;
import com.item.vo.iam.ExistsUserResVO;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 招聘者注册-信用卡操作 已废弃 在未来版本删除
 * @author : lh
 */
@Deprecated
@RestController
@RequestMapping("/recruit/account/")
@RequiredArgsConstructor
public class IamAccountCompanyController {
    private final IamAccountDomainService iamAccountDomainService;
    private final CrmDomainService crmDomainService;
    private final PointService pointService;

    /**
     * 注册账号和公司
     *
     * @param signUpDTO
     * @return
     */
//    @PostMapping(value = "sign-up")
//    public IamSignUpVO signUp(@RequestBody @Validated IamSignUpDTO signUpDTO) {
//        return iamAccountDomainService.signUp(signUpDTO);
//    }

    /**
     * 创建子账号
     *
     * @param createSubUser
     * @return
     */
//    @Auth(roleType = RoleType.MASTER_USER)
//    @PostMapping(value = "sub-user")
//    public IamCreateSubUserVO createSubUserVO(@RequestBody @Validated IamCreateSubUserReqDTO createSubUser) {
//        return iamAccountDomainService.createSubUser(createSubUser);
//    }

    /**
     * 账号初始化积分
     *
     * @param initPoints
     * @return
     */
//    @Auth(roleType = RoleType.MASTER_USER)
//    @PostMapping(value = "init/points")
//    public Boolean initPoints(@RequestBody @Validated InitPointsDTO initPoints) {
//        return pointService.initPoints(initPoints.getUserId(), initPoints.getUserEmail(), initPoints.getUserName());
//    }

//    /**
//     * 绑卡前调用该接口进行数据检查或创建 必须调用 checkAccess属性为true 才可以继续操作
//     *
//     * @return
//     */
//    @Auth(roleType = RoleType.MASTER_USER)
//    @PostMapping(value = "leads/info")
//    public LeadsCustomerInfoVO checkOrCreateLeadsCustomer(){
//        return iamAccountDomainService.checkOrCreateLeadsCustomerCompany();
//    }
//
//    /**
//     * 添加卡片
//     *
//     * @param cardPayment
//     * @return
//     */
//    @Auth(roleType = RoleType.MASTER_USER)
//    @PostMapping(value = "card/payment")
//    public Boolean addCrmCardPayment(@RequestBody @Validated CrmAddCardPaymentDTO cardPayment) {
//        return crmDomainService.addCardPaymentInformation(cardPayment);
//    }
//
//    /**
//     * 获取卡片信息
//     *
//     * @param queryPaymentReqDTO
//     * @return
//     */
//    @Auth(roleType = RoleType.MASTER_USER)
//    @PostMapping(value = "card/payment-list")
//    public PaymentInformationResVO getPaymentInformationList(@RequestBody @Validated QueryPaymentReqDTO queryPaymentReqDTO) {
//        return crmDomainService.getPaymentInformationList(queryPaymentReqDTO);
//    }
//
//    /**
//     * 删除卡片信息
//     *
//     * @param deletePaymentDTO
//     * @return
//     */
//    @Auth(roleType = RoleType.MASTER_USER)
//    @DeleteMapping(value = "card/payment")
//    public Boolean updatePayment(@RequestBody @Validated DeletePaymentDTO deletePaymentDTO) {
//        return crmDomainService.deletePayment(deletePaymentDTO);
//    }
//
//    /**
//     * 更新卡片信息
//     *
//     * @param updatePaymentDTO
//     * @return
//     */
//    @Auth(roleType = RoleType.MASTER_USER)
//    @PutMapping(value = "card/payment")
//    public Boolean updatePayment(@RequestBody @Validated UpdatePaymentDTO updatePaymentDTO) {
//        return crmDomainService.updatePayment(updatePaymentDTO);
//    }

    /**
     * 校验公司名称是否存在
     *
     * @param existsCompanyDTO
     * @return
     */
    @Auth(roleType = RoleType.SUB_USER)
    @PostMapping(value = "exists/company")
    public ExistsCompanyResVO existsCompany(@RequestBody @Validated ExistsCompanyDTO existsCompanyDTO) {
        return iamAccountDomainService.existCompany(existsCompanyDTO);
    }

    /**
     * 校验用户名信息是否存在 在创建子账号的时候
     *
     * @param existsUserDTO
     * @return
     */
    @Auth(roleType = RoleType.SUB_USER)
    @PostMapping(value = "exists/user")
    public ExistsUserResVO existsCompany(@RequestBody @Validated ExistsUserDTO existsUserDTO) {
        return iamAccountDomainService.existUser(existsUserDTO);
    }

    /**
     * 获取当前登录信息 已经废弃 在未来版本删除  使用com.item.controller.UserAccountController#getCurrentUserInfo() 代替
     *
     * 其中有建立candidate与iamuser关联关系的处理
     * @return
     */
    @Deprecated
    @GetMapping(value = "current/user-info")
    public UserInfoResVO getCurrentUserInfo(){
        return iamAccountDomainService.getCurrentUserInfo();
    }

}
