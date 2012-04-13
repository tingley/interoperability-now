package com.globalsight.tip;

public interface TIPResponsePackage extends TIPPackage {
	/**
	 * Get the ID of the request package to which this is a response.
	 * @return request package id
	 */
	String getRequestPackageId();
	
	/**
	 * Get information about the creator of the package to which this is
	 * a response.
	 * @return TIPCreator 
	 */
	TIPCreator getRequestCreator();
	
	/**
	 * Get the success/failure message for this package.
	 * @return TIPResponseMessage value
	 */
	TIPResponseMessage getMessage();
	
	/**
	 * Get the response package comment, if any.
	 * @return comment string
	 */
	String getComment();
}
