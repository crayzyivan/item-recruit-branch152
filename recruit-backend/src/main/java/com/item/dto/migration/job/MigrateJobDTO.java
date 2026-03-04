package com.item.dto.migration.job;

import com.item.entity.migration.job.PgJobEntity;
import com.item.entity.migration.location.PgLocationEntity;
import com.item.service.migration.category.Constant;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * @author : lh
 */
@Data
public class MigrateJobDTO {
    private PgJobEntity pgJob;
    private Map<Integer, Integer> categoryMap = Constant.CATEGORY_IP_MAP;
    private Map<String, Long> countryMap = new HashMap<String, Long>();
    private Map<String, String> countryNameMap = new HashMap<String, String>();
    private Map<String, Long> cityMap = new HashMap<String, Long>();
    private Map<String, Long> stateMap = new HashMap<String, Long>();
    private Map<Integer, PgLocationEntity> pgLocationMap= new HashMap<>();
    private Map<Integer, CompanyInfoDTO>  companyInfoMap= new HashMap<>();
    private Map<String, Long>  userIdMap= new HashMap<>();
    private Map<String, String>  userNameEmail= new HashMap<>();
}
