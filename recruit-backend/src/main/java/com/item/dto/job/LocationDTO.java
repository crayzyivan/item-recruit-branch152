package com.item.dto.job;

import lombok.Data;

/**
 * @author : lh
 */
@Data
public class LocationDTO {

    private Long countryId;
    private String countryName;

    private Long stateId;
    private String stateName;

    private Long cityId;
    private String cityName;

    private String locationName;

    private GeoPointDTO geoPoint;

    /**
     * Google Map Place Id
     */
    private String placeId;
    /**
     * Google Map Place Name
     */
    private String placeName;
}
