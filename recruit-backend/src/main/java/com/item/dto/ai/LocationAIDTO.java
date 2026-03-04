package com.item.dto.ai;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.item.dto.job.GeoPointDTO;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LocationAIDTO {

    private Long countryId;
    private String countryName;

    private Long stateId;
    private String stateName;

    private Long cityId;
    private String cityName;

    private String locationName;

    private GeoPointDTO geoPoint;
}
