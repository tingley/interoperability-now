package com.globalsight.tip;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;
import static org.junit.Assert.*;

import static com.globalsight.tip.TIPPError.Type.*;
import static com.globalsight.tip.TIPPErrorSeverity.*;

public class TestTIPPLoadStatus {

    @Test
    public void testNoErrors() { 
        TIPPLoadStatus status = new TIPPLoadStatus();
        assertNotNull(status.getAllErrors());
        assertEquals(0, status.getAllErrors().size());
        assertEquals(TIPPErrorSeverity.NONE, status.getSeverity());
    }
    
    @Test
    public void testSeverity() {
        TIPPLoadStatus status = new TIPPLoadStatus();
        status.addError(new TIPPError(INVALID_PAYLOAD_ZIP));
        status.addError(new TIPPError(MISSING_MANIFEST));
        status.addError(new TIPPError(MISSING_PAYLOAD_RESOURCE));
        assertEquals(3, status.getAllErrors().size());
        assertEquals(FATAL, status.getSeverity());
        Set<TIPPError> errors = new HashSet<TIPPError>(status.getAllErrors());
        assertTrue(errors.contains(new TIPPError(INVALID_PAYLOAD_ZIP)));
        assertTrue(errors.contains(new TIPPError(MISSING_MANIFEST)));
        assertTrue(errors.contains(new TIPPError(MISSING_PAYLOAD_RESOURCE)));
    }
}
