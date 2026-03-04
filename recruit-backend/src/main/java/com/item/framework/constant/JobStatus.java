package com.item.framework.constant;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Job status enumeration
 */
public enum JobStatus {
    DRAFT(0, "Draft"),
    ACTIVE(1, "Active"),
    CLOSED(2, "Closed"),
    OTHER(3, "Other"),
    AWAITING_PAYMENT(4, "Awaiting Payment"),
    ON_HOLD(5, "On Hold"),
    PENDING_REVIEW(6, "Pending Review"),
    PENDING_PUBLICATION(7, "Pending Publication"),
    PENDING_MODIFICATION(8, "Pending Modification"),

    ;

    private final Integer code;
    private final String description;

    JobStatus(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    public Integer getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    private static final class Holder{
        private static final Map<Integer, JobStatus> MAP = Arrays.stream(values())
                .collect(Collectors.toUnmodifiableMap(JobStatus::getCode, Function.identity()));
    }

    public static JobStatus getByCode(Integer code) {
        if (code == null) {
            return OTHER;
        }
        return Holder.MAP.get(code);
    }
} 
