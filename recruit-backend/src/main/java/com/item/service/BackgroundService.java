package com.item.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.item.dto.background.UserBackgroundCheckDTO;
import com.item.entity.BackgroundDataEntity;
import com.item.framework.http.Pager;
import com.item.vo.BackgroundListVO;
import com.item.vo.BackgroundQueryVO;

/**
 * <p>
 *
 * </p>
 *
 * @author liuyabin on 2025/8/3
 * @since 1.0.0
 */
public interface BackgroundService extends IService<BackgroundDataEntity> {

    /**
     * 开始背调
     * @param candidateJobId
     *
     */
    void startCheck(Long candidateJobId);

    void setWebHook();

    void backgroundPdfReportProcess(String data);

    UserBackgroundCheckDTO getUserBackgroundCheckResult(Long id);

    UserBackgroundCheckDTO getUserReportByCandidateAndJob(Long candidateId, Long jobId);

    /**
     * 背调列表
     * @param backgroundQueryVO
     * @return
     */
    Pager<BackgroundListVO> selectBackgroundPageList(BackgroundQueryVO backgroundQueryVO);
}
