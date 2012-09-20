package com.globalsight.tip;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipInputStream;

/**
 * PackageSource that reads contents from a zipped package archive.
 */
class StreamPackageSource extends PackageSource {

    private InputStream inputStream;
    private TIPPLoadStatus status;

    StreamPackageSource(InputStream inputStream) {
        this.inputStream = inputStream;
    }
    
    @Override
    boolean close() throws IOException {
        return true;
    }

    @Override
    void copyToStore(PackageStore store) throws IOException {
        try {
            ZipInputStream zis = FileUtil.getZipInputStream(inputStream);
            for (ZipEntry entry = zis.getNextEntry(); entry != null; 
                    entry = zis.getNextEntry()) {
                if (entry.isDirectory()) {
                    continue;
                }
                String name = entry.getName();
                if (name.equals(PackageBase.MANIFEST)) {
                    FileUtil.copyStreamToStreamAndCloseDest(zis, store.storeManifestData());
                }
                // TODO: this is a bug - we need to always check for the secure payload first,
                // so that people can't poison packages by adding an insecure payload.
                else if (name.equals(PackageBase.INSECURE_OBJECTS_FILE)) {
                    copyPayloadToStore(zis, store);
                }
                else if (name.equals(PackageBase.SECURE_OBJECTS_FILE)) {
                    throw new UnsupportedOperationException("Encrypted payloads are not supported");
                }
                else {
                    status.addError(TIPPError.Type.UNEXPECTED_PACKAGE_CONTENTS, 
                            "Unexpected package contents: " + name);
                }
                zis.closeEntry();
            }
            zis.close();
        }
        catch (ZipException e) {
            // This exception is not called when you expect due to the 
            // odd behavior of the Java zip library.  For example, if the
            // ZIP file is not actually a ZIP, no error is thrown!  The stream
            // will just produce zero entries instead.
            status.addError(TIPPError.Type.INVALID_PACKAGE_ZIP,
                            "Could not read package zip", e);
            throw new ReportedException(e);
        }
    }
    
    private void copyPayloadToStore(InputStream is, PackageStore store) throws IOException {
        // TODO: There's a bug in the Java zip implementation -- I can't actually open 
        // a zip stream within another stream without buffering it.  As a result, I need 
        // to dump the contents of the payload object into a temporary location and then
        // read it back as a zip archive.
        FileUtil.copyStreamToStreamAndCloseDest(is, 
                            store.storeTransientData("payload"));
        ZipInputStream zis = FileUtil.getZipInputStream(store.getTransientData("payload"));
        for (ZipEntry entry = zis.getNextEntry(); entry != null; 
                entry = zis.getNextEntry()) {
            if (entry.isDirectory()) {
                continue;
            }
            FileUtil.copyStreamToStreamAndCloseDest(zis, 
                    store.storeObjectFileData(entry.getName()));
            zis.closeEntry();
        }
        zis.close();
    }
    
    @Override
    void open(TIPPLoadStatus status) throws IOException {
        this.status = status;
    }


}
