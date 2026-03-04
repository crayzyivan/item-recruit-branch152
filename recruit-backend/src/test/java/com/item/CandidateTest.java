package com.item;

import com.item.convert.CandidateConverter;
import com.item.dto.CandidateDTO;
import com.item.entity.CandidateEsEntity;
import com.item.es.ResumeEsService;
import com.item.service.CandidateService;
import com.item.vo.CandidateProfileVO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;

/**
 * 候选人测试类
 *
 * @author liyunlong
 * @version 1.0
 * @since 2025-09-09  13:23
 */
@SpringBootTest
@Slf4j
public class CandidateTest {

    @Resource
    private CandidateService candidateService;


    @Test
    public void testGetCandidateDetails(){
        CandidateProfileVO candidateDetails = candidateService.getCandidateDetailsByCandidateId(132L);
        log.info("profileVO: {}", candidateDetails);
    }

    @Test
    public void uploadResumeAndAddCandidate() throws Exception {
        CandidateDTO candidateDTO =new CandidateDTO();
        candidateDTO.setId(163L);
        //candidateDTO.setResumeUrl("https://unis-stage-data.s3.us-west-2.amazonaws.com/recruit/8873f10a-3f94-409a-af27-5141ebed0bdc__2186d286-84b6-45a4-91ca-da5f6fbd3c5f_3.pdf?x-amz-checksum-mode=ENABLED&response-content-disposition=inline%3B%20filename%3D%22%252F8873f10a-3f94-409a-af27-5141ebed0bdc__2186d286-84b6-45a4-91ca-da5f6fbd3c5f_3.pdf%22&x-id=GetObject&host=unis-stage-data.s3.us-west-2.amazonaws.com&response-content-type=application%2Fpdf&X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Date=20251015T031558Z&X-Amz-SignedHeaders=host&X-Amz-Expires=86399&X-Amz-Credential=AKIA33NYZAMIR3HM5X74%2F20251015%2Fus-west-2%2Fs3%2Faws4_request&X-Amz-Signature=93e44645d0da45dae90a53f001ead2c40aee3301456427f95da8b35d2b9eef9d");
        candidateDTO.setEducationList(new ArrayList<>());
        candidateDTO.setEmploymentList(new ArrayList<>());


        Boolean editFlag=true;
        candidateService.uploadResumeAndAddCandidate(candidateDTO,editFlag);
    }


}