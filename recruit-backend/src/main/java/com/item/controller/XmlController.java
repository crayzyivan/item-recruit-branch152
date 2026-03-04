package com.item.controller;

import com.item.framework.annotation.Auth;
import com.item.framework.constant.RoleType;
import com.item.service.XmlFeedService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * XML Feed生成接口
 *
 * @author liyunlong
 * @version 1.0
 * @since 2025-08-26
 */
@Slf4j
@RestController
@RequestMapping("/api/xml")
@RequiredArgsConstructor
public class XmlController {

    private final XmlFeedService xmlFeedService;

    /**
     * 获取LinkedIn XML Feed
     * 
     * 返回系统中所有活跃职位的LinkedIn格式XML数据，
     * 供LinkedIn等外部平台直接集成使用。
     *
     * @return LinkedIn XML格式的职位数据
     */
    @GetMapping(value = "/linked-in", produces = MediaType.APPLICATION_XML_VALUE)
    public String getLinkedInXml() {
        return xmlFeedService.getLinkedInXml(null);
    }

    /**
     * 获取Indeed XML Feed
     * 
     * 返回系统中所有活跃职位的Indeed格式XML数据，
     * 供Indeed等外部平台直接集成使用。
     *
     * @return Indeed XML格式的职位数据
     */
    @GetMapping(value="/indeed",produces = MediaType.APPLICATION_XML_VALUE)
    public String getIndeedXml() {
        return xmlFeedService.getIndeedXml(null);
    }

    /**
     * 获取指定公司的LinkedIn XML Feed
     *
     * @param companyCode 公司代码
     * @return LinkedIn XML格式的职位数据
     */
    @GetMapping(value = "/linked-in/{companyCode}", produces = MediaType.APPLICATION_XML_VALUE)
    public String getLinkedInXmlByCompany(@PathVariable String companyCode) {
        return xmlFeedService.getLinkedInXml(companyCode);
    }

    /**
     * 获取指定公司的Indeed XML Feed
     *
     * @param companyCode 公司代码
     * @return Indeed XML格式的职位数据
     */
    @GetMapping(value="/indeed/{companyCode}",produces = MediaType.APPLICATION_XML_VALUE)
    public String getIndeedXmlByCompany(@PathVariable String companyCode) {
        return xmlFeedService.getIndeedXml(companyCode);
    }

    /**
     * 获取指定公司的zipRecruiter XML Feed
     *
     * @param companyCode 公司代码
     * @return Indeed XML格式的职位数据
     */
    @GetMapping(value="/ziprecruiter/{companyCode}",produces = MediaType.APPLICATION_XML_VALUE)
    public String getZipRecruiterByCompany(@PathVariable String companyCode) {
        return xmlFeedService.getZipRecruiterXml(companyCode);
    }


    /**
     * 生成LinkedIn XML Feed
     * 
     * 手动触发LinkedIn格式XML文件的生成，
     * 主要用于测试和调试目的。
     *
     * @return 生成成功的职位数量
     */
    @Auth(roleType = RoleType.SUB_USER)
    @GetMapping("/generate/linked-in")
    public void generateLinkedInXML() {
        xmlFeedService.generateLinkedInXML(null);
    }

    /**
     * 生成Indeed XML Feed
     * 
     * 手动触发Indeed格式XML文件的生成，
     * 主要用于测试和调试目的。
     *
     * @return 生成成功的职位数量
     */
    @Auth(roleType = RoleType.SUB_USER)
    @GetMapping("/generate/indeed")
    public void generateIndeedXML() {
        xmlFeedService.generateIndeedXML(null,null);
    }
}