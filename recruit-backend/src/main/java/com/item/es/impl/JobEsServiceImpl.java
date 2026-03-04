package com.item.es.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.Refresh;
import co.elastic.clients.elasticsearch._types.Result;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregate;
import co.elastic.clients.elasticsearch._types.query_dsl.FunctionBoostMode;
import co.elastic.clients.elasticsearch._types.query_dsl.FunctionScoreMode;
import co.elastic.clients.elasticsearch._types.query_dsl.MatchQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.RangeQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.TermsQuery;
import co.elastic.clients.elasticsearch.core.DeleteRequest;
import co.elastic.clients.elasticsearch.core.DeleteResponse;
import co.elastic.clients.elasticsearch.core.GetResponse;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import co.elastic.clients.elasticsearch.core.MgetRequest;
import co.elastic.clients.elasticsearch.core.MgetResponse;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.UpdateRequest;
import co.elastic.clients.elasticsearch.core.UpdateResponse;
import co.elastic.clients.elasticsearch.core.mget.MultiGetResponseItem;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.core.search.SourceConfig;
import co.elastic.clients.elasticsearch.core.search.TotalHits;
import com.google.common.base.Strings;
import com.item.dto.JobDto;
import com.item.dto.job.LocationValDTO;
import com.item.es.JobEsService;
import com.item.es.entity.JobEsEntity;
import com.item.framework.config.RecruitCommonNacosConfig;
import com.item.framework.constant.CommonConstants;
import static com.item.framework.constant.CommonConstants.StrConstants.ES_FIELD_KEY_WORD;
import com.item.framework.constant.JobResponseCode;
import com.item.framework.constant.JobStatus;
import com.item.framework.constant.TimeEnum;
import com.item.framework.error.BusinessException;
import com.item.framework.http.Pager;
import com.item.util.CommonUtils;
import com.item.util.JsonUtils;
import com.item.util.LambdaUtil;
import com.item.vo.JobOptionVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

/**
 * @author hua.liu
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class JobEsServiceImpl implements JobEsService {
    private static final String INDEX = "recruit_job_ext";

    private final ElasticsearchClient esClient;
    private final RecruitCommonNacosConfig recruitCommonNacosConfig;

    @Override
    public void saveJobToEs(JobEsEntity jobEsEntity) {
        try {
            IndexRequest<JobEsEntity> request = IndexRequest.of(i -> i
                .index(INDEX)
                .id(String.valueOf(jobEsEntity.getId()))
                .document(jobEsEntity)
                .refresh(Refresh.WaitFor)
            );
            IndexResponse response = esClient.index(request);
            log.info("save job to es response {}", response);
        } catch (IOException e) {
            log.error("saveJobToEs error jobes {} ", jobEsEntity, e);
            throw BusinessException.of(JobResponseCode.JOB_PUBLISH_FAIL);
        }
    }

    @Override
    public Pager<JobEsEntity> searchJobs(String keyword,
                                         int pageNum,
                                         int pageSize,
                                         JobDto jobDto,
                                         int datePosted) {
        try {
            List<Query> queryList = new ArrayList<>();
            // 构建多字段模糊查询
            // String[] fieldNames = LambdaUtil.getFieldNames(JobEsEntity::getTitle/*, JobEsEntity::getJobDetail, JobEsEntity::getMinimumJobRequirement, JobEsEntity::getPreferredJobRequirement, JobEsEntity::getSkills*/);
            if (!Strings.isNullOrEmpty(keyword)) {
                String allLikeEscapeKeyWord = CommonUtils.getAllLikeEscapeKeyWord(keyword);
                Query keywordQueryPhrase = Query.of(q -> q
                                .wildcard(m -> m
                                        .value(allLikeEscapeKeyWord)
                                        .field(LambdaUtil.getFieldName(JobEsEntity::getTitle) + ES_FIELD_KEY_WORD)
                                        .caseInsensitive(true)
                                )
                );
                queryList.add(keywordQueryPhrase);
            }
            if(pageNum < 1) {
                pageNum = 1;
            }
            if(pageSize < 10) {
                pageSize = 10;
            }

            String creatTimeFieldName = LambdaUtil.getFieldName(JobEsEntity::getCreateTime);

            TimeEnum timeEnum = TimeEnum.getByCode(datePosted);
            LocalDateTime now = LocalDateTime.now();
            if (timeEnum != null) {
                switch (timeEnum) {
                    case LASTHOUR -> queryList.add(RangeQuery.of(r -> r.field(creatTimeFieldName)
                                    .from(now.minusHours(1).format(CommonConstants.LocalDateTimeConstant.LOCAL_DATE_TIME_FORMATTER))
                                    .to(now.format(CommonConstants.LocalDateTimeConstant.LOCAL_DATE_TIME_FORMATTER)))
                                    ._toQuery());
                    case LASTDAY -> queryList.add(RangeQuery.of(r -> r.field(creatTimeFieldName)
                                    .from(now.minusDays(1).format(CommonConstants.LocalDateTimeConstant.LOCAL_DATE_TIME_FORMATTER))
                                    .to(now.format(CommonConstants.LocalDateTimeConstant.LOCAL_DATE_TIME_FORMATTER)))
                                    ._toQuery());
                    case LASTWEEK -> queryList.add(RangeQuery.of(r -> r.field(creatTimeFieldName)
                                    .from(now.minusDays(7).format(CommonConstants.LocalDateTimeConstant.LOCAL_DATE_TIME_FORMATTER))
                                    .to(now.format(CommonConstants.LocalDateTimeConstant.LOCAL_DATE_TIME_FORMATTER)))
                                    ._toQuery());
                    case LAST14DAYS -> queryList.add(RangeQuery.of(r -> r.field(creatTimeFieldName)
                                    .from(now.minusDays(14).format(CommonConstants.LocalDateTimeConstant.LOCAL_DATE_TIME_FORMATTER))
                                    .to(now.format(CommonConstants.LocalDateTimeConstant.LOCAL_DATE_TIME_FORMATTER)))
                                    ._toQuery());
                    case LAST30DAYS -> queryList.add(RangeQuery.of(r -> r.field(creatTimeFieldName)
                                    .from(now.minusDays(30).format(CommonConstants.LocalDateTimeConstant.LOCAL_DATE_TIME_FORMATTER))
                                    .to(now.format(CommonConstants.LocalDateTimeConstant.LOCAL_DATE_TIME_FORMATTER)))
                                    ._toQuery());
                }
            }

            if (jobDto.getLocationId() != null && jobDto.getLocationId() > 0) {
                String locationId = LambdaUtil.getFieldName(JobEsEntity::getLocationId);
                queryList.add(MatchQuery.of(m -> m.field(/*"locationId"*/locationId).query(jobDto.getLocationId()))._toQuery());
            }
            if (CollectionUtils.isNotEmpty(jobDto.getQJobTypeIds())) {
                String typeId = LambdaUtil.getFieldName(JobEsEntity::getTypeId);
                queryList.add(TermsQuery.of(t -> t
                        .field(typeId)
                        .terms(v -> v.value(jobDto.getQJobTypeIds().stream().map(FieldValue::of).toList())))._toQuery());
            }
//            if (jobDto.getTypeId() != null && jobDto.getTypeId() > 0) {
//                String typeId = LambdaUtil.getFieldName(JobEsEntity::getTypeId);
//                queryList.add(MatchQuery.of(m -> m.field(typeId/*"typeId"*/).query(jobDto.getTypeId()))._toQuery());
//            }
            if (jobDto.getCategoryId() != null && jobDto.getCategoryId() > 0) {
                String categoryId = LambdaUtil.getFieldName(JobEsEntity::getCategoryId);
                queryList.add(TermsQuery.of(t -> t
                        .field(categoryId)
                        .terms(v -> v.value(Stream.of(jobDto.getJobCategoryId()).map(FieldValue::of).toList())))._toQuery());
//                queryList.add(MatchQuery.of(m -> m.field(categoryId/*"categoryId"*/).query(jobDto.getCategoryId()))._toQuery());
            }
            if(!Strings.isNullOrEmpty(jobDto.getCompanyCode())) {
                String companyCode = LambdaUtil.getFieldName(JobEsEntity::getCompanyCode);
//                queryList.add(MatchQuery.of(m -> m.field(companyCode/*"companyCode"*/).query(jobDto.getCompanyCode()))._toQuery());
                queryList.add(TermsQuery.of(t -> t
                        .field(companyCode + ES_FIELD_KEY_WORD)
                        .terms(v -> v.value(Stream.of(jobDto.getCompanyCode()).map(FieldValue::of).toList())))._toQuery());
            }
            if (CollectionUtils.isNotEmpty(jobDto.getQJobStatus())) {
                String jobStatus = LambdaUtil.getFieldName(JobEsEntity::getJobStatus);
                queryList.add(TermsQuery.of(t -> t
                        .field(jobStatus)
                        .terms(v -> v.value(jobDto.getQJobStatus().stream().map(FieldValue::of).toList())))._toQuery());
            }
            if (CollectionUtils.isNotEmpty(jobDto.getQSalaryTypes())) {
                String salaryType = LambdaUtil.getFieldName(JobEsEntity::getSalaryType);
                queryList.add(TermsQuery.of(t -> t
                        .field(salaryType)
                        .terms(v -> v.value(jobDto.getQSalaryTypes().stream().map(FieldValue::of).toList())))._toQuery());
            }
            if (CollectionUtils.isNotEmpty(jobDto.getQTypeIds())) {
                String typeId = LambdaUtil.getFieldName(JobEsEntity::getTypeId);
                queryList.add(TermsQuery.of(t -> t
                        .field(typeId)
                        .terms(v -> v.value(jobDto.getQTypeIds().stream().map(FieldValue::of).toList())))._toQuery());
            }
            if (CollectionUtils.isNotEmpty(jobDto.getQCategoryIds())) {
                String categoryId = LambdaUtil.getFieldName(JobEsEntity::getCategoryId);
                queryList.add(TermsQuery.of(t -> t
                        .field(categoryId)
                        .terms(v -> v.value(jobDto.getQCategoryIds().stream().map(FieldValue::of).toList())))._toQuery());
            }
            if (CollectionUtils.isNotEmpty(jobDto.getQModeIds())) {
                String modeId = LambdaUtil.getFieldName(JobEsEntity::getModeId);
                queryList.add(TermsQuery.of(t -> t
                        .field(modeId)
                        .terms(v -> v.value(jobDto.getQModeIds().stream().map(FieldValue::of).toList())))._toQuery());
            }
//            if (jobDto.getJobStatus() != null && jobDto.getJobStatus() > -1) {
//                String jobStatus = LambdaUtil.getFieldName(JobEsEntity::getJobStatus);
//                queryList.add(MatchQuery.of(m -> m.field(jobStatus/*"companyCode"*/).query(jobDto.getJobStatus()))._toQuery());
//            }
            //增加删除逻辑过滤
            queryList.add(MatchQuery.of(m -> m.field(LambdaUtil.getFieldName(JobEsEntity::getDeleted)).query(0))._toQuery());

            // ========== 新增：地理位置查询 ==========
            if (jobDto.getUserLatitude() != null && jobDto.getUserLongitude() != null) {
                String radius = StringUtils.isNotBlank(jobDto.getSearchRadius())
                    ? jobDto.getSearchRadius()
                    : CommonConstants.DEFAULT_SEARCH_RADIUS;

                Query geoQuery = Query.of(q -> q
                    .nested(n -> n
                        .path(LambdaUtil.getFieldName(JobEsEntity::getLocations))
                        .query(nq -> nq
                            .geoDistance(gd -> gd
                                .field("locations.geoPoint")
                                .distance(radius)
                                .location(loc -> loc.latlon(ll -> ll
                                    .lat(jobDto.getUserLatitude())
                                    .lon(jobDto.getUserLongitude())
                                ))
                            )
                        )
                    )
                );
                queryList.add(geoQuery);

                log.debug("Added geo_distance query: lat={}, lon={}, radius={}",
                    jobDto.getUserLatitude(), jobDto.getUserLongitude(), radius);
            }
            // TODO:考虑异常情况
            //String id = LambdaUtil.getFieldName(JobEsEntity::getId);
            //queryList.add(RangeQuery.of(r -> r.field(id/*"id"*/).from("0"))._toQuery());

            String[] selectFields = LambdaUtil.getFieldNames(JobEsEntity::getHotList, JobEsEntity::getTitle, JobEsEntity::getCustomerName,
                    JobEsEntity::getJobStatus, JobEsEntity::getCreateTime, JobEsEntity::getCreateUser, JobEsEntity::getLocationName,
                    JobEsEntity::getModeName, JobEsEntity::getCategoryName, JobEsEntity::getTypeName, JobEsEntity::getId, JobEsEntity::getMinSalary,
                    JobEsEntity::getMaxSalary, JobEsEntity::getCurrencyName, JobEsEntity::getLocationId, JobEsEntity::getLocations,JobEsEntity::getSalaryType,
                    JobEsEntity::getCompanyCode, JobEsEntity::getModeId, JobEsEntity::getTypeId, JobEsEntity::getCategoryId, JobEsEntity::getCurrency,
                    JobEsEntity::getAyrshareStatus, JobEsEntity::getJobDetail,JobEsEntity::getInterviewType,JobEsEntity::getInterviewLength,JobEsEntity::getCreateBy,
                    JobEsEntity::getIntelligenceSwitch);
            SourceConfig sourceConfig = new SourceConfig.Builder()
//                    .filter(f -> f.includes(
//                            "hotList",
//                            "title",
//                            "customerName",
//                            "jobStatus",
//                            "createTime",
//                            "createUser",
//                            "locationName",
//                            "modeName",
//                            "categoryName",
//                            "typeName",
//                            "id"))
                    .filter(f -> f.includes(
                            Arrays.stream(selectFields).toList()))
                    .build();

//            String updateTime = LambdaUtil.getFieldName(JobEsEntity::getUpdateTime);
//            TrackHits.Builder trackHitsbuilder = new TrackHits.Builder();
            SearchRequest.Builder builder = new SearchRequest.Builder()
                    //已经沟通需要总记录数 注释掉
//                    .trackTotalHits(trackHitsbuilder.enabled(false).build())
                    .index(INDEX)
                    .query(q -> q.bool(b -> b.must(queryList))) //多条件组合查询
                    .source(sourceConfig)
                    .from((pageNum - 1) * pageSize)
                    //.sort(t -> t.field(f-> f.field(updateTime/*"updateTime"*/).order(SortOrder.Desc)))
                    .sort(t -> t.field(f -> f.field("id").order(SortOrder.Desc)))
                    .size(pageSize);

            // ========== 修改：支持地理位置排序 ==========
            // 注意：由于 ES Java Client 8.13.4 的 geoDistance 排序不支持嵌套字段
            // 我们在查询时已经通过 geo_distance 过滤，这里使用默认排序
            // 距离信息会在应用层计算并返回给前端
            if (jobDto.getUserLatitude() != null
                && jobDto.getUserLongitude() != null
                && Boolean.TRUE.equals(jobDto.getSortByDistance())) {
                // 地理位置查询已经过滤了范围内的职位
                // 按 ID 倒序返回，距离信息在 JobDomainServiceImpl 中计算
                // 前端可以根据返回的 distance 字段进行二次排序
                builder.sort(t -> t.field(f -> f.field("id").order(SortOrder.Desc)));
                log.debug("Geo distance sorting will be handled in application layer");
            } else {
                // 默认排序
                builder.sort(t -> t.field(f -> f.field("id").order(SortOrder.Desc)));
            }

            SearchResponse<JobEsEntity> response = esClient.search(builder.build(), JobEsEntity.class);

            List<JobEsEntity> results = new ArrayList<>();
            for (Hit<JobEsEntity> hit : response.hits().hits()) {
                results.add(hit.source());
            }

            Pager<JobEsEntity> pageResult = new Pager<>();
            pageResult.setCurrentPageRecords(results);
            pageResult.setPageIndex(pageNum);
            pageResult.setPageSize(pageSize);
            if (response.hits() == null || response.hits().hits() == null) {
                pageResult.setTotalCount(0);
            } else {
                pageResult.setTotalCount(Optional.ofNullable(response.hits().total()).map(TotalHits::value).orElse((long) response.hits().hits().size()));
            }


            return pageResult;
        } catch (Exception e) {
            log.error("Failed to search jobs in ES keyword {}", keyword, e);
            throw BusinessException.of(JobResponseCode.JOB_SEARCH_FAIL);
        }
    }

    @Override
    public JobEsEntity getJobById(Long jobId) {
        if (jobId == null) {
            return null;
        }
        try {
            GetResponse<JobEsEntity> response = esClient.get(g -> g
                    .index(INDEX)
                    .id(jobId.toString()), JobEsEntity.class);
            if (response != null && response.found()) {
                return response.source();
            }
            return null;
        } catch (IOException e) {
            log.error("Failed to get job from ES with id: {}", jobId, e);
            return null;
        }
    }

    @Override
    public JobEsEntity getJobById(Long jobId, String[] selectField) {
        if (jobId == null) {
            return null;
        }
        if (selectField != null && selectField.length > 0) {
            String idFieldName = LambdaUtil.getFieldName(JobEsEntity::getId);
            try {
                GetResponse<JobEsEntity> response = esClient.get(g -> g
                        .index(INDEX)
                        .id(jobId.toString()).sourceIncludes(idFieldName, selectField), JobEsEntity.class);
                if (response != null && response.found()) {
                    return response.source();
                }
                return null;
            } catch (IOException e) {
                log.error("Failed to get job from ES with id: {}", jobId, e);
                return null;
            }
        }
        try {
            GetResponse<JobEsEntity> response = esClient.get(g -> g
                    .index(INDEX)
                    .id(jobId.toString()), JobEsEntity.class);
            if (response != null && response.found()) {
                return response.source();
            }
            return null;
        } catch (IOException e) {
            log.error("Failed to get job from ES with id: {}", jobId, e);
            return null;
        }
    }

    @Override
    public List<JobEsEntity> getJobByIds(Collection<Long> jobIds) {
        if (CollectionUtils.isEmpty(jobIds)) {
            return List.of();
        }
        List<String> idList = jobIds.stream().distinct().map(Objects::toString).toList();
        MgetRequest mgetRequest = MgetRequest.of(m -> m
                .index(INDEX)
                .ids(idList)
        );
        List<JobEsEntity> resultList = new ArrayList<>();
        try {
            MgetResponse<JobEsEntity> response = esClient.mget(mgetRequest, JobEsEntity.class);
            // 处理结果
            for (MultiGetResponseItem<JobEsEntity> item : response.docs()) {
                if (item.isResult()) {
                    // 成功获取的文档
                    JobEsEntity entity = item.result().source();
                    if (entity != null) {
                        resultList.add(entity);
                    }
                } else if (item.isFailure()) {
                    // 获取失败的文档，记录日志但不影响其他文档
                    String failedId = item.failure().id();
                    String error = item.failure().error() != null ? item.failure().error().reason() : "Unknown error";
                    log.warn("Failed to get job from ES with id: {}, error: {}", failedId, error);
                }
            }
        } catch (IOException e) {
            log.error("Failed to get jobs from ES with ids: {}", idList, e);
            return List.of();
        }
        return resultList;
    }


    @Override
    public List<JobEsEntity> listJobIntelligenceScoreRuleByIds(Collection<Long> jobIds) {
        if (CollectionUtils.isEmpty(jobIds)) {
            return List.of();
        }
        String[] selectFields = LambdaUtil.getFieldNames(JobEsEntity::getId,
                JobEsEntity::getInterviewType,
                JobEsEntity::getIntelligenceSwitch,
                JobEsEntity::getScoreRules);
        List<String> idList = jobIds.stream().distinct().map(Objects::toString).toList();
        MgetRequest mgetRequest = MgetRequest.of(m -> m
                .index(INDEX)
                .source(s -> s.fields(Arrays.stream(selectFields).toList()))
                .ids(idList)
        );
        List<JobEsEntity> resultList = new ArrayList<>();
        try {
            MgetResponse<JobEsEntity> response = esClient.mget(mgetRequest, JobEsEntity.class);
            // 处理结果
            for (MultiGetResponseItem<JobEsEntity> item : response.docs()) {
                if (item.isResult()) {
                    // 成功获取的文档
                    JobEsEntity entity = item.result().source();
                    if (entity != null) {
                        resultList.add(entity);
                    }
                } else if (item.isFailure()) {
                    // 获取失败的文档，记录日志但不影响其他文档
                    String failedId = item.failure().id();
                    String error = item.failure().error() != null ? item.failure().error().reason() : "Unknown error";
                    log.warn("Failed to get job from ES with id: {}, error: {}", failedId, error);
                }
            }
        } catch (IOException e) {
            log.error("Failed to get jobs from ES with ids: {}", idList, e);
            return List.of();
        }
        return resultList;
    }

    @Override
    public void updateJobEs(JobEsEntity jobEsEntity) {
        try {
            Map<String, Object> updateMap = JsonUtils.toMap(jobEsEntity);
            updateMap.remove(LambdaUtil.getFieldName(JobEsEntity::getId));
            UpdateRequest<Object, Object> updateRequest = UpdateRequest.of(u -> u
                    .index(INDEX)
                    .id(jobEsEntity.getId().toString())
                    .doc(updateMap)
                    .docAsUpsert(false)
                    .refresh(Refresh.WaitFor));
            UpdateResponse<Object> updateResult = esClient.update(updateRequest, Objects.class);
            log.info("update job to es {} response {}", updateMap, updateResult);
            if (updateResult.result().equals(Result.Updated)) {
                log.info("Document updated successfully");
            }
        } catch (ElasticsearchException e) {
            log.warn("Document not found {}", jobEsEntity, e);
        } catch (IOException e) {
            log.error("update error job {} ", jobEsEntity, e);
            throw BusinessException.of(JobResponseCode.JOB_UPDATE_FAIL);
        }
    }

    @Override
    public boolean deleteJobById(Long jobId) {
        try {
            DeleteRequest request = DeleteRequest.of(d -> d
                .index(INDEX)
                .id(String.valueOf(jobId))
                .refresh(Refresh.WaitFor)
            );
            DeleteResponse response = esClient.delete(request);
            log.info("Delete job from ES response: {}", response.result());
            return response.result().jsonValue().equals("deleted") || response.result().jsonValue().equals("not_found");
        } catch (IOException e) {
            log.error("Failed to delete job from ES with id: {}", jobId, e);
            return false;
        }
    }

     /**
     * 根据状态查询职位
     * @param jobStatus 工作状态，不为空时才添加查询条件
     * @param companyCode 公司代码，不为空时才添加查询条件
     * @param size 查询数量
     * @return
     */
     public List<JobEsEntity> searchFeedXmlJobs(Integer jobStatus, String companyCode, int size,boolean distinct){
         // 构建查询请求
         String[] selectFields = LambdaUtil.getFieldNames(JobEsEntity::getId,JobEsEntity::getCompanyCode, JobEsEntity::getTitle,
                 JobEsEntity::getUrlCode, JobEsEntity::getJobDetail, JobEsEntity::getMainDuty, JobEsEntity::getMinimumJobRequirement,
                 JobEsEntity::getPreferredJobRequirement, JobEsEntity::getSkills, JobEsEntity::getLocations, JobEsEntity::getCreateTime,
                 JobEsEntity::getMinSalary, JobEsEntity::getMaxSalary, JobEsEntity::getCurrency, JobEsEntity::getCurrencyName, JobEsEntity::getSalaryType,
                 JobEsEntity::getSalaryTypeName, JobEsEntity::getTypeId, JobEsEntity::getTypeName, JobEsEntity::getModeId, JobEsEntity::getModeName,
                 JobEsEntity::getJobStatus);
         SourceConfig sourceConfig = new SourceConfig.Builder().filter(f -> f.includes(Arrays.stream(selectFields).toList())).build();

         SearchRequest.Builder searchBuilder = new SearchRequest.Builder()
                 .index(INDEX)
                 .query(q -> q
                         .bool(b -> {
                             // 必须条件：deleted = 0
                             b.must(m -> m.term(t -> t
                                     .field(LambdaUtil.getFieldName(JobEsEntity::getDeleted))
                                     .value(0)
                             ));

                             // 条件查询：jobStatus不为空时添加
                             if (jobStatus != null) {
                                 b.must(m -> m.term(t -> t
                                         .field(LambdaUtil.getFieldName(JobEsEntity::getJobStatus))
                                         .value(jobStatus)
                                 ));
                             }

                             // 条件查询：companyCode不为空时添加
                             if (!Strings.isNullOrEmpty(companyCode)) {
                                 b.must(m -> m.term(t -> t
                                         .field(LambdaUtil.getFieldName(JobEsEntity::getCompanyCode) + ES_FIELD_KEY_WORD)
                                         .value(companyCode)
                                 ));
                             }

                             return b;
                         })
                 )
                 .source(sourceConfig)
//                 .sort(so -> so
//                         .field(f -> f
//                                 .field(LambdaUtil.getFieldName(JobEsEntity::getCreateTime))
//                                 .order(SortOrder.Desc)
//                         )
//                 )
                 .size(size);

         // 根据 distinct 控制去重逻辑
         if (distinct) {
             // 去重：按 title.keyword 折叠，每组取 id 最大的
             searchBuilder
                     .collapse(c -> c
                             .field(LambdaUtil.getFieldName(JobEsEntity::getTitle) + ES_FIELD_KEY_WORD)
                     )
                     .sort(so -> so
                             .field(f -> f
                                     .field(LambdaUtil.getFieldName(JobEsEntity::getId))
                                     .order(SortOrder.Desc)
                             )
                     );
         } else {
             // 默认：按 createTime 倒序
             searchBuilder
                     .sort(so -> so
                             .field(f -> f
                                     .field(LambdaUtil.getFieldName(JobEsEntity::getCreateTime))
                                     .order(SortOrder.Desc)
                             )
                     );
         }
         // 执行查询
         SearchResponse<JobEsEntity> response = null;
         try {
             response = esClient.search(searchBuilder.build(), JobEsEntity.class);
             // 获取结果
             return response.hits().hits().stream()
                     .map(hit -> hit.source())
                     .toList();
         } catch (IOException e) {
             log.error("Failed to searchFeedXmlJobs error}", e);
             throw BusinessException.of(JobResponseCode.JOB_SEARCH_FAIL);
         }
     }

    @Override
    public Pager<JobOptionVO> searchHistoryJobsWithDeduplication(String keyword, int pageIndex, int pageSize, String companyCode) {
        try {
            // 构建查询条件
            List<Query> queryList = new ArrayList<>();
            
            // 必须条件：companyCode匹配
            queryList.add(Query.of(q -> q
                    .term(t -> t
                            .field(LambdaUtil.getFieldName(JobEsEntity::getCompanyCode) + ES_FIELD_KEY_WORD)
                            .value(companyCode)
                    )
            ));
            
            // 必须条件：未删除的职位
            queryList.add(Query.of(q -> q
                    .term(t -> t
                            .field(LambdaUtil.getFieldName(JobEsEntity::getDeleted))
                            .value(0)
                    )
            ));
            
            // 可选条件：title字段模糊匹配
            if (StringUtils.isNotEmpty(keyword)) {
                Query keywordQuery = Query.of(q -> q
                        .wildcard(w -> w
                                .field(LambdaUtil.getFieldName(JobEsEntity::getTitle) + ES_FIELD_KEY_WORD)
                                .value(CommonUtils.getAllLikeEscapeKeyWord(keyword))
                                .caseInsensitive(true)
                        )
                );
                queryList.add(keywordQuery);
            }
            
            // 只查询需要的字段
            String[] selectFields = LambdaUtil.getFieldNames(
                    JobEsEntity::getId, 
                    JobEsEntity::getTitle
            );
            SourceConfig sourceConfig = new SourceConfig.Builder()
                    .filter(f -> f.includes(Arrays.stream(selectFields).toList()))
                    .build();
            
            // 构建搜索请求
            SearchRequest.Builder searchRequest = new SearchRequest.Builder()
                    .index(INDEX)
                    .source(sourceConfig)
                    .query(q -> q.bool(b -> b.must(queryList)))
                    // 根据title字段去重
                    .collapse(c -> c.field(LambdaUtil.getFieldName(JobEsEntity::getTitle) + ES_FIELD_KEY_WORD))
                    // 按id倒序排列
                    .sort(sort -> sort.field(f -> f.field("id").order(SortOrder.Desc)))
                    // 分页设置
                    .from((pageIndex - 1) * pageSize)
                    .size(pageSize);
            
            // 添加cardinality聚合计算去重后的总数量
            searchRequest.aggregations("distinct_count", a -> a
                    .cardinality(c -> c.field(LambdaUtil.getFieldName(JobEsEntity::getTitle) + ES_FIELD_KEY_WORD)));
            
            // 执行查询
            SearchResponse<JobEsEntity> response = esClient.search(searchRequest.build(), JobEsEntity.class);
            
            // 处理结果
            long totalHits = getDistinctTotal(response);
            List<JobOptionVO> resultList = new ArrayList<>();
            for (Hit<JobEsEntity> hit : response.hits().hits()) {
                if (hit.source() != null) {
                    JobEsEntity jobEsEntity = hit.source();
                    JobOptionVO jobOptionVO = new JobOptionVO();
                    jobOptionVO.setJobId(jobEsEntity.getId());
                    jobOptionVO.setTitle(jobEsEntity.getTitle());
                    resultList.add(jobOptionVO);
                }
            }
            
            Pager<JobOptionVO> pageResult = new Pager<>();
            pageResult.setCurrentPageRecords(resultList);
            pageResult.setPageIndex(pageIndex);
            pageResult.setPageSize(pageSize);
            pageResult.setTotalCount(totalHits);
            
            return pageResult;
        } catch (Exception e) {
            log.error("Failed to search history jobs with deduplication, keyword: {}, companyCode: {}", keyword, companyCode, e);
            throw BusinessException.of(JobResponseCode.JOB_SEARCH_FAIL);
        }
    }
    
    @Override
    public List<JobEsEntity> searchJobsByTitleAndLocations(String title, List<LocationValDTO> locations, String companyCode, Long excludeJobId, int size) {
        try {
            // 构建查询请求
            String[] selectFields = LambdaUtil.getFieldNames(JobEsEntity::getId,JobEsEntity::getCompanyCode, JobEsEntity::getTitle,
                    JobEsEntity::getUrlCode, JobEsEntity::getLocations, JobEsEntity::getCreateTime, JobEsEntity::getJobStatus);
            //判断条件 如果title无效 locations为空 size <=0 返回空集合
            if (StringUtils.isBlank(title) || CollectionUtils.isEmpty(locations) || size <= 0) {
                return Collections.emptyList();
            }
            List<Query> queryList = new ArrayList<>();

            // 在queryList中添加排除条件（当excludeJobId不为null时）
            if (excludeJobId != null) {
                Query excludeJobQuery = Query.of(q -> q
                        .bool(b -> b
                                .mustNot(mn -> mn
                                        .term(t -> t
                                                .field(LambdaUtil.getFieldName(JobEsEntity::getId))
                                                .value(excludeJobId)
                                        )
                                )
                        )
                );
                queryList.add(excludeJobQuery);
            }

            // 必须条件：companyCode匹配
            queryList.add(Query.of(q -> q
                    .term(t -> t
                            .field(LambdaUtil.getFieldName(JobEsEntity::getCompanyCode) + ES_FIELD_KEY_WORD)
                            .value(companyCode)
                    )
            ));

            // 必须条件：title精确匹配（忽略大小写）
            if (StringUtils.isNotBlank(title)) {
                Query titleQuery = Query.of(q -> q
                    .term(t -> t
                        .field(LambdaUtil.getFieldName(JobEsEntity::getTitle) + ES_FIELD_KEY_WORD)
                        .value(title)
                        .caseInsensitive(true)
                    )
                );
                queryList.add(titleQuery);
            }
            
            // 必须条件：locations嵌套查询
            if (recruitCommonNacosConfig.isDuplicateJobLocationsEffect() && CollectionUtils.isNotEmpty(locations)) {
                List<Query> locationShouldQueries = locations.stream()
                    .map(loc -> Query.of(innerQ -> innerQ
                        .bool(innerB -> innerB
                            .must(m1 -> m1.term(t -> t.field("locations.countryId").value(loc.getCountryId())))
                            .must(m2 -> m2.term(t -> t.field("locations.stateId").value(loc.getStateId())))
                            .must(m3 -> m3.term(t -> t.field("locations.cityId").value(loc.getCityId())))
                        )
                    ))
                    .toList();

                Query locationsQuery = Query.of(q -> q
                        .nested(n -> n
                                .path(LambdaUtil.getFieldName(JobEsEntity::getLocations))
                                .query(nq -> nq
                                        .bool(b -> b.should(locationShouldQueries).minimumShouldMatch("1"))
                                )
                        )
                );
                queryList.add(locationsQuery);
            }
            
            // 必须条件：未删除的职位
            queryList.add(Query.of(q -> q
                .term(t -> t
                    .field(LambdaUtil.getFieldName(JobEsEntity::getDeleted))
                    .value(0)
                )
            ));
            SourceConfig sourceConfig = new SourceConfig.Builder()
                    .filter(f -> f.includes(Arrays.stream(selectFields).toList()))
                    .build();

            // 构建搜索请求
            SearchRequest.Builder searchRequest = new SearchRequest.Builder()
                .index(INDEX)
                .source(sourceConfig)
                .query(q -> q.bool(b -> b.must(queryList)))
                .sort(sort -> sort.field(f -> f.field(LambdaUtil.getFieldName(JobEsEntity::getId)).order(SortOrder.Desc)))
                .size(size);
            
            // 执行查询
            SearchResponse<JobEsEntity> response = esClient.search(searchRequest.build(), JobEsEntity.class);
            
            // 处理结果
            List<JobEsEntity> resultList = new ArrayList<>();
            for (Hit<JobEsEntity> hit : response.hits().hits()) {
                if (hit.source() != null) {
                    resultList.add(hit.source());
                }
            }
            
            return resultList;
        } catch (Exception e) {
            log.error("Failed to search jobs by title and locations, title: {}, locations size: {}", 
                title, locations != null ? locations.size() : 0, e);
            throw BusinessException.of(JobResponseCode.JOB_SEARCH_FAIL);
        }
    }

    /**
     * 使用cardinality聚合获取去重后的总数量
     */
    private long getDistinctTotal(SearchResponse<JobEsEntity> response) {
        // 解析聚合结果
        Aggregate aggregation = response.aggregations().get("distinct_count");
        if (aggregation != null && aggregation.cardinality() != null) {
            return aggregation.cardinality().value();
        }
        return 0;
    }

    @Override
    public List<JobEsEntity> searchRandomJobs(Integer size, String companyCode, Long seed, List<Long> categoryIds, List<LocationValDTO> locations, Set<Long> excludeJobIds) {
        try {
            // Input validation
            if (size == null || size <= 0) {
                return Collections.emptyList();
            }

            // 构建基础查询条件，包含通用过滤条件
            List<Query> mustQueries = buildBasicQueries(companyCode, excludeJobIds);
            
            // 构建分类ID查询条件，支持多个分类ID的OR查询
            Query categoryQuery = buildCategoryIdQuery(categoryIds);
            if (categoryQuery != null) {
                mustQueries.add(categoryQuery);
            }
            
            // 构建位置查询条件
            Query locationQuery = buildLocationQuery(locations);
            if (locationQuery != null) {
                mustQueries.add(locationQuery);
            }
            
            // 创建搜索请求构建器
            SearchRequest.Builder searchBuilder = new SearchRequest.Builder()
                    .index(INDEX)
                    .source(buildSourceConfig())
                    .size(size);
            
            // 应用纯随机策略，使用ES的random_score函数
            applyPureRandomStrategy(searchBuilder, mustQueries, seed);

            // 执行查询
            SearchResponse<JobEsEntity> response = esClient.search(searchBuilder.build(), JobEsEntity.class);

            // 处理结果
            List<JobEsEntity> resultList = new ArrayList<>();
            for (Hit<JobEsEntity> hit : response.hits().hits()) {
                if (hit.source() != null) {
                    resultList.add(hit.source());
                }
            }

            log.debug("Random job search completed. Query conditions: companyCode={}, categoryIds={}, locations={}, seed={}, excludeJobIds={}, resultSize={}",
                    companyCode, categoryIds, locations != null ? locations.size() : 0, seed, 
                    excludeJobIds, resultList.size());

            return resultList;
        } catch (Exception e) {
            log.error("Failed to search random jobs, size: {}, companyCode: {}, categoryIds: {}, locations: {}, seed: {}, excludeJobIds: {}",
                    size, companyCode, categoryIds, locations, seed, excludeJobIds, e);
            throw BusinessException.of(JobResponseCode.JOB_SEARCH_FAIL);
        }
    }

    /**
     * 构建基础查询条件
     * 包含deleted=0和jobStatus=ACTIVE的必须条件，以及可选的companyCode过滤和excludeJobIds排除
     * 
     * @param companyCode 公司代码，可为null
     * @param excludeJobIds 需要排除的职位ID集合，可为null
     * @return 基础查询条件列表
     */
    private List<Query> buildBasicQueries(String companyCode, Set<Long> excludeJobIds) {
        List<Query> queryList = new ArrayList<>();

        // 必须条件：未删除的职位
        queryList.add(Query.of(q -> q
            .term(t -> t
                .field(LambdaUtil.getFieldName(JobEsEntity::getDeleted))
                .value(0)
            )
        ));

        // 必须条件：活跃状态的职位
        queryList.add(Query.of(q -> q
            .term(t -> t
                .field(LambdaUtil.getFieldName(JobEsEntity::getJobStatus))
                .value(JobStatus.ACTIVE.getCode())
            )
        ));

        // 可选条件：companyCode过滤
        if (StringUtils.isNotBlank(companyCode)) {
            queryList.add(Query.of(q -> q
                .term(t -> t
                    .field(LambdaUtil.getFieldName(JobEsEntity::getCompanyCode) + ES_FIELD_KEY_WORD)
                    .value(companyCode)
                )
            ));
        }

        // 可选条件：排除指定的职位ID
        if (CollectionUtils.isNotEmpty(excludeJobIds)) {
            queryList.add(Query.of(q -> q
                .bool(b -> b
                    .mustNot(mn -> mn
                        .terms(t -> t
                            .field(LambdaUtil.getFieldName(JobEsEntity::getId))
                            .terms(tt -> tt.value(excludeJobIds.stream()
                                .map(FieldValue::of)
                                .toList()))
                        )
                    )
                )
            ));
        }

        return queryList;
    }

    /**
     * 构建分类ID查询条件
     * 支持多个分类ID的OR查询（terms查询）
     * 
     * @param categoryIds 分类ID列表，可为null或空
     * @return 分类查询条件，如果categoryIds为空则返回null
     */
    private Query buildCategoryIdQuery(List<Long> categoryIds) {
        if (CollectionUtils.isEmpty(categoryIds)) {
            return null;
        }

        return Query.of(q -> q
            .terms(t -> t
                .field(LambdaUtil.getFieldName(JobEsEntity::getCategoryId))
                .terms(v -> v.value(categoryIds.stream().map(FieldValue::of).toList()))
            )
        );
    }

    /**
     * 构建位置查询条件
     * 支持嵌套对象的多条件查询，多个位置之间是OR关系
     * 
     * @param locations 位置条件列表，可为null或空
     * @return 位置查询条件，如果locations为空则返回null
     */
    private Query buildLocationQuery(List<LocationValDTO> locations) {
        if (CollectionUtils.isEmpty(locations)) {
            return null;
        }

        List<Query> locationShouldQueries = locations.stream()
            .map(loc -> Query.of(innerQ -> innerQ
                .bool(innerB -> innerB
                    .must(m1 -> m1.term(t -> t.field("locations.countryId").value(loc.getCountryId())))
                    .must(m2 -> m2.term(t -> t.field("locations.stateId").value(loc.getStateId())))
                    .must(m3 -> m3.term(t -> t.field("locations.cityId").value(loc.getCityId())))
                )
            ))
            .toList();

        return Query.of(q -> q
            .nested(n -> n
                .path(LambdaUtil.getFieldName(JobEsEntity::getLocations))
                .query(nq -> nq
                    .bool(b -> b.should(locationShouldQueries).minimumShouldMatch("1"))
                )
            )
        );
    }

    /**
     * 构建源字段配置
     * 指定需要返回的字段，减少网络传输和内存使用
     * 
     * @return 源字段配置
     */
    private SourceConfig buildSourceConfig() {
        String[] selectFields = LambdaUtil.getFieldNames(JobEsEntity::getHotList, JobEsEntity::getTitle, JobEsEntity::getCustomerName,
                JobEsEntity::getJobStatus, JobEsEntity::getCreateTime, JobEsEntity::getCreateUser, JobEsEntity::getLocationName,
                JobEsEntity::getModeName, JobEsEntity::getCategoryName, JobEsEntity::getTypeName, JobEsEntity::getId, JobEsEntity::getMinSalary,
                JobEsEntity::getMaxSalary, JobEsEntity::getCurrencyName, JobEsEntity::getLocationId, JobEsEntity::getLocations,
                JobEsEntity::getCompanyCode, JobEsEntity::getModeId, JobEsEntity::getTypeId, JobEsEntity::getCategoryId, JobEsEntity::getCurrency,
                JobEsEntity::getAyrshareStatus, JobEsEntity::getJobDetail, JobEsEntity::getIntelligenceSwitch, JobEsEntity::getScoreRules);
        
        return new SourceConfig.Builder()
                .filter(f -> f.includes(Arrays.stream(selectFields).toList()))
                .build();
    }

    /**
     * 应用纯随机策略
     * 使用ES的random_score函数实现完全随机排序
     * 
     * @param searchBuilder 搜索请求构建器
     * @param mustQueries 必须查询条件列表
     * @param randomSeed 随机种子值，可为null
     */
    private void applyPureRandomStrategy(SearchRequest.Builder searchBuilder, 
                                       List<Query> mustQueries, 
                                       Long randomSeed) {
        // 确定随机种子值，如果未提供则使用当前时间戳
        long seed = randomSeed != null ? randomSeed : System.currentTimeMillis();
        
        // 使用function_score查询结合random_score函数
        Query functionScoreQuery = Query.of(q -> q
                .functionScore(fs -> fs
                        // 设置基础查询为bool查询，包含所有必须条件
                        .query(Query.of(bq -> bq.bool(b -> b.must(mustQueries))))
                        // 添加随机评分函数
                        .functions(f -> f
                                // 使用指定种子的随机评分（转换为字符串）
                                // ES 7.0+要求：使用seed时必须指定field参数
                                .randomScore(rs -> rs
                                        .seed(String.valueOf(seed))
                                        // 使用id字段作为随机基础
                                        .field(LambdaUtil.getFieldName(JobEsEntity::getId))
                                )
                                // 设置权重为1.0
                                .weight(1.0)
                        )
                        // 使用函数分数替换原始分数
                        .boostMode(FunctionBoostMode.Replace)
                        // 多个函数分数求和
                        .scoreMode(FunctionScoreMode.Sum)
                )
        );
        
        // 将function_score查询设置到搜索请求中
        searchBuilder.query(functionScoreQuery);
    }

} 