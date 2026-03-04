package com.item.convert.migration.job;

import com.item.dto.migration.job.DataMigrationMappingDTO;
import com.item.dto.migration.job.JobDetailsDTO;
import com.item.entity.JobEntity;
import com.item.entity.migration.DataMigrationMappingEntity;
import com.item.entity.migration.job.PgJobEntity;
import com.item.es.entity.JobEsEntity;
import com.item.framework.constant.JobStatus;
import static com.item.service.migration.category.Constant.CATEGORY_IP_MAP;
import static com.item.service.migration.category.Constant.CATEGORY_TYPE_MAP;
import static com.item.service.migration.category.Constant.CURRENCY_MAP;
import static com.item.service.migration.category.Constant.JOB_CURRENCY;
import static com.item.service.migration.category.Constant.JOB_MODE;
import static com.item.service.migration.category.Constant.JOB_STATUS_MAP;
import static com.item.service.migration.category.Constant.JOB_TYPE;
import static com.item.service.migration.category.Constant.JOB_SALARY_TYPE;
import static com.item.service.migration.category.Constant.JOB_TYPE_MAP;
import static com.item.service.migration.category.Constant.LOCATION_TYPE_MAP;
import static com.item.service.migration.category.Constant.SALARY_TYPE_MAP;
import static com.item.service.migration.category.Constant.TYPE_REFERENCE_LIST;
import static com.item.service.migration.category.Constant.TYPE_REFERENCE_LIST_STR;
import com.item.util.JsonUtils;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Job 迁移数据转换器
 * 
 * 使用 MapStruct 处理 PostgreSQL Job 到 MySQL JobEntity 和 JobEsEntity 的字段映射和数据转换。
 * 包括 JSONB 字段解析、枚举值转换、ID 映射等复杂转换逻辑。
 *
 * @author system
 * @since 2025-09-30
 */
@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface JobMigrationConvert {

    /**
     * PostgreSQL Job 转换为 MySQL JobEntity
     * 
     * @param pgJob PostgreSQL Job 实体
     * @return MySQL Job 实体
     */
    @Mapping(source = "id", target = "id", ignore = true)
    @Mapping(source = "title", target = "title")
    @Mapping(source = "companyId", target = "customerId", qualifiedByName = "convertIntegerToLong")
    @Mapping(target = "masterAccountId",constant = "0L")
    @Mapping(target = "locationId", constant = "0")
    @Mapping(source = "status", target = "jobStatus", qualifiedByName = "convertJobStatus")
    @Mapping(target = "hotList", constant = "0")
    @Mapping(source = "slug", target = "urlCode", ignore = true)
    @Mapping(source = "type", target = "typeId", qualifiedByName = "convertJobType")
    @Mapping(source = "categoryIds", target = "categoryId", qualifiedByName = "convertCategoryIds")
    @Mapping(source = "locationType", target = "modeId", qualifiedByName = "convertLocationType")
    @Mapping(source = "createdBy", target = "createBy", ignore = true)
    @Mapping(source = "updatedBy", target = "updateBy", ignore = true)
    @Mapping(source = "createdOn", target = "createTime", qualifiedByName = "convertOffsetDateTimeToLocalDateTime")
    @Mapping(source = "updatedOn", target = "updateTime", qualifiedByName = "convertOffsetDateTimeToLocalDateTime")
    @Mapping(source = "salaryType", target = "salaryType", qualifiedByName = "convertSalaryType")
    @Mapping(source = "currencyId", target = "currency", qualifiedByName = "convertShortToInteger")
    @Mapping(source = "minSalary", target = "minSalary", qualifiedByName = "convertBigDecimalToInteger")
    @Mapping(source = "maxSalary", target = "maxSalary", qualifiedByName = "convertBigDecimalToInteger")
    @Mapping(source = "numberOfOpenings", target = "numberOpenings")
    @Mapping(target = "needListed", constant = "1")
    @Mapping(target = "companyCode", ignore = true) // TODO: next confirm - 需要通过 company_id 查询获取
    @Mapping(target = "companyTitleHash", ignore = true)
    @Mapping(target = "createUser", ignore = true)
    @Mapping(target = "ayrshareStatus", constant = "0") // 默认不分享
    @Mapping(target = "interviewLength", constant = "15") // 默认不分享
    @Mapping(target = "enableWrittenTest", constant = "false") // 默认不分享
    JobEntity convertToJobEntity(PgJobEntity pgJob);

    /**
     * PostgreSQL Job 转换为 ES JobEsEntity
     * 需要结合 JobDetailsDTO 来填充 ES 特有的字段
     *
     * @param jobEntity Job 实体
     * @param jobDetails 解析后的 Job 详情
     * @return ES Job 实体
     */
    @Mapping(source = "jobEntity.id", target = "id")
    @Mapping(source = "jobEntity.title", target = "title")
    @Mapping(source = "jobEntity.customerId", target = "customerId")
    @Mapping(source = "jobEntity.masterAccountId", target = "masterAccountId")
    @Mapping(source = "jobEntity.locationId", target = "locationId")
    @Mapping(source = "jobEntity.jobStatus", target = "jobStatus")
    @Mapping(source = "jobEntity.hotList", target = "hotList")
    @Mapping(source = "jobEntity.urlCode", target = "urlCode")
    @Mapping(source = "jobEntity.typeId", target = "typeId")
    @Mapping(source = "jobEntity.typeId", target = "typeName", qualifiedByName = "convertJobTypeName")
    @Mapping(source = "jobEntity.categoryId", target = "categoryId")
    @Mapping(source = "jobEntity.categoryId", target = "categoryName", qualifiedByName = "convertCategoryName")
    @Mapping(source = "jobEntity.modeId", target = "modeId", qualifiedByName = "convertLocationType")
    @Mapping(source = "jobEntity.modeId", target = "modeName", qualifiedByName = "convertModeName")
    @Mapping(source = "jobEntity.createBy", target = "createBy")
    @Mapping(source = "jobEntity.updateBy", target = "updateBy")
    @Mapping(source = "jobEntity.createTime", target = "createTime")
    @Mapping(source = "jobEntity.updateTime", target = "updateTime")
    @Mapping(source = "jobEntity.salaryType", target = "salaryType")
    @Mapping(source = "jobEntity.salaryType", target = "salaryTypeName", qualifiedByName = "convertSalaryTypeName")
    @Mapping(source = "jobEntity.currency", target = "currency")
    @Mapping(source = "jobEntity.currency", target = "currencyName", qualifiedByName = "convertCurrencyName")
    @Mapping(source = "jobEntity.minSalary", target = "minSalary")
    @Mapping(source = "jobEntity.maxSalary", target = "maxSalary")
    @Mapping(source = "jobEntity.numberOpenings", target = "numberOpenings")
    @Mapping(source = "jobDetails.overview", target = "jobDetail")
    @Mapping(source = "jobDetails.skills", target = "skills")
    @Mapping(source = "jobDetails.benefits", target = "benefits")
    @Mapping(source = "jobDetails.responsibilities", target = "mainDuty")
    @Mapping(source = "jobDetails.requirements.minimum", target = "minimumJobRequirement")
    @Mapping(source = "jobDetails.requirements.preferred", target = "preferredJobRequirement")
    @Mapping(source = "pgJob.customQuestions", target = "customQuestions", qualifiedByName = "convertCustomQuestions")
    @Mapping(source = "jobEntity.jobStatus", target = "jobStatusName", qualifiedByName = "convertJobStatusName")
    @Mapping(source = "jobEntity.deleted", target = "deleted", qualifiedByName = "convertBooleanToInteger")
    JobEsEntity convertToJobEsEntity(JobEntity jobEntity, PgJobEntity pgJob, JobDetailsDTO jobDetails);

    @Mapping(source = "pgsqlId", target = "createTime", qualifiedByName = "currentTime")
    DataMigrationMappingEntity convert2DataMigrationMappingEntity(DataMigrationMappingDTO dataMigrationMappingDTO);
    List<DataMigrationMappingEntity> convert2DataMigrationMappingEntitys(List<DataMigrationMappingDTO> dataMigrationMappingDTOs);
    /**
     * 解析 JSONB details 字段
     * 
     * @param detailsJson JSONB 字符串
     * @return 解析后的 JobDetailsDTO
     */
    @Named("parseJobDetails")
    static JobDetailsDTO parseJobDetails(String detailsJson) {
        if (StringUtils.isBlank(detailsJson)) {
            return new JobDetailsDTO();
        }
        try {
            return JsonUtils.toObject(detailsJson, JobDetailsDTO.class);
        } catch (Exception e) {
            // TODO: next confirm - 需要确认解析失败时的处理策略
            return new JobDetailsDTO();
        }
    }

    @Named("currentTime")
    static LocalDateTime currentTime(String s) {
        return LocalDateTime.now();
    }


    // ========== 类型转换方法 ==========

    @Named("convertIntegerToLong")
    static Long convertIntegerToLong(Integer value) {
        return value != null ? value.longValue() : null;
    }

    @Named("convertBooleanToInteger")
    static Integer convertBooleanToInteger(Boolean value) {
        return (value != null && value) ? 1 : 0;
    }

    @Named("convertUuidToLong")
    static Long convertUuidToLong(UUID uuid) {
        // TODO: next confirm - 需要确认 UUID 到 Long 的转换策略，可能需要通过映射表查询
        if (uuid == null) {
            return null;
        }
        return (long) uuid.hashCode();
    }

    @Named("convertOffsetDateTimeToLocalDateTime")
    static LocalDateTime convertOffsetDateTimeToLocalDateTime(OffsetDateTime offsetDateTime) {
        return offsetDateTime != null ? offsetDateTime.toLocalDateTime() : null;
    }

    @Named("convertShortToInteger")
    static Integer convertShortToInteger(Short value) {
        return value != null ? JOB_CURRENCY.getOrDefault(value.intValue(), null) : null;
    }

    @Named("convertCurrencyName")
    static String convertCurrencyName(Integer value) {
        return value != null ? CURRENCY_MAP.getOrDefault(value, null) : null;
    }

    @Named("convertBigDecimalToInteger")
    static Integer convertBigDecimalToInteger(java.math.BigDecimal value) {
        return value != null ? value.intValue() : null;
    }

    @Named("convertCategoryIds")
    static Integer convertCategoryIds(String categoryIdsStr) {
        // 需要确认多个分类ID的处理策略，当前取第一个
        if (StringUtils.isBlank(categoryIdsStr)) {
            return null;
        }
        categoryIdsStr = StringUtils.replaceChars(categoryIdsStr, "{", "[");
        categoryIdsStr = StringUtils.replaceChars(categoryIdsStr, "}", "]");
        List<Integer> categoryIds = JsonUtils.toObject(categoryIdsStr, TYPE_REFERENCE_LIST);
        if (categoryIds == null || categoryIds.isEmpty()) {
            //pg没有 设置为60 其他
            return 60;
        }
        return CATEGORY_IP_MAP.get(categoryIds.getFirst());
    }

    @Named("convertCategoryName")
    static String convertCategoryName(Integer categoryId) {
        // 需要确认多个分类ID的处理策略，当前取第一个
        if (categoryId == null) {
            return null;
        }

        return CATEGORY_TYPE_MAP.get(categoryId);
    }

    @Named("convertCustomQuestions")
    static List<String> convertCustomQuestions(String customQuestionsStr) {
        //
        List<String> customQuestions = JsonUtils.toObject(customQuestionsStr, TYPE_REFERENCE_LIST_STR);
        return customQuestions;
    }

    // ========== 枚举值转换方法 ==========

    @Named("convertJobStatus")
    static Integer convertJobStatus(String status) {
        // 需要确认 PostgreSQL 枚举值到 MySQL 整型的映射关系
        if (StringUtils.isBlank(status)) {
            return JobStatus.CLOSED.getCode();
        }
        return JOB_STATUS_MAP.getOrDefault(status, JobStatus.CLOSED.getCode());
    }

    @Named("convertJobStatusName")
    static String convertJobStatusName(Integer code) {
        if (code == null) {
            return "";
        }
        return JobStatus.getByCode(code).getDescription();
    }

    @Named("convertJobType")
    static Integer convertJobType(String type) {
        // 需要确认 PostgreSQL job type 枚举值到 MySQL type_id 的映射关系
        if (StringUtils.isBlank(type)) {
            return 6;
        }
        return JOB_TYPE.getOrDefault(type, 6);
    }

    @Named("convertJobTypeName")
    static String convertJobTypeName(Integer type) {
        // 需要确认 PostgreSQL job type 枚举值到 MySQL type_id 的映射关系
        if (type == null) {
            return "Other";
        }
        return JOB_TYPE_MAP.getOrDefault(type, "Other");
    }

    @Named("convertLocationType")
    static Integer convertLocationType(String locationType) {
        // PostgreSQL location type 枚举值到 MySQL mode_id 的映射关系
        if (StringUtils.isBlank(locationType)) {
            return 4;
        }
        return JOB_MODE.getOrDefault(locationType, 4);
    }
    @Named("convertModeName")
    static String convertModeName(Integer modeId) {
        // PostgreSQL location type 枚举值到 MySQL mode_id 的映射关系
        if (modeId == null) {
            return "Other";
        }
        return LOCATION_TYPE_MAP.getOrDefault(modeId, "Other");
    }

    @Named("convertSalaryType")
    static Integer convertSalaryType(String salaryType) {
        // PostgreSQL salary type 枚举值到 MySQL salary_type 的映射关系
        if (StringUtils.isBlank(salaryType)) {
            return null;
        }
        return JOB_SALARY_TYPE.getOrDefault(salaryType, null);
    }

    @Named("convertSalaryTypeName")
    static String convertSalaryTypeName(Integer salaryType) {
        // PostgreSQL salary type 枚举值到 MySQL salary_type 的映射关系
        if (salaryType == null) {
            return null;
        }
        return SALARY_TYPE_MAP.getOrDefault(salaryType, null);
    }
}
