package com.globalsight.tip;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Iterator;

import org.junit.*;

import com.globalsight.tip.FileUtil;
import com.globalsight.tip.TIPManifest;
import com.globalsight.tip.TIPObjectFile;
import com.globalsight.tip.TIPObjectSection;
import com.globalsight.tip.TIPObjectSectionType;
import com.globalsight.tip.TIPPackage;

import static org.junit.Assert.*;

public class TestTIPPackage {
    
    @Test
    public void testPackageLoad() throws Exception {
        TIPPackage tip = getSamplePackage("data/test_package.zip");
        TestTIPManifest.verifySampleManifest(tip.getManifest());
        TIPManifest manifest = tip.getManifest();
        TIPObjectSection biSection = 
            manifest.getObjectSections(TIPObjectSectionType.BILINGUAL)
            .iterator().next();
        for (TIPObjectFile file : biSection.getObjectFiles()) {
            // Just instantiating the input stream is the real test..
            InputStream is = file.getInputStream();
            assertNotNull(is);
            is.close();
        }
        assertTrue("Could not clean up package", tip.close());
    }
    
    @Test
    public void testPackageSave() throws Exception {
        // Load the package, save it out to a zip file, read it back.
        TIPPackage tip = getSamplePackage("data/test_package.zip");
        File temp = File.createTempFile("tiptest", ".zip");
        OutputStream os = new BufferedOutputStream(new FileOutputStream(temp));
        tip.save(os);
        os.close();
        TIPPackage roundtrip  = TIPPackage.createFromStream(
                new BufferedInputStream(new FileInputStream(temp)));
        roundtrip.open();
        TestTIPManifest.verifySampleManifest(roundtrip.getManifest());
        verifyPackageParts(tip, roundtrip);
        temp.delete();
        assertTrue("Could not clean up package", tip.close());
        assertTrue("Could not clean up pacakge", roundtrip.close());
    }
    
    @Test
    public void testResponsePackage() throws Exception {
        TIPPackage tip = getSamplePackage("data/test_response_package.zip");
        TestTIPManifest.verifySampleResponseManifest(tip.getManifest());
        File temp = File.createTempFile("tiptest", ".zip");
        OutputStream os = new BufferedOutputStream(new FileOutputStream(temp));
        tip.save(os);
        os.close();
        TIPPackage roundtrip  = TIPPackage.createFromStream(
                new BufferedInputStream(new FileInputStream(temp)));
        roundtrip.open();
        TestTIPManifest.verifySampleResponseManifest(roundtrip.getManifest());
        verifyPackageParts(tip, roundtrip);
        temp.delete();
        assertTrue("Could not clean up package", tip.close());
        assertTrue("Could not clean up pacakge", roundtrip.close());
    }
    
    @Test
    public void testPackageSaveToDirectory() throws Exception {
        TIPPackage tip = getSamplePackage("data/test_package.zip");        
        File dir = FileUtil.createTempDir("tiptest");
        System.out.println("Using dir " + dir);
        tip.saveToDirectory(dir);
        TIPPackage roundtrip = TIPPackage.createFromDirectory(dir);
        roundtrip.open();
        TestTIPManifest.verifySampleManifest(roundtrip.getManifest());
        verifyPackageParts(tip, roundtrip);
        assertTrue("Could not clean up package", tip.close());
        assertTrue("Could not clean up package", roundtrip.close());
        // Cleanup our temp directory
        assertTrue("Could not clean up temp directory",
                   FileUtil.recursiveDelete(dir));
    }
    
    private TIPPackage getSamplePackage(String path) throws Exception {
        InputStream is = 
            getClass().getResourceAsStream(path);
        TIPPackage tip = TIPPackage.createFromStream(is);
        tip.open();
        return tip;
    }
    
    private void verifyPackageParts(TIPPackage p1, TIPPackage p2) throws Exception {
        TIPManifest m1 = p1.getManifest();
        TIPManifest m2 = p2.getManifest();
        // XXX Again, this cheats slightly by assuming a particular order
        assertEquals(m1.getObjectSections().size(), 
                     m2.getObjectSections().size());
        Iterator<TIPObjectSection> it1 = m1.getObjectSections().iterator();
        Iterator<TIPObjectSection> it2 = m2.getObjectSections().iterator();
        while (it1.hasNext()) {
            TIPObjectSection s1 = it1.next();
            assertTrue(it2.hasNext());
            TIPObjectSection s2 = it2.next();
            assertEquals(s1.getObjectFiles().size(), 
                         s2.getObjectFiles().size());
            Iterator<TIPObjectFile> fit1 = s1.getObjectFiles().iterator();
            Iterator<TIPObjectFile> fit2 = s2.getObjectFiles().iterator();
            while (fit1.hasNext()) {
                TIPObjectFile f1 = fit1.next();
                assertTrue(fit2.hasNext());
                TIPObjectFile f2 = fit2.next();
                assertEquals(f1, f2);
                InputStream is1 = f1.getInputStream();
                InputStream is2 = f2.getInputStream();
                verifyBytes(is1, is2);
                is1.close();
                is2.close();
            }
        }
    }
    
    private void verifyBytes(InputStream is1, InputStream is2) throws IOException {
        byte[] b1 = new byte[4096];
        byte[] b2 = new byte[4096];
        while (true) {
            Arrays.fill(b1, (byte)0);
            Arrays.fill(b2, (byte)0);
            int read1 = is1.read(b1);
            int read2 = is2.read(b2);
            assertEquals(read1, read2);
            if (read1 == -1) {
                break;
            }
            assertTrue(Arrays.equals(b1, b2));
        }
    }
}
