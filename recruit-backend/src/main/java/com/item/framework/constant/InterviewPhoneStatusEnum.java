package com.item.framework.constant;

public enum InterviewPhoneStatusEnum {
    NOT_SCHEDULED(0,"Not scheduled interview phone"),
    SCHEDULED(1, "Scheduled interview phone"),
    NOT_CONNECTED(2, "Candidate missed interview phone"),   //需要发送短信提醒，24小时未变成 4 需要回拨
    CANCEL(3, "Candidate cancel interview phone"),   //回拨后未接听面试，视为放弃
    INTERVIEWED(4, "Candidate has interviewed"),
    ;

    private Integer code;
    private String name;

    InterviewPhoneStatusEnum(Integer code, String name){
        this.code = code;
        this.name = name;
    }

    public Integer getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public static InterviewPhoneStatusEnum getByCode(Integer code){
        if(code == null){ return null;}
        for(InterviewPhoneStatusEnum timeEnum : InterviewPhoneStatusEnum.values()){
            if(timeEnum.getCode().equals(code)){ return timeEnum; }
        }

        return null;
    }

    public static InterviewPhoneStatusEnum change(Integer code, boolean connected) {
        if (code == null) {
            return NOT_SCHEDULED;
        }
        
        InterviewPhoneStatusEnum currentStatus = getByCode(code);
        if (currentStatus == null) {
            return NOT_SCHEDULED;
        }
        
        switch (currentStatus) {
            case SCHEDULED:
                return connected ? INTERVIEWED : NOT_CONNECTED;
            case NOT_CONNECTED:
                return connected ? INTERVIEWED : CANCEL;
            case INTERVIEWED:
            case CANCEL:
                // Terminal states - no transition
                return currentStatus;
            default:
                return NOT_SCHEDULED;
        }
    }

}
