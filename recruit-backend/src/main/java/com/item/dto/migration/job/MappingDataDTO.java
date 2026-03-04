package com.item.dto.migration.job;

import lombok.Data;

import java.util.List;

/**
 * @author : lh
 */
@Data
public class MappingDataDTO {
    private List<DataMigrationMappingDTO> mappingData;
    private String security;
}
