package com.globalsight.tip;

public interface TIPWriteablePackage extends TIPPackage {

	void setPackageId(String id);
	
	void setCreator(TIPCreator creator);
	
	void setTaskType(String taskTypeUri);
	
	void setSourceLocale(String sourceLocale);
	
	void setTargetLocale(String targetLocale);
	
	TIPObjectSection addObjectSection(String name, String type);
}
