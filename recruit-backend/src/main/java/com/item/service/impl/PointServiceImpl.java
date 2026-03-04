package com.item.service.impl;

import com.item.convert.PointOperationLogConverter;
import com.item.dto.iam.CurrencyToPointsRuleDTO;
import com.item.dto.iam.FeignResponse;
import com.item.dto.iam.IamPointsCancelFreezeDTO;
import com.item.dto.iam.IamPointsConfirmFreezeDTO;
import com.item.dto.iam.IamPointsDeductDTO;
import com.item.dto.iam.IamPointsFreezeDTO;
import com.item.dto.iam.IamPointsInitAccountDTO;
import com.item.dto.iam.IamPointsTopUpDTO;
import com.item.dto.iam.IamPointsTransactionDTO;
import com.item.dto.iam.IamPointsTransactionReqDTO;
import com.item.dto.iam.IamPointsTransactionResDTO;
import com.item.dto.iam.IamPointsTransferDTO;
import com.item.dto.iam.IamUserContextDTO;
import com.item.dto.iam.IamUserPointsBaseDTO;
import com.item.dto.iam.IamUserPointsDTO;
import com.item.dto.iam.IamUserPrimaryDTO;
import com.item.dto.iam.PagedResult;
import com.item.dto.iam.PointRuleDTO;
import com.item.dto.iam.SubscriptionTrialDTO;
import com.item.entity.PointsOperationLog;
import com.item.framework.config.BusinessDeductionPointsConfig;
import com.item.framework.config.MinuteBasedConfig;
import com.item.framework.constant.CommonConstants;
import com.item.framework.constant.CommonResponseCode;
import static com.item.framework.constant.CommonResponseCode.POINTS_TOP_UP_FAIL;
import com.item.framework.constant.FreezeActionTypeEnum;
import com.item.framework.constant.InterviewTypeEnum;
import com.item.framework.constant.PointStatusEnum;
import com.item.framework.constant.PricingModelEnum;
import com.item.framework.constant.TransactionNoTypeEnum;
import com.item.framework.constant.TransactionTypeEnum;
import com.item.framework.constant.UnChangeResponseCode;
import com.item.framework.error.BusinessException;
import com.item.framework.http.Pager;
import com.item.service.PointService;
import com.item.service.PointsOperationLogService;
import com.item.service.client.adapter.IamPointsRpcAdapter;
import com.item.util.RedisSerialNumberUtils;
import com.item.util.UserContextUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import java.time.Duration;
import com.item.vo.PointLogVO;
import com.item.vo.PointTopUpVO;
import com.item.vo.PointTransferVO;
import com.item.vo.ResumeDownloadVO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 积分服务实现类
 *
 * @author liyunlong
 * @version 1.0
 * @since 2025-07-29  15:05
 */
@Slf4j
@RefreshScope
@Service
public class PointServiceImpl implements PointService {

    @Resource
    private IamPointsRpcAdapter iamPointsRpcAdapter;
    @Resource
    private RedisSerialNumberUtils redisSerialNumberUtils;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Resource
    private PointsOperationLogService pointsOperationLogService;
    @Resource
    private BusinessDeductionPointsConfig businessDeductionPointsConfig;
    //美元与积分兑换比例
    @Value("${recruit.business.exchange.rate}")
    private int exchangeRate;
    //开关
    @Value("${recruit.business.switch}")
    private boolean businessSwitch;
    //默认计费模式
    @Value("${recruit.business.pricing.model}")
    private int defaulrPricingModel;
    @Value("${recruit.business.exempt-points-code}")
    private String exemptPointsCode;
    @Resource
    private MinuteBasedConfig minuteBasedConfig;

    /**
     * 获取积分计费模式
     * @param companyCode 公司编码
     * @return 积分计费模式
     */
    @Override
    public int getPricingModel(String companyCode) {
        try{
            SubscriptionTrialDTO subscriptionTrial = iamPointsRpcAdapter.getSubscriptionTrial(companyCode);
            if (subscriptionTrial!=null){
                Integer chargeByTime = subscriptionTrial.getChargeByTime();
                log.info("积分计费模式:companyCode{},chargeByTime:{}",companyCode,chargeByTime);
                if(chargeByTime!=null && chargeByTime==0){
                    return PricingModelEnum.MINUTE.getCode();
                }else if(chargeByTime!=null && chargeByTime==1) {
                    return PricingModelEnum.TOKEN.getCode();
                }
            }
        }catch (Exception e){
            log.error("获取积分计费模式异常,companyCode:{},异常信息:",companyCode,e);
            return defaulrPricingModel;
        }
        return defaulrPricingModel;
    }

    /**
     * 积分初始化
     * @param userId 用户id
     * @param email 邮箱
     * @param userName 用户名
     */
    @Override
    public Boolean initPoints(Long userId, String email, String userName) {
        if (!businessSwitch){
            return true;
        }
        IamPointsInitAccountDTO initAccountDTO = new IamPointsInitAccountDTO();
        initAccountDTO.setUserId(userId);
        initAccountDTO.setUserName(userName);
        initAccountDTO.setUserEmail(email);
        FeignResponse<Object> iamResponse = iamPointsRpcAdapter.initCreditCenterPoints(initAccountDTO);
        return iamResponse.getSuccess();
    }

    /**
     * 检查积分余额是否充足
     *
     * @param userId 用户id
     * @param point 预计扣除积分
     */
    @Override
    public Boolean checkPointsEnough(Long userId, Integer point) {
        if (!businessSwitch){
            return true;
        }
        IamUserPointsBaseDTO iamUserPointsBaseDTO=new IamUserPointsBaseDTO();
        iamUserPointsBaseDTO.setUserId(userId);
        IamUserPointsDTO creditCenterPoints = iamPointsRpcAdapter.getCreditCenterPoints(iamUserPointsBaseDTO);
        if (creditCenterPoints==null || point>creditCenterPoints.getAvailablePoints()){
            return false;
        }
        return true;
    }

    /**
     * 检查积分是否充足、并提前冻结积分
     * @param vo
     * @return
     */
    @Override
    public Boolean checkPointsFreeze(PointLogVO vo) {
        log.info("checkPointsFreeze:{}",vo);
        if (!businessSwitch){
            return true;
        }
        if (vo.getUserId()==null || StringUtils.isEmpty(vo.getTransactionNo()) || vo.getPoints()==null || vo.getTransactionType()==null){
            return false;
        }
        boolean success=true;
        //冻结积分
        IamPointsFreezeDTO iamPointsFreezeDTO=new IamPointsFreezeDTO();
        iamPointsFreezeDTO.setUserId(vo.getUserId());
        iamPointsFreezeDTO.setTransactionNo(vo.getTransactionNo());
        iamPointsFreezeDTO.setPoints(vo.getPoints());
        iamPointsFreezeDTO.setRemark(vo.getRemark());

        int negativePoints=-Math.abs(vo.getPoints());
        vo.setPoints(negativePoints);
        try{
            FeignResponse<Object> booleanIamResponse = iamPointsRpcAdapter.freezeCreditCenterPoints(iamPointsFreezeDTO);
            //积分状态
            int pointStatus= PointStatusEnum.FREEZE.getCode();
            String errorMessage="";
            if (!booleanIamResponse.getSuccess()){
                errorMessage=booleanIamResponse.getMsg();
                pointStatus= PointStatusEnum.ERROR.getCode();
                success=false;
            }
            //记录积分操作日志
            PointsOperationLog pointsOperationLog = PointOperationLogConverter.INSTANCE.convertVoToEntity(vo);
            pointsOperationLog.setPointStatus(pointStatus);
            pointsOperationLog.setErrorMessage(errorMessage);
            pointsOperationLog.setUpdateTime(LocalDateTime.now());
            addPointsOperationLog(pointsOperationLog);
        }catch (Exception e){
            log.error("checkPointsFreeze,vo:{},error:", vo,e);
            //记录积分操作日志
            PointsOperationLog pointsOperationLog = PointOperationLogConverter.INSTANCE.convertVoToEntity(vo);
            pointsOperationLog.setPointStatus(PointStatusEnum.ERROR.getCode());
            pointsOperationLog.setErrorMessage(e.getMessage());
            pointsOperationLog.setUpdateTime(LocalDateTime.now());
            addPointsOperationLog(pointsOperationLog);
            return false;
        }
        return success;
    }

    /**
     * 取消积分冻结
     * @param vo 积分情况
     */
    @Override
    public Boolean cancelPointsFreeze(PointLogVO vo) {
        log.info("cancelPointsFreeze:{}",vo);
        if (!businessSwitch){
            return true;
        }
        if (vo.getUserId()==null || StringUtils.isEmpty(vo.getTransactionNo()) || vo.getTransactionType()==null){
            return false;
        }
        //查询冻结的积分操作记录
        PointsOperationLog pointsOperationLog = pointsOperationLogService.selectFreezeLogByNo(vo.getUserId(),vo.getTransactionNo(), vo.getTransactionType());
        return cancelPointsFreezeByLog(pointsOperationLog);
    }

    /**
     * 确认积分冻结
     * @param vo 积分情况
     */
    @Override
    public Boolean confirmPointsFreeze(PointLogVO vo) {
        log.info("confirmPointsFreeze:{}",vo);
        if (!businessSwitch){
            return true;
        }
        if (vo.getUserId()==null || StringUtils.isEmpty(vo.getTransactionNo()) || vo.getTransactionType()==null){
            return false;
        }
        //查询冻结的交易号
        PointsOperationLog pointsOperationLog = pointsOperationLogService.selectFreezeLogByNo(vo.getUserId(),vo.getTransactionNo(), vo.getTransactionType());
        return confirmPointsFreezeByLog(pointsOperationLog);
    }


    /**
     * 取消积分冻结
     * @param pointsOperationLog  积分记录
     * @return
     */
    @Override
    public Boolean cancelPointsFreezeByLog(PointsOperationLog pointsOperationLog) {
        log.info("cancelPointsFreezeByLog:{}",pointsOperationLog);
        if (!businessSwitch){
            return true;
        }
        if (pointsOperationLog!=null){
            //取消冻结的积分
            IamPointsCancelFreezeDTO cancelFreezeDTO = new IamPointsCancelFreezeDTO();
            cancelFreezeDTO.setUserId(pointsOperationLog.getUserId());
            cancelFreezeDTO.setTransactionNo(pointsOperationLog.getTransactionNo());
            FeignResponse<Object> objectIamResponse = iamPointsRpcAdapter.cancelFreezeCreditCenterPoints(cancelFreezeDTO);

            if (objectIamResponse.getSuccess()){
                //取消冻结成功 状态变更为已取消
                pointsOperationLog.setPointStatus(PointStatusEnum.CANCELED.getCode());
                pointsOperationLog.setErrorMessage(null);
                pointsOperationLog.setUpdateTime(LocalDateTime.now());
                pointsOperationLogService.updateById(pointsOperationLog);
                return true;
            }else{
                //取消冻结失败 记录失败原因
                if (pointsOperationLog.getRetryCount()==null){
                    pointsOperationLog.setRetryCount(1);
                }else{
                    pointsOperationLog.setRetryCount(pointsOperationLog.getRetryCount()+1);
                }
                pointsOperationLog.setFreezeActionType(FreezeActionTypeEnum.CAMCEL.getCode());
                pointsOperationLog.setErrorMessage(objectIamResponse.getMsg());
                pointsOperationLog.setUpdateTime(LocalDateTime.now());
                pointsOperationLogService.updateById(pointsOperationLog);
                return false;
            }
        }
        return false;
    }

    /**
     * 确认积分冻结
     * @param pointsOperationLog 积分记录
     * @return
     */
    @Override
    public Boolean confirmPointsFreezeByLog(PointsOperationLog pointsOperationLog) {
        log.info("confirmPointsFreezeByLog:{}",pointsOperationLog);
        if (!businessSwitch){
            return true;
        }
        if (pointsOperationLog!=null){
            //确认消耗的冻结积分
            IamPointsConfirmFreezeDTO confirmFreezeDTO=new IamPointsConfirmFreezeDTO();
            confirmFreezeDTO.setUserId(pointsOperationLog.getUserId());
            confirmFreezeDTO.setTransactionNo(pointsOperationLog.getTransactionNo());
            confirmFreezeDTO.setOperator(String.valueOf(pointsOperationLog.getUserId()));
            confirmFreezeDTO.setConfirmRemark(pointsOperationLog.getRemark());
            FeignResponse<Object> iamResponse = iamPointsRpcAdapter.confirmFreezeCreditCenterPoints(confirmFreezeDTO);
            if (iamResponse.getSuccess()){
                pointsOperationLog.setPointStatus(PointStatusEnum.NORMAL.getCode());
                pointsOperationLog.setUpdateTime(LocalDateTime.now());
                pointsOperationLog.setErrorMessage(null);
                pointsOperationLogService.updateById(pointsOperationLog);
            }else{
                //确认冻结失败 记录失败原因
                if (pointsOperationLog.getRetryCount()==null){
                    pointsOperationLog.setRetryCount(1);
                }else{
                    pointsOperationLog.setRetryCount(pointsOperationLog.getRetryCount()+1);
                }
                pointsOperationLog.setFreezeActionType(FreezeActionTypeEnum.CONFIRM.getCode());
                pointsOperationLog.setUpdateTime(LocalDateTime.now());
                pointsOperationLog.setErrorMessage(iamResponse.getMsg());
                pointsOperationLogService.updateById(pointsOperationLog);
                return false;
            }
        }
        return false;
    }

    /**
     * 查询积分兑换规则
     * @param currencyCode 币种
     * @return 积分兑换规则
     */
    @Override
    public CurrencyToPointsRuleDTO queryCurrencyToPointsRule(String currencyCode) {
        PointRuleDTO pointRuleDTO=new PointRuleDTO();
        pointRuleDTO.setStatus(1);//有效
        pointRuleDTO.setCurrencyCode(currencyCode);
        FeignResponse<PagedResult<CurrencyToPointsRuleDTO>> pagedResultIamResponse = iamPointsRpcAdapter.pointExchangeRulePage(pointRuleDTO);
        if (pagedResultIamResponse.getSuccess()){
            PagedResult<CurrencyToPointsRuleDTO> pagedResult = pagedResultIamResponse.getData();
            if (pagedResult!=null){
                List<CurrencyToPointsRuleDTO> list = pagedResultIamResponse.getData().getList();
                if (CollectionUtils.isNotEmpty(list)){
                    return list.getFirst();
                }
            }
        }
        return null;
    }

    /**
     * 积分充值
     * @param vo
     * @return
     */
    public Boolean pointsTopUp(PointTopUpVO vo){
        if (!businessSwitch){
            return true;
        }
        if (!topUp(vo)){
            throw new BusinessException(CommonResponseCode.POINTS_RECHARGE_FAILED);
        }
        return true;
    }

    /**
     * 充值 积分记录
     */
    @Override
    public Boolean topUp(PointTopUpVO vo) {
        IamUserContextDTO iamUserContextDTO = UserContextUtil.getCurrentUserRecruitNeedLogin();
        if (iamUserContextDTO.getId() == null || vo.getAmount()==null || StringUtils.isEmpty(iamUserContextDTO.getCompanyCode())){
            return false;
        }
        //执行结果
        Boolean success=true;
        //积分充值
        IamPointsTopUpDTO iamPointsTopUpDTO=new IamPointsTopUpDTO();
        iamPointsTopUpDTO.setUserId(Long.parseLong(iamUserContextDTO.getId()));
        String transactionNo=redisSerialNumberUtils.generate(TransactionNoTypeEnum.TOP_UP.getCode());
        iamPointsTopUpDTO.setTransactionNo(transactionNo);
        iamPointsTopUpDTO.setAmount(vo.getAmount());

        iamPointsTopUpDTO.setRemark(vo.getRemark());
        //目前只有美元
        iamPointsTopUpDTO.setCurrencyCode(CommonConstants.TOP_UP_CURRENCY_CODE);
        iamPointsTopUpDTO.setCompanyCode(iamUserContextDTO.getCompanyCode());
        FeignResponse<Boolean> booleanIamResponse = iamPointsRpcAdapter.topUpCreditCenterPoints(iamPointsTopUpDTO);
        //充值的积分
        Integer moneyToPoints=exchangeRate;
        //查询积分系统配置的兑换比例
        CurrencyToPointsRuleDTO currencyToPointsRuleDTO = queryCurrencyToPointsRule(CommonConstants.TOP_UP_CURRENCY_CODE);
        if (currencyToPointsRuleDTO!=null){
            moneyToPoints=currencyToPointsRuleDTO.getMoneyToPoints();
        }
        int points= vo.getAmount().multiply(new BigDecimal(moneyToPoints)).intValue();
        //积分类型
        int pointStatus= PointStatusEnum.NORMAL.getCode();
        String errorMessage="";
        if (!booleanIamResponse.getSuccess()){
            errorMessage=booleanIamResponse.getMsg();
            pointStatus= PointStatusEnum.ERROR.getCode();
            success=false;
        }
        //记录积分操作日志
        PointsOperationLog operationLog = PointOperationLogConverter.INSTANCE.convertTopUpVoToEntity(vo);
        operationLog.setPoints(points);
        operationLog.setTransactionType(TransactionTypeEnum.TOP_UP.getCode());
        operationLog.setPointStatus(pointStatus);
        operationLog.setTransactionNo(transactionNo);
        operationLog.setErrorMessage(errorMessage);
        addPointsOperationLog(operationLog);
        if (!success) {
            throw BusinessException.of(POINTS_TOP_UP_FAIL.getCode(), errorMessage);
        }
        return success;
    }


    /**
     * 下载简历  校验积分是否足够
     * @param vo
     * @return
     */
    @Override
    public Boolean resumeDownload(ResumeDownloadVO vo) {
        if (!businessSwitch){
            return true;
        }
        //检查积分是否足够
        if (!checkPointsEnough(vo.getUserId(),businessDeductionPointsConfig.getDownloadResume().getDeductedPoints())){
            throw new BusinessException(UnChangeResponseCode.INSUFFICIENT_POINTS);
        }
        //扣减积分
        if (!resumeDownload(vo.getUserId(),vo.getResumeUrl())){
            throw new BusinessException(CommonResponseCode.POINTS_DOWNLOAD_FAILED);
        }
        return true;
    }

    /**
     *  下载简历积分记录
     * @param userId 用户ID
     * @param remark 备注
     * @return
     */
    public Boolean resumeDownload(Long userId, String remark) {
        if (userId==null){
            return false;
        }
        log.info("resumeDownload:userId:{},remark:{}",userId,remark);
        //执行结果
        Boolean success=true;

        IamPointsDeductDTO iamPointsDeductDTO=new IamPointsDeductDTO();
        iamPointsDeductDTO.setUserId(userId);
        String transactionNo=redisSerialNumberUtils.generate(TransactionNoTypeEnum.RESUME_DOWNLOAD.getCode());
        iamPointsDeductDTO.setTransactionNo(transactionNo);
        int points=businessDeductionPointsConfig.getDownloadResume().getDeductedPoints();
        iamPointsDeductDTO.setPoints(points);
        //iamPointsDeductDTO.setRemark(remark);

        int negativePoints=-Math.abs(points);
        //记录积分操作
        PointsOperationLog pointsOperationLog=new PointsOperationLog();
        pointsOperationLog.setUserId(userId);
        pointsOperationLog.setTransactionNo(transactionNo);
        pointsOperationLog.setTransactionType(TransactionTypeEnum.RESUME_DOWNLOAD.getCode());
        pointsOperationLog.setPoints(negativePoints);
        //pointsOperationLog.setRemark(remark);

        try{
            FeignResponse<Object> booleanIamResponse = iamPointsRpcAdapter.deductCreditCenterPoints(iamPointsDeductDTO);
            //积分类型
            int pointStatus= PointStatusEnum.NORMAL.getCode();
            String errorMessage="";
            if (!booleanIamResponse.getSuccess()){
                errorMessage=booleanIamResponse.getMsg();
                pointStatus= PointStatusEnum.ERROR.getCode();
                success=false;
            }
            pointsOperationLog.setPointStatus(pointStatus);
            pointsOperationLog.setErrorMessage(errorMessage);
            addPointsOperationLog(pointsOperationLog);
        }catch (Exception e){
            log.error("resumeDownload error:", e);
            pointsOperationLog.setPointStatus(PointStatusEnum.ERROR.getCode());
            pointsOperationLog.setErrorMessage(e.getMessage());
            addPointsOperationLog(pointsOperationLog);
            return false;
        }
        return success;
    }


    /**
     * 背景检查积分扣除
     * @param userId
     * @param candidateJobId 候选人与职位关系ID
     * @param remark
     * @return
     */
    @Override
    public Boolean baskgroundCheck(Long userId, Long candidateJobId, String remark) {
        if (!businessSwitch){
            return true;
        }
        if (userId==null || candidateJobId==null){
            return false;
        }
        //执行结果
        Boolean success=true;

        IamPointsFreezeDTO iamPointsFreezeDTO=new IamPointsFreezeDTO();
        iamPointsFreezeDTO.setUserId(userId);
        String transactionNo=redisSerialNumberUtils.generate(TransactionNoTypeEnum.BACKGROUND_CHECK.getCode());
        iamPointsFreezeDTO.setTransactionNo(transactionNo);
        int points=businessDeductionPointsConfig.getBackgroundCheck().getDeductedPoints();
        iamPointsFreezeDTO.setPoints(points);
        iamPointsFreezeDTO.setRemark(remark);

        //记录操作记录
        PointsOperationLog operationLog=new PointsOperationLog();
        operationLog.setUserId(userId);
        operationLog.setTransactionNo(transactionNo);
        operationLog.setTransactionType(TransactionTypeEnum.BACKGROUND_CHECK.getCode());
        operationLog.setPoints(-Math.abs(points));
        operationLog.setCandidateJobId(candidateJobId);
        operationLog.setRemark(remark);
        try{
            FeignResponse<Object> booleanIamResponse = iamPointsRpcAdapter.freezeCreditCenterPoints(iamPointsFreezeDTO);
            //积分类型
            int pointStatus= PointStatusEnum.NORMAL.getCode();
            String errorMessage="";
            if (!booleanIamResponse.getSuccess()){
                errorMessage=booleanIamResponse.getMsg();
                pointStatus= PointStatusEnum.ERROR.getCode();
                success=false;
            }
            operationLog.setPointStatus(pointStatus);
            operationLog.setErrorMessage(errorMessage);
            addPointsOperationLog(operationLog);
        }catch (Exception e){
            log.error("baskgroundCheck error:",e);
            operationLog.setPointStatus(PointStatusEnum.ERROR.getCode());
            operationLog.setErrorMessage(e.getMessage());
            addPointsOperationLog(operationLog);
            return false;
        }
        return success;
    }


    /**
     * 积分转移
     * @param vo
     * @return
     */
    @Override
    public Boolean pointTransfer(PointTransferVO vo) {
        if (!businessSwitch){
            return true;
        }
        //校验积分是否充足
//        if (!checkPointsEnough(vo.getFromUserId(),vo.getPoints().intValue())) {
//            throw BusinessException.of(CommonResponseCode.INSUFFICIENT_POINTS);
//        }
        //进行积分转移记录
        Boolean transfer = transfer(vo);
        if (!transfer){
            throw BusinessException.of(CommonResponseCode.POINTS_TRANSFER_FAILED);
        }
        return true;
    }

    /**
     * 积分转移记录
     * @param vo
     * @return
     */
    public Boolean transfer(PointTransferVO vo) {
        if (vo.getFromUserId()==null || vo.getToUserId()==null || vo.getPoints()==null){
            return false;
        }
        //进行积分转移
        IamPointsTransferDTO transferDTO=new IamPointsTransferDTO();
        transferDTO.setFromUserId(vo.getFromUserId());
        transferDTO.setToUserId(vo.getToUserId());
        transferDTO.setPoints(vo.getPoints());
        String transactionNo = redisSerialNumberUtils.generate(TransactionNoTypeEnum.TRANSFER.getCode());
        transferDTO.setTransactionNo(transactionNo);
        transferDTO.setPointType(vo.getPointType());
        transferDTO.setRemark(vo.getRemark());
        FeignResponse<Object> iamResponse = iamPointsRpcAdapter.transferCreditCenterPoints(transferDTO);
        //负积分
        int negativePoints=-Math.abs(vo.getPoints().intValue());

        if (!iamResponse.getSuccess()){
            PointsOperationLog pointsOperationLog=new PointsOperationLog();
            pointsOperationLog.setUserId(vo.getFromUserId());
            pointsOperationLog.setTransactionNo(transactionNo);
            pointsOperationLog.setTransactionType(TransactionTypeEnum.TRANSFER_OUT.getCode());
            pointsOperationLog.setPoints(negativePoints);
            pointsOperationLog.setPointStatus(PointStatusEnum.ERROR.getCode());
            pointsOperationLog.setRemark(vo.getRemark());
            pointsOperationLog.setErrorMessage(iamResponse.getMsg());
            addPointsOperationLog(pointsOperationLog);
            return false;
        }else{
            //转出记录
            PointsOperationLog out=new PointsOperationLog();
            out.setUserId(vo.getFromUserId());
            out.setTransactionNo(transactionNo);
            out.setTransactionType(TransactionTypeEnum.TRANSFER_OUT.getCode());
            out.setPointStatus(PointStatusEnum.NORMAL.getCode());
            out.setPoints(negativePoints);
            out.setRemark(vo.getRemark());
            out.setErrorMessage(iamResponse.getMsg());
            addPointsOperationLog(out);
            //转入记录
            PointsOperationLog inLog=new PointsOperationLog();
            inLog.setUserId(vo.getToUserId());
            inLog.setTransactionNo(transactionNo);
            inLog.setTransactionType(TransactionTypeEnum.TRANSFER_IN.getCode());
            inLog.setPointStatus(PointStatusEnum.NORMAL.getCode());
            inLog.setPoints(vo.getPoints().intValue());
            inLog.setRemark(vo.getRemark());
            inLog.setErrorMessage(iamResponse.getMsg());
            addPointsOperationLog(inLog);
            return true;
        }
    }


    /**
     * 扣除面试冻结积分
     * @param candidateJobId 候选人职位关联
     * @return
     */
    @Override
    public Boolean confirmInterviewFreeze(Long candidateJobId) {
        if (!businessSwitch){
            return true;
        }
        if(candidateJobId==null){
            return false;
        }
        PointsOperationLog pointsOperationLog = pointsOperationLogService.selectinterviewFreezeLog(candidateJobId);
        if (pointsOperationLog!=null){
            PointLogVO pointLogVO=new PointLogVO();
            pointLogVO.setUserId(pointsOperationLog.getUserId());
            pointLogVO.setTransactionNo(pointsOperationLog.getTransactionNo());
            pointLogVO.setTransactionType(pointsOperationLog.getTransactionType());
            return confirmPointsFreeze(pointLogVO);
        }
        return false;
    }

    /**
     * 取消面试冻结积分
     * @param candidateJobId
     * @return
     */
    @Override
    public Boolean cancelInterviewFreeze(Long candidateJobId) {
        if (!businessSwitch){
            return true;
        }
        if(candidateJobId==null){
            return false;
        }
        PointsOperationLog pointsOperationLog = pointsOperationLogService.selectinterviewFreezeLog(candidateJobId);
        if (pointsOperationLog!=null){
            PointLogVO pointLogVO=new PointLogVO();
            pointLogVO.setUserId(pointsOperationLog.getUserId());
            pointLogVO.setTransactionNo(pointsOperationLog.getTransactionNo());
            pointLogVO.setTransactionType(pointsOperationLog.getTransactionType());
            return cancelPointsFreeze(pointLogVO);
        }
        return false;
    }

    @Override
    public Pager<IamPointsTransactionResDTO> pagePointsTransaction(IamPointsTransactionDTO iamPoints) {
        IamPointsTransactionReqDTO iamPointsDTO = PointOperationLogConverter.INSTANCE.convert2ReqDTO(iamPoints);
        IamUserContextDTO currentUserNeedLogin = UserContextUtil.getCurrentUserRecruitNeedLogin();
        iamPointsDTO.setCompanyCode(currentUserNeedLogin.getCompanyCode());
        iamPointsDTO.setAppCode(CommonConstants.StrConstants.APP_CODE);
        iamPointsDTO.setPageNum(iamPoints.getPageIndex());
        PagedResult<IamPointsTransactionResDTO> pagedResult = iamPointsRpcAdapter.pagePointsTransaction(iamPointsDTO);
        if (CollectionUtils.isEmpty(pagedResult.getList()) || pagedResult.getTotalCount() == 0) {
            return Pager.buildEmpty();
        }
        Pager<IamPointsTransactionResDTO> pager = new Pager<>();
        pager.setTotalCount(pagedResult.getTotalCount());
        pager.setPageIndex(pagedResult.getCurrentPage());
        pager.setPageSize(pagedResult.getPageSize());
        pager.setTotalPage(pagedResult.getTotalPage());
        pager.setCurrentPageRecords(pagedResult.getList());
        return pager;
    }

    /**
     * 添加积分操作记录
     * @param pointsOperationLog 用户id
     */
    private void addPointsOperationLog(PointsOperationLog pointsOperationLog){
        pointsOperationLog.setCreateTime(LocalDateTime.now());
        pointsOperationLog.setUpdateTime(LocalDateTime.now());
        IamUserContextDTO iamUserContextDTO = UserContextUtil.getCurrentUser();
        if (iamUserContextDTO!=null && StringUtils.isEmpty(pointsOperationLog.getCompanyCode())){
            pointsOperationLog.setCompanyCode(iamUserContextDTO.getCompanyCode());
        }
        pointsOperationLog.setPricingModel(getPricingModel(pointsOperationLog.getCompanyCode()));
        pointsOperationLogService.save(pointsOperationLog);
    }


    /**
     * 面试积分扣除  适用于 minute模式
     * @param pointLogVO
     * @return
     */
    public Boolean interviewDeduct(PointLogVO pointLogVO) {
        log.info("interviewDeduct:{}",pointLogVO);
        if (!businessSwitch){
            return true;
        }
        //执行结果
        Boolean success=true;

        IamPointsDeductDTO iamPointsDeductDTO=new IamPointsDeductDTO();
        iamPointsDeductDTO.setUserId(pointLogVO.getUserId());
        iamPointsDeductDTO.setTransactionNo(pointLogVO.getTransactionNo());
        iamPointsDeductDTO.setPoints(pointLogVO.getPoints());
        iamPointsDeductDTO.setRemark(pointLogVO.getRemark());


        int negativePoints=-Math.abs(pointLogVO.getPoints());
        //记录积分操作
        PointsOperationLog pointsOperationLog=new PointsOperationLog();
        pointsOperationLog.setUserId(pointLogVO.getUserId());
        pointsOperationLog.setOperUserId(pointLogVO.getOperUserId());
        pointsOperationLog.setTransactionNo(pointLogVO.getTransactionNo());
        pointsOperationLog.setTransactionType(TransactionTypeEnum.AI_INTERVIEW.getCode());
        pointsOperationLog.setPoints(negativePoints);
        pointsOperationLog.setRemark(pointLogVO.getRemark());
        pointsOperationLog.setCandidateJobId(pointLogVO.getCandidateJobId());
        pointsOperationLog.setCandidateId(pointLogVO.getCandidateId());
        pointsOperationLog.setJobId(pointLogVO.getJobId());
        pointsOperationLog.setDurationMinutes(pointLogVO.getDurationMinutes());
        pointsOperationLog.setDurationSeconds(pointLogVO.getDurationSeconds());
        pointsOperationLog.setConsumedTokens(pointLogVO.getConsumedTokens());
        pointsOperationLog.setCompanyCode(pointLogVO.getCompanyCode());
        pointsOperationLog.setUpdateTime(LocalDateTime.now());
        pointsOperationLog.setFreezeInterviewType(pointLogVO.getFreezeInterviewType());
        pointsOperationLog.setActualInterviewType(pointLogVO.getActualInterviewType());

        try{
            FeignResponse<Object> booleanIamResponse = iamPointsRpcAdapter.deductCreditCenterPoints(iamPointsDeductDTO);
            //积分类型
            int pointStatus= PointStatusEnum.NORMAL.getCode();
            String errorMessage="";
            if (!booleanIamResponse.getSuccess()){
                errorMessage=booleanIamResponse.getMsg();
                pointStatus= PointStatusEnum.ERROR.getCode();
                success=false;
            }
            pointsOperationLog.setPointStatus(pointStatus);
            pointsOperationLog.setErrorMessage(errorMessage);
            addPointsOperationLog(pointsOperationLog);
        }catch (Exception e){
            log.error("interviewDeduct:pointLogVO:{},error:",pointLogVO,e);
            pointsOperationLog.setPointStatus(PointStatusEnum.ERROR.getCode());
            pointsOperationLog.setErrorMessage(e.getMessage());
            addPointsOperationLog(pointsOperationLog);
            return false;
        }
        return success;
    }


    /**
     * 积分扣减
     * @param pointLogVO
     * @return
     */
    @Override
    public Boolean deductCreditCenterPoints(PointLogVO pointLogVO) {
        log.info("deductCreditCenterPoints:{}",pointLogVO);
        if (!businessSwitch){
            return true;
        }
        //执行结果
        Boolean success=true;

        IamPointsDeductDTO iamPointsDeductDTO=new IamPointsDeductDTO();
        iamPointsDeductDTO.setUserId(pointLogVO.getUserId());
        iamPointsDeductDTO.setTransactionNo(pointLogVO.getTransactionNo());
        iamPointsDeductDTO.setPoints(pointLogVO.getPoints());
        iamPointsDeductDTO.setRemark(pointLogVO.getRemark());

        //记录积分操作
        int negativePoints=-Math.abs(pointLogVO.getPoints());
        PointsOperationLog pointsOperationLog=new PointsOperationLog();
        pointsOperationLog.setUserId(pointLogVO.getUserId());
        pointsOperationLog.setOperUserId(pointLogVO.getOperUserId());
        pointsOperationLog.setTransactionNo(pointLogVO.getTransactionNo());
        pointsOperationLog.setTransactionType(pointLogVO.getTransactionType());
        pointsOperationLog.setJobId(pointLogVO.getJobId());
        pointsOperationLog.setCandidateId(pointLogVO.getCandidateId());
        pointsOperationLog.setCandidateJobId(pointLogVO.getCandidateJobId());
        pointsOperationLog.setPoints(negativePoints);
        pointsOperationLog.setCompanyCode(pointLogVO.getCompanyCode());
        pointsOperationLog.setRemark(pointLogVO.getRemark());

        try{
            FeignResponse<Object> booleanIamResponse = iamPointsRpcAdapter.deductCreditCenterPoints(iamPointsDeductDTO);
            //积分类型
            int pointStatus= PointStatusEnum.NORMAL.getCode();
            String errorMessage="";
            if (!booleanIamResponse.getSuccess()){
                errorMessage=booleanIamResponse.getMsg();
                pointStatus= PointStatusEnum.ERROR.getCode();
                success=false;
            }
            pointsOperationLog.setPointStatus(pointStatus);
            pointsOperationLog.setErrorMessage(errorMessage);
            addPointsOperationLog(pointsOperationLog);
        }catch (Exception e){
            log.error("deductCreditCenterPoints:pointLogVO:{},error:",pointLogVO,e);
            pointsOperationLog.setPointStatus(PointStatusEnum.ERROR.getCode());
            pointsOperationLog.setErrorMessage(e.getMessage());
            addPointsOperationLog(pointsOperationLog);
            return false;
        }
        return success;
    }

    /**
     * 查询积分
     * @return
     */
    @Override
    public IamUserPointsDTO getCreditCenterPoints() {
        IamUserContextDTO iamUserContextDTO = UserContextUtil.getCurrentUserRecruitNeedLogin();
        IamUserPointsBaseDTO iamUserPointsBaseDTO=new IamUserPointsBaseDTO();
        iamUserPointsBaseDTO.setUserId(Long.parseLong(iamUserContextDTO.getId()));
        try{
            IamUserPointsDTO iamUserPointsDTO = iamPointsRpcAdapter.getCreditCenterPoints(iamUserPointsBaseDTO);
            if(iamUserPointsDTO==null){
                iamUserPointsDTO=new IamUserPointsDTO();
                iamUserPointsDTO.setUserId(iamUserPointsBaseDTO.getUserId());
                iamUserPointsDTO.setAppCode(iamUserPointsBaseDTO.getAppCode());
                iamUserPointsDTO.setTotalPoints(0);
                iamUserPointsDTO.setAvailablePoints(0);
                iamUserPointsDTO.setFrozenPoints(0);
                iamUserPointsDTO.setGiftedPoints(0);
                iamUserPointsDTO.setPaidPoints(0);
            }
            return iamUserPointsDTO;
        } catch (Exception e) {
            log.error("getCreditCenterPoints:iamUserPointsBaseDTO:{},error:",iamUserPointsBaseDTO,e);
            throw new BusinessException(CommonResponseCode.CENTER_POINTS_FAILED);
        }
    }

    /**
     * 是否豁免积分
     * @param companyCode
     * @return
     */
    @Override
    public boolean isExempt(String companyCode) {
        List<String> exemptList;
        if (StringUtils.isEmpty(exemptPointsCode)) {
            exemptList = Collections.emptyList();
        } else {
            exemptList = Arrays.asList(exemptPointsCode.split(","));
        }
        return exemptList.contains(companyCode);
    }
    /**
     * 变更职位时校验积分是否充足
     * @param JobId
     * @return
     */
    @Override
    public boolean updateJobCheckPoints(Long JobId,String companyCode,Integer oldInterviewType,Integer newInterviewType) {
        log.info("updateJobCheckPoints jobId:{},oldInterviewType:{},newInterviewType:{}",JobId,oldInterviewType,newInterviewType);
        if (Objects.equals(oldInterviewType, newInterviewType)){
            return true;
        }
        if (newInterviewType!=null && newInterviewType== InterviewTypeEnum.AUDIO.getCode()){
            return true;
        }
        if (isExempt(companyCode)){
            return true;
        }
        List<PointsOperationLog> pointsOperationLogs = pointsOperationLogService.audioFreezeListByJobId(JobId);
        if (CollectionUtils.isNotEmpty(pointsOperationLogs)) {
            Map<Long,List<PointsOperationLog>> userLogMap=pointsOperationLogs.stream().collect(Collectors.groupingBy(PointsOperationLog::getUserId));
            Set<Long> userIds = userLogMap.keySet();
            for (Long userId : userIds) {
                IamUserPointsBaseDTO iamUserPointsBaseDTO=new IamUserPointsBaseDTO();
                iamUserPointsBaseDTO.setUserId(userId);
                try {
                    IamUserPointsDTO iamUserPointsDTO = iamPointsRpcAdapter.getCreditCenterPoints(iamUserPointsBaseDTO);
                    log.info("updateJobCheckPoints jobId:{},userId:{},iamUserPointsDTO:{}",JobId,userId,iamUserPointsDTO);
                    if (iamUserPointsDTO!=null){
                        //计算额外的积分
                        int totalFreePoints = Math.abs(userLogMap.get(userId).stream().mapToInt(PointsOperationLog::getPoints).sum()) ;
                        log.info("updateJobCheckPoints jobId:{},userId:{},totalFreePoints:{}",JobId,userId,totalFreePoints);
                        int extraPoints = new BigDecimal(totalFreePoints)
                                .multiply(minuteBasedConfig.getInterviewCostPerMinute())
                                .divide(minuteBasedConfig.getAudioInterviewCostPerMinute(), 0, RoundingMode.HALF_UP)
                                .intValue();
                        log.info("updateJobCheckPoints jobId:{},userId:{},extraPoints:{}",JobId,userId,extraPoints);
                        if (extraPoints>iamUserPointsDTO.getAvailablePoints()){
                            log.info("updateJobCheckPoints userId:{}",userId);
                            return false;
                        }
                    }
                }catch (Exception e){
                    log.error("getCreditCenterPoints:iamUserPointsBaseDTO:{},error:",iamUserPointsBaseDTO,e);
                }
            }
        }
        return true;
    }


    @Override
    public Long getUserPrimaryId(String companyCode) {
        // 构造Redis键
        String redisKey = String.format("%s:%s", CommonConstants.REDIS_CACHE_USER_PRIMARY_BY_COMPANY, companyCode);
        // 尝试从Redis获取数据
        String cachedValue = redisTemplate.opsForValue().get(redisKey);
        if (cachedValue != null) {
            try {
                return Long.valueOf(cachedValue);
            } catch (NumberFormatException e) {
                log.error("Redis缓存值格式错误,key:{},value:{}", redisKey, cachedValue);
            }
        }
        // 缓存未命中，从数据库获取
        try {
            log.info("iamUserPrimaryDTO companyCode:{}", companyCode);
            IamUserPrimaryDTO iamUserPrimaryDTO = iamPointsRpcAdapter.queryUserPrimary(companyCode);
            if (iamUserPrimaryDTO != null) {
                log.info("iamUserPrimaryDTO iamUserPrimaryDTO:{}", iamUserPrimaryDTO);
                Long userId = Long.valueOf(iamUserPrimaryDTO.getId());
                // 将结果存入Redis，设置30分钟过期
                redisTemplate.opsForValue().set(redisKey, userId.toString(), Duration.ofMinutes(30));
                return userId;
            }
        } catch (Exception e) {
            log.error("获取租户主账号异常,companyCode:{},异常信息:", companyCode, e);
            return null;
        }
        return null;
    }


}