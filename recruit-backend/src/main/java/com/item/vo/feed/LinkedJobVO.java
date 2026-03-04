package com.item.vo.feed;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlCData;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Data;

import java.util.List;

/**
 * LinkedIn Job VO
 * 
 * This class represents a single job entry in the LinkedIn XML feed.
 * It contains all job-related information required by LinkedIn's feed specification.
 *
 * @author liyunlong
 * @version 1.0
 * @since 2025-08-26
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LinkedJobVO {
    @JacksonXmlProperty(localName = "partnerJobId")
    @JacksonXmlCData
    private String partnerJobId;
    @JacksonXmlProperty(localName = "company")
    @JacksonXmlCData
    private String company;
    @JacksonXmlProperty(localName = "companyId")
    @JacksonXmlCData
    private String companyId;
    @JacksonXmlProperty(localName = "title")
    @JacksonXmlCData
    private String title;

    // 需要保留 HTML/富文本 —— 用 CDATA
    @JacksonXmlProperty(localName = "description")
    @JacksonXmlCData
    private String description;

    @JacksonXmlProperty(localName = "applyUrl")
    @JacksonXmlCData
    private String applyUrl;

    @JacksonXmlProperty(localName = "location")
    @JacksonXmlCData
    private String location;
    @JacksonXmlProperty(localName = "jobtype")
    @JacksonXmlCData
    private String jobtype;
    @JacksonXmlProperty(localName = "listDate")
    @JacksonXmlCData
    private String listDate;
    @JacksonXmlElementWrapper(localName = "salaries", useWrapping = true)
    @JacksonXmlProperty(localName = "salary")
    private List<LinkedSalaryVO> salaries;

    @JacksonXmlProperty(localName = "workplaceTypes")
    @JacksonXmlCData
    private String workplaceTypes;

    @JacksonXmlProperty(localName = "jobPostingAvailability")
    private String jobPostingAvailability;

    @JacksonXmlElementWrapper(localName = "skills", useWrapping = true)
    @JacksonXmlProperty(localName = "skill")
    @JacksonXmlCData
    private List<String> skills;

}