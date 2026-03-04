package com.item.dto.ayrshare;

import static com.item.framework.constant.CommonConstants.NumConstants.TWITTER_LENGTH_LIMIT;
import lombok.Data;

/**
 * @author : lh
 */
@Data
public class AyrShareTwitterOptionsDTO {
    private String twitterPost;

    public void subTwitterPost(String message, String url) {
        if (message.length() + url.length() > TWITTER_LENGTH_LIMIT) {
            if (url.length() > TWITTER_LENGTH_LIMIT) {
                this.setTwitterPost(message.substring(0, TWITTER_LENGTH_LIMIT));
                return;
            }
            this.setTwitterPost(message.substring(0, TWITTER_LENGTH_LIMIT - url.length() - 1) + " " + url);
            return;
        }
        this.setTwitterPost(message + url);
    }
}
