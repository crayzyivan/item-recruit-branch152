package com.item.dto.job;

import lombok.Data;

import java.util.List;

/**
 * @author : lh
 */
@Data
public class RecruitJobListReqDTO {
    private int pageIndex = 1;
    private int pageSize = 10;
    private int locationId = 0;
    private int jobTypeId = 0;
    private int jobCategoryId = 0;
    private int datePosted = -1;
    private String keyword = "";
    private List<Integer> jobStatus;
    private List<Integer> salaryTypes;
    private List<Integer> typeIds;
    private List<Integer> categoryIds;
    private List<Integer> modeIds;

}
