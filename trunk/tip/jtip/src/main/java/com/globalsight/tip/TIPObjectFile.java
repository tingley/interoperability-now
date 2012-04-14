package com.globalsight.tip;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;

/**
 * Represents a TIP resource represented as a file.
 */
public class TIPObjectFile {
    private PackageBase tipPackage;
    private TIPObjectSection section;

    private String name, location;
    private int sequence = 1;
    
    TIPObjectFile() { }

    /**
     * Constructor where name and location are the same.
     * @param location
     */
    TIPObjectFile(String location) {
        this(location, location);
    }

    TIPObjectFile(String location, int sequence) {
        this(location, location, sequence);
    }

    TIPObjectFile(String location, String name) {
        this.location = location;
        this.name = name;
    }
    
    TIPObjectFile(String location, String name, int sequence) {
        this(location, name);
        this.sequence = sequence;
    }
    
    TIPObjectSection getSection() {
        return section;
    }
    
    void setSection(TIPObjectSection section) {
        this.section = section;
    }
    
    public BufferedInputStream getInputStream() throws IOException {
    	return tipPackage.getPackageObjectInputStream(getObjectPath());
    }

    public BufferedOutputStream getOutputStream() throws IOException, TIPException {
    	return tipPackage.getPackageObjectOutputStream(getObjectPath());
    }
    
    private String getObjectPath() {
    	return section.getName() + File.separator + location;
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
        if (o == null || !(o instanceof TIPObjectFile)) {
            return false;
        }
        TIPObjectFile f = (TIPObjectFile)o;
        return f.getLocation().equals(getLocation()) &&
                f.getName().equals(getName()) &&
                f.getSequence() == getSequence();
    }
}
