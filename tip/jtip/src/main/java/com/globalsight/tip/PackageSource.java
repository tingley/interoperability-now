package com.globalsight.tip;

import java.io.IOException;

abstract class PackageSource {
    
    static final String SEPARATOR = "/";
    
    abstract void open(TIPPLoadStatus status) throws IOException;
    
    abstract boolean close() throws IOException;

    abstract void copyToStore(PackageStore store) throws IOException;

}
