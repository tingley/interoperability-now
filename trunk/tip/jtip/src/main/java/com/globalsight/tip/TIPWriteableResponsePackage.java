package com.globalsight.tip;

public class TIPWriteableResponsePackage extends ResponsePackageBase implements TIPResponsePackage {

	TIPWriteableResponsePackage(PackageSource packageSource) {
		super(packageSource);
	}
	
	@Override
	public void setRequestPackageId(String requestPackageId) {
		super.setRequestPackageId(requestPackageId);
	}
	
	@Override
	public void setRequestCreator(TIPCreator requestCreator) {
		super.setRequestCreator(requestCreator);
	}

	@Override
	public void setMessage(TIPResponseMessage message) {
		super.setMessage(message);
	}
	
	@Override
	public void setComment(String comment) {
		super.setComment(comment);
	}
}
