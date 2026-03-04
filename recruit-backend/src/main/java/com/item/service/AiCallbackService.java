package com.item.service;

import com.item.entity.AiCallbackEntity;
import com.item.entity.AiVettedResultEntity;
import com.item.entity.AiVettedResultSkillEntity;
import com.item.entity.CandidateJobEntity;
import com.item.vo.ai.AiCallbackDataVO;

import java.util.List;

public interface AiCallbackService {

    /**
     * 保存完成 AI 面试并结果
     * @param candidateJobEntity
     * @param aiCallback
     * @param aiVettedResult
     * @param aiVettedResultSkills
     */
    void saveAiInterviewResult(
            CandidateJobEntity candidateJobEntity,
            AiCallbackEntity aiCallback,
            AiVettedResultEntity aiVettedResult,
            List<AiVettedResultSkillEntity> aiVettedResultSkills
    );

    /**
     * 保存未完成 AI 面试结果，会记录 24 小时发短信逻辑
     * @param scheduleId
     * @param candidateJobEntity
     * @param aiCallback
     * @param aiVettedResult
     * @param aiVettedResultSkills
     */
    void saveAiInterviewIncompleteResult(
            String scheduleId,
            CandidateJobEntity candidateJobEntity,
            AiCallbackEntity aiCallback,
            AiVettedResultEntity aiVettedResult,
            List<AiVettedResultSkillEntity> aiVettedResultSkills
    );

    void candidateRejectPhoneInterview(Long candidateJobId, CandidateJobEntity candidateJobEntity, AiCallbackEntity callback);

    void recallCandidateInterviewPhone(Long candidateJobId, CandidateJobEntity candidateJobEntity, AiCallbackDataVO data, AiCallbackEntity callback);

    void saveReportCallback(AiCallbackEntity reportCallback, AiVettedResultEntity aiVettedResult);

    /**
     * 取消电话面试，记录回调并删除回拨记录
     * @param callback 回调实体
     * @param candidateJobId 候选人职位ID
     */
    void cancelPhoneInterview(AiCallbackEntity callback, Long candidateJobId);
}
