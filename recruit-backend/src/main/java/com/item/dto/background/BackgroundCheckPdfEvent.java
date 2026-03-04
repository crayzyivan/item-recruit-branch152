package com.item.dto.background;

import java.io.Serializable;

import lombok.Data;

/**
 * <p>
 *
 * </p>
 *
 * @author liuyabin on 2025/8/3
 * @since 1.0.0
 */
@Data
public class BackgroundCheckPdfEvent extends BackgroundResultBase implements Serializable {
    private BackgroundPdfOrderDetailsDTO order;
    private String event;
}
