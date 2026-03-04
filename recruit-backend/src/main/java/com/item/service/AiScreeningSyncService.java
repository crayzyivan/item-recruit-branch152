package com.item.service;

import com.item.vo.ApplicationsSyncVO;

import java.util.List;

/**
 * AI筛选结果同步服务接口
 * 
 * 提供AI筛选结果同步的核心业务方法，包括从PostgreSQL的application_screening_reports表
 * 同步筛选结果到MySQL的r_candidate_job表的功能。
 * 
 * @author liyunlong
 * @version 1.0
 * @since 2025-10-10
 */
public interface AiScreeningSyncService {

    /**
     * 同步AI筛选结果到r_candidate_job表
     * 
     * @param candidateJobId 候选人职位关联ID，对应r_candidate_job表的id
     * @param applicationId 申请ID，对应PostgreSQL数据库candidates.applications的申请信息
     * @return 同步是否成功
     */
    Boolean syncScreeningResult(Long candidateJobId, String applicationId);

    /**
     * 批量同步
     * @param syncVo
     * @return
     */
    boolean aiScreeningSync(ApplicationsSyncVO syncVo);

    /**
     * 未同步
     * @return
     */
    List<String> notAiScreeningSync();
}
