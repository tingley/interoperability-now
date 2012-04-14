package com.globalsight.tip;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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

	// For now, this sorts every time
	public List<TIPObjectFile> getSectionObjects(String sectionTypeUri) {
		TIPObjectSection section = 
				getManifest().getObjectSection(sectionTypeUri);
		if (section == null) {
			return Collections.emptyList();
		}
		List<TIPObjectFile> list = 
				new ArrayList<TIPObjectFile>(section.getObjectFiles());
		Collections.sort(list, new Comparator<TIPObjectFile>() {
			public int compare(TIPObjectFile f1, TIPObjectFile f2) {
				return f1.getSequence() - f2.getSequence();
			}
		});
		return list;
	}
	
	public Set<String> getSections() {
		Set<String> sections = new HashSet<String>();
		for (TIPObjectSection s : getManifest().getObjectSections()) {
			sections.add(s.getType());
		}
		return sections;
	}
	
	public String getSectionName(String sectionTypeUri) {
		TIPObjectSection section = 
				getManifest().getObjectSection(sectionTypeUri);
		return (section == null) ? null : section.getName();
	}

	public TIPObjectFile addSectionObject(String sectionTypeUri, 
			String objectName, InputStream objectData) throws IOException, TIPException {
		TIPObjectSection section = manifest.getObjectSection(sectionTypeUri);
		if (section == null) {
			// Create the section.  Derive the section name from the uri.
			section = manifest.addObjectSection(
					sectionNameFromUri(sectionTypeUri), sectionTypeUri);
		}
		// TODO: path normalization, etc
		TIPObjectFile objectFile = new TIPObjectFile(objectName, objectName);
		section.addObject(objectFile);
		// Copy the data
		OutputStream os = objectFile.getOutputStream();
		FileUtil.copyStreamToStream(objectData, os);
		objectData.close();
		os.close();
		return objectFile;
	}
	
	public TIPObjectFile addSectionObject(String sectionTypeUri, 
			String objectName, File objectData) throws IOException, TIPException {
		return addSectionObject(sectionTypeUri, objectName, 
				new BufferedInputStream(new FileInputStream(objectData)));
	}
	
	private String sectionNameFromUri(String uri) {
		if (uri.endsWith("/")) {
			uri = uri.substring(0, uri.length() - 1);
		}
		return uri.substring(uri.lastIndexOf('/'), uri.length());
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
