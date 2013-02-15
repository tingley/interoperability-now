package com.globalsight.tip;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public interface WriteableTIPP extends TIPP {

	void setPackageId(String id);
	
	void setCreator(TIPPCreator creator);
	
	void setTaskType(String taskTypeUri);
	
	void setSourceLocale(String sourceLocale);
	
	void setTargetLocale(String targetLocale);
	
	/**
	 * Add data to the package as an embedded object.  This will copy the 
	 * specified data stream into temporary storage for inclusion in the 
	 * final package.  Sequence numbers will be assigned to the objects
	 * within a given section, starting with 1 and increasing in the order
	 * that objects are added to that section.
	 * 
	 * @param sectionType The type of the section to which the object
	 *  		should be added.
	 * @param objectName The name of the resource, as it should be preserved
	 * 			in the package.  The name may optionally be a relative path.
	 * 			If so, all path separators will be normalized to forward
	 * 			slashes (UNIX-style) before storing in the package.
	 * 		    Note that within the package itself, the data may be stored
	 * 			under a different name, in order to comply by the restrictions
	 * 		    of the archiving system or other platform. 
	 * @param objectData Object data stream.
	 * @return TIPObjectFile that is added to the package
	 */
	TIPPObjectFile addSectionObject(TIPPObjectSectionType sectionType, String objectName,
								   InputStream objectData) 
										   throws IOException, TIPPException;
	
	/**
	 * Add data to the package as an embedded object.  This will copy the 
	 * specified data file into temporary storage for inclusion in the 
	 * final package.  Sequence numbers will be assigned to the objects
	 * within a given section, starting with 1 and increasing in the order
	 * that objects are added to that section.
	 * 
	 * @param sectionType The type of the section to which the object
	 *  		should be added.
	 * @param objectName The name of the resource, as it should be preserved
	 * 			in the package.  The name may optionally be a relative path.
	 * 			If so, all path separators will be normalized to forward
	 * 			slashes (UNIX-style) before storing in the package.
	 * 		    Note that within the package itself, the data may be stored
	 * 			under a different name, in order to comply by the restrictions
	 * 		    of the archiving system or other platform. 
	 * @param objectData Object data file.
	 * @return TIPObjectFile that is added to the package
	 */
	TIPPObjectFile addSectionObject(TIPPObjectSectionType sectionType, String objectName, 
								   File objectData) 
										   throws IOException, TIPPException;
}
