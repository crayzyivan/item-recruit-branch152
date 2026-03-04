package com.item.vo.feed;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * XML Feed configuration response VO
 *
 * @author liyunlong
 * @since 2025-08-26
 */
@Data
public class XmlFeedConfigVO {

    private Long id;

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
     * Platform guide documentation URL
     */
    private String guideUrl;

    /**
     * Auto update interval (hours)
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

}
