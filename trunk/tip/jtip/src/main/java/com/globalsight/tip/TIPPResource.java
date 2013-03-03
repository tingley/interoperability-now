package com.globalsight.tip;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;

/**
 * Represents a TIP resource represented as a file.
 */
public abstract class TIPPResource {
    private PackageBase tipPackage;
    private TIPPSection section;

    private String name;
    private int sequence = 1;
    
    TIPPResource() { }
    
    TIPPResource(String name) {
        this.name = name;
    }
    
    TIPPResource(String name, int sequence) {
        this.name = name;
        this.sequence = sequence;
    }
    
    PackageBase getPackage() {
        return tipPackage;
    }
    
    TIPPSection getSection() {
        return section;
    }
    
    void setSection(TIPPSection section) {
        this.section = section;
    }
    
    public abstract BufferedInputStream getInputStream() throws IOException;

    public abstract BufferedOutputStream getOutputStream() throws IOException, TIPPException;
    
    void setPackage(PackageBase tipPackage) {
        this.tipPackage = tipPackage;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getSequence() {
        return sequence;
    }

    public void setSequence(int sequence) {
        this.sequence = sequence;
    }
    
    @Override
    public String toString() {
        return name + "(" + sequence + ")";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + name.hashCode();
        result = prime * result + sequence;
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (o == null || !(o instanceof TIPPResource)) {
            return false;
        }
        TIPPResource f = (TIPPResource)o;
        return f.getName().equals(getName()) &&
               f.getSequence() == getSequence();
    }
}
