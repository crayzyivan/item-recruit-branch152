package com.item.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.item.convert.CandidateConverter;
import com.item.dto.iam.IamUserContextDTO;
import com.item.dto.iam.IamUserRegisterCandidateReqDTO;
import com.item.framework.annotation.Auth;
import com.item.framework.constant.CandidateResponseCode;
import com.item.framework.constant.GlobalStatusCode;
import com.item.framework.constant.RoleType;
import com.item.framework.error.BusinessException;
import com.item.service.AIService;
import com.item.service.CandidateService;
import com.item.util.UserContextUtil;
import com.item.vo.CandidateFullDetailsVO;
import com.item.vo.CandidateProfileVO;
import com.item.vo.CandidateRegisterRequestVO;
import com.item.vo.CandidateRegisterResponseVO;
import com.item.vo.CandidateRequestVO;
import com.item.vo.CandidateVO;
import com.item.vo.PreLoginCheckResVO;
import com.item.vo.iam.IamUserRegisterCandidateResVO;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/candidate")
@RequiredArgsConstructor
@Slf4j
public class CandidateController {

    private final CandidateService candidateService;

    private final AIService aiService;

    private final ObjectMapper objectMapper;

    private final Validator validator;

    @PostMapping("/register")
    public CandidateRegisterResponseVO register(@RequestBody @Validated CandidateRegisterRequestVO requestVO) {
        return CandidateConverter.INSTANCE.convertDtoToVo(candidateService.register(CandidateConverter.INSTANCE.convertVoToDto(requestVO)));
    }

    /**
     * 应聘者注册 iam
     * @param request
     * @return
     */
    @PostMapping("/new-register")
    public IamUserRegisterCandidateResVO newRegister(@RequestBody @Validated IamUserRegisterCandidateReqDTO request) {
        return candidateService.registerCandidate(request);
    }

    /**
     * 废弃接口 20250919与前端确认未使用
     * @param token
     * @return
     */
    @Deprecated
    @PostMapping(value = "pre-login-check")
    public PreLoginCheckResVO preLoginCheck(@RequestHeader("Authorization") String token){
        return candidateService.preLoginCheck(token);
    }

    @Auth(roleType = RoleType.CANDIDATE)
    @PostMapping(value = "/uploadResume")
    public CandidateVO uploadResume(@RequestBody @Validated CandidateRequestVO candidateVO
    ) {
        try {
            return CandidateConverter.INSTANCE.convertDtoToCandidateVO(candidateService.uploadResumeAndAddCandidate(CandidateConverter.INSTANCE.convertCandidateRequestVOToDto(candidateVO),false));
        } catch (Exception e) {
            if (e instanceof ConstraintViolationException cex) {
                throw cex;
            } else {
                log.error("Resume upload failed",e);
                throw new BusinessException(GlobalStatusCode.FAIL,"Resume upload failed");
            }
        }
    }

    /**
     * 编辑简历
     * @param candidateVO
     * @return
     */
    @Auth(roleType = RoleType.CANDIDATE)
    @PostMapping(value = "/edit-candidate")
    public CandidateVO editCandidate(@RequestBody @Validated CandidateRequestVO candidateVO
    ) {
        try {

            return CandidateConverter.INSTANCE.convertDtoToCandidateVO(candidateService.uploadResumeAndAddCandidate(CandidateConverter.INSTANCE.convertCandidateRequestVOToDto(candidateVO),true));
        } catch (Exception e) {
            if (e instanceof ConstraintViolationException cex) {
                throw cex;
            } else {
                log.error("Resume upload failed",e);
                throw new BusinessException(GlobalStatusCode.FAIL,"Resume upload failed");
            }
        }
    }

    @Auth(roleType = RoleType.CANDIDATE)
    @PostMapping("/resume-parsing")
    public CandidateRequestVO resumeParsing(@RequestPart(value = "file") MultipartFile file){
        //校验文件类型（MIME Type）
        String contentType = file.getContentType();
        String filename = file.getOriginalFilename();
        boolean isValidPdf = "application/pdf".equalsIgnoreCase(contentType)
                && filename != null && filename.toLowerCase().endsWith(".pdf");

        boolean isValidDocx = filename != null && filename.toLowerCase().endsWith(".docx");

        if (!isValidPdf && !isValidDocx) {
            throw new BusinessException(GlobalStatusCode.UNSUPPORTED_FILE_TYPE, "Only PDF and Word(.docx) formats are supported.");
        }
        return aiService.resumeParsing(file);
    }

    @Auth(roleType = RoleType.SUB_USER)
    @PostMapping("/resume-parsing-dashboard")
    public CandidateRequestVO resumeParsingDashboard(@RequestPart(value = "file") MultipartFile file){
        //校验文件类型（MIME Type）
        String contentType = file.getContentType();
        String filename = file.getOriginalFilename();
        boolean isValidPdf = "application/pdf".equalsIgnoreCase(contentType)
                && filename != null && filename.toLowerCase().endsWith(".pdf");

        boolean isValidDocx = filename != null && filename.toLowerCase().endsWith(".docx");

        if (!isValidPdf && !isValidDocx) {
            throw new BusinessException(GlobalStatusCode.UNSUPPORTED_FILE_TYPE, "Only PDF and Word(.docx) formats are supported.");
        }
        return aiService.resumeParsing(file);
    }

    /**
     * 获取候选人全量数据
     * @param email
     * @return
     */
    @Auth(roleType = RoleType.CANDIDATE)
    @GetMapping("/full-details")
    public CandidateFullDetailsVO getCandidateFullDetailsVO(String email){
        return CandidateConverter.INSTANCE.convertToCandidateFullDetailsVO(candidateService.getByEmailFromEs(email));
    }

    /**
     * 获取当前候选人的详细信息
     * 通过JWT认证获取当前登录候选人的完整个人信息，包括基本信息、教育经历、工作经历等
     * @return 候选人完整详细信息
     */
    @Auth(roleType = RoleType.CANDIDATE)
    @GetMapping("/details")
    public CandidateProfileVO getCandidateDetails() {
        IamUserContextDTO currentUser = UserContextUtil.getCurrentUserCandidateNeedLogin();
        return candidateService.getCandidateDetailsByCandidateId(Long.valueOf(currentUser.getId()));
    }

    //@Auth(roleType = RoleType.CANDIDATE)
    @GetMapping("/check-resume-uploaded")
    public boolean checkResumeUploaded() {
        IamUserContextDTO currentUser = UserContextUtil.getCurrentUserNeedLogin();
        return candidateService.checkResumeUploaded(Long.valueOf(currentUser.getId()));
    }



    @Auth(roleType = RoleType.CANDIDATE)
    @PostMapping(value = "/upload")
    public String upload(@RequestPart(value = "file") MultipartFile file) {
        // 校验文件类型（MIME Type）
        String contentType = file.getContentType();
        String filename = file.getOriginalFilename();
        if (!"application/pdf".equalsIgnoreCase(contentType) || filename == null || !filename.toLowerCase().endsWith(".pdf")) {
            throw new BusinessException(CandidateResponseCode.RESUME_INVALID_FORMAT);
        }
        return candidateService.upload(file);
    }

}