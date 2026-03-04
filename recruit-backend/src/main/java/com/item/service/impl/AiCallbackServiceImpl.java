package com.item.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.item.entity.*;
import com.item.framework.constant.RecallStatusEnum;
import com.item.framework.constant.SmsSendStatusEnum;
import com.item.mapper.AICallbackMapper;
import com.item.service.*;
import com.item.vo.ai.AiCallbackDataVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class AiCallbackServiceImpl implements AiCallbackService {

    @Autowired
    private AICallbackMapper aiCallbackMapper;
    @Autowired
    private CandidateJobService candidateJobService;
    @Autowired
    private AiVettedResultService aiVettedResultService;
    @Autowired
    private AiVettedResultSkillService aiVettedResultSkillService;
    @Autowired
    private CandidateRecallService candidateRecallService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveAiInterviewResult(CandidateJobEntity candidateJobEntity, AiCallbackEntity aiCallback, AiVettedResultEntity aiVettedResult, List<AiVettedResultSkillEntity> aiVettedResultSkills) {
        log.info("Save Ai phone interview result aiCallback:{}", aiCallback);
        aiCallbackMapper.insertOrUpdate(aiCallback);
        log.info("Save Ai phone interview result aiVettedResult:{}", aiVettedResult);
        aiVettedResultService.saveOrUpdate(aiVettedResult);
        for (AiVettedResultSkillEntity skill : aiVettedResultSkills) {
            skill.setVettedResultId(aiVettedResult.getId());
        }
        log.info("Save Ai phone interview result aiVettedResultSkills:{}", aiVettedResultSkills);
        aiVettedResultSkillService.remove(new LambdaQueryWrapper<AiVettedResultSkillEntity>().eq(AiVettedResultSkillEntity::getVettedResultId, aiVettedResult.getId()));
        aiVettedResultSkillService.saveBatch(aiVettedResultSkills);
        log.info("Save Ai phone interview result candidateJobEntity:{}", candidateJobEntity);
        candidateJobService.updateById(candidateJobEntity);
        //24 小时后再次拨打的电话面试，需要删除 candidate_recall 这里为了逻辑统一简化处理，直接删除
        try {
            candidateRecallService.deleteByCandidateJobId(candidateJobEntity.getId());
        } catch (Exception e) {
            log.error("Failed to delete candidate recall by candidateJobId: {}", candidateJobEntity.getId(), e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveAiInterviewIncompleteResult(String scheduleId, CandidateJobEntity candidateJobEntity, AiCallbackEntity aiCallback, AiVettedResultEntity aiVettedResult, List<AiVettedResultSkillEntity> aiVettedResultSkills) {
        log.info("Save Ai phone interview incomplete result aiCallback:{}", aiCallback);
        aiCallbackMapper.insertOrUpdate(aiCallback);
        log.info("Save Ai phone interview incomplete result aiVettedResult:{}", aiVettedResult);
        aiVettedResultService.saveOrUpdate(aiVettedResult);
        for (AiVettedResultSkillEntity skill : aiVettedResultSkills) {
            skill.setVettedResultId(aiVettedResult.getId());
        }
        log.info("Save Ai phone interview incomplete result aiVettedResultSkills:{}", aiVettedResultSkills);
        aiVettedResultSkillService.remove(new LambdaQueryWrapper<AiVettedResultSkillEntity>().eq(AiVettedResultSkillEntity::getVettedResultId, aiVettedResult.getId()));
        aiVettedResultSkillService.saveBatch(aiVettedResultSkills);
        log.info("Save Ai phone interview incomplete result candidateJobEntity:{}", candidateJobEntity);
        candidateJobService.updateById(candidateJobEntity);

        CandidateRecallEntity recallEntity = new CandidateRecallEntity();
        recallEntity.setCandidateJobId(candidateJobEntity.getId());
        recallEntity.setInterviewPhone(candidateJobEntity.getInterviewPhone());
        recallEntity.setInterviewLanguage(candidateJobEntity.getInterviewLanguage());
        recallEntity.setPreferredInterviewStartTime(candidateJobEntity.getPreferredInterviewStartTime());
        recallEntity.setPreferredInterviewEndTime(candidateJobEntity.getPreferredInterviewEndTime());
        recallEntity.setCreateTime(LocalDateTime.now());
        recallEntity.setScheduleId(scheduleId);
        recallEntity.setSmsStatus(SmsSendStatusEnum.FAIL.getCode());
        recallEntity.setRecallStatus(RecallStatusEnum.UNRECALLED.getCode());
        candidateRecallService.saveCandidateRecall(recallEntity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void candidateRejectPhoneInterview(Long candidateJobId, CandidateJobEntity candidateJobEntity, AiCallbackEntity callback) {
//        candidateRecallService.deleteByCandidateJobId(candidateJobId);
        candidateJobService.updateById(candidateJobEntity);
        aiCallbackMapper.insert(callback);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recallCandidateInterviewPhone(Long candidateJobId, CandidateJobEntity candidateJobEntity, AiCallbackDataVO data, AiCallbackEntity callback) {
        CandidateRecallEntity recallEntity = new CandidateRecallEntity();
        recallEntity.setCandidateJobId(candidateJobId);
        recallEntity.setInterviewPhone(candidateJobEntity.getInterviewPhone());
        recallEntity.setInterviewLanguage(candidateJobEntity.getInterviewLanguage());
        recallEntity.setPreferredInterviewStartTime(candidateJobEntity.getPreferredInterviewStartTime());
        recallEntity.setPreferredInterviewEndTime(candidateJobEntity.getPreferredInterviewEndTime());
        recallEntity.setCreateTime(LocalDateTime.now());
        recallEntity.setScheduleId(data.getScheduleId());
        recallEntity.setSmsStatus(SmsSendStatusEnum.FAIL.getCode());
        recallEntity.setRecallStatus(RecallStatusEnum.UNRECALLED.getCode());
        candidateRecallService.saveCandidateRecall(recallEntity);
        candidateJobService.updateById(candidateJobEntity);
        aiCallbackMapper.insert(callback);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveReportCallback(AiCallbackEntity reportCallback, AiVettedResultEntity aiVettedResult) {
        aiVettedResultService.saveOrUpdate(aiVettedResult);
        aiCallbackMapper.insert(reportCallback);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelPhoneInterview(AiCallbackEntity callback, Long candidateJobId) {
        log.info("Cancel phone interview, candidateJobId: {}, callback: {}", candidateJobId, callback);
        // 1. 记录回调结果到 r_ai_callback
        aiCallbackMapper.insert(callback);
        log.info("cancelPhoneInterview: saved callback record, candidateJobId: {}", candidateJobId);

        // 2. 删除 r_candidate_recall 记录
        boolean deleteResult = candidateRecallService.deleteByCandidateJobId(candidateJobId);
        log.info("cancelPhoneInterview: deleted candidate recall record, candidateJobId: {}, result: {}", candidateJobId, deleteResult);
    }
}
