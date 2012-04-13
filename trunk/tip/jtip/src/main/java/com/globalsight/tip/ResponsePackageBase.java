package com.globalsight.tip;

class ResponsePackageBase extends PackageBase implements TIPResponsePackage {

	ResponsePackageBase(PackageSource packageSource) {
		super(packageSource);
	}

	public boolean isRequest() {
		return false;
	}

	@Override
	void setManifest(TIPManifest manifest) {
		if (manifest.isRequest()) {
			throw new IllegalStateException(
					"Constructing a response package with request manifest");
		}
		super.setManifest(manifest);
	}
	
	public String getRequestPackageId() {
		return ((TIPTaskResponse)getManifest().getTask())
				.getRequestPackageId();
	}

	public TIPCreator getRequestCreator() {
		return ((TIPTaskResponse)getManifest().getTask()).getRequestCreator();
	}

	void setRequestPackageId(String requestPackageId) {
		((TIPTaskResponse)getManifest().getTask())
				.setRequestPackageId(requestPackageId);
	}
	
	void setRequestCreator(TIPCreator requestCreator) {
		((TIPTaskResponse)getManifest().getTask())
				.setRequestCreator(requestCreator);
	}

	public TIPResponseMessage getMessage() {
		return ((TIPTaskResponse)getManifest().getTask()).getMessage();
	}
	
	void setMessage(TIPResponseMessage message) {
		((TIPTaskResponse)getManifest().getTask()).setMessage(message);
	}

	public String getComment() {
		return ((TIPTaskResponse)getManifest().getTask()).getComment();
	}
	
	void setComment(String comment) {
		((TIPTaskResponse)getManifest().getTask()).setComment(comment);
	}

}
