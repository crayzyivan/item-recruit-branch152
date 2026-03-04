package com.item.dto;

import com.item.dto.ayrshare.AyrShareRedditOptionsDTO;
import com.item.dto.ayrshare.AyrShareTwitterOptionsDTO;
import com.item.dto.ayrshare.AyrShareYouTubeOptionsDTO;
import com.item.framework.constant.CommonConstants;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.text.MessageFormat;
import java.util.List;
import java.util.Set;

/**
 * @author : lh
 */
@Data
public class AyrShareRequestDTO {
//    参考 {
//        "post": "Today is a great day!",
//            "platforms": ["twitter", "facebook", "instagram", "linkedin"],
//        "mediaUrls": ["https://img.ayrshare.com/012/gb.jpg"]
//    }

    private String post;
    private List<String> platforms;
    private String mediaUrls;
    private AyrShareRedditOptionsDTO redditOptions;
    private AyrShareYouTubeOptionsDTO youTubeOptions;
    private AyrShareTwitterOptionsDTO twitterCustomerOptions;

    public static AyrShareRequestDTO buildDefault(String post) {
        return build(post, List.of("twitter", "facebook", "instagram", "linkedin"), null);
    }

    public static AyrShareRequestDTO buildDefault(String post, String mediaUrls) {
        return build(post, List.of("twitter", "facebook", "instagram", "linkedin"), mediaUrls);
    }

    public static AyrShareRequestDTO build(String post, List<String> platforms, String mediaUrls) {
        AyrShareRequestDTO ayrShareRequestDTO = new AyrShareRequestDTO();
        ayrShareRequestDTO.setPost(post);
        ayrShareRequestDTO.setPlatforms(platforms);
        ayrShareRequestDTO.setMediaUrls(mediaUrls);
        return ayrShareRequestDTO;
    }

    public static AyrShareRequestDTO build(List<String> platforms, String publishUrl, String subreddit, String companyName,
                                           String jobTitle, String jobPublishShareCase, String jobPublishShareTitleCase) {
        AyrShareRequestDTO ayrShareRequestDTO = new AyrShareRequestDTO();
        String message = MessageFormat.format(jobPublishShareCase, companyName, jobTitle, publishUrl);
        ayrShareRequestDTO.setPost(message);
        ayrShareRequestDTO.setPlatforms(platforms);
        ayrShareRequestDTO.setMediaUrls(null);
        AyrShareRedditOptionsDTO ayrShareRedditOptionsDTO = buildRedditOptionsByPlatform(platforms, publishUrl, subreddit, companyName, jobTitle, jobPublishShareTitleCase);
        AyrShareYouTubeOptionsDTO ayrShareYouTubeOptionsDTO = buildYouTubeOptionsByPlatform(platforms, publishUrl, subreddit, jobTitle, jobPublishShareTitleCase);
        AyrShareTwitterOptionsDTO ayrShareTwitterOptionsDTO = buildTwitterOptionsByPlatform(platforms, publishUrl, companyName, jobTitle, jobPublishShareTitleCase);
        ayrShareRequestDTO.setRedditOptions(ayrShareRedditOptionsDTO);
        ayrShareRequestDTO.setYouTubeOptions(ayrShareYouTubeOptionsDTO);
        ayrShareRequestDTO.setTwitterCustomerOptions(ayrShareTwitterOptionsDTO);

        return ayrShareRequestDTO;
    }

    public static AyrShareRedditOptionsDTO buildRedditOptionsByPlatform(List<String> platforms, String publishUrl, String subreddit, String companyName,
                                                                        String jobTitle, String jobPublishShareTitleCase) {

        boolean reddit = platforms.stream().anyMatch(p -> p.equalsIgnoreCase(CommonConstants.StrConstants.ALL) || p.equalsIgnoreCase(CommonConstants.StrConstants.REDDIT));
        if (reddit) {
            AyrShareRedditOptionsDTO redditDTO = new AyrShareRedditOptionsDTO();
//            redditDTO.setLink(publishUrl);
            redditDTO.setSubreddit(subreddit);
            String message = MessageFormat.format(jobPublishShareTitleCase, companyName, jobTitle);
            redditDTO.setTitle(message);
            return redditDTO;
        }
        return null;
    }

    public static AyrShareYouTubeOptionsDTO buildYouTubeOptionsByPlatform(List<String> platforms, String publishUrl, String companyName,
                                                                          String jobTitle, String jobPublishShareTitleCase) {

        boolean youTube = platforms.stream().anyMatch(p -> p.equalsIgnoreCase(CommonConstants.StrConstants.ALL) || p.equalsIgnoreCase(CommonConstants.StrConstants.YOUTUBE));
        if (youTube) {
            AyrShareYouTubeOptionsDTO youTubeDTO = new AyrShareYouTubeOptionsDTO();
            String message = MessageFormat.format(jobPublishShareTitleCase, companyName, jobTitle);
            message = StringUtils.abbreviate(message, "", 100);
            youTubeDTO.setTitle(message);
            return youTubeDTO;
        }
        return null;
    }

    public static AyrShareTwitterOptionsDTO buildTwitterOptionsByPlatform(List<String> platforms, String publishUrl, String companyName,
                                                                          String jobTitle, String jobPublishShareTitleCase) {
        boolean twitter = platforms.stream().anyMatch(p -> p.equalsIgnoreCase(CommonConstants.StrConstants.ALL) || p.equalsIgnoreCase(CommonConstants.StrConstants.TWITTER));
        if (twitter) {
            AyrShareTwitterOptionsDTO youTubeDTO = new AyrShareTwitterOptionsDTO();
            String message = MessageFormat.format(jobPublishShareTitleCase, companyName, jobTitle);
            youTubeDTO.subTwitterPost(message, publishUrl);
            return youTubeDTO;
        }
        return null;
    }

    public AyrShareRequestDTO convertTwitter() {
        boolean isTwitter = this.getPlatforms().stream().anyMatch(p -> p.equalsIgnoreCase(CommonConstants.StrConstants.TWITTER));
        if (!isTwitter) {
            return null;
        }
        AyrShareRequestDTO twitter = new AyrShareRequestDTO();
        twitter.setPost(this.getTwitterCustomerOptions().getTwitterPost());
        twitter.setPlatforms(List.of(CommonConstants.StrConstants.TWITTER));
        return twitter;
    }

    public AyrShareRequestDTO convertRemoveTwitter() {
        this.setTwitterCustomerOptions(null);
        this.setPlatforms(this.getPlatforms().stream().filter(p -> !p.equalsIgnoreCase(CommonConstants.StrConstants.TWITTER)).toList());
        return this;
    }

    public AyrShareRequestDTO fillMediaUrls(String url) {
        Set<String> needMediaUrlsPlatform = needMediaUrlsPlatform();
        List<String> mediaUrls = this.getPlatforms().stream().filter(needMediaUrlsPlatform::contains).toList();
        if (CollectionUtils.isEmpty(mediaUrls)) {
            return null;
        }
        AyrShareRequestDTO media = new AyrShareRequestDTO();
        media.setPlatforms(mediaUrls);
        media.setPost(this.getPost());
        media.setMediaUrls(url);
        return media;
    }

    public AyrShareRequestDTO cleanNeedMediaUrlsPlatform() {
        Set<String> needMediaUrlsPlatform = needMediaUrlsPlatform();
        this.setPlatforms(this.getPlatforms().stream().filter(p -> !needMediaUrlsPlatform.contains(p)).toList());
        return this;
    }

    private Set<String> needMediaUrlsPlatform() {
        return Set.of(CommonConstants.StrConstants.INSTAGRAM, CommonConstants.StrConstants.PINTEREST);
    }
}
