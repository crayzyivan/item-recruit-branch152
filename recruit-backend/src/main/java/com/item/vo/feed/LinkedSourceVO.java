package com.item.vo.feed;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Data;

import java.util.List;

/**
 * LinkedIn XML Feed Source VO
 * 
 * This class represents the root element of LinkedIn XML feed structure.
 * It contains the job listings and metadata for LinkedIn feed generation.
 *
 * @author liyunlong
 * @version 1.0
 * @since 2025-08-26
 */
// 根：<source>
@Data
@JacksonXmlRootElement(localName = "source")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LinkedSourceVO {
    @JacksonXmlProperty(localName = "lastBuildDate")
    private String lastBuildDate;

    // 多个 <job>，不再额外包一层 <jobs>
    @JacksonXmlProperty(localName = "job")
    @JacksonXmlElementWrapper(useWrapping = false)
    private List<LinkedJobVO> job;
}