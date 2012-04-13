package com.globalsight.tip;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

abstract class PackageSource {

    abstract void open() throws IOException;
    
    abstract boolean close() throws IOException;
    
    abstract File getPackageObjectFile(String path);
    
    abstract File getPackageFile(String path);
    
    BufferedInputStream getPackageStream(String path) throws FileNotFoundException {
    	return getInputStream(getPackageFile(path));
    }
    
    BufferedInputStream getPackageObjectInputStream(String path) throws FileNotFoundException {
    	return getInputStream(getPackageObjectFile(path));
    }
    
    protected BufferedInputStream getInputStream(File file) 
    					throws FileNotFoundException {
		return new BufferedInputStream(new FileInputStream(file));
    }
    
    /**
     * @param path
     * @return
     * @throws TIPException 
     * @throws IOException 
     */
    BufferedOutputStream getPackageObjectOutputStream(String path) throws IOException, TIPException {
    	File f = getPackageObjectFile(path);
        if (!f.exists()) {
            if (!FileUtil.recursiveCreate(f)) {
                throw new TIPException(
                        "Unable to open resource for writing: " + path);
            }
        }
    	if (f.isDirectory()) {
    		throw new FileNotFoundException("Path is a directory: " + path);
    	}
    	return new BufferedOutputStream(new FileOutputStream(f));
    }
    
    // TODO: this needs to throw a better exception
    File findObjectsFile() throws IOException {
        // Very important that we check for the secure file 
        // first so that somebody can't poison the package by
        // inserting an unsigned file.
        File secureFile = getPackageFile(PackageBase.SECURE_OBJECTS_FILE);
        if (secureFile.exists()) {
            throw new UnsupportedOperationException(
                    "TIP security is not yet supported");
        }
        File insecureFile = getPackageFile(PackageBase.INSECURE_OBJECTS_FILE);
        if (insecureFile.exists()) {
            return insecureFile;
        }
        throw new IllegalArgumentException(
                "Package did not contain an objects file");
    }

}
