package com.item.dto.job;

import co.elastic.clients.elasticsearch._types.GeoLocation;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.item.convert.GeoLocationDeserializer;
import com.item.convert.GeoLocationSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author : lh
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LocationValRecordDTO {

    private Long countryId;
    private  String countryName;
    private Long stateId;
    private String stateName;
    private Long cityId;

    private String cityName;
    private String locationName;

    @JsonSerialize(using = GeoLocationSerializer.class)
    @JsonDeserialize(using = GeoLocationDeserializer.class)
    private GeoLocation geoPoint;

    /**
     * 对应 Google Map Place Id
     */
    private String placeId;

    /**
     * 对应 Google Map Place Description
     */
    private String placeName;

}
