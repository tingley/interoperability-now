package com.globalsight.tip;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipInputStream;

class StreamPackageSource extends PackageSource {

    private InputStream inputStream;
    private File packageDir;
    private File packageObjectsDir;
    
    StreamPackageSource(InputStream inputStream) {
        this.inputStream = inputStream;
    }
    
    @Override
    File getPackageObjectFile(String path) {
        return new File(packageObjectsDir, path);
    }

    @Override
    File getPackageFile(String path) {
        return new File(packageDir, path);
    }
    
    @Override
    void open() throws IOException {
        ZipInputStream zis = FileUtil.getZipInputStream(inputStream);
        packageDir = FileUtil.createTempDir("tipPkg");
        FileUtil.expandZipArchive(zis, packageDir);
        
        // Expand package objects 
        File objectsFile = findObjectsFile();
        packageObjectsDir = FileUtil.createTempDir("tipObj");
        FileUtil.expandZipArchive(
                FileUtil.getZipInputStream(new BufferedInputStream(
                        new FileInputStream(objectsFile))), 
                packageObjectsDir);
    }
    
    @Override
    boolean close() throws IOException {
        boolean success = FileUtil.recursiveDelete(packageObjectsDir);
        return FileUtil.recursiveDelete(packageDir) && success;
    }

}
