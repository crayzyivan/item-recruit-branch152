package com.item.dto;

import java.io.Serializable;

import lombok.Data;

/**
 * <p>
 * location全称
 * </p>
 *
 * @author liuyabin on 2025/7/25
 * @since 1.0.0
 */
@Data
public class FullLocationDTO implements Serializable {
    private Long cityId;
    private Long stateId;
    private Long countryId;

    private String cityName;
    private String stateName;
    private String countryName;
}
