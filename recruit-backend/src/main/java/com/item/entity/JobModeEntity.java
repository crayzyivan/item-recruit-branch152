package com.item.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * <p>
 * Job Mode
 * </p>
 *
 * @author liuyabin on 2025/7/8
 * @since 1.0.0
 */
@TableName(value = "r_job_mode")
@Data
public class JobModeEntity implements Serializable {
    @TableId
    private int id;
    @TableField(value = "mode_name")
    private String modeName;
    private String chineseName;
    private String spanishName;
    private String japaneseName;
}
