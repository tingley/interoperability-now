package com.globalsight.tip;

import java.io.File;
import java.io.IOException;

/**
 * Package backed by a temporary directory.  Used for 
 * generating new packages.
 */
class TempFilePackageSource extends FilePackageSource {

    TempFilePackageSource() {
        super();
    }

    @Override
    void open() throws IOException {
        File packageDir = FileUtil.createTempDir("tip");
        setPackageDir(packageDir);        
    }
    
    @Override
    boolean close() throws IOException {
        FileUtil.recursiveDelete(getPackageDir());
        return true;
    }
}
