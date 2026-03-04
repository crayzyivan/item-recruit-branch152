package com.item.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.item.convert.CandidateConverter;
import com.item.dto.CandidateMigrationDTO;
import com.item.dto.iam.FeignResponse;
import com.item.dto.iam.IamCreateUserReqDTO;
import com.item.dto.iam.IamCreateUserResDTO;
import com.item.dto.iam.IamUserDetailDTO;
import com.item.entity.CandidateEducationEntity;
import com.item.entity.CandidateEntity;
import com.item.entity.CandidateEsEntity;
import com.item.entity.CityEntity;
import com.item.entity.CountryEntity;
import com.item.entity.DictionaryEntity;
import com.item.entity.EmploymentHistoryEntity;
import com.item.entity.StateEntity;
import com.item.entity.migration.DataMigrationMappingEntity;
import com.item.es.ResumeEsService;
import com.item.framework.config.IamCommonConfig;
import com.item.framework.config.S3Config;
import com.item.framework.error.BusinessException;
import com.item.mapper.CityMapper;
import com.item.mapper.CountryMapper;
import com.item.mapper.StateMapper;
import com.item.mapper.migration.DataMigrationMappingMapper;
import com.item.pgentity.PgApplication;
import com.item.pgentity.PgCandidateData;
import com.item.pgentity.PgCurrency;
import com.item.pgentity.PgEducationHistory;
import com.item.pgentity.PgEmploymentHistory;
import com.item.pgentity.PgResumeData;
import com.item.pgentity.PgUser;
import com.item.pgmapper.PgApplicationMapper;
import com.item.pgmapper.PgCandidateDataMapper;
import com.item.pgmapper.PgCurrencyMapper;
import com.item.pgmapper.PgEducationHistoryMapper;
import com.item.pgmapper.PgEmploymentHistoryMapper;
import com.item.pgmapper.PgResumeDataMapper;
import com.item.pgmapper.PgUserMapper;
import com.item.service.CandidateDataMigrationService;
import com.item.service.CandidateService;
import com.item.service.DictionaryService;
import com.item.service.client.IamFeignClient;
import com.item.util.S3SecondaryUtils;
import com.item.util.S3Utils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * 应聘者数据迁移服务
 * 从PostgreSQL迁移数据到MySQL和Elasticsearch
 * 
 * @author system
 */
@Service
@ConditionalOnProperty(name = "spring.postgresql.enabled", havingValue = "true", matchIfMissing = false)
@RequiredArgsConstructor
@Slf4j
public class CandidateDataMigrationServiceImpl implements CandidateDataMigrationService {

    private final PgCandidateDataMapper pgCandidateDataMapper;
    private final PgUserMapper pgUserMapper;
    private final PgEducationHistoryMapper pgEducationHistoryMapper;
    private final PgEmploymentHistoryMapper pgEmploymentHistoryMapper;
    private final PgResumeDataMapper pgResumeDataMapper;
    private final PgApplicationMapper pgApplicationMapper;
    private final PgCurrencyMapper pgCurrencyMapper;
    private final ResumeEsService resumeEsService;
    private final CandidateService candidateService;
    private final DictionaryService dictionaryService;
    private final CountryMapper countryMapper;
    private final StateMapper stateMapper;
    private final CityMapper cityMapper;
    private final S3Utils s3Utils;
    private final S3SecondaryUtils s3SecondaryUtils;
    private final DataMigrationMappingMapper dataMigrationMappingMapper;
    private final IamFeignClient iamFeignClient;
    private final IamCommonConfig iamCommonConfig;
    private final CandidateConverter candidateConverter;
    private final S3Config s3Config;
    private final ObjectMapper objectMapper;
    private static final String FILE_SEPARATOR = "/";

    // 线程池配置
    private static final int THREAD_POOL_SIZE = 10; // 线程池大小
    /**
     * 迁移所有应聘者数据（使用线程池并发处理）
     */
    @Override
    public void migrateAllCandidates() {
        migrateAllCandidates(THREAD_POOL_SIZE);
    }

    /**
     * 迁移所有应聘者数据（使用指定线程数的线程池并发处理）
     * 
     * @param threadPoolSize 线程池大小
     */
    @Override
    public void migrateAllCandidates(int threadPoolSize) {
        log.info("开始迁移所有应聘者数据（使用线程池，线程数: {}）...", threadPoolSize);
        
        // 创建指定大小的线程池
        Executor executor = Executors.newFixedThreadPool(threadPoolSize);
        
        try {
            // 获取所有应聘者数据
            List<PgCandidateData> candidates = pgCandidateDataMapper.selectList(null);
            log.info("找到 {} 个应聘者需要迁移", candidates.size());
            
            // 使用原子计数器来跟踪成功和失败的数量
            AtomicInteger successCount = new AtomicInteger(0);
            AtomicInteger failCount = new AtomicInteger(0);

            // 创建异步任务列表
            List<CompletableFuture<Void>> futures = candidates.stream()
                .map(candidate -> CompletableFuture.runAsync(() -> {
                    try {
                        migrateSingleCandidate(candidate);
                        int currentSuccess = successCount.incrementAndGet();
                        
                        if (currentSuccess % 100 == 0) {
                            log.info("已迁移 {} 个应聘者", currentSuccess);
                        }
                    } catch (Exception e) {
                        failCount.incrementAndGet();
                        log.error("迁移应聘者失败: candidate_id={}, error={}",
                            candidate.getId(), e.getMessage(), e);
                    }
                }, executor))
                .collect(Collectors.toList());
            
            // 等待所有任务完成
            CompletableFuture<Void> allTasks = CompletableFuture.allOf(
                futures.toArray(new CompletableFuture[0])
            );
            
            allTasks.get();
            
            log.info("应聘者数据迁移完成: 成功={}, 失败={}", successCount.get(), failCount.get());
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("迁移应聘者数据被中断", e);
            throw new RuntimeException("数据迁移被中断", e);
        } catch (Exception e) {
            log.error("迁移应聘者数据失败", e);
            throw new RuntimeException("数据迁移失败", e);
        } finally {
            // 关闭线程池
            if (executor instanceof ExecutorService) {
                ((ExecutorService) executor).shutdown();
            }
        }
    }

    /**
     * 迁移单个应聘者数据
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void migrateSingleCandidate(PgCandidateData pgCandidate) {
        log.debug("开始迁移应聘者: {}", pgCandidate.getId());
        
        try {
            LambdaQueryWrapper<PgUser> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(PgUser::getId, UUID.fromString(pgCandidate.getUserId()))
                    .last("limit 1");
            // 1. 获取用户基本信息
            PgUser user = pgUserMapper.selectOne(queryWrapper);
            if (user == null) {
                log.warn("未找到用户信息: user_id={}", pgCandidate.getUserId());
                return;
            }
            LambdaQueryWrapper<PgEducationHistory> queryWrapper2 = new LambdaQueryWrapper<>();
            queryWrapper2.eq(PgEducationHistory::getCandidateId, UUID.fromString(pgCandidate.getId()));
            // 2. 获取教育历史（最新一条）
            List<PgEducationHistory> latestEducations = pgEducationHistoryMapper.selectList(queryWrapper2);
            LambdaQueryWrapper<PgEmploymentHistory> queryWrapper3 = new LambdaQueryWrapper<>();
            queryWrapper3.eq(PgEmploymentHistory::getCandidateId, UUID.fromString(pgCandidate.getId()));
            // 3. 获取工作历史（最新一条）
            List<PgEmploymentHistory> latestEmployments = pgEmploymentHistoryMapper.selectList(queryWrapper3);
            LambdaQueryWrapper<PgResumeData> queryWrapper4 = new LambdaQueryWrapper<>();
            queryWrapper4.eq(PgResumeData::getCandidateId, UUID.fromString(pgCandidate.getId()))
                    .orderByDesc(PgResumeData::getCreatedOn)
                    .last("limit 1");
            // 4. 获取最新简历文件
            PgResumeData latestResume = pgResumeDataMapper.selectOne(queryWrapper4);
            LambdaQueryWrapper<PgApplication> queryWrapper5 = new LambdaQueryWrapper<>();
            queryWrapper5.eq(PgApplication::getCandidateId, UUID.fromString(pgCandidate.getId()))
                    .orderByDesc(PgApplication::getCreatedOn)
                    .last("limit 1");
            // 5. 获取最新申请信息
            PgApplication latestApplication = pgApplicationMapper.selectOne(queryWrapper5);
            
            // 6. 构建迁移数据对象
            CandidateMigrationDTO migrationData = buildMigrationData(
                pgCandidate, user, latestEducations, latestEmployments, latestResume, latestApplication);
            
            // 7. 保存到MySQL
            CandidateEntity candidateEntity = saveToMySQL(migrationData);
            // 8. 保存到Elasticsearch
            saveToElasticsearch(candidateEntity,migrationData);
            
            log.debug("应聘者迁移成功: candidate_id={}, r_candidate_id={}", 
                pgCandidate.getId(), candidateEntity.getId());
            
        } catch (Exception e) {
            log.error("迁移应聘者失败: candidate_id={}", pgCandidate.getId(), e);
            throw e;
        }
    }

    /**
     * 构建迁移数据对象
     */
    private CandidateMigrationDTO buildMigrationData(
            PgCandidateData pgCandidate,
            PgUser user,
            List<PgEducationHistory> educations,
            List<PgEmploymentHistory> employments,
            PgResumeData resume,
            PgApplication application) {
        CandidateMigrationDTO dto = new CandidateMigrationDTO();


        // 基本信息
        dto.setPgCandidateId(pgCandidate.getId());
        dto.setUserId(pgCandidate.getUserId());
        dto.setSlug(pgCandidate.getSlug());
        
        // 用户信息
        dto.setFirstName(user.getFirstName());
        dto.setMiddleName(user.getMiddleName());
        dto.setLastName(user.getLastName());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        
        // 应聘者信息
        dto.setGender(pgCandidate.getGender());
        dto.setDateOfBirth(pgCandidate.getCandidateDob());
        dto.setAddress(pgCandidate.getAddress());
        dto.setCity(pgCandidate.getCity());
        dto.setState(pgCandidate.getState());
        dto.setCountry(pgCandidate.getCountry());
        dto.setPostalCode(pgCandidate.getPostalCode());
        
        // 社交媒体信息
        dto.setFacebook(pgCandidate.getFacebook());
        dto.setLinkedin(pgCandidate.getLinkedin());
        dto.setGithub(pgCandidate.getGithub());
        
        // 手动转换教育历史数据
        List<CandidateEducationEntity> candidateEducations = convertEducationsToCandidateEducations(educations);
        
        // 手动转换工作历史数据
        List<EmploymentHistoryEntity> employmentHistories = convertEmploymentsToEmploymentHistories(employments);


        // 简历信息
        if (resume != null) {
            dto.setResumeFileName(resume.getFileName());
            dto.setResumeCreatedOn(resume.getCreatedOn());
        }
        
        // 申请信息（最新一条）
        if (application != null) {
            dto.setExpectedSalary(application.getExpectedSalary());
            dto.setSalaryType(application.getSalaryType());
            dto.setAvailableFrom(application.getAvailableFrom());
            dto.setCurrencyId(application.getCurrencyId());
        }
        
        // 时间信息 - OffsetDateTime转换为LocalDateTime
        dto.setCreatedOn(pgCandidate.getCreatedOn());
        dto.setUpdatedOn(pgCandidate.getUpdatedOn());
        dto.setCreatedBy(pgCandidate.getCreatedBy());
        dto.setUpdatedBy(pgCandidate.getUpdatedBy());
        
        // 设置转换后的实体列表
        dto.setCandidateEducations(candidateEducations);
        dto.setEmploymentHistories(employmentHistories);
        
        return dto;
    }

    /**
     * 保存到MySQL
     */
    private CandidateEntity saveToMySQL(CandidateMigrationDTO dto) {
        log.debug("保存应聘者到MySQL: {}", dto.getPgCandidateId());

        // 创建新的应聘者记录
        CandidateEntity candidateEntity = new CandidateEntity();
        LambdaQueryWrapper<DataMigrationMappingEntity> dqueryWrapper = new LambdaQueryWrapper<>();
        dqueryWrapper.eq(DataMigrationMappingEntity::getPgsqlId, dto.getPgCandidateId()).last("limit 1");
        DataMigrationMappingEntity dataMigrationMappingEntity = dataMigrationMappingMapper.selectOne(dqueryWrapper);
        if (Objects.isNull(dataMigrationMappingEntity)) {
            dataMigrationMappingEntity = new DataMigrationMappingEntity();
            dataMigrationMappingEntity.setCreateTime(LocalDateTime.now());
            dataMigrationMappingEntity.setBusType(2);
        } else {
            candidateEntity.setId(dataMigrationMappingEntity.getMysqlId());
            log.debug("应聘者已存在，更新数据: r_candidate_id={}", dataMigrationMappingEntity.getMysqlId());
        }

        // 基本信息
        candidateEntity.setCandidateName(dto.getFirstName() + " " + dto.getLastName());
        candidateEntity.setMiddleName(dto.getMiddleName());
        candidateEntity.setCandidateEmail(dto.getEmail());
        candidateEntity.setCandidatePermanentEmail(dto.getEmail());
        candidateEntity.setPhoneNumber(dto.getPhone());
        candidateEntity.setFirstName(dto.getFirstName());
        candidateEntity.setLastName(dto.getLastName());

        List<DictionaryEntity> dictionaryEntities = dictionaryService.list();
        Map<String, Long> dictionaryMap = dictionaryEntities.stream()
                // 按type分组
                .collect(
                        // 每组内再按value分组，保留对应的实体
                        Collectors.toMap(
                                entity -> entity.getCode().toLowerCase(),
                                DictionaryEntity::getId,
                                // 若存在相同value的实体，保留第一个
                                (existing, replacement) -> existing
                        )
                );

        // 性别映射
        Long genderId = dictionaryMap.get(dto.getGender().toLowerCase());
        candidateEntity.setGender(genderId != null ? genderId.toString() : null);

        // 出生日期
        candidateEntity.setDateOfBirth(dto.getDateOfBirth());

        // 地址信息
        candidateEntity.setStreetAddress(dto.getAddress());

        candidateEntity.setPostalCode(dto.getPostalCode());
        candidateEntity.setCityName(dto.getCity());
        candidateEntity.setStateName(dto.getState());
        candidateEntity.setCountryName(dto.getCountry());
        if (StringUtils.hasText(dto.getCity())) {
            LambdaQueryWrapper<CityEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(CityEntity::getName, dto.getCity());
            queryWrapper.last("LIMIT 1");
            CityEntity cityEntity = cityMapper.selectOne(queryWrapper);
            if (Objects.nonNull(cityEntity)) {
                candidateEntity.setCityId(cityEntity.getId());
                if (candidateEntity.getStateId() == null) {
                    candidateEntity.setStateId(cityEntity.getStateId());
                }
                if (candidateEntity.getCountryId() == null) {
                    candidateEntity.setCountryId(cityEntity.getCountryId());
                }
            }
        }
        if (StringUtils.hasText(dto.getState()) && candidateEntity.getStateId() == null) {
            LambdaQueryWrapper<StateEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(StateEntity::getName, dto.getState());
            queryWrapper.last("LIMIT 1");
            StateEntity stateEntity = stateMapper.selectOne(queryWrapper);
            if (Objects.nonNull(stateEntity)) {
                candidateEntity.setStateId(stateEntity.getId());
                if (candidateEntity.getCountryId() == null) {
                    candidateEntity.setCountryId(stateEntity.getCountryId());
                }
            }
        }
        if (StringUtils.hasText(dto.getCountry()) && candidateEntity.getCountryId() == null) {
            LambdaQueryWrapper<CountryEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(CountryEntity::getName, dto.getCountry());
            queryWrapper.last("LIMIT 1");
            CountryEntity countryEntity = countryMapper.selectOne(queryWrapper);
            candidateEntity.setCountryId(countryEntity != null ? countryEntity.getId() : null);
        }

        // 简历信息
        if (dto.getResumeFileName() != null) {
            String resumeKey = dto.getUserId() + "/" + dto.getResumeFileName();
            String targetFileName = dto.getResumeFileName() + ".pdf";
            String s3Key = s3Config.getFolder() + FILE_SEPARATOR + targetFileName;
            // 检查目标S3中是否已存在该文件
            if (s3Utils.doesObjectExist(s3Key)) {
                // 文件已存在，直接使用现有key
                candidateEntity.setResumeUrl(s3Key);
                candidateEntity.setUploadStatus(1); // 已上传
                log.info("Resume file already exists in S3, using existing key: {}", s3Key);
            } else {
                // 文件不存在，需要从S3 secondary下载并上传
                InputStream in = s3SecondaryUtils.downloadFileSafely(resumeKey);
                try {
                    if (in != null) {
                        String key = s3Utils.uploadFile(in, targetFileName);
                        candidateEntity.setResumeUrl(key);
                        candidateEntity.setUploadStatus(1); // 已上传
                        log.info("Successfully migrated resume file: {} -> {}", resumeKey, targetFileName);
                    } else {
                        log.info("Resume file not found in S3 secondary: {}, setting upload status to 0", resumeKey);
                        candidateEntity.setUploadStatus(0); // 未上传
                    }
                } catch (Exception e) {
                    log.error("Failed to migrate resume file: {}", resumeKey, e);
                    candidateEntity.setUploadStatus(0); // 未上传
                } finally {
                    if (in != null) {
                        try {
                            in.close();
                        } catch (IOException e) {
                            log.error("Failed to close input stream", e);
                        }
                    }
                }
            }
        } else {
            candidateEntity.setUploadStatus(0); // 未上传
        }

        // 薪资信息映射
        if (dto.getExpectedSalary() != null) {
            candidateEntity.setExpectedSalary(dto.getExpectedSalary().intValue());
        }

        if (dto.getSalaryType() != null) {
            Long salaryTypeId = dictionaryMap.get(dto.getSalaryType().toLowerCase());
            candidateEntity.setSalaryTypeId(salaryTypeId);
        }

        if (dto.getCurrencyId() != null) {
            // 从PostgreSQL货币表获取货币信息
            PgCurrency pgCurrency = pgCurrencyMapper.selectById(dto.getCurrencyId());
            if (pgCurrency != null) {
                // 根据货币名称或符号查找MySQL字典表中的对应ID
                Long currencyTypeId = findCurrencyTypeIdByNameOrSymbol(pgCurrency.getName(), pgCurrency.getSymbol(), dictionaryMap);
                candidateEntity.setCurrencyTypeId(currencyTypeId);
            }
        }

        if (dto.getAvailableFrom() != null) {
            candidateEntity.setAvailableFrom(convertOffsetDateTimeToLocalDate(dto.getAvailableFrom()));
        }

        // 扩展字段
        candidateEntity.setExt1(dto.getFacebook());
        candidateEntity.setExt2(dto.getLinkedin());
        candidateEntity.setCreateTime(convertOffsetDateTimeToLocalDateTime(dto.getCreatedOn()));
        if (Objects.nonNull(dto.getUpdatedOn())) {
            candidateEntity.setUpdateTime(convertOffsetDateTimeToLocalDateTime(dto.getUpdatedOn()));
        } else {
            candidateEntity.setUpdateTime(convertOffsetDateTimeToLocalDateTime(dto.getCreatedOn()));
        }

        IamCreateUserReqDTO iamCreateUserReqDTO = new IamCreateUserReqDTO();
        iamCreateUserReqDTO.setUserName(dto.getEmail());
        iamCreateUserReqDTO.setFirstName(candidateEntity.getFirstName());
        iamCreateUserReqDTO.setLastName(candidateEntity.getLastName());
        iamCreateUserReqDTO.setEmail(dto.getEmail());
        iamCreateUserReqDTO.setRawPassword("password123");
        iamCreateUserReqDTO.setContactNumber(candidateEntity.getPhoneNumber());
        iamCreateUserReqDTO.setGrantedAppCodes(iamCommonConfig.getRegisterCandidate().getGrantedAppCodes());
        try {
            CandidateEntity candidate = registerCandidate(iamCreateUserReqDTO);
            candidateEntity.setCandidateId(candidate.getCandidateId());
        } catch (Exception e) {
            try {
                log.error("应聘者注册iam失败，iamCreateUserReqDTO：{}",objectMapper.writeValueAsString(iamCreateUserReqDTO),e);
            } catch (JsonProcessingException ex) {
                e.printStackTrace();
            }
        }
        candidateEntity.setDeleted(0);
        // 保存到数据库
        candidateService.saveOrUpdate(candidateEntity);
        dataMigrationMappingEntity.setPgsqlId(dto.getPgCandidateId());
        dataMigrationMappingEntity.setMysqlId(candidateEntity.getId());
        dataMigrationMappingMapper.insertOrUpdate(dataMigrationMappingEntity);
        log.debug("应聘者保存到MySQL成功: r_candidate_id={}", candidateEntity.getId());
        return candidateEntity;
    }

    /**
     * 更新已存在的应聘者
     */
    private CandidateEntity updateExistingCandidate(CandidateEntity existing, CandidateMigrationDTO dto) {
        // 更新基本信息
        existing.setCandidateName(dto.getFirstName() + " " + dto.getLastName());
        existing.setMiddleName(dto.getMiddleName());
        existing.setFirstName(dto.getFirstName());
        existing.setLastName(dto.getLastName());
        existing.setPhoneNumber(dto.getPhone());
        
        // 更新时间信息
        existing.setUpdateTime(convertOffsetDateTimeToLocalDateTime(dto.getUpdatedOn()));
        
        // 更新其他信息...
        candidateService.updateById(existing);
        return existing;
    }

    /**
     * 保存到Elasticsearch
     */
    private void saveToElasticsearch(CandidateEntity candidateEntity,CandidateMigrationDTO migrationData) {
        log.debug("保存应聘者到Elasticsearch: r_candidate_id={}", candidateEntity.getId());
        
        try {
            // 构建ES文档
            CandidateEsEntity esEntity = buildElasticsearchEntity(candidateEntity,migrationData);

            // 保存到ES
            resumeEsService.saveResumeToEs(esEntity);
            
            log.debug("应聘者保存到Elasticsearch成功: r_candidate_id={}", candidateEntity.getId());
            
        } catch (Exception e) {
            log.error("保存应聘者到Elasticsearch失败: r_candidate_id={}", candidateEntity.getId(), e);
            // ES保存失败不影响主流程，只记录日志
        }
    }

    /**
     * 构建Elasticsearch实体
     */
    private CandidateEsEntity buildElasticsearchEntity(CandidateEntity candidateEntity,CandidateMigrationDTO migrationData) {
        return CandidateEsEntity.builder()
            .id(candidateEntity.getId())
            .candidateName(candidateEntity.getCandidateName())
            .middleName(candidateEntity.getMiddleName())
            .candidateEmail(candidateEntity.getCandidateEmail())
            .phoneNumber(candidateEntity.getPhoneNumber())
            .gender(candidateEntity.getGender())
            .dateOfBirth(candidateEntity.getDateOfBirth())
            .streetAddress(candidateEntity.getStreetAddress())
            .apartmentOrSuite(candidateEntity.getApartmentOrSuite())
            .countryId(candidateEntity.getCountryId())
            .countryName(candidateEntity.getCountryName())
            .stateId(candidateEntity.getStateId())
            .stateName(candidateEntity.getStateName())
            .cityId(candidateEntity.getCityId())
            .cityName(candidateEntity.getCityName())
            .postalCode(candidateEntity.getPostalCode())
            .candidateEducations(migrationData.getCandidateEducations())
            .employmentHistories(migrationData.getEmploymentHistories())
            .currencyTypeId(candidateEntity.getCurrencyTypeId())
            .expectedSalary(candidateEntity.getExpectedSalary())
            .salaryTypeId(candidateEntity.getSalaryTypeId())
            .availableFrom(candidateEntity.getAvailableFrom())
            .createTime(candidateEntity.getCreateTime())
            .updateTime(candidateEntity.getUpdateTime())
            .deleted(candidateEntity.getDeleted())
            .resumeUrl(candidateEntity.getResumeUrl())
            .uploadStatus(candidateEntity.getUploadStatus())
            .build();
    }

    /**
     * 批量迁移指定数量的应聘者（使用线程池并发处理）
     */
    @Override
    public void migrateCandidatesBatch(int batchSize, int offset) {
        migrateCandidatesBatch(batchSize, offset, THREAD_POOL_SIZE);
    }

    /**
     * 批量迁移指定数量的应聘者（使用指定线程数的线程池并发处理）
     * 
     * @param batchSize 批次大小
     * @param offset 偏移量
     * @param threadPoolSize 线程池大小
     */
    @Override
    public void migrateCandidatesBatch(int batchSize, int offset, int threadPoolSize) {
        log.info("开始批量迁移应聘者: batchSize={}, offset={}, 线程数={}", batchSize, offset, threadPoolSize);
        
        // 创建指定大小的线程池
        Executor executor = Executors.newFixedThreadPool(threadPoolSize);
        
        try {
            List<PgCandidateData> candidates = pgCandidateDataMapper.selectBatch(offset, batchSize);
            log.info("找到 {} 个应聘者需要迁移", candidates.size());
            
            // 使用原子计数器来跟踪成功和失败的数量
            AtomicInteger successCount = new AtomicInteger(0);
            AtomicInteger failCount = new AtomicInteger(0);
            
            // 创建异步任务列表
            List<CompletableFuture<Void>> futures = candidates.stream()
                .map(candidate -> CompletableFuture.runAsync(() -> {
                    try {
                        migrateSingleCandidate(candidate);
                        successCount.incrementAndGet();
                    } catch (Exception e) {
                        failCount.incrementAndGet();
                        log.error("批量迁移应聘者失败: candidate_id={}", candidate.getId(), e);
                    }
                }, executor))
                .collect(Collectors.toList());
            
            // 等待所有任务完成
            CompletableFuture<Void> allTasks = CompletableFuture.allOf(
                futures.toArray(new CompletableFuture[0])
            );
            
            // 等待所有任务完成，最多等待10分钟
            allTasks.get(120, TimeUnit.MINUTES);
            
            log.info("批量迁移完成: 处理了 {} 个应聘者, 成功={}, 失败={}", 
                candidates.size(), successCount.get(), failCount.get());
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("批量迁移应聘者被中断", e);
            throw new RuntimeException("批量迁移被中断", e);
        } catch (Exception e) {
            log.error("批量迁移应聘者失败", e);
            throw new RuntimeException("批量迁移失败", e);
        } finally {
            // 关闭线程池
            if (executor instanceof ExecutorService) {
                ((ExecutorService) executor).shutdown();
            }
        }
    }

    /**
     * 批量迁移指定数量的应聘者
     */
    @Override
    public void migrateCandidatesbyCandidateId(String id) {
        log.info("开始批量迁移应聘者: CandidateId={}", id);

        try {
            LambdaQueryWrapper<PgCandidateData> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(PgCandidateData::getId,UUID.fromString(id)).last("limit 1");
            PgCandidateData candidate = pgCandidateDataMapper.selectOne(queryWrapper);
            migrateSingleCandidate(candidate);

        } catch (Exception e) {
            log.error("批量迁移应聘者失败", e);
            throw e;
        }
    }

    /**
     * 手动转换教育历史数据
     * 将PostgreSQL教育历史转换为MySQL教育历史实体
     */
    private List<CandidateEducationEntity> convertEducationsToCandidateEducations(List<PgEducationHistory> educations) {
        if (educations == null || educations.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 获取字典映射数据
        List<DictionaryEntity> dictionaryEntities = dictionaryService.list();
        Map<String, Long> dictionaryMap = dictionaryEntities.stream()
                .collect(Collectors.toMap(
                        entity -> entity.getCode().toLowerCase(),
                        DictionaryEntity::getId,
                        (existing, replacement) -> existing
                ));
        
        List<CandidateEducationEntity> candidateEducations = new ArrayList<>();
        for (PgEducationHistory pgEducation : educations) {
            CandidateEducationEntity candidateEducation = new CandidateEducationEntity();
            
            // 使用字典表数据转换institutionTypeId，忽略大小写
            if (StringUtils.hasText(pgEducation.getInstitutionType())) {
                Long institutionTypeId = dictionaryMap.get(pgEducation.getInstitutionType().toLowerCase());
                candidateEducation.setInstitutionTypeId(institutionTypeId);
            }
            
            // 使用字典表数据转换degreeId，忽略大小写
            if (StringUtils.hasText(pgEducation.getDegreeEarned())) {
                Long degreeId = dictionaryMap.get(pgEducation.getDegreeEarned().toLowerCase());
                candidateEducation.setDegreeId(degreeId);
            }
            
            // 基本信息映射
            candidateEducation.setInstitutionName(pgEducation.getInstitutionName());
            candidateEducation.setStartDate(pgEducation.getStartDate());
            candidateEducation.setEndDate(pgEducation.getEndDate());
            candidateEducation.setMajor(pgEducation.getMajor());
            candidateEducation.setMinor(pgEducation.getMinor());
            
            // Boolean转Integer转换
            if (pgEducation.getGraduated() != null) {
                candidateEducation.setGraduated(Boolean.TRUE.equals(pgEducation.getGraduated()) ? 1 : 0);
            }
            
            candidateEducations.add(candidateEducation);
        }
        
        return candidateEducations;
    }

    /**
     * 手动转换工作历史数据
     * 将PostgreSQL工作历史转换为MySQL工作历史实体
     */
    private List<EmploymentHistoryEntity> convertEmploymentsToEmploymentHistories(List<PgEmploymentHistory> employments) {
        if (employments == null || employments.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<EmploymentHistoryEntity> employmentHistories = new ArrayList<>();
        for (PgEmploymentHistory pgEmployment : employments) {
            EmploymentHistoryEntity employmentHistory = new EmploymentHistoryEntity();
            
            // 基本信息映射
            employmentHistory.setCompanyName(pgEmployment.getCompanyName());
            employmentHistory.setJobTitle(pgEmployment.getJobTitle());
            employmentHistory.setStartDate(pgEmployment.getStartDate());
            employmentHistory.setEndDate(pgEmployment.getEndDate());
            employmentHistory.setKeyResponsibilities(pgEmployment.getResponsibilities());
            
            // Boolean转String转换
            if (pgEmployment.getCurrentEmployer() != null) {
                employmentHistory.setCurrentEmployer(Boolean.TRUE.equals(pgEmployment.getCurrentEmployer()) ? "1" : "0");
            }
            
            employmentHistories.add(employmentHistory);
        }
        
        return employmentHistories;
    }

    /**
     * 将OffsetDateTime转换为LocalDateTime
     * 用于PostgreSQL到MySQL的数据迁移
     * 
     * @param offsetDateTime OffsetDateTime对象
     * @return LocalDateTime对象，如果输入为null则返回null
     */
    private java.time.LocalDateTime convertOffsetDateTimeToLocalDateTime(java.time.OffsetDateTime offsetDateTime) {
        if (offsetDateTime == null) {
            return null;
        }
        return offsetDateTime.toLocalDateTime();
    }

    /**
     * 将OffsetDateTime转换为LocalDate
     * 用于PostgreSQL到MySQL的数据迁移
     * 
     * @param offsetDateTime OffsetDateTime对象
     * @return LocalDate对象，如果输入为null则返回null
     */
    private java.time.LocalDate convertOffsetDateTimeToLocalDate(java.time.OffsetDateTime offsetDateTime) {
        if (offsetDateTime == null) {
            return null;
        }
        return offsetDateTime.toLocalDate();
    }

    /**
     * 根据货币名称或符号查找MySQL字典表中的货币类型ID
     * 
     * @param currencyName 货币名称
     * @param currencySymbol 货币符号
     * @return 货币类型ID，如果未找到则返回null
     */
    private Long findCurrencyTypeIdByNameOrSymbol(String currencyName, String currencySymbol,Map<String, Long> dictionaryMap) {
        if (!StringUtils.hasText(currencyName) && !StringUtils.hasText(currencySymbol)) {
            return null;
        }
        
        // 先尝试按货币名称查找
        if (StringUtils.hasText(currencyName)) {
            Long currencyId = dictionaryMap.get(currencyName.toLowerCase());
            if (currencyId != null) {
                return currencyId;
            }
        }
        
        // 再尝试按货币符号查找
        if (StringUtils.hasText(currencySymbol)) {
            Long currencyId = dictionaryMap.get(currencySymbol.toLowerCase());
            if (currencyId != null) {
                return currencyId;
            }
        }
        
        log.warn("未找到货币类型ID，货币名称: {}, 货币符号: {}", currencyName, currencySymbol);
        return null;
    }

    @Override
    public CandidateEntity registerCandidate(IamCreateUserReqDTO iamCreateUserReqDTO) {
        QueryWrapper<CandidateEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(CandidateEntity::getCandidatePermanentEmail, iamCreateUserReqDTO.getEmail()).last("limit 1");
        CandidateEntity rCandidate = candidateService.getOne(queryWrapper);
        if ( Objects.nonNull(rCandidate) && Objects.nonNull(rCandidate.getCandidateId()) ) {
            return rCandidate;
        }
        iamCreateUserReqDTO.setCompanyCode("MKT");
        
        IamCreateUserResDTO iamCreateUserResDTO;
        
        try {
            log.info("iam createUser : {}",objectMapper.writeValueAsString(iamCreateUserReqDTO));
            iamCreateUserResDTO = iamFeignClient.createUser(iamCreateUserReqDTO).getData();
        } catch (BusinessException e) {
            if (e.getCode() == 210010022) {
                // 用户已存在，根据邮箱获取 IAM 用户信息
                log.info("用户已存在，根据邮箱获取 IAM 用户信息: {}", iamCreateUserReqDTO.getEmail());
                iamCreateUserResDTO = getUserFromIamByEmail(iamCreateUserReqDTO.getEmail());
            } else {
                // 其他业务异常，直接抛出
                log.error("创建 IAM 用户失败: {}", e.getMessage(), e);
                throw e;
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        
        CandidateEntity candidate = new CandidateEntity();
        candidate.setCandidateName(iamCreateUserResDTO.getUserName());
        candidate.setCandidateEmail(iamCreateUserResDTO.getEmail());
        candidate.setPhoneNumber(iamCreateUserResDTO.getContactNumber());
        candidate.setCandidateId(Long.parseLong(iamCreateUserResDTO.getId()));
        candidate.setFirstName(iamCreateUserResDTO.getFirstName());
        candidate.setLastName(iamCreateUserResDTO.getLastName());
        log.info("registerCandidate candidate {} iamUserRegisterResDTO {}", candidate, iamCreateUserResDTO);
        return candidate;
    }

    /**
     * 根据邮箱从 IAM 系统获取用户信息并映射到 IamCreateUserResDTO
     * 
     * @param email 用户邮箱
     * @return IamCreateUserResDTO 用户信息
     */
    private IamCreateUserResDTO getUserFromIamByEmail(String email) {
        try {
            log.info("根据邮箱查询 IAM 用户信息: {}", email);
            
            // 调用 IAM 用户查询接口
            FeignResponse<IamUserDetailDTO> response = iamFeignClient.queryUserByIdentifier(email);
            
            if (response != null && response.getSuccess() && response.getData() != null) {
                IamUserDetailDTO userDetail = response.getData();

                // 将 IamUserDetailDTO 映射到 IamCreateUserResDTO
                IamCreateUserResDTO iamCreateUserResDTO = new IamCreateUserResDTO();
                iamCreateUserResDTO.setId(userDetail.getId());
                iamCreateUserResDTO.setUserName(userDetail.getUserName());
                iamCreateUserResDTO.setEmail(userDetail.getEmail());
                iamCreateUserResDTO.setFirstName(userDetail.getFirstName());
                iamCreateUserResDTO.setLastName(userDetail.getLastName());
                iamCreateUserResDTO.setContactNumber(userDetail.getContactNumber());
                iamCreateUserResDTO.setCompanyCode(userDetail.getCompanyCode());
                iamCreateUserResDTO.setUserStatus(userDetail.getUserStatus());
                iamCreateUserResDTO.setUserType(userDetail.getUserType());
                iamCreateUserResDTO.setPrimaryUser(userDetail.getPrimaryUser());
                
                log.info("成功获取 IAM 用户信息: id={}, userName={}, email={}", 
                    iamCreateUserResDTO.getId(), iamCreateUserResDTO.getUserName(), iamCreateUserResDTO.getEmail());
                
                return iamCreateUserResDTO;
            } else {
                log.error("根据邮箱查询 IAM 用户信息失败: email={}, response={}", email, response);
                throw new RuntimeException("根据邮箱查询 IAM 用户信息失败: " + email);
            }
        } catch (Exception e) {
            log.error("根据邮箱查询 IAM 用户信息异常: email={}", email, e);
            throw new RuntimeException("根据邮箱查询 IAM 用户信息异常: " + email, e);
        }
    }

}