package com.item.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.item.entity.CandidateRecallEntity;
import com.item.framework.constant.RecallStatusEnum;
import com.item.framework.constant.SmsSendStatusEnum;
import com.item.mapper.CandidateRecallMapper;
import com.item.service.CandidateRecallService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CandidateRecallServiceImpl extends ServiceImpl<CandidateRecallMapper, CandidateRecallEntity> implements CandidateRecallService {

    @Override
    public List<CandidateRecallEntity> listAllOrderByCreateTimeDesc() {
        LambdaQueryWrapper<CandidateRecallEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CandidateRecallEntity::getDeleted, false)
                .eq(CandidateRecallEntity::getSmsStatus, SmsSendStatusEnum.SUCCESS.getCode())
                .eq(CandidateRecallEntity::getRecallStatus, RecallStatusEnum.UNRECALLED.getCode())
                .orderByDesc(CandidateRecallEntity::getCreateTime);
        return list(queryWrapper);
    }


    @Override
    public boolean saveCandidateRecall(CandidateRecallEntity candidateRecallEntity) {
        if (candidateRecallEntity == null) {
            log.warn("saveCandidateRecall: candidateRecallEntity is null");
            return false;
        }
        
        if (candidateRecallEntity.getCandidateJobId() == null) {
            log.warn("saveCandidateRecall: candidateJobId is null");
            return false;
        }
        
        // 检查是否已存在相同 candidateJobId 的记录
        LambdaQueryWrapper<CandidateRecallEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CandidateRecallEntity::getDeleted, false)
                .eq(CandidateRecallEntity::getCandidateJobId, candidateRecallEntity.getCandidateJobId());
        long count = count(queryWrapper);
        
        if (count > 0) {
            log.info("saveCandidateRecall: candidateJobId={} already exists, skip saving", 
                    candidateRecallEntity.getCandidateJobId());
            return true;
        }
        
        boolean result = save(candidateRecallEntity);
        log.info("saveCandidateRecall: candidateJobId={}, result={}", 
                candidateRecallEntity.getCandidateJobId(), result);
        
        return result;
    }

    @Override
    public boolean deleteByCandidateJobId(Long candidateJobId) {
        if (candidateJobId == null) {
            log.warn("deleteByCandidateJobId: candidateJobId is null");
            return false;
        }
        
        // 逻辑删除：更新 deleted 为 true 和 modifiedTime
        LambdaUpdateWrapper<CandidateRecallEntity> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(CandidateRecallEntity::getCandidateJobId, candidateJobId)
                .eq(CandidateRecallEntity::getDeleted, false)
                .set(CandidateRecallEntity::getDeleted, true)
                .set(CandidateRecallEntity::getUpdateTime, LocalDateTime.now());
        
        boolean result = update(updateWrapper);
        log.info("deleteByCandidateJobId: candidateJobId={}, result={}", candidateJobId, result);
        
        return result;
    }

    @Override
    public boolean updateSmsStatusByCandidateJobId(Long candidateJobId, Integer smsStatus) {
        if (candidateJobId == null) {
            log.warn("updateSmsStatusByCandidateJobId: candidateJobId is null");
            return false;
        }
        
        if (smsStatus == null) {
            log.warn("updateSmsStatusByCandidateJobId: smsStatus is null");
            return false;
        }
        
        LambdaUpdateWrapper<CandidateRecallEntity> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(CandidateRecallEntity::getCandidateJobId, candidateJobId)
                .eq(CandidateRecallEntity::getDeleted, false)
                .set(CandidateRecallEntity::getSmsStatus, smsStatus)
                .set(CandidateRecallEntity::getUpdateTime, LocalDateTime.now());
        
        boolean result = update(updateWrapper);
        log.info("updateSmsStatusByCandidateJobId: candidateJobId={}, smsStatus={}, result={}", 
                candidateJobId, smsStatus, result);
        
        return result;
    }

    @Override
    public List<CandidateRecallEntity> listBySmsStatusFail() {
        LambdaQueryWrapper<CandidateRecallEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CandidateRecallEntity::getDeleted, false)
                .eq(CandidateRecallEntity::getSmsStatus, SmsSendStatusEnum.FAIL.getCode());
        return list(queryWrapper);
    }

    @Override
    public boolean updateRecallStatusById(Long id, Integer recallStatus) {
        if (id == null) {
            log.warn("updateRecallStatusById: id is null");
            return false;
        }

        if (recallStatus == null) {
            log.warn("updateRecallStatusById: recallStatus is null");
            return false;
        }

        LambdaUpdateWrapper<CandidateRecallEntity> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(CandidateRecallEntity::getId, id)
                .eq(CandidateRecallEntity::getDeleted, false)
                .set(CandidateRecallEntity::getRecallStatus, recallStatus)
                .set(CandidateRecallEntity::getUpdateTime, LocalDateTime.now());

        boolean result = update(updateWrapper);
        log.info("updateRecallStatusById: id={}, recallStatus={}, result={}",
                id, recallStatus, result);

        return result;
    }

    @Override
    public CandidateRecallEntity getByCandidateJobId(Long candidateJobId) {
        if (candidateJobId == null) {
            log.warn("getByCandidateJobId: candidateJobId is null");
            return null;
        }

        LambdaQueryWrapper<CandidateRecallEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CandidateRecallEntity::getDeleted, false)
                .eq(CandidateRecallEntity::getCandidateJobId, candidateJobId)
                .last("LIMIT 1");

        CandidateRecallEntity result = getOne(queryWrapper);
        log.info("getByCandidateJobId: candidateJobId={}, found={}", candidateJobId, result != null);

        return result;
    }

    @Override
    public List<CandidateRecallEntity> listBySmsStatusAndRecallStatus(Integer smsStatus, Integer recallStatus) {
        if (smsStatus == null || recallStatus == null) {
            log.warn("listBySmsStatusAndRecallStatus: smsStatus or recallStatus is null");
            return List.of();
        }

        LambdaQueryWrapper<CandidateRecallEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CandidateRecallEntity::getDeleted, false)
                .eq(CandidateRecallEntity::getSmsStatus, smsStatus)
                .eq(CandidateRecallEntity::getRecallStatus, recallStatus);

        List<CandidateRecallEntity> result = list(queryWrapper);
        log.info("listBySmsStatusAndRecallStatus: smsStatus={}, recallStatus={}, count={}",
                smsStatus, recallStatus, result.size());

        return result;
    }
}