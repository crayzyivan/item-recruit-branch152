package com.item.service.migration.job.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.google.common.base.MoreObjects;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.item.convert.JobConvert;
import com.item.convert.migration.job.JobMigrationConvert;
import com.item.dto.CountryDTO;
import com.item.dto.ai.InterviewDataDTO;
import com.item.dto.ai.InterviewRequestDTO;
import com.item.dto.ai.QuestionGenerationDTO;
import com.item.dto.job.LocationValRecordDTO;
import com.item.dto.migration.company.CompanyProfileKeyDTO;
import com.item.dto.migration.job.CompanyInfoDTO;
import com.item.dto.migration.job.JobDetailsDTO;
import com.item.dto.migration.job.JobMigrationRequestDTO;
import com.item.dto.migration.job.JobMigrationResponseDTO;
import com.item.dto.migration.job.MigrateJobDTO;
import com.item.entity.AyrshareCompanyConfigEntity;
import com.item.entity.CityEntity;
import com.item.entity.CountryEntity;
import com.item.entity.JobEntity;
import com.item.entity.StateEntity;
import com.item.entity.migration.DataMigrationMappingEntity;
import com.item.entity.migration.company.PgCompanyEntity;
import com.item.entity.migration.job.PgJobEntity;
import com.item.entity.migration.location.PgLocationEntity;
import com.item.es.JobEsService;
import com.item.es.entity.JobEsEntity;
import com.item.framework.config.AiInterviewConfig;
import com.item.framework.constant.MigrationBusTypeEnum;
import static com.item.framework.constant.MigrationBusTypeEnum.JOB;
import com.item.framework.utils.MDCThreadPoolExecutor;
import com.item.mapper.CityMapper;
import com.item.mapper.CountryMapper;
import com.item.mapper.StateMapper;
import com.item.pgmapper.migration.company.PgCompanyMapper;
import com.item.service.AyrshareCompanyConfigService;
import com.item.service.JobService;
import com.item.service.LocationService;
import com.item.service.XmlFeedConfigService;
import com.item.service.impl.AIServiceImpl;
import com.item.service.migration.DataMigrationMappingService;
import static com.item.service.migration.category.Constant.CITY_MAP;
import static com.item.service.migration.category.Constant.PG_COMPANY_ID_NAME_MAP;
import static com.item.service.migration.category.Constant.STATE_MAP;
import com.item.service.migration.category.ConvertService;
import com.item.service.migration.job.JobMigrationService;
import com.item.service.migration.job.PgJobService;
import com.item.service.migration.job.Utils;
import com.item.service.migration.location.PgLocationService;
import com.item.util.CommonUtils;
import com.item.util.HashUtils;
import com.item.util.JsonUtils;
import com.item.vo.ai.InterviewResultVO;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.apache.hc.core5.util.Timeout;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Job 迁移服务实现类
 * 
 * 实现 PostgreSQL Job 数据迁移到 MySQL 和 Elasticsearch 的核心业务逻辑。
 * 通过依赖注入的方式使用各个服务，保持职责分离和代码清晰。
 * 包含批量迁移、单条迁移、数据验证、错误处理等功能。
 *
 * @author system
 * @version 1.0
 * @since 2025-09-30
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "spring.postgresql.enabled", havingValue = "true", matchIfMissing = false)
@RequiredArgsConstructor
public class JobMigrationServiceImpl implements JobMigrationService {
    private static final ThreadPoolExecutor POOL_EXECUTOR = new MDCThreadPoolExecutor(20, 40, 60,
            TimeUnit.SECONDS, new LinkedBlockingQueue<>(300), new ThreadFactoryBuilder()
            .setNameFormat("migration-%d")
            .build());

    @Value("${company.name.replace.str:,.，。#@%$}")
    private String companyNameReplace;

    private final PgJobService pgJobService;
    private final JobService jobService;
    private final JobEsService jobEsService;
    private final JobMigrationConvert jobMigrationConvert;
    private final DataMigrationMappingService dataMigrationMappingService;
    private final ConvertService categoryConvertService;
    private final PgLocationService pgLocationService;
    private final CountryMapper countryMapper;
    private final CityMapper cityMapper;
    private final StateMapper stateMapper;
    private final PgCompanyMapper pgCompanyMapper;
    private final AyrshareCompanyConfigService ayrshareCompanyConfigService;


    private final XmlFeedConfigService xmlFeedConfigService;
    private final LocationService locationService;
    private final AiInterviewConfig aiInterviewConfig;
    private final AIServiceImpl aiService;



    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String LIMIT_ONE = "LIMIT 1";
    // 响应状态常量
    private static final String STATUS_SUCCESS = "SUCCESS";
    private static final String STATUS_FAILED = "FAILED";

    @Override
    public JobMigrationResponseDTO batchMigrateJobs(JobMigrationRequestDTO request) {
        long startTimeMillis = System.currentTimeMillis();
        long totalProcessed = 0;
        Set<Integer> totalProcessedIds = new HashSet<>();
        long successMigratedToMysql = 0;
        Set<Integer> successMigratedToMysqlIds = new HashSet<>();
        long successSyncedToEs = 0;
        long skippedCount = 0;
        Set<Integer> skippedCountIds = new HashSet<>();
        long failedCount = 0;
        Set<Integer> failedCountIds = new HashSet<>();

        try {
            // 分页处理
            int pageNum = 1;
            int batchSize = request.getBatchSize();
            boolean hasMoreData = true;

            while (hasMoreData) {
                // 处理单页数据
                BatchProcessResult result = ((JobMigrationService)AopContext.currentProxy()).processSinglePageJobs(pageNum, batchSize, request,
                    totalProcessedIds, successMigratedToMysqlIds, skippedCountIds, failedCountIds);
                // 更新统计数据
                totalProcessed += result.getProcessedCount();
                successMigratedToMysql += result.getSuccessCount();
                skippedCount += result.getSkippedCount();
                failedCount += result.getFailedCount();
                
                // 检查是否还有更多数据
                hasMoreData = result.isHasMoreData();
                pageNum++;

                // 记录进度
                if (totalProcessed % 1000 == 0) {
                    log.info("已处理 {} 条记录，成功: {}, 跳过: {}, 失败: {}",
                        totalProcessed, successMigratedToMysql, skippedCount, failedCount);
                }
                Timeout.ofSeconds(2);
            }
        } catch (Exception e) {
            log.error("批量迁移 Job 发生异常", e);
            failedCount = totalProcessed - successMigratedToMysql - skippedCount; // 重新计算失败数
        }

        String endTime = LocalDateTime.now().format(DATETIME_FORMATTER);
        long elapsedTimeMillis = System.currentTimeMillis() - startTimeMillis;

        // 构建响应结果
        String status;
        if (failedCount == 0) {
            status = "SUCCESS";
        } else if (successMigratedToMysql > 0) {
            status = "PARTIAL_SUCCESS";
        } else {
            status = "FAILED";
        }

        String message = String.format("迁移完成。总计: %d, 成功: %d, 跳过: %d, 失败: %d",
            totalProcessed, successMigratedToMysql, skippedCount, failedCount);

        return JobMigrationResponseDTO.builder()
            .totalProcessed(totalProcessed)
            .successMigratedToMysql(successMigratedToMysql)
            .successSyncedToEs(successSyncedToEs)
            .skippedCount(skippedCount)
            .failedCount(failedCount)
            .startTime(startTimeMillis > 0 ? LocalDateTime.ofEpochSecond(startTimeMillis / 1000, 0, java.time.ZoneOffset.UTC).format(DATETIME_FORMATTER) : null)
            .endTime(endTime)
            .elapsedTimeMillis(elapsedTimeMillis)
            .status(status)
            .message(message)
                .failedCountIds(failedCountIds)
                .skippedCountIds(skippedCountIds)
                .successMigratedToMysqlIds(successMigratedToMysqlIds)
                .totalProcessedIds(totalProcessedIds)
            .build();
    }
//
//    @Override
//    @Transactional(rollbackFor = Exception.class)
//    public Long migrateSingleJob(PgJobEntity pgJob) {
//        log.debug("开始迁移单个 Job，PostgreSQL ID: {}", pgJob.getId());
//
//        try {
//            // 检查是否已存在映射关系
//            if (isJobMigrated(pgJob.getId())) {
//                log.debug("Job ID {} 已存在映射关系，跳过迁移", pgJob.getId());
//                Optional<DataMigrationMappingEntity> mapping = dataMigrationMappingService
//                    .findByPgsqlIdAndType(pgJob.getId().toString(), MigrationBusTypeEnum.JOB);
//                return mapping.map(DataMigrationMappingEntity::getMysqlId).orElse(null);
//            }
//
//            return doMigrateSingleJob(pgJob);
//
//        } catch (Exception e) {
//            log.error("迁移单个 Job 失败，PostgreSQL Job ID: {}", pgJob.getId(), e);
//            return null;
//        }
//    }
//
//    @Override
//    @Transactional(rollbackFor = Exception.class)
//    public Long forceMigrateSingleJob(PgJobEntity pgJob) {
//        log.debug("强制重新迁移单个 Job，PostgreSQL ID: {}", pgJob.getId());
//
//        try {
//            // 删除已存在的映射关系
//            dataMigrationMappingService.deleteByPgsqlIdAndType(
//                pgJob.getId().toString(), MigrationBusTypeEnum.JOB);
//
//            return doMigrateSingleJob(pgJob);
//
//        } catch (Exception e) {
//            log.error("强制迁移单个 Job 失败，PostgreSQL Job ID: {}", pgJob.getId(), e);
//            return null;
//        }
//    }

    /**
     * 执行单个 Job 的实际迁移逻辑
     */
    public Long doMigrateSingleJob(MigrateJobDTO migrateJobDTO) {
        PgJobEntity pgJob = migrateJobDTO.getPgJob();
        Map<Integer, CompanyInfoDTO> companyInfoMap = migrateJobDTO.getCompanyInfoMap();
        Map<String, Long> userIdMap = migrateJobDTO.getUserIdMap();
        Map<String, String> userNameEmail = migrateJobDTO.getUserNameEmail();
        String details = pgJob.getDetails();
        // 1. 解析 JSONB details 字段
        if (StringUtils.isNotBlank(details) && Strings.CS.startsWith(details, "\"") && Strings.CS.endsWith(details, "\"")) {
            details = Strings.CS.removeStart(details, "\"");
            details = Strings.CS.removeEnd(details, "\"");
            details = Strings.CS.replace(details, "\\\"", "\"");
        }
        JobDetailsDTO jobDetails = parseJobDetails(details);

        // 2. 转换为 MySQL JobEntity
        JobEntity jobEntity = jobMigrationConvert.convertToJobEntity(pgJob);
        // 设置companyCode
        Integer companyId = pgJob.getCompanyId();
        CompanyInfoDTO companyInfoDTO = companyInfoMap.get(companyId);
        if (StringUtils.isBlank(companyInfoDTO.getCompanyCode())) {
            log.warn("company is not found company id {} pgJob {}", companyId, JsonUtils.toJson(pgJob));
            return null;
        }
        jobEntity.setCompanyCode(companyInfoDTO.getCompanyCode());

        //设置companyName
        String companyName = PG_COMPANY_ID_NAME_MAP.get(pgJob.getCompanyId());
        companyName = MoreObjects.firstNonNull(companyInfoDTO.getCompanyName(), companyName);
        //设置urlCode
        String cleanCompanyName = CommonUtils.cleanInputReplace(companyName, companyNameReplace);
        String title = CommonUtils.cleanInputReplace(jobEntity.getTitle(), companyNameReplace);
        String urlCode = CommonUtils.join(cleanCompanyName, title);
        jobEntity.setUrlCode(urlCode);
        //设置 companyTitleHash
        String companyTitleHash = HashUtils.murmur3Hash(urlCode);
        jobEntity.setCompanyTitleHash(companyTitleHash);
        Long createdBy = userIdMap.get(pgJob.getCreatedBy());
        if (createdBy == null) {
            log.warn("createdBy is not found createdBy id {} pgJob {}", pgJob.getCreatedBy(), JsonUtils.toJson(pgJob));
            return null;
        }
        //设置createdBy
        jobEntity.setCreateBy(createdBy);
        //设置updatedBy
        Long updatedBy = userIdMap.getOrDefault(pgJob.getCreatedBy(), createdBy);
        jobEntity.setUpdateBy(updatedBy);
        //设置createUser
        jobEntity.setCreateUser(userNameEmail.get(pgJob.getCreatedBy()));

        // 3. 保存到 MySQL
        log.info("job entity {}", jobEntity);
        jobService.save(jobEntity); // MyBatis Plus 会自动回填 ID

        Long mysqlJobId = jobEntity.getId();
        log.debug("Job 保存到 MySQL 成功，MySQL ID: {}", mysqlJobId);

        // 4. 保存映射关系
        boolean mappingSaved = dataMigrationMappingService.saveMapping(
            pgJob.getId().toString(), mysqlJobId, MigrationBusTypeEnum.JOB);

        if (!mappingSaved) {
            log.warn("保存映射关系失败，PostgreSQL Job ID: {}, MySQL Job ID: {}",
                pgJob.getId(), mysqlJobId);
        }

        // 5. 转换为 ES 实体并同步到 Elasticsearch
        JobEsEntity jobEsEntity = jobMigrationConvert.convertToJobEsEntity(jobEntity, pgJob, jobDetails);
        jobEsEntity.setId(mysqlJobId); // ES ID 使用 MySQL ID
        // 需要设置customerName
        jobEsEntity.setCustomerName(companyName);
        // 设置locations
        setJobEsLocations(jobEsEntity, pgJob, migrateJobDTO);

        jobEsService.saveJobToEs(jobEsEntity);
        log.info("Job Elasticsearch {}", jobEsEntity);
        log.debug("Job 同步到 Elasticsearch 成功，ES ID: {}", mysqlJobId);

        POOL_EXECUTOR.execute(() -> {
            long start = System.currentTimeMillis();
            InterviewResultVO interviewResultVO = this.createAIInterview(jobEsEntity);
            log.info("createAIInterview jobId:{},time:{},结果:{}", jobEsEntity.getId(), System.currentTimeMillis() - start, interviewResultVO);

            jobService.updateInterviewUrlId(jobEsEntity.getId(), interviewResultVO.getUrlId());
        });
        //异步生成 feed xml
        POOL_EXECUTOR.execute(() -> {
            xmlFeedConfigService.jobUpdate(jobEsEntity.getCompanyCode(),Long.valueOf(jobEsEntity.getCreateBy()));
        });

        return mysqlJobId;
    }

    private InterviewResultVO createAIInterview(JobEsEntity jobCreateBO){
        //创建ai面试
        InterviewRequestDTO interviewRequestDTO = new InterviewRequestDTO();
        InterviewDataDTO interviewDataDTO = new InterviewDataDTO();
        QuestionGenerationDTO questionGeneration =  new QuestionGenerationDTO();
        interviewRequestDTO.setOrganizationName(aiInterviewConfig.getOrganizationName());
        interviewRequestDTO.setInterviewData(interviewDataDTO);
        interviewRequestDTO.setQuestionGeneration(questionGeneration);
        interviewRequestDTO.setCustomQuestions(jobCreateBO.getCustomQuestions());
        interviewDataDTO.setInterviewerId(aiInterviewConfig.getInterviewerId());
        interviewDataDTO.setLogoUrl(aiInterviewConfig.getLogoUrl());
        interviewDataDTO.setIsAnonymous(aiInterviewConfig.getIsAnonymous());
        interviewDataDTO.setUserId(aiInterviewConfig.getUserId());
        interviewDataDTO.setOrganizationId(aiInterviewConfig.getOrganizationId());
        interviewDataDTO.setResponseCount(aiInterviewConfig.getResponseCount());
        interviewDataDTO.setTimeDuration(jobCreateBO.getInterviewLength());
        interviewDataDTO.setName(jobCreateBO.getTitle());
        interviewDataDTO.setDescription(jobCreateBO.getJobDetail());
        interviewDataDTO.setCanChangeInterviewLanguage(true);
        interviewDataDTO.setEnableWrittenTest(false);
        CountryDTO countryDTO = locationService.getCountryByCityId(jobCreateBO.getLocations().getFirst().getCityId());
        if (countryDTO != null) interviewDataDTO.setObjective(countryDTO.getName());
        questionGeneration.setDimensions(jobCreateBO.getSkills());
        questionGeneration.setNumber(aiInterviewConfig.getQuestionNumber());
        return aiService.createInterview(interviewRequestDTO, JobConvert.INSTANCE.toJobCreateBoFromEntity(jobCreateBO));
    }

    @Override
    public boolean isJobMigrated(Integer pgJobId) {
        return dataMigrationMappingService.existsByPgsqlIdAndType(
            pgJobId.toString(), JOB);
    }
//
//    @Override
//    public JobMigrationResponseDTO getMigrationStatistics() {
//        log.info("获取 Job 迁移统计信息");
//
//        try {
//            // 统计映射关系数量
//            long migratedCount = dataMigrationMappingService.countByType(MigrationBusTypeEnum.JOB);
//
//            // 统计 PostgreSQL 总数
//            long totalPgJobs = pgJobService.getTotalJobCount(null, null);
//
//            double progressPercentage = totalPgJobs > 0 ? (double) migratedCount / totalPgJobs * 100 : 0;
//
//            String message = String.format("已迁移: %d/%d (%.2f%%)",
//                migratedCount, totalPgJobs, progressPercentage);
//
//            return JobMigrationResponseDTO.builder()
//                .totalProcessed(totalPgJobs) // 假设总处理数就是 PostgreSQL 总数
//                .successMigratedToMysql(migratedCount)
//                .skippedCount(0) // 统计信息中不区分跳过
//                .failedCount(0) // 统计信息中不区分失败
//                .status("SUCCESS") // 统计信息总是成功
//                .message(message)
//                .build();
//        } catch (Exception e) {
//            log.error("获取迁移统计信息失败", e);
//            return JobMigrationResponseDTO.builder()
//                .status("FAILED")
//                .message("获取迁移统计信息失败: " + e.getMessage())
//                .build();
//        }
//    }
//
//    @Override
//    public boolean validateMigratedJob(Integer pgJobId) {
//        log.debug("验证迁移数据完整性，PostgreSQL Job ID: {}", pgJobId);
//
//        try {
//            // 1. 检查映射关系是否存在
//            Optional<DataMigrationMappingEntity> mappingOpt = dataMigrationMappingService
//                .findByPgsqlIdAndType(pgJobId.toString(), MigrationBusTypeEnum.JOB);
//
//            if (mappingOpt.isEmpty()) {
//                log.warn("映射关系不存在，PostgreSQL Job ID: {}", pgJobId);
//                return false;
//            }
//
//            Long mysqlJobId = mappingOpt.get().getMysqlId();
//
//            // 2. 检查 MySQL 中是否存在
//            JobEntity mysqlJob = jobService.getById(mysqlJobId);
//            if (mysqlJob == null) {
//                log.warn("MySQL 中 Job 不存在，MySQL ID: {}", mysqlJobId);
//                return false;
//            }
//
//            // 3. 检查 Elasticsearch 中是否存在
//            JobEsEntity esJob = jobEsService.getJobById(mysqlJobId);
//            if (esJob == null) {
//                log.warn("Elasticsearch 中 Job 不存在，ES ID: {}", mysqlJobId);
//                return false;
//            }
//
//            log.debug("Job ID {} 迁移数据验证成功", pgJobId);
//            return true;
//        } catch (Exception e) {
//            log.error("验证迁移数据完整性失败，PostgreSQL Job ID: {}", pgJobId, e);
//            return false;
//        }
//    }
//
//    @Override
//    @Transactional(rollbackFor = Exception.class)
//    public int cleanupFailedMigrations() {
//        log.info("开始清理失败的迁移记录");
//
//        int cleanedCount = 0;
//
//        try {
//            // 获取所有 Job 类型的映射关系
//            List<DataMigrationMappingEntity> mappings = dataMigrationMappingService
//                .findAllByType(MigrationBusTypeEnum.JOB);
//
//            List<String> toDelete = new ArrayList<>();
//
//            for (DataMigrationMappingEntity mapping : mappings) {
//                Long mysqlJobId = mapping.getMysqlId();
//                // 检查 MySQL 中是否存在对应的 Job
//                if (mysqlJobId != null && jobService.getById(mysqlJobId) == null) {
//                    // 如果 MySQL 中不存在，则认为该映射关系是孤立的，需要清理
//                    toDelete.add(mapping.getPgsqlId());
//                    cleanedCount++;
//                }
//            }
//
//            // 批量删除孤立的映射关系
//            for (String pgsqlId : toDelete) {
//                dataMigrationMappingService.deleteByPgsqlIdAndType(pgsqlId, MigrationBusTypeEnum.JOB);
//            }
//
//            log.info("清理完成，删除了 {} 条孤立的映射关系", cleanedCount);
//
//        } catch (Exception e) {
//            log.error("清理失败的迁移记录时发生异常", e);
//        }
//
//        return cleanedCount;
//    }


    /**
     * 解析 JSONB details 字段
     */
    private JobDetailsDTO parseJobDetails(String detailsJson) {
        if (detailsJson == null || detailsJson.trim().isEmpty()) {
            return new JobDetailsDTO();
        }

        try {
            return JsonUtils.toObject(detailsJson, JobDetailsDTO.class);
        } catch (Exception e) {
            log.warn("解析 Job details 失败: {}", e.getMessage());
            return new JobDetailsDTO();
        }
    }

    /**
     * 预处理位置信息映射
     * 
     * 根据 Job 列表中的 locationId，批量查询 PostgreSQL location 信息，
     * 并将 country、city、state 信息映射到 MySQL 对应的 ID。
     *                 //获取jobs中的locationId去重 得到locationIds
     *                 //通过locationIds获取location信息
     *                 //通过location的country属性 判断com.item.dto.migration.job.MigrateJobDTO.countryMap中是否存在这个key 不存在通过countryMapper的iso2属性查询 并且put到com.item.dto.migration.job.MigrateJobDTO.countryMap
     *                 //通过location的city属性 判断com.item.dto.migration.job.MigrateJobDTO.cityMap中是否存在这个key 不存在通过cityMapper的name属性查询 并且put到com.item.dto.migration.job.MigrateJobDTO.cityMap
     *                 //通过location的state属性 判断com.item.dto.migration.job.MigrateJobDTO.stateMap中是否存在这个key 不存在通过stateMapper的name属性查询 并且put到com.item.dto.migration.job.MigrateJobDTO.stateMap
     * @param jobs Job 列表
     * @param migrateJobDTO 迁移数据传输对象，包含各种映射关系
     */
    private void preprocessLocationMappings(List<PgJobEntity> jobs, MigrateJobDTO migrateJobDTO) {
        if (jobs == null || jobs.isEmpty()) {
            log.debug("preprocessLocationMappings: jobs list is empty");
            return;
        }

        try {
            // 1. 获取 jobs 中的 locationId 去重，得到 locationIds
            List<Integer> locationIds = jobs.stream()
                .map(PgJobEntity::getLocationId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

            if (locationIds.isEmpty()) {
                log.debug("preprocessLocationMappings: no valid locationIds found");
                return;
            }

            log.debug("preprocessLocationMappings: processing {} unique location IDs: {}", 
                    locationIds.size(), locationIds);

            // 2. 通过 locationIds 批量获取 location 信息
            List<PgLocationEntity> locations = pgLocationService.getLocationsByIds(locationIds);
            
            if (locations.isEmpty()) {
                log.warn("preprocessLocationMappings: no locations found for IDs: {}", locationIds);
                return;
            }

            log.debug("preprocessLocationMappings: found {} locations", locations.size());

            // 3. 处理每个 location 的 country、city、state 映射
            for (PgLocationEntity location : locations) {
                processCountryMapping(location, migrateJobDTO);
                processStateMapping(location, migrateJobDTO);
                processCityMapping(location, migrateJobDTO);
                migrateJobDTO.getPgLocationMap().computeIfAbsent(location.getId(), locationId -> location);
            }

            log.info("preprocessLocationMappings completed: processed {} locations, " +
                    "countryMap size: {}, cityMap size: {}, stateMap size: {}",
                    locations.size(), 
                    migrateJobDTO.getCountryMap().size(),
                    migrateJobDTO.getCityMap().size(),
                    migrateJobDTO.getStateMap().size());

        } catch (Exception e) {
            log.error("preprocessLocationMappings failed", e);
        }
    }

    /**
     * 处理国家映射
     * 
     * @param location PostgreSQL location 实体
     * @param migrateJobDTO 迁移数据传输对象
     */
    private void processCountryMapping(PgLocationEntity location, MigrateJobDTO migrateJobDTO) {
        String countryName = location.getCountry();
        if (StringUtils.isBlank(countryName)) {
            return;
        }

        // 检查 countryMap 中是否已存在这个 key
        if (migrateJobDTO.getCountryMap().containsKey(Utils.getCountryKey(countryName))) {
            return;
        }

        try {
            // 通过 countryMapper 的 iso2 属性查询
            LambdaQueryWrapper<CountryEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(CountryEntity::getIso2, countryName)
                       .last(LIMIT_ONE);
            
            CountryEntity countryEntity = countryMapper.selectOne(queryWrapper);
            
            if (countryEntity != null) {
                // 将查询结果 put 到 countryMap
                migrateJobDTO.getCountryMap().put(Utils.getCountryKey(countryName), countryEntity.getId());
                migrateJobDTO.getCountryNameMap().put(Utils.getCountryKey(countryName), countryEntity.getName());
                log.debug("Country mapping added: {} -> {}", Utils.getCountryKey(countryName), countryEntity.getId());
            } else {
                log.warn("Country not found in MySQL for name: {}", Utils.getCountryKey(countryName));
            }
        } catch (Exception e) {
            log.error("Failed to process country mapping for: {}", Utils.getCountryKey(countryName), e);
        }
    }

    /**
     * 处理城市映射
     * 
     * @param location PostgreSQL location 实体
     * @param migrateJobDTO 迁移数据传输对象
     */
    private void processCityMapping(PgLocationEntity location, MigrateJobDTO migrateJobDTO) {
        String cityName = location.getCity();
        cityName = CITY_MAP.getOrDefault(cityName, cityName);
        if (StringUtils.isBlank(cityName)) {
            return;
        }

        Long countryId = migrateJobDTO.getCountryMap().get(Utils.getCountryKey(location.getCountry()));
        Long stateId = migrateJobDTO.getStateMap().get(Utils.getStateKey(location.getState(), countryId));
        // 检查 cityMap 中是否已存在这个 key
        if (migrateJobDTO.getCityMap().containsKey(Utils.getCityKey(cityName, stateId, countryId))) {
            return;
        }
        try {
            // 通过 cityMapper 的 name 属性查询
            LambdaQueryWrapper<CityEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(CityEntity::getName, cityName)
                    .eq(CityEntity::getStateId, stateId)
                    .eq(CityEntity::getCountryId, countryId)
                       .last(LIMIT_ONE);
            
            CityEntity cityEntity = cityMapper.selectOne(queryWrapper);
            
            if (cityEntity != null) {
                // 将查询结果 put 到 cityMap
                migrateJobDTO.getCityMap().put(Utils.getCityKey(cityName, stateId, countryId), cityEntity.getId());
                log.debug("City mapping added: {} -> {}", Utils.getCityKey(cityName, stateId, countryId), cityEntity.getId());
            } else {
                log.warn("City not found in MySQL for name: {}", Utils.getCityKey(cityName, stateId, countryId));
            }
        } catch (Exception e) {
            log.error("Failed to process city mapping for: {}", Utils.getCityKey(cityName, stateId, countryId), e);
        }
    }

    /**
     * 处理州/省映射
     * 
     * @param location PostgreSQL location 实体
     * @param migrateJobDTO 迁移数据传输对象
     */
    private void processStateMapping(PgLocationEntity location, MigrateJobDTO migrateJobDTO) {
        String stateName = location.getState();
        stateName = STATE_MAP.getOrDefault(stateName, stateName);
        if (StringUtils.isBlank(stateName)) {
            return;
        }
        Long countryId = migrateJobDTO.getCountryMap().get(location.getCountry());
        // 检查 stateMap 中是否已存在这个 key
        if (migrateJobDTO.getStateMap().containsKey(Utils.getStateKey(stateName, countryId))) {
            return;
        }

        try {
            // 通过 stateMapper 的 name 属性查询
            LambdaQueryWrapper<StateEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(StateEntity::getName, stateName)
                    .eq(StateEntity::getCountryId, countryId)
                       .last(LIMIT_ONE);
            
            StateEntity stateEntity = stateMapper.selectOne(queryWrapper);
            
            if (stateEntity != null) {
                // 将查询结果 put 到 stateMap
                migrateJobDTO.getStateMap().put(Utils.getStateKey(stateName, countryId), stateEntity.getId());
                log.debug("State mapping added: {} -> {}", Utils.getStateKey(stateName, countryId), stateEntity.getId());
            } else {
                log.warn("State not found in MySQL for name: {}", Utils.getStateKey(stateName, countryId));
            }
        } catch (Exception e) {
            log.error("Failed to process state mapping for: {}", Utils.getStateKey(stateName, countryId), e);
        }
    }

    /**
     * 设置 JobEs 实体的位置信息
     * 
     * 根据 PostgreSQL Job 的 locationId，从预处理的映射数据中获取对应的 MySQL location 信息，
     * 并设置到 JobEsEntity 的 locations 字段中。
     * 
     * @param jobEsEntity JobEs 实体
     * @param pgJob PostgreSQL Job 实体
     * @param migrateJobDTO 迁移数据传输对象，包含各种映射关系
     */
    private void setJobEsLocations(JobEsEntity jobEsEntity, PgJobEntity pgJob, MigrateJobDTO migrateJobDTO) {
        if (pgJob.getLocationId() == null) {
            log.debug("setJobEsLocations: pgJob locationId is null, skipping location setup");
            return;
        }

        try {
            // 获取各种映射表
            Map<String, Long> countryMap = migrateJobDTO.getCountryMap();
            Map<String, String> countryNameMap = migrateJobDTO.getCountryNameMap();
            Map<String, Long> stateMap = migrateJobDTO.getStateMap();
            Map<String, Long> cityMap = migrateJobDTO.getCityMap();
            Map<Integer, PgLocationEntity> pgLocationMap = migrateJobDTO.getPgLocationMap();

            // 获取 PostgreSQL location 实体
            PgLocationEntity pgLocationEntity = pgLocationMap.get(pgJob.getLocationId());
            if (pgLocationEntity == null) {
                log.warn("setJobEsLocations: PgLocationEntity not found for locationId: {}", pgJob.getLocationId());
                return;
            }

            // 获取映射后的 MySQL IDs 和名称
            Long countryId = countryMap.get(Utils.getCountryKey(pgLocationEntity.getCountry()));
            String countryName = countryNameMap.get(Utils.getCountryKey(pgLocationEntity.getCountry()));
            Long stateId = stateMap.get(Utils.getStateKey(pgLocationEntity.getState(), countryId));
            Long cityId = cityMap.get(Utils.getCityKey(pgLocationEntity.getCity(), stateId, countryId));

            // 创建 LocationValRecordDTO 并设置各字段
            LocationValRecordDTO locationValRecordDTO = new LocationValRecordDTO();
            locationValRecordDTO.setCountryId(countryId);
            locationValRecordDTO.setCountryName(countryName);
            locationValRecordDTO.setStateId(stateId);
            locationValRecordDTO.setStateName(pgLocationEntity.getState());
            locationValRecordDTO.setCityName(pgLocationEntity.getCity());
            locationValRecordDTO.setCityId(cityId);
            
            // 生成完整的位置名称
            String locationName = CommonUtils.getLocationName(countryName, pgLocationEntity.getState(), pgLocationEntity.getCity());
            locationValRecordDTO.setLocationName(locationName);

            // 设置到 JobEsEntity
            jobEsEntity.setLocations(List.of(locationValRecordDTO));

            log.debug("setJobEsLocations completed for Job ID: {}, Location: {}", 
                    pgJob.getId(), locationName);

        } catch (Exception e) {
            log.error("setJobEsLocations failed for Job ID: {}, LocationId: {}", 
                    pgJob.getId(), pgJob.getLocationId(), e);
        }
    }

    /**
     * 处理单页Job数据
     * 
     * @param pageNum 页码
     * @param batchSize 批次大小
     * @param request 迁移请求
     * @param totalProcessedIds 总处理ID集合
     * @param successMigratedToMysqlIds 成功迁移到MySQL的ID集合
     * @param skippedCountIds 跳过的ID集合
     * @param failedCountIds 失败的ID集合
     * @return 批处理结果
     */
    @Transactional(rollbackFor = Exception.class)
    public BatchProcessResult processSinglePageJobs(int pageNum, int batchSize, JobMigrationRequestDTO request,
                                                   Set<Integer> totalProcessedIds, Set<Integer> successMigratedToMysqlIds,
                                                   Set<Integer> skippedCountIds, Set<Integer> failedCountIds) {
        log.info("处理第 {} 页数据，每页 {} 条记录", pageNum, batchSize);

        try {
            // 使用 PgJobService 进行分页查询
            IPage<PgJobEntity> jobPage = pgJobService.getJobsWithPagination(
                pageNum, batchSize, request.getStartPgJobId(), request.getEndPgJobId());

            List<PgJobEntity> jobs = jobPage.getRecords();
            if (jobs.isEmpty()) {
                return new BatchProcessResult(0, 0, 0, 0, false);
            }

            MigrateJobDTO migrateJobDTO = new MigrateJobDTO();
            //填充location信息
            preprocessLocationMappings(jobs, migrateJobDTO);
            //填充company信息
            preprocessCompanyMapping(jobs, migrateJobDTO);
            //填充user信息
            preprocessUserInfoMapping(jobs, migrateJobDTO);

            int processedCount = 0;
            int successCount = 0;
            int skippedCount = 0;
            int failedCount = 0;

            // 批量处理当前页的数据
            for (PgJobEntity pgJob : jobs) {
                processedCount++;
                totalProcessedIds.add(pgJob.getId());
                
                try {
                    // 检查是否已存在映射关系
                    if (!request.getForceMigrate() && isJobMigrated(pgJob.getId())) {
                        skippedCount++;
                        skippedCountIds.add(pgJob.getId());
                        log.debug("Job ID {} 已存在映射关系，跳过迁移", pgJob.getId());
                        continue;
                    }
                    
                    migrateJobDTO.setPgJob(pgJob);
                    Long mysqlJobId = doMigrateSingleJob(migrateJobDTO);

                    if (mysqlJobId != null) {
                        successCount++;
                        successMigratedToMysqlIds.add(pgJob.getId());
                    } else {
                        failedCount++;
                        failedCountIds.add(pgJob.getId());
                    }
                } catch (Exception e) {
                    failedCount++;
                    failedCountIds.add(pgJob.getId());
                    log.error("迁移 Job 失败，PostgreSQL Job ID: {}", pgJob.getId(), e);
                }
            }

            // 检查是否还有更多数据
            boolean hasMoreData = (jobPage.getCurrent() * jobPage.getSize()) < jobPage.getTotal();

            return new BatchProcessResult(processedCount, successCount, skippedCount, failedCount, hasMoreData);

        } catch (Exception e) {
            log.error("处理第 {} 页数据时发生异常", pageNum, e);
            return new BatchProcessResult(0, 0, 0, 0, false);
        }
    }

    /**
     * 处理B端租户
     *
     * @param jobs
     * @param migrateJobDTO
     */
    private void preprocessUserInfoMapping(List<PgJobEntity> jobs, MigrateJobDTO migrateJobDTO) {
        List<String> userUUIDIds = jobs.stream().flatMap(s -> Stream.of(s.getCreatedBy(), s.getUpdatedBy())).filter(StringUtils::isNotBlank).distinct().toList();
        List<DataMigrationMappingEntity> byPgsqlIdsAndType = dataMigrationMappingService.findByPgsqlIdsAndType(userUUIDIds, MigrationBusTypeEnum.RECRUIT);
        Map<String, Long> userIdMap = migrateJobDTO.getUserIdMap();
        Map<String, String> userNameEmail = migrateJobDTO.getUserNameEmail();
        for (DataMigrationMappingEntity dataMigrationMappingEntity : byPgsqlIdsAndType) {
            String pgsqlId = dataMigrationMappingEntity.getPgsqlId();
            userIdMap.computeIfAbsent(pgsqlId, v -> dataMigrationMappingEntity.getMysqlId());
            userNameEmail.computeIfAbsent(pgsqlId, v -> dataMigrationMappingEntity.getExt());
        }
    }

    /**
     * 处理租户公司信息
     *
     * @param jobs
     * @param migrateJobDTO
     */
    private void preprocessCompanyMapping(List<PgJobEntity> jobs, MigrateJobDTO migrateJobDTO) {
        List<String> companyId = jobs.stream().map(PgJobEntity::getCompanyId).distinct().map(Object::toString).toList();
        List<DataMigrationMappingEntity> byPgsqlIdsAndType = dataMigrationMappingService.findByPgsqlIdsAndType(companyId, MigrationBusTypeEnum.COMPANY_DATA);
        Map<Integer, CompanyInfoDTO> companyInfoMap = migrateJobDTO.getCompanyInfoMap();
        for (DataMigrationMappingEntity dataMigrationMappingEntity : byPgsqlIdsAndType) {
            if (dataMigrationMappingEntity != null && StringUtils.isNotBlank(dataMigrationMappingEntity.getExt())) {
                companyInfoMap.computeIfAbsent(Integer.valueOf(dataMigrationMappingEntity.getPgsqlId()), id -> JsonUtils.toObject(dataMigrationMappingEntity.getExt(), CompanyInfoDTO.class));
            }
        }
    }

    /**
     * 批处理结果内部类
     */
    @Data
    public static class BatchProcessResult {
        private final int processedCount;
        private final int successCount;
        private final int skippedCount;
        private final int failedCount;
        private final boolean hasMoreData;

        public BatchProcessResult(int processedCount, int successCount, int skippedCount, 
                                int failedCount, boolean hasMoreData) {
            this.processedCount = processedCount;
            this.successCount = successCount;
            this.skippedCount = skippedCount;
            this.failedCount = failedCount;
            this.hasMoreData = hasMoreData;
        }

        public int getProcessedCount() {
            return processedCount;
        }

        public int getSuccessCount() {
            return successCount;
        }

        public int getSkippedCount() {
            return skippedCount;
        }

        public int getFailedCount() {
            return failedCount;
        }

        public boolean isHasMoreData() {
            return hasMoreData;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public JobMigrationResponseDTO migrateAyrshareCompanyConfig() {
        long startTimeMillis = System.currentTimeMillis();
        long totalProcessed = 0;
        long successCount = 0;
        long skippedCount = 0;
        long failedCount = 0;

        try {
            log.info("开始迁移 Ayrshare 公司配置数据");

            // 1. 获取 PostgreSQL companies.company_data 所有数据
            List<PgCompanyEntity> pgCompanies = queryAllPgCompanies();
            
            if (pgCompanies.isEmpty()) {
                log.info("PostgreSQL 中未找到公司数据，迁移结束");
                return buildMigrationResponse(STATUS_SUCCESS, "未找到需要迁移的公司数据", 
                    0, 0, 0, 0);
            }

            log.info("从 PostgreSQL 找到 {} 条公司数据，开始处理", pgCompanies.size());

            // 2. 提取所有公司ID集合
            List<String> pgCompanyIds = pgCompanies.stream()
                .map(company -> String.valueOf(company.getId()))
                .toList();

            // 3. 根据 postgres.companies.company_data.id 集合和 bustype=8 从 recruit.r_data_migration_pgsql_mysql_id 查询所有数据
            List<DataMigrationMappingEntity> companyMappings = dataMigrationMappingService
                .findByPgsqlIdsAndType(pgCompanyIds, MigrationBusTypeEnum.COMPANY_DATA);
            
            log.info("找到 {} 条公司映射记录（bustype=8）", companyMappings.size());

            // 3.1 获取所有 postgres.companies.company_data.id 集合和 bustype=9 profileKey类型的迁移记录
            List<DataMigrationMappingEntity> profileKeyMappings = dataMigrationMappingService
                .findByPgsqlIdsAndType(pgCompanyIds, MigrationBusTypeEnum.COMPANY_PROFILE_KEY);
            
            log.info("找到 {} 条公司 ProfileKey 映射记录（bustype=9）", profileKeyMappings.size());
            profileKeyMappings = profileKeyMappings.stream().filter(s -> s.getMysqlId() != null&& s.getMysqlId() != 0).toList();
            log.info("找到 {} 条公司 ProfileKey 映射记录（bustype=9）", profileKeyMappings.size());

            // 4. 构建 PostgreSQL ID 到映射记录的映射表，便于快速查找
            Map<String, DataMigrationMappingEntity> mappingMap = companyMappings.stream()
                .collect(Collectors.toMap(
                    DataMigrationMappingEntity::getPgsqlId,
                    mapping -> mapping
                ));

            // 4.1 构建 ProfileKey 映射表
            Map<String, DataMigrationMappingEntity> profileKeyMappingMap = profileKeyMappings.stream()
                .collect(Collectors.toMap(
                    DataMigrationMappingEntity::getPgsqlId,
                    mapping -> mapping
                ));

            // 5. 以 pgCompanies 集合循环处理
            for (PgCompanyEntity pgCompany : pgCompanies) {
                totalProcessed++;
                
                try {
                    // 检查是否有 ayrshare_profile_key
                    if (StringUtils.isBlank(pgCompany.getAyrshareProfileKey())) {
                        log.debug("PostgreSQL 公司 {} 无 ayrshare_profile_key，跳过", pgCompany.getId());
                        skippedCount++;
                        continue;
                    }

                    // 6. 从映射表中查找对应的映射记录
                    String pgCompanyId = String.valueOf(pgCompany.getId());
                    
                    // 6.2 从 bustype=8 映射表中查找公司映射记录
                    DataMigrationMappingEntity mapping = mappingMap.get(pgCompanyId);
                    
                    if (mapping == null) {
                        log.debug("PostgreSQL 公司 {} 没有找到映射记录（bustype=8），跳过", pgCompanyId);
                        skippedCount++;
                        continue;
                    }

                    // 6.1 检查是否存在 bustype=9 的 ProfileKey 映射记录
                    DataMigrationMappingEntity profileKeyMapping = profileKeyMappingMap.get(pgCompanyId);
                    if (profileKeyMapping == null) {
                        // 不存在则创建新的 ProfileKey 映射记录
                        log.info("PostgreSQL 公司 {} 没有 ProfileKey 映射记录（bustype=9），创建新记录", pgCompanyId);
                        profileKeyMapping = createProfileKeyMapping(pgCompany);
                        if (profileKeyMapping != null) {
                            profileKeyMappingMap.put(pgCompanyId, profileKeyMapping);
                        }
                    }

                    // 7. 解析 companyCode 信息
                    String companyCode = extractCompanyCodeFromMapping(mapping);
                    if (StringUtils.isBlank(companyCode)) {
                        log.warn("无法从映射记录中解析 companyCode，跳过 PostgreSQL 公司：{}", pgCompanyId);
                        skippedCount++;
                        continue;
                    }

                    // 8. 判断 recruit.r_ayrshare_company_config 中是否已经存在
                    AyrshareCompanyConfigEntity existingConfig = ayrshareCompanyConfigService
                        .findByCompanyCode(companyCode);
                    
                    if (existingConfig != null) {
                        log.debug("公司 {} 的 Ayrshare 配置已存在，跳过", companyCode);
                        skippedCount++;
                        continue;
                    }

                    // 9. 不存在，执行插入操作
                    // 按照 null,750500AE-66124789-BF48A16C-2A445453,id-7f3ce,ayrshare_profile_key,recruit,0,system_migration,0,system_migration,companyCode,0,当前时间,当前时间
                    AyrshareCompanyConfigEntity newConfig = buildAyrshareConfig(pgCompany, companyCode);
                    ayrshareCompanyConfigService.saveAyrShareConfig(newConfig);
                    
                    log.info("成功迁移公司 {} (PostgreSQL ID: {}) 的 Ayrshare 配置，MySQL ID: {}", 
                        companyCode, pgCompanyId, newConfig.getId());
                    
                    // 10. 更新 ProfileKey 映射记录的 mysqlId
                    if (profileKeyMapping != null && newConfig.getId() != null) {
                        profileKeyMapping.setMysqlId(newConfig.getId());
                        boolean updated = dataMigrationMappingService.updateById(profileKeyMapping);
                        if (updated) {
                            log.info("成功更新 ProfileKey 映射记录的 mysqlId，PostgreSQL 公司 ID: {}, MySQL ID: {}", 
                                pgCompanyId, newConfig.getId());
                        } else {
                            log.warn("更新 ProfileKey 映射记录的 mysqlId 失败，PostgreSQL 公司 ID: {}, MySQL ID: {}", 
                                pgCompanyId, newConfig.getId());
                        }
                    }
                    
                    successCount++;
                    
                } catch (Exception e) {
                    log.error("处理 PostgreSQL 公司 {} 失败", pgCompany.getId(), e);
                    failedCount++;
                }
            }

            long duration = System.currentTimeMillis() - startTimeMillis;
            log.info("Ayrshare 配置迁移完成，总处理: {}, 成功: {}, 跳过: {}, 失败: {}, 耗时: {}ms",
                totalProcessed, successCount, skippedCount, failedCount, duration);

            return buildMigrationResponse(STATUS_SUCCESS, 
                String.format("迁移完成，总处理: %d, 成功: %d, 跳过: %d, 失败: %d", 
                    totalProcessed, successCount, skippedCount, failedCount),
                totalProcessed, successCount, skippedCount, failedCount);

        } catch (Exception e) {
            log.error("Ayrshare 配置迁移发生异常", e);
            return buildMigrationResponse(STATUS_FAILED, "迁移失败: " + e.getMessage(),
                totalProcessed, successCount, skippedCount, failedCount);
        }
    }

    /**
     * 查询 PostgreSQL 所有公司数据
     */
    private List<PgCompanyEntity> queryAllPgCompanies() {
        try {
            return pgCompanyMapper.selectList(null);
        } catch (Exception e) {
            log.error("查询 PostgreSQL 所有公司数据失败", e);
            return new ArrayList<>();
        }
    }

    /**
     * 从映射记录的 ext 字段中解析 companyCode
     */
    private String extractCompanyCodeFromMapping(DataMigrationMappingEntity mapping) {
        try {
            if (StringUtils.isNotBlank(mapping.getExt())) {
                // 假设 ext 字段存储的是 JSON 格式的 CompanyInfoDTO
                CompanyInfoDTO companyInfo = JsonUtils.toObject(mapping.getExt(), CompanyInfoDTO.class);
                return companyInfo != null ? companyInfo.getCompanyCode() : null;
            }
            return null;
        } catch (Exception e) {
            log.warn("解析 companyCode 失败，mapping: {}", mapping, e);
            return null;
        }
    }


    /**
     * 构建 AyrshareCompanyConfigEntity 对象
     */
    private AyrshareCompanyConfigEntity buildAyrshareConfig(PgCompanyEntity pgCompany, String companyCode) {
        AyrshareCompanyConfigEntity config = new AyrshareCompanyConfigEntity();
        
        // 按照用户要求的格式设置字段值
        // null,750500AE-66124789-BF48A16C-2A445453,id-7f3ce,ayrshare_profile_key,recruit,0,system_migration,0,system_migration,companyCode,0,当前时间,当前时间
        config.setId(null); // 自增主键
        config.setAyrshareApiKey("750500AE-66124789-BF48A16C-2A445453"); // 固定API Key
        config.setAyrshareDomain("id-7f3ce"); // 固定domain
        config.setAyrshareProfileKey(pgCompany.getAyrshareProfileKey()); // 从postgres获取
        config.setAyrshareSubreddit("recruit"); // 固定值
        config.setDeleted(false); // 0-未删除
        config.setCreateByName("system_migration"); // 创建人名称
        config.setCreateById(0L); // 创建人ID
        config.setUpdateByName("system_migration"); // 更新人名称  
        config.setUpdateById(0L); // 更新人ID
        config.setCompanyCode(companyCode); // 解析的公司代码
        config.setCreateTime(LocalDateTime.now());
        config.setUpdateTime(LocalDateTime.now());
        // createTime 和 updateTime 由 @FieldFill 自动填充

        return config;
    }

    /**
     * 构建迁移响应对象
     */
    private JobMigrationResponseDTO buildMigrationResponse(String status, String message,
                                                         long totalProcessed, long successCount, 
                                                         long skippedCount, long failedCount) {
        return JobMigrationResponseDTO.builder()
            .status(status)
            .message(message)
            .totalProcessed(totalProcessed)
            .successMigratedToMysql(successCount)
            .successSyncedToEs(0L) // Ayrshare 配置不需要同步到 ES
            .skippedCount(skippedCount)
            .failedCount(failedCount)
            .build();
    }

    /**
     * 创建 ProfileKey 映射记录
     * 
     * @param pgCompany PostgreSQL 公司实体
     * @return 创建的映射记录，失败返回 null
     */
    private DataMigrationMappingEntity createProfileKeyMapping(PgCompanyEntity pgCompany) {
        try {
            String pgCompanyId = String.valueOf(pgCompany.getId());
            
            // 构建 ProfileKey DTO
            CompanyProfileKeyDTO profileKeyDTO = new CompanyProfileKeyDTO();
            profileKeyDTO.setAyrshareProfileKey(pgCompany.getAyrshareProfileKey());
            profileKeyDTO.setLinkedinCompanyId(pgCompany.getLinkedinCompanyId());
            
            // 转换为 JSON 字符串
            String extJson = JsonUtils.toJson(profileKeyDTO);
            
            // 创建映射实体
            DataMigrationMappingEntity mapping = new DataMigrationMappingEntity();
            mapping.setPgsqlId(pgCompanyId);
            mapping.setMysqlId(0L); // 初始为0，后续插入成功后会更新
            mapping.setBusType(MigrationBusTypeEnum.COMPANY_PROFILE_KEY.getCode());
            mapping.setExt(extJson);
            mapping.setCreateTime(LocalDateTime.now());
            
            // 直接保存到数据库，save方法会自动填充实体的ID
            boolean saved = dataMigrationMappingService.save(mapping);
            
            if (saved) {
                log.info("成功创建 ProfileKey 映射记录，PostgreSQL 公司 ID: {}, 映射记录 ID: {}, mysqlId 初始为 0", 
                    pgCompanyId, mapping.getId());
                return mapping;
            } else {
                log.warn("创建 ProfileKey 映射记录失败，PostgreSQL 公司 ID: {}", pgCompanyId);
                return null;
            }
            
        } catch (Exception e) {
            log.error("创建 ProfileKey 映射记录异常，PostgreSQL 公司 ID: {}", pgCompany.getId(), e);
            return null;
        }
    }

}