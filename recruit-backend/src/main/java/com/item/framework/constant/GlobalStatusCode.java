package com.item.framework.constant;

import java.io.Serializable;

public class GlobalStatusCode  implements Serializable {
    // 通用
    public static final int SUCCESS = 0; // 成功
    public static final int FAIL = 1; // 失败
    public static final int SYSTEM_ERROR = 1000; // 系统异常

    //认证相关
    public static final int USER_NOT_FOUND = 1001; // 用户不存在
    public static final int PASSWORD_ERROR = 1002; // 密码错误
    public static final int ACCOUNT_LOCKED = 1003; // 账号被锁定
    public static final int TOKEN_EXPIRED = 1004; // token过期
    public static final int TOKEN_INVALID = 1005; // token无效
    public static final int NOT_LOGGED_IN = 1006; // 未登录
    public static final int NO_PERMISSION = 1007; // 无权限


    // 参数相关
    public static final int PARAM_ERROR = 2001; // 参数错误
    public static final int PARAM_MISSING = 2002; // 缺少参数
    public static final int PARAM_TYPE_ERROR = 2003; // 参数类型错误
    public static final int EMAIL_ADDRESS_USE = 2004;

    //其他异常
    public static final int EMAIL_SEND_ERROR = 3001; //邮件发送失败
    public static final int UNSUPPORTED_FILE_TYPE = 3002; //不支持的文件格式
    public static final int FILE_CONVERSION_FAILED = 3003;//文件转换失败
    public static final int NOT_FIND_FILE = 3004;//文件未找到
    public static final int FILE_UPLOAD_FAILED = 3005;//文件上传失败
    public static final int FILE_DOWNLOAD_FAILED = 3006; //文件下载失败
    public static final int FILE_DELETE_FAILED = 3007; //文件删除失败
    public static final int URL_GENERATION_FAILED = 3008;//生成url失败
    public static final int CHECK_FILE_FAILED = 3009;//检查文件是否存在失败
    public static final int SAVE_ES_FAILED = 3010;//存储es失败
    public static final int SEARCH_RESUME_BASE_ON_CANDIDATE = 3011;//基于候选人获取简历失败
    public static final int SENDING_EMAIL = 3012;
    public static final int AI_PHONE_CALL_FAILED = 3015;//预约电话面试失败
    //基于id搜索失败
    public static final int SEARCH_RESUME_BASE_ON_ID = 3012;
    public static final int SEARCH_RESUME_CANDIDATE = 3013; //简历信息查询失败


    public static final String MESSAGE_INTERNAL_SERVER_ERROR = "Internal server error.";

}
