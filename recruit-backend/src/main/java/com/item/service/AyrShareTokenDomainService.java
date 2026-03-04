package com.item.service;

import com.item.vo.ayrshare.AyrShareJwtResponseVO;
import com.item.vo.ayrshare.AyrShareUserPlatformsVO;

/**
 * AyrShare Token领域服务接口
 *
 * @author hua.liu
 * @since 2025-08-27
 */
public interface AyrShareTokenDomainService {

    /**
     * 生成JWT Token
     *
     * @return JWT响应信息
     */
    AyrShareJwtResponseVO generateJwt();

    /**
     * 获取平台详情信息
     *
     * @return 平台信息
     */
    AyrShareUserPlatformsVO getProfileDetails();

    Boolean allowHotList();

    Boolean allowHotListSaveConfig();
}
