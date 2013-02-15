package com.globalsight.tip;

import java.util.Set;

public interface TIPPTaskType {

    public String getType();

    public Set<TIPPObjectSectionType> getSupportedSectionTypes();
}
