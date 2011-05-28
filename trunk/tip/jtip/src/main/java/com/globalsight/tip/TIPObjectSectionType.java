package com.globalsight.tip;

public enum TIPObjectSectionType {
    // TODO: more
    INPUT("input"),
    OUTPUT("output"),
    BILINGUAL("bilingual"),
    REFERENCE("reference");
    
    private String value;
    
    TIPObjectSectionType(String value) {
        this.value = value;
    }
    
    public String getValue() {
        return value;
    }
    
    public static TIPObjectSectionType fromValue(String value) {
        for (TIPObjectSectionType type : values()) {
            if (type.getValue().equals(value)) {
                return type;
            }
        }
        return null;
    }
}
