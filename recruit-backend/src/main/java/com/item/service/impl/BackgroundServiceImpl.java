package com.item.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.base.Strings;
import com.item.convert.BackgroundConverter;
import com.item.dto.background.BackgroundCheckPdfEvent;
import com.item.dto.background.BackgroundCheckReqDTO;
import com.item.dto.background.CreateUserResponse;
import com.item.dto.background.UserBackgroundCheckDTO;
import com.item.dto.background.UserBackgroundInfoDTO;
import com.item.dto.background.WebhookReqDTO;
import com.item.dto.iam.IamUserContextDTO;
import com.item.entity.BackgroundDataEntity;
import com.item.entity.CandidateEntity;
import com.item.entity.CandidateJobEntity;
import com.item.entity.CountryEntity;
import com.item.entity.JobEntity;
import com.item.framework.config.BusinessDeductionPointsConfig;
import com.item.framework.constant.BackgroundCheckEventConstants;
import com.item.framework.constant.BackgroundCheckStatus;
import com.item.framework.constant.CandidateResponseCode;
import com.item.framework.constant.CommonResponseCode;
import com.item.framework.constant.GlobalStatusCode;
import com.item.framework.constant.JobApplyStatus;
import com.item.framework.constant.JobApplyStatusEvent;
import com.item.framework.constant.TransactionNoTypeEnum;
import com.item.framework.constant.TransactionTypeEnum;
import com.item.framework.constant.UnChangeResponseCode;
import com.item.framework.error.BusinessException;
import com.item.framework.http.Pager;
import com.item.mapper.BackgroundDataMapper;
import com.item.mapper.CountryMapper;
import com.item.service.BackgroundService;
import com.item.service.CandidateJobService;
import com.item.service.CandidateService;
import com.item.service.JobFlowService;
import com.item.service.JobService;
import com.item.service.PointService;
import com.item.util.JsonUtils;
import com.item.util.MailUtils;
import com.item.util.RedisSerialNumberUtils;
import com.item.util.UserContextUtil;
import com.item.vo.BackgroundListVO;
import com.item.vo.BackgroundQueryVO;
import com.item.vo.CandidateJobQueryVO;
import com.item.vo.PointLogVO;
import com.item.vo.background.BackgroundGetTokenResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * <p>
 *
 * </p>
 *
 * @author liuyabin on 2025/8/3
 * @since 1.0.0
 */
@Slf4j
@Service
@RefreshScope
@RequiredArgsConstructor
public class BackgroundServiceImpl extends ServiceImpl<BackgroundDataMapper, BackgroundDataEntity> implements BackgroundService {
    private final CandidateService candidateService;
    private final JobService jobService;
    private final CandidateJobService candidateJobService;
    private final JobFlowService jobFlowService;
    private final MailUtils mailUtils;
    private final BusinessDeductionPointsConfig businessDeductionPointsConfig;
    private final RedisSerialNumberUtils redisSerialNumberUtils;
    private final PointService pointService;
    private final CountryMapper countryMapper;

    @Value("${background.api.key:}")
    private String backgroundCheckKey;
    @Value("${background.companyAccessCode}")
    private String companyAccessCode;
    @Value("${background.webhookUrl}")
    private String webhookUrl;

    private final RestTemplate restTemplate;
    private static final String AUTH_URL = "https://api-v3.authenticating.com/user/jwt";

    @Override
    public void startCheck(Long candidateJobId) {
        CandidateJobEntity candidateJobEntity = candidateJobService.getById(candidateJobId);
        if (candidateJobEntity == null) {
            return;
        }
        CandidateEntity candidate = candidateService.getById(candidateJobEntity.getCandidateId());
        if (candidate == null) {
            log.warn("can not find candidate by id {}", candidateJobEntity.getCandidateId());
            throw BusinessException.of(CandidateResponseCode.CANDIDATE_NOT_FOUND);
        }
        if (candidate.getDateOfBirth()==null){
            throw new BusinessException(CommonResponseCode.BIRTHDAY_MISSING_BACKGROUND_CHECK_FAILED);
        }
        //是否豁免积分
         boolean isExempt=pointService.isExempt(candidateJobEntity.getCompanyCode());

        //检查积分是否充足、冻结积分
        IamUserContextDTO iamUserContextDTO = UserContextUtil.getCurrentUser();
        PointLogVO pointLogVO=new PointLogVO();
        if (!isExempt){
            Long userPrimaryId = pointService.getUserPrimaryId(iamUserContextDTO.getCompanyCode());
            if (userPrimaryId==null){
                throw new BusinessException(UnChangeResponseCode.PRIMARY_ACCOUNT_NOT_FOUND);
            }
            pointLogVO.setUserId(userPrimaryId);
            pointLogVO.setOperUserId(Long.parseLong(iamUserContextDTO.getId()));
            pointLogVO.setPoints(businessDeductionPointsConfig.getBackgroundCheck().getDeductedPoints());
            pointLogVO.setTransactionType(TransactionTypeEnum.BACKGROUND_CHECK.getCode());
            pointLogVO.setTransactionNo(redisSerialNumberUtils.generate(TransactionNoTypeEnum.BACKGROUND_CHECK.getCode()));
            pointLogVO.setCandidateJobId(candidateJobId);
            pointLogVO.setJobId(candidateJobEntity.getJobId());
            pointLogVO.setCandidateId(candidate.getId());
            if (!pointService.checkPointsFreeze(pointLogVO)){
                throw new BusinessException(UnChangeResponseCode.INSUFFICIENT_POINTS);
            }
        }

        try {
            BackgroundCheckReqDTO dto = new BackgroundCheckReqDTO();
            dto.setEmail(candidate.getCandidateEmail());
            dto.setFirstName(candidate.getFirstName());
            dto.setLastName(candidate.getLastName());
            dto.setDob(candidate.getDateOfBirth().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")));
            if (candidate.getPhoneNumber().contains("+")){
                dto.setPhone(candidate.getPhoneNumber());
            } else {
                if (Objects.nonNull(candidate.getCountryId())){
                    CountryEntity countryEntity = countryMapper.selectById(candidate.getCountryId());
                    dto.setPhone("+" + countryEntity.getPhonecode() + candidate.getPhoneNumber());
                } else {
                    log.error("background check create user error");
                    throw new BusinessException(GlobalStatusCode.FAIL,"background check create user error");
                }
            }

            Map<String, String> headers = new HashMap<>();
            headers.put("Content-Type", "application/json");
            headers.put("authorization", "Bearer " + backgroundCheckKey);

            OkHttpClient client = new OkHttpClient();
            MediaType mediaType = MediaType.parse("application/json");
            RequestBody body = RequestBody.create(mediaType, JsonUtils.toJson(dto));
            Request request = new Request.Builder()
                    .url("https://api-v3.authenticating.com/user/create")
                    .post(body)
                    .addHeader("accept", "application/json")
                    .addHeader("content-type", "application/json")
                    .addHeader("authorization", "Bearer " + backgroundCheckKey)
                    .build();

            Response response = client.newCall(request).execute();
            String respData = response.body().string();
            if (Strings.isNullOrEmpty(respData)) {
                log.warn("authenticate.com response data is empty when call create user api.");
                //如果发生异常、则取消冻结的积分
                if (!isExempt){
                    pointService.cancelPointsFreeze(pointLogVO);
                }
                return;
            }
            UserBackgroundInfoDTO backgroundInfoDTO = JsonUtils.toObject(respData, UserBackgroundInfoDTO.class);
            if (StringUtils.isNotEmpty(backgroundInfoDTO.getErrorMessage())){
                log.error("startCheck authenticate.com candidateJobId:{},response error message: {}",candidateJobId, backgroundInfoDTO.getErrorMessage());
                throw new BusinessException(CommonResponseCode.BACKGROUND_CHECK_FAILED);
            }
            sendBackgroundCheckEmail(dto, backgroundInfoDTO);
            JobEntity jobEntity = jobService.getById(candidateJobEntity.getJobId());

            CreateUserResponse createUserResponse = JsonUtils.toObject(respData, CreateUserResponse.class);
            initBackgroundCheckData(candidateJobId,candidate,candidateJobEntity, jobEntity, createUserResponse.getUserAccessCode());
            jobFlowService.fireEvent(candidateJobId, JobApplyStatusEvent.BACKGROUND_CHECK);
            //扣除冻结的积分
            if (!isExempt){
                pointService.confirmPointsFreeze(pointLogVO);
            }
        } catch (Exception e) {
            log.error("background check create user error: ", e);
            //如果发生异常、则取消冻结的积分
            if (!isExempt){
                pointService.cancelPointsFreeze(pointLogVO);
            }
        }

    }

    private void sendBackgroundCheckEmail(BackgroundCheckReqDTO dto, UserBackgroundInfoDTO userInfo) {
        if (Objects.nonNull(userInfo) && StringUtils.isNotEmpty(userInfo.getUserAccessCode())) {
            try {
                // 设置请求头
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
                headers.setAccept(java.util.Collections.singletonList(org.springframework.http.MediaType.APPLICATION_JSON));
                headers.set("authorization", "Bearer " + backgroundCheckKey);
                // 设置请求体
                Map<String, String> map = new HashMap<>();
                map.put("userAccessCode",userInfo.getUserAccessCode());
                // 创建HttpEntity对象，封装请求头和请求体
                HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(map, headers);
                // 使用postForEntity发送POST请求
                ResponseEntity<BackgroundGetTokenResponse> response = restTemplate.postForEntity(
                        AUTH_URL,
                        requestEntity,
                        BackgroundGetTokenResponse.class
                );
                if (response.getStatusCode().is2xxSuccessful() && Objects.nonNull(response.getBody())) {
                    String token = response.getBody().getToken();
                    if (StringUtils.isEmpty(token)) {
                        log.error("background check email send error, token is empty");
                        throw new BusinessException(CommonResponseCode.BACKGROUND_CHECK_FAILED);
                    }
                    log.info("Obtain user token for background check,token:{}",token);
                    Map<String, Object> vars = new HashMap<>();
                    vars.put("uuid", token);
                    mailUtils.sendHtmlTemplateMail("no-reply@item.com",dto.getEmail(), "Background Check Request", "BackgroundCheck.html", vars);
                } else {
                    log.error("background check email send error");
                    throw new BusinessException(CommonResponseCode.BACKGROUND_CHECK_FAILED);
                }
            } catch (Exception e) {
                log.error("background check email send error: ", e);
                throw new BusinessException(CommonResponseCode.BACKGROUND_CHECK_FAILED);
            }
        } else {
            log.error("sendBackgroundCheckEmail error dto:{},userInfo or userAccessCode is null",dto);
        }
    }

    public void setWebHook() {
        WebhookReqDTO webhookReqDTO = new WebhookReqDTO();
        webhookReqDTO.setWebhookUrl(this.webhookUrl);
        webhookReqDTO.setCompanyAccessCode(this.companyAccessCode);
        OkHttpClient client = new OkHttpClient();

        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, JsonUtils.toJson(client));
        Request request = new Request.Builder()
                .url("https://api-v3.authenticating.com/company/webhook")
                .post(body)
                .addHeader("accept", "application/json")
                .addHeader("content-type", "application/json")
                .addHeader("authorization", "Bearer " + this.backgroundCheckKey)
                .build();
        try {
            client.newCall(request).execute();
        } catch (IOException e) {
            log.error("background check set webhook error: ", e);
        }
    }

    @Override
    public void backgroundPdfReportProcess(String data) {
        try {
            String event = JsonUtils.getNodeByName("event", data).asText();
            if (!BackgroundCheckEventConstants.USER_PDF_REPORT_GENERATION.equals(event)) {
                return;
            }
            BackgroundCheckPdfEvent result = JsonUtils.toObject(data, BackgroundCheckPdfEvent.class);
            if (result.getOrder() == null)
                return;
            LambdaQueryWrapper<BackgroundDataEntity> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(BackgroundDataEntity::getUserAccessCode, result.getOrder().getUserAccessCode());

            BackgroundDataEntity backgroundDataEntity = this.getOne(wrapper);
            backgroundDataEntity.setReportUrl(result.getOrder().getReportLink());
            if (result.getOrder().getExpires() != null) {
                backgroundDataEntity.setExpires(LocalDateTime.parse(result.getOrder().getExpires()));
            }
            backgroundDataEntity.setReportStage(result.getOrder().getStatus());

            this.saveOrUpdate(backgroundDataEntity);
        } catch (IOException e) {
            log.error("background webhook get data format error: ", e);
        }
    }

    @Override
    public UserBackgroundCheckDTO getUserBackgroundCheckResult(Long id) {
        BackgroundDataEntity entity = this.getById(id);
        if (entity == null) {
            return null;
        }

        return BackgroundConverter.INSTANCE.converToDTO(entity);
    }

    @Override
    public UserBackgroundCheckDTO getUserReportByCandidateAndJob(Long candidateId, Long jobId) {
        LambdaQueryWrapper<BackgroundDataEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BackgroundDataEntity::getCandidateId, candidateId);
        wrapper.eq(BackgroundDataEntity::getJobId, jobId);
        wrapper.orderByAsc(BackgroundDataEntity::getId);
        wrapper.last("limit 1");
        BackgroundDataEntity entity = this.getOne(wrapper);
        if (entity == null) {
            return null;
        }
        return BackgroundConverter.INSTANCE.converToDTO(entity);
    }

    private void initBackgroundCheckData(Long candidateJobId,CandidateEntity candidate,CandidateJobEntity candidateJobEntity, JobEntity jobEntity, String userAccessCode) {
        BackgroundDataEntity backgroundDataEntity = new BackgroundDataEntity();
        backgroundDataEntity.setCandidateJobId(candidateJobId);
        backgroundDataEntity.setCandidateId(candidate.getId());
        backgroundDataEntity.setJobId(jobEntity.getId());
        backgroundDataEntity.setJobTitle(jobEntity.getTitle());
        backgroundDataEntity.setEmail(candidate.getCandidateEmail());
        backgroundDataEntity.setApplicationDate(candidateJobEntity.getCreateTime());

        backgroundDataEntity.setBackgroundStatus(BackgroundCheckStatus.INITIATED.getCode());
        backgroundDataEntity.setEmail(candidate.getCandidateEmail());
        backgroundDataEntity.setCandidateName(candidate.getCandidateName());
        backgroundDataEntity.setFirstName(candidate.getFirstName());
        backgroundDataEntity.setLastName(candidate.getLastName());
        backgroundDataEntity.setBackgroundDate(LocalDateTime.now());
        backgroundDataEntity.setUserAccessCode(userAccessCode);
        this.save(backgroundDataEntity);
    }


    /**
     * 背调列表
     * @param vo
     * @return
     */
    @Override
    public Pager<BackgroundListVO> selectBackgroundPageList(BackgroundQueryVO vo) {
        CandidateJobQueryVO queryVO= CandidateJobQueryVO.builder().pageIndex(vo.getPageIndex()).pageSize(vo.getPageSize())
                .jobId(vo.getJobId()).applyStatus(JobApplyStatus.BACKGROUND.getCode()).build();
        //查询符合要求的候选人与职位表记录
        Page<CandidateJobEntity> page=candidateJobService.selectCandidateJobPageList(queryVO);
        //背景调查列表数据
        List<BackgroundListVO> backgroundListVOS=new ArrayList<>();
        List<CandidateJobEntity> records = page.getRecords();
        if (CollectionUtils.isNotEmpty(records)){
            //候选人职位关联id
            List<Long> cadidateJobIds=records.stream().map(CandidateJobEntity::getId).toList();
            //背景调查结果
            List<BackgroundDataEntity> backgroundDataEntities = listByCandidateJobIds(cadidateJobIds);
            Map<Long, BackgroundDataEntity> backgroundMap = backgroundDataEntities.stream()
                    .collect(Collectors.toMap(BackgroundDataEntity::getCandidateJobId, v -> v, (existing, replacement) -> replacement));
            for (CandidateJobEntity candidateJob: records){
                if (backgroundMap.containsKey(candidateJob.getId())){
                    BackgroundListVO backgroundListVO=BackgroundConverter.INSTANCE.converToListVO(backgroundMap.get(candidateJob.getId()));
                    backgroundListVOS.add(backgroundListVO);
                }
            }
        }
        Pager<BackgroundListVO> pager = new Pager<>();
        pager.setCurrentPageRecords(backgroundListVOS);
        pager.setPageIndex(vo.getPageIndex());
        pager.setPageSize(vo.getPageSize());
        pager.setTotalCount(page.getTotal());
        return pager;
    }


    /**
     * 查询背景记录列表
     * @param candidateJobIds
     */
    private List<BackgroundDataEntity> listByCandidateJobIds(List<Long> candidateJobIds){
        LambdaQueryWrapper<BackgroundDataEntity> wrapper=new LambdaQueryWrapper<>();
        wrapper.in(BackgroundDataEntity::getCandidateJobId, candidateJobIds);
        return this.list(wrapper);
    }

}
