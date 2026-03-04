package com.item.service.impl;

import com.item.framework.config.RecruitCommonNacosConfig;
import com.item.framework.constant.CommonConstants;
import com.item.service.GenerateUrlCodeService;
import com.item.service.ShortIdGenerator;
import com.item.util.CommonUtils;
import com.item.util.Md5SignatureUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/**
 * @author : lh
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GenerateUrlCodeServiceImpl implements GenerateUrlCodeService {
    private final RecruitCommonNacosConfig recruitCommonNacosConfig;
    private final ShortIdGenerator shortIdGenerator;

    @Override
    public String generateUrlCodeByConfig(String jobTitle, String urlCode, String companyName, String replace) {
        log.info("generateUrlCodeByConfig jobTitle {} companyName {} urlCode {} recruitCommonNacosConfig {} replace {}", jobTitle, companyName, urlCode, recruitCommonNacosConfig, replace);
        if (recruitCommonNacosConfig != null && recruitCommonNacosConfig.getUrlCodeStyle() != null && recruitCommonNacosConfig.getUrlCodeStyle() == 1) {
            String title = CommonUtils.cleanInputReplace(jobTitle, replace);
            log.info("generateUrlCodeByConfig urlCodeJoin {}", title);
            return title;
        }
        if (StringUtils.isNotBlank(urlCode)) {
            return urlCode;
        }
        String companyNameClean = CommonUtils.cleanInputReplace(companyName, replace);
        String title = CommonUtils.cleanInputReplace(jobTitle, replace);
        String urlCodeJoin = CommonUtils.join(companyNameClean, title);
        log.info("generateUrlCodeByConfig urlCodeJoin {}", urlCodeJoin);
        return urlCodeJoin;
    }

    @Override
    public String generateUrlQuestionLink(Long candidateJobId, String datetime) {
        log.info("generateUrlQuestionLink candidateJobId {} {}", candidateJobId, datetime);
        if (candidateJobId == null || StringUtils.isBlank(datetime)) {
            return "";
        }
        String encrypt = shortIdGenerator.encrypt(candidateJobId, CommonConstants.StrConstants.QUESTION_5S);
        String join = CommonUtils.join(encrypt, datetime);
        String signatureInput = Md5SignatureUtil.md5(join);

        String candidateAnswerQuestion5SRoute = recruitCommonNacosConfig.getCandidateAnswerQuestion5sRoute();
        if (StringUtils.isBlank(candidateAnswerQuestion5SRoute)) {
            return "";
        }
        String linkRoute =candidateAnswerQuestion5SRoute+"?dateTime="+datetime+"&candidateJobId="+encrypt+"&signature="+signatureInput;
        log.info("generateUrlQuestionLink link {}", linkRoute);
        return linkRoute;
    }
}
