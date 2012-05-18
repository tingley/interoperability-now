package com.globalsight.tip;

public class WriteableResponseTIPP extends ResponsePackageBase implements ResponseTIPP {

	WriteableResponseTIPP(PackageSource packageSource) {
		super(packageSource);
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
	public void setMessage(TIPPResponseMessage message) {
		super.setMessage(message);
	}
	
	@Override
	public void setComment(String comment) {
		super.setComment(comment);
	}
}
