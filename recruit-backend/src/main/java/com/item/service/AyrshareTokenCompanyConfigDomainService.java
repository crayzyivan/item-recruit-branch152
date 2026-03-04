package com.item.service;

import com.item.dto.ayrshare.AyrshareTokenSaveDTO;
import com.item.vo.ayrshare.AyrshareTokenVO;

/**
 * Ayrshare Token领域服务接口
 *
 * @author lh
 * @since 2025-08-28
 */
public interface AyrshareTokenCompanyConfigDomainService {

    /**
     * 保存Token配置
     * 不在使用 需求变更 在https://jira.logisticsteam.com/browse/RP-193 为了安全底层逻辑注释
     * @param saveDTO Token保存DTO
     * @return Token视图VO
     */
    @Deprecated
    Boolean saveToken(AyrshareTokenSaveDTO saveDTO);

    /**
     * 获取Token信息
     * 不在使用 需求变更 在https://jira.logisticsteam.com/browse/RP-193  为了安全底层逻辑注释
     * @return Token视图VO
     */
    @Deprecated
    AyrshareTokenVO getTokenInfo();
}
