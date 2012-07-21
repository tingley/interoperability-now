package com.globalsight.tip;

import java.io.File;
import java.io.IOException;

/**
 * Package source backed by a set of files in local file system.
 */
class FilePackageSource extends PackageSource {

    private File packageDir;
    
    FilePackageSource(File packageDir) {
        this.packageDir = packageDir;
    }
    
    protected FilePackageSource() {
    }
    
    protected void setPackageDir(File packageDir) {
        this.packageDir = packageDir;
    }
    
    protected File getPackageDir() {
        return packageDir;
    }
    
    @Override
    File getPackageFile(String path) {
        return new File(packageDir, path);
    }

    @Override
    File getPackageObjectFile(String path) {
        return getPackageFile(path);
    }

    @Override
    void open() throws IOException {
        // XXX Anything to do here?  Verify existence of manifest?
    }
    
    @Override
    boolean close() throws IOException { 
        return true;
    }

}
