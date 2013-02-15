package com.globalsight.tip;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;

/**
 * Represents a TIP resource represented as a file.
 */
public class TIPPObjectFile {
    private PackageBase tipPackage;
    private TIPPObjectSection section;

    private String name, location;
    private int sequence = 1;
    
    TIPPObjectFile() { }

    /**
     * Constructor where name and location are the same.
     * @param location
     */
    TIPPObjectFile(String location) {
        this(location, location);
    }

    TIPPObjectFile(String location, int sequence) {
        this(location, location, sequence);
    }

    TIPPObjectFile(String location, String name) {
        this.location = location;
        this.name = name;
    }
    
    TIPPObjectFile(String location, String name, int sequence) {
        this(location, name);
        this.sequence = sequence;
    }
    
    TIPPObjectSection getSection() {
        return section;
    }
    
    void setSection(TIPPObjectSection section) {
        this.section = section;
    }
    
    public BufferedInputStream getInputStream() throws IOException {
    	return tipPackage.getPackageObjectInputStream(getCanonicalObjectPath());
    }

    public BufferedOutputStream getOutputStream() throws IOException, TIPPException {
    	return tipPackage.getPackageObjectOutputStream(getCanonicalObjectPath());
    }
    
    public String getCanonicalObjectPath() {
    	return section.getName() + PackageSource.SEPARATOR + location;
    }
    
    void setPackage(PackageBase tipPackage) {
        this.tipPackage = tipPackage;
    }

    public String getName() {
        // Name defaults to location, if name is not specified
        return name != null ? name : getLocation();
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public int getSequence() {
        return sequence;
    }

    public void setSequence(int sequence) {
        this.sequence = sequence;
    }
    
    @Override
    public String toString() {
        return location + "(" + name + ", " + sequence + ")";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + location.hashCode();
        result = prime * result + name.hashCode();
        result = prime * result + sequence;
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (o == null || !(o instanceof TIPPObjectFile)) {
            return false;
        }
        TIPPObjectFile f = (TIPPObjectFile)o;
        return f.getLocation().equals(getLocation()) &&
                f.getName().equals(getName()) &&
                f.getSequence() == getSequence();
    }
}
