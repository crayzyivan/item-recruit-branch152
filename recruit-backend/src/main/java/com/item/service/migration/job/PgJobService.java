package com.item.service.migration.job;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.item.entity.migration.job.PgJobEntity;

/**
 * PostgreSQL Job 服务接口
 * 
 * 提供对 PostgreSQL 数据库中 companies.jobs 表的业务操作。
 * 继承 MyBatis Plus 的 IService 接口，提供完整的 CRUD 操作。
 * 支持分页查询、条件查询等复杂业务场景。
 *
 * @author system
 * @version 1.0
 * @since 2025-09-30
 */
public interface PgJobService extends IService<PgJobEntity> {

    /**
     * 分页查询 Job 数据
     * 
     * 支持按 ID 范围过滤的分页查询，用于批量迁移场景。
     * 
     * @param pageNum 页码（从1开始）
     * @param pageSize 每页大小
     * @param startPgJobId 起始 Job ID（可选）
     * @param endPgJobId 结束 Job ID（可选）
     * @return 分页结果
     */
    IPage<PgJobEntity> getJobsWithPagination(int pageNum, int pageSize, Integer startPgJobId, Integer endPgJobId);

    /**
     * 获取 Job 总数
     * 
     * 支持按 ID 范围过滤的总数统计。
     * 
     * @param startPgJobId 起始 Job ID（可选）
     * @param endPgJobId 结束 Job ID（可选）
     * @return Job 总数
     */
    long getTotalJobCount(Integer startPgJobId, Integer endPgJobId);

    /**
     * 根据 ID 获取单个 Job
     * 
     * @param jobId Job ID
     * @return Job 实体，不存在返回 null
     */
    PgJobEntity getJobById(Integer jobId);

    /**
     * 检查 Job 是否存在
     * 
     * @param jobId Job ID
     * @return 存在返回 true，不存在返回 false
     */
    boolean existsById(Integer jobId);
}
