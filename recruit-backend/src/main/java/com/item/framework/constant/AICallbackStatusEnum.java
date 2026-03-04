package com.item.framework.constant;

public enum AICallbackStatusEnum {
    PR_FAILED(-1,"Processing failed"),
    UN_PR(0,"Unprocessed"),
    RP_I(1,"Processing"),
    PR_D(2, "Processed");
    private Integer code;
    private String name;

    AICallbackStatusEnum(Integer code, String name){
        this.code = code;
        this.name = name;
    }

    public Integer getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public static AICallbackStatusEnum getByCode(Integer code){
        if(code == null){ return null;}
        for(AICallbackStatusEnum timeEnum : AICallbackStatusEnum.values()){
            if(timeEnum.getCode().equals(code)){ return timeEnum; }
        }

        return null;
    }

}
