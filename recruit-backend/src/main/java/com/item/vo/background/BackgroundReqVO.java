package com.item.vo.background;

import java.io.Serializable;

import lombok.Data;

/**
 * <p>
 * 背调请求
 * </p>
 *
 * @author liuyabin on 2025/8/1
 * @since 1.0.0
 */
@Data
public class BackgroundReqVO implements Serializable {
    private Long candidateId;
    private Long jobId;
}