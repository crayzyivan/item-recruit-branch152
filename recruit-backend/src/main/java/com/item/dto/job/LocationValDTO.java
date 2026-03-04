package com.item.dto.job;

import com.item.framework.annotation.Xss;
import jakarta.validation.constraints.Min;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

/**
 * @author : lh
 */
@Data
public class LocationValDTO {

//    @NotNull(message = "countryId must not be null")
    @Min(value = 1, message = "countryId must be greater than or equal to {value}")
    private Long countryId;
//    @NotBlank(message = "Country name must not be blank")
    @Xss(message = "Country name some special characters, such as \"<\", \">\", \"<a>\", etc.")
    private String countryName;

//    @NotNull(message = "stateId must not be null")
//    @Min(value = 1, message = "stateId must be greater than or equal to {value}")
    private Long stateId;
//    @NotBlank(message = "stateName must not be blank")
    @Xss(message = "State name some special characters, such as \"<\", \">\", \"<a>\", etc.")
    private String stateName;

//    @NotNull(message = "cityId must not be null")
//    @Min(value = 1, message = "stateId must be greater than or equal to {value}")
    private Long cityId;
//    @NotBlank(message = "cityName must not be blank")
    @Xss(message = "City name some special characters, such as \"<\", \">\", \"<a>\", etc.")
    private String cityName;

    @Xss(message = "Location name some special characters, such as \"<\", \">\", \"<a>\", etc.")
    private String locationName;

    private GeoPointDTO geoPoint;

    /**
     * Google Map Place Id
     */
    private String placeId;

    /**
     * Google Map Place Name
     */
    @Xss(message = "Place name some special characters, such as \"<\", \">\", \"<a>\", etc.")
    private String placeName;

    public void replaceLocationName() {
        if (StringUtils.isNotEmpty(this.placeName)) {
            this.locationName = this.placeName;
        }
    }
}
