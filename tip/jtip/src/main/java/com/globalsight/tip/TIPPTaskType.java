package com.globalsight.tip;

import java.util.Collection;

/**
 * Representation of a TIPP task type.  This may be a built-in 
 * type (see {@link StandardTaskType}) or a {@link CustomTaskType}.
 *
 */
public interface TIPPTaskType {

    public String getType();

    public Collection<TIPPSectionType> getSupportedSectionTypes();
}
