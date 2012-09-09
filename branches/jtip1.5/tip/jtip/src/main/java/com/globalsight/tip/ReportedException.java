package com.globalsight.tip;

/**
 * ReportedException is a wrapper for exceptions that occur during
 * package load and have already been reported via the TIPPError /
 * TIPPLoadStatus mechanism, but still need to be rethrown in order
 * to halt processing.
 */
class ReportedException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    private Exception inner;
    
    ReportedException(Exception e) {
        inner = e;
    }
    public Exception getException() {
        return inner;
    }
}
