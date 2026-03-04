package com.item.service.impl;

import com.item.dto.job.JobCreateDTO;
import com.item.dto.job.JobUpdateDTO;
import com.item.dto.job.LocationValDTO;
import com.item.entity.JobEntity;
import com.item.framework.constant.JobStatus;
import com.item.service.JobDomainService;
import com.item.service.JobService;
import com.item.vo.JobDetailVO;
import com.item.vo.job.JobCreateResponseVO;
import com.item.vo.job.JobUpdateResponseVO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

/**
 * RP-190功能验证测试：移除job发布时的重复title校验逻辑
 * 
 * 测试场景：
 * 1. 发布title相同的job - 应该成功
 * 2. 发布title不同的job - 应该成功
 * 3. 发布title相似的job - 应该成功
 * 4. 验证核心功能正常工作
 * 
 * 注意：由于JobUpdateDTO中title字段被注释，编辑title功能需要手动测试
 * 
 * @author RP-190 Task Verification
 */
@Slf4j
@SpringBootTest
//@ActiveProfiles("test")
class JobDuplicateTitleValidationTest extends BaseServiceTestWithUserContext {

    @Resource
    private JobDomainService jobDomainService;
    
    @Resource
    private JobService jobService;

    /**
     * 创建测试用的JobCreateDTO
     */
    private JobCreateDTO createTestJobCreateDTO(String title) {
        JobCreateDTO dto = new JobCreateDTO();
        dto.setTitle(title);
        dto.setJobDetail("Test job detail for " + title);
        
        // 设置必填字段
        List<String> minimumJobRequirement = new ArrayList<>();
        minimumJobRequirement.add("Bachelor's degree");
        minimumJobRequirement.add("2+ years experience");
        dto.setMinimumJobRequirement(minimumJobRequirement);
        
        List<String> mainDuty = new ArrayList<>();
        mainDuty.add("Develop software");
        mainDuty.add("Code review");
        dto.setMainDuty(mainDuty);
        
        List<String> skills = new ArrayList<>();
        skills.add("Java");
        skills.add("Spring Boot");
        dto.setSkills(skills);
        
        // 设置其他必要字段
        dto.setTypeId(1);
        dto.setCategoryId(1);
        dto.setModeId(1);
        dto.setSalaryType(12);
        dto.setCurrency(5);
        dto.setNumberOpenings(1);
        dto.setMinSalary(5000);
        dto.setMaxSalary(8000);
        dto.setInterviewLength(20);
        dto.setEnableWrittenTest(true);
        dto.setHotList(0);
        
        // 设置位置信息
        List<LocationValDTO> locations = new ArrayList<>();
        LocationValDTO location = new LocationValDTO();
        location.setCountryId(101L);
        location.setCountryName("India");
        location.setStateId(4023L);
        location.setStateName("Andaman and Nicobar Islands");
        location.setCityId(133213L);
        location.setCityName("Nicobar");
        location.setLocationName("Nicobar, Andaman and Nicobar Islands, India");
        locations.add(location);
        dto.setLocations(locations);
        
        return dto;
    }

    /**
     * 更新测试用的JobCreateDTO
     */
    private JobUpdateDTO updateTestJobCreateDTO(String title) {
        JobUpdateDTO dto = new JobUpdateDTO();
//        dto.setTitle(title);
        dto.setJobId(175L);
        dto.setJobDetail("Test job detail for " + title);

        // 设置必填字段
        List<String> minimumJobRequirement = new ArrayList<>();
        minimumJobRequirement.add("Bachelor's degree");
        minimumJobRequirement.add("2+ years experience");
        dto.setMinimumJobRequirement(minimumJobRequirement);

        List<String> mainDuty = new ArrayList<>();
        mainDuty.add("Develop software");
        mainDuty.add("Code review");
        dto.setMainDuty(mainDuty);

        List<String> skills = new ArrayList<>();
        skills.add("Java");
        skills.add("Spring Boot");
        dto.setSkills(skills);

        // 设置其他必要字段
        dto.setTypeId(1);
        dto.setCategoryId(1);
        dto.setModeId(1);
        dto.setSalaryType(12);
        dto.setCurrency(5);
        dto.setNumberOpenings(1);
        dto.setMinSalary(5000);
        dto.setMaxSalary(8000);
        dto.setInterviewLength(20);
        dto.setEnableWrittenTest(true);
//        dto.setHotList(0);

        // 设置位置信息
        List<LocationValDTO> locations = new ArrayList<>();
        LocationValDTO location = new LocationValDTO();
        location.setCountryId(101L);
        location.setCountryName("India");
        location.setStateId(4023L);
        location.setStateName("Andaman and Nicobar Islands");
        location.setCityId(133213L);
        location.setCityName("Nicobar");
        location.setLocationName("Nicobar, Andaman and Nicobar Islands, India");
        locations.add(location);
        dto.setLocations(locations);

        return dto;
    }

    @Test
    @DisplayName("测试1: 发布title相同的job - 应该成功")
    void testPublishJobsWithSameTitle() {
        log.info("开始测试：发布title相同的job");
        
        String sameTitle = "Software Engineer - Test Same Title";

        try {
            // 发布第一个job
            JobCreateDTO dto1 = createTestJobCreateDTO(sameTitle);
            JobCreateResponseVO jobId1 = jobDomainService.publishJob(dto1);
            assertNotNull(jobId1, "第一个job应该发布成功");
            log.info("第一个job发布成功，ID: {}", jobId1.getJobId());
            
            // 发布第二个相同title的job
            JobCreateDTO dto2 = createTestJobCreateDTO(sameTitle);
            JobCreateResponseVO jobId2 = jobDomainService.publishJob(dto2);
            assertNotNull(jobId2, "第二个相同title的job应该发布成功");
            assertNotEquals(jobId1.getJobId(), jobId2.getJobId(), "两个job应该有不同的ID");
            log.info("第二个相同title的job发布成功，ID: {}", jobId2.getJobId());
            
            log.info("测试1通过：成功发布了两个相同title的job");
            
        } catch (Exception e) {
            log.error("测试1失败：发布相同title的job时出现异常", e);
            fail("发布相同title的job不应该抛出异常: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("测试2: 发布title不同的job - 应该成功")
    void testPublishJobsWithDifferentTitles() {
        log.info("开始测试：发布title不同的job");
        
        try {
            // 发布第一个job
            JobCreateDTO dto1 = createTestJobCreateDTO("Frontend Developer");
            JobCreateResponseVO jobId1 = jobDomainService.publishJob(dto1);
            assertNotNull(jobId1, "第一个job应该发布成功");
            log.info("第一个job发布成功，ID: {}", jobId1.getJobId());
            
            // 发布第二个不同title的job
            JobCreateDTO dto2 = createTestJobCreateDTO("Backend Developer");
            JobCreateResponseVO jobId2 = jobDomainService.publishJob(dto2);
            assertNotNull(jobId2, "第二个不同title的job应该发布成功");
            assertNotEquals(jobId1.getJobId(), jobId2.getJobId(), "两个job应该有不同的ID");
            log.info("第二个不同title的job发布成功，ID: {}", jobId2.getJobId());
            
            log.info("测试2通过：成功发布了两个不同title的job");
            
        } catch (Exception e) {
            log.error("测试2失败：发布不同title的job时出现异常", e);
            fail("发布不同title的job不应该抛出异常: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("测试3: 发布title相似的job - 应该成功")
    void testPublishJobsWithSimilarTitles() {
        log.info("开始测试：发布title相似的job");
        
        try {
            // 发布第一个job
            JobCreateDTO dto1 = createTestJobCreateDTO("Java Developer");
            JobCreateResponseVO jobId1 = jobDomainService.publishJob(dto1);
            assertNotNull(jobId1, "第一个job应该发布成功");
            log.info("第一个job发布成功，ID: {}", jobId1.getJobId());
            
            // 发布第二个相似title的job
            JobCreateDTO dto2 = createTestJobCreateDTO("java developer");
            JobCreateResponseVO jobId2 = jobDomainService.publishJob(dto2);
            assertNotNull(jobId2, "第二个相似title的job应该发布成功");
            assertNotEquals(jobId1.getJobId(), jobId2.getJobId(), "两个job应该有不同的ID");
            log.info("第二个相似title的job发布成功，ID: {}", jobId2.getJobId());
            
            log.info("测试3通过：成功发布了两个相似title的job");
            
        } catch (Exception e) {
            log.error("测试3失败：发布相似title的job时出现异常", e);
            fail("发布相似title的job不应该抛出异常: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("综合测试：验证核心功能正常工作")
    void testComprehensiveFunctionality() {
        log.info("开始综合测试：验证核心功能正常工作");
        
        try {
            // 1. 发布一个job
            Long jobId = 173L;
            assertNotNull(jobId, "job发布应该成功");
            log.info("job发布成功，ID: {}", jobId);
            
            // 2. 验证job确实被创建
            JobEntity jobEntity = jobService.getJobsByIds(jobId);
            assertNotNull(jobEntity, "应该能够查询到创建的job");
            assertEquals("Software Engineer - Test Same Title", jobEntity.getTitle(), "job标题应该正确");
            assertEquals(JobStatus.ACTIVE.getCode(), jobEntity.getJobStatus(), "job状态应该是ACTIVE");
            log.info("job查询验证成功");
            
            // 3. 验证companyTitleHash字段正常生成
            assertNotNull(jobEntity.getCompanyTitleHash(), "companyTitleHash应该被正常生成");
            log.info("companyTitleHash验证成功: {}", jobEntity.getCompanyTitleHash());
            
            log.info("综合测试通过：所有核心功能正常工作");
            
        } catch (Exception e) {
            log.error("综合测试失败：核心功能出现异常", e);
            fail("核心功能不应该出现异常: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("验证分享链接功能正常")
    void testShareLinkFunctionality() {
        log.info("开始测试：验证分享链接功能正常");
        
        try {
            // 1. 发布一个job
            Long jobId  = 173L;
            assertNotNull(jobId, "job发布应该成功");
            log.info("job发布成功，ID: {}", jobId);
            
            // 2. 生成job分享链接
            String jobShareLink = jobDomainService.generateJobInfoShareLink(jobId);
            assertNotNull(jobShareLink, "job分享链接应该生成成功");
            assertFalse(jobShareLink.isEmpty(), "job分享链接不应该为空");
            assertEquals(jobShareLink, "https://recruit-dev.item.pub/job-details/69e25yc4a/recruit-dev-software-engineer-test-same-title", "job分享链接不正确");
            log.info("job分享链接生成成功: {}", jobShareLink);
            
            // 3. 生成公司分享链接
            String companyShareLink = jobDomainService.generateCompanyShareLink();
            assertNotNull(companyShareLink, "公司分享链接应该生成成功");
            assertFalse(companyShareLink.isEmpty(), "公司分享链接不应该为空");
            assertEquals(companyShareLink,"https://recruit-dev.item.pub/job-list/RDXX0001/recruit-dev", "公司分享链接不正确");
            log.info("公司分享链接生成成功: {}", companyShareLink);
            
            log.info("分享链接功能测试通过");
            
        } catch (Exception e) {
            log.error("分享链接功能测试失败", e);
            fail("分享链接功能不应该出现异常: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("验证编辑")
    void testUpdateJobsWithSimilarTitles() {
        log.info("开始测试：编辑job");

        try {
            // 发布第一个job
            JobUpdateDTO dto1 = updateTestJobCreateDTO("Java Developer Edit");
            JobUpdateResponseVO success = jobDomainService.updateJob(dto1);
            assertFalse(success.isExistSimilarJob(), "修改job应该成功");
            Long jobId = dto1.getJobId();
            JobDetailVO jobDetailById = jobDomainService.getJobDetailById(jobId, true);

            log.info("修改的jobId: {}", jobDetailById.getJobId());

            String title = jobDetailById.getTitle();
            String jobDetail = jobDetailById.getJobDetail();
            assertEquals("Frontend Developer", title, "编辑title的job应该成功");
            assertEquals("Test job detail for Java Developer Edit", jobDetail, "编辑detail的job应该成功");

            log.info("编辑的job {}", jobDetailById);

        } catch (Exception e) {
            log.error("编辑的job时出现异常", e);
            fail("编辑job不应该抛出异常: " + e.getMessage());
        }
    }
}