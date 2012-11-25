package com.globalsight.tip;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class FileSystemBackingStore implements PackageStore {

    private File dir;
    private Map<String, File> transientData = new HashMap<String, File>();

    public FileSystemBackingStore(File directory) {
        this.dir = directory;
        if (!directory.exists()) {
            directory.mkdirs();
        }
        if (!directory.isDirectory()) {
            throw new IllegalArgumentException("Not a directory: " + directory);
        }
    }

    private File getManifest() {
        return new File(dir, PackageBase.MANIFEST);
    }
    
    public OutputStream storeManifestData() throws IOException {
        return new FileOutputStream(getManifest());
    }

    public InputStream getManifestData() throws IOException {
        return new FileInputStream(getManifest());
    }
    
    private File getObjectFile(String path) throws IOException {
        File f = new File(dir, path);
        if (!f.exists() && !FileUtil.recursiveCreate(f)) {
            throw new RuntimeException("Unable to create temporary file: " + f);
        }
        return f;
    }
    
    public OutputStream storeObjectFileData(String path) throws IOException {
        return new FileOutputStream(getObjectFile(path));
    }

    public InputStream getObjectFileData(String path) throws IOException {
        return new FileInputStream(getObjectFile(path));
    }

    public OutputStream storeTransientData(String id) throws IOException {
        File temp = File.createTempFile("jtip-" + id, "");
        transientData.put(id, temp);
        return new FileOutputStream(temp);
    }
    
    public InputStream getTransientData(String id) throws IOException {
        File temp = transientData.get(id);
        return new FileInputStream(temp);
    }
    
    public InputStream removeTransientData(String id) throws IOException {
        File temp = transientData.get(id);
        transientData.remove(id);
        return new FileInputStream(temp);
    }

    public Set<String> getObjectFilePaths() {
        Set<String> paths = new HashSet<String>();
        getPackageObjectPaths("", dir, paths);
        paths.remove(PackageBase.MANIFEST);
        return paths;
    }

    private void getPackageObjectPaths(String prefix, File directory, Set<String> objects) {
        for (File file : directory.listFiles()) {
            if (file.isDirectory()) {
                getPackageObjectPaths(prefix + file.getName() + PackageSource.SEPARATOR, 
                                     file, objects);
            }
            else {
                objects.add(prefix + file.getName());
            }
        }
    }
    
    public boolean close() throws IOException {
        // TODO what is the correct behavior here
        return false;
    }
    
}
