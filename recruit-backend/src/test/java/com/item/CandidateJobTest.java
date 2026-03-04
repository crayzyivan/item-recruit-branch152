package com.item;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import com.item.dto.job.CandidateJobApplyDto;
import com.item.dto.job.LocationValRecordDTO;
import com.item.es.JobEsService;
import com.item.es.entity.JobEsEntity;
import com.item.service.CandidateJobDomainService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

/**
 * 测试候选人与职位关联
 *
 * @author liyunlong
 * @version 1.0
 * @since 2025-08-05  20:55
 */
@Slf4j
@SpringBootTest
public class CandidateJobTest {
    @Autowired
    private  ElasticsearchClient esClient;
    @Autowired
    private  JobEsService jobEsService;
    @Resource
    private CandidateJobDomainService candidateJobDomainService;


    @Test
    public void selectEsJob(){
        JobEsEntity jobEsEntity = jobEsService.getJobById(125L);
        log.info("jobEsEntity:{}",jobEsEntity);
    }


    @Test
    public void updateEsJob(){
        JobEsEntity jobEsEntity = jobEsService.getJobById(125L);
        List<LocationValRecordDTO> locations=new ArrayList<>();
        LocationValRecordDTO location1=new LocationValRecordDTO();
        location1.setCountryId(1L);
        location1.setStateId(3871L);
        location1.setCityId(108L);
        location1.setCountryName("Afghanistan");
        location1.setStateName("Badghis");
        location1.setCityName("Qala i Naw");
        locations.add(location1);
        jobEsEntity.setLocations(locations);
        jobEsService.updateJobEs(jobEsEntity);
    }



    @Test
    public void testApplyJob(){
        CandidateJobApplyDto dto = new CandidateJobApplyDto();
        dto.setCandidateId(120L);
        dto.setJobId(125L);
        dto.setJobCountryId(1L);
        dto.setJobStateId(3871L);
        dto.setJobCityId(108L);
        candidateJobDomainService.applyJob(dto);
    }




}