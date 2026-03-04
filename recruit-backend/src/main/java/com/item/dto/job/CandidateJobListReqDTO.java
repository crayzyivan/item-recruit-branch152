package com.item.dto.job;

import lombok.Data;

import java.util.List;

/**
 * @author : lh
 */
@Data
public class CandidateJobListReqDTO {
    private int pageIndex = 1;
    private int pageSize = 10;
    private int locationId = 0;
    private List<Integer> jobTypeIds;
    private int jobCategoryId = 0;
    private int datePosted = -1;
    private String keyword = "";
    private List<Integer> jobStatus;
    private List<Integer> salaryTypes;
    private List<Integer> typeIds;
    private List<Integer> categoryIds;
    private List<Integer> modeIds;

    /**
     * 用户当前位置 - 纬度
     * 用于地理位置查询，范围: -90 到 90
     */
    private Double latitude;

    /**
     * 用户当前位置 - 经度
     * 用于地理位置查询，范围: -180 到 180
     */
    private Double longitude;
}
