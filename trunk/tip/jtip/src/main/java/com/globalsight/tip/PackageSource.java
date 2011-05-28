package com.globalsight.tip;

import java.io.File;
import java.io.IOException;

abstract class PackageSource {

    abstract void open() throws IOException;
    
    abstract boolean close() throws IOException;
    
    abstract File getPackageObjectFile(String path);
    
    abstract File getPackageFile(String path);
    
    // TODO: this needs to throw a better exception
    File findObjectsFile() throws IOException {
        // Very important that we check for the secure file 
        // first so that somebody can't poison the package by
        // inserting an unsigned file.
        File secureFile = getPackageFile(TIPPackage.SECURE_OBJECTS_FILE);
        if (secureFile.exists()) {
            throw new UnsupportedOperationException(
                    "TIP security is not yet supported");
        }
        File insecureFile = getPackageFile(TIPPackage.INSECURE_OBJECTS_FILE);
        if (insecureFile.exists()) {
            return insecureFile;
        }
        throw new IllegalArgumentException(
                "Package did not contain an objects file");
    }

}
