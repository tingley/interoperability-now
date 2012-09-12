package com.globalsight.tip;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

abstract class PackageSource {
    
    static final String SEPARATOR = "/"; 

    abstract void open(TIPPLoadStatus status) throws IOException;
    
    abstract boolean close() throws IOException;
    
    abstract File getPackageObjectFile(String path);
    
    abstract File getPackageFile(String path);
    
    protected abstract File getPackageDir();
    
    // XXX What's the difference between this and the next one?
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
    
    Set<String> getPackageObjects() {
        Set<String> objects = new HashSet<String>();
        // Recursively iterate through all the directories in the 
        // package object directory and create a package object
        // for every file (not directory).
        createPackageObjects(SEPARATOR, getPackageDir(), objects);
        return objects;
    }
    
    void createPackageObjects(String prefix, File directory, Set<String> objects) {
        for (File file : directory.listFiles()) {
            if (file.isDirectory()) {
                createPackageObjects(prefix + file.getName() + SEPARATOR, 
                                     file, objects);
            }
            else {
                objects.add(prefix + file.getName());
            }
        }
    }
    
    /**
     * @param path
     * @return
     * @throws TIPPException 
     * @throws IOException 
     */
    BufferedOutputStream getPackageObjectOutputStream(String path) throws IOException, TIPPException {
    	File f = getPackageObjectFile(path);
        if (!f.exists()) {
            if (!FileUtil.recursiveCreate(f)) {
                throw new TIPPException(
                        "Unable to open resource for writing: " + path);
            }
        }
    	if (f.isDirectory()) {
    		throw new FileNotFoundException("Path is a directory: " + path);
    	}
    	return new BufferedOutputStream(new FileOutputStream(f));
    }
    
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
        throw new IllegalStateException("Package contains no payload");
    }

}
