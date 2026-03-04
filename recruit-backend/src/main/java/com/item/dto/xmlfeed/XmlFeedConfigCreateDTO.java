package com.item.dto.xmlfeed;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * XML Feed configuration create DTO
 *
 * @author liyunlong
 * @since 2025-08-26
 */
@Data
public class XmlFeedConfigCreateDTO {

    /**
     * Platform type: 1-LinkedIn, 2-Indeed
     */
    @NotNull(message = "Platform type cannot be null")
    @Min(value = 1, message = "Platform type must be between 1-3")
    @Max(value = 3, message = "Platform type must be between 1-3")
    private Integer platformType;

    /**
     * Indeed dedicated account email (only required for Indeed platform)
     */
    @Email(message = "Invalid email format")
    private String accountEmail;

    /**
     * Auto update interval (hours), range 1-24
     */
    @NotNull(message = "Update interval cannot be null")
    @Min(value = 1, message = "Update interval must be between 1-24 hours")
    @Max(value = 24, message = "Update interval must be between 1-24 hours")
    private Integer updateIntervalHours;
}
