package com.globalsight.tip;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Base package implementations.  This is actually mutable, and the 
 * readable/writeable stuff is enforced by the interfaces.  Shhh, 
 * don't tell anybody.
 */
abstract class PackageBase implements TIPWriteablePackage {

    private PackageSource packageSource;
    private TIPManifest manifest;
    
    PackageBase(PackageSource packageSource) {
        this.packageSource = packageSource;
    }
    
    static final String MANIFEST = "manifest.xml";
    static final String INSECURE_OBJECTS_FILE = "pobjects.zip";
    static final String SECURE_OBJECTS_FILE = "pobjects.zip.enc";

    TIPManifest getManifest() {
        return manifest;
    }
    
    void setManifest(TIPManifest manifest) {
    	this.manifest = manifest;
    }
    
    public String getPackageId() {
		return getManifest().getPackageId();
	}
	
	public TIPCreator getCreator() {
		return getManifest().getCreator();
	}
	
	public String getTaskType() {
		return getManifest().getTask().getTaskType();
	}
	
	public String getSourceLocale() {
		return getManifest().getTask().getSourceLocale();
	}
	
	public String getTargetLocale() {
		return getManifest().getTask().getTargetLocale();
	}
	
	public TIPObjectSection getObjectSection(String sectionType) {
		return getManifest().getObjectSection(sectionType);
	}
	
	public Collection<TIPObjectSection> getObjectSections() {
		return getManifest().getObjectSections();
	}
	
	public void setPackageId(String id) {
		getManifest().setPackageId(id);
	}
	
	public void setCreator(TIPCreator creator) {
		getManifest().setCreator(creator);
	}
	
	public void setTaskType(String taskTypeUri) {
		getManifest().getTask().setTaskType(taskTypeUri);
	}
	
	public void setSourceLocale(String sourceLocale) {
		getManifest().getTask().setSourceLocale(sourceLocale);
	}
	
	public void setTargetLocale(String targetLocale) {
		getManifest().getTask().setTargetLocale(targetLocale);
	}
	
	public TIPObjectSection addObjectSection(String name, String type) {
		return getManifest().addObjectSection(name, type);
	}
    
    /**
     * Write this package to an output stream as a ZIP archive
     * @param outputStream
     * @throws TIPException
     * @throws IOException
     */
    public void saveToStream(OutputStream outputStream) throws TIPException, IOException {
        // XXX What if this has no backing on disk?
        ZipOutputStream zos = new ZipOutputStream(outputStream);

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
    
    /**
     * Write the contents of this package to a directory on disk. 
     * @param outputDirectory top-level directory to contain the package.
     *        This directory should be empty.
     * @throws TIPException
     */
    public void saveToDirectory(File outputDirectory) throws TIPException, IOException {
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
    
    /**
     * Close the package and release any resources used by it
     * (temporary files, etc).
     * @throws IOException
     * @return true if this succeeds, false is some resources could
     *         not be released
     */
    public boolean close() throws IOException {
        manifest = null;
        return packageSource.close();
    }
    
    BufferedInputStream getPackageObjectInputStream(String path) throws FileNotFoundException {
    	return packageSource.getPackageObjectInputStream(path);
    }
    
    BufferedOutputStream getPackageObjectOutputStream(String path) throws IOException, TIPException {
    	return packageSource.getPackageObjectOutputStream(path);
    }
}
