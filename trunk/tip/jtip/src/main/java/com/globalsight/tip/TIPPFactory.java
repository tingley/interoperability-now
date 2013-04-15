package com.globalsight.tip;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.crypto.KeySelector;

public class TIPPFactory {

    /**
     * Create a new TIPP object from a byte stream representation of 
     * a zipped TIPP.  The package data will be expanded into the provided
     * PackageStore as part of processing.  If the package is signed, the
     * signature will <b>not</b> be verified.
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
    public static TIPP openFromStream(InputStream inputStream, 
            PackageStore store, TIPPLoadStatus status) throws IOException {
        return openFromStream(inputStream, store, status, null);
    }
    
    /**
     * Create a new TIPP object from a byte stream representation of 
     * a zipped TIPP.  The package data will be expanded into the provided
     * PackageStore as part of processing.  If the package is signed, it will
     * be verified using the provided key.
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
    public static TIPP openFromStream(InputStream inputStream, 
            PackageStore store, TIPPLoadStatus status,
            KeySelector keySelector) throws IOException {
        try {
            // XXX Not clear that open, close are still needed
            // TODO: move this elsewhere
            PackageSource source = new StreamPackageSource(inputStream);
            source.open(status);
            source.copyToStore(store);
            source.close();

            return new PackageReader(store).load(status, keySelector);
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
     * This method does not validate the package signature, even if it is present.
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
            return new PackageReader(store).load(status, null);
        }
        catch (ReportedException e) {
            // Reported exceptions will be logged as part of the load status;
            // we catch them (to terminate loading), but don't need to propagate them
            // further.
            return null;
        }
    }

    /**
     * Create a new TIPP request with the specified task type and storage.
     * 
     * @param type task type for the new TIPP
     * @param store backing store for any data added to the TIPP
     * @return TIPP
     * @throws TIPPException
     * @throws IOException
     */
    public static RequestTIPP newRequestPackage(TIPPTaskType type, PackageStore store) 
            throws TIPPException, IOException {
        RequestPackageBase tipPackage = new RequestPackageBase(store);
        tipPackage.setManifest(Manifest.newRequestManifest(tipPackage, type));
        return tipPackage;
    }

    /**
     * Create a new TIPP response with the specified task type and storage.
     * 
     * @param type task type for the new TIPP
     * @param store backing store for any data added to the TIPP
     * @return TIPP
     * @throws TIPPException
     * @throws IOException
     */
    public static ResponseTIPP newResponsePackage(TIPPTaskType type, PackageStore store)
            throws TIPPException, IOException {
        ResponsePackageBase tipPackage = new ResponsePackageBase(store);
        tipPackage.setManifest(Manifest.newResponseManifest(tipPackage, type));
        return tipPackage;
    }

    /**
     * Create a new TIPP response based on an existing request TIPP, using the specified storage.
     * <br>
     * The task type from the request TIPP will also become the task type for the response TIPP.
     * Additionally, the GlobalDescriptor information from the request TIPP (package id, tool, 
     * creator, etc) will be used to populate the InResponseTo information in the response.
     * @param requestPackage an existing request TIPP that will be used to populate the
     *        response metadata for the new TIPP.
     * @param store backing store for any data added to the TIPP
     * @return TIPP
     * @throws TIPPException
     * @throws IOException
     */
    public static ResponseTIPP newResponsePackage(RequestTIPP requestPackage, PackageStore store)
            throws TIPPException, IOException {
        ResponsePackageBase tipPackage = new ResponsePackageBase(store);
        tipPackage.setManifest(Manifest.newResponseManifest(tipPackage, 
                requestPackage));
        return tipPackage;	
    }
}