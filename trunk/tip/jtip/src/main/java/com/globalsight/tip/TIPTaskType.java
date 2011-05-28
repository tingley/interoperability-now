package com.globalsight.tip;

public enum TIPTaskType {
    TRANSLATE("Translate");
    
    private String value;
    TIPTaskType(String value) {
        this.value = value;
    }
    
    public String getValue() {
        return value;
    }
    
    public static TIPTaskType fromValue(String value) {
        for (TIPTaskType type : values()) {
            if (type.getValue().equals(value)) {
                return type;
            }
        }
        return null;
    }
}
