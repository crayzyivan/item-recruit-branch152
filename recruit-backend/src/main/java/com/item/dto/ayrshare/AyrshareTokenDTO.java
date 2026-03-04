package com.item.dto.ayrshare;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * Ayrshare Token业务DTO
 *
 * @author lh
 * @since 2025-08-28
 */
@Data
public class AyrshareTokenDTO {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 公司代码
     */
    private String companyCode;

    /**
     * Ayrshare API Key
     */
    private String ayrshareApiKey;

    /**
     * Ayrshare Profile Key
     */
    private String ayrshareProfileKey;

    /**
     * Ayrshare Subreddit
     */
    private String ayrshareSubreddit;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 创建人
     */
    private String createByName;

    private Long createById;

    /**
     * 更新人
     */
    private String updateByName;

    private Long updateById;

    private String  ayrshareDomain;
}
