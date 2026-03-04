package com.item.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.item.dto.AiVettedResultDTO;
import com.item.dto.AnswerQuestion5sTestConfirmRequestDTO;
import com.item.dto.AnswerQuestion5sTestStatusRequestDTO;
import com.item.dto.CandidateJobCountDTO;
import com.item.dto.CandidateShareVO;
import com.item.dto.ReadyPassDto;
import com.item.dto.RejectCandidateDto;
import com.item.dto.ReviewPassDto;
import com.item.dto.VettedPassDto;
import com.item.dto.ai.ResumeAIMatchDTO;
import com.item.dto.job.CandidateJobApplyDto;
import com.item.dto.job.CandidateJobUpdateStatusDto;
import com.item.entity.CandidateJobEntity;
import com.item.framework.http.Pager;
import com.item.vo.AiVettedQueryVO;
import com.item.vo.ApplicationQueryVO;
import com.item.vo.ApplicationVO;
import com.item.vo.CandidateDetailsVO;
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
import com.item.vo.ai.WrittenDetailVO;
import jakarta.servlet.http.HttpServletResponse;

import java.time.OffsetDateTime;

/**
 * 应聘者职位关系领域服务接口
 */
public interface CandidateJobDomainService {

    /**
     * 申请职位
     *
     * @param dto 职位申请请求
     * @return 是否申请成功
     */
    boolean applyJob(CandidateJobApplyDto dto);

    /**
     * 更新应聘状态
     *
     * @param dto 状态更新请求
     * @return 是否更新成功
     */
    @Deprecated
    boolean updateStatus(CandidateJobUpdateStatusDto dto);
    IPage<ResumeAIMatchDTO> resumeAIMatch(int pageNo, int pageSize, ResumeAIMatchDTO dto);

    /**
     * 获取AI筛选结果列表
     *
     * @param aiVettedQueryVO 筛选结果查询参数
     * @return 筛选结果列表
     */
    IPage<AiVettedResultDTO> selectAiVettedPageList(AiVettedQueryVO aiVettedQueryVO);

    /**
     * 根据候选人投递岗位信息创建 AI 面试内容，如 AI 视频面试和 AI 电话面试
     * @param candidateJobId    候选人投递岗位ID
     * @return
     */
    String createAiInterview(Long candidateJobId);

    CandidateJobCountDTO getCandidateNumber(Long jobId);

    /**
     * 获取待人工审核列表
     * @param pendingReviewQueryVO
     * @return
     */
    Pager<PendingReviewVO> selectPendingReviewPageList(PendingReviewQueryVO pendingReviewQueryVO);

    /**
     * 获取自动流程-待人工审核列表
     * @param manualReviewQueryVO
     * @return
     */
    Pager<ManualReviewVO> selectManualReviewPageList(ManualReviewQueryVO manualReviewQueryVO);

    /**
     * 获取拒绝列表
     * @param deniedQueryVO
     * @return
     */
    Pager<DeniedListVO> selectDeniedPageList(CandidateJobQueryVO deniedQueryVO);

    /**
     * 获取就绪列表
     * @param readyQueryVO
     * @return
     */
    Pager<ReadyListVO> selectReadyPageList(ReadyQueryVO readyQueryVO);

    /**
     * ai面试结果页面 通过
     * @param vettedPassDto
     * @return
     */
    Boolean aiVettedPass(VettedPassDto vettedPassDto);

    /**
     * 人工审核页面 通过
     * @param reviewPassDto
     * @return
     */
    Boolean reviewPass(ReviewPassDto reviewPassDto);

    /**
     * 自动流程-人工审核页面 通过
     * @param reviewPassDto
     * @return
     */
    Boolean manualReviewPass(ReviewPassDto reviewPassDto);

    /**
     * 就绪页面 通过
     * @param reviewPassDto
     * @return
     */
    Boolean readyPass(ReadyPassDto reviewPassDto);

    /**
     * 拒绝候选人
     * @param rejectCandidateDto
     * @return
     */
    Boolean rejectCandidate(RejectCandidateDto rejectCandidateDto);

    /**
     * 获取面试报告页面数据
     * @param candidateJobId
     * @return
     */
    InterviewReportVO getInterviewReport(Long candidateJobId);

    /**
     * 候选人应聘流程时间线
     * @param candidateJobId
     * @return
     */
    CandidateProcessVO getCandidateProcess(Long candidateJobId);

    /**
     * 候选人详细信息
     * @param id 候选人id
     * @return
     */
    CandidateDetailsVO getCandidateDetails(Long id);

    /**
     * Get candidate details by encrypted share ID
     * Decrypt the encrypted candidate ID from share link and retrieve candidate details
     * This method is used by recruiters to view shared candidate information
     *
     * @param encryptedId Encrypted candidate ID from share link
     * @return Candidate details VO with complete information
     */
    CandidateDetailsVO getCandidateDetailsByShareId(String encryptedId);

    /**
     *  ai简历筛选 更新招聘状态
     * @param candidateJobEntity
     * @return
     */
     ResumeAIMatchDTO resumeAIMatch(CandidateJobEntity candidateJobEntity,Long userId,String companyCode);


    /**
     * 发面试邮件 扣积分
     * @param candidateJob
     */
    void processInterviewMail(CandidateJobEntity candidateJob,Long userId, boolean isAutoTrigger);

    /**
     * application
     * @param applicationQueryVO
     * @return
     */
    Pager<ApplicationVO> selectApplicationPageList(ApplicationQueryVO applicationQueryVO);

    /**
     * 发送重新申请邀请邮件
     * @param candidateJobId 候选人职位关联ID
     * @return 是否发送成功
     */
    Boolean sendReapplyInvitation(Long candidateJobId);

    /**
     * 检查职位是否可以申请
     * @param jobId 职位ID
     * @return 是否可以申请
     */
    Boolean checkApplicationJob(Long jobId);

    /**
     * Validate interview time to check for conflicts
     * @param candidateId Candidate ID
     * @param preferredInterviewStartTime Preferred interview start time
     * @param preferredInterviewEndTime Preferred interview end time
     * @return true if no conflict, false otherwise
     */
    Boolean validateInterviewTime(Long candidateId, OffsetDateTime preferredInterviewStartTime, OffsetDateTime preferredInterviewEndTime);

    /**
     * Generate encrypted candidate ID for sharing
     * Validates candidate existence and encrypts candidate ID
     *
     * @param candidateId Candidate ID to encrypt
     * @return Encrypted candidate ID string
     */
    CandidateShareVO generateCandidateShareLink(Long candidateId);

    /**
     * 获取笔试结果
     * @param candidateJobId
     * @return
     */
    WrittenDetailVO getVettedWrittenResult(Long candidateJobId);

    /**
     * 视频跳转中转
     * @param applicationId
     * @param response
     */
    void redirectToVideo(String applicationId, HttpServletResponse response);

    /**
     *
     * @param candidateJobId
     */
    void sendInterviewReportMailToCandidate(Long candidateJobId);

    /**
     * 候选人投递记录
     * @param candidateId
     * @param pageIndex
     * @param pageSize
     * @return
     */
    Pager<CandidateJobRecordVO> getCandidateJobRecords(Long candidateId, int pageIndex, int pageSize);

    /**
     * 是否已经作答
     *
     * @param requestDTO
     * @return
     */
    Boolean isAnswerQuestion5sTest(AnswerQuestion5sTestStatusRequestDTO requestDTO);

    /**
     * 提交作答
     *
     * @param requestDTO
     * @return
     */
    Boolean answerQuestion5sTestConfirm(AnswerQuestion5sTestConfirmRequestDTO requestDTO);
}
