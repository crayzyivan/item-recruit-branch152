package com.item.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * XML Feed更新日志实体类
 *
 * @author liyunlong
 * @since 2025-08-26
 */
@Data
@TableName(value = "r_xml_feed_update_log")
public class XmlFeedUpdateLogEntity implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * Feed配置ID
     */
    @TableField(value = "feed_config_id")
    private Long feedConfigId;

    /**
     * 更新类型：1-手动更新，2-自动更新
     */
    @TableField(value = "update_type")
    private Integer updateType;

    /**
     * 更新状态：1-成功，2-失败
     */
    @TableField(value = "update_status")
    private Integer updateStatus;

    /**
     * 更新的职位数量
     */
    @TableField(value = "job_count")
    private Integer jobCount;

    /**
     * 错误信息
     */
    @TableField(value = "error_message")
    private String errorMessage;

    /**
     * 操作人ID（可选）
     */
    @TableField(value = "operator_id")
    private Long operatorId;

    /**
     * 开始时间
     */
    @TableField(value = "start_time")
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    @TableField(value = "end_time")
    private LocalDateTime endTime;
}
