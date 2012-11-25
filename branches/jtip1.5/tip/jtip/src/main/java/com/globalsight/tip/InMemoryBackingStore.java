package com.globalsight.tip;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * A backing store that holds its data in memory.
 */
public class InMemoryBackingStore implements PackageStore {

    private byte[] manifestData;
    private byte[] temp;
    private Map<String, byte[]> objectData = 
            new HashMap<String, byte[]>();
    
    public OutputStream storeManifestData() throws IOException {
        return new ByteArrayOutputStream() {
            @Override
            public void close() throws IOException {
                super.close();
                manifestData = toByteArray();
            }
        };
    }

    public OutputStream storeObjectFileData(final String path)
            throws IOException {
        return new ByteArrayOutputStream() {
            @Override
            public void close() throws IOException {
                super.close();
                objectData.put(path, toByteArray());
            }
        };
    }
    
    public InputStream getManifestData() {
        return getStream(manifestData);
    }

    public InputStream getObjectFileData(String path) {
        byte[] data = objectData.get(path);
        return getStream(data);
    }
    
    public OutputStream storeTransientData(String id) throws IOException {
        return new ByteArrayOutputStream() {
            @Override
            public void close() throws IOException {
                super.close();
                temp = toByteArray();
            }
        };
    }

    public InputStream getTransientData(String id) throws IOException {
        InputStream is = getStream(temp);
        return is;
    }
    
    public InputStream removeTransientData(String id) throws IOException {
        InputStream is = getStream(temp);
        temp = null;
        return is;
    }


    private ByteArrayInputStream getStream(byte[] b) {
        return b != null ? new ByteArrayInputStream(b) : null;
    }
    
    public Set<String> getObjectFilePaths() {
        return objectData.keySet();
    }

    public boolean close() {
        return true;
    }
}
