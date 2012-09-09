package com.globalsight.tip;

/**
 * Severity for an error encountered while loading a TIPP.
 */
public enum TIPPErrorSeverity {

    /**
     * NONE is a sentinel severity that is only used as the return
     * value from TIPPLoadStatus.getSeverity().
     */
    NONE,

    /**
     * The WARN severity indicates a problem that should probably
     * result in a rejection of the TIPP (as it does not conform
     * with the specification), but that it may be possible for the
     * user to intervene or to automatically correct the data.  Warnings
     * include incorrectly formatted dates or package IDs.
     */
    WARN,

    /**
     * The ERROR severity indicates a problem that should result in 
     * rejection of the TIPP, although the TIPP data is probably at 
     * least semi-intact (and thus may be useful for reporting).  Errors
     * include invalid success/failure codes, TIPPs that contain
     * inappropriate files for a standard task type, objects that are
     * listed in the manifest but not present in the TIPP, invalid
     * section types, or objects that are present in the TIPP but not 
     * listed in the manifest.
     */
    ERROR,

    /**
     * A FATAL error meant that the manifest could not even be constructed
     * due to an error that derailed the parsing process.  Examples include
     * XML well-formedness errors, XML schema violations, I/O problems, or 
     * random exceptions.
     */
    FATAL;
}
