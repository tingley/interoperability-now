package com.globalsight.tip;

import java.io.IOException;
import java.io.InputStream;

public class TIPPFactory {

    /**
     * Create a new TIPP object from a byte stream representation of 
     * a zipped TIPP.  The package data will be expanded into the provided
     * PackageStore as part of processing.
     * <p>
     * This method will return a non-null TIPP as long as the package could be 
     * loaded without encounterning a fatal error.  However, the TIPPLoadStatus object
     * passed as a parameter should be examined to see how successful the loading 
     * actually was.
     * 
     * @param inputStream zipped package data
     * @param store PackageStore to hold the expanded package data.
     * @param status a record of any errors encountered during loading
     * 
     * @return a TIPP if parsing was completed, or null if a FATAL error occurred.
     * @throws IOException 
     * @throws TIPPException if some other type of error occurred
     */
    // TODO - split the expand and the open operations up, so I can open just
    // from the store.  This involves change to the PackageReader.
    public static TIPP openFromStream(InputStream inputStream, 
            PackageStore store, TIPPLoadStatus status) throws IOException {
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
            // Reported exceptions will be logged as part of the load status;
            // we catch them (to terminate loading), but don't need to propagate them
            // further.
            return null;
        }
    }
    
    /**
     * Create a TIPP object from a PackageStore that already contains package data.
     * <p>
     * This method will return a non-null TIPP as long as the package could be 
     * loaded without encounterning a fatal error.  However, the TIPPLoadStatus object
     * passed as a parameter should be examined to see how successful the loading 
     * actually was.
     * 
     * @param store package store from which to load the package
     * @param status a record of any errors encountered during loading
     * @return a TIPP if parsing was completed, or null if a FATAL error occurred.
     * @throws TIPPException
     * @throws IOException
     */
    public static TIPP openFromStore(PackageStore store, TIPPLoadStatus status) 
            throws IOException {
        try {
            return new PackageReader(store).load(status);
        }
        catch (ReportedException e) {
            // Reported exceptions will be logged as part of the load status;
            // we catch them (to terminate loading), but don't need to propagate them
            // further.
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