package com.globalsight.tip;

public interface ResponseTIPP extends TIPP {
	/**
	 * Get the ID of the request package to which this is a response.
	 * @return request package id
	 */
	String getRequestPackageId();
	
	void setRequestPackageId(String requestPackageId);
	
	/**
	 * Get information about the creator of the package to which this is
	 * a response.
	 * @return TIPCreator 
	 */
	TIPPCreator getRequestCreator();
	
	void setRequestCreator(TIPPCreator creator);
	
	/**
	 * Get the success/failure message for this package.
	 * @return TIPResponseMessage value
	 */
	TIPPResponseCode getCode();
	
	void setCode(TIPPResponseCode code);
	
	/**
	 * Get the response package comment, if any.
	 * @return comment string
	 */
	String getComment();
	
	void setComment(String comment);
}
