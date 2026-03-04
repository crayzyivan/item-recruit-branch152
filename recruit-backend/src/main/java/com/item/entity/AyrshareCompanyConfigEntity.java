package com.item.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Ayrshare公司配置实体类
 *
 * @author lh
 * @since 2025-08-28
 */
@Data
@TableName(value = "r_ayrshare_company_config")
public class AyrshareCompanyConfigEntity implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 公司代码
     */
    @TableField(value = "company_code")
    private String companyCode;

    /**
     * Ayrshare API Key
     */
    @TableField(value = "ayrshare_api_key")
    private String ayrshareApiKey;

    /**
     * Ayrshare Profile Key
     */
    @TableField(value = "ayrshare_profile_key")
    private String ayrshareProfileKey;

    /**
     * Ayrshare Subreddit
     */
    @TableField(value = "ayrshare_subreddit")
    private String ayrshareSubreddit;

    /**
     * 逻辑删除标识：0-未删除，1-已删除
     */
    @TableLogic
    @TableField(value = "deleted")
    private Boolean deleted;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
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
