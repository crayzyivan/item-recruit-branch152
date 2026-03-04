package com.item.service.impl;

import com.item.convert.AyrShareConverter;
import com.item.dto.AyrShareRequestDTO;
import com.item.dto.ayrshare.AyrShareCreateUserProfileReqDTO;
import com.item.dto.ayrshare.AyrShareCreateUserProfileResDTO;
import com.item.dto.ayrshare.AyrShareGenerateJwtDTO;
import com.item.dto.ayrshare.AyrShareJwtResponseDTO;
import com.item.dto.ayrshare.AyrSharePostDTO;
import com.item.dto.ayrshare.AyrSharePostIdDTO;
import com.item.dto.ayrshare.AyrSharePostResDTO;
import com.item.dto.ayrshare.AyrShareUserResDTO;
import com.item.framework.config.AyrShareConfig;
import com.item.framework.constant.AyrshareStatus;
import static com.item.framework.constant.CommonConstants.NumConstants.TWITTER_LENGTH_LIMIT;
import static com.item.framework.constant.CommonConstants.StrConstants.ERROR;
import static com.item.framework.constant.CommonConstants.StrConstants.INSTAGRAM;
import static com.item.framework.constant.CommonConstants.StrConstants.SUCCESS;
import static com.item.framework.constant.CommonConstants.StrConstants.TWITTER;
import com.item.service.AyrShareService;
import com.item.util.JsonUtils;
import com.item.vo.ayrshare.AyrShareJwtResponseVO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.core5.util.Timeout;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

/**
 * @author : lh
 */
@Slf4j
@SpringBootTest
public class AyrShareServiceTest {
    @Resource
    private AyrShareService ayrShareService;
    @Resource
    private  AyrShareConfig ayrShareConfig;
    @Resource
    private AyrShareConverter ayrShareConverter;


    @Test
    public void generateJwt() {
        AyrShareGenerateJwtDTO ayrShareGenerateJwtDTO = new AyrShareGenerateJwtDTO();
        ayrShareGenerateJwtDTO.setDomain(ayrShareConfig.getDomain());
        ayrShareGenerateJwtDTO.setPrivateKey(ayrShareConfig.getPrivateKey());
        ayrShareGenerateJwtDTO.setProfileKey(ayrShareConfig.getUserProfileKey());
        log.info("ayrShareGenerateJwtDTO {} {} {}" , ayrShareConfig.getDomain(), ayrShareConfig.getUserProfileKey(), ayrShareConfig.getPrivateKey());
        AyrShareJwtResponseDTO ayrShareJwtResponseDTO = ayrShareService.generateJWT(ayrShareGenerateJwtDTO);

        AyrShareJwtResponseVO jwtVO = ayrShareConverter.toJwtVO(ayrShareJwtResponseDTO);

        log.info("jwtVO={}", jwtVO);

    }

    @Test
    public void createUserProfile() {
        AyrShareCreateUserProfileReqDTO ayrShareCreateUserProfileReqDTO = new AyrShareCreateUserProfileReqDTO();
        ayrShareCreateUserProfileReqDTO.setTitle("recruit");
        AyrShareCreateUserProfileResDTO userProfile = ayrShareService.createUserProfile(ayrShareCreateUserProfileReqDTO);
        log.info("userProfile={}", userProfile);
    }

    @Test
    public void getUserProfileDetails(){
        AyrShareUserResDTO userProfileDetails = ayrShareService.getUserProfileDetails();
        log.info("userProfileDetails={}", userProfileDetails);
    }

    @Test
    public void send() throws InterruptedException {
        AyrSharePostDTO ayrSharePostDTO = new AyrSharePostDTO();
        ayrSharePostDTO.setJobId(158L);
        ayrSharePostDTO.setCompanyName("cn_t_r");
        ayrSharePostDTO.setCompanyCode("RDXX0001");
        ayrSharePostDTO.setJobTitle("j_title");
        ayrSharePostDTO.setJobPublishShareCase("{0} t_t {1}. Click {2} t.");
        ayrSharePostDTO.setJobPublishShareTitleCase("{0} t_t {1}.");
        ayrSharePostDTO.setPublishUrl("p_url");
        log.info("ayrSharePost ayrSharePostDTO {}", ayrSharePostDTO);
        // 使用新的CompletableFuture方法，从数据库获取配置信息并更新Job状态
        ayrShareService.asyncSendToAyrShareWithConfig(ayrSharePostDTO);
        Timeout.ofSeconds(30).sleep();
    }

    @Test
    public void testRes(){
        AyrSharePostResDTO ayrSharePostResDTO = new AyrSharePostResDTO();
        ayrSharePostResDTO.setStatus(ERROR);

        boolean allError = ayrShareService.isAllError(null);
        Assertions.assertTrue(allError);

        allError = ayrShareService.isAllError(ayrSharePostResDTO);
        Assertions.assertTrue(allError);

        ayrSharePostResDTO.setStatus(SUCCESS);
        allError = ayrShareService.isAllSuccess(ayrSharePostResDTO);
        Assertions.assertTrue(allError);

        ayrSharePostResDTO.setStatus(ERROR);
        ayrSharePostResDTO.setPostIds(List.of(new AyrSharePostIdDTO()));
        allError =  ayrShareService.isPartialSuccess(ayrSharePostResDTO);

        Assertions.assertTrue(allError);
    }
    @Test
    public void buildDTO(){
        AyrShareRequestDTO build = AyrShareRequestDTO.build(List.of("all"), "https://recruit.com", "subreddit"
                , "companyName", "jobTitle", "{0} is hiring for {1}. Click {2} to apply for the job.", "{0} is hiring for {1}.");
        log.info("ayrShareRequestDTO={}", JsonUtils.toSkipNullJson(build));
        AyrShareRequestDTO build1 = AyrShareRequestDTO.build(List.of("facebook", "reddit", "youtube"), "https://recruit.com", "subreddit"
                , "companyName", "jobTitle", "{0} is hiring for {1}. Click {2} to apply for the job.", "{0} is hiring for {1}.");

        log.info("ayrShareRequestDTO={}", JsonUtils.toSkipNullJson(build1));

        AyrShareRequestDTO build2 = AyrShareRequestDTO.build(List.of("facebook", "reddit"), "https://recruit.com", "subreddit"
                , "companyName", "jobTitle", "{0} is hiring for {1}. Click {2} to apply for the job.", "{0} is hiring for {1}.");

        log.info("ayrShareRequestDTO={}", JsonUtils.toSkipNullJson(build2));

        AyrShareRequestDTO build3 = AyrShareRequestDTO.build(List.of("facebook", "youtube"), "https://recruit.com", "subreddit"
                , "companyName", "jobTitle", "{0} is hiring for {1}. Click {2} to apply for the job.", "{0} is hiring for {1}.");

        log.info("ayrShareRequestDTO={}", JsonUtils.toSkipNullJson(build3));

        AyrShareRequestDTO build4 = AyrShareRequestDTO.build(List.of("facebook"), "https://recruit.com", "subreddit"
                , "companyName", "jobTitle", "{0} is hiring for {1}. Click {2} to apply for the job.", "{0} is hiring for {1}.");

        log.info("ayrShareRequestDTO={}", JsonUtils.toSkipNullJson(build4));

        AyrShareRequestDTO build5 = AyrShareRequestDTO.build(List.of("facebook","youtube"), "https://recruit.com", "subreddit"
                , "companyName", "jobTitle asd asczxc 12312 fghfgh 123 3edfgcb jobTitle asd asczxc 12312 fghfgh 123 3edfgcbjobTitle asd asczxc 12312 fghfgh 123 3edfgcbjobTitle asd asczxc 12312 fghfgh 123 3edfgcbjobTitle asd asczxc 12312 fghfgh 123 3edfgcbjobTitle asd asczxc 12312 fghfgh 123 3edfgcbjobTitle asd asczxc 12312 fghfgh 123 3edfgcbjobTitle asd asczxc 12312 fghfgh 123 3edfgcbjobTitle asd asczxc 12312 fghfgh 123 3edfgcbjobTitle asd asczxc 12312 fghfgh 123 3edfgcbjobTitle asd asczxc 12312 fghfgh 123 3edfgcbjobTitle asd asczxc 12312 fghfgh 123 3edfgcbjobTitle asd asczxc 12312 fghfgh 123 3edfgcbjobTitle asd asczxc 12312 fghfgh 123 3edfgcb", "{0} is hiring for {1}. Click {2} to apply for the job.", "{0} is hiring for {1}.");

        log.info("ayrShareRequestDTO={}", JsonUtils.toSkipNullJson(build5));

        AyrShareRequestDTO build6 = AyrShareRequestDTO.build(List.of("facebook","youtube", "twitter"), "https://recruit.com", "subreddit"
                , "companyName", "jobTitle asd asczxc 12312 fghfgh 123 3edfgcb jobTitle asd asczxc 12312 fghfgh 123 3edfgcbjobTitle asd asczxc 12312 fghfgh 123 3edfgcbjobTitle asd asczxc 12312 fghfgh 123 3edfgcbjobTitle asd asczxc 12312 fghfgh 123 3edfgcbjobTitle asd asczxc 12312 fghfgh 123 3edfgcbjobTitle asd asczxc 12312 fghfgh 123 3edfgcbjobTitle asd asczxc 12312 fghfgh 123 3edfgcbjobTitle asd asczxc 12312 fghfgh 123 3edfgcbjobTitle asd asczxc 12312 fghfgh 123 3edfgcbjobTitle asd asczxc 12312 fghfgh 123 3edfgcbjobTitle asd asczxc 12312 fghfgh 123 3edfgcbjobTitle asd asczxc 12312 fghfgh 123 3edfgcbjobTitle asd asczxc 12312 fghfgh 123 3edfgcb", "{0} is hiring for {1}. Click {2} to apply for the job.", "{0} is hiring for {1}.");

        log.info("ayrShareRequestDTO={}", JsonUtils.toSkipNullJson(build6));
    }

    @Test
    public void buildDTOV1(){
        //
        AyrShareRequestDTO build1 = AyrShareRequestDTO.build(List.of("facebook", "reddit","youtube", "twitter", INSTAGRAM), "https://recruit.com", "subreddit"
                , "companyName", "jobTitle asd asczxc 12312 fghfgh 123 3edfgcb jobTitle asd asczxc 12312 fghfgh 123 3edfgcbjobTitle asd asczxc 12312 fghfgh 123 3edfgcbjobTitle asd asczxc 12312 fghfgh 123 3edfgcbjobTitle asd asczxc 12312 fghfgh 123 3edfgcbjobTitle asd asczxc 12312 fghfgh 123 3edfgcbjobTitle asd asczxc 12312 fghfgh 123 3edfgcbjobTitle asd asczxc 12312 fghfgh 123 3edfgcbjobTitle asd asczxc 12312 fghfgh 123 3edfgcbjobTitle asd asczxc 12312 fghfgh 123 3edfgcbjobTitle asd asczxc 12312 fghfgh 123 3edfgcbjobTitle asd asczxc 12312 fghfgh 123 3edfgcbjobTitle asd asczxc 12312 fghfgh 123 3edfgcbjobTitle asd asczxc 12312 fghfgh 123 3edfgcb", "{0} is hiring for {1}. Click {2} to apply for the job.", "{0} is hiring for {1}.");
        log.info("ayrShareRequestDTO={}", JsonUtils.toSkipNullJson(build1));
        Assertions.assertTrue(build1.getTwitterCustomerOptions().getTwitterPost().length() <= TWITTER_LENGTH_LIMIT);
        AyrShareRequestDTO ayrShareRequestDTO5 = build1.fillMediaUrls("http://ima.png");

        AyrShareRequestDTO ayrShareRequestDTO = build1.convertTwitter();
        Assertions.assertNull(ayrShareRequestDTO.getTwitterCustomerOptions());
        Assertions.assertTrue(ayrShareRequestDTO.getPost().length() <= TWITTER_LENGTH_LIMIT);
        Assertions.assertEquals(1, ayrShareRequestDTO.getPlatforms().size());
        Assertions.assertEquals(TWITTER, ayrShareRequestDTO.getPlatforms().getFirst());
        Assertions.assertNotNull(ayrShareRequestDTO5);
        Assertions.assertEquals("http://ima.png", ayrShareRequestDTO5.getMediaUrls());
        AyrShareRequestDTO ayrShareRequestDTO7 = build1.cleanNeedMediaUrlsPlatform();
        Assertions.assertEquals(4, ayrShareRequestDTO7.getPlatforms().size());
        Assertions.assertEquals(4, build1.getPlatforms().size());
        log.info("ayrShareRequestDTO={}", JsonUtils.toSkipNullJson(ayrShareRequestDTO));

        AyrShareRequestDTO ayrShareRequestDTO1 = build1.convertRemoveTwitter();
        Assertions.assertNotNull(ayrShareRequestDTO1.getPost());
        Assertions.assertNull(ayrShareRequestDTO1.getTwitterCustomerOptions());
        Assertions.assertFalse(ayrShareRequestDTO1.getPlatforms().isEmpty());
        Assertions.assertIterableEquals(List.of("facebook", "reddit","youtube"), ayrShareRequestDTO1.getPlatforms());
        Assertions.assertNotNull(ayrShareRequestDTO1.getRedditOptions());
        Assertions.assertNotNull(ayrShareRequestDTO1.getYouTubeOptions());
        log.info("ayrShareRequestDTO={}", JsonUtils.toSkipNullJson(ayrShareRequestDTO1));


        AyrShareRequestDTO build2 = AyrShareRequestDTO.build(List.of("facebook", "twitter"), "https://recruit.com", "subreddit"
                , "companyName", "jobTitle asd asczxc 12312 fghfgh 123 3edfgcb jobTitle asd asczxc 12312 fghfgh 123 3edfgcbjobTitle asd asczxc 12312 fghfgh 123 3edfgcbjobTitle asd asczxc 12312 fghfgh 123 3edfgcbjobTitle asd asczxc 12312 fghfgh 123 3edfgcbjobTitle asd asczxc 12312 fghfgh 123 3edfgcbjobTitle asd asczxc 12312 fghfgh 123 3edfgcbjobTitle asd asczxc 12312 fghfgh 123 3edfgcbjobTitle asd asczxc 12312 fghfgh 123 3edfgcbjobTitle asd asczxc 12312 fghfgh 123 3edfgcbjobTitle asd asczxc 12312 fghfgh 123 3edfgcbjobTitle asd asczxc 12312 fghfgh 123 3edfgcbjobTitle asd asczxc 12312 fghfgh 123 3edfgcbjobTitle asd asczxc 12312 fghfgh 123 3edfgcb", "{0} is hiring for {1}. Click {2} to apply for the job.", "{0} is hiring for {1}.");
        log.info("ayrShareRequestDTO={}", JsonUtils.toSkipNullJson(build2));
        Assertions.assertTrue(build2.getTwitterCustomerOptions().getTwitterPost().length() <= TWITTER_LENGTH_LIMIT);
        ayrShareRequestDTO5 = build2.fillMediaUrls("http://ima.png");
        Assertions.assertNull(ayrShareRequestDTO5);
        AyrShareRequestDTO ayrShareRequestDTO6 = build2.cleanNeedMediaUrlsPlatform();
        Assertions.assertEquals(2, ayrShareRequestDTO6.getPlatforms().size());
        Assertions.assertEquals(2, build2.getPlatforms().size());
        AyrShareRequestDTO ayrShareRequestDTO2 = build2.convertTwitter();
        Assertions.assertNull(ayrShareRequestDTO2.getTwitterCustomerOptions());
        Assertions.assertTrue(ayrShareRequestDTO2.getPost().length() <= TWITTER_LENGTH_LIMIT);
        Assertions.assertEquals(1, ayrShareRequestDTO2.getPlatforms().size());
        Assertions.assertEquals(TWITTER, ayrShareRequestDTO2.getPlatforms().getFirst());
        log.info("ayrShareRequestDTO={}", JsonUtils.toSkipNullJson(ayrShareRequestDTO2));

        AyrShareRequestDTO ayrShareRequestDTO21 = build2.convertRemoveTwitter();
        Assertions.assertNotNull(ayrShareRequestDTO21.getPost());
        Assertions.assertNull(ayrShareRequestDTO21.getTwitterCustomerOptions());
        Assertions.assertFalse(ayrShareRequestDTO21.getPlatforms().isEmpty());
        Assertions.assertIterableEquals(List.of("facebook"), ayrShareRequestDTO21.getPlatforms());
        Assertions.assertNull(ayrShareRequestDTO21.getRedditOptions());
        Assertions.assertNull(ayrShareRequestDTO21.getYouTubeOptions());
        log.info("ayrShareRequestDTO={}", JsonUtils.toSkipNullJson(ayrShareRequestDTO21));



        AyrShareRequestDTO build3 = AyrShareRequestDTO.build(List.of("twitter"), "https://recruit.com", "subreddit"
                , "companyName", "jobTitle asd asczxc 12312 fghfgh 123 3edfgcb jobTitle asd asczxc 12312 fghfgh 123 3edfgcbjobTitle asd asczxc 12312 fghfgh 123 3edfgcbjobTitle asd asczxc 12312 fghfgh 123 3edfgcbjobTitle asd asczxc 12312 fghfgh 123 3edfgcbjobTitle asd asczxc 12312 fghfgh 123 3edfgcbjobTitle asd asczxc 12312 fghfgh 123 3edfgcbjobTitle asd asczxc 12312 fghfgh 123 3edfgcbjobTitle asd asczxc 12312 fghfgh 123 3edfgcbjobTitle asd asczxc 12312 fghfgh 123 3edfgcbjobTitle asd asczxc 12312 fghfgh 123 3edfgcbjobTitle asd asczxc 12312 fghfgh 123 3edfgcbjobTitle asd asczxc 12312 fghfgh 123 3edfgcbjobTitle asd asczxc 12312 fghfgh 123 3edfgcb", "{0} is hiring for {1}. Click {2} to apply for the job.", "{0} is hiring for {1}.");
        log.info("ayrShareRequestDTO={}", JsonUtils.toSkipNullJson(build3));
        Assertions.assertTrue(build3.getTwitterCustomerOptions().getTwitterPost().length() <= TWITTER_LENGTH_LIMIT);

        AyrShareRequestDTO ayrShareRequestDTO3 = build3.convertTwitter();
        Assertions.assertNull(ayrShareRequestDTO3.getTwitterCustomerOptions());
        Assertions.assertTrue(ayrShareRequestDTO3.getPost().length() <= TWITTER_LENGTH_LIMIT);
        Assertions.assertEquals(1, ayrShareRequestDTO3.getPlatforms().size());
        Assertions.assertEquals(TWITTER, ayrShareRequestDTO3.getPlatforms().getFirst());
        log.info("ayrShareRequestDTO={}", JsonUtils.toSkipNullJson(ayrShareRequestDTO3));

        AyrShareRequestDTO ayrShareRequestDTO31 = build3.convertRemoveTwitter();
        Assertions.assertNotNull(ayrShareRequestDTO31.getPost());
        Assertions.assertNull(ayrShareRequestDTO31.getTwitterCustomerOptions());
        Assertions.assertTrue(ayrShareRequestDTO31.getPlatforms().isEmpty());
        Assertions.assertNull(ayrShareRequestDTO31.getRedditOptions());
        Assertions.assertNull(ayrShareRequestDTO31.getYouTubeOptions());
        log.info("ayrShareRequestDTO={}", JsonUtils.toSkipNullJson(ayrShareRequestDTO31));

        AyrShareRequestDTO build4 = AyrShareRequestDTO.build(List.of("facebook"), "https://recruit.com", "subreddit"
                , "companyName", "jobTitle asd asczxc 12312 fghfgh 123 3edfgcb jobTitle asd asczxc 12312 fghfgh 123 3edfgcbjobTitle asd asczxc 12312 fghfgh 123 3edfgcbjobTitle asd asczxc 12312 fghfgh 123 3edfgcbjobTitle asd asczxc 12312 fghfgh 123 3edfgcbjobTitle asd asczxc 12312 fghfgh 123 3edfgcbjobTitle asd asczxc 12312 fghfgh 123 3edfgcbjobTitle asd asczxc 12312 fghfgh 123 3edfgcbjobTitle asd asczxc 12312 fghfgh 123 3edfgcbjobTitle asd asczxc 12312 fghfgh 123 3edfgcbjobTitle asd asczxc 12312 fghfgh 123 3edfgcbjobTitle asd asczxc 12312 fghfgh 123 3edfgcbjobTitle asd asczxc 12312 fghfgh 123 3edfgcbjobTitle asd asczxc 12312 fghfgh 123 3edfgcb", "{0} is hiring for {1}. Click {2} to apply for the job.", "{0} is hiring for {1}.");
        log.info("ayrShareRequestDTO={}", JsonUtils.toSkipNullJson(build4));
        Assertions.assertNull(build4.getTwitterCustomerOptions());

        AyrShareRequestDTO ayrShareRequestDTO4 = build4.convertTwitter();
        Assertions.assertNull(ayrShareRequestDTO4);
        log.info("ayrShareRequestDTO={}", JsonUtils.toSkipNullJson(ayrShareRequestDTO4));

        AyrShareRequestDTO ayrShareRequestDTO41 = build4.convertRemoveTwitter();
        Assertions.assertNotNull(ayrShareRequestDTO41.getPost());
        Assertions.assertNull(ayrShareRequestDTO41.getTwitterCustomerOptions());
        Assertions.assertFalse(ayrShareRequestDTO41.getPlatforms().isEmpty());
        Assertions.assertNull(ayrShareRequestDTO41.getRedditOptions());
        Assertions.assertNull(ayrShareRequestDTO41.getYouTubeOptions());
        log.info("ayrShareRequestDTO={}", JsonUtils.toSkipNullJson(ayrShareRequestDTO41));
    }

    @Test
    public void resDTO(){
        //部分成功
        AyrSharePostResDTO merge = AyrSharePostResDTO.merge(getAllRes());
        Assertions.assertNotNull(merge);
        Assertions.assertEquals("error", merge.getStatus());
        Assertions.assertFalse(merge.getPostIds().isEmpty());
        Assertions.assertFalse(merge.getErrors().isEmpty());
        Assertions.assertFalse(ayrShareService.isAllError(merge));
        Assertions.assertFalse(ayrShareService.isAllSuccess(merge));
        Assertions.assertTrue(ayrShareService.isPartialSuccess(merge));

        //部分成功
        AyrSharePostResDTO merge1 = AyrSharePostResDTO.merge(getSuccessErrorRes());
        Assertions.assertNotNull(merge1);
        Assertions.assertEquals("error", merge1.getStatus());
        Assertions.assertFalse(merge1.getPostIds().isEmpty());
        Assertions.assertFalse(merge1.getErrors().isEmpty());
        Assertions.assertFalse(ayrShareService.isAllError(merge1));
        Assertions.assertFalse(ayrShareService.isAllSuccess(merge1));
        Assertions.assertTrue(ayrShareService.isPartialSuccess(merge1));

        //部分成功
        AyrSharePostResDTO merge2 = AyrSharePostResDTO.merge(getTwoErrorSuccessRes());
        Assertions.assertNotNull(merge2);
        Assertions.assertEquals("error", merge2.getStatus());
        Assertions.assertFalse(merge2.getPostIds().isEmpty());
        Assertions.assertFalse(merge2.getErrors().isEmpty());
        Assertions.assertFalse(ayrShareService.isAllError(merge2));
        Assertions.assertFalse(ayrShareService.isAllSuccess(merge2));
        Assertions.assertTrue(ayrShareService.isPartialSuccess(merge2));

        //部分成功
        AyrSharePostResDTO merge3 = AyrSharePostResDTO.merge(getErrorSuccessRes());
        Assertions.assertNotNull(merge3);
        Assertions.assertEquals("error", merge3.getStatus());
        Assertions.assertFalse(merge3.getPostIds().isEmpty());
        Assertions.assertFalse(merge3.getErrors().isEmpty());
        Assertions.assertFalse(ayrShareService.isAllError(merge3));
        Assertions.assertFalse(ayrShareService.isAllSuccess(merge3));
        Assertions.assertTrue(ayrShareService.isPartialSuccess(merge3));

        //全部成功
        AyrSharePostResDTO merge4 = AyrSharePostResDTO.merge(getTwoSuccessRes());
        Assertions.assertNotNull(merge4);
        Assertions.assertEquals("success", merge4.getStatus());
        Assertions.assertFalse(merge4.getPostIds().isEmpty());
        Assertions.assertNull(merge4.getErrors());
        Assertions.assertFalse(ayrShareService.isAllError(merge4));
        Assertions.assertTrue(ayrShareService.isAllSuccess(merge4));
        Assertions.assertFalse(ayrShareService.isPartialSuccess(merge4));

        //全部成功
        AyrSharePostResDTO merge5 = AyrSharePostResDTO.merge(getAllSuccessRes());
        Assertions.assertNotNull(merge5);
        Assertions.assertEquals("success", merge5.getStatus());
        Assertions.assertFalse(merge5.getPostIds().isEmpty());
        Assertions.assertNull(merge5.getErrors());
        Assertions.assertFalse(ayrShareService.isAllError(merge5));
        Assertions.assertTrue(ayrShareService.isAllSuccess(merge5));
        Assertions.assertFalse(ayrShareService.isPartialSuccess(merge5));

        //全部失败
        AyrSharePostResDTO merge6 = AyrSharePostResDTO.merge(getAllErrorRes());
        Assertions.assertNotNull(merge6);
        Assertions.assertEquals("error", merge6.getStatus());
        Assertions.assertNull(merge6.getPostIds());
        Assertions.assertFalse(merge6.getErrors().isEmpty());
        Assertions.assertTrue(ayrShareService.isAllError(merge6));
        Assertions.assertFalse(ayrShareService.isAllSuccess(merge6));
        Assertions.assertFalse(ayrShareService.isPartialSuccess(merge6));

        //全部失败
        AyrSharePostResDTO merge7 = AyrSharePostResDTO.merge(getTwoErrorRes());
        Assertions.assertNotNull(merge7);
        Assertions.assertEquals("error", merge7.getStatus());
        Assertions.assertNull(merge7.getPostIds());
        Assertions.assertFalse(merge7.getErrors().isEmpty());
        Assertions.assertTrue(ayrShareService.isAllError(merge7));
        Assertions.assertFalse(ayrShareService.isAllSuccess(merge7));
        Assertions.assertFalse(ayrShareService.isPartialSuccess(merge7));
    }

    @Test
    void testProfile() {
        List<AyrSharePostResDTO> profileAllError = getProfileAllError();
        AyrshareStatus ayrshareStatus = AyrSharePostResDTO.checkResponse(profileAllError);
        Assertions.assertEquals(ayrshareStatus.getCode(), AyrshareStatus.SHARE_FAILED.getCode());

        profileAllError = getProfilePSuccess();
        ayrshareStatus = AyrSharePostResDTO.checkResponse(profileAllError);
        Assertions.assertEquals(ayrshareStatus.getCode(), AyrshareStatus.PARTIAL_SUCCESS.getCode());

        profileAllError = getProfileAllSuccess();
        ayrshareStatus = AyrSharePostResDTO.checkResponse(profileAllError);
        Assertions.assertEquals(ayrshareStatus.getCode(), AyrshareStatus.SHARE_SUCCESS.getCode());

    }
    public List<AyrSharePostResDTO> getProfileAllError(){
        String error = """
                {
                	"status": "error",
                	"posts": [{
                		"status": "error",
                		"errors": [{
                			"action": "post",
                			"status": "error",
                			"code": 161,
                			"message": "Facebook/Instagram authorization error. This can occur for Meta security reasons or incorrect permissions. Try unlinking and relinking Facebook/Instagram and granting all permissions. https://www.ayrshare.com/docs/help-center/technical-support/facebook_or_instagram_unlinked",
                			"resolution": {
                				"relink": true,
                				"platform": "facebook"
                			},
                			"details": "Error validating access token: Sessions for the user are not allowed because the user is not a confirmed user.",
                			"mediaUrl": "https://recruit-employee-staging.item.com/third/ins-image.png",
                			"platform": "instagram"
                		}],
                		"postIds": [{
                			"status": "success",
                			"id": "922745411165549573",
                			"postUrl": "https://www.pinterest.com/pin/922745411165549573/",
                			"platform": "pinterest"
                		}],
                		"id": "qbIGUWcNat2E0hgS97lI",
                		"refId": "abe354a9974ccc92e57f7e078b1d13ade0d4ed11",
                		"profileTitle": "item001",
                		"post": "item001 is hiring for chef992. Click https://recruit-employee-staging.item.com/job-details/87595ybpr/item001-chef992 to apply for the job."
                	}],
                	"validate": true
                }
                """;
        String success= """
                {
                	"status": "success",
                	"posts": [{
                		"status": "success",
                		"errors": [],
                		"postIds": [{
                			"status": "success",
                			"id": "1ncdhya",
                			"postUrl": "https://www.reddit.com/r/recruit/comments/1ncdhya/item001_is_hiring_for_chef992/",
                			"platform": "reddit"
                		}],
                		"id": "bN5KsBJDL1yq85inHdaq",
                		"refId": "abe354a9974ccc92e57f7e078b1d13ade0d4ed11",
                		"profileTitle": "item001",
                		"post": "item001 is hiring for chef992. Click https://recruit-employee-staging.item.com/job-details/87595ybpr/item001-chef992 to apply for the job."
                	}],
                	"validate": true
                }
                """;
        AyrSharePostResDTO allErrorDTO = JsonUtils.toObject(error, AyrSharePostResDTO.class);
        AyrSharePostResDTO allSuccessDTO = JsonUtils.toObject(success, AyrSharePostResDTO.class);
        return List.of(allErrorDTO, allErrorDTO);
    }

    public List<AyrSharePostResDTO> getProfilePSuccess(){
        String error = """
                {
                	"status": "error",
                	"posts": [{
                		"status": "error",
                		"errors": [{
                			"action": "post",
                			"status": "error",
                			"code": 161,
                			"message": "Facebook/Instagram authorization error. This can occur for Meta security reasons or incorrect permissions. Try unlinking and relinking Facebook/Instagram and granting all permissions. https://www.ayrshare.com/docs/help-center/technical-support/facebook_or_instagram_unlinked",
                			"resolution": {
                				"relink": true,
                				"platform": "facebook"
                			},
                			"details": "Error validating access token: Sessions for the user are not allowed because the user is not a confirmed user.",
                			"mediaUrl": "https://recruit-employee-staging.item.com/third/ins-image.png",
                			"platform": "instagram"
                		}],
                		"postIds": [{
                			"status": "success",
                			"id": "922745411165549573",
                			"postUrl": "https://www.pinterest.com/pin/922745411165549573/",
                			"platform": "pinterest"
                		}],
                		"id": "qbIGUWcNat2E0hgS97lI",
                		"refId": "abe354a9974ccc92e57f7e078b1d13ade0d4ed11",
                		"profileTitle": "item001",
                		"post": "item001 is hiring for chef992. Click https://recruit-employee-staging.item.com/job-details/87595ybpr/item001-chef992 to apply for the job."
                	}],
                	"validate": true
                }
                """;
        String success= """
                {
                	"status": "success",
                	"posts": [{
                		"status": "success",
                		"errors": [],
                		"postIds": [{
                			"status": "success",
                			"id": "1ncdhya",
                			"postUrl": "https://www.reddit.com/r/recruit/comments/1ncdhya/item001_is_hiring_for_chef992/",
                			"platform": "reddit"
                		}],
                		"id": "bN5KsBJDL1yq85inHdaq",
                		"refId": "abe354a9974ccc92e57f7e078b1d13ade0d4ed11",
                		"profileTitle": "item001",
                		"post": "item001 is hiring for chef992. Click https://recruit-employee-staging.item.com/job-details/87595ybpr/item001-chef992 to apply for the job."
                	}],
                	"validate": true
                }
                """;
        AyrSharePostResDTO allErrorDTO = JsonUtils.toObject(error, AyrSharePostResDTO.class);
        AyrSharePostResDTO allSuccessDTO = JsonUtils.toObject(success, AyrSharePostResDTO.class);
        return List.of(allErrorDTO, allSuccessDTO);
    }

    public List<AyrSharePostResDTO> getProfileAllSuccess(){
        String success0 = """
                {
                 	"status": "success",
                 	"posts": [{
                 		"status": "success",
                 		"errors": [],
                 		"postIds": [{
                 			"status": "success",
                 			"id": "urn:li:share:7371109798311956480",
                 			"postUrl": "https://www.linkedin.com/feed/update/urn:li:share:7371109798311956480",
                 			"owner": "urn:li:person:rw25J9wn-n",
                 			"linkPreviewFailed": true,
                 			"platform": "linkedin"
                 		}, {
                 			"status": "success",
                 			"id": "1nce3ql",
                 			"postUrl": "https://www.reddit.com/r/recruit/comments/1nce3ql/item001_is_hiring_for_chef993/",
                 			"platform": "reddit"
                 		}, {
                 			"status": "success",
                 			"id": "at://did:plc:e5xoxh5wdes6bdr5mynpm2bb/app.bsky.feed.post/3lyff7b4gge2l",
                 			"cid": "bafyreiflkprjgpmcxjejfbxinvvjuoyd6nyifv6tda2kquwjkbwpsjn2la",
                 			"postUrl": "https://bsky.app/profile/l-eeyan.bsky.social/post/3lyff7b4gge2l",
                 			"platform": "bluesky"
                 		}],
                 		"id": "pymL5LdopFb4WzcVgyc9",
                 		"refId": "abe354a9974ccc92e57f7e078b1d13ade0d4ed11",
                 		"profileTitle": "item001",
                 		"post": "item001 is hiring for chef993. Click https://recruit-employee-staging.item.com/job-details/4ffb5ybpp/item001-chef993 to apply for the job."
                 	}],
                 	"validate": true
                 }
                """;
        String success1 = """
                {
                 	"status": "success",
                 	"posts": [{
                 		"status": "success",
                 		"errors": [],
                 		"postIds": [{
                 			"status": "success",
                 			"id": "1965344102955757719",
                 			"postUrl": "https://twitter.com/hhhccc19572788/status/1965344102955757719",
                 			"platform": "twitter"
                 		}],
                 		"id": "4Bw9JG7NdpryBAy9Of4g",
                 		"refId": "abe354a9974ccc92e57f7e078b1d13ade0d4ed11",
                 		"profileTitle": "item001",
                 		"post": "item001 is hiring for chef993.https://recruit-employee-staging.item.com/job-details/4ffb5ybpp/item001-chef993"
                 	}],
                 	"validate": true
                 }
                """;
        String success2 = """
                {
                  	"status": "success",
                  	"posts": [{
                  		"status": "success",
                  		"errors": [],
                  		"postIds": [{
                  			"status": "success",
                  			"id": "18028383995705839",
                  			"postUrl": "https://www.instagram.com/p/DOYE4Fuj-31/",
                  			"usedQuota": 1,
                  			"platform": "instagram"
                  		}, {
                  			"status": "success",
                  			"id": "922745411165550181",
                  			"postUrl": "https://www.pinterest.com/pin/922745411165550181/",
                  			"platform": "pinterest"
                  		}],
                  		"id": "eubeI9xLStqP7SwKQ0gg",
                  		"refId": "abe354a9974ccc92e57f7e078b1d13ade0d4ed11",
                  		"profileTitle": "item001",
                  		"post": "item001 is hiring for chef993. Click https://recruit-employee-staging.item.com/job-details/4ffb5ybpp/item001-chef993 to apply for the job."
                  	}],
                  	"validate": true
                  }
                """;
        AyrSharePostResDTO s0 = JsonUtils.toObject(success0, AyrSharePostResDTO.class);
        AyrSharePostResDTO s1 = JsonUtils.toObject(success1, AyrSharePostResDTO.class);
        AyrSharePostResDTO s2 = JsonUtils.toObject(success2, AyrSharePostResDTO.class);
        return List.of(s0, s1, s2);
    }

    private List<AyrSharePostResDTO> getAllRes() {
        String allError = """
                {
                    "status": "error",
                    "errors": [
                        {
                            "action": "post",
                            "status": "error",
                            "code": 156,
                            "message": "Linkedin is not linked. Please confirm the linkage on the Social Accounts page in the dashboard. https://www.ayrshare.com/docs/dashboard/connect-social-accounts/overview",\s
                            "platform": "linkedin"
                        },
                        {
                            "action": "post",
                            "status": "error",
                            "code": 156,
                            "message": "Pinterest is not linked. Please confirm the linkage on the Social Accounts page in the dashboard. https://www.ayrshare.com/docs/dashboard/connect-social-accounts/overview",\s
                            "platform": "pinterest"
                        }
                    ],
                    "postIds": [ ],
                    "id": "eNUtHafBevIr9WgyS84o",
                    "refId": "492efe9b701be6a4e3780dc034468e1114da441a",
                    "post": "java https://recruit-dev.item.pub/candidate/company-test-java-4be45yc4s&type=list",
                    "validate": true
                }
                """;
        String allSuccess = """
                {
                    "status": "success",
                    "errors": [
                
                    ],
                    "postIds": [
                        {
                            "status": "success",
                            "id": "104467885891838_768561116149037",
                            "postUrl": "https://www.facebook.com/104467885891838/posts/768561116149037",
                            "platform": "facebook"
                        }
                    ],
                    "id": "DiMXw9j0aE6dBdO8HBxH",
                    "fbId": "104467885891838_768561116149037",
                    "refId": "492efe9b701be6a4e3780dc034468e1114da441a",
                    "post": "java h-4be45yc4s&type=list",
                    "validate": true
                }
                """;
        String errorSuccess = """
                {
                    "status": "error",
                    "errors": [
                        {
                            "action": "post",
                            "status": "error",
                            "code": 156,
                            "message": "Linkedin is not linked. Please confirm the linkage on the Social Accounts page in the dashboard. https://www.ayrshare.com/docs/dashboard/connect-social-accounts/overview",
                            "platform": "linkedin"
                        },
                        {
                            "action": "post",
                            "status": "error",
                            "code": 156,
                            "message": "Instagram is not linked. Please confirm the linkage on the Social Accounts page in the dashboard. https://www.ayrshare.com/docs/dashboard/connect-social-accounts/overview",
                            "platform": "instagram"
                        }
                    ],
                    "postIds": [
                        {
                            "status": "success",
                            "id": "104467885891838_768548412816974",
                            "postUrl": "https://www.facebook.com/104467885891838/posts/768548412816974",
                            "platform": "facebook"
                        }
                    ],
                    "id": "zm4PKUurHOpsRagC9ZB3",
                    "fbId": "104467885891838_768548412816974",
                    "refId": "492efe9b701be6a4e3780dc034468e1114da441a",
                    "post": "java -4be45yc4s&type=list",
                    "validate": true
                }
                """;

        AyrSharePostResDTO allErrorDTO = JsonUtils.toObject(allError, AyrSharePostResDTO.class);
        AyrSharePostResDTO allSuccessDTO = JsonUtils.toObject(allSuccess, AyrSharePostResDTO.class);
        AyrSharePostResDTO errorSuccessDTO = JsonUtils.toObject(errorSuccess, AyrSharePostResDTO.class);
        return List.of(allErrorDTO, allSuccessDTO, errorSuccessDTO);
    }


    private List<AyrSharePostResDTO> getSuccessErrorRes() {
        String allError = """
                {
                    "status": "error",
                    "errors": [
                        {
                            "action": "post",
                            "status": "error",
                            "code": 156,
                            "message": "Linkedin is not linked. Please confirm the linkage on the Social Accounts page in the dashboard. https://www.ayrshare.com/docs/dashboard/connect-social-accounts/overview",\s
                            "platform": "linkedin"
                        },
                        {
                            "action": "post",
                            "status": "error",
                            "code": 156,
                            "message": "Pinterest is not linked. Please confirm the linkage on the Social Accounts page in the dashboard. https://www.ayrshare.com/docs/dashboard/connect-social-accounts/overview",\s
                            "platform": "pinterest"
                        }
                    ],
                    "postIds": [ ],
                    "id": "eNUtHafBevIr9WgyS84o",
                    "refId": "492efe9b701be6a4e3780dc034468e1114da441a",
                    "post": "java https://recruit-dev.item.pub/candidate/company-test-java-4be45yc4s&type=list",
                    "validate": true
                }
                """;
        String allSuccess = """
                {
                    "status": "success",
                    "errors": [
                
                    ],
                    "postIds": [
                        {
                            "status": "success",
                            "id": "104467885891838_768561116149037",
                            "postUrl": "https://www.facebook.com/104467885891838/posts/768561116149037",
                            "platform": "facebook"
                        }
                    ],
                    "id": "DiMXw9j0aE6dBdO8HBxH",
                    "fbId": "104467885891838_768561116149037",
                    "refId": "492efe9b701be6a4e3780dc034468e1114da441a",
                    "post": "java h-4be45yc4s&type=list",
                    "validate": true
                }
                """;

        AyrSharePostResDTO allErrorDTO = JsonUtils.toObject(allError, AyrSharePostResDTO.class);
        AyrSharePostResDTO allSuccessDTO = JsonUtils.toObject(allSuccess, AyrSharePostResDTO.class);
        return List.of(allErrorDTO, allSuccessDTO);
    }

    private List<AyrSharePostResDTO> getTwoErrorSuccessRes() {
        String errorSuccess = """
                {
                    "status": "error",
                    "errors": [
                        {
                            "action": "post",
                            "status": "error",
                            "code": 156,
                            "message": "Linkedin is not linked. Please confirm the linkage on the Social Accounts page in the dashboard. https://www.ayrshare.com/docs/dashboard/connect-social-accounts/overview",
                            "platform": "linkedin"
                        },
                        {
                            "action": "post",
                            "status": "error",
                            "code": 156,
                            "message": "Instagram is not linked. Please confirm the linkage on the Social Accounts page in the dashboard. https://www.ayrshare.com/docs/dashboard/connect-social-accounts/overview",
                            "platform": "instagram"
                        }
                    ],
                    "postIds": [
                        {
                            "status": "success",
                            "id": "104467885891838_768548412816974",
                            "postUrl": "https://www.facebook.com/104467885891838/posts/768548412816974",
                            "platform": "facebook"
                        }
                    ],
                    "id": "zm4PKUurHOpsRagC9ZB3",
                    "fbId": "104467885891838_768548412816974",
                    "refId": "492efe9b701be6a4e3780dc034468e1114da441a",
                    "post": "java -4be45yc4s&type=list",
                    "validate": true
                }
                """;

        AyrSharePostResDTO errorSuccessDTO = JsonUtils.toObject(errorSuccess, AyrSharePostResDTO.class);
        return List.of(errorSuccessDTO, errorSuccessDTO);
    }

    private List<AyrSharePostResDTO> getErrorSuccessRes() {
        String errorSuccess = """
                {
                    "status": "error",
                    "errors": [
                        {
                            "action": "post",
                            "status": "error",
                            "code": 156,
                            "message": "Linkedin is not linked. Please confirm the linkage on the Social Accounts page in the dashboard. https://www.ayrshare.com/docs/dashboard/connect-social-accounts/overview",
                            "platform": "linkedin"
                        },
                        {
                            "action": "post",
                            "status": "error",
                            "code": 156,
                            "message": "Instagram is not linked. Please confirm the linkage on the Social Accounts page in the dashboard. https://www.ayrshare.com/docs/dashboard/connect-social-accounts/overview",
                            "platform": "instagram"
                        }
                    ],
                    "postIds": [
                        {
                            "status": "success",
                            "id": "104467885891838_768548412816974",
                            "postUrl": "https://www.facebook.com/104467885891838/posts/768548412816974",
                            "platform": "facebook"
                        }
                    ],
                    "id": "zm4PKUurHOpsRagC9ZB3",
                    "fbId": "104467885891838_768548412816974",
                    "refId": "492efe9b701be6a4e3780dc034468e1114da441a",
                    "post": "java -4be45yc4s&type=list",
                    "validate": true
                }
                """;

        AyrSharePostResDTO errorSuccessDTO = JsonUtils.toObject(errorSuccess, AyrSharePostResDTO.class);
        return List.of(errorSuccessDTO);
    }

    private List<AyrSharePostResDTO> getTwoSuccessRes() {
        String allSuccess = """
                {
                    "status": "success",
                    "errors": [
                
                    ],
                    "postIds": [
                        {
                            "status": "success",
                            "id": "104467885891838_768561116149037",
                            "postUrl": "https://www.facebook.com/104467885891838/posts/768561116149037",
                            "platform": "facebook"
                        }
                    ],
                    "id": "DiMXw9j0aE6dBdO8HBxH",
                    "fbId": "104467885891838_768561116149037",
                    "refId": "492efe9b701be6a4e3780dc034468e1114da441a",
                    "post": "java h-4be45yc4s&type=list",
                    "validate": true
                }
                """;


        AyrSharePostResDTO allSuccessDTO = JsonUtils.toObject(allSuccess, AyrSharePostResDTO.class);
        return List.of(allSuccessDTO, allSuccessDTO);
    }
    private List<AyrSharePostResDTO> getAllSuccessRes() {
        String allSuccess = """
                {
                    "status": "success",
                    "errors": [
                
                    ],
                    "postIds": [
                        {
                            "status": "success",
                            "id": "104467885891838_768561116149037",
                            "postUrl": "https://www.facebook.com/104467885891838/posts/768561116149037",
                            "platform": "facebook"
                        }
                    ],
                    "id": "DiMXw9j0aE6dBdO8HBxH",
                    "fbId": "104467885891838_768561116149037",
                    "refId": "492efe9b701be6a4e3780dc034468e1114da441a",
                    "post": "java h-4be45yc4s&type=list",
                    "validate": true
                }
                """;


        AyrSharePostResDTO allSuccessDTO = JsonUtils.toObject(allSuccess, AyrSharePostResDTO.class);
        return List.of(allSuccessDTO);
    }

    private List<AyrSharePostResDTO> getAllErrorRes() {
        String allError = """
                {
                    "status": "error",
                    "errors": [
                        {
                            "action": "post",
                            "status": "error",
                            "code": 156,
                            "message": "Linkedin is not linked. Please confirm the linkage on the Social Accounts page in the dashboard. https://www.ayrshare.com/docs/dashboard/connect-social-accounts/overview",\s
                            "platform": "linkedin"
                        },
                        {
                            "action": "post",
                            "status": "error",
                            "code": 156,
                            "message": "Pinterest is not linked. Please confirm the linkage on the Social Accounts page in the dashboard. https://www.ayrshare.com/docs/dashboard/connect-social-accounts/overview",\s
                            "platform": "pinterest"
                        }
                    ],
                    "postIds": [ ],
                    "id": "eNUtHafBevIr9WgyS84o",
                    "refId": "492efe9b701be6a4e3780dc034468e1114da441a",
                    "post": "java https://recruit-dev.item.pub/candidate/company-test-java-4be45yc4s&type=list",
                    "validate": true
                }
                """;

        AyrSharePostResDTO allErrorDTO = JsonUtils.toObject(allError, AyrSharePostResDTO.class);
        return List.of(allErrorDTO);
    }

    private List<AyrSharePostResDTO> getTwoErrorRes() {
        String allError = """
                {
                    "status": "error",
                    "errors": [
                        {
                            "action": "post",
                            "status": "error",
                            "code": 156,
                            "message": "Linkedin is not linked. Please confirm the linkage on the Social Accounts page in the dashboard. https://www.ayrshare.com/docs/dashboard/connect-social-accounts/overview",\s
                            "platform": "linkedin"
                        },
                        {
                            "action": "post",
                            "status": "error",
                            "code": 156,
                            "message": "Pinterest is not linked. Please confirm the linkage on the Social Accounts page in the dashboard. https://www.ayrshare.com/docs/dashboard/connect-social-accounts/overview",\s
                            "platform": "pinterest"
                        }
                    ],
                    "postIds": [ ],
                    "id": "eNUtHafBevIr9WgyS84o",
                    "refId": "492efe9b701be6a4e3780dc034468e1114da441a",
                    "post": "java https://recruit-dev.item.pub/candidate/company-test-java-4be45yc4s&type=list",
                    "validate": true
                }
                """;

        AyrSharePostResDTO allErrorDTO = JsonUtils.toObject(allError, AyrSharePostResDTO.class);
        return List.of(allErrorDTO, allErrorDTO);
    }

}
