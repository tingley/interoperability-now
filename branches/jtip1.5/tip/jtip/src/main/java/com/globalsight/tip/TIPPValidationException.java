package com.globalsight.tip;

/**
 * TIPException that is thrown when some aspect of a package
 * is invalid - for reasons of spec conformance, schema 
 * validation, etc. 
 */
public class TIPPValidationException extends TIPPException {
    private static final long serialVersionUID = 1L;

    TIPPValidationException(String message) {
        super(message);
    }
    TIPPValidationException(Throwable cause) {
        super(cause);
    }
    TIPPValidationException(String message, Throwable cause) {
        super(message, cause);
    }

}
