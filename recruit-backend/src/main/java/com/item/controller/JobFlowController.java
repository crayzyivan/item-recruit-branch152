package com.item.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.google.common.base.Strings;
import com.item.convert.AiVettedResultConverter;
import com.item.convert.CandidateJobConvert;
import com.item.convert.CandidateJobCountConvert;
import com.item.dto.AiVettedResultDTO;
import com.item.dto.CandidateShareVO;
import com.item.dto.ReadyPassDto;
import com.item.dto.RejectCandidateDto;
import com.item.dto.ReviewPassDto;
import com.item.dto.VettedPassDto;
import com.item.dto.ai.ResumeAIMatchDTO;
import com.item.es.ResumeEsService;
import com.item.framework.annotation.Auth;
import com.item.framework.constant.CandidateResponseCode;
import com.item.framework.constant.DataSourceEnum;
import com.item.framework.constant.DictionaryEnum;
import com.item.framework.constant.RoleType;
import com.item.framework.error.BusinessException;
import com.item.framework.http.Pager;
import com.item.service.AIService;
import com.item.service.CandidateJobDomainService;
import com.item.service.DictionaryService;
import com.item.service.RecommendationCandidateJobDomainService;
import com.item.vo.AiVettedQueryVO;
import com.item.vo.AiVettedResultVO;
import com.item.vo.ApplicationQueryVO;
import com.item.vo.ApplicationVO;
import com.item.vo.CandidateDetailsVO;
import com.item.vo.CandidateJobCountVO;
import com.item.vo.CandidateJobQueryVO;
import com.item.vo.CandidateJobRecordVO;
import com.item.vo.CandidateProcessVO;
import com.item.vo.DeniedListVO;
import com.item.vo.InterviewReportVO;
import com.item.vo.ManualReviewQueryVO;
import com.item.vo.ManualReviewVO;
import com.item.vo.PendingReviewQueryVO;
import com.item.vo.PendingReviewVO;
import com.item.vo.ReadyListVO;
import com.item.vo.ReadyQueryVO;
import com.item.vo.RecommendJobEmailRequestDTO;
import com.item.vo.ResumeAIscreenVO;
import com.item.vo.ai.JobMatchResultVO;
import com.item.vo.ai.WrittenDetailVO;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 岗位流程
 * </p>
 *
 * @author liuyabin on 2025/7/21
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/jobFlow")
@RequiredArgsConstructor
public class JobFlowController {
    private final CandidateJobDomainService candidateJobDomainService;
    private final DictionaryService dictionaryService;
    private final ResumeEsService resumeEsService;
    private final AIService aiService;
    private final RecommendationCandidateJobDomainService recommendationDomainService;

    @Auth(roleType = RoleType.SUB_USER)
    @GetMapping("/screenResult")
    public Pager<ResumeAIscreenVO> resumeScreen(@RequestParam(value = "pageIndex", required = false, defaultValue =
                                                              "1") int pageIndex,
                                                  @RequestParam(value = "pageSize", required = false, defaultValue = "10") int pageSize,
                                                  @RequestParam(value = "jobId") Long jobId,
                                                @RequestParam(value = "score", required = false) Integer score){
        ResumeAIMatchDTO resumeAIMatchDTO = ResumeAIMatchDTO.builder().jobId(jobId).score(score).build();
        IPage<ResumeAIMatchDTO> page= candidateJobDomainService.resumeAIMatch(pageIndex,pageSize, resumeAIMatchDTO);
        return Pager.build(page, CandidateJobConvert.INSTANCE::dtoListToVOList);
    }

    /**
     * 根据关联关系id查找简历筛选结果
     * @param id
     * @return
     */
    @Auth(roleType = RoleType.SUB_USER)
    @GetMapping("/screen-result-by-id/{id}")
    public JobMatchResultVO resumeScreenResult(@PathVariable(value = "id") String id) {
        if (Strings.isNullOrEmpty(id)) {
            return null;
        }
        JobMatchResultVO matchResultById = resumeEsService.getMatchResultById(id);
        Map<Long, String> dictMapping = dictionaryService.listDictMapping(List.of(DictionaryEnum.CHEATING_ANALYSIS_COMPLETED.getName()));
        matchResultById.convertJobSalaryTypeNameByLanguage(dictMapping);
        return matchResultById;
    }

    @Auth(roleType = RoleType.SUB_USER)
    @GetMapping("/send/Interview-mail/{candidateJobId}")
    public String interviewMail(@PathVariable(value = "candidateJobId") Long candidateJobId){
        return candidateJobDomainService.createAiInterview(candidateJobId);
    }


    /**
     * ai面试结果列表
     * @param pageIndex
     * @param pageSize
     * @param jobId
     * @return
     */
    @Auth(roleType = RoleType.SUB_USER)
    @GetMapping("/ai-vetted-list")
    public Pager<AiVettedResultVO> selectAiVettedPageList(@RequestParam(value = "pageIndex", required = false, defaultValue = "1") int pageIndex,
                                                          @RequestParam(value = "pageSize", required = false, defaultValue = "10") int pageSize,
                                                          @RequestParam(value = "jobId") Long jobId,
                                                          @RequestParam(value = "score", required = false) Integer score){
        AiVettedQueryVO aiVettedQueryVO = AiVettedQueryVO.builder().pageIndex(pageIndex).pageSize(pageSize).jobId(jobId).score(score).build();
        IPage<AiVettedResultDTO> page =candidateJobDomainService.selectAiVettedPageList(aiVettedQueryVO);
        return Pager.build(page, AiVettedResultConverter.INSTANCE::toDtoList);
    }

    @Auth(roleType = RoleType.SUB_USER)
    @GetMapping("/candidate-number/{jobId}")
    public CandidateJobCountVO candidateNumber(@PathVariable(value = "jobId") Long jobId){
        return CandidateJobCountConvert.INSTANCE.toVo(candidateJobDomainService.getCandidateNumber(jobId));
    }

    /**
     * 人工审核列表
     * @param pageIndex
     * @param pageSize
     * @param jobId
     * @return
     */
    @Auth(roleType = RoleType.SUB_USER)
    @GetMapping("/pending-review-list")
    public Pager<PendingReviewVO> selectPendingReviewPageList(@RequestParam(value = "pageIndex", required = false, defaultValue = "1") int pageIndex,
                                                              @RequestParam(value = "pageSize", required = false, defaultValue = "10") int pageSize,
                                                              @RequestParam(value = "jobId") Long jobId,
                                                              @RequestParam(value = "score", required = false) Integer score){
        PendingReviewQueryVO pendingReviewQueryVO = PendingReviewQueryVO.builder().pageIndex(pageIndex).pageSize(pageSize).jobId(jobId).score(score).build();
        Pager<PendingReviewVO> page =candidateJobDomainService.selectPendingReviewPageList(pendingReviewQueryVO);
        return page;
    }

    /**
     * 自动流程-人工审核列表
     * @param pageIndex
     * @param pageSize
     * @param jobId
     * @return
     */
    @Auth(roleType = RoleType.SUB_USER)
    @GetMapping("/manual-review-list")
    public Pager<ManualReviewVO> selectManualReviewPageList(@RequestParam(value = "pageIndex", required = false, defaultValue = "1") int pageIndex,
                                                            @RequestParam(value = "pageSize", required = false, defaultValue = "10") int pageSize,
                                                            @RequestParam(value = "jobId") Long jobId){
        ManualReviewQueryVO manualReviewQueryVO = ManualReviewQueryVO.builder().pageIndex(pageIndex).pageSize(pageSize).jobId(jobId).build();
        Pager<ManualReviewVO> page =candidateJobDomainService.selectManualReviewPageList(manualReviewQueryVO);
        return page;
    }

    /**
     * 拒绝列表
     * @param pageIndex
     * @param pageSize
     * @param jobId
     * @return
     */
    @Auth(roleType = RoleType.SUB_USER)
    @GetMapping("/denied-list")
    public Pager<DeniedListVO> selectDeniedPageList(@RequestParam(value = "pageIndex", required = false, defaultValue = "1") int pageIndex,
                                                    @RequestParam(value = "pageSize", required = false, defaultValue = "10") int pageSize,
                                                    @RequestParam(value = "jobId") Long jobId){
        CandidateJobQueryVO deniedQueryVO = CandidateJobQueryVO.builder().pageIndex(pageIndex).pageSize(pageSize).jobId(jobId).build();
        Pager<DeniedListVO> page =candidateJobDomainService.selectDeniedPageList(deniedQueryVO);
        return page;
    }


    /**
     * 就绪列表
     * @param pageIndex
     * @param pageSize
     * @param jobId
     * @return
     */
    @Auth(roleType = RoleType.SUB_USER)
    @GetMapping("/ready-list")
    public Pager<ReadyListVO> selectreadyPageList(@RequestParam(value = "pageIndex", required = false, defaultValue = "1") int pageIndex,
                                                  @RequestParam(value = "pageSize", required = false, defaultValue = "10") int pageSize,
                                                  @RequestParam(value = "jobId") Long jobId){
        ReadyQueryVO readyQueryVO = ReadyQueryVO.builder().pageIndex(pageIndex).pageSize(pageSize).jobId(jobId).build();
        Pager<ReadyListVO> page =candidateJobDomainService.selectReadyPageList(readyQueryVO);
        return page;
    }


    /**
     * ai面试结果页面 通过
     * @param vettedPassDto
     * @return
     */
    @Auth(roleType = RoleType.SUB_USER)
    @PostMapping("/ai-vetted-pass")
    public Boolean aiVettedPass(@RequestBody @Validated VettedPassDto vettedPassDto){
        return candidateJobDomainService.aiVettedPass(vettedPassDto);
    }

    /**
     * 人工审核页面 通过
     * @param reviewPassDto
     * @return
     */
    @Auth(roleType = RoleType.SUB_USER)
    @PostMapping("/review-pass")
    public Boolean reviewPass(@RequestBody @Validated ReviewPassDto reviewPassDto){
        return candidateJobDomainService.reviewPass(reviewPassDto);
    }

    /**
     * 自动流程-人工审核页面 通过
     * @param reviewPassDto
     * @return
     */
    @Auth(roleType = RoleType.SUB_USER)
    @PostMapping("/manual-review-pass")
    public Boolean manualReviewPass(@RequestBody @Validated ReviewPassDto reviewPassDto){
        return candidateJobDomainService.manualReviewPass(reviewPassDto);
    }

    /**
     * 就绪页面 通过
     * @param reviewPassDto
     * @return
     */
    @Auth(roleType = RoleType.SUB_USER)
    @PostMapping("/ready-pass")
    public Boolean readyPass(@RequestBody @Validated ReadyPassDto reviewPassDto){
        return candidateJobDomainService.readyPass(reviewPassDto);
    }

    /**
     * 拒绝候选人
     * @param rejectCandidateDto
     * @return
     */
    @Auth(roleType = RoleType.SUB_USER)
    @PostMapping("/reject-candidate")
    public Boolean rejectCandidate(@RequestBody @Validated RejectCandidateDto rejectCandidateDto){
        return candidateJobDomainService.rejectCandidate(rejectCandidateDto);
    }


    /**
     * ai面试报告
     * @param id
     * @return
     */
    @Auth(roleType = RoleType.SUB_USER)
    @GetMapping("/interview-report")
    public InterviewReportVO getInterviewReport(@RequestParam(value = "id", required = true) Long id){
        InterviewReportVO interviewReport =candidateJobDomainService.getInterviewReport(id);
        return interviewReport;
    }

    /**
     * 候选人应聘流程时间线
     * @param id
     * @return
     */
    @Auth(roleType = RoleType.SUB_USER)
    @GetMapping("/candidate-process")
    public CandidateProcessVO getCandidateProcess(@RequestParam(value = "id", required = true) Long id){
        CandidateProcessVO candidateProcess =candidateJobDomainService.getCandidateProcess(id);
        return candidateProcess;
    }


    /**
     * 候选人详情
     * @param id
     * @return
     */
    @Auth(roleType = RoleType.SUB_USER)
    @GetMapping("/candidate-details")
    public CandidateDetailsVO getCandidateDetails(@RequestParam(value = "id", required = true) Long id){
        CandidateDetailsVO candidateDetailsVO =candidateJobDomainService.getCandidateDetails(id);
        return candidateDetailsVO;
    }

    /**
     * 获取笔试结果
     * @return
     */
    @Auth(roleType = RoleType.SUB_USER)
    @GetMapping("/interview-result")
    public WrittenDetailVO getWrittenResult(@RequestParam(value = "callId", required = true) String callId){
        if (callId.contains(DataSourceEnum.PHL.getName())){
            Long candidateJobId = Long.parseLong(callId.replace(DataSourceEnum.PHL.getName(), ""));
            return candidateJobDomainService.getVettedWrittenResult(candidateJobId);
        }else{
            return aiService.getWrittenDetailResult(callId);
        }
    }



    //@Auth(roleType = RoleType.SUB_USER)
    @GetMapping("/export-interview-report/{callId}")
    public String exportInterviewReport(@PathVariable(value = "callId") String callId , HttpServletResponse response) {
        return aiService.getInterviewReportUrl(callId);
    }


    /**
     * application
     * @param pageIndex
     * @param pageSize
     * @param jobId
     * @return
     */
    @Auth(roleType = RoleType.SUB_USER)
    @GetMapping("/application-list")
    public Pager<ApplicationVO> selectApplicationPageList(@RequestParam(value = "pageIndex", required = false,
                                                                 defaultValue = "1") int pageIndex,
                                                            @RequestParam(value = "pageSize", required = false, defaultValue = "10") int pageSize,
                                                            @RequestParam(value = "jobId") Long jobId,
                                                          @RequestParam(value = "score", required = false) Integer score){
        ApplicationQueryVO applicationQueryVO =
                ApplicationQueryVO.builder().pageIndex(pageIndex).pageSize(pageSize).jobId(jobId).score(score).build();
        Pager<ApplicationVO> page =candidateJobDomainService.selectApplicationPageList(applicationQueryVO);
        return page;
    }

    /**
     * 邀请重新申请
     *
     * @param candidateJobId 候选人职位关联ID
     * @return 邀请结果
     */
    @Auth(roleType = RoleType.SUB_USER)
    @GetMapping("/invite-reapply/{candidateJobId}")
    public Boolean inviteReapply(@PathVariable Long candidateJobId) {
        return candidateJobDomainService.sendReapplyInvitation(candidateJobId);
    }


    /**
     * Generate candidate share link
     * Validates candidate existence, encrypts candidate ID and returns complete shareable URL
     *
     * @param candidateId Candidate ID to generate share link for
     * @return Complete share link URL
     */
    @Auth(roleType = RoleType.SUB_USER)
    @GetMapping("/share-link/{candidateId}")
    public CandidateShareVO generateCandidateShareLink(@PathVariable Long candidateId) {
        return candidateJobDomainService.generateCandidateShareLink(candidateId);
    }

    /**
     * Get candidate details by encrypted share ID (for recruiters)
     * Requires recruiter authentication to access shared candidate information
     *
     * @param encryptedId Encrypted candidate ID from share link
     * @return Complete candidate details including education and employment history
     */
    @Auth(roleType = RoleType.SUB_USER)
    @GetMapping("/shared-details/{encryptedId}")
    public CandidateDetailsVO getCandidateSharedDetails(@PathVariable String encryptedId) {
        log.info("Getting candidate shared details for encrypted ID: {}", encryptedId);

        // Validate input
        if (StringUtils.isBlank(encryptedId)) {
            log.warn("Encrypted candidate ID is blank");
            throw new BusinessException(CandidateResponseCode.CANDIDATE_SHARE_LINK_INVALID);
        }

        // Call service to decrypt and get candidate details
        CandidateDetailsVO candidateDetails =
                candidateJobDomainService.getCandidateDetailsByShareId(encryptedId);

        log.info("Successfully retrieved shared candidate details for encrypted ID: {}",
                encryptedId);

        return candidateDetails;
    }

    /**
     * Recommend job to candidate
     * Sends a recommendation email to the candidate with job details
     * Only accessible by recruiters (SUB_USER role)
     *
     * @param request request containing jobId, candidateId and optional recommendReasons
     * @return true if email sent successfully, false otherwise
     */
    @Auth(roleType = RoleType.SUB_USER)
    @PostMapping("/recommend/invite/email")
    public Boolean recommendJob(@RequestBody @Validated RecommendJobEmailRequestDTO request) {
        return recommendationDomainService.sendRecommendationEmail(request);
    }

    /**
     * 获取候选人投递记录
     *
     * @param candidateId 候选人ID
     * @param pageIndex 页码
     * @param pageSize 每页大小
     * @return 候选人投递记录分页数据
     */
    @Auth(roleType = RoleType.SUB_USER)
    @GetMapping("/candidate-job-records")
    public Pager<CandidateJobRecordVO> getCandidateJobRecords(
            @RequestParam Long candidateId,
            @RequestParam(defaultValue = "1") int pageIndex,
            @RequestParam(defaultValue = "10") int pageSize) {
        return candidateJobDomainService.getCandidateJobRecords(candidateId, pageIndex, pageSize);
    }
}
