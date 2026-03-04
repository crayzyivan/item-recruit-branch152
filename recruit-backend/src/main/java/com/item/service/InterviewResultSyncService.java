package com.item.service;

import com.item.vo.ApplicationsSyncVO;
import jakarta.servlet.http.HttpServletResponse;

import java.util.List;

/**
 * 面试结果同步服务接口
 * 
 * 提供PostgreSQL面试结果数据同步到MySQL的业务操作。
 * 支持从candidates.application_interview_reports表同步数据到r_ai_vetted_result表。
 * 
 * @author liyunlong
 * @version 1.0
 * @since 2025-09-29
 */
public interface InterviewResultSyncService {

    /**
     * 同步PostgreSQL面试结果数据到MySQL
     * 
     * 从PostgreSQL的candidates.application_interview_reports表查询面试结果数据，
     * 转换后保存到MySQL的r_ai_vetted_result表中。
     * 该方法具有幂等性，支持重复调用而不产生副作用。
     * 
     * @param candidateJobId 候选人职位关联ID，对应r_ai_vetted_result表的candidate_job_id字段
     * @param applicationId 申请ID，对应PostgreSQL candidates.applications表的面试结果信息
     * @return 同步结果，true表示同步成功，false表示同步失败
     * @throws com.item.framework.error.BusinessException 当参数无效、数据不存在或同步失败时抛出业务异常
     */
    Boolean syncInterviewResultFromPg(Long candidateJobId, String applicationId);

    /**
     * 下载m3u8视频并上传到S3
     *
     * @param videoUrl 原始视频URL（m3u8格式）
     * @param candidateJobId 候选人职位关联ID
     * @return S3中的文件key，如果失败则返回原始URL
     */
    String downloadAndUploadVideoToS3(String videoUrl, Long candidateJobId,String applicationId);


    /**
     * 视频跳转中转
     * @param applicationId
     * @param response
     */
    void redirectToVideo(String applicationId, HttpServletResponse response);

    /**
     * 同步面试结果数据
     * @param syncVo
     * @return
     */
    boolean interviewResultSync(ApplicationsSyncVO syncVo);

    /**
     * 未同步的id
     * @return
     */
    List<String> notInterviewResultSync();
}
