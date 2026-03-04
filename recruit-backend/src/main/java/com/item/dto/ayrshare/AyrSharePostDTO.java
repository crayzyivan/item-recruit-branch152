package com.item.dto.ayrshare;

import lombok.Data;

import java.util.List;

/**
 * @author : lh
 */
@Data
public class AyrSharePostDTO {

    private String companyName;
    private String companyCode;
    private Long jobId;
    private String jobTitle;
    private List<String> platforms;
    private String mediaUrls;
    private String publishUrl;

    private String jobPublishShareCase;
    private String jobPublishShareTitleCase;
}
