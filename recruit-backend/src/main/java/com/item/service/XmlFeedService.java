package com.item.service;

import com.item.es.entity.JobEsEntity;

import java.time.LocalDateTime;
import java.util.List;

/**
 * XML Feed Service Interface
 * 
 * This service provides methods for generating and retrieving XML feeds
 * for LinkedIn and Indeed job posting platforms.
 *
 * @author liyunlong
 * @since 2025-08-26
 */
public interface XmlFeedService {

    /**
     * 生成linkedin xml
     */
    Integer generateLinkedInXML(String companyCode);

    Integer generateIndeedXML(String companyCode,String email);

    Integer generateZipRecruiterXML(String companyCode,String email);

    void generateJobsXML(Integer platformType,String companyCode, String email, List<JobEsEntity>activeJobs);

    String getLinkedInXml(String companyCode);

    String getIndeedXml(String companyCode);

    String getZipRecruiterXml(String companyCode);
}
