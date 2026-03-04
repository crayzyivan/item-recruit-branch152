package com.item.dto.ayrshare;

import lombok.Data;

/**
 * @author : lh
 */
@Data
public class AyrShareRedditOptionsDTO {
    /**
     * required
     */
    private String title;
    /**
     * required
     */
    private String subreddit;
    private String link;
}
