package com.item.convert;

import co.elastic.clients.elasticsearch._types.GeoLocation;
import com.item.dto.ai.GenerateJobRequestDTO;
import com.item.dto.ai.JobDescriptionDTO;
import com.item.dto.ai.JobResponseDataDTO;
import com.item.dto.job.GenerateJobDescDTO;
import com.item.dto.job.GeoPointDTO;
import com.item.dto.job.IntelligenceScoreRuleDTO;
import com.item.dto.job.JobCreateBO;
import com.item.dto.job.JobCreateDTO;
import com.item.dto.job.JobCreateRequestVO;
import com.item.dto.job.JobUpdateBO;
import com.item.dto.job.JobUpdateDTO;
import com.item.dto.job.JobUpdateRequestVO;
import com.item.dto.job.LocationDTO;
import com.item.dto.job.LocationValDTO;
import com.item.dto.job.LocationValRecordDTO;
import com.item.entity.JobEntity;
import com.item.es.entity.JobEsEntity;
import com.item.framework.constant.JobStatus;
import com.item.vo.IntelligenceScoreRuleRequestDTO;
import com.item.vo.IntelligenceScoreRuleVO;
import com.item.vo.JobDetailVO;
import com.item.vo.JobHistoryDetailVO;
import com.item.vo.JobListVO;
import com.item.vo.JobOptionVO;
import com.item.vo.ai.AIJobDescriptionVO;
import com.item.vo.ai.AIJobResponseDataVO;
import org.apache.commons.collections4.CollectionUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.util.List;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface JobConvert {
    JobConvert INSTANCE = Mappers.getMapper(JobConvert.class);

    JobEntity toJob(JobCreateBO dto);

    @Mapping(source = "id", target = "jobId")
    JobDetailVO toJobVO(JobEntity jobEntity);

    @Mapping(source = "jobStatus", target = "jobStatusName", qualifiedByName = "convertName")
    @Mapping(source = "companyTitleHash", target = "companyTitleHash")
    @Mapping(source = "locations", target = "locations", qualifiedByName = "convertToGenLocation")
    JobEsEntity toJobEsFromDTO(JobCreateBO dto);

    JobCreateBO toJobCreateBoFromEntity(JobEsEntity entity);

    JobCreateBO toJobCreateBO(JobCreateDTO dto);

    @Mapping(target = "checkpointEnabled", source = "checkpointEnabled", defaultValue = "false")
    @Mapping(target = "customQuestionEnabled", source = "customQuestionEnabled", defaultValue = "false")
    @Mapping(target = "personalityTestEnabled", source = "personalityTestEnabled", defaultValue = "false")
    JobCreateDTO toJobCreateDTO(JobCreateRequestVO vo);

    /**
     * JobUpdateRequestVO -> JobUpdateDTO
     */
    @Mapping(target = "checkpointEnabled", source = "checkpointEnabled", defaultValue = "false")
    @Mapping(target = "customQuestionEnabled", source = "customQuestionEnabled", defaultValue = "false")
    @Mapping(target = "personalityTestEnabled", source = "personalityTestEnabled", defaultValue = "false")
    JobUpdateDTO toJobUpdateDTO(JobUpdateRequestVO vo);

    /**
     * JobUpdateDTO -> JobUpdateBO
     */
    JobUpdateBO toJobUpdateBO(JobUpdateDTO dto);

    /**
     * JobUpdateBO -> JobEntity
     */
    @Mapping(source = "jobId", target = "id")
    JobEntity convertFromUpdateBO(JobUpdateBO bo);

    @Mapping(source = "id", target = "jobId")
    @Mapping(source = "locations", target = "locations", qualifiedByName = "convertToGenLocationDTO")
    @Mapping(source = "ayrshareStatus", target = "ayrshareStatus", qualifiedByName = "convertAyrShareStatus")
    @Mapping(source = "createBy", target = "createUserId")
    JobListVO converToJobListVO(JobEsEntity esEntity);

    /**
     * JobUpdateBO -> JobEsEntity
     */
    @Mapping(source = "jobStatus", target = "jobStatusName", qualifiedByName = "convertName")
    @Mapping(source = "companyTitleHash", target = "companyTitleHash")
    @Mapping(source = "jobId", target = "id")
    @Mapping(source = "locations", target = "locations", qualifiedByName = "convertToGenLocation")
    void updateEsEntityFromUpdateBO(JobUpdateBO bo, @MappingTarget JobEsEntity entity);

    @Mapping(source = "id", target = "jobId")
    JobOptionVO toJobOption(JobEntity list);

    List<JobOptionVO> toJobOption(List<JobEntity> list);

    @Mapping(source = "currencyName", target = "currencySimpleDesc")
    @Mapping(source = "locations", target = "locations",  qualifiedByName = "convert2GenLocationDTO")
    JobHistoryDetailVO toJobHistoryDetail(JobEsEntity esEntity);

    @Named("convertName")
    static String convert(Integer code) {
        if (code == null) {
            return "";
        }
        return JobStatus.getByCode(code).getDescription();
    }

    @Named("convertBigDecimalToDouble")
    static Double convertBigDecimalToDouble(BigDecimal value) {
        if (value == null) {
            return null;
        }
        return value.doubleValue();
    }

    @Named("convertToGenLocation")
    static List<LocationValRecordDTO> convertToGenLocation(List<LocationValDTO> locations) {
        if (CollectionUtils.isEmpty(locations)) {
            return null;
        }
        return locations.stream().map(lr -> {
            GeoPointDTO geoPoint = lr.getGeoPoint();
            GeoLocation geoLocation = null;
            if (geoPoint != null) {
                geoLocation = GeoLocation.of(g -> g.latlon(l -> l.lat(geoPoint.getLatitude()).lon(geoPoint.getLongitude())));
            }

            return new LocationValRecordDTO(
                    lr.getCountryId(),
                    lr.getCountryName(),
                    lr.getStateId(),
                    lr.getStateName(),
                    lr.getCityId(),
                    lr.getCityName(),
                    lr.getLocationName(),
                    geoLocation,
                    lr.getPlaceId(),
                    lr.getPlaceName()
            );

        }).toList();
    }

    @Named("convertToGenLocationDTO")
    static List<LocationValDTO> convertToGenLocationDTO(List<LocationValRecordDTO> locations) {
        if (CollectionUtils.isEmpty(locations)) {
            return null;
        }
        return locations.stream().map(lr -> {
            GeoLocation geoPoint = lr.getGeoPoint();
            GeoPointDTO geoLocation = null;
            if (geoPoint != null) {
                geoLocation = new GeoPointDTO();
                geoLocation.setLatitude(geoPoint.latlon().lat());
                geoLocation.setLongitude(geoPoint.latlon().lon());
            }
            return getLocationValDTO(lr, geoLocation);

        }).toList();
    }

    @Named("convertAyrShareStatus")
    static Integer convertAyrShareStatus(Integer ayrshareStatus) {
        if (ayrshareStatus == null) {
            return 0;
        }
        return ayrshareStatus;
    }

    @Named("convert2GenLocationDTO")
    static List<LocationDTO> convert2GenLocationDTO(List<LocationValRecordDTO> geo) {
        if (CollectionUtils.isEmpty(geo)) {
            return List.of();
        }
        return geo.stream().map(lr -> {
            GeoLocation geoPoint = lr.getGeoPoint();
            LocationDTO locationDTO = new LocationDTO();
            GeoPointDTO geoLocation = null;
            if (geoPoint != null) {
                geoLocation = new GeoPointDTO();
                geoLocation.setLatitude(geoPoint.latlon().lat());
                geoLocation.setLongitude(geoPoint.latlon().lon());
            }
            locationDTO.setCountryId(lr.getCountryId());
            locationDTO.setCountryName(lr.getCountryName());
            locationDTO.setStateId(lr.getStateId());
            locationDTO.setCityId(lr.getCityId());
            locationDTO.setCityName(lr.getCityName());
            locationDTO.setStateName(lr.getStateName());
            locationDTO.setLocationName(lr.getLocationName());
            locationDTO.setGeoPoint(geoLocation);
            return locationDTO;

        }).toList();
    }

    private static LocationValDTO getLocationValDTO(LocationValRecordDTO lr, GeoPointDTO geoLocation) {
        LocationValDTO locationValDTO = new LocationValDTO();
        locationValDTO.setLocationName(lr.getLocationName());
        locationValDTO.setCountryName(lr.getCountryName());
        locationValDTO.setStateName(lr.getStateName());
        locationValDTO.setCityName(lr.getCityName());
        locationValDTO.setCityId(lr.getCityId());
        locationValDTO.setCountryId(lr.getCountryId());
        locationValDTO.setStateId(lr.getStateId());
        locationValDTO.setGeoPoint(geoLocation);
        return locationValDTO;
    }

    AIJobResponseDataVO toAIJobResponseDataVO(JobResponseDataDTO jobResponseDataDTO);

    @Mapping(source = "workplace", target = "locations")
    @Mapping(source = "minimumSalary", target = "minSalary")
    @Mapping(source = "maximumSalary", target = "maxSalary")
    @Mapping(source = "numberOfPositions", target = "numberOpenings")
    @Mapping(source = "salaryTypeId", target = "salaryType")
    AIJobDescriptionVO toAIJobDescriptionVO(JobDescriptionDTO jobDescriptionDTO);

    @Mapping(source = "extraAi", target = "extraInfo")
    GenerateJobRequestDTO toGenerateJobRequestDTO(GenerateJobDescDTO generateJobDesc);

    /**
     * IntelligenceScoreRuleVO -> IntelligenceScoreRuleDTO
     */
    IntelligenceScoreRuleDTO toIntelligenceScoreRuleDTO(IntelligenceScoreRuleRequestDTO vo);

    /**
     * IntelligenceScoreRuleDTO -> IntelligenceScoreRuleVO
     */
    IntelligenceScoreRuleVO toIntelligenceScoreRuleVO(IntelligenceScoreRuleDTO dto);

    /**
     * List<IntelligenceScoreRuleVO> -> List<IntelligenceScoreRuleDTO>
     */
    List<IntelligenceScoreRuleDTO> toIntelligenceScoreRuleDTOList(List<IntelligenceScoreRuleRequestDTO> voList);

    /**
     * List<IntelligenceScoreRuleDTO> -> List<IntelligenceScoreRuleVO>
     */
    List<IntelligenceScoreRuleVO> toIntelligenceScoreRuleVOList(List<IntelligenceScoreRuleDTO> dtoList);

    /**
     * JobEsEntity -> JobUpdateBO
     */
    @Mapping(source = "id", target = "jobId")
    @Mapping(source = "longitude", target = "longitude", qualifiedByName = "convertBigDecimalToDouble")
    @Mapping(source = "latitude", target = "latitude", qualifiedByName = "convertBigDecimalToDouble")
    @Mapping(source = "locations", target = "locations", qualifiedByName = "convertToGenLocationDTO")
    JobUpdateBO toJobUpdateBoFromEntity(JobEsEntity entity);

}