package com.item.vo.feed;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Data;

import java.util.List;

/**
 * ZipRecruiter source
 *
 * @author liyunlong
 * @version 1.0
 * @since 2025-09-02  13:27
 */
@Data
@JacksonXmlRootElement(localName = "source")
public class ZipRecruiterSourceVO {
    @JacksonXmlProperty(localName = "lastBuildDate")
    private String lastBuildDate;

//    @JacksonXmlProperty(localName = "publisherurl")
//    private String publisherUrl;
//
//    @JacksonXmlProperty(localName = "publisher")
//    private String publisher;

    @JacksonXmlElementWrapper(useWrapping = false) // 不要额外包一层 <jobs>
    @JacksonXmlProperty(localName = "job")
    private List<ZipRecruiterJobVO> jobs;
}