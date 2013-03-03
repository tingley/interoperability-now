package com.globalsight.tip;

import java.util.Collections;
import java.util.Collection;

/**
 * Representation of a custom task type.
 */
public class CustomTaskType implements TIPPTaskType {

    private String type;
    private Collection<TIPPSectionType> supportedSectionTypes;
   
    @SuppressWarnings("unchecked")
    public CustomTaskType(String type, Collection<TIPPSectionType> supportedSectionTypes) {
        this.type = type;
        this.supportedSectionTypes = supportedSectionTypes != null ?  
                Collections.unmodifiableCollection(supportedSectionTypes) :
                Collections.EMPTY_SET;
    }
    
    public String getType() {
        return type;
    }

    public Collection<TIPPSectionType> getSupportedSectionTypes() {
        return supportedSectionTypes;
    }

}
