package com.item.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.item.convert.CandidateConverter;
import com.item.convert.CandidateEducationConverter;
import com.item.convert.CandidateJobConvert;
import com.item.convert.EmploymentHistoryConverter;
import com.item.dto.CandidateDTO;
import com.item.dto.CityDTO;
import com.item.dto.CountryDTO;
import com.item.dto.DictionaryDTO;
import com.item.dto.FullLocationDTO;
import com.item.dto.LoginDTO;
import com.item.dto.StateDTO;
import com.item.dto.iam.FeignResponse;
import com.item.dto.iam.IamUserContextDTO;
import com.item.dto.iam.IamUserRegisterCandidateReqDTO;
import com.item.dto.iam.IamUserRegisterReqDTO;
import com.item.dto.iam.IamUserRegisterResDTO;
import com.item.dto.report.CandidateSimpleDTO;
import com.item.entity.CandidateEducationEntity;
import com.item.entity.CandidateEntity;
import com.item.entity.CandidateEsEntity;
import com.item.entity.CountryEntity;
import com.item.entity.EmploymentHistoryEntity;
import com.item.es.ResumeEsService;
import com.item.framework.config.IamCommonConfig;
import com.item.framework.constant.CandidateResponseCode;
import com.item.framework.constant.CommonResponseCode;
import static com.item.framework.constant.CommonResponseCode.CURRENT_USER_EXIST_REGISTER;
import com.item.framework.constant.DictionaryEnum;
import com.item.framework.constant.GlobalStatusCode;
import com.item.framework.error.BusinessException;
import com.item.framework.http.Pager;
import com.item.mapper.CandidateMapper;
import com.item.service.CandidateService;
import com.item.service.DictionaryService;
import com.item.service.LocationService;
import com.item.service.ShortIdGenerator;
import com.item.service.client.adapter.IamRpcAdapter;
import com.item.util.Argon2PasswordUtil;
import com.item.util.CommonUtils;
import com.item.util.JsonUtils;
import com.item.util.RedisKeyUtil;
import com.item.util.S3Utils;
import com.item.util.UserContextUtil;
import com.item.vo.CandidateJobQueryVO;
import com.item.vo.CandidateProfileEducationVO;
import com.item.vo.CandidateProfileVO;
import com.item.vo.PreLoginCheckResVO;
import com.item.vo.ai.JobMatchResultVO;
import com.item.vo.iam.IamUserRegisterCandidateResVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 候选人服务实现类
 */
@Slf4j
@Service
@RefreshScope
@RequiredArgsConstructor
public class CandidateServiceImpl extends ServiceImpl<CandidateMapper, CandidateEntity> implements CandidateService {
    private final S3Utils s3Utils;
    private final ResumeEsService resumeEsService;
    private final LocationService locationService;
    private final IamRpcAdapter iamRpcAdapter;
    private final IamCommonConfig iamCommonConfig;
    private final CandidateConverter candidateConverter;
    private final RedissonClient redissonClient;
    private final DictionaryService dictionaryService;
    private final ShortIdGenerator shortIdGenerator;

    @Value("${recruit.all-candidates-user}")
    private String allCandidatesUser;

    private static final TypeReference<FeignResponse<IamUserContextDTO>> RESPONSE_TYPE= new TypeReference<FeignResponse<IamUserContextDTO>>() {};

    @Override
    public CandidateEntity getByEmail(String email) {
        if (!StringUtils.hasText(email)) {
            return null;
        }
        LambdaQueryWrapper<CandidateEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CandidateEntity::getCandidateEmail, email);
        return this.getOne(wrapper);
    }

    @Override
    public CandidateEsEntity getByEmailFromEs(String email) {
        CandidateEntity entity = this.getByEmail(email);
        if (entity == null) {
            return null;
        }
        return resumeEsService.searchResumeToEs(entity.getId());
    }

    @Override
    public CandidateEntity getByPhoneNumber(String phoneNumber) {
        if (!StringUtils.hasText(phoneNumber)) {
            return null;
        }
        LambdaQueryWrapper<CandidateEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CandidateEntity::getPhoneNumber, phoneNumber);
        return this.getOne(wrapper);
    }
    
    @Override
    public List<CandidateEntity> getByCandidateName(String candidateName) {
        if (!StringUtils.hasText(candidateName)) {
            return List.of();
        }
        LambdaQueryWrapper<CandidateEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(CandidateEntity::getCandidateName, candidateName);
        return this.list(wrapper);
    }
    
    @Override
    public CandidateEntity getByEmailAndPassword(String email, String password) {
        if (!StringUtils.hasText(email) || !StringUtils.hasText(password)) {
            return null;
        }
        LambdaQueryWrapper<CandidateEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CandidateEntity::getCandidateEmail, email)
               .eq(CandidateEntity::getPassword, password);
        return this.getOne(wrapper);
    }
    
    @Override
    public boolean existsByEmail(String email) {
        if (!StringUtils.hasText(email)) {
            return false;
        }
        LambdaQueryWrapper<CandidateEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CandidateEntity::getCandidateEmail, email);
        return this.count(wrapper) > 0;
    }
    
    @Override
    public boolean existsByPhoneNumber(String phoneNumber) {
        if (!StringUtils.hasText(phoneNumber)) {
            return false;
        }
        LambdaQueryWrapper<CandidateEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CandidateEntity::getPhoneNumber, phoneNumber);
        return this.count(wrapper) > 0;
    }

    @Override
    public CandidateDTO register(CandidateDTO registerDTO) {
        QueryWrapper<CandidateEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(CandidateEntity::getCandidateEmail, registerDTO.getCandidateEmail());
        CandidateEntity rCandidate = this.getOne(queryWrapper);
        if ( Objects.nonNull(rCandidate) ) {
            throw  new BusinessException(GlobalStatusCode.EMAIL_ADDRESS_USE,"Email address is already in use");
        }
        CandidateEntity candidate = CandidateConverter.INSTANCE.dtoToEntity(registerDTO);
        candidate.setCandidateName(registerDTO.getCandidateName());
        candidate.setCandidateEmail(registerDTO.getCandidateEmail());
        candidate.setCandidatePermanentEmail(registerDTO.getCandidateEmail());
        candidate.setPassword(Argon2PasswordUtil.encryptPassword(registerDTO.getPassword()));
        candidate.setPhoneNumber(registerDTO.getPhoneNumber());
        candidate.setCreateTime(LocalDateTime.now());
        candidate.setUpdateTime(LocalDateTime.now());
        candidate.setDeleted(0);
        this.save(candidate);
        return CandidateConverter.INSTANCE.convertEntityToDto(candidate);
    }

    @Override
    public CandidateDTO candidateLogin(LoginDTO loginDTO) {
        QueryWrapper<CandidateEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(CandidateEntity::getCandidateEmail, loginDTO.getEmail());
        return CandidateConverter.INSTANCE.convertEntityToDto(this.getOne(queryWrapper));
    }

    @Override
    @Transactional
    public CandidateDTO uploadResumeAndAddCandidate(CandidateDTO candidateDTO,Boolean editFlag)  {
        try {
//            if (!editFlag && !StringUtils.hasText(candidateDTO.getResumeUrl())) {
//                throw new BusinessException(GlobalStatusCode.FAIL,"the resumeUrl cannot be empty");
//            }
            String newResumeUrl=candidateDTO.getResumeUrl();
            if (editFlag && StringUtils.hasText(newResumeUrl) && newResumeUrl.contains("http")) {
                candidateDTO.setResumeUrl(null);
            }
            //存储候选人信息
            Long candidateId = UserContextUtil.getCurrentUserCandidateNeedLogin().getCandidateOneselfId();
            if (candidateId == null) {
                throw BusinessException.of(CandidateResponseCode.CANDIDATE_NOT_FOUND);
            }
            CandidateEntity candidateEntity = this.getById(candidateId);
            if (candidateEntity == null) {
                throw BusinessException.of(CandidateResponseCode.CANDIDATE_NOT_FOUND);
            }

            CandidateConverter.INSTANCE.dtoToEntity(candidateDTO,candidateEntity);
            candidateEntity.setGender(candidateDTO.getGender());
            if (!StringUtils.hasText(newResumeUrl)) {
                candidateEntity.setResumeUrl(null);
            }
            candidateEntity.setDateOfBirth(candidateDTO.getDateOfBirth());
            candidateEntity.setCandidateName(candidateDTO.getFirstName() + " " + candidateDTO.getLastName());
            candidateEntity.setUpdateTime(LocalDateTime.now());
            //添加候选人地址名称
            addCandidateLocationName(candidateEntity);
            //存储教育经历
            List<CandidateEducationEntity> educationEntities = candidateDTO.getEducationList().stream().map(dto -> {
                CandidateEducationEntity entity = CandidateEducationConverter.INSTANCE.convertDtoToEntity(dto);
                entity.setCandidateId(candidateId);
                return entity;
            }).collect(Collectors.toList());

            //存储工作经历
            List<EmploymentHistoryEntity> employmentEntities = candidateDTO.getEmploymentList().stream().map(dto -> {
                EmploymentHistoryEntity entity = EmploymentHistoryConverter.INSTANCE.convertDtoToEntity(dto);
                entity.setCandidateId(candidateId);
                return entity;
            }).collect(Collectors.toList());
            CandidateEsEntity candidateEsEntity = CandidateEsEntity.builder().id(candidateEntity.getId()).candidateEducations(educationEntities).employmentHistories(employmentEntities).build();
            candidateEntity.setUploadStatus(1);
            CandidateConverter.INSTANCE.entityToEsEntity(candidateEntity,candidateEsEntity);
            //简历内容存ES
            resumeEsService.saveResumeToEs(candidateEsEntity);
            //更新JobMatchResultVO中候选人信息
            JobMatchResultVO jobMatchResultVO = new JobMatchResultVO();
            CandidateJobConvert.INSTANCE.candidateEntityToJobMatchResultVO(candidateEntity, jobMatchResultVO);
            resumeEsService.batchUpdateByCandidateId(jobMatchResultVO);

            this.updateById(candidateEntity);

            return CandidateConverter.INSTANCE.convertEntityToDto(candidateEntity);
        } catch (Exception e) {
            log.error("Resume upload failed",e);
            throw new BusinessException(GlobalStatusCode.FAIL,"Resume upload failed");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateCandidate(CandidateDTO candidateDTO) {
        CandidateEntity candidateEntity = this.getById(candidateDTO.getId());
        if (candidateEntity == null) {
            throw BusinessException.of(CandidateResponseCode.CANDIDATE_NOT_FOUND);
        }

        CandidateConverter.INSTANCE.dtoToEntity(candidateDTO,candidateEntity);
        candidateEntity.setGender(candidateDTO.getGender());
        candidateEntity.setDateOfBirth(candidateDTO.getDateOfBirth());
        candidateEntity.setCandidateName(candidateDTO.getFirstName() + " " + candidateDTO.getLastName());

        candidateEntity.setUpdateTime(LocalDateTime.now());
        //添加候选人地址名称
        addCandidateLocationName(candidateEntity);
        // 5. 存储教育经历
        List<CandidateEducationEntity> educationEntities = candidateDTO.getEducationList().stream().map(dto -> {
            CandidateEducationEntity entity = CandidateEducationConverter.INSTANCE.convertDtoToEntity(dto);
            entity.setCandidateId(candidateDTO.getId());
            return entity;
        }).collect(Collectors.toList());

        // 6. 存储工作经历
        List<EmploymentHistoryEntity> employmentEntities = candidateDTO.getEmploymentList().stream().map(dto -> {
            EmploymentHistoryEntity entity = EmploymentHistoryConverter.INSTANCE.convertDtoToEntity(dto);
            entity.setCandidateId(candidateDTO.getId());
            return entity;
        }).collect(Collectors.toList());
        CandidateEsEntity candidateEsEntity = CandidateEsEntity.builder().id(candidateEntity.getId()).candidateEducations(educationEntities).employmentHistories(employmentEntities).build();

        CandidateConverter.INSTANCE.entityToEsEntity(candidateEntity,candidateEsEntity);

        this.updateById(candidateEntity);
        resumeEsService.updateCandidate(candidateEsEntity);

        return true;
    }

    @Override
    public List<CandidateEntity> getByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        LambdaQueryWrapper<CandidateEntity> wrapper = Wrappers.lambdaQuery();
        wrapper.in(CandidateEntity::getId, ids);

        return this.list(wrapper);
    }

    @Override
    public IamUserRegisterCandidateResVO registerCandidate(IamUserRegisterCandidateReqDTO iamUserRegisterCandidateReqDTO) {
        QueryWrapper<CandidateEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(CandidateEntity::getCandidateEmail, iamUserRegisterCandidateReqDTO.getEmail());
        CandidateEntity rCandidate = this.getOne(queryWrapper);
        if ( Objects.nonNull(rCandidate) ) {
            throw new BusinessException(GlobalStatusCode.EMAIL_ADDRESS_USE,"Email address is already in use");
        }
        IamUserRegisterReqDTO iamUserRegisterReqDTO = candidateConverter.convert2Req(iamUserRegisterCandidateReqDTO);
        IamCommonConfig.RegisterCandidateConfig registerCandidate = iamCommonConfig.getRegisterCandidate();
        iamUserRegisterReqDTO.setApplicationNote(registerCandidate.getApplicationNote());
        iamUserRegisterReqDTO.setCompanyCode(registerCandidate.getCompanyCode());
        iamUserRegisterReqDTO.setSource(registerCandidate.getSource());
        iamUserRegisterReqDTO.setEmployeeCode(registerCandidate.getEmployeeCode());
        IamUserRegisterResDTO iamUserRegisterResDTO = iamRpcAdapter.iamUserRegister(iamUserRegisterReqDTO);

        QueryWrapper<CandidateEntity> queryWrapperCandidateId = new QueryWrapper<>();
        queryWrapperCandidateId.lambda().eq(CandidateEntity::getCandidateId, Long.parseLong(iamUserRegisterResDTO.getId()));
        CandidateEntity rCandidateId = this.getOne(queryWrapperCandidateId);
        if ( Objects.nonNull(rCandidateId) ) {
            log.warn("registerCandidate rCandidateId {} iamUserRegisterResDTO {} ", rCandidateId, iamUserRegisterResDTO);
            throw BusinessException.of(CURRENT_USER_EXIST_REGISTER);
        }

        CandidateEntity candidate = new CandidateEntity();
        candidate.setCandidateName(iamUserRegisterResDTO.getUserName());
        candidate.setCandidateEmail(iamUserRegisterResDTO.getEmail());
        candidate.setCandidatePermanentEmail(iamUserRegisterResDTO.getEmail());
//        candidate.setPassword(Argon2PasswordUtil.encryptPassword(registerDTO.getPassword()));
        candidate.setPhoneNumber(iamUserRegisterResDTO.getContactNumber());
        candidate.setCandidateId(Long.parseLong(iamUserRegisterResDTO.getId()));
        candidate.setFirstName(iamUserRegisterResDTO.getFirstName());
        candidate.setLastName(iamUserRegisterResDTO.getLastName());
        candidate.setMiddleName(iamUserRegisterCandidateReqDTO.getMiddleName());
        log.info("registerCandidate candidate {} iamUserRegisterResDTO {}", candidate, iamUserRegisterResDTO);
        this.save(candidate);
        return candidateConverter.convert2Res(iamUserRegisterResDTO);
    }

    @Override
    public PreLoginCheckResVO preLoginCheck(String accessToken) {
        String[] tokenAll = org.apache.commons.lang3.StringUtils.split(" ");
        if (tokenAll == null || tokenAll.length < 2) {
            log.warn("preLoginCheck accessToken {}", accessToken);
            throw BusinessException.of(GlobalStatusCode.TOKEN_INVALID, "token invalid");
        }
        Map<String, Object> userIdByAccessToken = iamRpcAdapter.getUserIdByAccessToken(accessToken);
        FeignResponse<IamUserContextDTO> iamUserResponse = JsonUtils.convertToEntity(userIdByAccessToken, RESPONSE_TYPE);
        IamUserContextDTO data = CommonUtils.getData(iamUserResponse);
        if (data == null) {
            log.warn("preLoginCheck accessToken {}", accessToken);
            throw BusinessException.of(GlobalStatusCode.TOKEN_INVALID, "token invalid");
        }
        String id = data.getId();
        if (org.apache.commons.lang3.StringUtils.isBlank(data.getId())) {
            log.warn("preLoginCheck accessToken {} data {}", accessToken, data);
            throw BusinessException.of(GlobalStatusCode.TOKEN_INVALID, "token invalid");
        }
        RLock lock = redissonClient.getLock(RedisKeyUtil.getLockPreLoginCheckKey(id));
        boolean locked = false;
        try {
            locked = lock.tryLock();
            if (locked) {
                QueryWrapper<CandidateEntity> queryWrapperCandidateId = new QueryWrapper<>();
                queryWrapperCandidateId.lambda().eq(CandidateEntity::getCandidateId, Long.parseLong(id));
                CandidateEntity rCandidateId = this.getOne(queryWrapperCandidateId);
                if (Objects.nonNull(rCandidateId)) {
                    log.warn("registerCandidate rCandidateId {} ", rCandidateId);
                    return PreLoginCheckResVO.buildSuccess(rCandidateId.getId(), data.getUserName(), rCandidateId.getCandidateEmail(), rCandidateId.getCandidateId());
                }
                CandidateEntity candidate = new CandidateEntity();
                candidate.setCandidateName(data.getUserName());
                candidate.setCandidateEmail(data.getEmail());
                candidate.setPhoneNumber(data.getContactNumber());
                candidate.setCandidateId(Long.parseLong(data.getId()));
                candidate.setFirstName(data.getFirstName());
                candidate.setLastName(data.getLastName());
                log.info("registerCandidate candidate {} iamUserRegisterResDTO {}", candidate, data);
                this.save(candidate);
                return PreLoginCheckResVO.buildSuccess(candidate.getId(), data.getUserName(), candidate.getCandidateEmail(), candidate.getCandidateId());
            } else {
                throw BusinessException.of(CommonResponseCode.COMMON_FREQUENT_OPERATION_PLEASE_TRY_AGAIN_LATER);
            }
        } catch (BusinessException e) {
            log.warn("preLoginCheck accessToken {}", accessToken, e);
            throw e;
        } catch (Exception e) {
            log.warn("preLoginCheck accessToken {}", accessToken, e);
            throw BusinessException.of(GlobalStatusCode.TOKEN_INVALID, "token invalid");
        } finally {
            if (locked) {
                lock.unlock();
            }
        }
    }

    @Override
    public List<CandidateEntity> getByCandidateId(Long iamId) {
        LambdaQueryWrapper<CandidateEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CandidateEntity::getCandidateId, iamId);
        wrapper.select(CandidateEntity::getId, CandidateEntity::getCandidateId);
        return this.list(wrapper);
    }

    public CandidateEntity getCandidateByCandidateId(Long iamId) {
        LambdaQueryWrapper<CandidateEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CandidateEntity::getCandidateId, iamId);
        List<CandidateEntity> list = this.list(wrapper);
        if (CollectionUtils.isNotEmpty(list)){
            return list.getFirst();
        }
        return null;
    }

    /**
     * 获取候选人个人资料信息
     * 根据候选人ID获取完整的个人资料信息，包括基本信息、教育经历、工作经历等
     * @param candidateId 候选人ID
     * @return 候选人个人资料信息
     */
    @Override
    public CandidateProfileVO getCandidateDetailsByCandidateId(Long candidateId) {
        try {
            log.info("getCandidateDetailsById candidateId:{}",candidateId);
            if (candidateId == null) {
                return new CandidateProfileVO();
            }
            List<CandidateEntity> candidateEntities = getByCandidateId(candidateId);
            Long id=null;
            if(CollectionUtils.isNotEmpty(candidateEntities)){
                id=candidateEntities.getFirst().getId();
            }
            if(id==null){
                return new CandidateProfileVO();
            }
            // 1. 从ES获取详细信息（教育经历、工作经历、简历内容）
            CandidateEsEntity esEntity = resumeEsService.searchResumeToEs(id);
            if (esEntity == null) {
                return new CandidateProfileVO();
            }
            // 2. 从数据库获取详细信息（基本信息）
            CandidateEntity candidateEntity = this.getById(id);

            // 3.字典查询
            Map<Long, String> dictionaryMap = dictionaryService
                    .listByTypes(Arrays.asList(DictionaryEnum.DEGREE_TYPE.getName(),DictionaryEnum.COMPLETION.getName(),
                                    DictionaryEnum.REPORT.getName(),DictionaryEnum.CHEATING_ANALYSIS_COMPLETED.getName(),
                            DictionaryEnum.CAMER_RECORDING_URL.getName()))
                    .stream()
                    .collect(Collectors.toMap(DictionaryDTO::getId, DictionaryDTO::getValue));

            // 4. 使用CandidateConverter进行转换
            CandidateProfileVO profileVO = CandidateConverter.INSTANCE.convertToCandidateProfileVO(esEntity);
            // 5. 补充基本信息
            profileVO.setFirstName(candidateEntity.getFirstName());
            profileVO.setLastName(candidateEntity.getLastName());
            if (CommonUtils.isLong(candidateEntity.getGender()) && dictionaryMap.containsKey(Long.valueOf(candidateEntity.getGender()))){
                profileVO.setGenderName(dictionaryMap.get(Long.valueOf(candidateEntity.getGender())));
            }
            profileVO.setCurrencyTypeName(dictionaryMap.get(esEntity.getCurrencyTypeId()));
            profileVO.setSalaryTypeName(dictionaryMap.get(esEntity.getSalaryTypeId()));
            if (CollectionUtils.isNotEmpty(profileVO.getEducationList())){
                for(CandidateProfileEducationVO educationVO: profileVO.getEducationList()){
                    educationVO.setDegreeName(dictionaryMap.get(educationVO.getDegree()));
                    educationVO.setInstitutionName(dictionaryMap.get(educationVO.getInstitutionType()));
                }
            }
            if (profileVO.getCountryId()!=null){
                List<CountryDTO> countryDTOS = locationService.listByCountryIds(List.of(profileVO.getCountryId()));
                if (CollectionUtils.isNotEmpty(countryDTOS)){
                    profileVO.setCountryName(countryDTOS.getFirst().getName());
                }
            }
            if(profileVO.getStateId()!=null){
                List<StateDTO> stateDTOS = locationService.listByStateIds(List.of(profileVO.getStateId()));
                if (CollectionUtils.isNotEmpty(stateDTOS)){
                    profileVO.setStateName(stateDTOS.getFirst().getName());
                }
            }
            if(profileVO.getCityId()!=null){
                List<CityDTO> cityDTOS = locationService.listByCityIds(List.of(profileVO.getCityId()));
                if (CollectionUtils.isNotEmpty(cityDTOS)){
                    profileVO.setCityName(cityDTOS.getFirst().getName());
                }
            }
            profileVO.setResumeName(CommonUtils.extractResumeName(candidateEntity.getResumeUrl()));

            CountryDTO countryDTO = locationService.getCountryById(profileVO.getCountryId());
            if (!profileVO.getPhoneNumber().contains("+")){
                if (Objects.nonNull(countryDTO.getId())){
                    profileVO.setPhoneNumber("+" + countryDTO.getPhonecode() + " " + profileVO.getPhoneNumber());
                } else {
                    log.error("background check create user error");
                }
            }
            // 6. 处理简历URL预签名
            try {
                profileVO.setResumeUrl(s3Utils.generatePresignedUrl(esEntity.getResumeUrl(),3600*24));
            } catch (Exception e) {
                profileVO.setResumeUrl("");
            }
            log.info("getMyProfile: successfully retrieved profile for candidateId: {}", id);
            return profileVO;
        } catch (Exception e) {
            log.error("getMyProfile: error retrieving profile for candidateId: {}", candidateId, e);
            throw new BusinessException(CandidateResponseCode.CANDIDATE_INFO_FETCH_FAILED);
        }
    }

    /**
     * 添加候选人地址名称
     * @param candidateEntity
     */
    public void addCandidateLocationName(CandidateEntity candidateEntity){
        List<CountryDTO> countryDTOS = locationService.listByCountryIds(List.of(candidateEntity.getCountryId()));
        if (CollectionUtils.isNotEmpty(countryDTOS)){
            candidateEntity.setCountryName(countryDTOS.getFirst().getName());
        }
        List<StateDTO> stateDTOS = locationService.listByStateIds(List.of(candidateEntity.getStateId()));
        if (CollectionUtils.isNotEmpty(stateDTOS)){
            candidateEntity.setStateName(stateDTOS.getFirst().getName());
        }
        List<CityDTO> cityDTOS = locationService.listByCityIds(List.of(candidateEntity.getCityId()));
        if (CollectionUtils.isNotEmpty(cityDTOS)){
            candidateEntity.setCityName(cityDTOS.getFirst().getName());
        }
    }

    /**
     * 检查简历是否已上传
     * @param candidateId 候选人ID
     * @return 是否已上传
     */
    @Override
    public boolean checkResumeUploaded(Long candidateId) {
        CandidateEntity candidateEntity = getCandidateByCandidateId(candidateId);
        if (candidateEntity==null){
            return false;
        }
        if (candidateEntity.getUploadStatus() != null && candidateEntity.getUploadStatus() == 0) {
            return false;
        }
        return true;
    }


    /**
     * 上传简历附件
     * @param file
     * @return
     */
    @Override
    public String upload(MultipartFile file) {
        try{
            // 1. 校验文件
            if (file == null || file.isEmpty()) {
                throw new BusinessException(CandidateResponseCode.RESUME_FILE_EMPTY);
            }
            if (file.getSize() > 4 * 1024 * 1024) {
                throw new BusinessException(CandidateResponseCode.RESUME_FILE_TOO_LARGE);
            }
            return s3Utils.uploadFile(file);
        }catch (Exception e){
            log.error("Resume upload failed",e);
            throw new BusinessException(GlobalStatusCode.FAIL,"Resume upload failed");
        }
    }

    /**
     * 候选人列表
     * @param queryVO
     * @return
     */
    @Override
    public Pager<CandidateSimpleDTO> getApplicationsCandidateList(CandidateJobQueryVO queryVO) {
        IamUserContextDTO recruitNeedLogin = UserContextUtil.getCurrentUserRecruitNeedLogin();
        List<String> allCandidatesUsers=new ArrayList<>();
        if (StringUtils.hasText(allCandidatesUser)){
            allCandidatesUsers=Arrays.asList(allCandidatesUser.split(","));
        }
        Pager<CandidateSimpleDTO> candidateSimpleDTOPager=new Pager<>();
        if (allCandidatesUsers.contains(recruitNeedLogin.getId()) || allCandidatesUsers.contains(recruitNeedLogin.getEmail())){
            candidateSimpleDTOPager = resumeEsService.getAllCandidatesByQuery(queryVO);

        }else{
            queryVO.setCompanyCode(recruitNeedLogin.getCompanyCode());
            candidateSimpleDTOPager=resumeEsService.getApplicationsCandidateByCompanyCode(queryVO);
        }

        List<CandidateSimpleDTO> records = candidateSimpleDTOPager.getCurrentPageRecords();
        if (CollectionUtils.isNotEmpty(records)){
            List<Long> cityIds = records.stream().map(CandidateSimpleDTO::getFullLocation)
                    .filter(Objects::nonNull).map(FullLocationDTO::getCityId).filter(Objects::nonNull).distinct().toList();
            List<Long> stateIds = records.stream().map(CandidateSimpleDTO::getFullLocation)
                    .filter(Objects::nonNull).map(FullLocationDTO::getStateId).filter(Objects::nonNull).distinct().toList();
            List<Long> countryIds = records.stream().map(CandidateSimpleDTO::getFullLocation)
                    .filter(Objects::nonNull).map(FullLocationDTO::getCountryId).filter(Objects::nonNull).distinct().toList();
            Map<Long, String> cityMap = locationService.listIdNameMapByCityIds(cityIds);
            Map<Long, String> stateMap = locationService.listIdNameMapByStateIds(stateIds);
            Map<Long, String> countryMap = locationService.listIdNameMapByCountryIds(countryIds);
            Map<Long, String> dictionaryMap = dictionaryService
                    .listByTypes(Arrays.asList(DictionaryEnum.DEGREE_TYPE.getName(),DictionaryEnum.COMPLETION.getName(),
                            DictionaryEnum.REPORT.getName(),DictionaryEnum.CHEATING_ANALYSIS_COMPLETED.getName(),
                            DictionaryEnum.CAMER_RECORDING_URL.getName()))
                    .stream()
                    .collect(Collectors.toMap(DictionaryDTO::getId, DictionaryDTO::getValue));
            for (CandidateSimpleDTO  candidateSimpleDTO : records) {
                candidateSimpleDTO.setSalaryTypeName(dictionaryMap.get(candidateSimpleDTO.getSalaryTypeId()));
                candidateSimpleDTO.setCurrencyName(dictionaryMap.get(candidateSimpleDTO.getCurrencyTypeId()));
                FullLocationDTO fullLocation = candidateSimpleDTO.getFullLocation();
                if (fullLocation!=null){
                    fullLocation.setCityName(cityMap.get(fullLocation.getCityId()));
                    fullLocation.setStateName(stateMap.get(fullLocation.getStateId()));
                    fullLocation.setCountryName(countryMap.get(fullLocation.getCountryId()));
                }
            }
        }
        return candidateSimpleDTOPager;
    }
}