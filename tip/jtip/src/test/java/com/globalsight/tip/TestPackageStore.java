package com.globalsight.tip;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import org.junit.Test;
import static org.junit.Assert.*;

public class TestPackageStore {

    @Test
    public void test() throws Exception {
        InputStream is =  
                getClass().getResourceAsStream("data/test_package.zip");
        TIPPLoadStatus status = new TIPPLoadStatus();
        PackageStore store = new InMemoryBackingStore();
        TIPPFactory.openFromStream(is, store, status);
        assertEquals(TIPPErrorSeverity.NONE, status.getSeverity());
    }
    
    @Test
    public void testInMemoryStore() throws Exception {
        PackageStore store = new InMemoryBackingStore();
        testStore(store);
    }
    
    @Test
    public void testTempFileStore() throws Exception {
        PackageStore store = new TempFileBackingStore();
        testStore(store);
    }
        
    void testStore(PackageStore store) throws Exception {
        writeLine(store.storeManifestData(), "MANIFEST");
        writeLine(store.storeObjectFileData("bilingual/bar"), "data1");
        writeLine(store.storeObjectFileData("bilingual/baz"), "data2");
        writeLine(store.storeObjectFileData("preview/quux"), "data3");
        assertEquals("MANIFEST", readLine(store.getManifestData()));
        assertEquals("data1", readLine(store.getObjectFileData("bilingual/bar")));
        assertEquals("data2", readLine(store.getObjectFileData("bilingual/baz")));
        assertEquals("data3", readLine(store.getObjectFileData("preview/quux")));
    }
    
    void writeLine(OutputStream os, String s) throws IOException {
        os.write(s.getBytes());
        os.close();
    }
    
    String readLine(InputStream is) throws IOException {
        String s = new BufferedReader(new InputStreamReader(is)).readLine();
        is.close();
        return s;
    }
}
