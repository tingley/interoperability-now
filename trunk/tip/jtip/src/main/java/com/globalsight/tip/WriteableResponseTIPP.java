package com.globalsight.tip;

public class WriteableResponseTIPP extends ResponsePackageBase implements ResponseTIPP {

	WriteableResponseTIPP(PackageStore store) {
		super(store);
	}
	
	@Override
	public void setRequestPackageId(String requestPackageId) {
		super.setRequestPackageId(requestPackageId);
	}
	
	@Override
	public void setRequestCreator(TIPPCreator requestCreator) {
		super.setRequestCreator(requestCreator);
	}

	@Override
	public void setCode(TIPPResponseCode message) {
		super.setCode(message);
	}
	
	@Override
	public void setComment(String comment) {
		super.setComment(comment);
	}
}
