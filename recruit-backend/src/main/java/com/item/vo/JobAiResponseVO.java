package com.item.vo;

import java.io.Serializable;
import java.util.List;

import lombok.Data;

/**
 * <p>
 *
 * </p>
 *
 * @author liuyabin on 2025/7/18
 * @since 1.0.0
 */
@Data
public class JobAiResponseVO implements Serializable {
    private String overview;
    private List<String> responsibilities;
    private List<String> responsibilities2;
}
