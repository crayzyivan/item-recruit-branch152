package com.item.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.item.dto.CandidateDTO;
import com.item.dto.LoginDTO;
import com.item.dto.iam.IamUserRegisterCandidateReqDTO;
import com.item.dto.report.CandidateSimpleDTO;
import com.item.entity.CandidateEntity;
import com.item.entity.CandidateEsEntity;
import com.item.framework.http.Pager;
import com.item.vo.CandidateJobQueryVO;
import com.item.vo.CandidateProfileVO;
import com.item.vo.PreLoginCheckResVO;
import com.item.vo.iam.IamUserRegisterCandidateResVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 候选人服务接口
 */
public interface CandidateService extends IService<CandidateEntity> {
    
    /**
     * 根据邮箱查询候选人
     * @param email 邮箱
     * @return 候选人信息
     */
    CandidateEntity getByEmail(String email);

    CandidateEsEntity getByEmailFromEs(String email);
    
    /**
     * 根据手机号查询候选人
     * @param phoneNumber 手机号
     * @return 候选人信息
     */
    CandidateEntity getByPhoneNumber(String phoneNumber);
    
    /**
     * 根据姓名模糊查询候选人列表
     * @param candidateName 候选人姓名
     * @return 候选人列表
     */
    List<CandidateEntity> getByCandidateName(String candidateName);
    
    /**
     * 根据邮箱和密码查询候选人（用于登录验证）
     * @param email 邮箱
     * @param password 密码
     * @return 候选人信息
     */
    CandidateEntity getByEmailAndPassword(String email, String password);
    
    /**
     * 检查邮箱是否已存在
     * @param email 邮箱
     * @return 是否存在
     */
    boolean existsByEmail(String email);
    
    /**
     * 检查手机号是否已存在
     * @param phoneNumber 手机号
     * @return 是否存在
     */
    boolean existsByPhoneNumber(String phoneNumber);

    CandidateDTO register(CandidateDTO registerDTO);
    CandidateDTO candidateLogin(LoginDTO loginDTO);

    /**
     * 上传简历并添加候选人
     * @param candidateDTO 候选人信息
     * @param edit 是否是编辑简历
     * @return 候选人信息
     */
    CandidateDTO uploadResumeAndAddCandidate( CandidateDTO candidateDTO,Boolean edit) throws Exception;

    boolean updateCandidate(CandidateDTO candidateDTO);

    List<CandidateEntity> getByIds(List<Long> ids);

    IamUserRegisterCandidateResVO registerCandidate(IamUserRegisterCandidateReqDTO iamUserRegisterCandidateReqDTO);

    PreLoginCheckResVO preLoginCheck(String accessToken);


    /**
     * 根据iamId查询
     * @return 候选人列表
     */
    List<CandidateEntity> getByCandidateId(Long iamId);

    /**
     * 根据iamId查询
     * @return 候选人列表
     */
    CandidateEntity getCandidateByCandidateId(Long iamId);

    /**
     * 获取候选人个人资料信息
     * 根据候选人ID获取完整的个人资料信息，包括基本信息、教育经历、工作经历等
     * @param candidateId 候选人ID
     * @return 候选人个人资料信息
     */
    CandidateProfileVO getCandidateDetailsByCandidateId(Long candidateId);

    /**
     * 检查简历是否已上传
     * @param candidateId 候选人ID
     * @return 是否已上传
     */
    boolean checkResumeUploaded(Long candidateId);

    /**
     * 上传简历附件
     * @param file
     * @return
     */
    String upload(MultipartFile file);

    /**
     * 候选人列表
     * @param queryVO
     * @return
     */
    Pager<CandidateSimpleDTO> getApplicationsCandidateList(CandidateJobQueryVO queryVO);
}