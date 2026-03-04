package com.item.vo.feed;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlCData;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Data;

/**
 * Indeed Job VO
 * 
 * This class represents a single job entry in the Indeed XML feed.
 * It contains all job-related information required by Indeed's feed specification.
 *
 * @author liyunlong
 * @version 1.0
 * @since 2025-08-26
 */
@Data
public class IndeedJobVO {
    @JacksonXmlProperty(localName = "title")
    @JacksonXmlCData
    private String title;

    @JacksonXmlProperty(localName = "date")
    @JacksonXmlCData
    private String date;

    @JacksonXmlProperty(localName = "referencenumber")
    @JacksonXmlCData
    private String referencenumber;

    @JacksonXmlProperty(localName = "requisitionid")
    @JacksonXmlCData
    private String requisitionid;

    @JacksonXmlProperty(localName = "url")
    @JacksonXmlCData
    private String url;

    @JacksonXmlProperty(localName = "company")
    @JacksonXmlCData
    private String company;

//    @JacksonXmlProperty(localName = "sourcename")
//    @JacksonXmlCData
//    private String sourcename;

    @JacksonXmlProperty(localName = "city")
    @JacksonXmlCData
    private String city;

    @JacksonXmlProperty(localName = "state")
    @JacksonXmlCData
    private String state;

    @JacksonXmlProperty(localName = "country")
    @JacksonXmlCData
    private String country;

    @JacksonXmlProperty(localName = "postalcode")
    @JacksonXmlCData
    private String postalcode;

    @JacksonXmlProperty(localName = "streetaddress")
    @JacksonXmlCData
    private String streetaddress;

    @JacksonXmlProperty(localName = "email")
    @JacksonXmlCData
    private String email;

    @JacksonXmlProperty(localName = "description")
    @JacksonXmlCData
    private String description;

    @JacksonXmlProperty(localName = "salary")
    @JacksonXmlCData
    private String salary;

    @JacksonXmlProperty(localName = "education")
    @JacksonXmlCData
    private String education;

    @JacksonXmlProperty(localName = "jobtype")
    @JacksonXmlCData
    private String jobtype;

    @JacksonXmlProperty(localName = "category")
    @JacksonXmlCData
    private String category;

    @JacksonXmlProperty(localName = "experience")
    @JacksonXmlCData
    private String experience;

    @JacksonXmlProperty(localName = "expirationdate")
    @JacksonXmlCData
    private String expirationdate;

    @JacksonXmlProperty(localName = "remotetype")
    @JacksonXmlCData
    private String remotetype;

    // 有横杠的字段必须指定 localName
    @JacksonXmlProperty(localName = "indeed-apply-data")
    @JacksonXmlCData
    private String indeedApplyData;
}