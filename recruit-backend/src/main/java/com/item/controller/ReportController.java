package com.item.controller;

import com.item.convert.ReportDailyConverter;
import com.item.convert.TopReportConverter;
import com.item.dto.iam.IamUserContextDTO;
import com.item.dto.report.CandidateSimpleDTO;
import com.item.es.ResumeEsService;
import com.item.framework.annotation.Auth;
import com.item.framework.constant.ReportType;
import com.item.framework.constant.RoleType;
import com.item.framework.http.Pager;
import com.item.service.CandidateJobService;
import com.item.service.CandidateService;
import com.item.service.DashboardDomainService;
import com.item.service.ReportService;
import com.item.util.UserContextUtil;
import com.item.vo.CandidateJobQueryVO;
import com.item.vo.report.ReportDailyVO;
import com.item.vo.report.TopViewDataVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * <p>
 *
 * </p>
 *
 * @author liuyabin on 2025/7/23
 * @since 1.0.0
 */
@RestController
@RequestMapping("/report")
@RequiredArgsConstructor
@Auth(roleType = RoleType.SUB_USER)
public class ReportController {
    private final ReportService reportService;
    private final CandidateJobService candidateJobService;
    private final ResumeEsService resumeEsService;
    private final DashboardDomainService dashboardDomainService;
    private final CandidateService candidateService;

    /**
     * 招聘者board页上部数据汇总
     * @return
     */
    @GetMapping("/top-view")
    public TopViewDataVO getTopView() {
        IamUserContextDTO userContextDTO = UserContextUtil.getCurrentUserRecruitNeedLogin();
        String companyCode = userContextDTO.getCompanyCode();

        return TopReportConverter.INSTANCE.convertToVO(reportService.getTopReport(companyCode));
    }

    /**
     * 招聘者board中间曲线图数据
     * @param timeType
     * @return
     */
    @GetMapping("/daily-report")
    public List<ReportDailyVO> getDailyReport(@RequestParam(value = "timeType", required = false, defaultValue = "0") Integer timeType) {
        String companyCode = UserContextUtil.getCurrentUserCompanyCode();
        ReportType reportType = ReportType.getByCode(timeType);

        return ReportDailyConverter.INSTANCE.convertDTOListToVOList(reportService.getDailyReport(companyCode, reportType));
    }

    /**
     * dashboard页上候选人列表
     * @param pageIndex
     * @param pageSize
     * @return
     */
    @GetMapping("/dashboard-candidate-list")
    public Pager<CandidateSimpleDTO> getCandidateList(@RequestParam(value = "pageIndex", required = false, defaultValue = "1") int pageIndex,
                                                      @RequestParam(value = "pageSize", required = false, defaultValue = "10") int pageSize) {
        String companyCode = UserContextUtil.getCurrentUserCompanyCode();
        CandidateJobQueryVO queryVO =
                CandidateJobQueryVO.builder().pageIndex(pageIndex).pageSize(pageSize).companyCode(companyCode).build();

//        return resumeEsService.getCandidateJobByCompanyCode(queryVO);
        return dashboardDomainService.getCandidateJobByCompanyCode(queryVO);
    }

    /**
     * dashboard页上候选人列表
     * @param pageIndex
     * @param pageSize
     * @return
     */
    @Auth(roleType = RoleType.SUB_USER)
    @GetMapping("/applications-candidate-list")
    public Pager<CandidateSimpleDTO> getApplicationsCandidateList(@RequestParam(value = "pageIndex", required = false, defaultValue = "1") int pageIndex,
                                                                 @RequestParam(value = "pageSize", required = false, defaultValue = "10") int pageSize,
                                                                  @RequestParam(value = "candidateName", required = false) String candidateName) {
        CandidateJobQueryVO queryVO =
                CandidateJobQueryVO.builder().pageIndex(pageIndex).pageSize(pageSize).candidateName(candidateName).build();
        return candidateService.getApplicationsCandidateList(queryVO);
    }
}
