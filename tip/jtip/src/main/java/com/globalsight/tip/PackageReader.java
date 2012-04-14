package com.globalsight.tip;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

class PackageReader {
	private PackageSource source;
	PackageReader(PackageSource source) {
		this.source = source;
	}
	
	PackageBase load() throws TIPException, IOException {
		source.open();
		
		try {
			InputStream is = source.getPackageStream(PackageBase.MANIFEST);
			TIPManifest manifest = new TIPManifest(null);
			manifest.loadFromStream(is);
			// What kind of manifest was it?
			PackageBase tip = null;
			if (manifest.isRequest()) {
				tip = new TIPWriteableRequestPackage(source);
			}
			else {
				tip = new TIPWriteableResponsePackage(source);
			}
			tip.setManifest(manifest);
			// HACK: Doing this to resolve an ugly chicken-and-egg
			// situation.  The package is injected by the manifest into
			// the package objects when they are created, but when we are
			// creating the package from a stream, the manifest is created
			// first and the package doesn't exist yet.  So I need to go back
			// and re-inject the package once it has been created.
			for (String sectionTypeUri : tip.getSections()) {
				for (TIPObjectFile file : tip.getSectionObjects(sectionTypeUri)) {
					file.setPackage(tip);
				}
			}
			return tip;
		}
		catch (FileNotFoundException e) {
			throw new TIPException("Package has no " + PackageBase.MANIFEST);
		}
	}
}
