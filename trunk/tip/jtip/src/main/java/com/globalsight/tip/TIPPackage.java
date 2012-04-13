package com.globalsight.tip;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;

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
	
	TIPObjectSection getObjectSection(String sectionType);
	
	Collection<TIPObjectSection> getObjectSections();
}
