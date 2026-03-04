package com.item.controller;

import com.item.convert.CandidateJobConvert;
import com.item.dto.AnswerQuestion5sTestConfirmRequestDTO;
import com.item.dto.AnswerQuestion5sTestStatusRequestDTO;
import com.item.dto.job.CandidateInterviewTimeRequestVO;
import com.item.dto.job.CandidateJobApplyDto;
import com.item.dto.job.CandidateJobApplyRequestVO;
import com.item.framework.annotation.Auth;
import com.item.framework.constant.RoleType;
import com.item.service.CandidateJobDomainService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 候选人
 *
 * @author lh
 * @since 2025-07-07
 */
@RestController
@RequestMapping("/candidate")
@RequiredArgsConstructor
public class CandidateJobController {

    private final CandidateJobDomainService candidateJobDomainService;
    private final CandidateJobConvert candidateJobConvert;

    @Auth(roleType = RoleType.CANDIDATE)
    @PostMapping("/application/job")
    public Boolean applyJob(@Validated @RequestBody CandidateJobApplyRequestVO vo) {
        CandidateJobApplyDto dto = candidateJobConvert.toCandidateJobDTO(vo);
        return candidateJobDomainService.applyJob(dto);
    }

    @Auth(roleType = RoleType.CANDIDATE)
    @GetMapping("/check-application-job/{jobId}")
    public Boolean checkApplicationJob(@PathVariable Long jobId) {
        return candidateJobDomainService.checkApplicationJob(jobId);
    }

    @Auth(roleType = RoleType.CANDIDATE)
    @PostMapping("/validate-interview-time")
    public Boolean validateInterviewTime(@Validated @RequestBody CandidateInterviewTimeRequestVO vo) {
        return candidateJobDomainService.validateInterviewTime(
                vo.getCandidateId(),
                vo.getPreferredInterviewStartTime(),
                vo.getPreferredInterviewEndTime()
        );
    }

    /**
     * 是否已经作答5s题目
     *
     * @param requestDTO
     * @return true 已经作答 false未作答
     */
    @PostMapping(value = "/answer/question-5s/status")
    public Boolean isAnswerQuestion5sTest(@RequestBody @Validated AnswerQuestion5sTestStatusRequestDTO requestDTO){
        return candidateJobDomainService.isAnswerQuestion5sTest(requestDTO);
    }

    /**
     * 作答5S题目
     *
     * @param requestDTO
     * @return true 作答成功 false 作答失败
     */
    @PostMapping(value = "/answer/question-5s/confirm")
    public Boolean answerQuestion5sTestConfirm(@RequestBody @Validated AnswerQuestion5sTestConfirmRequestDTO requestDTO){
        return candidateJobDomainService.answerQuestion5sTestConfirm(requestDTO);
    }


//    /**
//     * 更新应聘状态
//     *
//     * @param dto 状态更新请求
//     * @return 更新结果
//     */
//    @Deprecated
//    @PutMapping("/status")
//    public Boolean updateStatus(@RequestBody @Validated CandidateJobUpdateStatusDto dto) {
//        return candidateJobDomainService.updateStatus(dto);
//    }
}