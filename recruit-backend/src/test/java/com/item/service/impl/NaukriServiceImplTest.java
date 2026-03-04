package com.item.service.impl;

import com.item.dto.job.JobCreateBO;
import com.item.dto.job.LocationValDTO;
import com.item.framework.net.HttpClient5Service;
import com.item.service.JobService;
import com.item.util.JsonUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class NaukriServiceImplTest {

    private HttpClient5Service httpClient5Service;
    private JobService jobService;
    private NaukriServiceImpl naukriService;

    @BeforeEach
    void setup() throws Exception {
        httpClient5Service = mock(HttpClient5Service.class);
        jobService = mock(JobService.class);
        naukriService = new NaukriServiceImpl(httpClient5Service, jobService);
        // set private fields via reflection
        setField(naukriService, "naukriEnabled", true);
        setField(naukriService, "apiUrl", "https://api.zwayam.com/amplify/v2");
        setField(naukriService, "apiKey", "TEST_KEY");
    }

    private static void setField(Object target, String field, Object value) throws Exception {
        var f = target.getClass().getDeclaredField(field);
        f.setAccessible(true);
        f.set(target, value);
    }

    @Test
    void shouldPostToNaukri_true_forIndia() {
        JobCreateBO bo = new JobCreateBO();
        LocationValDTO loc = new LocationValDTO();
        loc.setCountryName("India");
        bo.setLocations(List.of(loc));
        assertTrue(naukriService.shouldPostToNaukri(bo));
    }

    @Test
    void shouldPostToNaukri_false_forOtherCountry() {
        JobCreateBO bo = new JobCreateBO();
        LocationValDTO loc = new LocationValDTO();
        loc.setCountryName("Philippines");
        bo.setLocations(List.of(loc));
        assertFalse(naukriService.shouldPostToNaukri(bo));
    }

    @Test
    void asyncPostJob_parsesId_andUpdatesDb() {
        JobCreateBO bo = new JobCreateBO();
        bo.setTitle("QA Engineer");
        bo.setJobDetail("desc");
        bo.setMinSalary(5000);
        bo.setMaxSalary(10000);
        bo.setCurrencyName("INR");
        LocationValDTO loc = new LocationValDTO();
        loc.setCountryName("India");
        bo.setLocations(List.of(loc));

        when(httpClient5Service.doPost(anyString(), anyString(), anyMap())).thenReturn("{\"id\":\"abc-123\"}");

        naukriService.asyncPostJob(bo, "ACME", 99L);

        verify(jobService, times(1)).updateNaukriJobId(99L, "abc-123");
    }

    @Test
    void asyncUnpublishJob_sendsJobBoardsNaukri() {
        ArgumentCaptor<String> urlCap = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> bodyCap = ArgumentCaptor.forClass(String.class);

        naukriService.asyncUnpublishJob("job-xyz");

        verify(httpClient5Service, times(1)).doPostAsync(urlCap.capture(), bodyCap.capture(), anyMap());
        assertTrue(urlCap.getValue().endsWith("/jobs/job-xyz/unpublish"));
        Map<?,?> body = JsonUtils.toObject(bodyCap.getValue(), Map.class);
        assertTrue(((List<?>) body.get("jobBoards")).contains("naukri"));
    }

    @Test
    void asyncRefreshJob_sendsJobBoardsNaukri() {
        ArgumentCaptor<String> urlCap = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> bodyCap = ArgumentCaptor.forClass(String.class);

        naukriService.asyncRefreshJob("job-xyz");

        verify(httpClient5Service, times(1)).doPostAsync(urlCap.capture(), bodyCap.capture(), anyMap());
        assertTrue(urlCap.getValue().endsWith("/jobs/job-xyz/refresh"));
        Map<?,?> body = JsonUtils.toObject(bodyCap.getValue(), Map.class);
        assertTrue(((List<?>) body.get("jobBoards")).contains("naukri"));
    }
}
