package com.item.convert.migration.job;

import com.item.dto.migration.job.JobDetailsDTO;
import com.item.entity.JobEntity;
import com.item.entity.migration.job.PgJobEntity;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.UUID;

/**
 * Job 迁移数据转换器单元测试
 * 
 * 验证 PostgreSQL Job 到 MySQL JobEntity 和 JobEsEntity 的字段映射和数据转换功能。
 *
 * @author system
 * @since 2025-09-30
 */
@SpringBootTest
@Transactional
@Rollback
@ActiveProfiles("test")
class JobMigrationConvertTest {

    @Autowired
    private JobMigrationConvert jobMigrationConvert;

    private PgJobEntity pgJob;
    private JobDetailsDTO jobDetails;

    @BeforeEach
    void setUp() {
        // 创建测试用的 PostgreSQL Job 实体
        pgJob = new PgJobEntity();
        pgJob.setId(123);
        pgJob.setTitle("Senior Java Developer");
        pgJob.setSlug("senior-java-developer");
        pgJob.setCompanyId(456);
        pgJob.setStatus("active");
        pgJob.setLocationId(789);
        pgJob.setNumberOfOpenings(3);
        pgJob.setType("full_time");
        pgJob.setLocationType("hybrid");
        pgJob.setSalaryType("monthly");
        pgJob.setCurrencyId((short) 1);
        pgJob.setMinSalary(new BigDecimal("5000.00"));
        pgJob.setMaxSalary(new BigDecimal("8000.00"));
        pgJob.setHotlist(true);
        pgJob.setCreatedOn(OffsetDateTime.now());
        pgJob.setUpdatedOn(OffsetDateTime.now());
        pgJob.setCreatedBy(UUID.randomUUID().toString());
        pgJob.setUpdatedBy(UUID.randomUUID().toString());
        pgJob.setCategoryIds("{1,2,3}");

        // 创建测试用的 Job 详情
        jobDetails = new JobDetailsDTO();
        jobDetails.setOverview("We are looking for a Senior Java Developer to join our team.");
        jobDetails.setSkills(Arrays.asList("Java", "Spring Boot", "MySQL", "Redis"));
        jobDetails.setBenefits(Arrays.asList("Health Insurance", "Flexible Hours"));
        jobDetails.setResponsibilities(Arrays.asList(
            "Develop and maintain Java applications",
            "Collaborate with cross-functional teams",
            "Write clean, maintainable code"
        ));

        JobDetailsDTO.RequirementsDTO requirements = new JobDetailsDTO.RequirementsDTO();
        requirements.setMinimum(Arrays.asList(
            "Bachelor's degree in Computer Science",
            "3+ years of Java development experience"
        ));
        requirements.setPreferred(Arrays.asList(
            "Experience with microservices",
            "Knowledge of cloud platforms"
        ));
        jobDetails.setRequirements(requirements);
    }

    @Test
    void testConvertToJobEntity() {
        // 执行转换
        JobEntity jobEntity = jobMigrationConvert.convertToJobEntity(pgJob);

        // 验证基本字段映射
        assertNotNull(jobEntity);
        assertEquals(123L, jobEntity.getId());
        assertEquals("Senior Java Developer", jobEntity.getTitle());
        assertEquals(456L, jobEntity.getCustomerId());
        assertEquals(456L, jobEntity.getMasterAccountId());
        assertEquals(Integer.valueOf(789), jobEntity.getLocationId());
        assertEquals("senior-java-developer", jobEntity.getUrlCode());
        assertEquals(Integer.valueOf(3), jobEntity.getNumberOpenings());

        // 验证枚举值转换
        assertEquals(Integer.valueOf(1), jobEntity.getJobStatus()); // active -> 1
        assertEquals(Integer.valueOf(1), jobEntity.getTypeId()); // full_time -> 1
        assertEquals(Integer.valueOf(3), jobEntity.getModeId()); // hybrid -> 3
        assertEquals(Integer.valueOf(4), jobEntity.getSalaryType()); // monthly -> 4

        // 验证数值转换
        assertEquals(Integer.valueOf(1), jobEntity.getHotList()); // true -> 1
        assertEquals(Integer.valueOf(1), jobEntity.getCurrency()); // short 1 -> int 1
        assertEquals(Integer.valueOf(5000), jobEntity.getMinSalary());
        assertEquals(Integer.valueOf(8000), jobEntity.getMaxSalary());

        // 验证分类ID转换（取第一个）
        assertEquals(Integer.valueOf(1), jobEntity.getCategoryId());

        // 验证时间转换
        assertNotNull(jobEntity.getCreateTime());
        assertNotNull(jobEntity.getUpdateTime());
        assertTrue(jobEntity.getCreateTime() instanceof LocalDateTime);
        assertTrue(jobEntity.getUpdateTime() instanceof LocalDateTime);

        // 验证默认值
        assertEquals(Integer.valueOf(1), jobEntity.getNeedListed());
        assertEquals(Integer.valueOf(0), jobEntity.getAyrshareStatus());
    }

//    @Test
//    void testConvertToJobEsEntity() {
//        // 执行转换
//        JobEsEntity jobEsEntity = jobMigrationConvert.convertToJobEsEntity(pgJob, jobDetails);
//
//        // 验证基本字段映射
//        assertNotNull(jobEsEntity);
//        assertEquals(123L, jobEsEntity.getId());
//        assertEquals("Senior Java Developer", jobEsEntity.getTitle());
//        assertEquals(456L, jobEsEntity.getCustomerId());
//        assertEquals(456L, jobEsEntity.getMasterAccountId());
//        assertEquals(Integer.valueOf(789), jobEsEntity.getLocationId());
//
//        // 验证 Job 详情字段映射
//        assertEquals("We are looking for a Senior Java Developer to join our team.", jobEsEntity.getJobDetail());
//        assertEquals(4, jobEsEntity.getSkills().size());
//        assertTrue(jobEsEntity.getSkills().contains("Java"));
//        assertTrue(jobEsEntity.getSkills().contains("Spring Boot"));
//
//        assertEquals(2, jobEsEntity.getBenefits().size());
//        assertTrue(jobEsEntity.getBenefits().contains("Health Insurance"));
//
//        assertEquals(3, jobEsEntity.getMainDuty().size());
//        assertTrue(jobEsEntity.getMainDuty().contains("Develop and maintain Java applications"));
//
//        assertEquals(2, jobEsEntity.getMinimumJobRequirement().size());
//        assertTrue(jobEsEntity.getMinimumJobRequirement().contains("Bachelor's degree in Computer Science"));
//
//        assertEquals(2, jobEsEntity.getPreferredJobRequirement().size());
//        assertTrue(jobEsEntity.getPreferredJobRequirement().contains("Experience with microservices"));
//
//        // 验证默认值
//        assertEquals(Integer.valueOf(1), jobEsEntity.getNeedListed());
//        assertEquals(Integer.valueOf(0), jobEsEntity.getDeleted());
//        assertEquals(Integer.valueOf(0), jobEsEntity.getAyrshareStatus());
//    }

    @Test
    void testParseJobDetails() {
        String jsonString = """
            {
                "overview": "Test overview",
                "skills": ["Java", "Spring"],
                "benefits": ["Insurance"],
                "responsibilities": ["Develop", "Test"],
                "requirements": {
                    "minimum": ["Bachelor degree"],
                    "preferred": ["Master degree"]
                }
            }
            """;

        JobDetailsDTO result = JobMigrationConvert.parseJobDetails(jsonString);

        assertNotNull(result);
        assertEquals("Test overview", result.getOverview());
        assertEquals(2, result.getSkills().size());
        assertEquals(1, result.getBenefits().size());
        assertEquals(2, result.getResponsibilities().size());
        assertNotNull(result.getRequirements());
        assertEquals(1, result.getRequirements().getMinimum().size());
        assertEquals(1, result.getRequirements().getPreferred().size());
    }

    @Test
    void testParseJobDetailsWithEmptyJson() {
        JobDetailsDTO result = JobMigrationConvert.parseJobDetails("");
        assertNotNull(result);

        result = JobMigrationConvert.parseJobDetails(null);
        assertNotNull(result);
    }

    @Test
    void testEnumConversions() {
        // 测试职位状态转换
        assertEquals(Integer.valueOf(0), JobMigrationConvert.convertJobStatus("draft"));
        assertEquals(Integer.valueOf(1), JobMigrationConvert.convertJobStatus("active"));
        assertEquals(Integer.valueOf(-1), JobMigrationConvert.convertJobStatus("closed"));
        assertEquals(Integer.valueOf(2), JobMigrationConvert.convertJobStatus("other"));
        assertEquals(Integer.valueOf(0), JobMigrationConvert.convertJobStatus("unknown"));
        assertNull(JobMigrationConvert.convertJobStatus(null));

        // 测试职位类型转换
        assertEquals(Integer.valueOf(1), JobMigrationConvert.convertJobType("full_time"));
        assertEquals(Integer.valueOf(2), JobMigrationConvert.convertJobType("part_time"));
        assertEquals(Integer.valueOf(3), JobMigrationConvert.convertJobType("contract"));
        assertEquals(Integer.valueOf(1), JobMigrationConvert.convertJobType("unknown"));

        // 测试位置类型转换
        assertEquals(Integer.valueOf(1), JobMigrationConvert.convertLocationType("onsite"));
        assertEquals(Integer.valueOf(2), JobMigrationConvert.convertLocationType("remote"));
        assertEquals(Integer.valueOf(3), JobMigrationConvert.convertLocationType("hybrid"));
        assertEquals(Integer.valueOf(1), JobMigrationConvert.convertLocationType("unknown"));

        // 测试薪资类型转换
        assertEquals(Integer.valueOf(1), JobMigrationConvert.convertSalaryType("hourly"));
        assertEquals(Integer.valueOf(4), JobMigrationConvert.convertSalaryType("monthly"));
        assertEquals(Integer.valueOf(5), JobMigrationConvert.convertSalaryType("yearly"));
        assertEquals(Integer.valueOf(4), JobMigrationConvert.convertSalaryType("unknown"));
    }

    @Test
    void testTypeConversions() {
        // 测试 Integer 到 Long 转换
        assertEquals(Long.valueOf(123), JobMigrationConvert.convertIntegerToLong(123));
        assertNull(JobMigrationConvert.convertIntegerToLong(null));

        // 测试 Boolean 到 Integer 转换
        assertEquals(Integer.valueOf(1), JobMigrationConvert.convertBooleanToInteger(true));
        assertEquals(Integer.valueOf(0), JobMigrationConvert.convertBooleanToInteger(false));
        assertNull(JobMigrationConvert.convertBooleanToInteger(null));

        // 测试 Short 到 Integer 转换
        assertEquals(Integer.valueOf(123), JobMigrationConvert.convertShortToInteger((short) 123));
        assertNull(JobMigrationConvert.convertShortToInteger(null));

        // 测试 BigDecimal 到 Integer 转换
        assertEquals(Integer.valueOf(1234), JobMigrationConvert.convertBigDecimalToInteger(new BigDecimal("1234.56")));
        assertNull(JobMigrationConvert.convertBigDecimalToInteger(null));
    }

    @Test
    void testUuidToLongConversion() {
        UUID testUuid = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        Long result = JobMigrationConvert.convertUuidToLong(testUuid);
        
        assertNotNull(result);
        // UUID 转换为 Long 应该是一致的
        assertEquals(result, JobMigrationConvert.convertUuidToLong(testUuid));
        
        assertNull(JobMigrationConvert.convertUuidToLong(null));
    }

    @Test
    void testOffsetDateTimeToLocalDateTimeConversion() {
        OffsetDateTime offsetDateTime = OffsetDateTime.now();
        LocalDateTime result = JobMigrationConvert.convertOffsetDateTimeToLocalDateTime(offsetDateTime);
        
        assertNotNull(result);
        assertEquals(offsetDateTime.toLocalDateTime(), result);
        
        assertNull(JobMigrationConvert.convertOffsetDateTimeToLocalDateTime(null));
    }
}
