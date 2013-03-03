package com.globalsight.tip;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;

public class TIPPFile extends TIPPResource {
    private String location;

    TIPPFile() { }

    /**
     * Constructor where name and location are the same.
     * @param location
     */
    
    TIPPFile(String location) {
        super(location);
        this.location = location;
    }

    TIPPFile(String location, int sequence) {
        super(location, sequence);
        this.location = location;
    }
    TIPPFile(String location, String name) {
        super(name);
        this.location = location;
    }
    
    TIPPFile(String location, String name, int sequence) {
        this(name, sequence);
        this.location = location;
    }
    
    @Override
    public String getName() {
        return super.getName() != null ? super.getName() : getLocation(); 
    }
        
    @Override
    public BufferedInputStream getInputStream() throws IOException {
        return getPackage().getPackageObjectInputStream(getCanonicalObjectPath());
    }

    @Override
    public BufferedOutputStream getOutputStream() throws IOException, TIPPException {
        return getPackage().getPackageObjectOutputStream(getCanonicalObjectPath());
    }

    public String getCanonicalObjectPath() {
        return getSection().getName() + PackageSource.SEPARATOR + location;
    }
    
    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    @Override
    public String toString() {
        return getName() + "(" + location + ", " + getSequence() + ")";
    }
    
    @Override
    public int hashCode() {
        return super.hashCode() * 31 + location.hashCode(); 
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TIPPFile)) return false;
        TIPPFile f = (TIPPFile)o;
        return super.equals(o) && f.getLocation().equals(getLocation()); 
    }
}
