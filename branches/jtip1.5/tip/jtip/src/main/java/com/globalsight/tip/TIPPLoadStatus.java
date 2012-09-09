package com.globalsight.tip;

import java.util.List;
import java.util.ArrayList;

public class TIPPLoadStatus {

    private TIPPErrorSeverity severity = TIPPErrorSeverity.NONE;
    private List<TIPPError> errors = new ArrayList<TIPPError>();

    public TIPPLoadStatus() {
    }

    void addError(TIPPError error) {
        errors.add(error);
        if (error.getErrorType().getSeverity().ordinal() > severity.ordinal()) {
            severity = error.getErrorType().getSeverity();
        }
    }
    
    public TIPPErrorSeverity getSeverity() {
        return severity;
    }

    public List<TIPPError> getAllErrors() {
        return errors;
    }
}
