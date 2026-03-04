package com.item.service;

import com.item.dto.job.LocationValDTO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class LocationServiceTests {

    @Autowired
    private LocationService locationService;

    @Test
    public void test() {
        String placeId = "ChIJmTuvubVT8DURy1HwiFWlrT4";
        LocationValDTO location = locationService.buildLocationStringByPlaceId(placeId);
        Assertions.assertNotNull(location);
    }
}
