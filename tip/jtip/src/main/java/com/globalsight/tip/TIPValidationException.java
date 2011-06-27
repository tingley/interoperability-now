package com.globalsight.tip;

/**
 * TIPException that is thrown when some aspect of a package
 * is invalid - for reasons of spec conformance, schema 
 * validation, etc. 
 */
public class TIPValidationException extends TIPException {
    private static final long serialVersionUID = 1L;

    TIPValidationException(String message) {
        super(message);
    }
    TIPValidationException(Throwable cause) {
        super(cause);
    }
    TIPValidationException(String message, Throwable cause) {
        super(message, cause);
    }

}
