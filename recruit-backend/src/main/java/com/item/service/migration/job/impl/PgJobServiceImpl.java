package com.item.service.migration.job.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.item.entity.migration.job.PgJobEntity;
import com.item.pgmapper.migration.job.PgJobMigrationMapper;
import com.item.service.migration.job.PgJobService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * PostgreSQL Job 服务实现类
 * 
 * 实现对 PostgreSQL 数据库中 companies.jobs 表的业务操作。
 * 继承 ServiceImpl 以便直接使用 MyBatis Plus 的 CRUD 方法。
 * 使用 LambdaQueryWrapper 实现类型安全的复杂查询逻辑。
 *
 * @author system
 * @version 1.0
 * @since 2025-09-30
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "spring.postgresql.enabled", havingValue = "true", matchIfMissing = false)
public class PgJobServiceImpl extends ServiceImpl<PgJobMigrationMapper, PgJobEntity> implements PgJobService {

    @Override
    public IPage<PgJobEntity> getJobsWithPagination(int pageNum, int pageSize, Integer startPgJobId, Integer endPgJobId) {
        log.debug("分页查询 PostgreSQL Job 数据: pageNum={}, pageSize={}, startPgJobId={}, endPgJobId={}", 
                pageNum, pageSize, startPgJobId, endPgJobId);

        try {
            // 构建查询条件
            LambdaQueryWrapper<PgJobEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.orderByAsc(PgJobEntity::getId);

            // 添加 ID 范围条件
            if (startPgJobId != null && endPgJobId != null) {
                queryWrapper.between(PgJobEntity::getId, startPgJobId, endPgJobId);
            } else if (startPgJobId != null) {
                queryWrapper.ge(PgJobEntity::getId, startPgJobId);
            } else if (endPgJobId != null) {
                queryWrapper.le(PgJobEntity::getId, endPgJobId);
            }

            // 执行分页查询
            IPage<PgJobEntity> result = this.page(new Page<>(pageNum, pageSize), queryWrapper);
            
            log.debug("分页查询完成: 总记录数={}, 当前页记录数={}", result.getTotal(), result.getRecords().size());
            return result;
        } catch (Exception e) {
            log.error("分页查询 PostgreSQL Job 数据失败: pageNum={}, pageSize={}, startPgJobId={}, endPgJobId={}", 
                    pageNum, pageSize, startPgJobId, endPgJobId, e);
            // 返回空的分页结果
            return new Page<>(pageNum, pageSize);
        }
    }

    @Override
    public long getTotalJobCount(Integer startPgJobId, Integer endPgJobId) {
        log.debug("统计 PostgreSQL Job 总数: startPgJobId={}, endPgJobId={}", startPgJobId, endPgJobId);

        try {
            // 构建查询条件
            LambdaQueryWrapper<PgJobEntity> queryWrapper = new LambdaQueryWrapper<>();

            // 添加 ID 范围条件
            if (startPgJobId != null && endPgJobId != null) {
                queryWrapper.between(PgJobEntity::getId, startPgJobId, endPgJobId);
            } else if (startPgJobId != null) {
                queryWrapper.ge(PgJobEntity::getId, startPgJobId);
            } else if (endPgJobId != null) {
                queryWrapper.le(PgJobEntity::getId, endPgJobId);
            }

            long count = this.count(queryWrapper);
            log.debug("统计完成: 总记录数={}", count);
            return count;
        } catch (Exception e) {
            log.error("统计 PostgreSQL Job 总数失败: startPgJobId={}, endPgJobId={}", startPgJobId, endPgJobId, e);
            return 0L;
        }
    }

    @Override
    public PgJobEntity getJobById(Integer jobId) {
        if (jobId == null) {
            log.warn("getJobById failed: jobId is null");
            return null;
        }

        try {
            PgJobEntity job = this.getById(jobId);
            log.debug("根据 ID 查询 Job: jobId={}, found={}", jobId, job != null);
            return job;
        } catch (Exception e) {
            log.error("根据 ID 查询 Job 失败: jobId={}", jobId, e);
            return null;
        }
    }

    @Override
    public boolean existsById(Integer jobId) {
        if (jobId == null) {
            return false;
        }

        try {
            LambdaQueryWrapper<PgJobEntity> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(PgJobEntity::getId, jobId);
            
            long count = this.count(wrapper);
            boolean exists = count > 0;
            log.debug("检查 Job 是否存在: jobId={}, exists={}", jobId, exists);
            return exists;
        } catch (Exception e) {
            log.error("检查 Job 是否存在失败: jobId={}", jobId, e);
            return false;
        }
    }
}
