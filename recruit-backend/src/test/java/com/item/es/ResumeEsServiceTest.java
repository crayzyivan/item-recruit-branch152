package com.item.es;

import com.item.dto.job.IntelligenceScoreRuleDTO;
import com.item.util.LambdaUtil;
import com.item.vo.ai.JobMatchResultVO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * TODO：功能描述
 *
 * @author liyunlong
 * @version 1.0
 * @since 2026-01-12  10:08
 */
@Slf4j
@SpringBootTest
public class ResumeEsServiceTest {

    @Resource
    private ResumeEsService resumeEsService;

    @Test
    public void updateMatchEsFieldValue(){
        String documentId="788";
        Map<String, Object> fieldsValues=new HashMap<>();
        List<IntelligenceScoreRuleDTO> scoreRuleDTOS=new ArrayList<>();
        IntelligenceScoreRuleDTO scoreRuleDTO=new IntelligenceScoreRuleDTO();
        scoreRuleDTO.setStageCode("SCREENED");
        scoreRuleDTO.setStageOrder(1);
        scoreRuleDTO.setSubStageCode("ASSESSMENT_SCORE");
        scoreRuleDTO.setSubStageOrder(1);
        scoreRuleDTO.setWeight(100);
        scoreRuleDTO.setThreshold(80);
        scoreRuleDTOS.add(scoreRuleDTO);
        fieldsValues.put(LambdaUtil.getFieldName(JobMatchResultVO::getScoreRules), scoreRuleDTOS);
        resumeEsService.updateMatchEsFieldValue(documentId, fieldsValues);

    }

    @Test
    public void test(){
        String documentId="788";
        JobMatchResultVO matchResultById = resumeEsService.getMatchResultById(documentId);
        log.info("matchResultById:{}",matchResultById);
    }


}