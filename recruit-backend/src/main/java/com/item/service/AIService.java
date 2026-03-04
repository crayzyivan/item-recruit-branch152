package com.item.service;

import com.item.dto.ai.*;
import com.item.dto.job.JobCreateBO;
import com.item.entity.CandidateJobEntity;
import com.item.entity.JobEntity;
import com.item.vo.CandidateRequestVO;
import com.item.vo.ai.InterviewTypeResultVO;
import com.item.vo.ai.InterviewUpdateResultVO;
import com.item.vo.ai.JobMatchResultVO;
import com.item.vo.ai.UpdateInterviewTypeResponseVO;
import com.item.vo.ai.WrittenDetailVO;
import org.springframework.web.multipart.MultipartFile;

public interface AIService {
    JobMatchResultVO aiMatch(CandidateJobEntity candidateJobEntity);
    Object createInterview(InterviewRequestDTO interviewRequestDTO, JobCreateBO jobCreateBO);
    JobResponseDataDTO generateJob(GenerateJobRequestDTO aiJobRequest);
    Boolean checkInfo(String candidateEmail, String interviewId, Long applicationId, String candidateName);
    Boolean callback(AiCallbackDTO aiCallbackDTO);
    Boolean getInterviewResult(AiCallbackDTO aiCallbackDTO,JobEntity jobEntity);
    WrittenDetailVO getWrittenDetailResult(String callId);
    InterviewResponseDTO getInterviewLink(GenerateInterviewLinkDTO generateInterviewLinkDTO,JobEntity jobEntity);

    /**
     * AI 面试结束后回调接口
     * @param aiCallbackDTO
     * @return
     */
    Boolean aiInterviewCallback(AiCallbackDTO aiCallbackDTO);

    /**
     * 预约 AI 电话面试
     * @param requestDTO
     * @param jobEntity
     * @return
     */
    Boolean createAIPhoneInterview(AIPhoneInterviewRequestDTO requestDTO, JobEntity jobEntity);

    /**
     * 超过 24 小时后候选人未参加面试，直接呼叫候选人进行电话面试
     * @param scheduleId    AI 侧数据唯一标识
     * @return
     */
    Boolean recallAIPhoneInterview(String scheduleId);

    /**
     * 根据上传pdf文件生成工作描述
     * @param file
     * @return
     */
    JobDescriptionDTO pdfGenerateJobDescription(MultipartFile file);

    String getCallId(Long candidateJobId);

    String exportInterviewReport(String callId);

    CandidateRequestVO resumeParsing(MultipartFile multipartFile);

    InterviewUpdateResultVO updateInterview(InterviewUpdateRequestDTO interviewRequestDTO,JobEntity jobEntity);

    String getInterviewReportUrl(String callId);

    /**
     * 更新面试类型
     * @param updateInterviewTypeDTO 更新面试类型请求参数
     * @return 更新结果
     */
    UpdateInterviewTypeResponseVO updateInterviewType(UpdateInterviewTypeDTO updateInterviewTypeDTO);

    Integer getInterviewType(String callId);
}
