package com.globalsight.tip;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Represents a TIPP section.  Sections are identified by
 * type.  A section contains one or more resources of the specified type.
 */
class TIPPSection {
    private PackageBase tipp;
    private TIPPSectionType type;
    private String name;
    List<TIPPResource> resources = new ArrayList<TIPPResource>();
    
    TIPPSection() { }

    TIPPSection(String name, TIPPSectionType type) {
        this.name = name;
        this.type = type;
    }
    
    public TIPPSectionType getType() {
        return type;
    }
    
    void setPackage(PackageBase tip) {
        this.tipp = tip;
    }
    
    TIPP getPackage() {
        return tipp;
    }
    
    public void setType(TIPPSectionType type) {
        this.type = type;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public Collection<TIPPResource> getResources() {
        return resources;
    }

    // TODO: need to strip off unsupported types/properties
    public TIPPResource addResource(TIPPResource object) {
        resources.add(object);
        object.setSequence(resources.size());
        object.setPackage(tipp);
        object.setSection(this);
        return object;
    }
    
    @Override
    public String toString() {
        return name + "(" + type + ")";
    }
    
    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (o == null || !(o instanceof TIPPSection)) {
            return false;
        }
        TIPPSection s = (TIPPSection)o;
        return type.equals(s.getType()) && 
                name.equals(s.getName()) &&
                resources.equals(s.getResources());
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + type.hashCode();
        result = prime * result + resources.hashCode();
        return result;
    }
}
