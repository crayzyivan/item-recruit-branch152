package com.item.vo.ai;

import com.item.dto.job.GeoPointDTO;
import lombok.Data;

@Data
public class LocationAIVO {

    private Long countryId;
    private String countryName;

    private Long stateId;
    private String stateName;

    private Long cityId;
    private String cityName;

    private String locationName;

    private GeoPointDTO geoPoint;
}
