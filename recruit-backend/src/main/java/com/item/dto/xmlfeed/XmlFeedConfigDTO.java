package com.item.dto.xmlfeed;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * XML Feed configuration DTO
 *
 * @author liyunlong
 * @since 2025-08-26
 */
@Data
public class XmlFeedConfigDTO {

    private Long id;

    /**
     * Company code
     */
    private String companyCode;

    /**
     * Platform type: 1-LinkedIn, 2-Indeed
     */
    private Integer platformType;

    /**
     * XML Feed URL address
     */
    private String feedUrl;

    /**
     * Indeed dedicated account email
     */
    private String accountEmail;

    /**
     * Auto update interval (hours), range 1-24
     */
    private Integer updateIntervalHours;

    /**
     * Last update time
     */
    private LocalDateTime lastUpdateTime;

    /**
     * Next update time
     */
    private LocalDateTime nextUpdateTime;

    /**
     * Create time
     */
    private LocalDateTime createTime;

    /**
     * Update time
     */
    private LocalDateTime updateTime;

    /**
     * Creator ID
     */
    private Long createBy;

    /**
     * Updater ID
     */
    private Long updateBy;
}
