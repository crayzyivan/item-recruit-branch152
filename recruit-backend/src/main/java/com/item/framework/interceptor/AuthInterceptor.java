package com.item.framework.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.item.dto.AuthCheckResultDTO;
import com.item.dto.iam.IamUserContextDTO;
import com.item.framework.annotation.Auth;
import com.item.framework.constant.AuthResponseCode;
import com.item.framework.constant.RoleType;
import com.item.framework.constant.UserIdentifyTypeEnum;
import com.item.util.UserContextUtil;
import com.item.vo.Result;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.lang.reflect.Method;
import java.text.MessageFormat;

/**
 * <p>
 *
 * </p>
 *
 * @author liuyabin on 2025/7/11
 * @since 1.0.0
 */
@Slf4j
@Component
public class AuthInterceptor implements HandlerInterceptor {
    private static final String OPTION_REQUEST = "OPTIONS";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 跳过 OPTIONS 请求的处理
        if (OPTION_REQUEST.equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }
        // 拦截逻辑
        Method method = ((HandlerMethod) handler).getMethod();
        Auth methodAuthAnnotation = method.getAnnotation(Auth.class);
        Auth typeAuthAnnotation = handler.getClass().getAnnotation(Auth.class);
        // 先取方法上的auth注解，如果没有再取类上的auth注解，都没有的情况下才放行
        if (methodAuthAnnotation == null && typeAuthAnnotation == null) {
            return true;
        }
        Auth workAuth = (methodAuthAnnotation == null) ? typeAuthAnnotation : methodAuthAnnotation;
//        boolean authSuccess;

        AuthCheckResultDTO authSuccess = verifyIdentifyPermission(workAuth.roleType());


        if (!authSuccess.authSuccess()) {
            // 需要返回权限不足
            response.setContentType("application/json;charset=UTF-8");
            Result<?> result = Result.fail(authSuccess.code(),authSuccess.msg());
            ObjectMapper objectMapper = new ObjectMapper();
            response.getWriter().write(objectMapper.writeValueAsString(result));

            return false;
        }

        return true;
    }

    private boolean verifyPermission(RoleType needRoleType) {
        //注解不需要任何权限
        if (needRoleType ==  RoleType.NONE) {
            return true;
        }
        //当前没有登录人信息
        IamUserContextDTO currentUserNeedLogin = UserContextUtil.getCurrentUser();
        if (currentUserNeedLogin == null) {
            return false;
        }

        //获取不到角色区分信息
        UserIdentifyTypeEnum userIdentify = UserIdentifyTypeEnum.getByCode(currentUserNeedLogin.getUserIdentifyCode());
        if (userIdentify == null) {
            return false;
        }

        //当前是应聘者 并且 注解也是应聘者 通过
//        if (!primaryUser && needRoleType == RoleType.CANDIDATE) {
//            return true;
//        }
        if (userIdentify == UserIdentifyTypeEnum.CANDIDATE && needRoleType == RoleType.CANDIDATE) {
            return true;
        }

        //当前登录人是主账号 并且 注解是主账号或者子账号 通过  主账号有权访问子账号和本身的权限
//        if (primaryUser && (needRoleType == RoleType.MASTER_USER || needRoleType == RoleType.SUB_USER)) {
//            return true;
//        }
        if (userIdentify == UserIdentifyTypeEnum.MASTER_RECRUIT && (needRoleType == RoleType.MASTER_USER || needRoleType == RoleType.SUB_USER)) {
            return true;
        }

        //当前登录人是子招聘者 并且 注解是子账号 通过
        if (userIdentify == UserIdentifyTypeEnum.SUB_RECRUIT && needRoleType == RoleType.SUB_USER) {
            return true;
        }

        //其他情况返回false
        return false;
    }

    private AuthCheckResultDTO verifyIdentifyPermission(RoleType needRoleType) {
        //注解不需要任何权限
        if (needRoleType ==  RoleType.NONE) {
            return new AuthCheckResultDTO(true, null, null);
        }
        //当前没有登录人信息
        IamUserContextDTO currentUserNeedLogin = UserContextUtil.getCurrentUserNeedLogin();

        //获取不到角色区分信息
        UserIdentifyTypeEnum userIdentify = UserIdentifyTypeEnum.getByCode(currentUserNeedLogin.getUserIdentifyCode());

        //当前是应聘者 并且 注解也是应聘者 通过
        if (userIdentify == UserIdentifyTypeEnum.CANDIDATE && needRoleType == RoleType.CANDIDATE) {
            return new AuthCheckResultDTO(true, null, null);
        }

        //当前登录人是主账号 并且 注解是主账号或者子账号 通过  主账号有权访问子账号和本身的权限
        if (userIdentify == UserIdentifyTypeEnum.MASTER_RECRUIT && (needRoleType == RoleType.MASTER_USER || needRoleType == RoleType.SUB_USER)) {
            return new AuthCheckResultDTO(true, null, null);
        }

        //当前登录人是子招聘者 并且 注解是子账号 通过
        if (userIdentify == UserIdentifyTypeEnum.SUB_RECRUIT && needRoleType == RoleType.SUB_USER) {
            return new AuthCheckResultDTO(true, null, null);
        }

        // 当前登录人的身份 和 注解需要的身份 不匹配  不通过
        if (!userIdentify.getIdentify().equals(needRoleType.getIdentity())) {
            AuthResponseCode authCurrentUserIdentifyNotMatch = AuthResponseCode.AUTH_CURRENT_USER_IDENTIFY_NOT_MATCH;
            String msg = MessageFormat.format(authCurrentUserIdentifyNotMatch.getMsg(), needRoleType.getIdentity());
            return new AuthCheckResultDTO(false, authCurrentUserIdentifyNotMatch.getCode(), msg);
        }

        //需要的身份是招聘者 并且 当前角色子账号 需要角色是主账号 不匹配 不通过 ；   子账号想访问主账号 不通过
        if (needRoleType.getIdentity().equals(RoleType.MASTER_USER.getIdentity()) && !userIdentify.getRole().equals(needRoleType.getRole())) {
            AuthResponseCode authCurrentUserRoleNotMatch = AuthResponseCode.AUTH_CURRENT_USER_ROLE_NOT_MATCH;
            String msg = MessageFormat.format(authCurrentUserRoleNotMatch.getMsg(), needRoleType.getRole());
            return new AuthCheckResultDTO(false, authCurrentUserRoleNotMatch.getCode(), msg);
        }

        //其他情况返回false
        return new AuthCheckResultDTO(false, AuthResponseCode.AUTH_CURRENT_USER_NOT_PASS.getCode(), AuthResponseCode.AUTH_CURRENT_USER_NOT_PASS.getMsg());
    }
}
