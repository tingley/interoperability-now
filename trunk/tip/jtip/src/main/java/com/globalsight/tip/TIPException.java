package com.globalsight.tip;

/**
 * Base class for all exceptions generated within TIP.
 */
public class TIPException extends Exception {
    private static final long serialVersionUID = 1L;

    TIPException(String message) {
        super(message);
    }
    TIPException(Throwable cause) {
        super(cause);
    }
    TIPException(String message, Throwable cause) {
        super(cause);
    }
}
