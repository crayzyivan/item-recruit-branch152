package com.item.framework.constant;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author : lh
 */
public enum CrmConvertResponseCode {
    CODE_72000H101("72000H101", CrmResponseCode.CRM_72000H101_FAIL),
    CODE_72000Z121("72000Z121", CrmResponseCode.CRM_FAIL),
    CODE_FAIL(CrmResponseCode.CRM_FAIL.getCode()+"", CrmResponseCode.CRM_FAIL),
    ;
    @Getter
    private String crmCode;

    @Getter
    private CrmResponseCode crmResponseCode;

    private CrmConvertResponseCode(String crmCode, CrmResponseCode crmResponseCode) {
        this.crmCode = crmCode;
        this.crmResponseCode = crmResponseCode;
    }


    private static final class Holder {
        private static final Map<String, CrmConvertResponseCode> MAP = Arrays.stream(values())
                .collect(Collectors.toUnmodifiableMap(CrmConvertResponseCode::getCrmCode, Function.identity()));
    }

    /**
     * 通过 code 获取枚举
     */
    public static CrmResponseCode getByCode(String crmCode) {
        if (StringUtils.isBlank(crmCode)) {
            return CrmResponseCode.CRM_72000H101_FAIL;
        }
        return Optional.ofNullable(CrmConvertResponseCode.Holder.MAP.get(crmCode)).map(CrmConvertResponseCode::getCrmResponseCode).orElse(CrmResponseCode.CRM_72000H101_FAIL);
    }
}
