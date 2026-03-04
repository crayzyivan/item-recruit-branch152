package com.item.vo.ayrshare;

import lombok.Data;

/**
 * Ayrshare Token视图VO
 *
 * @author lh
 * @since 2025-08-28
 */
@Data
public class AyrshareTokenVO {

    /**
     * 主键ID
     */
//    private Long id;

    /**
     * 公司代码
     */
    private String companyCode;

    /**
     * Ayrshare API Key (掩码显示)
     */
    private String ayrshareApiKey;

    /**
     * 0 未配置paiKey 1存在apikey但是不能其中hotlist 由于没有linked到任何平台 2 可以使用hotlist功能
     */
    private Integer hotListStatus;

//    /**
//     * Ayrshare Profile Key
//     */
//    private String ayrshareProfileKey;
//
//    /**
//     * Ayrshare Subreddit
//     */
//    private String ayrshareSubreddit;
//
//    /**
//     * 是否启用
//     */
//    private Boolean isEnabled;
//
//    /**
//     * 创建时间
//     */
//    private LocalDateTime createTime;
//
//    /**
//     * 更新时间
//     */
//    private LocalDateTime updateTime;
//
//    /**
//     * 创建人
//     */
//    private String createBy;
//
//    /**
//     * 更新人
//     */
//    private String updateBy;
}
