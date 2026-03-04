package com.item.convert;

import com.item.dto.job.JobAuditHistoryBO;
import com.item.entity.JobAuditHistoryEntity;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface JobAuditHistoryConvert {

    JobAuditHistoryConvert INSTANCE = Mappers.getMapper(JobAuditHistoryConvert.class);

    JobAuditHistoryEntity convertFromUpdateBO(JobAuditHistoryBO bo);

}
