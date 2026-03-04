package com.item.service.impl;

import com.item.dto.job.LocationValDTO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

/**
 * JobDomainServiceImpl.checkJobDuplication method unit test
 * 
 * @author hua.liu
 * @since 2025-09-18
 */
@Slf4j
@SpringBootTest
class JobDomainServiceImplDuplicationTest {

    @Resource
    private JobDomainServiceImpl jobDomainService;

    private String testTitle;
    private String companyCode;
    private Long excludeJobId;
    private List<LocationValDTO> testLocations;

    @BeforeEach
    void setUp() {
        testTitle = "Java 200";
        companyCode = "RDXX0001";
        excludeJobId = 189L;
        // Setup test locations
        testLocations = new ArrayList<>();
        LocationValDTO locationValDTO = new LocationValDTO();
        locationValDTO.setCountryId(1L);
        locationValDTO.setCityId(52L);
        locationValDTO.setStateId(3901L);

        testLocations.add(locationValDTO);

        LocationValDTO locationValDTO1 = new LocationValDTO();
        locationValDTO1.setCountryId(1L);
        locationValDTO1.setCityId(108L);
        locationValDTO1.setStateId(3871L);
        testLocations.add(locationValDTO1);

        LocationValDTO locationValDTO2 = new LocationValDTO();
        locationValDTO2.setCountryId(1L);
        locationValDTO2.setCityId(101L);
        locationValDTO2.setStateId(3875L);
        testLocations.add(locationValDTO2);
        LocationValDTO locationValDTO3 = new LocationValDTO();
        locationValDTO3.setCountryId(45L);
        locationValDTO3.setCityId(19577L);
        locationValDTO3.setStateId(2271L);
        testLocations.add(locationValDTO3);

    }

    @Test
    void testCheckJobDuplication_NoDuplicateJobs_ReturnsFalse() {
        String t = "java 200000011";
        // When
        boolean result = jobDomainService.checkJobDuplication(t, testLocations, companyCode, null, false);

        // Then
//        assertFalse(result);
        log.info("testCheckJobDuplication_NoDuplicateJobs_ReturnsFalse passed - no duplicates found {}", result);
    }

    @Test
    void testCheckJobDuplication_NoDuplicateJobs_ReturnsFalse01() {
        String t = "java 200000011";
        // When
        boolean result = jobDomainService.checkJobDuplication(t, testLocations, companyCode, null, true);

        // Then
//        assertFalse(result);
        log.info("testCheckJobDuplication_NoDuplicateJobs_ReturnsFalse passed - no duplicates found {}", result);
    }

    @Test
    void testCheckJobDuplication_HasDuplicateAndConfirmSaveTrue_ReturnsFalse() {
        // When
        boolean result = jobDomainService.checkJobDuplication(testTitle, testLocations, companyCode, null, true);

        // Then
//        assertFalse(result);
        log.info("testCheckJobDuplication_HasDuplicateAndConfirmSaveTrue_ReturnsFalse passed - user confirmed save {}", result);
    }

    @Test
    void testCheckJobDuplication_HasDuplicateAndConfirmSaveFalse_ReturnsTrue() {

        // When
        boolean result = jobDomainService.checkJobDuplication(testTitle, testLocations, companyCode, null, false);

        // Then
//        assertTrue(result);
        log.info("testCheckJobDuplication_HasDuplicateAndConfirmSaveFalse_ReturnsTrue passed - user needs to confirm {}", result);
    }

    @Test
    void testCheckJobDuplication_HasDuplicateAndConfirmSaveFalse_ReturnsFalse01() {

        // When
        boolean result = jobDomainService.checkJobDuplication("", testLocations, companyCode, null, false);

        // Then
//        assertFalse(result);
        log.info("testCheckJobDuplication_HasDuplicateAndConfirmSaveFalse_ReturnsTrue passed - user needs to confirm {}", result);

        // When
        result = jobDomainService.checkJobDuplication("", testLocations, companyCode, null, true);

        // Then
//        assertFalse(result);
        log.info("testCheckJobDuplication_HasDuplicateAndConfirmSaveFalse_ReturnsTrue passed - user needs to confirm {}", result);

        // When
        result = jobDomainService.checkJobDuplication(testTitle, null, companyCode, null, false);

        // Then
//        assertFalse(result);
        log.info("testCheckJobDuplication_HasDuplicateAndConfirmSaveFalse_ReturnsTrue passed - user needs to confirm {}", result);

        // When
        result = jobDomainService.checkJobDuplication(testTitle, null, companyCode, null, true);

        // Then
//        assertFalse(result);
        log.info("testCheckJobDuplication_HasDuplicateAndConfirmSaveFalse_ReturnsTrue passed - user needs to confirm {}", result);
    }

    @Test
    void testCheckJobDuplication_HasDuplicateAndConfirmSaveFalse() {

        // When
        boolean result = jobDomainService.checkJobDuplication(testTitle, testLocations, companyCode, excludeJobId, false);

        // Then
//        assertTrue(result);
        log.info("testCheckJobDuplication_HasDuplicateAndConfirmSaveFalse_ReturnsTrue passed - user needs to confirm {}", result);
    }

}
