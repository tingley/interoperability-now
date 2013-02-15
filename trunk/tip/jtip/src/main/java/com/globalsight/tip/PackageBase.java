package com.globalsight.tip;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.KeyPair;
import java.util.ArrayList;
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
abstract class PackageBase implements WriteableTIPP {

    private PackageStore store;
    private Manifest manifest;
    
    PackageBase(PackageStore store) {
        this.store = store;
    }
    
    static final String MANIFEST = "manifest.xml";
    static final String PAYLOAD_FILE = "pobjects.zip";

    Manifest getManifest() {
        return manifest;
    }
    
    void setManifest(Manifest manifest) {
    	this.manifest = manifest;
    }
    
    public String getPackageId() {
		return getManifest().getPackageId();
	}
	
	public TIPPCreator getCreator() {
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
	
	public void setCreator(TIPPCreator creator) {
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
	public List<TIPPObjectFile> getSectionObjects(TIPPObjectSectionType sectionType) {
		TIPPObjectSection section = 
				getManifest().getObjectSection(sectionType);
		if (section == null) {
			return Collections.emptyList();
		}
		List<TIPPObjectFile> list = 
				new ArrayList<TIPPObjectFile>(section.getObjectFiles());
		Collections.sort(list, new Comparator<TIPPObjectFile>() {
			public int compare(TIPPObjectFile f1, TIPPObjectFile f2) {
				return f1.getSequence() - f2.getSequence();
			}
		});
		return list;
	}
	
	public Set<TIPPObjectSectionType> getSections() {
		Set<TIPPObjectSectionType> sections = new HashSet<TIPPObjectSectionType>();
		for (TIPPObjectSection s : getManifest().getObjectSections()) {
			sections.add(s.getType());
		}
		return sections;
	}
	
	public String getSectionName(TIPPObjectSectionType sectionType) {
		TIPPObjectSection section = 
				getManifest().getObjectSection(sectionType);
		return (section == null) ? null : section.getName();
	}

	public TIPPObjectFile addSectionObject(TIPPObjectSectionType sectionType, 
			String objectName, InputStream objectData) throws IOException, TIPPException {
		TIPPObjectSection section = manifest.getObjectSection(sectionType);
		if (section == null) {
			// Create the section.  Derive the section name from the uri.
			section = manifest.addObjectSection(sectionType.getDefaultName(),
					sectionType);
		}
		// TODO: path normalization, etc
		// TODO: handle reference special case, and refactor with the Manifest code
		TIPPObjectFile objectFile = new TIPPObjectFile(objectName, objectName);
		section.addObject(objectFile);
		// Copy the data
		OutputStream os = objectFile.getOutputStream();
		FileUtil.copyStreamToStream(objectData, os);
		objectData.close();
		os.close();
		return objectFile;
	}
	
	public TIPPObjectFile addSectionObject(TIPPObjectSectionType sectionType, 
			String objectName, File objectData) throws IOException, TIPPException {
		return addSectionObject(sectionType, objectName, 
				new BufferedInputStream(new FileInputStream(objectData)));
	}
	
    /**
     * Write this package to an output stream as a ZIP archive
     * @param outputStream
     * @throws TIPPException
     * @throws IOException
     */
    public void saveToStream(OutputStream outputStream) throws TIPPException, IOException {
        saveToStream(new ManifestWriter(), outputStream);
    }
    
    /**
     * Write this package to an output stream as a ZIP archive, including 
     * digital signature information in the Manifest using the specified keypair.
     * @param outputStream
     * @param keyPair keypair with which to sign the manifest
     * @throws TIPPException
     * @throws IOException
     */
    public void saveToStream(OutputStream outputStream, KeyPair keyPair) throws TIPPException, IOException {
        ManifestWriter mw = new ManifestWriter();
        mw.setKeyPair(keyPair);
        saveToStream(mw, outputStream);
    }
    
    private void saveToStream(ManifestWriter mw, OutputStream outputStream) throws TIPPException, IOException {
        ZipOutputStream zos = new ZipOutputStream(outputStream);

        // For some reason writing the zip stream out within another
        // zip stream gives me strange zip corruption errors.  Write
        // pobjects.zip out to a temp file and copy it over.
        OutputStream tempOutputStream = store.storeTransientData("output-stream");
        writeObjects(tempOutputStream);
        tempOutputStream.close();
        
        // Write out all the parts as an inner archive
        zos.putNextEntry(new ZipEntry(PAYLOAD_FILE));
        FileUtil.copyStreamToStream(store.getTransientData("output-stream"), zos);
        zos.closeEntry();

        // Now write out the manifest.  We do this last so we can
        // pass the objects reference.
        // Add the payload as well, in case we are signing.
        mw.setPayload(store.removeTransientData("output-stream"));
        zos.putNextEntry(new ZipEntry(MANIFEST));
        mw.saveToStream(manifest, zos);
        zos.closeEntry();

        zos.flush();
        zos.close();
    }
    
    /**
     * Create a zip archive of the package objects as a file on disk.
     * @return 
     */
    void writeObjects(OutputStream os) throws IOException {
        ZipOutputStream zos = new ZipOutputStream(os);
        writeZipObjects(zos);
        zos.close();
    }
    
    /**
     * Write out objects as a zip archive to the specified stream.  
     * Leaves the output stream open.
     * @param outputStream
     * @throws IOException
     */
    void writeZipObjects(ZipOutputStream zos) throws IOException {
        for (TIPPObjectSection section : manifest.getObjectSections()) {
            for (TIPPObjectFile file : section.getObjectFiles()) {
                String path = file.getCanonicalObjectPath();
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
        return store.close();
    }
    
    BufferedInputStream getPackageObjectInputStream(String path) throws IOException {
    	return new BufferedInputStream(store.getObjectFileData(path));
    }
    
    BufferedOutputStream getPackageObjectOutputStream(String path) throws IOException, TIPPException {
    	return new BufferedOutputStream(store.storeObjectFileData(path));
    }
}
