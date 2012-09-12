package com.globalsight.tip;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipException;
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
    protected File getPackageDir() {
        return packageObjectsDir;
    }
    
    @Override
    void open(TIPPLoadStatus status) throws IOException {
        packageDir = FileUtil.createTempDir("tipPkg");
        try {
            ZipInputStream zis = FileUtil.getZipInputStream(inputStream);
            FileUtil.expandZipArchive(zis, packageDir);
        }
        catch (ZipException e) {
            // This exception is not called when you expect due to the 
            // odd behavior of the Java zip library.  For example, if the
            // ZIP file is not actually a ZIP, no error is thrown!  The stream
            // will just produce zero entries instead.
            status.addError(new TIPPError(TIPPError.Type.INVALID_PACKAGE_ZIP,
                            "Could not read package zip", e));
            throw new ReportedException(e);
        }
        
        // Expand package objects 
        File objectsFile = null;
        try {
            objectsFile = findObjectsFile();
        }
        catch (Exception e) {
            status.addError(new TIPPError(TIPPError.Type.MISSING_PAYLOAD));
            throw new ReportedException(e);
        }
        packageObjectsDir = FileUtil.createTempDir("tipObj");
        try {
            FileUtil.expandZipArchive(
                    FileUtil.getZipInputStream(new BufferedInputStream(
                            new FileInputStream(objectsFile))), 
                    packageObjectsDir);
        }
        catch (ZipException e) {
            status.addError(new TIPPError(TIPPError.Type.INVALID_PAYLOAD_ZIP,
                            "Could not read payload zip", e));
            throw new ReportedException(e);
        }
    }
    
    @Override
    boolean close() throws IOException {
        boolean success = FileUtil.recursiveDelete(packageObjectsDir);
        return FileUtil.recursiveDelete(packageDir) && success;
    }

}
