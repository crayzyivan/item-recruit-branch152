package com.item.convert;

import com.item.vo.InviteInterviewResponseVO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * 邀请面试转换器
 * 使用MapStruct实现VO和DTO之间的转换
 *
 * @since 2025-11-13
 */
@Mapper(componentModel = "spring")
public interface InviteInterviewConvert {
    
    InviteInterviewConvert INSTANCE = Mappers.getMapper(InviteInterviewConvert.class);
    
    /**
     * 构建邀请面试响应VO
     *
     * @param applicationId 申请ID
     * @param candidateId 候选人ID
     * @param message 响应消息
     * @return 响应VO
     */
    default InviteInterviewResponseVO toResponseVO(Long applicationId, Long candidateId, String message) {
        return InviteInterviewResponseVO.builder()
                .applicationId(applicationId)
                .candidateId(candidateId)
                .message(message)
                .build();
    }
}

