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
    List<TIPPResource> resources = new ArrayList<TIPPResource>();
    
    TIPPSection() { }

    TIPPSection(TIPPSectionType type) {
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
    
    public Collection<TIPPResource> getResources() {
        return resources;
    }

    protected TIPPFile createFile(String name) {
        return new TIPPFile(name, name);
    }
    
    public TIPPFile addFile(String name) {
        return addFile(createFile(name));
    }
    
    TIPPFile addFile(TIPPFile file) {
        resources.add(file);
        file.setSequence(resources.size());
        file.setPackage(tipp);
        file.setSection(this);
        return file;
    }
    
    @Override
    public String toString() {
        return type.toString();
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
