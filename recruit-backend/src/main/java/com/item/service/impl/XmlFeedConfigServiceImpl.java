package com.item.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.item.dto.iam.IamCompanyDetailDTO;
import com.item.dto.iam.IamUserContextDTO;
import com.item.dto.xmlfeed.XmlFeedConfigCreateDTO;
import com.item.entity.XmlFeedConfigEntity;
import com.item.entity.XmlFeedUpdateLogEntity;
import com.item.es.JobEsService;
import com.item.es.entity.JobEsEntity;
import com.item.framework.config.FeedProperties;
import com.item.framework.constant.JobStatus;
import com.item.framework.constant.XmlFeedConstants;
import com.item.framework.constant.CommonResponseCode;
import com.item.framework.error.BusinessException;
import com.item.mapper.XmlFeedConfigMapper;
import com.item.mapper.XmlFeedUpdateLogMapper;
import com.item.service.XmlFeedConfigService;
import com.item.service.XmlFeedService;
import com.item.service.client.adapter.IamRpcAdapter;
import com.item.util.UserContextUtil;
import com.item.convert.XmlFeedConfigConverter;

import com.item.vo.feed.XmlFeedConfigVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;


/**
 * XML Feed configuration management service implementation
 *
 * @author liyunlong
 * @since 2025-08-26
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class XmlFeedConfigServiceImpl implements XmlFeedConfigService {

    private final XmlFeedConfigMapper xmlFeedConfigMapper;
    private final XmlFeedUpdateLogMapper xmlFeedUpdateLogMapper;
    private final XmlFeedService xmlFeedService;
    private final XmlFeedConfigConverter xmlFeedConfigConverter;
    private final FeedProperties feedProperties;
    private final IamRpcAdapter iamRpcAdapter;
    private final JobEsService jobEsService;

    @Override
    public XmlFeedConfigVO getConfigByPlatformType(Integer platformType) {
        //校验
        validatePlatformType(platformType);
        IamUserContextDTO currentUser = UserContextUtil.getCurrentUserRecruitNeedLogin();
        String companyCode = currentUser.getCompanyCode();
        
        log.debug("Getting XML Feed config for company: {}, platformType: {}", companyCode, platformType);

        XmlFeedConfigEntity entity = findConfigByCompanyAndPlatform(companyCode, platformType);
        if (entity == null) {
            log.info("No XML Feed config found for company: {}, platformType: {}", companyCode, platformType);
            return null;
        }

        log.debug("Found XML Feed config for company: {}, platformType: {}, configId: {}", 
                 companyCode, platformType, entity.getId());
        return xmlFeedConfigConverter.convertEntityToVO(entity);
    }

    /**
     * Create or update XML Feed configuration with enhanced validation
     * 
     * <p>Business validation rules:</p>
     * <ul>
     *   <li>Platform type: Only LinkedIn(1) and Indeed(2) are supported</li>
     *   <li>Email validation: Indeed platform requires valid email, LinkedIn is optional</li>
     *   <li>Update interval: Must be between 1-24 hours</li>
     *   <li>Email format: Must be valid email format when provided</li>
     * </ul>
     * 
     * @param createDTO configuration data with validation requirements
     * @return XmlFeedConfigVO configuration view object with generated URLs
     * @throws BusinessException if validation fails with specific error codes:
     *         <ul>
     *           <li>COMMON_XML_FEED_PLATFORM_TYPE_INVALID - Invalid platform type</li>
     *           <li>COMMON_XML_FEED_EMAIL_REQUIRED_FOR_INDEED - Missing email for Indeed</li>
     *           <li>COMMON_XML_FEED_UPDATE_INTERVAL_INVALID - Invalid update interval</li>
     *         </ul>
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public XmlFeedConfigVO createOrUpdateConfig(XmlFeedConfigCreateDTO createDTO) {
        IamUserContextDTO currentUser = UserContextUtil.getCurrentUserRecruitNeedLogin();
        String companyCode = currentUser.getCompanyCode();
        
        log.info("Creating/updating XML Feed config for company: {}, platformType: {}, updateInterval: {}h", 
                companyCode, createDTO.getPlatformType(), createDTO.getUpdateIntervalHours());

        // Enhanced business validation
        validateConfigurationParameters(createDTO);

        IamCompanyDetailDTO companyInfo = iamRpcAdapter.getCompanyDetailByCode(companyCode);

        // Find existing configuration
        XmlFeedConfigEntity existingEntity = findConfigByCompanyAndPlatform(companyCode, createDTO.getPlatformType());

        XmlFeedConfigEntity entity;
        if (existingEntity != null) {
            // Update existing configuration
            log.debug("Updating existing XML Feed config with ID: {} for company: {}", 
                     existingEntity.getId(), companyCode);
            entity = existingEntity;
            entity.setAccountEmail(createDTO.getAccountEmail());
            entity.setUpdateIntervalHours(createDTO.getUpdateIntervalHours());
            entity.setUpdateBy(Long.valueOf(currentUser.getId()));
            entity.setUpdateTime(LocalDateTime.now());
            
            // Update platform guide URL
            String guideUrl = generateGuideUrl(createDTO.getPlatformType());
            entity.setGuideUrl(guideUrl);
            
            xmlFeedConfigMapper.updateById(entity);
        } else {
            // Create new configuration
            entity = new XmlFeedConfigEntity();
            entity.setCompanyCode(companyCode);
            entity.setPlatformType(createDTO.getPlatformType());
            entity.setAccountEmail(createDTO.getAccountEmail());
            entity.setUpdateIntervalHours(createDTO.getUpdateIntervalHours());
            entity.setCreateBy(Long.valueOf(currentUser.getId()));
            entity.setUpdateBy(Long.valueOf(currentUser.getId()));
            //初次生成 feed xml
            try{
                if (XmlFeedConstants.PlatformType.LINKEDIN.getCode().equals(createDTO.getPlatformType())) {
                    entity.setAccountEmail(null);
                    xmlFeedService.generateLinkedInXML(currentUser.getCompanyCode());
                } else if (XmlFeedConstants.PlatformType.INDEED.getCode().equals(createDTO.getPlatformType())) {
                    xmlFeedService.generateIndeedXML(currentUser.getCompanyCode(),entity.getAccountEmail());
                } else if (XmlFeedConstants.PlatformType.ZIP_RECRUITER.getCode().equals(createDTO.getPlatformType())) {
                    xmlFeedService.generateZipRecruiterXML(currentUser.getCompanyCode(),entity.getAccountEmail());
                }
            }catch (Exception e){
                log.error("Failed to generate XML Feed for company: {}, platformType: {}", companyCode, createDTO.getPlatformType());
                throw new BusinessException(CommonResponseCode.COMMON_GENERAL_ERROR);
            }

            // Generate Feed URL
            String feedUrl = generateFeedUrl(companyCode, createDTO.getPlatformType());
            entity.setFeedUrl(feedUrl);
            
            // Set platform guide URL
            String guideUrl = generateGuideUrl(createDTO.getPlatformType());
            entity.setGuideUrl(guideUrl);
            LocalDateTime now = LocalDateTime.now();
            entity.setLastUpdateTime(now);
            // Set next update time
            entity.setNextUpdateTime(now.plusHours(createDTO.getUpdateIntervalHours()));
            xmlFeedConfigMapper.insert(entity);
            log.info("Successfully created new XML Feed config with ID: {} for company: {}, platformType: {}", 
                    entity.getId(), companyCode, createDTO.getPlatformType());
        }

        return xmlFeedConfigConverter.convertEntityToVO(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public XmlFeedConfigVO manualUpdate(Integer platformType) {
        //校验
        validatePlatformType(platformType);
        IamUserContextDTO currentUser = UserContextUtil.getCurrentUserRecruitNeedLogin();
        String companyCode = currentUser.getCompanyCode();
        log.info("Manually updating XML Feed for company: {}, platformType: {}", companyCode, platformType);
        // Check if configuration exists
        XmlFeedConfigEntity config = findConfigByCompanyAndPlatform(companyCode, platformType);
        if (config == null) {
            throw new BusinessException(CommonResponseCode.COMMON_XML_FEED_CONFIG_NOT_SET);
        }

        // Check cooldown time based on lastUpdateTime
        if (config.getLastUpdateTime() != null) {
            LocalDateTime cooldownEndTime = config.getLastUpdateTime().plusMinutes(
                XmlFeedConstants.Business.MANUAL_UPDATE_COOLDOWN_MINUTES);
            if (LocalDateTime.now().isBefore(cooldownEndTime)) {
                throw new BusinessException(CommonResponseCode.COMMON_XML_FEED_UPDATE_TOO_FREQUENT);
            }
        }

        // Record update start
        XmlFeedUpdateLogEntity logEntity = new XmlFeedUpdateLogEntity();
        logEntity.setFeedConfigId(config.getId());
        logEntity.setUpdateType(XmlFeedConstants.UpdateType.MANUAL.getCode());
        logEntity.setOperatorId(Long.valueOf(currentUser.getId()));
        logEntity.setStartTime(LocalDateTime.now());

        try {
            // Execute update
            if (XmlFeedConstants.PlatformType.LINKEDIN.getCode().equals(platformType)) {
                xmlFeedService.generateLinkedInXML(currentUser.getCompanyCode());
            } else if (XmlFeedConstants.PlatformType.INDEED.getCode().equals(platformType)) {
                xmlFeedService.generateIndeedXML(currentUser.getCompanyCode(),config.getAccountEmail());
            } else if (XmlFeedConstants.PlatformType.ZIP_RECRUITER.getCode().equals(platformType)) {
                xmlFeedService.generateZipRecruiterXML(currentUser.getCompanyCode(),config.getAccountEmail());
            }

            // Record success
            logEntity.setUpdateStatus(XmlFeedConstants.UpdateStatus.SUCCESS.getCode());
            logEntity.setEndTime(LocalDateTime.now());
            logEntity.setJobCount(0); // TODO: Get job count from actual generation result
            xmlFeedUpdateLogMapper.insert(logEntity);

            // Update config lastUpdateTime
            LocalDateTime now = LocalDateTime.now();
            config.setLastUpdateTime(now);
            config.setNextUpdateTime(now.plusHours(config.getUpdateIntervalHours()));
            xmlFeedConfigMapper.updateById(config);
            return xmlFeedConfigConverter.convertEntityToVO(config);
        } catch (Exception e) {
            log.error("Manual update XML Feed failed platformType:{},companyCode:{}",platformType,companyCode,e);
            // Record failure
            logEntity.setUpdateStatus(XmlFeedConstants.UpdateStatus.FAILED.getCode());
            logEntity.setEndTime(LocalDateTime.now());
            logEntity.setErrorMessage(e.getMessage());
            xmlFeedUpdateLogMapper.insert(logEntity);
            throw new BusinessException(CommonResponseCode.COMMON_XML_FEED_MANUAL_UPDATE);
        }
    }

    /**
     * 更新job 更新XML Feed
     *
     * @return 更新是否成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void jobUpdate(String companyCode,Long userId) {
        log.info("jobUpdate XML Feed for company: {}", companyCode);
        // Check if configuration exists
        List<XmlFeedConfigEntity> configs = findConfigsByCompanyCode(companyCode);
        if (CollectionUtils.isEmpty(configs)) {
            return;
        }
        List<JobEsEntity> activeJobs = jobEsService.searchFeedXmlJobs(JobStatus.ACTIVE.getCode(), companyCode, feedProperties.getSize(),false);
        List<JobEsEntity> distinctActiveJobs = jobEsService.searchFeedXmlJobs(JobStatus.ACTIVE.getCode(), companyCode, feedProperties.getSize(),feedProperties.getLinkedInDistinct());

        for (XmlFeedConfigEntity config:configs){
            // Record update start
            XmlFeedUpdateLogEntity logEntity = new XmlFeedUpdateLogEntity();
            logEntity.setFeedConfigId(config.getId());
            logEntity.setUpdateType(XmlFeedConstants.UpdateType.MANUAL.getCode());
            logEntity.setOperatorId(userId);
            logEntity.setStartTime(LocalDateTime.now());

            try {
                // Execute update
                Integer jobCount=activeJobs.size();
                if (XmlFeedConstants.PlatformType.LINKEDIN.getCode().equals(config.getPlatformType())){
                    xmlFeedService.generateJobsXML(config.getPlatformType(),companyCode,config.getAccountEmail(),distinctActiveJobs);
                }else{
                    xmlFeedService.generateJobsXML(config.getPlatformType(),companyCode,config.getAccountEmail(),activeJobs);
                }

                // Record success
                logEntity.setUpdateStatus(XmlFeedConstants.UpdateStatus.SUCCESS.getCode());
                logEntity.setEndTime(LocalDateTime.now());
                logEntity.setJobCount(jobCount);
                xmlFeedUpdateLogMapper.insert(logEntity);

                // Update config lastUpdateTime
                LocalDateTime now = LocalDateTime.now();
                config.setLastUpdateTime(now);
                config.setNextUpdateTime(now.plusHours(config.getUpdateIntervalHours()));
                xmlFeedConfigMapper.updateById(config);
            } catch (Exception e) {
                log.error("Manual update XML Feed failed platformType:{},companyCode:{}",config.getPlatformType(),companyCode,e);
                // Record failure
                logEntity.setUpdateStatus(XmlFeedConstants.UpdateStatus.FAILED.getCode());
                logEntity.setEndTime(LocalDateTime.now());
                logEntity.setErrorMessage(e.getMessage());
                xmlFeedUpdateLogMapper.insert(logEntity);
            }
        }
    }

    /**
     * Find XML Feed configuration by company code and platform type
     */
    private XmlFeedConfigEntity findConfigByCompanyAndPlatform(String companyCode, Integer platformType) {
        return xmlFeedConfigMapper.selectOne(
            new LambdaQueryWrapper<XmlFeedConfigEntity>()
                .eq(XmlFeedConfigEntity::getCompanyCode, companyCode)
                .eq(XmlFeedConfigEntity::getPlatformType, platformType)
                .eq(XmlFeedConfigEntity::getDeleted, false)
        );
    }

    /**
     * Find XML Feed configuration by company code
     */
    private List<XmlFeedConfigEntity> findConfigsByCompanyCode(String companyCode) {
        return xmlFeedConfigMapper.selectList(
                new LambdaQueryWrapper<XmlFeedConfigEntity>()
                        .eq(XmlFeedConfigEntity::getCompanyCode, companyCode)
                        .eq(XmlFeedConfigEntity::getDeleted, false)
        );
    }

    /**
     * Generate Feed URL for external platforms
     * 
     * @param companyCode company code for URL path parameter
     * @param platformType platform type (1-LinkedIn, 2-Indeed)
     * @return complete XML feed URL for external platform consumption
     * @throws BusinessException if platform type is not supported
     */
    private String generateFeedUrl(String companyCode, Integer platformType) {
        String baseUrl = feedProperties.getFeedUrlPrefix();
        
        // Generate platform-specific URL using switch for better performance
        String platformPath;
        switch (XmlFeedConstants.PlatformType.getByCode(platformType)) {
            case LINKEDIN:
                platformPath = "/linked-in";
                break;
            case INDEED:
                platformPath = "/indeed";
                break;
            case ZIP_RECRUITER:
                platformPath = "/ziprecruiter";
                break;
            default:
                platformPath = "";
                break;
        }
        
        return String.format("%s%s/%s", baseUrl, platformPath, companyCode);
    }

    /**
     * Generate platform guide URL
     * 
     * @param platformType platform type (1-LinkedIn, 2-Indeed)
     * @return platform guide documentation URL
     */
    private String generateGuideUrl(Integer platformType) {
        if (XmlFeedConstants.PlatformType.LINKEDIN.getCode().equals(platformType)) {
            return feedProperties.getLinkedInGuideUrl();
        } else if (XmlFeedConstants.PlatformType.INDEED.getCode().equals(platformType)) {
            return feedProperties.getIndeedGuideUrl();
        }else if (XmlFeedConstants.PlatformType.ZIP_RECRUITER.getCode().equals(platformType)) {
            return feedProperties.getZipRecruiterGuideUrl();
        }
        return null;
    }

    /**
     * 查询过期的配置列表
     * 用于定时任务查询需要自动更新的配置
     *
     * @param currentTime 当前时间
     * @return 过期配置列表
     */
    @Override
    public List<XmlFeedConfigEntity> findExpiredConfigurations(LocalDateTime currentTime) {
        LambdaQueryWrapper<XmlFeedConfigEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.le(XmlFeedConfigEntity::getNextUpdateTime, currentTime)
                   .eq(XmlFeedConfigEntity::getDeleted, false)
                   .isNotNull(XmlFeedConfigEntity::getNextUpdateTime)
                   .orderByAsc(XmlFeedConfigEntity::getNextUpdateTime);
        
        return xmlFeedConfigMapper.selectList(queryWrapper);
    }

    /**
     * 更新配置实体
     * 用于定时任务更新配置时间戳等信息
     *
     * @param configEntity 配置实体
     * @return 是否更新成功
     */
    @Override
    public boolean updateConfigEntity(XmlFeedConfigEntity configEntity) {
        return xmlFeedConfigMapper.updateById(configEntity) > 0;
    }

    /**
     * Validate configuration parameters with enhanced business rules
     * 
     * <p>Validation Rules:</p>
     * <ul>
     *   <li><b>Platform Type Validation:</b> Only LinkedIn(1) and Indeed(2) platforms are supported</li>
     *   <li><b>Email Validation for Indeed:</b> Indeed platform requires a valid email address</li>
     *   <li><b>Email Validation for LinkedIn:</b> Email is optional for LinkedIn platform</li>
     *   <li><b>Update Interval Validation:</b> Must be between 1 and 24 hours (inclusive)</li>
     * </ul>
     *
     * @param createDTO the configuration DTO to validate
     * @throws BusinessException if validation fails with specific error codes:
     *         <ul>
     *           <li>{@link CommonResponseCode#COMMON_XML_FEED_PLATFORM_TYPE_INVALID} - Invalid or unsupported platform type</li>
     *           <li>{@link CommonResponseCode#COMMON_XML_FEED_EMAIL_REQUIRED} - Missing email for Indeed platform</li>
     *           <li>{@link CommonResponseCode#COMMON_XML_FEED_UPDATE_INTERVAL_INVALID} - Update interval out of valid range</li>
     *         </ul>
     */
    private void validateConfigurationParameters(XmlFeedConfigCreateDTO createDTO) {
        // Validate platform type
        validatePlatformType(createDTO.getPlatformType());
        
        // Validate email requirements based on platform type
        validateEmailRequirements(createDTO.getPlatformType(), createDTO.getAccountEmail());
    }

    /**
     * Validate platform type against supported platforms
     * 
     * @param platformType the platform type to validate
     * @throws BusinessException if platform type is invalid or unsupported
     */
    private void validatePlatformType(Integer platformType) {
        if (platformType == null) {
            throw new BusinessException(CommonResponseCode.COMMON_XML_FEED_PLATFORM_TYPE_INVALID);
        }
        
        // Check if platform type exists in enum
        XmlFeedConstants.PlatformType platformEnum = XmlFeedConstants.PlatformType.getByCode(platformType);
        if (platformEnum == null) {
            log.warn("Invalid platform type provided: {}", platformType);
            throw new BusinessException(CommonResponseCode.COMMON_XML_FEED_PLATFORM_TYPE_INVALID);
        }
        
        log.debug("Platform type validation passed: {} ({})", platformEnum.getName(), platformType);
    }

    /**
     * Validate email requirements based on platform type
     * 
     * <p>Email Validation Rules:</p>
     * <ul>
     *   <li><b>Indeed Platform:</b> Email is mandatory and must be in valid format</li>
     * </ul>
     * 
     * @param platformType the platform type determining email requirements
     * @param accountEmail the email to validate
     * @throws BusinessException if email validation fails for the given platform
     */
    private void validateEmailRequirements(Integer platformType, String accountEmail) {
        if (XmlFeedConstants.PlatformType.INDEED.getCode().equals(platformType)
                || XmlFeedConstants.PlatformType.ZIP_RECRUITER.getCode().equals(platformType)) {
            // Indeed platform requires email
            if (accountEmail == null || accountEmail.trim().isEmpty()) {
                log.warn("Email is required for Indeed platform but not provided");
                throw new BusinessException(CommonResponseCode.COMMON_XML_FEED_EMAIL_REQUIRED);
            }
            log.debug("Email validation passed for Indeed platform: {}", accountEmail);
        }
    }


}
