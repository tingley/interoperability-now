package com.globalsight.tip;

public enum TIPPResponseMessage {
    Success("Success"),
    InvalidManifest("Invalid Manifest"),
    InvalidPayload("Invalid Payload"),
    SecurityFailure("Security Failure"),
    UnsupportedTaskType("Unsupported Task Type"),
    TaskFailure("Task Failure");
    
    private String value;
    
    TIPPResponseMessage(String schemaValue) {
        this.value = schemaValue;
    }
    
    public String getName() {
        return value;
    }
    
    public static TIPPResponseMessage fromSchemaValue(String value) {
        for (TIPPResponseMessage msg : values()) {
            if (msg.value.equals(value)) {
                return msg;
            }
        }
        return null;
    }
}
