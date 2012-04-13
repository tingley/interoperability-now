package com.globalsight.tip;

public enum TIPResponseMessage {
    SUCCESS("Success"),
    FAILURE("Failure");
    
    private String value;
    
    TIPResponseMessage(String value) {
        this.value = value;
    }
    public String getValue() {
        return value;
    }
    public static TIPResponseMessage fromValue(String value) {
        for (TIPResponseMessage m : values()) {
            if (m.getValue().equals(value)) {
                return m;
            }
        }
        return null;
    }
    @Override
    public String toString() {
        return value;
    }
}
