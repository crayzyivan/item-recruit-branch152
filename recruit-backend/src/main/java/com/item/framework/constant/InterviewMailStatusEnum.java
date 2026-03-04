package com.item.framework.constant;

public enum InterviewMailStatusEnum {
    NOTSEND(0,"notsend"),
    SEND(1, "send");

    private Integer code;
    private String name;

    InterviewMailStatusEnum(Integer code, String name){
        this.code = code;
        this.name = name;
    }

    public Integer getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public static InterviewMailStatusEnum getByCode(Integer code){
        if(code == null){ return null;}
        for(InterviewMailStatusEnum timeEnum : InterviewMailStatusEnum.values()){
            if(timeEnum.getCode().equals(code)){ return timeEnum; }
        }

        return null;
    }

}
