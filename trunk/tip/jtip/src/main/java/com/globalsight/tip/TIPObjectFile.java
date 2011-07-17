package com.globalsight.tip;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Represents a TIP resource represented as a file.
 */
public class TIPObjectFile {
    private TIPPackage tipPackage;
    private TIPObjectSection section;
    private String type;
    private String path;
    private boolean localizable;
    
    TIPObjectFile() { }

    public TIPObjectFile(String type, String path, boolean localizable) {
        this.type = type;
        this.path = path;
        this.localizable = localizable;
    }
    
    public TIPObjectSection getSection() {
        return section;
    }
    
    public void setSection(TIPObjectSection section) {
        this.section = section;
    }
    
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setLocalizable(boolean localizable) {
        this.localizable = localizable;
    }

    public boolean isLocalizable() {
        return localizable;
    }
    
    public InputStream getInputStream() throws IOException {
        return new BufferedInputStream(
                new FileInputStream(getFilePath()));
    }

    public OutputStream getOutputStream() throws IOException, TIPException {
        File f = getFilePath();
        if (!f.exists()) {
            if (!FileUtil.recursiveCreate(f)) {
                throw new TIPException(
                        "Unable to open resource for writing: " + path);
            }
        }
        return new BufferedOutputStream(
                new FileOutputStream(getFilePath()));
    }
    
    private File getFilePath() {
        return tipPackage.getPackageObjectFile(
                section.getObjectSectionType().getValue() + 
                    File.separator + path);
    }
    
    void setPackage(TIPPackage tipPackage) {
        this.tipPackage = tipPackage;
    }
        
    @Override
    public String toString() {
        return section.toString() + File.separator + path;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (localizable ? 1231 : 1237);
        result = prime * result + path.hashCode();
        result = prime * result + type.hashCode();
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
        return (f.isLocalizable() == isLocalizable() &&
                f.getType().equals(getType()) &&
                f.getPath().equals(getPath()));
    }
}
