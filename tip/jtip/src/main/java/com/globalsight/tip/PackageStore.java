package com.globalsight.tip;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;

/**
 * Representation of the backing store for an opened TIPP.  
 * Callers may implement this interface to allow for different
 * representations -- for example, in a database or in memory
 * as opposed to on the local file system.
 */
public interface PackageStore {

    public OutputStream storeManifestData() throws IOException;
    
    public OutputStream storeObjectFileData(String path) throws IOException;
    
    public OutputStream storeTransientData(String id) throws IOException;
    
    public OutputStream storeRawPayloadData() throws IOException;
    
    public InputStream getManifestData() throws IOException;
    
    public InputStream getObjectFileData(String path) throws IOException;
    
    public InputStream getRawPayloadData() throws IOException;
    
    public InputStream getTransientData(String id) throws IOException;
    
    public InputStream removeTransientData(String id) throws IOException;
    
    public Set<String> getObjectFilePaths();
    
    // TODO: is this ever called?
    public boolean close() throws IOException;
}
