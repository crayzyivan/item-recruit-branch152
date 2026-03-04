package com.item;

import com.item.dto.iam.CurrencyToPointsRuleDTO;
import com.item.dto.iam.FeignResponse;
import com.item.dto.iam.IamPointsAddDTO;
import com.item.dto.iam.IamPointsCancelFreezeDTO;
import com.item.dto.iam.IamPointsConfirmFreezeDTO;
import com.item.dto.iam.IamPointsDeductDTO;
import com.item.dto.iam.IamPointsInitAccountDTO;
import com.item.dto.iam.IamPointsTopUpDTO;
import com.item.dto.iam.IamUserContextDTO;
import com.item.dto.iam.IamUserPointsBaseDTO;
import com.item.dto.iam.IamUserPointsDTO;
import com.item.dto.iam.PagedResult;
import com.item.dto.iam.PointRuleDTO;
import com.item.entity.CandidateJobEntity;
import com.item.entity.PointsOperationLog;
import com.item.framework.config.BusinessDeductionPointsConfig;
import com.item.framework.config.MinuteBasedConfig;
import com.item.framework.constant.CommonResponseCode;
import com.item.framework.constant.InterviewMailStatusEnum;
import com.item.framework.constant.PointTypeEnum;
import com.item.framework.constant.TransactionNoTypeEnum;
import com.item.framework.constant.TransactionTypeEnum;
import com.item.framework.constant.UnChangeResponseCode;
import com.item.framework.error.BusinessException;
import com.item.schedule.CancelInterviewFreezeTask;
import com.item.schedule.PointRetryTask;
import com.item.service.BackgroundService;
import com.item.service.CandidateJobDomainService;
import com.item.service.CandidateJobService;
import com.item.service.PointService;
import com.item.service.client.adapter.IamPointsRpcAdapter;
import com.item.util.RedisSerialNumberUtils;
import com.item.util.UserContextUtil;
import static com.item.util.UserContextUtil.getCurrentUser;
import com.item.vo.PointLogVO;
import com.item.vo.PointTopUpVO;
import com.item.vo.PointTransferVO;
import com.item.vo.ResumeDownloadVO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 积分
 *
 * @author liyunlong
 * @version 1.0
 * @since 2025-07-28  09:37
 */
@Slf4j
@SpringBootTest
public class PointTest {
    @Resource
    private IamPointsRpcAdapter iamPointsRpcAdapter;
    @Resource
    private PointService pointService;
    @Resource
    private BusinessDeductionPointsConfig businessDeductionPointsConfig;
    @Resource
    private RedisSerialNumberUtils redisSerialNumberUtils;
    @Resource
    private CancelInterviewFreezeTask cancelInterviewFreezeTask;
    @Resource
    private PointRetryTask pointRetryTask;
    @Resource
    private BackgroundService backgroundService;
    @Resource
    private MinuteBasedConfig minuteBasedConfig;
    @Resource
    private CandidateJobDomainService candidateJobDomainService;
    @Resource
    private CandidateJobService candidateJobService;

    @Value("${recruit.business.exempt-points-code}")
    private String exemptPointsCode;

    /**
     * 初始化
     * 1212L yunlong.li@item.com lyl19930323
     * 1949666613178462210L  r_test@item.com  r_test
     * 1949712138917441537L  r_test@item.com  r_sub_test001
     */
    @Test
    public void initCreditCenterPoints(){
        IamPointsInitAccountDTO iamPointsInitAccountDTO=new IamPointsInitAccountDTO();
        iamPointsInitAccountDTO.setUserId(1953437895435956227L );
        iamPointsInitAccountDTO.setUserEmail("boll.lai01@item.com");
        iamPointsInitAccountDTO.setUserName("bolllai01");
        iamPointsInitAccountDTO.setCreatedBy("system");
        FeignResponse<Object> objectIamResponse = iamPointsRpcAdapter.initCreditCenterPoints(iamPointsInitAccountDTO);
        log.info("-------------------------------------------------initCreditCenterPoints data:{}",objectIamResponse.toString());
    }


    /**
     * 查询用户积分
     * 1949666613178462210L  主
     * 1949712138917441537L 子
     */
    @Test
    public void queryUserPoints(){
        IamUserPointsBaseDTO iamUserPointsBaseDTO=new IamUserPointsBaseDTO();
        iamUserPointsBaseDTO.setUserId(1949712138917441537L);
        IamUserPointsDTO iamUserPointsDTO = iamPointsRpcAdapter.getCreditCenterPoints(iamUserPointsBaseDTO);
        log.info("-------------------------------------------------queryUserPoints data:{}",iamUserPointsDTO);
    }

    /**
     * 转移积分
     */
    @Test
    public void transferCreditCenterPoints(){
//        IamPointsTransferDTO iamPointsTransferDTO=new IamPointsTransferDTO();
//        iamPointsTransferDTO.setFromUserId(1949666613178462210L);
//        iamPointsTransferDTO.setToUserId(1949712138917441537L);
//        iamPointsTransferDTO.setTransactionNo("transfer001");
//        iamPointsTransferDTO.setPoints(100L);
//        iamPointsTransferDTO.setPointType(2);
//        iamPointsTransferDTO.setOperator("system");
//        iamPointsTransferDTO.setRemark("");
//        IamResponse<Object> objectIamResponse = iamPointsRpcAdapter.transferCreditCenterPoints(iamPointsTransferDTO);
//        log.info("-------------------------------------------------transferCreditCenterPoints data:{}",objectIamResponse.toString());

        PointTransferVO vo = new PointTransferVO();
        vo.setFromUserId(1949666613178462210L);
        vo.setToUserId(1949712138917441537L);
        vo.setPoints(100L);
        boolean b = pointService.pointTransfer(vo);
        log.info("-------------------------------------------------pointTransfer data:{}",b);

    }


    /**
     * 充值积分
     */
    @Test
    public void topUpCreditCenterPoints(){
        IamPointsTopUpDTO iamPointsTopUpDTO=new IamPointsTopUpDTO();
        iamPointsTopUpDTO.setUserId(1949666613178462210L);
        iamPointsTopUpDTO.setTransactionNo("p0002");
        iamPointsTopUpDTO.setAmount(new BigDecimal(1));
        iamPointsTopUpDTO.setCustomerCode("RCTX0001");
        iamPointsTopUpDTO.setRemark("");
        iamPointsTopUpDTO.setCurrencyCode("USD");
        FeignResponse<Boolean> booleanIamResponse = iamPointsRpcAdapter.topUpCreditCenterPoints(iamPointsTopUpDTO);
        log.info("-------------------------------------------------topUpCreditCenterPoints data:{}",booleanIamResponse.toString());

    }



    /**
     * 添加积分
     */
    @Test
    public void addCreditCenterPoints(){
        IamPointsAddDTO iamPointsAddDTO=new IamPointsAddDTO();
        iamPointsAddDTO.setUserId(1965018656581844994L);
        iamPointsAddDTO.setTransactionNo("p0001265");
        iamPointsAddDTO.setPoints(100000L);
        iamPointsAddDTO.setPointType(1);//1赠送的积分 2购买积分
        iamPointsAddDTO.setOperator("");
        iamPointsAddDTO.setRemark("");
        FeignResponse<Object> objectIamResponse = iamPointsRpcAdapter.addCreditCenterPoints(iamPointsAddDTO);
        log.info("-------------------------------------------------addCreditCenterPoints data:{}",objectIamResponse.toString());
    }



    /**
     * 冻结积分
     */
    @Test
    public void freezeCreditCenterPoints(){
//        IamPointsFreezeDTO iamPointsFreezeDTO=new IamPointsFreezeDTO();
//        iamPointsFreezeDTO.setUserId(1949666613178462210L);
//        iamPointsFreezeDTO.setTransactionNo("interview003");
//        iamPointsFreezeDTO.setPoints(100);
//        iamPointsFreezeDTO.setExpireHours(1);
//        iamPointsFreezeDTO.setOperator("");
//        iamPointsFreezeDTO.setRemark("");
//        IamResponse<Object> objectIamResponse = iamPointsRpcAdapter.freezeCreditCenterPoints(iamPointsFreezeDTO);
//        log.info("-------------------------------------------------freezeCreditCenterPoints data:{}",objectIamResponse.toString());

    }

    /**
     * 取消冻结积分
     */
    @Test
    public void cancelFreezeCreditCenterPoints(){
        IamPointsCancelFreezeDTO iamPointsCancelFreezeDTO=new IamPointsCancelFreezeDTO();
        iamPointsCancelFreezeDTO.setUserId(1949666613178462210L);
        iamPointsCancelFreezeDTO.setTransactionNo("IM2025073100000002");
        iamPointsCancelFreezeDTO.setOperator("");
        iamPointsCancelFreezeDTO.setCancelRemark("");
        FeignResponse<Object> objectIamResponse = iamPointsRpcAdapter.cancelFreezeCreditCenterPoints(iamPointsCancelFreezeDTO);
        log.info("-------------------------------------------------cancelFreezeCreditCenterPoints data:{}",objectIamResponse.toString());
    }

    /**
     * 确认冻结积分
     */
    @Test
    public void confirmFreezeCreditCenterPoints(){
        IamPointsConfirmFreezeDTO iamPointsConfirmFreezeDTO=new IamPointsConfirmFreezeDTO();
        iamPointsConfirmFreezeDTO.setUserId(1949666613178462210L);
        iamPointsConfirmFreezeDTO.setTransactionNo("interview002");
        iamPointsConfirmFreezeDTO.setConfirmRemark("");
        iamPointsConfirmFreezeDTO.setOperator("");
        FeignResponse<Object> objectIamResponse = iamPointsRpcAdapter.confirmFreezeCreditCenterPoints(iamPointsConfirmFreezeDTO);
        log.info("-------------------------------------------------confirmFreezeCreditCenterPoints data:{}",objectIamResponse.toString());
    }



    /**
     * 扣除积分
     */
    @Test
    public void deductCreditCenterPoints(){
        IamPointsDeductDTO iamPointsDeductDTO=new IamPointsDeductDTO();
        iamPointsDeductDTO.setUserId(1953437895435956227L);
        iamPointsDeductDTO.setTransactionNo("screening0003");
        iamPointsDeductDTO.setPoints(100000000);
        iamPointsDeductDTO.setOperator("");
        iamPointsDeductDTO.setRemark("");
        FeignResponse<Object> objectIamResponse = iamPointsRpcAdapter.deductCreditCenterPoints(iamPointsDeductDTO);
        log.info("-------------------------------------------------deductCreditCenterPoints data:{}",objectIamResponse.toString());

        //发布岗位
        //Boolean flag= pointService.publishJob(1949666613178462210L, 1L,"java");
        //log.info("-------------------------------------------------publishJob data:{}",flag);

    }


    /**
     * 查询积分规则
     */
    @Test
    public void queryPointRulePage(){
        PointRuleDTO pointRuleDTO=new PointRuleDTO();
        pointRuleDTO.setStatus(1);
        pointRuleDTO.setCurrencyCode("USD");
        FeignResponse<PagedResult<CurrencyToPointsRuleDTO>> pagedResultIamResponse = iamPointsRpcAdapter.pointExchangeRulePage(pointRuleDTO);
        if (pagedResultIamResponse.getSuccess()){
            PagedResult<CurrencyToPointsRuleDTO> pointsRuleDTOPagedResult =pagedResultIamResponse.getData();
            log.info("积分规则列表:{}",pointsRuleDTOPagedResult);
        }
    }



    @Test
    public void checkPoints(){
        pointService.checkPointsEnough(1949666613178462210L, businessDeductionPointsConfig.getBackgroundCheck().getDeductedPoints());
    }


    /**
     * 充值
     */
    @Test
    public void topUp(){
        PointTopUpVO vo=new PointTopUpVO();
//        vo.setUserId(1949666613178462210L);
        vo.setAmount(new BigDecimal(1));
//        vo.setCustomerCode("RCTX0001");
        vo.setRemark("充值");
        Boolean b = pointService.pointsTopUp(vo);
        log.info("-------------------------------------------------topUp:{}",b);
    }



    /**
     * 简历下载
     * 1949666613178462210L
     * 1949712138917441537L
     */
    @Test
    public void resumeDownload(){
        //下载简历
        ResumeDownloadVO resumeDownloadVO=new ResumeDownloadVO();
        resumeDownloadVO.setUserId(1949712138917441537L);
        resumeDownloadVO.setResumeUrl("https://www.baidu.com");
        Boolean b = pointService.resumeDownload(resumeDownloadVO);
        log.info("-------------------------------------------------resumeDownloadValidate data:{}",b);
    }


    /**
     * 发送面试邮件
     */
    @Test
    public void sendEmail(){
        //校验面试积分是否充足
        Long userId=1949666613178462210L;
        //发送邮件
        CandidateJobEntity candidateJob=new CandidateJobEntity();
        candidateJob.setCandidateId(1234L);
        candidateJob.setJobId(12L);
        candidateJob.setId(123L);

        //发送邮件积分情况
        PointLogVO emailVo=new PointLogVO();
        //发送面试积分情况
        PointLogVO interviewVo=new PointLogVO();
        emailVo.setUserId(userId);
        interviewVo.setUserId(userId);
        emailVo.setCandidateId(candidateJob.getCandidateId());
        interviewVo.setCandidateId(candidateJob.getCandidateId());
        emailVo.setJobId(candidateJob.getJobId());
        interviewVo.setJobId(candidateJob.getJobId());
        emailVo.setCandidateJobId(candidateJob.getId());
        interviewVo.setCandidateJobId(candidateJob.getId());


        //发送邮件交易号
        emailVo.setTransactionNo(redisSerialNumberUtils.generate(TransactionNoTypeEnum.INTERVIEW_MAIL.getCode()));
        //ai面试交易号
        interviewVo.setTransactionNo(redisSerialNumberUtils.generate(TransactionNoTypeEnum.AI_INTERVIEW.getCode()));
        //发送邮件扣减分数
        emailVo.setPoints(businessDeductionPointsConfig.getAiInterview().getEmailDeductedPoints());
        //ai面试扣减分数
        interviewVo.setPoints(businessDeductionPointsConfig.getAiInterview().getInterviewFreezePoints());
        interviewVo.setPoints(300);
        //交易类型
        emailVo.setTransactionType(TransactionTypeEnum.INTERVIEW_MAIL.getCode());
        interviewVo.setTransactionType(TransactionTypeEnum.AI_INTERVIEW.getCode());
        //冻结发送邮件积分
        if (!pointService.checkPointsFreeze(emailVo)){
            throw new BusinessException(UnChangeResponseCode.INSUFFICIENT_POINTS);
        }
        //冻结ai面试积分
        if (!pointService.checkPointsFreeze(interviewVo)){
            //取消邮件冻结积分
            pointService.cancelPointsFreeze(emailVo);
            throw new BusinessException(UnChangeResponseCode.INSUFFICIENT_POINTS);
        }

        try{
            //int i=1/0;
            candidateJob.setInterviewMailStatus(InterviewMailStatusEnum.SEND.getCode());
        }catch (Exception e){
            //取消邮件冻结积分
            pointService.cancelPointsFreeze(emailVo);
            //取消ai面试积分冻结
            pointService.cancelPointsFreeze(interviewVo);
            throw e;
        }
        //发送邮件成功，则扣除冻结的邮件积分
        if (candidateJob.getInterviewMailStatus().equals(InterviewMailStatusEnum.SEND.getCode())){
            pointService.confirmPointsFreeze(emailVo);
        }
    }

    /**
     * 转移积分
     */
    @Test
    public void pointTransfer(){
        PointTransferVO transferVO=new PointTransferVO();
        transferVO.setFromUserId(1949666613178462210L);
        transferVO.setToUserId(1949712138917441537L);
        transferVO.setPoints(10L);
        transferVO.setRemark("积分转移");
        transferVO.setPointType(PointTypeEnum.PAID.getCode());
        boolean b = pointService.pointTransfer(transferVO);
        log.info("积分转移结果：{}",b);
    }

    /**
     * 面试取消时冻结积分解冻定时任务
     */
    @Test
    public void cancelInterviewFreezeHandler(){
        cancelInterviewFreezeTask.cancelInterviewFreezeTask();
    }


    /**
     * 积分取消占用、确认占用失败重试任务
     */
    @Test
    public void pointRetryTask(){
        pointRetryTask.cancelInterviewFreezeTask();
    }

    @Test
    public void test(){
        IamUserContextDTO currentUser = getCurrentUser();
        log.info("当前用户：{}",currentUser);
        IamUserContextDTO currentUserNeedLogin = UserContextUtil.getCurrentUserRecruitNeedLogin();
        log.info("当前用户：{}",currentUserNeedLogin);
    }


    @Test
    public void test2(){
        backgroundService.startCheck(180L);
    }

    @Test
    public void test111(){
        CandidateJobEntity candidateJob = candidateJobService.getById(361L);
        candidateJobDomainService.processInterviewMail(candidateJob,null,true);
    }
    @Test
    public void test112(){
        int rctx0001 = pointService.getPricingModel("RCTX0001");
        log.info("rctx0001:{}",rctx0001);
    }

    @Test
    public void test113(){
        List<String> exemptList;
        if (StringUtils.isEmpty(exemptPointsCode)) {
            exemptList = Collections.emptyList();
        } else {
            exemptList = Arrays.asList(exemptPointsCode.split(","));
        }
        log.info("list:{}",exemptList);

    }


    @Test
    public void updateJobCheckPoints(){
//        boolean b = pointService.updateJobCheckPoints(109L, 1, 0);
//        log.info("=============:"+b);

        int totalFreePoints = Math.abs(-80) ;
        int extraPoints = new BigDecimal(totalFreePoints)
                .multiply(new BigDecimal(minuteBasedConfig.getInterviewFreezeMinutes()))
                .divide(minuteBasedConfig.getAudioInterviewCostPerMinute(), 0, RoundingMode.HALF_UP)
                .intValue();
        log.info("totalFreePoints:{}",totalFreePoints);

    }



}