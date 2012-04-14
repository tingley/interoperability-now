package com.globalsight.tip;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface TIPPackage {

    /**
     * Close the package and release any resources used by it
     * (temporary files, etc).
     * @throws IOException
     * @return true if this succeeds, false is some resources could
     *         not be released
     */
	boolean close() throws IOException;
	
	/**
     * Write the contents of this package to a directory on disk. 
     * @param outputDirectory top-level directory to contain the package.
     *        This directory should be empty.
     * @throws IOException
     * @throws TIPException
     */
    void saveToDirectory(File outputDirectory) throws IOException, TIPException;
    
    /**
     * Write this package to an output stream as a ZIP archive
     * @param outputStream
     * @throws TIPException
     * @throws IOException
     */
    void saveToStream(OutputStream outputStream) throws TIPException, IOException;
	
	/**
	 * Is this package a request?  If true, the package may be safely
	 * cast to TIPRequestPackage; if false, teh package may be safely
	 * cast to TIPResponsePackage.
	 *   
	 * @return boolean
	 */
	boolean isRequest();
	
	String getPackageId();
	
	TIPCreator getCreator();
	
	String getTaskType();
	
	String getSourceLocale();
	
	String getTargetLocale();
	
	/**
	 * Return the type URIs of all sections in this package.
	 * @return set of section uris
	 */
	Set<String> getSections();
	
	/**
	 * Return the name used to identify the specified section in this
	 * package.
	 * @return section name, or null if there is no section of the specified 
	 * 		type
	 */
	String getSectionName(String sectionTypeUri);
	
	/**
	 * Return a list of all the objects in the specified section, ordered 
	 * according to the sequence values of the objects.  If the section does not
	 * exist in the package or contains no objects, an empty list is returned.
	 * 
	 * @param sectionTypeUri the type uri of this section
	 * @return list of package objects, or an empty list.
	 */
	List<TIPObjectFile> getSectionObjects(String sectionTypeUri);

}
