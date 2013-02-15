package com.globalsight.tip;

class ResponsePackageBase extends PackageBase implements ResponseTIPP {

	ResponsePackageBase(PackageStore store) {
		super(store);
	}

	public boolean isRequest() {
		return false;
	}

	@Override
	void setManifest(Manifest manifest) {
		if (manifest.isRequest()) {
			throw new IllegalStateException(
					"Constructing a response package with request manifest");
		}
		super.setManifest(manifest);
	}
	
	public String getRequestPackageId() {
		return ((TIPPTaskResponse)getManifest().getTask())
				.getRequestPackageId();
	}

	public TIPPCreator getRequestCreator() {
		return ((TIPPTaskResponse)getManifest().getTask()).getRequestCreator();
	}

	void setRequestPackageId(String requestPackageId) {
		((TIPPTaskResponse)getManifest().getTask())
				.setRequestPackageId(requestPackageId);
	}
	
	void setRequestCreator(TIPPCreator requestCreator) {
		((TIPPTaskResponse)getManifest().getTask())
				.setRequestCreator(requestCreator);
	}

	public TIPPResponseMessage getMessage() {
		return ((TIPPTaskResponse)getManifest().getTask()).getMessage();
	}
	
	void setMessage(TIPPResponseMessage message) {
		((TIPPTaskResponse)getManifest().getTask()).setMessage(message);
	}

	public String getComment() {
		return ((TIPPTaskResponse)getManifest().getTask()).getComment();
	}
	
	void setComment(String comment) {
		((TIPPTaskResponse)getManifest().getTask()).setComment(comment);
	}

}
