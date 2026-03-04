package com.item.framework.constant;

/**
 * Job approval action enumeration
 * 
 * @author system
 * @since 1.0.0
 */
public enum JobApprovalAction {
    APPROVE,
    REJECT,
    MODIFY,
    PUBLISH,
    ON_HOLD,
    CLOSE,
    RESUBMIT,
    SUBMITTED;

    @Override
    public String toString() {
        return name();
    }
}
