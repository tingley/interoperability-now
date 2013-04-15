package com.globalsight.tip;

import java.io.IOException;
import java.io.OutputStream;
import java.security.KeyPair;
import java.util.List;
import java.util.Collection;

public interface TIPP {

    /**
     * Close the package and release any resources used by it
     * (temporary files, etc).
     * @throws IOException
     * @return true if this succeeds, false is some resources could
     *         not be released
     */
	boolean close() throws IOException;
	
    /**
     * Write this package to an output stream as a ZIP archive
     * @param outputStream
     * @throws TIPPException
     * @throws IOException
     */
    void saveToStream(OutputStream outputStream) throws TIPPException, IOException;

    /**
     * Write this package to an output stream as a ZIP archive, signed using the 
     * provided KeyPair.
     * @param outputStream
     * @param keyPair a public/private keypair for generating a digital signature.
     * @throws TIPPException
     * @throws IOException
     */
    void saveToStream(OutputStream outputStream, KeyPair keyPair) throws TIPPException, IOException;

	/**
	 * Is this package a request?  If true, the package may be safely
	 * cast to TIPRequestPackage; if false, teh package may be safely
	 * cast to TIPResponsePackage.
	 *   
	 * @return boolean
	 */
	boolean isRequest();
	
	String getPackageId();
	
	TIPPCreator getCreator();
	
	String getTaskType();
	
	String getSourceLocale();
	
	String getTargetLocale();
	
	void setPackageId(String id);

	void setCreator(TIPPCreator creator);

	void setTaskType(String taskTypeUri);

	void setSourceLocale(String sourceLocale);

	void setTargetLocale(String targetLocale);
	
	TIPPSection getBilingualSection();
	TIPPSection getInputSection();
	TIPPSection getOutputSection();
	TIPPSection getSpecificationsSection();
	TIPPSection getTmSection();
	TIPPSection getTerminologySection();
	TIPPReferenceSection getReferenceSection();
	TIPPSection getPreviewSection();
	TIPPSection getMetricsSection();
	TIPPSection getExtrasSection();
	
	/**
	 * Return all the sections in this package.
	 * @return collection of sections
	 */
	Collection<TIPPSection> getSections();

	/**
	 * Return a list of all the objects in the specified section, ordered 
	 * according to the sequence values of the objects.  If the section does not
	 * exist in the package or contains no objects, an empty list is returned.
	 * 
	 * @param sectionType the type of the section to fetch objects for
	 * @return list of package objects, or an empty list.
	 */
	// TODO: put this in the section?
	List<TIPPResource> getSectionObjects(TIPPSectionType sectionType);

}
