package com.item;

import com.item.dto.PersonalUserRegisterReqDTO;
import com.item.dto.PersonalUserRegisterResDTO;
import com.item.framework.error.BusinessException;
import com.item.service.PersonalService;
import lombok.extern.slf4j.Slf4j;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * PersonalService integration test
 * Tests personal user registration functionality
 * 
 * @author lh
 */
@SpringBootTest
@Slf4j
class PersonalServiceTest {
    
    @Autowired
    private PersonalService personalService;
    
    /**
     * Test registerPersonalUser method
     * Verifies personal user registration creates both IAM account and local candidate record
     */
    @Test
    void testRegisterPersonalUser() {
        // Prepare test data with unique identifiers to avoid duplicate email
        String timestamp = String.valueOf(System.currentTimeMillis());
        
        PersonalUserRegisterReqDTO request = new PersonalUserRegisterReqDTO();
        request.setUserName("testuser" + timestamp);
//        request.setUserName("testuser1760605449718");
        request.setRawPassword("Test@123456");
        request.setFirstName("Test");
        request.setLastName("User");
        request.setEmail("testuser" + timestamp + "@example.com");
//        request.setEmail("test1760605449718@example.com");
        request.setContactNumber("1234567890");
        
        // Execute registration
        PersonalUserRegisterResDTO result = null;

        try {
            result = personalService.registerPersonalUser(request);
        } catch (BusinessException e) {
            log.warn("registerPersonalUser BusinessException ", e);
            throw e;
        } catch (Exception e) {
            log.error("registerPersonalUser BusinessException ", e);
            throw e;
        }
        
        // Verify result not null
        assertNotNull(result, "Registration result should not be null");
        
        // Verify candidateId
        assertNotNull(result.getCandidateId(), "Candidate ID should not be null");
        assertTrue(result.getCandidateId() > 0, "Candidate ID should be positive");
        
        // Verify iamId
        assertNotNull(result.getIamId(), "IAM ID should not be null");
        
        // Log result for verification
        log.info("Registration test passed - candidateId: {}, iamId: {}", 
            result.getCandidateId(), result.getIamId());
    }
}

