package com.globalsight.tip;

class RequestPackageBase extends PackageBase implements TIPRequestPackage {

	RequestPackageBase(PackageSource packageSource) {
		super(packageSource);
	}
	
	@Override
	void setManifest(TIPManifest manifest) {
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
