package com.item.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * XML Builder Utility
 * 
 * This utility class provides methods for building XML feeds from source objects.
 * It uses Jackson XML mapper to serialize objects to XML format with proper formatting.
 *
 * @author liyunlong
 * @version 1.0
 * @since 2025-08-26
 */
@Slf4j
@Component
public class XmlBuilder {

    private final XmlMapper xmlMapper;

    public XmlBuilder() {
        this.xmlMapper = new XmlMapper();
        this.xmlMapper.setDefaultUseWrapper(false);
    }

    public String buildXmlFeed(Object sourceVO) {
        xmlMapper.enable(SerializationFeature.INDENT_OUTPUT);
        xmlMapper.enable(ToXmlGenerator.Feature.WRITE_XML_DECLARATION);
        // 输出
        String xml = null;
        try {
            xml = xmlMapper.writeValueAsString(sourceVO);
            return xml;
        } catch (JsonProcessingException e) {
            log.info("Failed to build XML feed",e);
            throw new RuntimeException(e);
        }
    }
}