package com.item.controller;

import com.item.dto.iam.IamPointsTransactionDTO;
import com.item.dto.iam.IamPointsTransactionResDTO;
import com.item.dto.iam.IamUserContextDTO;
import com.item.dto.iam.IamUserPointsDTO;
import com.item.framework.annotation.Auth;
import com.item.framework.constant.RoleType;
import com.item.framework.http.Pager;
import com.item.service.PointService;
import com.item.service.PointsOperationLogService;
import com.item.util.UserContextUtil;
import com.item.vo.PointLogListVo;
import com.item.vo.PointLogQueryVo;
import com.item.vo.PointTopUpVO;
import com.item.vo.PointTransferVO;
import com.item.vo.ResumeDownloadVO;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 积分操作控制器
 *
 * @author liyunlong
 * @version 1.0
 * @since 2025-07-29  19:02
 */
@RestController
@RequestMapping("/point")
public class PointController {

    @Resource
    private PointService pointService;
    @Resource
    private PointsOperationLogService pointsOperationLogService;

//    /**
//     * 积分充值
//     * @param vo
//     * @return
//     */
//    @Auth(roleType = RoleType.MASTER_USER)
//    @PostMapping("/top-up")
//    public Boolean pointTopUp(@Validated @RequestBody PointTopUpVO vo) {
//        return pointService.pointsTopUp(vo);
//    }

//    /**
//     * 积分转移
//     * @param vo
//     * @return
//     */
//    @Auth(roleType = RoleType.SUB_USER)
//    @PostMapping("/transfer")
//    public Boolean pointTransfer(@Validated @RequestBody PointTransferVO  vo) {
//        return pointService.pointTransfer(vo);
//    }

    /**
     * 下载简历
     * @return
     */
    @Auth(roleType = RoleType.SUB_USER)
    @PostMapping("/resume-download")
    public Boolean resumeDownload(@Validated @RequestBody ResumeDownloadVO  vo){
//        IamUserContextDTO iamUserContextDTO = UserContextUtil.getCurrentUserRecruitNeedLogin();
//        vo.setUserId(Long.parseLong(iamUserContextDTO.getId()));
//        return pointService.resumeDownload(vo);
        return true;
    }

    /**
     * 获取积分消费记录
     * @param pointsTransactionDTO
     * @return
     */
    @Auth(roleType = RoleType.SUB_USER)
    @PostMapping(value = "/transaction/page")
    public Pager<IamPointsTransactionResDTO> pointsTransactionPage(@RequestBody @Validated IamPointsTransactionDTO pointsTransactionDTO) {
        return pointService.pagePointsTransaction(pointsTransactionDTO);
    }

    @Auth(roleType = RoleType.SUB_USER)
    @GetMapping("/point-log-list")
    public Pager<PointLogListVo> selectPointLogPageList(@RequestParam(value = "pageIndex", required = false, defaultValue = "1") int pageIndex,
                                                     @RequestParam(value = "pageSize", required = false, defaultValue = "10") int pageSize,
                                                     @RequestParam(value = "transactionNo",required = false) String transactionNo,
                                                     @RequestParam(value = "transactionType",required = false) Integer transactionType,
                                                     @RequestParam(value = "pointStatus",required = false) Integer pointStatus){
        String userId = UserContextUtil.getCurrentUserRecruitNeedLogin().getId();
        PointLogQueryVo queryVo = PointLogQueryVo.builder().pageIndex(pageIndex).pageSize(pageSize).transactionNo(transactionNo).pointStatus(pointStatus)
                .transactionType(transactionType).userId(Long.parseLong(userId)).build();
        Pager<PointLogListVo> page =pointsOperationLogService.selectPointLogPageList(queryVo);
        return page;
    }

    /**
     * 查询用户积分
     * @return
     */
    @Auth(roleType = RoleType.SUB_USER)
    @GetMapping("/user-points")
    public IamUserPointsDTO getCreditCenterPoints(){
        return pointService.getCreditCenterPoints();
    }

}