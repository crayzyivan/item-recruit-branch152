package com.item.es.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.Refresh;
import co.elastic.clients.elasticsearch._types.Result;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregate;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.RangeQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.TermQuery;
import co.elastic.clients.elasticsearch.core.GetResponse;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.UpdateByQueryResponse;
import co.elastic.clients.elasticsearch.core.UpdateRequest;
import co.elastic.clients.elasticsearch.core.UpdateResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.core.search.SourceConfig;
import co.elastic.clients.elasticsearch.core.search.TotalHits;
import co.elastic.clients.json.JsonData;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.item.convert.CandidateJobConvert;
import com.item.dto.CityDTO;
import com.item.dto.CountryDTO;
import com.item.dto.DictionaryDTO;
import com.item.dto.FullLocationDTO;
import com.item.dto.StateDTO;
import com.item.dto.report.CandidateSimpleDTO;
import com.item.entity.CandidateEsEntity;
import com.item.es.ResumeEsService;
import com.item.es.entity.JobEsEntity;
import com.item.framework.constant.CommonConstants;
import static com.item.framework.constant.CommonConstants.StrConstants.ES_FIELD_KEY_WORD;
import com.item.framework.constant.CommonResponseCode;
import com.item.framework.constant.DictionaryEnum;
import com.item.framework.constant.GlobalStatusCode;
import com.item.framework.constant.JobApplyStatus;
import com.item.framework.constant.JobResponseCode;
import com.item.framework.constant.SortTypeEnum;
import com.item.framework.error.BusinessException;
import com.item.framework.http.Pager;
import com.item.service.DictionaryService;
import com.item.service.LocationService;
import com.item.util.CommonUtils;
import com.item.util.ExtractKeyWordUtils;
import com.item.util.JsonUtils;
import com.item.util.LambdaUtil;
import com.item.vo.CandidateJobQueryVO;
import com.item.vo.CandidateJobVO;
import com.item.vo.SortFieldVO;
import com.item.vo.ai.JobMatchResultVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.StringJoiner;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ResumeEsServiceImpl implements ResumeEsService {

    private static final String CANDIDATE_INDEX_NAME = "recruit_resume_ext";

    private static final String INDEX_AI_SEARCH = "recruit_resume_match";

    private final ElasticsearchClient esClient;
    private final LocationService locationService;
    private final DictionaryService dictionaryService;

    @Override
    public void saveResumeToEs(CandidateEsEntity candidateEsEntity) {
        try {
            IndexRequest<CandidateEsEntity> request = IndexRequest.of(i -> i
                    .index(CANDIDATE_INDEX_NAME)
                    .id(String.valueOf(candidateEsEntity.getId()))
                    .document(candidateEsEntity)
            );
            IndexResponse response = esClient.index(request);
            log.info("save resume to es response {}", response);
        } catch (Exception e) {
            log.error("save resume to es error", e);
            throw new BusinessException(GlobalStatusCode.SAVE_ES_FAILED,"save resume to es error");
        }
    }

    @Override
    public void saveResumeAiResultToEs(JobMatchResultVO jobMatchResultVO) {
        try {
            IndexRequest<JobMatchResultVO> request = IndexRequest.of(i -> i
                    .index(INDEX_AI_SEARCH)
                    .id(String.valueOf(jobMatchResultVO.getId()))
                    .document(jobMatchResultVO)
            );
            IndexResponse response = esClient.index(request);
            log.info("save ResumeAiResult to es response {}", response);
        } catch (Exception e) {
            log.error("save ResumeAiResult to es error", e);
            throw new BusinessException(GlobalStatusCode.SAVE_ES_FAILED,"save ResumeAiResult to es error");
        }
    }

    @Override
    public CandidateEsEntity searchResumeToEs(Long candidateId) {
        try {
            GetResponse<CandidateEsEntity> response = esClient.get(g -> g
                            .index(CANDIDATE_INDEX_NAME)
                            .id(String.valueOf(candidateId)),
                    CandidateEsEntity.class);

            if (response.source() == null) {
                return null;
            }
            return response.source();
        } catch (IOException e) {
            log.error("search Resume from es error", e);
            throw new BusinessException(GlobalStatusCode.SEARCH_RESUME_BASE_ON_CANDIDATE, "search Resume base on "
                    + "candidate error");
        }
    }

    @Override
    public JobMatchResultVO getMatchResultById(String id) {
        try {
            GetResponse<JobMatchResultVO> response = esClient.get(g -> g
                            .index(INDEX_AI_SEARCH)
                            .id(id),
                    JobMatchResultVO.class);

            if (response.source() == null) {
                return null;
            }
            return response.source();
        } catch (IOException ex) {
            log.error("search from es Resume match result error", ex);
            throw new BusinessException(GlobalStatusCode.SEARCH_RESUME_BASE_ON_ID, "search Resume base on id error");
        }

    }



    /**
     * 分页查询并获取总数
     * @return 查询结果（包含列表和总数）
     */
    public Pager<CandidateSimpleDTO> getCandidateJobByCompanyCode(CandidateJobQueryVO queryVO) {
        try {
            String[] selectFields = LambdaUtil.getFieldNames(JobMatchResultVO::getId,JobMatchResultVO::getJobId,JobMatchResultVO::getCandidateId,JobMatchResultVO::getCandidateName,
                    JobMatchResultVO::getCandidateEmail,JobMatchResultVO::getLocationId,JobMatchResultVO::getTitle,JobMatchResultVO::getLocationName,JobMatchResultVO::getExpectedSalary,
                    JobMatchResultVO::getSalaryTypeId,JobMatchResultVO::getSalaryTypeName,JobMatchResultVO::getCurrencyName,JobMatchResultVO::getApplyStatus,JobMatchResultVO::getJobCreateTime,
                    JobMatchResultVO::getJobUpdateTime,JobMatchResultVO::getCandidateCountryId,JobMatchResultVO::getCandidateCountryName,JobMatchResultVO::getCandidateStateName,
                    JobMatchResultVO::getCandidateCityName,JobMatchResultVO::getCandidateStateId, JobMatchResultVO::getCandidateCityId,
                    JobMatchResultVO::getApplyStatusName,JobMatchResultVO::getJobDeleted);
            SourceConfig sourceConfig = new SourceConfig.Builder()
                    .filter(f -> f.includes(
                            Arrays.stream(selectFields).toList()))
                    .build();
            List<Query> queryList = new ArrayList<>();
            // 构建查询条件
            queryList.add(new TermQuery.Builder().field(LambdaUtil.getFieldName(JobMatchResultVO::getCompanyCode)).value(queryVO.getCompanyCode()).build()._toQuery());
            queryList.add(new RangeQuery.Builder().field(LambdaUtil.getFieldName(JobMatchResultVO::getApplyStatus)).gt(JsonData.of(JobApplyStatus.SUBMITTED.getCode())).build()._toQuery());
            // 构建 SearchRequest
            SearchRequest searchRequest = SearchRequest.of(s -> s
                    .index(INDEX_AI_SEARCH)
                    .query(q -> q.bool(b -> b.must(queryList)))
                    .source(sourceConfig)
                    .sort(sort -> sort
                            .field(f -> f
                                    .field("id")
                                    .order(SortOrder.Desc)
                            )
                    )
                    .from((queryVO.getPageIndex() - 1) * queryVO.getPageSize())
                    .size(queryVO.getPageSize())
                    .trackTotalHits(t -> t.enabled(true))
            );
            // 执行查询
            SearchResponse<JobMatchResultVO> searchResponse = esClient.search(searchRequest, JobMatchResultVO.class);

            // 处理结果
            long totalHits = getTotalHits(searchResponse.hits().total());
            List<CandidateSimpleDTO> resultList = new ArrayList<>();
            // 获取es中的实体类
            List<JobMatchResultVO> jobMatchResultVOs = getJobMatchResultVO(searchResponse);
            List<Long> jobCityIds = jobMatchResultVOs.stream().map(JobMatchResultVO::getLocationId).filter(Objects::nonNull).map(Integer::longValue).distinct().toList();
            Map<Long, String> jobCityFullNameMap = getFullNameByCityIds(jobCityIds);

            List<Long> candidateCountryIds = jobMatchResultVOs.stream().map(JobMatchResultVO::getCandidateCountryId).distinct().toList();
            List<Long> candidateStateIds = jobMatchResultVOs.stream().map(JobMatchResultVO::getCandidateStateId).distinct().toList();
            List<Long> candidateCityIds = jobMatchResultVOs.stream().map(JobMatchResultVO::getCandidateCityId).distinct().toList();

            Map<Long, String> countryMaps = locationService.listIdNameMapByCountryIds(candidateCountryIds);
            Map<Long, String> stateMaps = locationService.listIdNameMapByStateIds(candidateStateIds);
            Map<Long, String> cityMaps = locationService.listIdNameMapByCityIds(candidateCityIds);
            Map<Long, String> dictionaryMap = dictionaryService
                    .listByTypes(Arrays.asList(DictionaryEnum.CHEATING_ANALYSIS_COMPLETED.getName()))
                    .stream()
                    .collect(Collectors.toMap(DictionaryDTO::getId, DictionaryDTO::getValue));
            for (Hit<JobMatchResultVO> hit : searchResponse.hits().hits()) {
                if (hit.source() != null) {
                    JobMatchResultVO jobMatchResultVO = hit.source();
                    CandidateSimpleDTO simpleDTO = CandidateJobConvert.INSTANCE.jobMatchResultVOToJobSimpleDTO(jobMatchResultVO);
                    if (Objects.nonNull(jobMatchResultVO.getJobDeleted()) && jobMatchResultVO.getJobDeleted()) {
                        simpleDTO.setJobTitle(CommonConstants.StrConstants.JOB_BY_DELETED);
                    }
                    simpleDTO.setSalaryTypeName(dictionaryMap.get(jobMatchResultVO.getSalaryTypeId()));
                    simpleDTO.setJobLocation(jobCityFullNameMap.getOrDefault(
                            CommonUtils.getSafeValueDefault0FormInt(jobMatchResultVO::getLocationId),
                            simpleDTO.getJobLocation()));
                    FullLocationDTO fullLocationDTO = new FullLocationDTO();
//                    fullLocationDTO.setCityName(jobMatchResultVO.getCandidateCityName());
//                    fullLocationDTO.setStateName(jobMatchResultVO.getCandidateStateName());
//                    fullLocationDTO.setCountryName(jobMatchResultVO.getCandidateCountryName());
                    fullLocationDTO.setCityName(cityMaps.getOrDefault(CommonUtils.getSafeValueDefault0(jobMatchResultVO::getCandidateCityId), jobMatchResultVO.getCandidateCityName()));
                    fullLocationDTO.setStateName(stateMaps.getOrDefault(CommonUtils.getSafeValueDefault0(jobMatchResultVO::getCandidateStateId), jobMatchResultVO.getCandidateStateName()));
                    fullLocationDTO.setCountryName(countryMaps.getOrDefault(CommonUtils.getSafeValueDefault0(jobMatchResultVO::getCandidateCountryId), jobMatchResultVO.getCandidateCountryName()));
                    simpleDTO.setFullLocation(fullLocationDTO);
                    resultList.add(simpleDTO);
                }
            }
            Pager<CandidateSimpleDTO> pageResult = new Pager<>();
            pageResult.setCurrentPageRecords(resultList);
            pageResult.setPageIndex(queryVO.getPageIndex());
            pageResult.setPageSize(queryVO.getPageSize());
            pageResult.setTotalCount(totalHits);

            return pageResult;
        } catch (Exception e) {
            log.error("Failed to search getCandidateJobByCompanyCode in ES CandidateJobQueryVO {}", queryVO, e);
            throw BusinessException.of(GlobalStatusCode.SEARCH_RESUME_CANDIDATE,"Failed to search getCandidateJobByCompanyCode in ES");
        }
    }

    private Map<Long, String> getFullNameByCityIds(List<Long> jobCityId) {
        List<CityDTO> cityDTOS = locationService.listByCityIds(jobCityId);
        List<Long> jobStateIds = cityDTOS.stream().map(CityDTO::getStateId).filter(Objects::nonNull).distinct().toList();
        List<StateDTO> stateDTOS = locationService.listByStateIds(jobStateIds);
        Map<Long, StateDTO> idStateMap = stateDTOS.stream().collect(Collectors.toMap(StateDTO::getId, Function.identity(), (s1, s2) -> s1));
        List<Long> jobCountryIds = stateDTOS.stream().map(StateDTO::getCountryId).filter(Objects::nonNull).distinct().toList();
        List<CountryDTO> countryDTOS = locationService.listByCountryIds(jobCountryIds);
        Map<Long, CountryDTO> idCountryMap = countryDTOS.stream().collect(Collectors.toMap(CountryDTO::getId, Function.identity(), (c1, c2) -> c1));

        Map<Long, String> idFullName = new HashMap<>(cityDTOS.size());
        for (CityDTO c : cityDTOS) {
            StateDTO stateDTO = idStateMap.get(c.getStateId());
            if (stateDTO == null) {
                log.warn("city not found state city {}", c);
                continue;
            }
            CountryDTO countryDTO = idCountryMap.get(stateDTO.getCountryId());
            if (countryDTO == null) {
                log.warn("city not found country city {}, state {}", c, stateDTO);
                continue;
            }
            idFullName.put(c.getId(), CommonUtils.getLocationName(countryDTO.getName(), stateDTO.getName(), c.getName()));
        }
        return idFullName;
    }

    private List<JobMatchResultVO> getJobMatchResultVO(SearchResponse<JobMatchResultVO> searchResponse) {
        try {
        List<Hit<JobMatchResultVO>> hits = searchResponse.hits().hits();
        return hits.stream().filter(Objects::nonNull).map(Hit::source).toList();
        } catch (Exception e) {
            log.error("getJobMatchResultVO ", e);
            return List.of();
        }
    }

    // 安全获取总记录数
    private long getTotalHits(TotalHits totalHits) {
        if (totalHits == null) return 0;
        return totalHits.value();
    }

    /**
     * 更新JobMatchResultVOES字段值
     * @param documentId
     * @param fieldsValues
     */
    @Override
    public void updateMatchEsFieldValue(String documentId, Map<String,Object> fieldsValues){
        try {
            esClient.update(u -> u
                            .index(INDEX_AI_SEARCH)
                            .id(documentId)
                            .doc(fieldsValues),
                    JobMatchResultVO.class
            );
        } catch (IOException e) {
            log.error("update JobMatchResultVO to es error", e);
            throw new BusinessException(GlobalStatusCode.SAVE_ES_FAILED,"update es error");
        }
    }

    @Override
    public void updateCandidate(CandidateEsEntity candidateEsEntity) {
        try {
            Map<String, Object> updateMap = JsonUtils.toMap(candidateEsEntity);
            updateMap.remove(LambdaUtil.getFieldName(JobEsEntity::getId));
            UpdateRequest<Object, Object> updateRequest = UpdateRequest.of(u -> u
                    .index(CANDIDATE_INDEX_NAME)
                    .id(candidateEsEntity.getId().toString())
                    .doc(updateMap)
                    .docAsUpsert(true)
                    .refresh(Refresh.WaitFor));
            UpdateResponse<Object> updateResult = esClient.update(updateRequest, Objects.class);
            log.info("update job to es {} response {}", updateMap, updateResult);
            if (updateResult.result().equals(Result.Updated)) {
                log.info("Document updated successfully");
            }
        } catch (ElasticsearchException e) {
            log.warn("Document not found {}", candidateEsEntity, e);
        } catch (IOException e) {
            log.error("update error job {} ", candidateEsEntity, e);
            throw BusinessException.of(JobResponseCode.JOB_UPDATE_FAIL);
        }
    }


    /**
     * 去重后进行条件模糊分页查询
     * @return 查询结果
     * @throws IOException
     */
    public Pager<CandidateSimpleDTO> getApplicationsCandidateByCompanyCode(CandidateJobQueryVO queryVO) {
        try {

            String[] selectFields = LambdaUtil.getFieldNames(JobMatchResultVO::getCandidateId,JobMatchResultVO::getCandidateName,JobMatchResultVO::getCandidateCreateTime,JobMatchResultVO::getCandidateUpdateTime,
                    JobMatchResultVO::getCandidateEmail,JobMatchResultVO::getExpectedSalary,JobMatchResultVO::getSalaryTypeId,JobMatchResultVO::getSalaryTypeName,
                    JobMatchResultVO::getCandidateCityId,JobMatchResultVO::getCandidateStateId,JobMatchResultVO::getCandidateCountryId,JobMatchResultVO::getCurrencyTypeId,
                    JobMatchResultVO::getCurrencyName,JobMatchResultVO::getCandidateCountryName,JobMatchResultVO::getCandidateStateName,JobMatchResultVO::getCandidateCityName);
            SourceConfig sourceConfig = new SourceConfig.Builder()
                    .filter(f -> f.includes(
                            Arrays.stream(selectFields).toList()))
                    .build();
            List<Query> queryList = new ArrayList<>();
            // 构建查询条件
            queryList.add(new TermQuery.Builder().field(LambdaUtil.getFieldName(JobMatchResultVO::getCompanyCode)).value(queryVO.getCompanyCode()).build()._toQuery());
            queryList.add(new RangeQuery.Builder().field(LambdaUtil.getFieldName(JobMatchResultVO::getApplyStatus)).gt(JsonData.of(JobApplyStatus.SUBMITTED.getCode())).build()._toQuery());
            if (StringUtils.hasText(queryVO.getCandidateName())) {
                String allLikeEscapeKeyWord = CommonUtils.getAllLikeEscapeKeyWord(queryVO.getCandidateName());
                Query keywordQueryPhrase = Query.of(q -> q
                        .wildcard(m -> m
                                .value(allLikeEscapeKeyWord)
                                .field(LambdaUtil.getFieldName(JobMatchResultVO::getCandidateName) + ES_FIELD_KEY_WORD)
                                .caseInsensitive(true)
                        )
                );
                queryList.add(keywordQueryPhrase);
            }

            // 构建搜索请求
            SearchRequest.Builder searchRequest = new SearchRequest.Builder()
                    .index(INDEX_AI_SEARCH)
                    .source(sourceConfig)
                    .query(q -> q.bool(b -> b.must(queryList)))
                    // 去重设置 - 使用collapse根据指定字段去重
                    .collapse(c -> c.field(LambdaUtil.getFieldName(JobMatchResultVO::getCandidateId)))
                    .sort(sort -> sort
                            .field(f -> f
                                    .field(LambdaUtil.getFieldName(JobMatchResultVO::getCandidateId))
                                    .order(SortOrder.Desc)
                            )
                    )
                    // 分页设置
                    .from((queryVO.getPageIndex() - 1) * queryVO.getPageSize())
                    .size(queryVO.getPageSize());
            // 添加cardinality聚合，计算去重后的总数量
            searchRequest.aggregations("distinct_count", a -> a
                    .cardinality(c -> c.field(LambdaUtil.getFieldName(JobMatchResultVO::getCandidateId))));

            // 执行查询
            SearchResponse<JobMatchResultVO> response = esClient.search(searchRequest.build(), JobMatchResultVO.class);

            // 处理结果
            long totalHits = getDistinctTotal(response);
            List<CandidateSimpleDTO> resultList = new ArrayList<>();
            for (Hit<JobMatchResultVO> hit : response.hits().hits()) {
                if (hit.source() != null) {
                    JobMatchResultVO jobMatchResultVO = hit.source();
                    CandidateSimpleDTO simpleDTO = CandidateJobConvert.INSTANCE.jobMatchResultVOToSimpleDTO(jobMatchResultVO);
                    FullLocationDTO fullLocationDTO = new FullLocationDTO();
                    fullLocationDTO.setCityId(jobMatchResultVO.getCandidateCityId());
                    fullLocationDTO.setStateId(jobMatchResultVO.getCandidateStateId());
                    fullLocationDTO.setCountryId(jobMatchResultVO.getCandidateCountryId());
                    simpleDTO.setFullLocation(fullLocationDTO);
                    resultList.add(simpleDTO);
                }
            }
            Pager<CandidateSimpleDTO> pageResult = new Pager<>();
            pageResult.setCurrentPageRecords(resultList);
            pageResult.setPageIndex(queryVO.getPageIndex());
            pageResult.setPageSize(queryVO.getPageSize());
            pageResult.setTotalCount(totalHits);

            return pageResult;
        } catch (Exception e) {
            log.error("Failed to search getApplicationsCandidateByCompanyCode in ES CandidateJobQueryVO {}", queryVO, e);
            throw BusinessException.of(GlobalStatusCode.SEARCH_RESUME_CANDIDATE,"Failed to search getApplicationsCandidateByCompanyCode in ES");
        }

    }

    /**
     * 拆分关键词（处理连续空格、前后空格）
     */
    private static List<String> splitKeywords(String keywords) {
        if (keywords == null || keywords.trim().isEmpty()) {
            return new ArrayList<>();
        }
        // 按空格拆分，过滤空字符串
        return Arrays.stream(keywords.trim().split("\\s+"))
                .filter(k -> !k.isEmpty())
                .toList();
    }

    /**
     * 使用cardinality聚合获取去重后的总数量
     */
    private long getDistinctTotal(SearchResponse<JobMatchResultVO> countResponse) {
        // 解析聚合结果
        Aggregate aggregation = countResponse.aggregations().get("distinct_count");
        if (aggregation != null && aggregation.cardinality() != null) {
            return aggregation.cardinality().value();
        }
        return 0;
    }


    /**
     * 根据指定字段和值列表批量更新数据
     * @return 批量更新结果
     * @throws IOException
     */
    public void batchUpdateByJobId(JobMatchResultVO jobMatchResultVO) {
        try {

            // 1. 构建查询条件：status 为 "pending" 且 create_time 在 7 天前
            Query query = Query.of(q -> q
                    .bool(b -> b
                            .filter(f -> f.term(t -> t.field(LambdaUtil.getFieldName(JobMatchResultVO::getJobId)).value(jobMatchResultVO.getJobId())))
                    )
            );
            // 将自定义对象转换为字段映射（包含嵌套字段）
            Map<String, Object> fieldMap = convertObjectToFieldMap(jobMatchResultVO);
            fieldMap.remove(LambdaUtil.getFieldName(JobMatchResultVO::getJobId));
            if (fieldMap.isEmpty()){
                return;
            }

            // 构建更新脚本
            String scriptSource = buildUpdateScript(fieldMap.keySet());

            // 准备脚本参数
            Map<String, JsonData> params = new HashMap<>();
            fieldMap.forEach((key, value) -> params.put(key, JsonData.of(value)));

            // 2. 执行条件更新
            UpdateByQueryResponse response = esClient.updateByQuery(u -> u
                    .index(INDEX_AI_SEARCH)
                    .query(query)  // 匹配条件
                    .script(s -> s  // 更新脚本
                            .inline(i -> i
                                    .source(scriptSource)
                                    .params(params)
                            )
                    )
            );
            // 处理结果
            log.info("Number of matching documents:{}, Number of successful updates:{}" ,response.total(),response.updated());
        } catch (IOException e) {
            log.error("update JobMatchResultVO to es error", e);
            throw new BusinessException(GlobalStatusCode.SAVE_ES_FAILED,"update es error");
        }
    }

    /**
     * 根据指定字段和值列表批量更新数据
     * @return 批量更新结果
     * @throws IOException
     */
    public void batchUpdateByCandidateId(JobMatchResultVO jobMatchResultVO) {
        try {

            // 1. 构建查询条件：status 为 "pending" 且 create_time 在 7 天前
            Query query = Query.of(q -> q
                    .bool(b -> b
                            .filter(f -> f.term(t -> t.field(LambdaUtil.getFieldName(JobMatchResultVO::getCandidateId)).value(jobMatchResultVO.getCandidateId())))
                    )
            );
            // 将自定义对象转换为字段映射（包含嵌套字段）
            Map<String, Object> fieldMap = convertObjectToFieldMap(jobMatchResultVO);
            fieldMap.remove(LambdaUtil.getFieldName(JobMatchResultVO::getCandidateId));
            if (fieldMap.isEmpty()){
                return;
            }

            // 构建更新脚本
            String scriptSource = buildUpdateScript(fieldMap.keySet());

            // 准备脚本参数
            Map<String, JsonData> params = new HashMap<>();
            fieldMap.forEach((key, value) -> params.put(key, JsonData.of(value)));

            // 2. 执行条件更新
            UpdateByQueryResponse response = esClient.updateByQuery(u -> u
                    .index(INDEX_AI_SEARCH)
                    .query(query)  // 匹配条件
                    .script(s -> s  // 更新脚本
                            .inline(i -> i
                                    .source(scriptSource)
                                    .params(params)
                            )
                    )
            );
            // 处理结果
            log.info("batchUpdateByCandidateId Number of matching documents:{}, Number of successful updates:{},scriptSource:{}" ,response.total(),response.updated(),scriptSource);
        } catch (IOException e) {
            log.error("batchUpdateByCandidateId update JobMatchResultVO to es error", e);
            throw new BusinessException(GlobalStatusCode.SAVE_ES_FAILED,"update JobMatchResultVO error");
        }
    }

    /**
     * 将自定义对象转换为字段映射   nacos配置值为null的默认不进行转换
     * @param updateObject
     * @return
     */
    private Map<String, Object> convertObjectToFieldMap(Object updateObject) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            // 支持 Java 8 时间类型序列化/反序列化
            objectMapper.registerModule(new JavaTimeModule());
            return objectMapper.convertValue(updateObject, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert object to Map", e);
        }
    }


    /**
     * 构建更新脚本
     */
    private String buildUpdateScript(Iterable<String> fieldNames) {
        StringJoiner joiner = new StringJoiner("; ");
        for (String field : fieldNames) {
            String escapedField = escapeFieldName(field);
            joiner.add(String.format("ctx._source.%s = params.%s", escapedField, field));
        }
        return joiner.toString();
    }

    /**
     * 处理字段名中的特殊字符和嵌套字段
     */
    private String escapeFieldName(String fieldName) {
        if (fieldName.contains(".")) {
            String[] parts = fieldName.split("\\.");
            StringJoiner joiner = new StringJoiner(".");
            for (String part : parts) {
                joiner.add("'" + part + "'");
            }
            return joiner.toString();
        }
        return fieldName;
    }

    @Override
    public Pager<JobMatchResultVO> getCandidateJobRecords(Long candidateId,String companyCode,int pageIndex, int pageSize) {
        try {
            // 构建查询条件
            List<Query> queryList = new ArrayList<>();
            queryList.add(new TermQuery.Builder().field(LambdaUtil.getFieldName(JobMatchResultVO::getCandidateId))
                    .value(candidateId).build()._toQuery());
            queryList.add(new TermQuery.Builder().field(LambdaUtil.getFieldName(JobMatchResultVO::getCompanyCode))
                    .value(companyCode).build()._toQuery());

            // 构建SearchRequest
            SearchRequest searchRequest = SearchRequest.of(s -> s
                    .index(INDEX_AI_SEARCH)
                    .query(q -> q.bool(b -> b.must(queryList)))
                    .sort(sort -> sort
                            .field(f -> f
                                    .field(LambdaUtil.getFieldName(JobMatchResultVO::getCreateTime))
                                    .order(SortOrder.Desc)
                            )
                    )
                    .from((pageIndex - 1) * pageSize)
                    .size(pageSize)
                    .trackTotalHits(t -> t.enabled(true)));

            // 执行查询
            SearchResponse<JobMatchResultVO> searchResponse = esClient.search(searchRequest, JobMatchResultVO.class);

            // 处理结果
            long totalHits = getTotalHits(searchResponse.hits().total());
            List<JobMatchResultVO> jobMatchResultVOs = getJobMatchResultVO(searchResponse);
            // 构建分页结果
            Pager<JobMatchResultVO> pageResult = new Pager<>();
            pageResult.setCurrentPageRecords(jobMatchResultVOs);
            pageResult.setPageIndex(pageIndex);
            pageResult.setPageSize(pageSize);
            pageResult.setTotalCount(totalHits);
            return pageResult;
        } catch (Exception e) {
            log.error("Failed to search getCandidateJobRecords in ES candidateId={}, pageIndex={}, pageSize={}", candidateId, pageIndex, pageSize, e);
            throw BusinessException.of(GlobalStatusCode.SEARCH_RESUME_CANDIDATE, "Failed to search getCandidateJobRecords in ES");
        }
    }

    /**
     * 查询应聘者职位列表
     * @param queryVO
     * @return
     */
    @Override
    public Pager<CandidateJobVO> queryCandidateJobVoList(CandidateJobQueryVO queryVO) {
        try {
            if (queryVO == null) {
                return new Pager<>();
            }
            // 定义需要返回的字段
            String[] selectFields = LambdaUtil.getFieldNames(
                    JobMatchResultVO::getId,
                    JobMatchResultVO::getJobId,
                    JobMatchResultVO::getAssessmentScore,
                    JobMatchResultVO::getCandidateId,
                    JobMatchResultVO::getCandidateName,
                    JobMatchResultVO::getCandidateEmail,
                    JobMatchResultVO::getCandidateCityId,
                    JobMatchResultVO::getCandidateStateId,
                    JobMatchResultVO::getCandidateCountryId,
                    JobMatchResultVO::getCandidateCountryName,
                    JobMatchResultVO::getCandidateStateName,
                    JobMatchResultVO::getCandidateCityName,
                    JobMatchResultVO::getPhoneNumber,
                    JobMatchResultVO::getUpdateTime,
                    JobMatchResultVO::getCreateTime
            );
            SourceConfig sourceConfig = new SourceConfig.Builder()
                    .filter(f -> f.includes(Arrays.stream(selectFields).toList()))
                    .build();
            List<Query> queryList = buildQueryConditions(queryVO);

            // 如果没有任何查询条件，添加一个匹配所有的查询（可选：根据业务需求决定是否允许）
            if (queryList.isEmpty()) {
                queryList.add(Query.of(q -> q.matchAll(m -> m)));
            }
            // 构建搜索请求
            SearchRequest.Builder searchRequest = new SearchRequest.Builder()
                    .index(INDEX_AI_SEARCH)
                    .source(sourceConfig)
                    .query(q -> q.bool(b -> b.must(queryList)))
                    .from((queryVO.getPageIndex() - 1) * queryVO.getPageSize())
                    .size(queryVO.getPageSize());

            // 应用排序设置
            if (queryVO.getSortFields() != null && !queryVO.getSortFields().isEmpty()) {
                for (SortFieldVO sortField : queryVO.getSortFields()) {
                    if (StringUtils.hasText(sortField.getFieldName())){
                        SortOrder sortOrder = SortTypeEnum.DESC.getType().equalsIgnoreCase(sortField.getSortOrder()) ? SortOrder.Desc : SortOrder.Asc;
                        searchRequest.sort(sort -> sort.field(f -> f.field(sortField.getFieldName()).order(sortOrder)));
                    }
                }
            } else {
                // 默认排序：按候选人更新时间降序
                searchRequest.sort(sort -> sort
                        .field(f -> f
                                .field(LambdaUtil.getFieldName(JobMatchResultVO::getUpdateTime))
                                .order(SortOrder.Desc)
                        )
                );
            }

            SearchResponse<JobMatchResultVO> response = esClient.search(searchRequest.build(), JobMatchResultVO.class);

            // 获取总记录数
            long totalHits=0;
            if (response.hits() != null && response.hits().total() != null) {
                totalHits = response.hits().total().value();
            }

            // 转换结果列表
            List<CandidateJobVO> resultList = new ArrayList<>();
            if (response.hits() != null && response.hits().hits() != null) {
                for (Hit<JobMatchResultVO> hit : response.hits().hits()) {
                    if (hit != null && hit.source() != null) {
                        JobMatchResultVO jobMatchResultVO = hit.source();
                        CandidateJobVO candidateJobVO = CandidateJobConvert.INSTANCE.jobMatchResultVOToCandidateJobVO(jobMatchResultVO);
                        resultList.add(candidateJobVO);
                    }
                }
            }

            // 构建分页结果
            Pager<CandidateJobVO> pageResult = new Pager<>();
            pageResult.setCurrentPageRecords(resultList);
            pageResult.setPageIndex(queryVO.getPageIndex());
            pageResult.setPageSize(queryVO.getPageSize());
            pageResult.setTotalCount(totalHits);

            return pageResult;
        } catch (Exception e) {
            log.error("Failed to search queryCandidateJobVoList in ES CandidateJobQueryVO {}", queryVO, e);
            throw new BusinessException(CommonResponseCode.ES_JOB_LIST_SEARCH_FAIL);
        }
    }

    /**
     * 构建查询条件
     * @param queryVO 查询参数
     * @return 查询条件列表
     */
    private List<Query> buildQueryConditions(CandidateJobQueryVO queryVO) {
        List<Query> queryList = new ArrayList<>();
        // 职位ID查询条件
        if (queryVO.getJobId() != null) {
            queryList.add(new TermQuery.Builder()
                    .field(LambdaUtil.getFieldName(JobMatchResultVO::getJobId))
                    .value(queryVO.getJobId())
                    .build()._toQuery());
        }

        // 申请状态查询条件
        if (queryVO.getApplyStatus() != null) {
            queryList.add(new TermQuery.Builder()
                    .field(LambdaUtil.getFieldName(JobMatchResultVO::getApplyStatus))
                    .value(queryVO.getApplyStatus())
                    .build()._toQuery());
        }

        // 候选人名称模糊查询条件
        if (StringUtils.hasText(queryVO.getCandidateName())) {
            List<String> keywordList = splitKeywords(queryVO.getCandidateName());
            for (String keyword : keywordList) {
                Query keywordQuery = Query.of(q -> q
                        .wildcard(w -> w
                                .field(LambdaUtil.getFieldName(JobMatchResultVO::getCandidateName))
                                .value(CommonUtils.buildWildcardPattern(queryVO.getCandidateName()))
                        )
                );
                queryList.add(keywordQuery);
            }
        }

        return queryList;
    }


    /**
     * 查询全部的候选人信息
     * @param queryVO
     * @return
     */
    public Pager<CandidateSimpleDTO> getAllCandidatesByQuery(CandidateJobQueryVO queryVO) {
        try {
            // Define fields to select
            String[] selectFields = LambdaUtil.getFieldNames(
                    CandidateEsEntity::getId,
                    CandidateEsEntity::getCandidateName,
                    CandidateEsEntity::getCandidateEmail,
                    CandidateEsEntity::getCreateTime,
                    CandidateEsEntity::getUpdateTime,
                    CandidateEsEntity::getExpectedSalary,
                    CandidateEsEntity::getSalaryTypeId,
                    CandidateEsEntity::getCurrencyTypeId,
                    CandidateEsEntity::getCityId,
                    CandidateEsEntity::getStateId,
                    CandidateEsEntity::getCountryId,
                    CandidateEsEntity::getCountryName,
                    CandidateEsEntity::getStateName,
                    CandidateEsEntity::getCityName);

            SourceConfig sourceConfig = new SourceConfig.Builder()
                    .filter(f -> f.includes(Arrays.stream(selectFields).toList()))
                    .build();

            List<Query> queryList = new ArrayList<>();

            // Filter by deleted status (only active candidates)
            queryList.add(new TermQuery.Builder()
                    .field(LambdaUtil.getFieldName(CandidateEsEntity::getDeleted))
                    .value(0)
                    .build()._toQuery());

            // Search by candidate name if provided
            if (StringUtils.hasText(queryVO.getCandidateName())) {
                // Split keywords by space and create OR condition for flexible matching
                String allLikeEscapeKeyWord = CommonUtils.getAllLikeEscapeKeyWord(queryVO.getCandidateName());
                Query keywordQueryPhrase = Query.of(q -> q
                        .wildcard(m -> m
                                .value(allLikeEscapeKeyWord)
                                .field(LambdaUtil.getFieldName(JobMatchResultVO::getCandidateName) + ES_FIELD_KEY_WORD)
                                .caseInsensitive(true)
                        )
                );
                queryList.add(keywordQueryPhrase);

            }

            // Build search request
            SearchRequest.Builder searchRequest = new SearchRequest.Builder()
                    .index(CANDIDATE_INDEX_NAME)
                    .source(sourceConfig)
                    .query(q -> q.bool(b -> b.must(queryList)))
                    .sort(sort -> sort
                            .field(f -> f
                                    .field(LambdaUtil.getFieldName(CandidateEsEntity::getId))
                                    .order(SortOrder.Desc)
                            )
                    )
                    // Pagination
                    .from((queryVO.getPageIndex() - 1) * queryVO.getPageSize())
                    .size(queryVO.getPageSize())
                    .trackTotalHits(t -> t.enabled(true));

            // Execute query
            SearchResponse<CandidateEsEntity> response = esClient.search(
                    searchRequest.build(),
                    CandidateEsEntity.class);

            // Process results
            long totalHits = response.hits().total() != null ?
                    response.hits().total().value() : 0;

            List<CandidateSimpleDTO> resultList = new ArrayList<>();
            for (Hit<CandidateEsEntity> hit : response.hits().hits()) {
                if (hit.source() != null) {
                    CandidateEsEntity entity = hit.source();
                    CandidateSimpleDTO simpleDTO = new CandidateSimpleDTO();
                    simpleDTO.setCandidateId(entity.getId());
                    simpleDTO.setCandidateName(entity.getCandidateName());
                    simpleDTO.setCandidateEmail(entity.getCandidateEmail());
                    simpleDTO.setCreateTime(entity.getCreateTime());
                    simpleDTO.setUpdateTime(entity.getUpdateTime());
                    simpleDTO.setExpectedSalary(entity.getExpectedSalary());
                    simpleDTO.setSalaryTypeId(entity.getSalaryTypeId());

                    // Set location info
                    FullLocationDTO fullLocationDTO = new FullLocationDTO();
                    fullLocationDTO.setCityId(entity.getCityId());
                    fullLocationDTO.setStateId(entity.getStateId());
                    fullLocationDTO.setCountryId(entity.getCountryId());
                    simpleDTO.setFullLocation(fullLocationDTO);

                    resultList.add(simpleDTO);
                }
            }

            Pager<CandidateSimpleDTO> pageResult = new Pager<>();
            pageResult.setCurrentPageRecords(resultList);
            pageResult.setPageIndex(queryVO.getPageIndex());
            pageResult.setPageSize(queryVO.getPageSize());
            pageResult.setTotalCount(totalHits);

            return pageResult;
        } catch (Exception e) {
            log.error("Failed to search candidates from recruit_resume_ext, query: {}", queryVO, e);
            throw new BusinessException(CommonResponseCode.SEARCH_RESUME_CANDIDATE);
        }
    }

    @Override
    public List<JobMatchResultVO> searchRecommendCandidatesByTitle(String jobTitle, Set<Long> excludeCandidateIds, Set<String> excludeCompanyCode, int limit) {
        try {
            log.info("Searching candidates by title using JobMatchResultVO: {}, exclude: {}, limit: {}", jobTitle, excludeCandidateIds, limit);

            if (!StringUtils.hasText(jobTitle)) {
                return new ArrayList<>();
            }

            // Define fields to select from JobMatchResultVO
            String[] selectFields = LambdaUtil.getFieldNames(
                    JobMatchResultVO::getId,
                    JobMatchResultVO::getJobId,
                    JobMatchResultVO::getCandidateId,
                    JobMatchResultVO::getCandidateName,
                    JobMatchResultVO::getCandidateEmail,
                    JobMatchResultVO::getCreateTime,
                    JobMatchResultVO::getUpdateTime,
                    JobMatchResultVO::getExpectedSalary,
                    JobMatchResultVO::getSalaryTypeId,
                    JobMatchResultVO::getCurrencyTypeId,
                    JobMatchResultVO::getCandidateCountryId,
                    JobMatchResultVO::getCandidateStateId,
                    JobMatchResultVO::getCandidateCityId,
                    JobMatchResultVO::getCandidateCountryName,
                    JobMatchResultVO::getCandidateStateName,
                    JobMatchResultVO::getCandidateCityName,
                    JobMatchResultVO::getJobTitleScore,
                    JobMatchResultVO::getTitle);

            SourceConfig sourceConfig = new SourceConfig.Builder()
                    .filter(f -> f.includes(Arrays.stream(selectFields).toList()))
                    .build();

            List<Query> queryList = new ArrayList<>();

            // Filter by deleted status (only active records)
            queryList.add(new TermQuery.Builder()
                    .field(LambdaUtil.getFieldName(JobMatchResultVO::getDeleted))
                    .value(0)
                    .build()._toQuery());

            // Exclude already applied candidates
            //  初始化BoolQuery构建器
            BoolQuery.Builder boolBuilder = new BoolQuery.Builder();
            boolean isExclude = false;
            if (excludeCandidateIds != null && !excludeCandidateIds.isEmpty()) {
                // 先添加candidateId的排除条件
                boolBuilder.mustNot(mn -> mn
                        .terms(t -> t
                                .field(LambdaUtil.getFieldName(JobMatchResultVO::getCandidateId))
                                .terms(terms -> terms.value(
                                        excludeCandidateIds.stream()
                                                .map(FieldValue::of)
                                                .collect(Collectors.toList())
                                ))
                        )
                );
                isExclude = true;
            }

            // 新增判断：excludeCompanyCode非空时才追加公司编码排除条件
            if (CollectionUtils.isNotEmpty(excludeCompanyCode)) {
                boolBuilder.mustNot(mn -> mn
                        .terms(t -> t
                                .field(LambdaUtil.getFieldName(JobMatchResultVO::getCompanyCode))
                                .terms(terms -> terms.value(
                                        excludeCompanyCode.stream()
                                                .map(FieldValue::of)
                                                .collect(Collectors.toList())
                                ))
                        )
                );
                isExclude = true;
            }

            if (isExclude) {
                // 构建最终的Query并添加到列表
                Query excludeQuery = Query.of(q -> q.bool(boolBuilder.build()));
                queryList.add(excludeQuery);
            }

//            if (excludeCandidateIds != null && !excludeCandidateIds.isEmpty()) {
//                Query excludeQuery = Query.of(q -> q
//                        .bool(b -> b
//                                .mustNot(mn -> mn
//                                        .terms(t -> t
//                                                .field(LambdaUtil.getFieldName(JobMatchResultVO::getCandidateId))
//                                                .terms(terms -> terms.value(excludeCandidateIds.stream()
//                                                        .map(id -> co.elastic.clients.elasticsearch._types.FieldValue.of(id))
//                                                        .toList()))
//                                        )
//                                )
//                        )
//                );
//                queryList.add(excludeQuery);
//            }

            // Search by job title using multiple query strategies for better precision
            List<String> keywordList = CommonUtils.splitKeywords(jobTitle);
            List<Query> titleQueryList = new ArrayList<>();

            for (String keyword : keywordList) {
                // 1. 精确匹配（权重最高）
                Query exactMatchQuery = Query.of(q -> q
                    .match(m -> m
                        .field(LambdaUtil.getFieldName(JobMatchResultVO::getTitle))
                        .query(keyword)
                        .boost(3.0f) // 高权重
                    )
                );
                titleQueryList.add(exactMatchQuery);
                
                // 2. 短语匹配（权重中等）
                Query phraseMatchQuery = Query.of(q -> q
                    .matchPhrase(mp -> mp
                        .field(LambdaUtil.getFieldName(JobMatchResultVO::getTitle))
                        .query(keyword)
                        .boost(2.0f) // 中等权重
                    )
                );
                titleQueryList.add(phraseMatchQuery);
                
                // 3. 通配符匹配（权重最低，仅用于兜底）
                Query wildcardQuery = Query.of(q -> q
                    .wildcard(w -> w
                        .field(LambdaUtil.getFieldName(JobMatchResultVO::getTitle))
                        .value(CommonUtils.getAllLikeEscapeKeyWord(keyword.toLowerCase()))
                        .caseInsensitive(true)
                        .boost(1.0f) // 低权重
                    )
                );
                titleQueryList.add(wildcardQuery);
            }

            // Use OR condition (should) so any keyword match will return the result
            if (!titleQueryList.isEmpty()) {
                Query titleQuery = Query.of(q -> q.bool(b -> b.should(titleQueryList)));
                queryList.add(titleQuery);
            }

            // Build search request - 使用 collapse 去重，按 candidateId 分组
            SearchRequest.Builder searchRequest = new SearchRequest.Builder()
                    .index(INDEX_AI_SEARCH)
                    .source(sourceConfig)
                    .query(q -> q.bool(b -> b.must(queryList)))
                    .collapse(c -> c.field(LambdaUtil.getFieldName(JobMatchResultVO::getCandidateId))) // 按候选人ID去重
                    .sort(sort -> sort
                            .score(s -> s.order(SortOrder.Desc)) // 按相关性评分排序
                    )
                    .size(limit) // 直接返回去重后的结果
                    .trackTotalHits(t -> t.enabled(false)); // Don't track total hits for performance

            // Execute query
            SearchResponse<JobMatchResultVO> response = esClient.search(
                    searchRequest.build(),
                    JobMatchResultVO.class);

            // Process results - 使用 collapse 后结果已经去重
            List<JobMatchResultVO> resultList = new ArrayList<>();
            
            for (Hit<JobMatchResultVO> hit : response.hits().hits()) {
                if (hit.source() != null) {
                    resultList.add(hit.source());
                }
            }
            log.info("Found {} unique candidates matched by title using JobMatchResultVO: {}", resultList.size(), jobTitle);
            return resultList;
        } catch (Exception e) {
            log.error("Failed to search candidates by title using JobMatchResultVO: {}", jobTitle, e);
            throw new BusinessException(CommonResponseCode.SEARCH_RESUME_CANDIDATE);
        }
    }

    @Override
    public List<CandidateEsEntity> searchRecommendCandidatesByEmploymentHistory(List<String> skills, Set<Long> includeCandidateIds, int limit) {
        try {
            log.info("Searching candidates by employment history skills: {}, include: {}, limit: {}", skills, includeCandidateIds, limit);
            
            if (skills == null || skills.isEmpty()) {
                return new ArrayList<>();
            }
            // Define fields to select
            String[] selectFields = LambdaUtil.getFieldNames(
                    CandidateEsEntity::getId,
                    CandidateEsEntity::getCandidateName,
                    CandidateEsEntity::getCandidateEmail,
                    CandidateEsEntity::getCreateTime,
                    CandidateEsEntity::getUpdateTime,
                    CandidateEsEntity::getCountryId,
                    CandidateEsEntity::getStateId,
                    CandidateEsEntity::getCityId,
                    CandidateEsEntity::getCountryName,
                    CandidateEsEntity::getStateName,
                    CandidateEsEntity::getCityName);

            SourceConfig sourceConfig = new SourceConfig.Builder()
                    .filter(f -> f.includes(Arrays.stream(selectFields).toList()))
                    .build();

            List<Query> queryList = new ArrayList<>();

            // Filter by deleted status (only active candidates)
            queryList.add(new TermQuery.Builder()
                    .field(LambdaUtil.getFieldName(CandidateEsEntity::getDeleted))
                    .value(0)
                    .build()._toQuery());

            // Include only specified candidates
            if (includeCandidateIds != null && !includeCandidateIds.isEmpty()) {
                Query includeQuery = Query.of(q -> q
                        .terms(t -> t
                                .field(LambdaUtil.getFieldName(CandidateEsEntity::getId))
                                .terms(terms -> terms.value(includeCandidateIds.stream()
                                        .map(id -> co.elastic.clients.elasticsearch._types.FieldValue.of(id))
                                        .toList()))
                        )
                );
                queryList.add(includeQuery);
            }

            // Search by skills in employment histories using multiple query strategies
            List<Query> skillQueryList = new ArrayList<>();
            // 提取技能关键词
            List<String> skillKeywords = ExtractKeyWordUtils.extractNounKeywords (skills,3);
            if (CollectionUtils.isNotEmpty(skillKeywords)){
                return new ArrayList<>();
            }

            for (String keyword : skillKeywords) {
                // 1. 精确匹配（权重最高）
                Query exactMatchQuery = Query.of(q -> q
                        .nested(n -> n
                                .path("employmentHistories")
                                .query(nq -> nq
                                        .match(m -> m
                                                .field("employmentHistories.jobTitle")
                                                .query(keyword)
                                                .boost(3.0f) // 高权重
                                        )
                                )
                        )
                );
                skillQueryList.add(exactMatchQuery);

                // 2. 短语匹配（权重中等）
                Query phraseMatchQuery = Query.of(q -> q
                        .nested(n -> n
                                .path("employmentHistories")
                                .query(nq -> nq
                                        .matchPhrase(mp -> mp
                                                .field("employmentHistories.jobTitle")
                                                .query(keyword)
                                                .boost(2.0f) // 中等权重
                                        )
                                )
                        )
                );
                skillQueryList.add(phraseMatchQuery);

                // 3. 通配符匹配（权重最低，仅用于兜底）
                Query wildcardQuery = Query.of(q -> q
                        .nested(n -> n
                                .path("employmentHistories")
                                .query(nq -> nq
                                        .wildcard(w -> w
                                                .field("employmentHistories.jobTitle")
                                                .value(CommonUtils.getAllLikeEscapeKeyWord(keyword.toLowerCase()))
                                                .caseInsensitive(true)
                                                .boost(1.0f) // 低权重
                                        )
                                )
                        )
                );
                skillQueryList.add(wildcardQuery);
            }
            
            // Use OR condition (should) so any skill match will return the result
            if (!skillQueryList.isEmpty()) {
                Query skillsQuery = Query.of(q -> q.bool(b -> b.should(skillQueryList)));
                queryList.add(skillsQuery);
            }

            // Build search request - 按ES分数排序，取前10条
            SearchRequest.Builder searchRequest = new SearchRequest.Builder()
                    .index(CANDIDATE_INDEX_NAME)
                    .source(sourceConfig)
                    .query(q -> q.bool(b -> b.must(queryList)))
                    .sort(sort -> sort
                            .score(s -> s.order(SortOrder.Desc)) // 按相关性评分排序
                    )
                    .size(Math.min(limit, 10)) // 限制最多返回10条记录
                    .trackTotalHits(t -> t.enabled(false)); // Don't track total hits for performance

            // Execute query
            SearchResponse<CandidateEsEntity> response = esClient.search(
                    searchRequest.build(),
                    CandidateEsEntity.class);

            // Process results
            List<CandidateEsEntity> resultList = new ArrayList<>();
            for (Hit<CandidateEsEntity> hit : response.hits().hits()) {
                if (hit.source() != null) {
                    resultList.add(hit.source());
                }
            }

            log.info("Found {} candidates matched by employment history skills (sorted by relevance score, max 10): {}", resultList.size(), skills);
            return resultList;
            
        } catch (Exception e) {
            log.error("Failed to search candidates by employment history skills: {}", skills, e);
            throw new BusinessException(CommonResponseCode.SEARCH_RESUME_CANDIDATE);
        }
    }
} 