package com.globalsight.tip;

public enum TIPPResponseCode {
    Success("Success"),
    InvalidManifest("Invalid Manifest"),
    InvalidPayload("Invalid Payload"),
    SecurityFailure("Security Failure"),
    UnsupportedTaskType("Unsupported Task Type"),
    TaskFailure("Task Failure");
    
    private String value;
    
    TIPPResponseCode(String schemaValue) {
        this.value = schemaValue;
    }
    
    public String getName() {
        return value;
    }
    
    public static TIPPResponseCode fromSchemaValue(String value) {
        for (TIPPResponseCode msg : values()) {
            if (msg.value.equals(value)) {
                return msg;
            }
        }
        return null;
    }
}
