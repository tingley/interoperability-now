package com.globalsight.tip;

class RequestPackageBase extends PackageBase implements RequestTIPP {
    
	RequestPackageBase(PackageStore store) {
		super(store);
	}
	
	@Override
	void setManifest(Manifest manifest) {
		if (!manifest.isRequest()) {
			throw new IllegalStateException(
					"Constructing a request package with response manifest");
		}
		super.setManifest(manifest);
	}
	
	public boolean isRequest() {
		return true;
	}
}
