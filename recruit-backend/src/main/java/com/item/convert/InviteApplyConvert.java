package com.item.convert;

import com.item.vo.InviteApplyResponseVO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * 邀请投递转换器
 * 用于VO和业务对象之间的转换
 *
 * @since 2025-11-13
 */
@Mapper(componentModel = "spring")
public interface InviteApplyConvert {
    InviteApplyConvert INSTANCE = Mappers.getMapper(InviteApplyConvert.class);

    /**
     * 将业务结果转换为InviteApplyResponseVO
     * @param iamAccount IAM账号（邮箱）
     * @param message 消息
     * @return InviteApplyResponseVO
     */
    default InviteApplyResponseVO toResponseVO( String iamAccount, String message) {
        return InviteApplyResponseVO.builder()
                .iamAccount(iamAccount)
                .message(message)
                .build();
    }
}

