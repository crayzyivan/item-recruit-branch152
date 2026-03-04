package com.item.dto.migration.company;

import lombok.Data;

import java.io.Serializable;

/**
 * 公司 ProfileKey 数据传输对象
 * 
 * 用于存储公司的 ayrshare_profile_key 和 linkedin_company_id 信息，
 * 在数据迁移映射表的 ext 字段中以 JSON 格式存储。
 *
 * @author system
 * @since 2025-10-16
 */
@Data
public class CompanyProfileKeyDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Ayrshare Profile Key
     */
    private String ayrshareProfileKey;

    /**
     * LinkedIn 公司 ID
     */
    private String linkedinCompanyId;
}

