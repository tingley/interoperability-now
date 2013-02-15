package com.globalsight.tip;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Represents a TIP object section.  Object sections are identified by
 * a name and a type.  (There may be more than one section 
 * for a given type, distinguished by sequence number.)  An object 
 * section contains one or more objects of the specified type.
 */
// TODO eventually factor out TIPObject from TIPObjectFile
class TIPPObjectSection {
    private PackageBase tip;
    private TIPPObjectSectionType type;
    private String name;
    List<TIPPObjectFile> objects = new ArrayList<TIPPObjectFile>();
    
    TIPPObjectSection() { }

    TIPPObjectSection(String name, TIPPObjectSectionType type) {
        this.name = name;
        this.type = type;
    }
    
    public TIPPObjectSectionType getType() {
        return type;
    }
    
    void setPackage(PackageBase tip) {
        this.tip = tip;
    }
    
    TIPP getPackage() {
        return tip;
    }
    
    public void setType(TIPPObjectSectionType type) {
        this.type = type;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public Collection<TIPPObjectFile> getObjectFiles() {
        return objects;
    }

    // TODO: need to strip off unsupported types/properties
    public TIPPObjectFile addObject(TIPPObjectFile object) {
        objects.add(object);
        object.setSequence(objects.size());
        object.setPackage(tip);
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
        if (o == null || !(o instanceof TIPPObjectSection)) {
            return false;
        }
        TIPPObjectSection s = (TIPPObjectSection)o;
        return type.equals(s.getType()) && 
                name.equals(s.getName()) &&
                objects.equals(s.getObjectFiles());
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + type.hashCode();
        result = prime * result + objects.hashCode();
        return result;
    }
}
