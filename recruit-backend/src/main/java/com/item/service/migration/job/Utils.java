package com.item.service.migration.job;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * @author : lh
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Utils {
    public static String getCountryKey(String countryName) {
        return countryName;
    }

    public static String getStateKey(String stateName, Long countryId) {
        return stateName + "-" + countryId;
    }

    public static String getCityKey(String cityName, Long stateId, Long countryId) {
        return cityName + "-" + stateId + "-" + countryId;
    }
}
