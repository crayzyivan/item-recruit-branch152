package com.item.service;

import com.item.dto.iam.IamCreateUserReqDTO;
import com.item.entity.CandidateEntity;
import com.item.pgentity.PgCandidateData;

/**
 * 应聘者数据迁移服务接口
 * 定义从PostgreSQL迁移数据到MySQL和Elasticsearch的契约
 * 
 * @author system
 */
public interface CandidateDataMigrationService {

    /**
     * 迁移所有应聘者数据（使用默认线程池）
     * 使用默认线程池大小进行并发迁移
     */
    void migrateAllCandidates();

    /**
     * 迁移所有应聘者数据（使用指定线程数的线程池并发处理）
     * 
     * @param threadPoolSize 线程池大小
     */
    void migrateAllCandidates(int threadPoolSize);

    /**
     * 迁移单个应聘者数据
     * 
     * @param pgCandidate PostgreSQL应聘者数据
     */
    void migrateSingleCandidate(PgCandidateData pgCandidate);

    /**
     * 批量迁移指定数量的应聘者（使用默认线程池）
     * 
     * @param batchSize 批次大小
     * @param offset 偏移量
     */
    void migrateCandidatesBatch(int batchSize, int offset);

    /**
     * 批量迁移指定数量的应聘者（使用指定线程数的线程池并发处理）
     * 
     * @param batchSize 批次大小
     * @param offset 偏移量
     * @param threadPoolSize 线程池大小
     */
    void migrateCandidatesBatch(int batchSize, int offset, int threadPoolSize);

    /**
     * 根据应聘者ID迁移指定应聘者
     * 
     * @param candidateId 应聘者ID
     */
    void migrateCandidatesbyCandidateId(String candidateId);

    /**
     * 注册应聘者到IAM系统
     * 
     * @param iamCreateUserReqDTO IAM用户创建请求
     * @return 注册后的应聘者实体
     */
    CandidateEntity registerCandidate(IamCreateUserReqDTO iamCreateUserReqDTO);
}
