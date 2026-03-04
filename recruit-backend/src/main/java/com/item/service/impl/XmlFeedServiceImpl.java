package com.item.service.impl;

import com.item.dto.CityDTO;
import com.item.dto.CompanyInfoSimpleDTO;
import com.item.dto.CountryDTO;
import com.item.dto.CountryIsoDTO;
import com.item.dto.DictionaryDTO;
import com.item.dto.StateDTO;
import com.item.dto.job.LocationValDTO;
import com.item.dto.job.LocationValRecordDTO;
import com.item.es.JobEsService;
import com.item.es.entity.JobEsEntity;
import com.item.framework.config.FeedProperties;
import com.item.framework.constant.DictionaryEnum;
import com.item.framework.constant.JobStatus;
import com.item.framework.constant.XmlFeedConstants;
import com.item.service.CompanyService;
import com.item.service.DictionaryService;
import com.item.service.GenerateUrlCodeService;
import com.item.service.JobDomainService;
import com.item.service.LocationService;
import com.item.service.ShortIdGenerator;
import com.item.service.XmlFeedService;
import com.item.util.S3Utils;
import com.item.util.XmlBuilder;
import com.item.vo.feed.IndeedJobVO;
import com.item.vo.feed.IndeedSourceVO;
import com.item.vo.feed.LinkedJobVO;
import com.item.vo.feed.LinkedSalaryRangeVO;
import com.item.vo.feed.LinkedSalaryVO;
import com.item.vo.feed.LinkedSourceVO;
import com.item.vo.feed.ZipRecruiterJobVO;
import com.item.vo.feed.ZipRecruiterSourceVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 生成xml
 *
 * @author liyunlong
 * @version 1.0
 * @since 2025-08-26
 */
@RefreshScope
@Slf4j
@Service
@RequiredArgsConstructor
public class XmlFeedServiceImpl implements XmlFeedService {

    private final S3Utils s3Utils;
    private final JobEsService jobEsService;
    private final XmlBuilder xmlBuilder;
    private final FeedProperties feedProperties;
    private final DictionaryService dictionaryService;
    private final CompanyService companyService;
    private final GenerateUrlCodeService generateUrlCodeService;
    private final JobDomainService jobDomainService;
    private final ShortIdGenerator shortIdGenerator;
    private final LocationService locationService;

    @Value("${company.name.replace.str:,.，。#@%$}")
    private String companyNameReplace;


    /**
     * 生成linkedin xml
     */
    @Override
    public Integer generateLinkedInXML(String companyCode) {
        try {
            log.info("Generating LinkedIn XML companyCode:{}",companyCode);
            List<JobEsEntity> activeJobs = jobEsService.searchFeedXmlJobs(JobStatus.ACTIVE.getCode(), companyCode, feedProperties.getSize(),feedProperties.getLinkedInDistinct());
            generateJobsXML(XmlFeedConstants.PlatformType.LINKEDIN.getCode(),companyCode,null, activeJobs);
            log.info("LinkedIn XML generated and uploaded companyCode:{},size:{}",companyCode,activeJobs.size());
            return activeJobs.size();
        } catch (Exception e) {
            log.error("Failed to generate LinkedIn XML size",e);
            throw new RuntimeException("Failed to generate LinkedIn XML ", e);
        }
    }

    /**
     * 生成Indeed xml
     */
    @Override
    public Integer generateIndeedXML(String companyCode,String email) {
        try {
            log.info("Generating Indeed XML companyCode:{},email:{}",companyCode,email);
            List<JobEsEntity> activeJobs = jobEsService.searchFeedXmlJobs(JobStatus.ACTIVE.getCode(), companyCode,feedProperties.getSize(),false );
            generateJobsXML(XmlFeedConstants.PlatformType.INDEED.getCode(),companyCode,email, activeJobs);
            log.info("Indeed XML generated and uploaded companyCode:{},size:{}",companyCode,activeJobs.size());
            return activeJobs.size();
        } catch (Exception e) {
            log.error("Failed to generate Indeed XML companyCode:{},email:{}",companyCode,email,e);
            throw new RuntimeException("Failed to generate Indeed XML", e);
        }
    }

    /**
     * 生成ZipRecruiter xml
     */
    @Override
    public Integer generateZipRecruiterXML(String companyCode,String email) {
        try {
            log.info("Generating ZipRecruiter XML companyCode:{},email:{}",companyCode,email);
            List<JobEsEntity> activeJobs = jobEsService.searchFeedXmlJobs(JobStatus.ACTIVE.getCode(), companyCode, feedProperties.getSize(),false);
            generateJobsXML(XmlFeedConstants.PlatformType.ZIP_RECRUITER.getCode(),companyCode,email, activeJobs);
            log.info("ZipRecruiter XML generated and uploaded companyCode:{},size:{}",companyCode,activeJobs.size());
            return activeJobs.size();
        } catch (Exception e) {
            log.error("Failed to generate ZipRecruiter XML companyCode:{},email:{}",companyCode,email,e);
            throw new RuntimeException("Failed to generate ZipRecruiter XML", e);
        }
    }

    /**
     * 生成xml
     */
    @Override
    public void generateJobsXML(Integer platformType,String companyCode, String email, List<JobEsEntity> activeJobs) {
        if (XmlFeedConstants.PlatformType.LINKEDIN.getCode().equals(platformType)) {
            LinkedSourceVO linkedInXML = getLinkedInXMLObject(activeJobs);
            String linkedInXMLContent = xmlBuilder.buildXmlFeed(linkedInXML);
            s3Utils.uploadFeedXmlToS3(feedProperties.getLinkedInXml(),companyCode,linkedInXMLContent);
        } else if (XmlFeedConstants.PlatformType.INDEED.getCode().equals(platformType)) {
            IndeedSourceVO indeedSourceVO = getIndeedXMLObject(activeJobs,email);
            String indeedXMLContent = xmlBuilder.buildXmlFeed(indeedSourceVO);
            s3Utils.uploadFeedXmlToS3(feedProperties.getIndeedXml(),companyCode,indeedXMLContent);
        }else if (XmlFeedConstants.PlatformType.ZIP_RECRUITER.getCode().equals(platformType)) {
            ZipRecruiterSourceVO sourceVO = getZipRecruiterXMLObject(activeJobs,email);
            String zipRecruiterXMLContent = xmlBuilder.buildXmlFeed(sourceVO);
            s3Utils.uploadFeedXmlToS3(feedProperties.getZipRecruiterXml(),companyCode,zipRecruiterXMLContent);
        }
    }

    /**
     * 获取LinkedIn XML对象
     */
    private IndeedSourceVO getIndeedXMLObject(List<JobEsEntity> activeJobs,String email){
        IndeedSourceVO indeed=new IndeedSourceVO();
//        indeed.setPublisher(feedProperties.getPublisher());
//        indeed.setPublisherurl(feedProperties.getPublisherUrl());
        List<IndeedJobVO> jobs=new ArrayList<>();
        if (CollectionUtils.isNotEmpty(activeJobs)){
            List<DictionaryDTO> dictionaryDTOS = dictionaryService.listByTypes(Arrays.asList(DictionaryEnum.REPORT.getName()));
            Map<Long,DictionaryDTO> dictionaryMap=dictionaryDTOS.stream().collect(Collectors.toMap(DictionaryDTO::getId, DictionaryDTO -> DictionaryDTO));
            Map<String,CompanyInfoSimpleDTO> companyInfoMap = getCompanyInfoMap(activeJobs.stream().map(JobEsEntity::getCompanyCode).collect(Collectors.toList()));
            for (JobEsEntity job:activeJobs){
                if (companyInfoMap.containsKey(job.getCompanyCode())){
                    IndeedJobVO linkedJobVO = buildInDeedJob(job,dictionaryMap,companyInfoMap,email);
                    jobs.add(linkedJobVO);
                }
            }

        }
        indeed.setJobs(jobs);
        return indeed;
    }


    /**
     * 获取LinkedIn XML对象
     */
    private LinkedSourceVO getLinkedInXMLObject(List<JobEsEntity> activeJobs) {
        LinkedSourceVO feed = new LinkedSourceVO();
        //feed.setLastBuildDate("Fri, 22 Aug 2025 10:00:00 GMT");
        List<LinkedJobVO> jobs=new ArrayList<>();
        if (CollectionUtils.isNotEmpty(activeJobs)){
            Map<String,CompanyInfoSimpleDTO> companyInfoMap = getCompanyInfoMap(activeJobs.stream().map(JobEsEntity::getCompanyCode).collect(Collectors.toList()));
            for (JobEsEntity job:activeJobs){
                if (companyInfoMap.containsKey(job.getCompanyCode())){
                    LinkedJobVO linkedJobVO = buildLinkedInJob(job,companyInfoMap);
                    jobs.add(linkedJobVO);
                }
            }

        }
        feed.setJob(jobs);
        return feed;
    }

    /**
     * 获取LinkedIn XML对象
     */
    private ZipRecruiterSourceVO getZipRecruiterXMLObject(List<JobEsEntity> activeJobs,String email){
        ZipRecruiterSourceVO sourceVO=new ZipRecruiterSourceVO();
        // 当前时间
        LocalDateTime now = LocalDateTime.now();
        // 转换成 GMT 时区
        ZonedDateTime gmtTime = now.atZone(ZoneId.systemDefault())
                .withZoneSameInstant(ZoneId.of("GMT"));
        // 格式化成 RFC1123 样式
        String lastBuildDate = gmtTime.format(DateTimeFormatter.RFC_1123_DATE_TIME);
        sourceVO.setLastBuildDate(lastBuildDate);
//        sourceVO.setPublisher(feedProperties.getPublisher());
//        sourceVO.setPublisherurl(feedProperties.getPublisherUrl());
        List<ZipRecruiterJobVO> jobs=new ArrayList<>();
        if (CollectionUtils.isNotEmpty(activeJobs)){
            List<CountryIsoDTO> countryIsoDTOS = locationService.selectAllCountryIso();
            Map<Long,CountryIsoDTO> countryIsoMap=countryIsoDTOS.stream().collect(Collectors.toMap(CountryIsoDTO::getId, CountryIsoDTO -> CountryIsoDTO));
            Map<String,CompanyInfoSimpleDTO> companyInfoMap = getCompanyInfoMap(activeJobs.stream().map(JobEsEntity::getCompanyCode).collect(Collectors.toList()));
            for (JobEsEntity job:activeJobs){
                if (companyInfoMap.containsKey(job.getCompanyCode())){
                    ZipRecruiterJobVO jobVO = buildZipRecruiterJob(job,countryIsoMap,companyInfoMap,email);
                    jobs.add(jobVO);
                }
            }

        }
        sourceVO.setJobs(jobs);
        return sourceVO;
    }

    /**
     * 获取公司信息
     * @param companyCodes
     * @return
     */
    private Map<String,CompanyInfoSimpleDTO> getCompanyInfoMap(List<String> companyCodes){
        Map<String,CompanyInfoSimpleDTO> companyInfoMap = new HashMap<>();
        if (CollectionUtils.isNotEmpty(companyCodes)){
            for (String companyCode:companyCodes){
                if (!companyInfoMap.containsKey(companyCode)){
                    try{
                        CompanyInfoSimpleDTO companyInfoSimpleDTO = companyService.getCompanyInfoByCode(companyCode);
                        companyInfoMap.put(companyCode,companyInfoSimpleDTO);
                    }catch (Exception e){
                        log.error("Failed to get company info for companyCode: {}", companyCode, e);
                    }
                }
            }
        }
        return companyInfoMap;
    }


    private LinkedJobVO buildLinkedInJob(JobEsEntity job,Map<String,CompanyInfoSimpleDTO> companyInfoMap) {
        LinkedJobVO linkedInJob = new LinkedJobVO();
        // 必需字段
        linkedInJob.setPartnerJobId(job.getId().toString());
        String companyName = "";
        if (companyInfoMap.containsKey(job.getCompanyCode())){
            companyName=companyInfoMap.get(job.getCompanyCode()).getName();
        }
        linkedInJob.setCompany(StringUtils.isEmpty(companyName)?job.getCompanyCode():companyName);
        linkedInJob.setTitle(job.getTitle());
        linkedInJob.setDescription(buildLinkedInJobDescription(job));

        String urlCode = generateUrlCodeService.generateUrlCodeByConfig(job.getTitle(), job.getUrlCode(), companyName, companyNameReplace);
        String idStr = shortIdGenerator.generateShortId(job.getId());
        String shareLink = jobDomainService.generateInfoShareLink(idStr, urlCode);
        linkedInJob.setApplyUrl(shareLink);
        linkedInJob.setLocation(linkedBuildLocationString(job));
        linkedInJob.setListDate(job.getCreateTime().format(DateTimeFormatter.ofPattern("MM/dd/yyyy")));
        linkedInJob.setWorkplaceTypes(job.getModeName());
        // 薪资信息
        if (job.getMinSalary() != null && job.getMaxSalary() != null && job.getSalaryType()!=null) {
            LinkedSalaryVO linkedSalaryVO = linkedBuildSalaryObject(job);
            List<LinkedSalaryVO> linkedSalaryVOS =new ArrayList<>();
            linkedSalaryVOS.add(linkedSalaryVO);
            linkedInJob.setSalaries(linkedSalaryVOS);
        }
        // 职位类型
        if (job.getTypeName() != null && StringUtils.isNotEmpty(linkedConvertJobType(job.getTypeName()))) {
            linkedInJob.setJobtype(linkedConvertJobType(job.getTypeName()));
        }

        // 技能标签
        if (job.getSkills() != null && !job.getSkills().isEmpty()) {
            linkedInJob.setSkills(linkedBuildSkillsObject(job.getSkills()));
        }

        return linkedInJob;
    }


    private IndeedJobVO buildInDeedJob(JobEsEntity job,Map<Long,DictionaryDTO> dictionaryMap,Map<String,CompanyInfoSimpleDTO> companyInfoMap,String email) {
        IndeedJobVO indeed=new IndeedJobVO();
        indeed.setTitle(job.getTitle());
        LocalDateTime createTime= job.getCreateTime();
        createTime = createTime.truncatedTo(ChronoUnit.SECONDS);
        indeed.setDate(DateTimeFormatter.ISO_INSTANT.format(createTime.toInstant(ZoneOffset.UTC)));
        indeed.setReferencenumber(String.valueOf(job.getId()));
        indeed.setRequisitionid(String.valueOf(job.getId()));
        String companyName = "";
        if (companyInfoMap.containsKey(job.getCompanyCode())){
            companyName=companyInfoMap.get(job.getCompanyCode()).getName();
        }
        indeed.setCompany(companyName);
        String urlCode = generateUrlCodeService.generateUrlCodeByConfig(job.getTitle(), job.getUrlCode(), companyName, companyNameReplace);
        String idStr = shortIdGenerator.generateShortId(job.getId());
        String shareLink = jobDomainService.generateInfoShareLink(idStr, urlCode);
        indeed.setUrl(shareLink+"?source=Indeed");
        //地点
        List<LocationValRecordDTO> locations = job.getLocations();
        if (CollectionUtils.isNotEmpty(locations)){
            LocationValRecordDTO location = locations.getFirst();
            String cityName="";
            String stateName="";
            String countryName="";

            if (StringUtils.isNotBlank(location.getPlaceId())) {
                LocationValDTO locationValDTO = locationService.buildLocationStringByPlaceId(location.getPlaceId());
                if (locationValDTO != null) {
                    cityName = locationValDTO.getCityName();
                    stateName = locationValDTO.getStateName();
                    countryName = locationValDTO.getCountryName();
                }
            }

            CountryDTO country = locationService.getCountryById(location.getCountryId());
            if (country!=null){
                countryName=country.getName();
            }
            List<StateDTO> stateDTOS = locationService.listEnStateByStateIds(Collections.singletonList(location.getStateId()));
            if (CollectionUtils.isNotEmpty(stateDTOS)){
                stateName=stateDTOS.getFirst().getName();
            }
            List<CityDTO> cityDTOS = locationService.listEnCityByCityIds(Collections.singletonList(location.getCityId()));
            if (CollectionUtils.isNotEmpty(cityDTOS)){
                cityName=cityDTOS.getFirst().getName();
            }
            indeed.setCity(cityName);
            indeed.setState(stateName);
            indeed.setCountry(countryName);
        }
        indeed.setEmail(StringUtils.isEmpty(email)?feedProperties.getIndeedEmail():email);
        indeed.setDescription(buildIndeedJobDescription(job));
        if (StringUtils.isNotEmpty(job.getTypeName())) {
            indeed.setJobtype(indeedConvertJobType(job.getTypeName()));
        }
        indeed.setSalary(indeedBuildSalaryString(job,dictionaryMap));
        return indeed;
    }


    /**
     * 构建ZipRecruiterJobVO
     * @param job
     * @param countryIsoMap
     * @param companyInfoMap
     * @param email
     * @return
     */
    private ZipRecruiterJobVO buildZipRecruiterJob(JobEsEntity job,Map<Long,CountryIsoDTO> countryIsoMap,
                               Map<String,CompanyInfoSimpleDTO> companyInfoMap,String email) {
        ZipRecruiterJobVO jobVO=new ZipRecruiterJobVO();
        jobVO.setReferenceNumber(String.valueOf(job.getId()));
        jobVO.setTitle(job.getTitle());
        jobVO.setDescription(buildZipRecruiterJobDescription(job));
        List<LocationValRecordDTO> locations = job.getLocations();
        if (CollectionUtils.isNotEmpty(locations)){
            Long countryId=locations.getFirst().getCountryId();
            if (countryIsoMap.containsKey(countryId)){
                jobVO.setCountry(countryIsoMap.get(countryId).getIso2());
            }
            String cityName="";
            String stateName="";
            if (StringUtils.isNotBlank(locations.getFirst().getPlaceId())) {
                LocationValDTO locationValDTO = locationService.buildLocationStringByPlaceId(locations.getFirst().getPlaceId());
                if (locationValDTO != null) {
                    jobVO.setCountry(locationValDTO.getCountryName());
                    cityName = locationValDTO.getCityName();
                    stateName = locationValDTO.getStateName();
                }
            }
            List<StateDTO> stateDTOS = locationService.listEnStateByStateIds(Collections.singletonList(locations.getFirst().getStateId()));
            if (CollectionUtils.isNotEmpty(stateDTOS)){
                stateName=stateDTOS.getFirst().getName();
            }
            List<CityDTO> cityDTOS = locationService.listEnCityByCityIds(Collections.singletonList(locations.getFirst().getCityId()));
            if (CollectionUtils.isNotEmpty(cityDTOS)){
                cityName=cityDTOS.getFirst().getName();
            }
            jobVO.setState(stateName);
            jobVO.setCity(cityName);
        }

        String companyName = "";
        if (companyInfoMap.containsKey(job.getCompanyCode())){
            companyName=companyInfoMap.get(job.getCompanyCode()).getName();
        }
        jobVO.setCompany(companyName);
        jobVO.setDate(job.getCreateTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        jobVO.setEmail(email);
        String urlCode = generateUrlCodeService.generateUrlCodeByConfig(job.getTitle(), job.getUrlCode(), companyName, companyNameReplace);
        String idStr = shortIdGenerator.generateShortId(job.getId());
        String shareLink = jobDomainService.generateInfoShareLink(idStr, urlCode);
        jobVO.setUrl(shareLink+"?source=ziprecruiter-feed");

        if (StringUtils.isNotEmpty(job.getTypeName())) {
            jobVO.setJobType(zipRecruiterConvertJobType(job.getTypeName()));
        }
        jobVO.setCompensationInterval(zipRecruiterConvertSalaryType(job.getSalaryTypeName()));
        if(job.getMinSalary()!=null){
            jobVO.setCompensationMin(job.getMinSalary().doubleValue());
        }
        if (job.getMaxSalary()!=null){
            jobVO.setCompensationMax(job.getMaxSalary().doubleValue());
        }
        jobVO.setCompensationCurrency(job.getCurrencyName());
        return jobVO;
    }

    /**
     * 构建技能对象
     */
    private List<String> linkedBuildSkillsObject(List<String> skills) {
        return skills.stream().limit(10).collect(Collectors.toList());
    }

    /**
     * 构建地址对象
     */
    private String linkedBuildLocationString(JobEsEntity job) {
        List<LocationValRecordDTO> locations = job.getLocations();
        if (CollectionUtils.isNotEmpty(locations)){
            LocationValRecordDTO location=locations.getFirst();
            String cityName="";
            String stateName="";
            String countryName="";
            if (StringUtils.isNotBlank(location.getPlaceId())) {
                LocationValDTO locationValDTO = locationService.buildLocationStringByPlaceId(location.getPlaceId());
                if (locationValDTO != null) {
                    cityName = locationValDTO.getCityName();
                    stateName = locationValDTO.getStateName();
                    countryName = locationValDTO.getCountryName();
                }
            }

            CountryDTO country = locationService.getCountryById(location.getCountryId());
            if (country!=null){
                countryName=country.getName();
            }
            List<StateDTO> stateDTOS = locationService.listEnStateByStateIds(Collections.singletonList(location.getStateId()));
            if (CollectionUtils.isNotEmpty(stateDTOS)){
                stateName=stateDTOS.getFirst().getName();
            }
            List<CityDTO> cityDTOS = locationService.listEnCityByCityIds(Collections.singletonList(location.getCityId()));
            if (CollectionUtils.isNotEmpty(cityDTOS)){
                cityName=cityDTOS.getFirst().getName();
            }
            return String.format("%s, %s, %s", cityName, stateName, countryName);
        }
        return "";
    }

    /**
     * 构建薪资对象
     */
    private LinkedSalaryVO linkedBuildSalaryObject(JobEsEntity job) {
        LinkedSalaryVO salaryObj = new LinkedSalaryVO();

        if (job.getMaxSalary() != null) {
            LinkedSalaryRangeVO salaryRange=new LinkedSalaryRangeVO();
            salaryRange.setAmount(String.valueOf(job.getMaxSalary()));
            salaryRange.setCurrencyCode(job.getCurrencyName());
            salaryObj.setHighEnd(salaryRange);
        }

        if (job.getMinSalary() != null) {
            LinkedSalaryRangeVO salaryRange=new LinkedSalaryRangeVO();
            salaryRange.setAmount(String.valueOf(job.getMinSalary()));
            salaryRange.setCurrencyCode(job.getCurrencyName());
            salaryObj.setLowEnd(salaryRange);
        }
        if (job.getSalaryType() != null) {
            String period = linkedConvertSalaryType(job.getSalaryTypeName());
            if (period != null) {
                salaryObj.setPeriod(period);
            }
        }
        salaryObj.setType("BASE_SALARY");
        return salaryObj;
    }

    /**
     * 薪资转换
     */
    private String indeedBuildSalaryString(JobEsEntity job,Map<Long,DictionaryDTO> dictionaryMap) {
        if (job.getMinSalary() == null && job.getMaxSalary() == null) {
            return null;
        }
        Integer currencyId = job.getCurrency();
        if (currencyId==null){
            return null;
        }
        String currencySymbol="";
        if (dictionaryMap.containsKey(Long.valueOf(currencyId))){
            currencySymbol=dictionaryMap.get(Long.valueOf(currencyId)).getValue();
        }

        StringBuilder salaryStr = new StringBuilder();
        if (job.getMinSalary() != null && job.getMaxSalary() != null) {
            salaryStr.append(currencySymbol)
                    .append(job.getMinSalary().intValue())
                    .append(" - ")
                    .append(currencySymbol)
                    .append(job.getMaxSalary().intValue());
        } else if (job.getMinSalary() != null) {
            salaryStr.append(currencySymbol)
                    .append(job.getMinSalary().intValue());
        }

        if (job.getSalaryType() != null) {
            String frequency = indeedConvertSalaryType(job.getSalaryTypeName());
            if(StringUtils.isEmpty(frequency)){
                return null;
            }
            salaryStr.append(" ").append(frequency);
        }

        return salaryStr.toString();
    }

    /**
     * 转换薪资类型
     */
    private String linkedConvertSalaryType(String salaryType) {
        switch (salaryType) {
            case "Yearly": return "YEARLY";
            case "Monthly": return "MONTHLY";
            case "Weekly": return "WEEKLY";
            case "Daily": return "DAILY";
            case "Hourly": return "HOURLY";
            default: return null;
        }
    }

    /**
     * 转换职位类型
     */
    private String linkedConvertJobType(String type) {
        switch (type) {
            case "Full Time": return "FULL_TIME";
            case "Part Time": return "PART_TIME";
            case "Contract": return "CONTRACT";
            case "Internship": return "INTERNSHIP";
            default: return null;
        }
    }

    /**
     * 转换薪资类型
     */
    private String indeedConvertSalaryType(String salaryType) {
        switch (salaryType) {
            case "Yearly": return "per year";
            case "Monthly": return "per month";
            case "Weekly": return "per week";
            case "Daily": return "per dail";
            case "Hourly": return "per hour";
            default: return null;
        }
    }

    /**
     * 转换薪资类型
     */
    private String zipRecruiterConvertSalaryType(String salaryType) {
        switch (salaryType) {
            case "Yearly": return "Annually";
            case "Monthly": return "Monthly";
            case "Weekly": return "Weekly";
            case "Daily": return "Daily";
            case "Hourly": return "Hourly";
            default: return null;
        }
    }


    /**
     * 转换职位类型
     */
    private String indeedConvertJobType(String type) {
        switch (type) {
            case "Full Time": return "fulltime";
            case "Part Time": return "parttime";
            case "Contract": return "contract";
            case "Internship": return "internship";
            default: return null;
        }
    }

    /**
     * 转换职位类型
     */
    private String zipRecruiterConvertJobType(String type) {
        switch (type) {
            case "Full Time": return "Full-Time";
            case "Part Time": return "Part-Time";
            case "Contract": return "Contractor";
            case "Temporary": return "Temporary";
            default: return "other";
        }
    }

    /**
     * 生成indeed职位描述
     * @param job
     * @return
     */
    private String buildIndeedJobDescription(JobEsEntity job) {
        StringBuilder sb = new StringBuilder();
        // Overview
        sb.append("<h2>Overview</h2>");
        sb.append("<p>").append(job.getJobDetail()).append("</p>");

         // Responsibilities
        if (job.getMainDuty() != null && !job.getMainDuty().isEmpty()) {
            sb.append("<h3>Responsibilities</h3><ul>");
            job.getMainDuty().forEach(r ->
                    sb.append("<li>").append(r).append("</li>")
            );
            sb.append("</ul>");
        }

        // Minimum Requirements
        if (job.getMinimumJobRequirement() != null && !job.getMinimumJobRequirement().isEmpty()) {
            sb.append("<h3>Minimum Requirements</h3><ul>");
            job.getMinimumJobRequirement().forEach(r ->
                    sb.append("<li>").append(r).append("</li>")
            );
            sb.append("</ul>");
        }

        // Preferred Requirements
        if (job.getPreferredJobRequirement() != null && !job.getPreferredJobRequirement().isEmpty()) {
            sb.append("<h3>Preferred Requirements</h3><ul>");
            job.getPreferredJobRequirement().forEach(r ->
                    sb.append("<li>").append(r).append("</li>")
            );
            sb.append("</ul>");
        }
        // Skills
        if (job.getSkills() != null && !job.getSkills().isEmpty()) {
            sb.append("<h3>Skills</h3><ul>");
            job.getSkills().forEach(r ->
                    sb.append("<li>").append(r).append("</li>")
            );
            sb.append("</ul>");
        }
        return sb.toString();
    }

    /**
     * 生成zipRecruiter职位描述
     * @param job
     * @return
     */
    private String buildZipRecruiterJobDescription(JobEsEntity job) {
        StringBuilder sb = new StringBuilder();
        // Overview
        sb.append("<h2>Overview</h2>");
        sb.append("<p>").append(job.getJobDetail()).append("</p>");

        // Responsibilities
        if (job.getMainDuty() != null && !job.getMainDuty().isEmpty()) {
            sb.append("<h3>Responsibilities</h3><ul>");
            job.getMainDuty().forEach(r ->
                    sb.append("<li>").append(r).append("</li>")
            );
            sb.append("</ul>");
        }

        // Minimum Requirements
        if (job.getMinimumJobRequirement() != null && !job.getMinimumJobRequirement().isEmpty()) {
            sb.append("<h3>Minimum Requirements</h3><ul>");
            job.getMinimumJobRequirement().forEach(r ->
                    sb.append("<li>").append(r).append("</li>")
            );
            sb.append("</ul>");
        }

        // Preferred Requirements
        if (job.getPreferredJobRequirement() != null && !job.getPreferredJobRequirement().isEmpty()) {
            sb.append("<h3>Preferred Requirements</h3><ul>");
            job.getPreferredJobRequirement().forEach(r ->
                    sb.append("<li>").append(r).append("</li>")
            );
            sb.append("</ul>");
        }
        // Skills
        if (job.getSkills() != null && !job.getSkills().isEmpty()) {
            sb.append("<h3>Skills</h3><ul>");
            job.getSkills().forEach(r ->
                    sb.append("<li>").append(r).append("</li>")
            );
            sb.append("</ul>");
        }
        return sb.toString();
    }

    /**
     * 生成LinkedIn职位描述
     * @param job
     * @return
     */
    private String buildLinkedInJobDescription(JobEsEntity job) {
        StringBuilder sb = new StringBuilder();
        // Overview
        sb.append("<strong>Overview:</strong>");
        sb.append("<p>").append(job.getJobDetail()).append("</p>");

        // Responsibilities
        if (job.getMainDuty() != null && !job.getMainDuty().isEmpty()) {
            sb.append("<b>Responsibilities:</b><ul>");
            job.getMainDuty().forEach(r ->
                    sb.append("<li>").append(r).append("</li>")
            );
            sb.append("</ul>");
        }

        // Minimum Requirements
        if (job.getMinimumJobRequirement() != null && !job.getMinimumJobRequirement().isEmpty()) {
            sb.append("<b>Minimum Requirements:</b><ul>");
            job.getMinimumJobRequirement().forEach(r ->
                    sb.append("<li>").append(r).append("</li>")
            );
            sb.append("</ul>");
        }

        // Preferred Requirements
        if (job.getPreferredJobRequirement() != null && !job.getPreferredJobRequirement().isEmpty()) {
            sb.append("<b>Preferred Requirements:</b><ul>");
            job.getPreferredJobRequirement().forEach(r ->
                    sb.append("<li>").append(r).append("</li>")
            );
            sb.append("</ul>");
        }
//        // Skills
//        if (job.getSkills() != null && !job.getSkills().isEmpty()) {
//            sb.append("<b>Skills:</b><ul>");
//            job.getSkills().forEach(r ->
//                    sb.append("<li>").append(r).append("</li>")
//            );
//            sb.append("</ul>");
//        }
        return sb.toString();
    }




    @Override
    public String getLinkedInXml(String companyCode) {
        return s3Utils.getFeedXmlFromS3(feedProperties.getLinkedInXml(),companyCode);
    }

    @Override
    public String getIndeedXml(String companyCode) {
        return s3Utils.getFeedXmlFromS3(feedProperties.getIndeedXml(),companyCode);
    }

    @Override
    public String getZipRecruiterXml(String companyCode) {
        return s3Utils.getFeedXmlFromS3(feedProperties.getZipRecruiterXml(),companyCode);
    }
}