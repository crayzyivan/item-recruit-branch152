package com.item.service;

import com.item.dto.report.CandidateSimpleDTO;
import com.item.framework.http.Pager;
import com.item.vo.CandidateJobQueryVO;

/**
 * @author : lh
 */
public interface DashboardDomainService {
    Pager<CandidateSimpleDTO> getCandidateJobByCompanyCode(CandidateJobQueryVO queryVO);
}
