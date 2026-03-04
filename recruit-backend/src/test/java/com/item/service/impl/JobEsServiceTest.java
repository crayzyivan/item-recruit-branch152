package com.item.service.impl;

import com.item.convert.LocationConverter;
import com.item.dto.JobDto;
import com.item.es.entity.JobEsEntity;
import com.item.framework.http.Pager;
import com.item.service.LocationService;
import com.item.util.CommonUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @author : lh
 */
@Slf4j
@SpringBootTest
public class JobEsServiceTest {
    @Resource
    private com.item.es.JobEsService jobEsService;
    @Resource
    private LocationService locationService;
    @Resource
    private LocationConverter locationConverter;

    @Test
    public void searchJobs() {
        String val1= "*";
        String escape1 = CommonUtils.escape(val1);
        log.info("val1 {} {}",val1, escape1);

        String val2= "*this a*";
        String escape2 = CommonUtils.escape(val2);
        log.info("val2 {} {}",val2, escape2);

        String val3= """
                *this \\* a*
                """;
        String escape3 = CommonUtils.escape(val3);
        log.info("val {} {}",val3, escape3);

        String val4= """
                *this \\* a* apple ?*
                """;
        String escape4 = CommonUtils.escape(val4);
        log.info("val {} {}",val4, escape4);

        String val5= """
                this a apple
                """;
        String escape5 = CommonUtils.escape(val5);
        log.info("val {} {}",val5, escape5);


        String val6= """
                .
                """;
        String escape6 = CommonUtils.escape(val6);
        log.info("val {} {}",val6, escape6);

        JobDto jobDto = new JobDto();
        Pager<JobEsEntity> jobEsEntityPager = jobEsService.searchJobs(val1, 1, 200, jobDto, -1);
        log.info("jobEsEntityPager {}",jobEsEntityPager.getTotalCount());

        jobEsEntityPager = jobEsService.searchJobs(escape1, 1, 200, jobDto, -1);
        log.info("jobEsEntityPager {}",jobEsEntityPager.getTotalCount());

        jobEsEntityPager = jobEsService.searchJobs(val2, 1, 200, jobDto, -1);
        log.info("jobEsEntityPager {}",jobEsEntityPager.getTotalCount());

        jobEsEntityPager = jobEsService.searchJobs(escape2, 1, 200, jobDto, -1);
        log.info("jobEsEntityPager {}",jobEsEntityPager.getTotalCount());
        jobEsEntityPager = jobEsService.searchJobs(escape6, 1, 200, jobDto, -1);
        log.info("jobEsEntityPager {}",jobEsEntityPager.getTotalCount());
    }

//    @Test
//    public void updateJobEsOld2New(){
//        JobDto jobDto = new JobDto();
//        Pager<JobEsEntity> jobEsEntityPager = jobEsService.searchJobs(null, 1, 200, jobDto, -1);
//        JobEsEntity jobEsEntity = jobEsEntityPager.getCurrentPageRecords().stream().filter(j -> j.getId().equals(112L)).findFirst().orElse(null);
//        if (jobEsEntity != null && CollectionUtils.isEmpty(jobEsEntity.getLocations())) {
//            Integer locationId = jobEsEntity.getLocationId();
//            CityVO cityDTO = getCityDTO(locationId.longValue());
//            StateDTO stateDTO = getStateDTO(locationId.longValue());
//            CountryDTO countryDTO = getCountryDTO(locationId.longValue());
//            JobEsEntity jobEsEntity1 = new JobEsEntity();
//            jobEsEntity1.setId(jobEsEntity.getId());
//            LocationValRecordDTO locationValRecordDTO = new LocationValRecordDTO();
//            locationValRecordDTO.setCityId(cityDTO.getId());
//            locationValRecordDTO.setStateId(stateDTO.getId());
//            locationValRecordDTO.setCountryId(countryDTO.getId());
//            locationValRecordDTO.setCityName(cityDTO.getName());
//            locationValRecordDTO.setStateName(stateDTO.getName());
//            locationValRecordDTO.setCountryName(countryDTO.getName());
//            locationValRecordDTO.setLocationName(CommonUtils.joinComma(countryDTO.getName(), stateDTO.getName(), cityDTO.getName()));
//            jobEsEntity1.setLocations(Collections.singletonList(locationValRecordDTO));
//            jobEsService.updateJobEs(jobEsEntity1);
//        }
//    }
//
//    private CityVO getCityDTO (Long cId) {
//        Object dto = locationService.getLocationDetail(null, null, cId);
//        if (dto instanceof CityDTO cityDTO) {
//            return locationConverter.toCityVO(cityDTO);
//        }
//        return null;
//    }
//    private StateDTO getStateDTO (Long cId) {
//        return locationService.getStateByCityId(cId);
//    }
//    private CountryDTO getCountryDTO (Long cId) {
//       return locationService.getCountryByCityId(cId);
//    }
}
