package com.globalsight.tip;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class TIPPackage {

    private PackageSource packageSource;
    private TIPManifest manifest;
    
    TIPPackage(PackageSource packageSource) {
        this.packageSource = packageSource;
    }
    
    static final String MANIFEST = "manifest.xml";
    static final String INSECURE_OBJECTS_FILE = "pobjects.zip";
    static final String SECURE_OBJECTS_FILE = "pobjects.zip.enc";
    
    /**
     * Create a new empty TIPPackage.
     * 
     * XXX The problem here is that the open semantics don't work right.
     * 
     * @return new TIPPackage
     */
    public static TIPPackage newPackage() throws TIPException {
        TIPPackage tipPackage = new TIPPackage(new TempFilePackageSource());
        tipPackage.manifest = TIPManifest.newManifest(tipPackage);
        tipPackage.open();
        return tipPackage;
    }
    
    /**
     * Create a new TIPPackage object from a byte stream.  The data 
     * must be ZIP-encoded.  The TIPPackage will be expanded to disk
     * and backed by a set of temporary files.
     * 
     * @param inputStream
     * @return new TIPPackage
     */
    public static TIPPackage openFromStream(InputStream inputStream) 
                throws TIPException {
        TIPPackage tip = new TIPPackage(new StreamPackageSource(inputStream));
        tip.open();
        return tip;
    }

    public static TIPPackage openFromDirectory(File directory) 
                throws TIPException {
        if (!directory.exists()) {
            throw new IllegalArgumentException(
                    "Directory " + directory + " does not exist");
        }
        if (!directory.isDirectory()) {
            throw new IllegalArgumentException(
                    "Not a directory: " + directory);
        }
        TIPPackage tip = new TIPPackage(new FilePackageSource(directory));
        tip.open();
        return tip;
    }
    
    public TIPManifest getManifest() {
        return manifest;
    }
    
    /**
     * Write this package to an output stream as a ZIP archive
     * @param outputStream
     * @throws TIPException if there is a problem saving the package
     */
    public void save(OutputStream outputStream) throws TIPException {
        // XXX What if this has no backing on disk?
        ZipOutputStream zos = new ZipOutputStream(outputStream);

        try {
            // Write out the manifest
            zos.putNextEntry(new ZipEntry(MANIFEST));
            manifest.saveToStream(zos);
            zos.closeEntry();
    
            // For some reason writing the zip stream out within another
            // zip stream gives me strange zip corruption errors.  Write
            // pobjects.zip out to a temp file and copy it over.
            File temp = writeObjectsToFile();
            
            // Now write out all the parts as an inner archive
            zos.putNextEntry(new ZipEntry(INSECURE_OBJECTS_FILE));
            FileUtil.copyFileToStream(temp, zos);
            zos.closeEntry();
            
            zos.flush();
            zos.close();
            temp.delete();
        }
        catch (Exception e) {
            throw new TIPException(e);
        }
    }
    
    /**
     * Write the contents of this package to a directory on disk. 
     * @param outputDirectory top-level directory to contain the package.
     *        This directory should be empty.
     * @throws TIPException
     */
    public void saveToDirectory(File outputDirectory) throws TIPException {
        if (!outputDirectory.exists()) {
            if (!outputDirectory.mkdir()) {
                throw new IllegalArgumentException("Directory " + 
                        outputDirectory + " can't be created");
            }
            // TODO: make sure the directory is empty
        }
        else if (!outputDirectory.isDirectory()) {
            throw new IllegalArgumentException("File " + 
                    outputDirectory + " is not a directory");
        }
        
        try {
            FileOutputStream manifestStream = new FileOutputStream(
                    new File(outputDirectory, MANIFEST));
            manifest.saveToStream(manifestStream);
            manifestStream.close();
            
            for (TIPObjectSection section : manifest.getObjectSections()) {
                for (TIPObjectFile objFile : section.getObjectFiles()) {
                    // TODO: convert path separators
                    String path = section.getName() + 
                        File.separator + objFile.getLocation();
                    File file = new File(outputDirectory, path);
                    File parent = file.getParentFile();
                    if (!parent.exists()) {
                        if (!parent.mkdirs()) {
                            throw new IllegalStateException(
                                    "Can't create directory " + parent);
                        }
                    }
                    InputStream is = objFile.getInputStream();
                    FileUtil.copyStreamToFile(is, file);
                    is.close();
                }
            }
        }
        catch (Exception e) {
            throw new TIPException(e);
        }
    }
    
    /**
     * Create a zip archive of the package objects as a file on disk.
     * @return 
     */
    File writeObjectsToFile() throws IOException {
        File temp = File.createTempFile("pobjects", "zip");
        temp.deleteOnExit();
        ZipOutputStream zos = new ZipOutputStream(
                new BufferedOutputStream(
                    new FileOutputStream(temp)));
        writeObjects(zos);
        zos.flush();
        zos.close();
        return temp;
    }
    
    /**
     * Write out objects as a zip archive to the specified stream.  
     * Leaves the output stream open.
     * @param outputStream
     * @throws IOException
     */
    void writeObjects(ZipOutputStream zos) throws IOException {
        for (TIPObjectSection section : manifest.getObjectSections()) {
            for (TIPObjectFile file : section.getObjectFiles()) {
                String path = section.getName() +  
                        "/" + file.getLocation();
                zos.putNextEntry(new ZipEntry(path));
                InputStream is = file.getInputStream();
                FileUtil.copyStreamToStream(is, zos);
                zos.closeEntry();
                is.close();
            }
        }
        zos.flush();
    }

    void open() throws TIPException {
        try {
            packageSource.open();
            loadManifest();
        }
        catch (IOException e) {
            throw new TIPException(e);
        }
    }
    
    /**
     * Close the package and release any resources used by it
     * (temporary files, etc).
     * @throws IOException
     * @return true if this succeeds, false is some resources could
     *         not be released
     */
    public boolean close() throws TIPException {
        try {
            manifest = null;
            return packageSource.close();
        }
        catch (IOException e) {
            throw new TIPException(e);
        }
    }
        
    void loadManifest() throws TIPException {
        if (manifest != null) {
            return;
        }
        File manifestFile = getPackageFile(MANIFEST);
        if (!manifestFile.exists() || manifestFile.isDirectory()) {
            throw new RuntimeException("Invalid manifest file");
        }
        manifest = new TIPManifest(this);
        try {
            manifest.loadFromStream(new FileInputStream(manifestFile));
        }
        catch (Exception e) {
            throw new TIPException(e);
        }
    }
    
    File getPackageFile(String path) {
        return packageSource.getPackageFile(path);
    }
    
    // Handle failure
    File getPackageObjectFile(String path) {
        return packageSource.getPackageObjectFile(path);
    }
    
}
