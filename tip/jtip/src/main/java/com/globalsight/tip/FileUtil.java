package com.globalsight.tip;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipInputStream;

class FileUtil {

    /**
     * Create a temporary directory.  The directory will be 
     * deleted when the JVM exits.
     * @return temporary directory
     * @throws IOException on IOError
     * @throws IllegalStateException if the directory (somehow) already exists
     */
    public static File createTempDir(String prefix) throws IOException {
        File tempFile = File.createTempFile(prefix, "");
        String name = tempFile.getCanonicalPath();
        tempFile.delete();
        File tempDir = new File(name);
        if (!tempDir.mkdir()) {
            throw new IllegalStateException("Unable to create directory " + name);
        }
        tempDir.deleteOnExit();
        return tempDir;
    }
    
    public static ZipInputStream getZipInputStream(InputStream inputStream) 
            throws IOException {
        if (inputStream instanceof ZipInputStream) {
            return (ZipInputStream)inputStream;
        }
        else {
            return new ZipInputStream(inputStream);
        }
    }
    
    /**
     * Expand the contents of a ZIP file to a directory on disk.  Closes
     * the stream when done.
     * @param zipStream ZIP contents
     * @param destDir destination directory.  Must exist (and be a directory).
     * @throws IOException
     * @throws ZipException
     * @throws IllegalStateException if the archive tree can not be 
     *          replicated on disk
     */
    public static void expandZipArchive(ZipInputStream zipStream, File destDir)
                throws IOException, ZipException {
        
        for (ZipEntry entry = zipStream.getNextEntry(); entry != null; 
                                            entry = zipStream.getNextEntry()) {
            if (entry.isDirectory()) {
                File subDir = new File(destDir, entry.getName());
                if (!subDir.exists()) {
                    if (!subDir.mkdirs()) {
                        throw new IllegalStateException(
                                "Could not create child directory " + subDir);
                    }
                }
                continue;
            }
            File child = new File(destDir, entry.getName());
            File parent = child.getParentFile();
            if (!parent.exists()) {
                if (!parent.mkdirs()) {
                    throw new IllegalArgumentException(
                            "Could not create directory " + parent);
                }
            }
            if (!child.createNewFile()) {
                throw new IllegalStateException(
                            "Could not create child file " + child);
            }
            copyStreamToFile(zipStream, child);
            zipStream.closeEntry();
        }
        zipStream.close();
    }
    
    /**
     * Delete a file and all its descendants, indicating success or
     * failure. 
     * @param file
     * @throws IOException
     * @return true if this succeeds, or false if one or more files
     *      or directories can't be deleted. 
     */
    public static boolean recursiveDelete(File file) throws IOException {
        boolean success = true;
        if (file.isDirectory()) {
            for (File child : file.listFiles()) {
                if (!recursiveDelete(child)) {
                    success = false;
                }
            }
        }
        if (!file.delete()) {
            success = false;
        }
        return success;
    }
    
    /**
     * Copy contents of a stream to a target file.  Leave the input
     * stream open afterwards.
     * @param is input stream
     * @param outputFile target file
     * @throws IOException
     */
    public static void copyStreamToFile(InputStream is, File outputFile) 
            throws IOException {
        OutputStream os = 
            new BufferedOutputStream(new FileOutputStream(outputFile));
        copyStreamToStream(is, os);
        os.close();
    }
    
    public static void copyFileToStream(File input, OutputStream os) 
            throws IOException {
        InputStream is = new BufferedInputStream(new FileInputStream(input));
        copyStreamToStream(is, os);
        is.close();
    }
    
    /**
     * Copy contents of an input stream to an output stream.  Leave both 
     * streams open afterwards.
     * @param is input stream
     * @param os output stream
     * @throws IOException
     */
    public static void copyStreamToStream(InputStream is, OutputStream os) 
            throws IOException {
        byte[] buffer = new byte[4096];
        for (int read = is.read(buffer); read != -1; read = is.read(buffer)) {
            os.write(buffer, 0, read);
        }
        os.flush();
    }
}
