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
 * XML Feed配置实体类
 *
 * @author liyunlong
 * @since 2025-08-26
 */
@Data
@TableName(value = "r_xml_feed_config")
public class XmlFeedConfigEntity implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 公司代码
     */
    @TableField(value = "company_code")
    private String companyCode;

    /**
     * 平台类型：1-LinkedIn, 2-Indeed
     */
    @TableField(value = "platform_type")
    private Integer platformType;

    /**
     * XML Feed URL地址
     */
    @TableField(value = "feed_url")
    private String feedUrl;

    /**
     * Indeed专用账户邮箱
     */
    @TableField(value = "account_email")
    private String accountEmail;

    /**
     * 平台指导文档URL
     */
    @TableField(value = "guide_url")
    private String guideUrl;

    /**
     * 自动更新间隔（小时），范围1-24
     */
    @TableField(value = "update_interval_hours")
    private Integer updateIntervalHours;

    /**
     * 最后更新时间
     */
    @TableField(value = "last_update_time")
    private LocalDateTime lastUpdateTime;

    /**
     * 下次更新时间
     */
    @TableField(value = "next_update_time")
    private LocalDateTime nextUpdateTime;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 修改时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 创建人ID
     */
    @TableField(value = "create_by")
    private Long createBy;

    /**
     * 修改人ID
     */
    @TableField(value = "update_by")
    private Long updateBy;

    /**
     * 是否删除：false-否，true-是
     */
    @TableField("deleted")
    @TableLogic
    private Boolean deleted;
}
