package com.item.vo.feed;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlCData;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Data;

/**
 * ZipRecruiter job
 *
 * @author liyunlong
 * @version 1.0
 * @since 2025-09-02  13:28
 */
@Data
public class ZipRecruiterJobVO {
    @JacksonXmlProperty(localName = "referencenumber")
    private String referenceNumber;

    @JacksonXmlProperty(localName = "title")
    private String title;

    @JacksonXmlProperty(localName = "description")
    @JacksonXmlCData
    private String description;

    @JacksonXmlProperty(localName = "country")
    private String country;

    @JacksonXmlProperty(localName = "city")
    private String city;

    @JacksonXmlProperty(localName = "state")
    private String state;

//    @JacksonXmlProperty(localName = "postalcode")
//    private String postalCode;

    @JacksonXmlProperty(localName = "company")
    private String company;

    @JacksonXmlProperty(localName = "date")
    private String date;

    // Candidate Delivery Fields
    @JacksonXmlProperty(localName = "email")
    private String email;

    @JacksonXmlProperty(localName = "url")
    @JacksonXmlCData
    private String url;

    // Extended Job Fields
//    @JacksonXmlProperty(localName = "address")
//    private String address;

//    @JacksonXmlProperty(localName = "accept_remote")
//    private Integer acceptRemote;

    @JacksonXmlProperty(localName = "jobtype")
    private String jobType;

//    @JacksonXmlProperty(localName = "experience")
//    private String experience;
//
//    @JacksonXmlProperty(localName = "education")
//    private String education;

    @JacksonXmlProperty(localName = "compensation_interval")
    private String compensationInterval;

    @JacksonXmlProperty(localName = "compensation_min")
    private Double compensationMin;

    @JacksonXmlProperty(localName = "compensation_max")
    private Double compensationMax;

    @JacksonXmlProperty(localName = "compensation_currency")
    private String compensationCurrency;
}