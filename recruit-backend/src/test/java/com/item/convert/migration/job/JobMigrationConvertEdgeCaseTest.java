package com.item.convert.migration.job;

import com.item.dto.migration.job.JobDetailsDTO;
import com.item.entity.JobEntity;
import com.item.entity.migration.job.PgJobEntity;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Job 迁移数据转换器边界情况测试
 * 
 * 专门测试数据转换器在各种边界情况和异常情况下的行为，
 * 确保转换器的健壮性和容错性。
 * 使用 @Transactional 和 @Rollback 确保测试不污染数据库。
 *
 * @author system
 * @since 2025-09-30
 */
@SpringBootTest
@Transactional
@Rollback
@ActiveProfiles("test")
class JobMigrationConvertEdgeCaseTest {

    @Autowired
    private JobMigrationConvert jobMigrationConvert;

    private PgJobEntity minimalPgJob;
    private PgJobEntity maximalPgJob;

    @BeforeEach
    void setUp() {
        // 创建最小化的 PostgreSQL Job 实体（只有必需字段）
        minimalPgJob = new PgJobEntity();
        minimalPgJob.setId(1);
        minimalPgJob.setTitle("Minimal Job");

        // 创建最大化的 PostgreSQL Job 实体（所有字段都有值）
        maximalPgJob = new PgJobEntity();
        maximalPgJob.setId(Integer.MAX_VALUE);
        maximalPgJob.setTitle("Maximal Job with Very Long Title That Might Exceed Normal Limits");
        maximalPgJob.setSlug("maximal-job-with-very-long-slug-that-might-exceed-normal-limits");
        maximalPgJob.setCompanyId(Integer.MAX_VALUE);
        maximalPgJob.setStatus("active");
        maximalPgJob.setLocationId(Integer.MAX_VALUE);
        maximalPgJob.setNumberOfOpenings(Integer.MAX_VALUE);
        maximalPgJob.setType("full_time");
        maximalPgJob.setLocationType("hybrid");
        maximalPgJob.setSalaryType("yearly");
        maximalPgJob.setCurrencyId(Short.MAX_VALUE);
        maximalPgJob.setMinSalary(new BigDecimal("999999.99"));
        maximalPgJob.setMaxSalary(new BigDecimal("9999999.99"));
        maximalPgJob.setHotlist(true);
        maximalPgJob.setCreatedOn(OffsetDateTime.now());
        maximalPgJob.setUpdatedOn(OffsetDateTime.now());
        maximalPgJob.setCreatedBy(UUID.randomUUID().toString());
        maximalPgJob.setUpdatedBy(UUID.randomUUID().toString());
        maximalPgJob.setCategoryIds("{1}");
        maximalPgJob.setDetails("{\"overview\":\"Very detailed job overview with lots of information\",\"skills\":[\"Java\",\"Spring Boot\",\"MySQL\",\"Redis\",\"Elasticsearch\",\"Docker\",\"Kubernetes\",\"AWS\",\"Microservices\",\"REST API\"]}");
        maximalPgJob.setCustomQuestions("[\"Are you available to work from home at times?\", \"Are you allowed to work in the US?\"]");
        maximalPgJob.setInterviewId("interview-123-456-789");
    }

    @Test
    void testConvertMinimalPgJobToJobEntity() {
        // 测试最小化数据的转换
        JobEntity result = jobMigrationConvert.convertToJobEntity(minimalPgJob);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Minimal Job", result.getTitle());
        
        // 验证默认值
        assertEquals(Integer.valueOf(1), result.getNeedListed());
        assertEquals(Integer.valueOf(0), result.getAyrshareStatus());
        
        // 验证 null 字段的处理
        assertNull(result.getCompanyCode());
    }

    @Test
    void testConvertMaximalPgJobToJobEntity() {
        // 测试最大化数据的转换
        JobEntity result = jobMigrationConvert.convertToJobEntity(maximalPgJob);

        assertNotNull(result);
        assertEquals((long) Integer.MAX_VALUE, result.getId());
        assertEquals("Maximal Job with Very Long Title That Might Exceed Normal Limits", result.getTitle());
        assertEquals((long) Integer.MAX_VALUE, result.getCustomerId());
        assertEquals((long) Integer.MAX_VALUE, result.getMasterAccountId());
        assertEquals(Integer.MAX_VALUE, result.getLocationId());
        assertEquals(Integer.MAX_VALUE, result.getNumberOpenings());
        
        // 验证大数值的转换
        assertEquals(Integer.valueOf(999999), result.getMinSalary());
        assertEquals(Integer.valueOf(9999999), result.getMaxSalary());
        assertEquals(Integer.valueOf(Short.MAX_VALUE), result.getCurrency());
        
        // 验证布尔值转换
        assertEquals(Integer.valueOf(1), result.getHotList());
        
        // 验证分类ID转换（取第一个）
        assertEquals(Integer.valueOf(1), result.getCategoryId());
        
        // 验证枚举转换
        assertEquals(Integer.valueOf(1), result.getJobStatus()); // active
        assertEquals(Integer.valueOf(1), result.getTypeId()); // full_time
        assertEquals(Integer.valueOf(3), result.getModeId()); // hybrid
        assertEquals(Integer.valueOf(5), result.getSalaryType()); // yearly
    }

    @Test
    void testConvertWithNullValues() {
        PgJobEntity nullJob = new PgJobEntity();
        nullJob.setId(1);
        nullJob.setTitle("Null Test Job");
        // 其他字段保持 null

        JobEntity result = jobMigrationConvert.convertToJobEntity(nullJob);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Null Test Job", result.getTitle());
        
        // 验证 null 值的处理
        assertNull(result.getCustomerId());
        assertNull(result.getLocationId());
        assertNull(result.getNumberOpenings());
        assertNull(result.getMinSalary());
        assertNull(result.getMaxSalary());
        assertNull(result.getCurrency());
        assertNull(result.getCategoryId());
        assertNull(result.getCreateTime());
        assertNull(result.getUpdateTime());
    }

//    @Test
//    void testConvertWithEmptyCollections() {
//        PgJobEntity emptyJob = new PgJobEntity();
//        emptyJob.setId(1);
//        emptyJob.setTitle("Empty Collections Job");
//        emptyJob.setCategoryIds(Collections.emptyList()); // 空列表
//
//        JobEntity result = jobMigrationConvert.convertToJobEntity(emptyJob);
//
//        assertNotNull(result);
//        assertNull(result.getCategoryId()); // 空列表应该返回 null
//    }

//    @Test
//    void testConvertToJobEsEntityWithMinimalData() {
//        JobDetailsDTO emptyDetails = new JobDetailsDTO();
//
//        JobEsEntity result = jobMigrationConvert.convertToJobEsEntity(minimalPgJob, emptyDetails);
//
//        assertNotNull(result);
//        assertEquals(1L, result.getId());
//        assertEquals("Minimal Job", result.getTitle());
//
//        // 验证空的详情字段
//        assertNull(result.getJobDetail());
//        assertNull(result.getSkills());
//        assertNull(result.getBenefits());
//        assertNull(result.getMainDuty());
//        assertNull(result.getMinimumJobRequirement());
//        assertNull(result.getPreferredJobRequirement());
//    }

//    @Test
//    void testConvertToJobEsEntityWithMaximalData() {
//        JobDetailsDTO maximalDetails = new JobDetailsDTO();
//        maximalDetails.setOverview("Very detailed job overview with lots of information and requirements");
//        maximalDetails.setSkills(Arrays.asList("Java", "Spring Boot", "MySQL", "Redis", "Elasticsearch", "Docker", "Kubernetes", "AWS", "Microservices", "REST API"));
//        maximalDetails.setBenefits(Arrays.asList("Health Insurance", "Dental Insurance", "Vision Insurance", "401k", "Flexible Hours", "Remote Work", "Paid Time Off", "Professional Development"));
//        maximalDetails.setResponsibilities(Arrays.asList(
//            "Develop and maintain Java applications using Spring Boot framework",
//            "Design and implement RESTful APIs for microservices architecture",
//            "Collaborate with cross-functional teams to deliver high-quality software",
//            "Write clean, maintainable, and well-documented code",
//            "Participate in code reviews and provide constructive feedback",
//            "Troubleshoot and debug production issues",
//            "Stay up-to-date with latest technologies and best practices"
//        ));
//
//        JobDetailsDTO.RequirementsDTO requirements = new JobDetailsDTO.RequirementsDTO();
//        requirements.setMinimum(Arrays.asList(
//            "Bachelor's degree in Computer Science or related field",
//            "5+ years of Java development experience",
//            "Strong knowledge of Spring Boot and Spring Framework",
//            "Experience with relational databases (MySQL, PostgreSQL)",
//            "Understanding of RESTful API design principles"
//        ));
//        requirements.setPreferred(Arrays.asList(
//            "Master's degree in Computer Science",
//            "Experience with microservices architecture",
//            "Knowledge of cloud platforms (AWS, Azure, GCP)",
//            "Experience with containerization (Docker, Kubernetes)",
//            "Familiarity with NoSQL databases (Redis, MongoDB)"
//        ));
//        maximalDetails.setRequirements(requirements);
//
//        JobEsEntity result = jobMigrationConvert.convertToJobEsEntity(maximalPgJob, maximalDetails);
//
//        assertNotNull(result);
//        assertEquals((long) Integer.MAX_VALUE, result.getId());
//        assertEquals("Maximal Job with Very Long Title That Might Exceed Normal Limits", result.getTitle());
//
//        // 验证详情字段
//        assertEquals("Very detailed job overview with lots of information and requirements", result.getJobDetail());
//        assertEquals(10, result.getSkills().size());
//        assertEquals(8, result.getBenefits().size());
//        assertEquals(7, result.getMainDuty().size());
//        assertEquals(5, result.getMinimumJobRequirement().size());
//        assertEquals(5, result.getPreferredJobRequirement().size());
//    }

    @Test
    void testParseJobDetailsWithMalformedJson() {
        // 测试格式错误的 JSON
        String malformedJson = "{\"overview\":\"Test\",\"skills\":[\"Java\",}"; // 缺少闭合括号

        JobDetailsDTO result = JobMigrationConvert.parseJobDetails(malformedJson);

        assertNotNull(result);
        // 应该返回空的 DTO 而不是抛出异常
        assertNull(result.getOverview());
        assertNull(result.getSkills());
    }

    @Test
    void testParseJobDetailsWithVeryLargeJson() {
        // 测试非常大的 JSON 数据
        StringBuilder largeJsonBuilder = new StringBuilder();
        largeJsonBuilder.append("{\"overview\":\"");
        for (int i = 0; i < 10000; i++) {
            largeJsonBuilder.append("This is a very long overview text. ");
        }
        largeJsonBuilder.append("\",\"skills\":[");
        for (int i = 0; i < 1000; i++) {
            largeJsonBuilder.append("\"Skill").append(i).append("\"");
            if (i < 999) largeJsonBuilder.append(",");
        }
        largeJsonBuilder.append("]}");

        String largeJson = largeJsonBuilder.toString();

        JobDetailsDTO result = JobMigrationConvert.parseJobDetails(largeJson);

        assertNotNull(result);
        assertNotNull(result.getOverview());
        assertNotNull(result.getSkills());
        assertEquals(1000, result.getSkills().size());
    }

    @Test
    void testEnumConversionsWithUnknownValues() {
        // 测试未知的枚举值
        assertEquals(Integer.valueOf(0), JobMigrationConvert.convertJobStatus("unknown_status"));
        assertEquals(Integer.valueOf(1), JobMigrationConvert.convertJobType("unknown_type"));
        assertEquals(Integer.valueOf(1), JobMigrationConvert.convertLocationType("unknown_location"));
        assertEquals(Integer.valueOf(4), JobMigrationConvert.convertSalaryType("unknown_salary"));
    }

    @Test
    void testTypeConversionsWithExtremeValues() {
        // 测试极值转换
        assertEquals(Long.valueOf(Integer.MAX_VALUE), JobMigrationConvert.convertIntegerToLong(Integer.MAX_VALUE));
        assertEquals(Long.valueOf(Integer.MIN_VALUE), JobMigrationConvert.convertIntegerToLong(Integer.MIN_VALUE));
        
        assertEquals(Integer.valueOf(Short.MAX_VALUE), JobMigrationConvert.convertShortToInteger(Short.MAX_VALUE));
        assertEquals(Integer.valueOf(Short.MIN_VALUE), JobMigrationConvert.convertShortToInteger(Short.MIN_VALUE));
        
        // 测试 BigDecimal 的极值转换
        BigDecimal largeBigDecimal = new BigDecimal("999999999999.99");
        Integer result = JobMigrationConvert.convertBigDecimalToInteger(largeBigDecimal);
        assertNotNull(result);
        assertEquals(Integer.valueOf(Integer.MAX_VALUE), result);
    }

    @Test
    void testUuidConsistency() {
        // 测试 UUID 转换的一致性
        UUID testUuid = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        
        Long result1 = JobMigrationConvert.convertUuidToLong(testUuid);
        Long result2 = JobMigrationConvert.convertUuidToLong(testUuid);
        
        assertNotNull(result1);
        assertNotNull(result2);
        assertEquals(result1, result2, "相同的 UUID 应该转换为相同的 Long 值");
    }

}
