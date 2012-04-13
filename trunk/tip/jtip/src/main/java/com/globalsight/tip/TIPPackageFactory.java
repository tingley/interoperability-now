package com.globalsight.tip;

import java.io.IOException;
import java.io.InputStream;

public class TIPPackageFactory {

    /**
     * Create a new TIPPackage object from a byte stream.  The data 
     * must be ZIP-encoded.  The TIPPackage will be expanded to disk
     * and backed by a set of temporary files.
     * 
     * @param inputStream
     * @return new TIPPackage
     * @throws IOException 
     */
    public static TIPPackage openFromStream(InputStream inputStream) 
            throws TIPException, IOException {
    	return new PackageReader(new StreamPackageSource(inputStream)).load();
	}
    
    public static TIPWriteableRequestPackage newRequestPackage(TIPTaskType type) 
			throws TIPException, IOException {
    	PackageSource source = new TempFilePackageSource();
    	source.open();
    	TIPWriteableRequestPackage tipPackage = new TIPWriteableRequestPackage(source);
    	tipPackage.setManifest(TIPManifest.newRequestManifest(tipPackage, type));
    	return tipPackage;
    }
    
    public static TIPWriteableResponsePackage newResponsePackage(TIPTaskType type)
    		throws TIPException, IOException {
    	PackageSource source = new TempFilePackageSource();
    	source.open();
		TIPWriteableResponsePackage tipPackage = new TIPWriteableResponsePackage(source);
		tipPackage.setManifest(TIPManifest.newResponseManifest(tipPackage, type));
		return tipPackage;
    }
    
    public static TIPWriteableResponsePackage newResponsePackage(TIPRequestPackage requestPackage)
    		throws TIPException, IOException {
    	PackageSource source = new TempFilePackageSource();
    	source.open();
    	TIPWriteableResponsePackage tipPackage = new TIPWriteableResponsePackage(source);
    	tipPackage.setManifest(TIPManifest.newResponseManifest(tipPackage, 
    										  requestPackage));
    	return tipPackage;	
    }
}