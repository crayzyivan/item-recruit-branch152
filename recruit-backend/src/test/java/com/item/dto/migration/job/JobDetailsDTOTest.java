package com.item.dto.migration.job;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JobDetailsDTO 单元测试
 * 
 * 验证 JSONB details 字段的序列化和反序列化功能。
 *
 * @author system
 * @since 2025-09-30
 */
class JobDetailsDTOTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void testJsonSerialization() throws Exception {
        // 创建测试数据
        JobDetailsDTO jobDetails = new JobDetailsDTO();
        jobDetails.setOverview("We are seeking a dedicated and enthusiastic Waiter to join our dynamic team.");
        jobDetails.setSkills(Arrays.asList(
            "Excellent communication skills",
            "Strong customer service orientation",
            "Ability to work in a fast-paced environment"
        ));
        jobDetails.setBenefits(Arrays.asList());
        jobDetails.setResponsibilities(Arrays.asList(
            "Greet and seat customers with a warm and welcoming demeanor",
            "Present menus and provide detailed information about food and beverage items"
        ));

        JobDetailsDTO.RequirementsDTO requirements = new JobDetailsDTO.RequirementsDTO();
        requirements.setMinimum(Arrays.asList(
            "High school diploma or equivalent",
            "Previous experience in a food service role"
        ));
        requirements.setPreferred(Arrays.asList(
            "Familiarity with POS systems",
            "Knowledge of food safety and sanitation regulations"
        ));
        jobDetails.setRequirements(requirements);

        // 序列化为 JSON
        String json = objectMapper.writeValueAsString(jobDetails);
        assertNotNull(json);
        assertTrue(json.contains("overview"));
        assertTrue(json.contains("skills"));
        assertTrue(json.contains("requirements"));

        // 反序列化回对象
        JobDetailsDTO deserializedJobDetails = objectMapper.readValue(json, JobDetailsDTO.class);
        assertNotNull(deserializedJobDetails);
        assertEquals(jobDetails.getOverview(), deserializedJobDetails.getOverview());
        assertEquals(jobDetails.getSkills().size(), deserializedJobDetails.getSkills().size());
        assertEquals(jobDetails.getRequirements().getMinimum().size(), 
                    deserializedJobDetails.getRequirements().getMinimum().size());
    }

    @Test
    void testJsonDeserialization() throws Exception {
        // 使用 JIRA 评论中的示例 JSON 数据
        String jsonString = """
            {
                "overview": "We are seeking a dedicated and enthusiastic Waiter to join our dynamic team. The ideal candidate will have a passion for providing exceptional customer service in a fast-paced restaurant environment.",
                "skills": [
                    "Excellent communication skills",
                    "Strong customer service orientation",
                    "Ability to work in a fast-paced environment",
                    "Team collaboration and interpersonal skills",
                    "Basic knowledge of food and beverage pairings",
                    "Attention to detail and accuracy",
                    "Problem-solving abilities"
                ],
                "benefits": [],
                "responsibilities": [
                    "Greet and seat customers with a warm and welcoming demeanor",
                    "Present menus and provide detailed information about food and beverage items",
                    "Take customer orders accurately and relay them to the kitchen staff",
                    "Serve food and drinks in a timely and professional manner",
                    "Monitor customer satisfaction and address any issues or complaints",
                    "Prepare and process customer payments efficiently",
                    "Collaborate with team members to ensure smooth restaurant operations"
                ],
                "requirements": {
                    "minimum": [
                        "High school diploma or equivalent",
                        "Previous experience in a food service role",
                        "Ability to work evenings, weekends, and holidays",
                        "Basic math skills for handling payments",
                        "Excellent hygiene and grooming standards"
                    ],
                    "preferred": [
                        "Familiarity with POS systems",
                        "Knowledge of food safety and sanitation regulations",
                        "Fluency in additional languages",
                        "Completed food safety certification",
                        "Minimum 2 years Experience in fine dining establishments"
                    ]
                }
            }
            """;

        // 反序列化
        JobDetailsDTO jobDetails = objectMapper.readValue(jsonString, JobDetailsDTO.class);

        // 验证
        assertNotNull(jobDetails);
        assertNotNull(jobDetails.getOverview());
        assertTrue(jobDetails.getOverview().contains("Waiter"));
        
        assertNotNull(jobDetails.getSkills());
        assertEquals(7, jobDetails.getSkills().size());
        assertTrue(jobDetails.getSkills().contains("Excellent communication skills"));
        
        assertNotNull(jobDetails.getBenefits());
        assertEquals(0, jobDetails.getBenefits().size());
        
        assertNotNull(jobDetails.getResponsibilities());
        assertEquals(7, jobDetails.getResponsibilities().size());
        
        assertNotNull(jobDetails.getRequirements());
        assertNotNull(jobDetails.getRequirements().getMinimum());
        assertEquals(5, jobDetails.getRequirements().getMinimum().size());
        
        assertNotNull(jobDetails.getRequirements().getPreferred());
        assertEquals(5, jobDetails.getRequirements().getPreferred().size());
    }
}
