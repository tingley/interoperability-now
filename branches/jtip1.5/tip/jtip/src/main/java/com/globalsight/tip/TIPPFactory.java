package com.globalsight.tip;

import java.io.IOException;
import java.io.InputStream;

public class TIPPFactory {

    /**
     * Create a new TIPPackage object from a byte stream.  The data 
     * must be ZIP-encoded.  The TIPPackage will be expanded to disk
     * and backed by a set of temporary files.
     * 
     * @param inputStream
     * @return new TIPPackage
     * @throws IOException 
     */
    // TODO - split the expand and the open operations up, so I can open just
    // from the store.  This involves change to the PackageReader.
    public static TIPP openFromStream(InputStream inputStream, 
            PackageStore store, TIPPLoadStatus status) throws TIPPException, IOException {
        try {
            // XXX Not clear that open, close are still needed
            // TODO: move this elsewhere
            PackageSource source = new StreamPackageSource(inputStream);
            source.open(status); 
            source.copyToStore(store);
            source.close();

            return new PackageReader(store).load(status);
        }
        catch (ReportedException e) {
            return null;
        }
    }

    public static WriteableRequestTIPP newRequestPackage(TIPPTaskType type, PackageStore store) 
            throws TIPPException, IOException {
        WriteableRequestTIPP tipPackage = new WriteableRequestTIPP(store);
        tipPackage.setManifest(Manifest.newRequestManifest(tipPackage, type));
        return tipPackage;
    }

    public static WriteableResponseTIPP newResponsePackage(TIPPTaskType type, PackageStore store)
            throws TIPPException, IOException {
        WriteableResponseTIPP tipPackage = new WriteableResponseTIPP(store);
        tipPackage.setManifest(Manifest.newResponseManifest(tipPackage, type));
        return tipPackage;
    }

    public static WriteableResponseTIPP newResponsePackage(RequestTIPP requestPackage, PackageStore store)
            throws TIPPException, IOException {
        WriteableResponseTIPP tipPackage = new WriteableResponseTIPP(store);
        tipPackage.setManifest(Manifest.newResponseManifest(tipPackage, 
                requestPackage));
        return tipPackage;	
    }
}