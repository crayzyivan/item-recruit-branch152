package com.item.service;

import com.item.dto.iam.CurrencyToPointsRuleDTO;
import com.item.dto.iam.IamPointsTransactionDTO;
import com.item.dto.iam.IamPointsTransactionResDTO;
import com.item.dto.iam.IamUserPointsDTO;
import com.item.entity.PointsOperationLog;
import com.item.framework.http.Pager;
import com.item.vo.PointLogListVo;
import com.item.vo.PointLogQueryVo;
import com.item.vo.PointLogVO;
import com.item.vo.PointTopUpVO;
import com.item.vo.PointTransferVO;
import com.item.vo.ResumeDownloadVO;


/**
 * 积分功能接口
 *
 * @author liyunlong
 * @version 1.0
 * @since 2025-07-29  15:05
 */
public interface PointService {

    /**
     * 获取积分计费模式
     * @param companyCode 公司编码
     * @return 积分计费模式
     */
    int getPricingModel(String companyCode);


    //TODO  创建招聘账号时需要进行积分初始化
    /**
     * 积分初始化
     * @param userId 用户id
     * @param email 邮箱
     * @param userName 用户名
     */
    Boolean initPoints(Long userId,String email,String userName);

    /**
     * 检查积分余额是否充足
     *
     * @param userId 用户id
     * @param point 预计扣除积分
     * @return true:余额充足
     */
    Boolean checkPointsEnough(Long userId, Integer point);

    /**
     * 检查积分是否充足、并提前冻结积分
     * @param pointLogVO  积分记录
     * @return 是否执行成功
     */
    Boolean checkPointsFreeze(PointLogVO pointLogVO);

    /**
     * 取消积分冻结
     * @param pointLogVO  积分记录
     */
    Boolean cancelPointsFreeze(PointLogVO pointLogVO);

    /**
     * 确认积分冻结
     * @param pointLogVO 积分记录
     */
    Boolean confirmPointsFreeze(PointLogVO pointLogVO);

    /**
     * 取消积分冻结
     * @param log  积分记录
     */
    Boolean cancelPointsFreezeByLog(PointsOperationLog log);

    /**
     * 确认积分冻结
     * @param log 积分记录
     */
    Boolean confirmPointsFreezeByLog(PointsOperationLog log);

    /**
     * 查询积分兑换规则
     * @param currencyCode 币种  CommonConstants.TOP_UP_CURRENCY_CODE
     * @return 积分兑换规则
     */
    CurrencyToPointsRuleDTO queryCurrencyToPointsRule(String currencyCode);

    /**
     * 积分充值
     * @param vo
     * @return
     */
    Boolean pointsTopUp(PointTopUpVO vo);

    /**
     * 充值 积分记录
     * @param vo 积分充值信息
     */
     Boolean topUp(PointTopUpVO vo);


    /**
     * 下载简历  校验积分是否足够、记录积分记录
     * @param vo
     * @return
     */
     Boolean resumeDownload(ResumeDownloadVO vo);

     /**
     * 背景检查积分扣除
     * @param userId
     * @param candidateJobId 候选人与职位关系ID
     * @param remark
     * @return
     */
     Boolean baskgroundCheck(Long userId,Long candidateJobId,String remark);

    /**
     * 积分转移
     * @param vo
     * @return
     */
    Boolean pointTransfer(PointTransferVO vo);


    /**
     * 扣除面试冻结积分
     * @param candidateJobId 候选人职位关联
     * @return
     */
    Boolean confirmInterviewFreeze(Long candidateJobId);

    /**
     * 取消面试冻结积分
     * @param candidateJobId
     * @return
     */
    Boolean cancelInterviewFreeze(Long candidateJobId);

    Pager<IamPointsTransactionResDTO> pagePointsTransaction(IamPointsTransactionDTO iamPointsTransactionDTO);

    /**
     * 面试积分扣除  适用于 minute模式
     * @param pointLogVO
     * @return
     */
    public Boolean interviewDeduct(PointLogVO pointLogVO);

    /**
     * ready 扣减
     * @param pointLogVO
     * @return
     */
    public Boolean deductCreditCenterPoints(PointLogVO pointLogVO);

    /**
     * 查询积分
     * @return
     */
    IamUserPointsDTO getCreditCenterPoints();

    /**
     * 是否豁免积分
     * @param companyCode
     * @return
     */
    boolean isExempt(String companyCode);


    /**
     * 变更职位时校验积分是否充足
     * @param JobId
     * @return
     */
    boolean updateJobCheckPoints(Long JobId,String companyCode,Integer oldInterviewType,Integer newInterviewType);

    /**
     * 获取租户主账号id
     * @param companyCode
     * @return
     */
    Long getUserPrimaryId(String companyCode);

}