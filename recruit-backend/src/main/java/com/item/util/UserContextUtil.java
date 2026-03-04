package com.item.util;

import com.alibaba.ttl.TransmittableThreadLocal;
import com.item.dto.iam.IamUserContextDTO;
import com.item.framework.constant.AuthResponseCode;
import com.item.framework.constant.GlobalStatusCode;
import com.item.framework.constant.UserIdentifyTypeEnum;
import com.item.framework.error.BusinessException;

import java.text.MessageFormat;
import java.util.Locale;

/**
 * @author : lh
 */
public class UserContextUtil {
    private static final TransmittableThreadLocal<IamUserContextDTO> USER_CONTEXT = new TransmittableThreadLocal<>();
    private static final TransmittableThreadLocal<Locale> LANGUAGE_LOCAL = new TransmittableThreadLocal<>();

    public static void setCurrentUser(IamUserContextDTO userInfo) {
        USER_CONTEXT.set(userInfo);
    }

    /**
     * 这个方法招聘者不登陆的时候或者token有异常时 调用不会抛出异常 需要调用方自己处理
     * @return
     */
    public static IamUserContextDTO getCurrentUser() {
        return USER_CONTEXT.get();
    }

    public static void clear() {
        // 关键：必须手动清除！
        USER_CONTEXT.remove();
    }

    /**
     * 这个方法会校验招聘者是否登录或者token是否异常 如果招聘者没有登录或者token异常 会抛出异常 code=1005 msg=Invalid authentication异常
     * 如果当前登陆人是应聘者 会抛出权限异常 1007
     * @return
     */
    public static IamUserContextDTO getCurrentUserRecruitNeedLogin() {
        IamUserContextDTO currentUser = getCurrentUserNeedLogin();
        UserIdentifyTypeEnum userIdentify = UserIdentifyTypeEnum.getByCode(currentUser.getUserIdentifyCode());
        if (userIdentify == UserIdentifyTypeEnum.CANDIDATE) {
            AuthResponseCode authCurrentUserIdentifyNotMatch = AuthResponseCode.AUTH_CURRENT_USER_IDENTIFY_NOT_MATCH;
            String msg = MessageFormat.format(authCurrentUserIdentifyNotMatch.getMsg(), UserIdentifyTypeEnum.MASTER_RECRUIT.getIdentify());
            throw BusinessException.of(AuthResponseCode.AUTH_CURRENT_USER_IDENTIFY_NOT_MATCH.getCode(), msg);
        }
        return currentUser;
    }

    /**
     * 这个方法会校验应聘者是否登录或者token是否异常 如果应聘者没有登录或者token异常 会抛出异常 code=1005 msg=Invalid authentication异常
     * 如果当前登陆人是招聘者 会抛出权限异常 1007
     * @return
     */
    public static IamUserContextDTO getCurrentUserCandidateNeedLogin() {
        IamUserContextDTO currentUser = getCurrentUserNeedLogin();
        UserIdentifyTypeEnum userIdentify = UserIdentifyTypeEnum.getByCode(currentUser.getUserIdentifyCode());
        if (userIdentify == UserIdentifyTypeEnum.MASTER_RECRUIT || userIdentify == UserIdentifyTypeEnum.SUB_RECRUIT) {
            AuthResponseCode authCurrentUserIdentifyNotMatch = AuthResponseCode.AUTH_CURRENT_USER_IDENTIFY_NOT_MATCH;
            String msg = MessageFormat.format(authCurrentUserIdentifyNotMatch.getMsg(), UserIdentifyTypeEnum.CANDIDATE.getRole());
            throw BusinessException.of(authCurrentUserIdentifyNotMatch.getCode(), msg);
        }
        return currentUser;
    }


    /**
     * 这个方法会校验是否登录或者token是否异常 如果没有登录或者token异常 会抛出异常 code=1005 msg=Invalid authentication异常
     * @return
     */
    public static IamUserContextDTO getCurrentUserNeedLogin() {
        IamUserContextDTO currentUser = getCurrentUser();
        if (currentUser == null) {
            throw BusinessException.of(GlobalStatusCode.TOKEN_INVALID,"Invalid authentication");
        }
        return currentUser;
    }

    /**
     * 招聘者未登录 或者 token有问题 会抛出异常
     * @return
     */
    public static String getCurrentUserCompanyCode() {
        IamUserContextDTO currentUser = getCurrentUserRecruitNeedLogin();
        return currentUser.getCompanyCode();
    }

    public static void setLanguageLocal(Locale locale) {
        LANGUAGE_LOCAL.set(locale);
    }

    public static Locale getLanguageLocal() {
        return LANGUAGE_LOCAL.get();
    }

    public static void cleanLocal() {
        // 关键：必须手动清除！
        LANGUAGE_LOCAL.remove();
    }
}