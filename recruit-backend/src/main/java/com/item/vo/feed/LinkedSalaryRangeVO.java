package com.item.vo.feed;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlCData;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Data;

/**
 * LinkedIn Salary Range VO
 * 
 * This class represents a salary range boundary in LinkedIn XML feed.
 * It contains the monetary amount and currency code for salary ranges.
 *
 * @author liyunlong
 * @version 1.0
 * @since 2025-08-26
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LinkedSalaryRangeVO {
    @JacksonXmlProperty(localName = "amount")
    @JacksonXmlCData
    private String amount; // e.g., 100000

    @JacksonXmlProperty(localName = "currencyCode")
    @JacksonXmlCData
    private String currencyCode;
}