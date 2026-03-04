package com.item.framework.constant;

public enum DictionaryEnum {
    COMPLETION("gender","gender"),
    REPORT( "currency_type","currency type"),
    CHEATING_ANALYSIS_COMPLETED( "pay_cycle","salary type"),
    CAMER_RECORDING_URL( "education_level","institution type"),
    DEGREE_TYPE("degree_type","degree")
    ;

    private String name;
    private String remark;

    DictionaryEnum(String name,String remark) {
        this.name = name;
        this.remark = remark;
    }

    public String getName() {
        return name;
    }
}
