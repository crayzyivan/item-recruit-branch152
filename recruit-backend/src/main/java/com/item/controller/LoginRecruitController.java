package com.item.controller;

import com.item.convert.LoginConverter;
import com.item.dto.CandidateDTO;
import com.item.dto.RecruiterDTO;
import com.item.framework.constant.GlobalStatusCode;
import com.item.framework.constant.RoleConstants;
import com.item.framework.error.LoginException;
import com.item.service.CandidateService;
import com.item.service.RecruiterService;
import com.item.util.Argon2PasswordUtil;
import com.item.util.JwtUtils;
import com.item.util.RedissonLoginLimitUtil;
import com.item.vo.LoginRequestVO;
import com.item.vo.LoginResponseVO;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class LoginRecruitController {

    private final RecruiterService recruiterService;

    private final CandidateService candidateService;

    @Resource
    private RedissonLoginLimitUtil loginLimitUtil;

    @PostMapping("/recruiter/login")
    public LoginResponseVO login(@RequestBody LoginRequestVO loginRequestVO) {
        LoginResponseVO vo = new LoginResponseVO();
        String email = vo.getEmail();
        // 检查是否被锁定
        if (loginLimitUtil.isLocked(email)) {
            long remainingTime = loginLimitUtil.getRemainingLockTime(email);
            throw new LoginException(GlobalStatusCode.ACCOUNT_LOCKED,"Account is locked, please try again in " + remainingTime + " minutes");
        }

        RecruiterDTO recruiter = recruiterService.recruiterLogin(LoginConverter.INSTANCE.convertVoToDto(loginRequestVO));
        if (recruiter == null ) {
            loginLimitUtil.recordLoginFail(email);
            long remainingAttempts = loginLimitUtil.getRemainingAttempts(email);
            throw new LoginException(GlobalStatusCode.USER_NOT_FOUND,"The email does not exist."+remainingAttempts+" attempts remaining");
        } else if (!recruiter.getPassword().equals(loginRequestVO.getPassword())) {
            loginLimitUtil.recordLoginFail(email);
            long remainingAttempts = loginLimitUtil.getRemainingAttempts(email);
            throw new LoginException(GlobalStatusCode.PASSWORD_ERROR,"Incorrect password."+remainingAttempts+" attempts remaining");
        }
        Map<String, Object> claims = new HashMap<>();
        claims.put("user", recruiter.getRecruiterName());
        claims.put("email", recruiter.getRecruiterEmail());
        claims.put("role", RoleConstants.MASTER_USER_ROLE);
        String token = JwtUtils.generateToken(claims);
        vo.setToken(token);
        vo.setUserName(recruiter.getRecruiterName());
        vo.setEmail(recruiter.getRecruiterEmail());
        vo.setCompanyName(recruiter.getCompanyName());
        vo.setLoginRole(RoleConstants.MASTER_USER_ROLE);
        // 登录成功，清除失败记录
        loginLimitUtil.clearLoginFail(email);
        return vo;
    }

    @PostMapping("/candidate/login")
    public LoginResponseVO candidateLogin(@RequestBody LoginRequestVO loginRequestVO) {
        LoginResponseVO vo = new LoginResponseVO();
        String email = vo.getEmail();
        // 检查是否被锁定
        if (loginLimitUtil.isLocked(email)) {
            long remainingTime = loginLimitUtil.getRemainingLockTime(email);
            throw new LoginException(GlobalStatusCode.ACCOUNT_LOCKED,"Account is locked, please try again in " + remainingTime + " minutes");
        }
        CandidateDTO candidate = candidateService.candidateLogin(LoginConverter.INSTANCE.convertVoToDto(loginRequestVO));
        if (candidate == null ) {
            loginLimitUtil.recordLoginFail(email);
            long remainingAttempts = loginLimitUtil.getRemainingAttempts(email);
            throw new LoginException(GlobalStatusCode.USER_NOT_FOUND,"The email does not exist."+remainingAttempts+" attempts remaining");
        } else if (!Argon2PasswordUtil.verifyPassword(candidate.getPassword(), loginRequestVO.getPassword())) {
            loginLimitUtil.recordLoginFail(email);
            long remainingAttempts = loginLimitUtil.getRemainingAttempts(email);
            throw new LoginException(GlobalStatusCode.PASSWORD_ERROR,"Incorrect password."+remainingAttempts+" attempts remaining");
        }
        Map<String, Object> claims = new HashMap<>();
        claims.put("user", candidate.getCandidateName());
        claims.put("email", candidate.getCandidateEmail());
        claims.put("role", RoleConstants.CANDIDATE_USER_ROLE);
        claims.put("userid",candidate.getId());
        claims.put("phoneNumber",candidate.getPhoneNumber());
        String token = JwtUtils.generateToken(claims);
        vo.setToken(token);
        vo.setUserName(candidate.getCandidateName());
        vo.setEmail(candidate.getCandidateEmail());
        vo.setUserid(candidate.getId());
        vo.setLoginRole(RoleConstants.CANDIDATE_USER_ROLE);
        vo.setPhoneNumber(candidate.getPhoneNumber());
        loginLimitUtil.clearLoginFail(email);
        return vo;
    }

}