package com.item.vo;

import com.item.framework.http.Pager;
import lombok.Data;

/**
 * @author : lh
 */
@Data
public class JobShareListVO {
    private CompanyInfoVO company;

    private Pager<JobVO> jobPager;
}
