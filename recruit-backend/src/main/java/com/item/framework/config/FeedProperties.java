package com.item.framework.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

/**
 * XML Feed configuration properties
 * 
 * 用于配置XML Feed相关的参数，包括S3存储路径、文件命名、
 * 平台特定配置、Guide URL等。支持通过配置中心动态刷新。
 *
 * 配置示例：
 * <pre>
 * feed:
 *   folder: "xml-feeds"
 *   linkedInXml: "linkedin-jobs.xml"
 *   indeedXml: "indeed-jobs.xml"
 *   size: 500
 *   publisher: "Your Company Name"
 *   publisherUrl: "https://your-company.com"
 *   linkedInGuideUrl: "https://business.linkedin.com/talent-solutions/post-jobs-free/xml-feed"
 *   indeedGuideUrl: "https://indeed.com/hire/how-to-post-a-job"
 * </pre>
 *
 * @author liyunlong
 * @version 1.0
 * @since 2025-08-26
 */
@RefreshScope
@Component
@Data
@ConfigurationProperties(prefix = "feed")
public class FeedProperties {
    
    /**
     * S3 folder for XML feed files
     */
    private String folder;
    
    /**
     * LinkedIn XML file name
     */
    private String linkedInXml;
    
    /**
     * Indeed XML file name
     */
    private String indeedXml;

    private String zipRecruiterXml="zipRecruiter.xml";
    
    /**
     * Maximum job count per XML feed (default 500)
     */
    private Integer size = 500;
    
    /**
     * Default Indeed email for fallback scenarios
     */
    private String indeedEmail;
    
//    /**
//     * Publisher name for XML feeds
//     */
//    private String publisher = "Recruit System";
//
//    /**
//     * Publisher URL for XML feeds
//     */
//    private String publisherUrl;
    
    /**
     * LinkedIn job posting guide URL
     */
    private String linkedInGuideUrl;
    
    /**
     * Indeed job posting guide URL
     */
    private String indeedGuideUrl;

    /**
     * ziprecruiter job posting guide URL
     */
    private String zipRecruiterGuideUrl;

    private String feedUrlPrefix;

    /**
     * 是否去重
     */
    private Boolean linkedInDistinct=true;
}