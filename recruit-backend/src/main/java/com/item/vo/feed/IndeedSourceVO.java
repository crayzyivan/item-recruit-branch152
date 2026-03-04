package com.item.vo.feed;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlCData;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Data;

import java.util.List;

/**
 * Indeed XML Feed Source VO
 * 
 * This class represents the root element of Indeed XML feed structure.
 * It contains publisher information and job listings for Indeed feed generation.
 *
 * @author liyunlong
 * @version 1.0
 * @since 2025-08-26
 */
@Data
@JacksonXmlRootElement(localName = "source")
public class IndeedSourceVO {
//    @JacksonXmlProperty(localName = "publisher")
//    private String publisher;
//
//    @JacksonXmlProperty(localName = "publisherurl")
//    private String publisherurl;

    // 注意：Jackson 默认会包一层 <jobs>，所以要用 useWrapping=false
    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "job")
    private List<IndeedJobVO> jobs;
}