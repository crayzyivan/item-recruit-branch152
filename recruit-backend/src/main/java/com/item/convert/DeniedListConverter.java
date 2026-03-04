package com.item.convert;

import com.item.vo.CandidateJobVO;
import com.item.vo.DeniedListVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * DeniedListVO转换器
 * 用于将CandidateJobVO转换为DeniedListVO
 *
 * @author liyunlong
 * @version 1.0
 * @since 2025-09-23
 */
@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface DeniedListConverter {
    
    DeniedListConverter INSTANCE = Mappers.getMapper(DeniedListConverter.class);

    /**
     * 将CandidateJobVO转换为DeniedListVO
     * 将updateTime字段映射为rejectTime字段（拒绝时间）
     *
     * @param candidateJobVO 候选人职位VO
     * @return 拒绝列表VO
     */
    DeniedListVO candidateJobVOToDeniedListVO(CandidateJobVO candidateJobVO);

    /**
     * 批量转换CandidateJobVO列表为DeniedListVO列表
     *
     * @param candidateJobVOList 候选人职位VO列表
     * @return 拒绝列表VO列表
     */
    List<DeniedListVO> candidateJobVOListToDeniedListVOList(List<CandidateJobVO> candidateJobVOList);
}
