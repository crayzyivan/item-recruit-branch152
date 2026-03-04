package com.item.controller;

import com.item.convert.AiCallbackConvert;
import com.item.framework.constant.GlobalStatusCode;
import com.item.framework.error.BusinessException;
import com.item.service.AIService;
import com.item.service.CandidateJobService;
import com.item.vo.ApplicationUserInfoVO;
import com.item.vo.ai.AiCallbackVO;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/interview/")
@RequiredArgsConstructor
public class AICallbackController {

    private final AIService aiService;
    private final CandidateJobService candidateJobService;

    @GetMapping("/check-info")
    public void checkInfo(@RequestParam("candidateEmail") String candidateEmail,
                         @RequestParam("interviewId") String interviewId,
                         @RequestParam(value = "application_id", required = false) Long applicationId,
                         @RequestParam(value = "candidate_name", required = false) String candidateName){
        if (!aiService.checkInfo(candidateEmail, interviewId, applicationId, candidateName)){
            throw new BusinessException(GlobalStatusCode.FAIL,"No interview invitations.");
        }
    }

    @PostMapping("/callback")
    public void callback(@RequestBody @Validated AiCallbackVO aiCallbackVO){
        if (!aiService.aiInterviewCallback(AiCallbackConvert.INSTANCE.toAiCallbackDTO(aiCallbackVO))){
            throw new BusinessException(GlobalStatusCode.FAIL,"failed.");
        }
    }

    /**
     * 根据申请ID获取用户信息（租户ID、用户ID、用户姓名）
     * 用于AI服务获取申请相关的用户上下文信息
     *
     * @param applicationId 申请ID（r_candidate_job表的主键id）
     * @return 用户信息，包含租户ID、用户ID和用户姓名
     */
    @GetMapping("/user-info/{application_id}")
    public ApplicationUserInfoVO getUserInfoByApplicationId(@PathVariable("application_id") Long applicationId) {
        return candidateJobService.getUserInfoByApplicationId(applicationId);
    }
}
