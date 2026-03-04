package com.item.dto.ayrshare;

import lombok.Data;

import java.util.List;

/**
 * Ayrshare用户信息响应DTO
 *
 * @author lh
 */
@Data
public class AyrShareUserResDTO {

    /**
     * 活跃的社交账户列表
     */
    private List<String> activeSocialAccounts;

    /**
     * 创建时间信息
     */
    private AyrShareCreatedInfoDTO created;

    /**
     * 显示名称列表
     */
    private List<AyrShareDisplayNameInfoDTO> displayNames;

    /**
     * 用户邮箱
     */
    private String email;

    /**
     * 最后API调用时间
     */
    private String lastApiCall;

    /**
     * 本月API调用次数
     */
    private Integer monthlyApiCalls;

    /**
     * 本月发布数量
     */
    private Integer monthlyPostCount;

    /**
     * 引用ID
     */
    private String refId;

    /**
     * 标题
     */
    private String title;

    /**
     * 最后更新时间
     */
    private String lastUpdated;

    /**
     * 下次更新时间
     */
    private String nextUpdate;

    /**
     * 操作
     */
    private String action;

    /**
     * 状态
     */
    private String status;

    /**
     * 状态码
     */
    private Integer code;

    /**
     * 消息
     */
    private String message;

    /**
     * 详细信息
     */
    private String details;
}
