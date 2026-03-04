package com.item.framework.constant;

/**
 * <p>
 *
 * </p>
 *
 * @author liuyabin on 2025/7/7
 * @since 1.0.0
 */
public enum TimeEnum {
    LASTHOUR(0,"lastHour"),
    LASTDAY(1, "lastDay"),
    LASTWEEK(2,"lastWeek"),
    LAST14DAYS(3, "last14Days"),
    LAST30DAYS(4, "last30Days");

    private Integer code;
    private String name;

    TimeEnum(Integer code, String name){
        this.code = code;
        this.name = name;
    }

    public Integer getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public static TimeEnum getByCode(Integer code){
        if(code == null){ return null;}
        for(TimeEnum timeEnum : TimeEnum.values()){
            if(timeEnum.getCode().equals(code)){ return timeEnum; }
        }

        return null;
    }

    public static boolean isValid(int code){
        return getByCode(code) != null;
    }
}
