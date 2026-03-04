package com.item.dto.naukri;

import lombok.Data;

import java.util.List;

@Data
public class NaukriApplicationsResponseDTO {
    private List<NaukriApplicationDTO> data;
    private Integer totalCount;
    private Integer currentPage;
    private Integer pageSize;
}


