package com.globalsight.tip;

import static com.globalsight.tip.TIPPErrorSeverity.*;

public class TIPPError {
    
    public enum Type {
        INVALID_PACKAGE_ZIP(FATAL),
        INVALID_PAYLOAD_ZIP(ERROR),
        // package (not payload) contains an unexpected file
        UNEXPECTED_PACKAGE_CONTENTS(ERROR),
        // manifest was not present
        MISSING_MANIFEST(FATAL),
        // manifest was present but unparseable
        CORRUPT_MANIFEST(FATAL),
        INVALID_SECTION_TYPE(ERROR),
        MISSING_PAYLOAD_RESOURCE(ERROR),
        UNEXPECTED_PAYLOAD_RESOURCE(ERROR),
        DUPLICATE_RESOURCE_IN_MANIFEST(ERROR);
        
        private TIPPErrorSeverity severity;
        Type(TIPPErrorSeverity severity) {
            this.severity = severity;
        }
        public TIPPErrorSeverity getSeverity() {
            return severity;
        }
    }
    
    private Type errorType;
    private String message;
    private Exception exception;
    
    TIPPError(Type errorType) {
        this(errorType, null, null);
    }
    
    TIPPError(Type errorType, String message) {
        this(errorType, message, null);
    }
    
    // TODO: This business with the message is messed up
    TIPPError(Type errorType, String message, Exception e) {
        this.errorType = errorType;
        this.message = message;
        this.exception = e;
    }
    
    public Type getErrorType() {
        return errorType;
    }
    
    public String getMessage() {
        return message;
    }
    
    public Exception getException() {
        return exception;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((errorType == null) ? 0 : errorType.hashCode());
        result = prime * result + ((message == null) ? 0 : message.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (o == null || !(o instanceof TIPPError)) return false;
        TIPPError e = (TIPPError)o;
        return (errorType == e.getErrorType() &&
                 (message == e.getMessage() || 
                     message != null && message.equals(e.getMessage())));
    } 
    
    @Override
    public String toString() {
        return errorType + "(" + message + ")";
    }
}
