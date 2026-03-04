package com.item.vo.feed;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlCData;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Data;

/**
 * LinkedIn Salary VO
 * 
 * This class represents salary information in LinkedIn XML feed.
 * It contains high/low salary ranges, payment period and salary type.
 *
 * @author liyunlong
 * @version 1.0
 * @since 2025-08-26
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LinkedSalaryVO {
    @JacksonXmlProperty(localName = "highEnd")
    private LinkedSalaryRangeVO highEnd;

    @JacksonXmlProperty(localName = "lowEnd")
    private LinkedSalaryRangeVO lowEnd;

    @JacksonXmlProperty(localName = "period")
    @JacksonXmlCData
    private String period;

    @JacksonXmlProperty(localName = "type")
    @JacksonXmlCData
    private String type;
}