package com.globalsight.tip;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.junit.*;

import com.globalsight.tip.FileUtil;
import com.globalsight.tip.TIPObjectFile;
import com.globalsight.tip.TIPObjectSection;
import com.globalsight.tip.TIPPackage;

import static org.junit.Assert.*;

public class TestTIPPackage {
    
    @Test
    public void testPackageLoad() throws Exception {
        TIPPackage tip = getSamplePackage("data/test_package.zip");
        verifyRequestPackage(tip);
        TIPObjectSection biSection = 
                tip.getObjectSection(StandardTaskTypeConstants.TranslateStrictBitext.BILINGUAL);
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
        tip.saveToStream(os);
        os.close();
        TIPPackage roundtrip  = TIPPackageFactory.openFromStream(
                new BufferedInputStream(new FileInputStream(temp)));
        verifyRequestPackage(roundtrip);
        comparePackageParts(tip, roundtrip);
        temp.delete();
        assertTrue("Could not clean up package", tip.close());
        assertTrue("Could not clean up pacakge", roundtrip.close());
    }
    
    @Test
    public void testResponsePackage() throws Exception {
        TIPPackage tip = getSamplePackage("data/test_response_package.zip");
        assertFalse(tip.isRequest());
        verifyResponsePackage((TIPResponsePackage)tip);
        File temp = File.createTempFile("tiptest", ".zip");
        OutputStream os = new BufferedOutputStream(new FileOutputStream(temp));
        tip.saveToStream(os);
        os.close();
        TIPPackage roundtrip  = TIPPackageFactory.openFromStream(
                new BufferedInputStream(new FileInputStream(temp)));
        assertFalse(roundtrip.isRequest());
        verifyResponsePackage((TIPResponsePackage)roundtrip);
        comparePackageParts(tip, roundtrip);
        temp.delete();
        assertTrue("Could not clean up package", tip.close());
        assertTrue("Could not clean up pacakge", roundtrip.close());
    }
    
    @Test
    public void testNewPackage() throws Exception {
        TIPWriteableRequestPackage tip = TIPPackageFactory.newRequestPackage(StandardTaskType.TRANSLATE_STRICT_BITEXT);
        tip.setCreator(
            new TIPCreator("testname", "testid", 
                           TestTIPManifest.getDate(2011, 7, 12, 20, 35, 12), 
                           new TIPTool("jtip", 
                                   "http://code.google.com/p/interoperability-now", "0.14"))
        );
        String requestPackageId = tip.getPackageId();
        assertNotNull(requestPackageId);
        assertTrue(requestPackageId.startsWith("urn:uuid"));
        tip.setSourceLocale("en-US");
        tip.setTargetLocale("fr-FR");
                
        TIPObjectSection inputSection = tip.addObjectSection("bilingual", 
            		StandardTaskTypeConstants.TranslateStrictBitext.BILINGUAL);
        TIPObjectFile f1 = inputSection.addObject(
                new TIPObjectFile("test1.xlf", 1));
        OutputStream os = f1.getOutputStream();
        FileUtil.copyStreamToStream(
                new ByteArrayInputStream("test".getBytes("UTF-8")), os);
        os.close();
        
        File temp = File.createTempFile("tiptest", ".zip");
        os = new FileOutputStream(temp);
        tip.saveToStream(os);
        os.close();
        TIPPackage roundTrip = 
            TIPPackageFactory.openFromStream(new FileInputStream(temp));
        assertNotNull(roundTrip);
        //assertEquals(manifest, roundTrip.getManifest());
        assertEquals(tip.getPackageId(), roundTrip.getPackageId());
        assertEquals(tip.getCreator(), roundTrip.getCreator());
        assertEquals(tip.getTaskType(), roundTrip.getTaskType());
        assertEquals(tip.getSourceLocale(), roundTrip.getSourceLocale());
        assertEquals(tip.getTargetLocale(), roundTrip.getTargetLocale());
        //assertEquals(manifest.getObjectSections(), roundTrip.getManifest().getObjectSections());
        Collection<TIPObjectSection> s1 = tip.getObjectSections();
        Collection<TIPObjectSection> s2 = roundTrip.getObjectSections();
        Iterator<TIPObjectSection> it1 = s1.iterator(), it2 = s2.iterator();
        for (; it1.hasNext() && it2.hasNext(); ) {
            assertEquals(it1.next(), it2.next());
        }
        assertFalse(it1.hasNext());
        assertFalse(it2.hasNext());
        comparePackageParts(tip, roundTrip);        
        temp.delete();
        tip.close();
    }
    
    
    /*
    @Test
    public void testPackageSaveToDirectory() throws Exception {
        TIPPackage tip = getSamplePackage("data/test_package.zip");        
        File dir = FileUtil.createTempDir("tiptest");
        System.out.println("Using dir " + dir);
        tip.saveToDirectory(dir);
        TIPRequestPackage roundtrip = TIPPackage.openFromDirectory(dir);
        verifyRequestPackage(roundtrip);
        comparePackageParts(tip, roundtrip);
        assertTrue("Could not clean up package", tip.close());
        assertTrue("Could not clean up package", roundtrip.close());
        // Cleanup our temp directory
        assertTrue("Could not clean up temp directory",
                   FileUtil.recursiveDelete(dir));
    }
    */
    
    private TIPPackage getSamplePackage(String path) throws Exception {
        InputStream is = 
            getClass().getResourceAsStream(path);
        return TIPPackageFactory.openFromStream(is);
    }
    
    private void comparePackageParts(TIPPackage p1, TIPPackage p2) throws Exception {
        assertNotNull(p1.getObjectSections());
        assertNotNull(p2.getObjectSections());
        // XXX Again, this cheats slightly by assuming a particular order
        assertEquals(p1.getObjectSections().size(), 
                     p2.getObjectSections().size());
        Iterator<TIPObjectSection> it1 = p1.getObjectSections().iterator();
        Iterator<TIPObjectSection> it2 = p2.getObjectSections().iterator();
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
    
    static void verifyRequestPackage(TIPPackage tip) {
        assertTrue(tip.isRequest());
    	verifySamplePackage(tip, "urn:uuid:12345-abc-6789-aslkjd-19193la-as9911");
    }

    @SuppressWarnings("serial")
    static void verifySamplePackage(TIPPackage tip, String packageId) {
        assertEquals(packageId, tip.getPackageId());
        assertEquals(new TIPCreator("Test Company", "http://127.0.0.1/test",
                TestTIPManifest.getDate(2011, 4, 9, 22, 45, 0), new TIPTool("TestTool",
                        "http://interoperability-now.org/", "1.0")),
                        tip.getCreator());
        assertEquals(StandardTaskTypeConstants.TRANSLATE_STRICT_BITEXT_URI,
        			 tip.getTaskType());
        assertEquals("en-US", tip.getSourceLocale());
        assertEquals("fr-FR", tip.getTargetLocale());

        // XXX This test is cheating by assuming a particular order,
        // which is not guaranteed
        expectObjectSection(tip, StandardTaskTypeConstants.TranslateStrictBitext.BILINGUAL,
                Collections.singletonList(
                        new TIPObjectFile("Peanut_Butter.xlf", 1)));
        expectObjectSection(tip, StandardTaskTypeConstants.TranslateStrictBitext.PREVIEW,
                new ArrayList<TIPObjectFile>() {
                    {
                        add(new TIPObjectFile(
                                "Peanut_Butter.html.skl", 1));
                        add(new TIPObjectFile(
                                "resources/20px-Padlock-silver.svg.png", 2));
                        add(new TIPObjectFile("resources/load.php", 3));
                        add(new TIPObjectFile(
                                "resources/290px-PeanutButter.jpg", 4));
                        add(new TIPObjectFile(
                                "resources/load(1).php", 5));
                        add(new TIPObjectFile(
                                "resources/magnify-clip.png", 6));
                    }
                });
    }
    
    @SuppressWarnings("serial")
    static void verifyResponsePackage(TIPResponsePackage tip) {
        assertEquals("urn:uuid:84983-zzz-0091-alpppq-184903b-aj1239", tip.getPackageId());
        assertEquals(new TIPCreator("Test Testerson", "http://interoperability-now.org",
                TestTIPManifest.getDate(2011, 4, 18, 19, 3, 15), new TIPTool("Test Workbench",
                        "http://interoperability-now.org", "2.0")),
                        tip.getCreator());
        assertEquals(new TIPCreator("Test Company", "http://127.0.0.1/test",
                TestTIPManifest.getDate(2011, 4, 9, 22, 45, 0), new TIPTool("TestTool",
                        "http://interoperability-now.org/", "1.0")),
                        tip.getRequestCreator());
        assertEquals("urn:uuid:12345-abc-6789-aslkjd-19193la-as9911",
        			 tip.getRequestPackageId());
        assertEquals(StandardTaskTypeConstants.TRANSLATE_STRICT_BITEXT_URI,
        			 tip.getTaskType());
        assertEquals("en-US", tip.getSourceLocale());
        assertEquals("fr-FR", tip.getTargetLocale());

        // XXX This test is cheating by assuming a particular order,
        // which is not guaranteed
        expectObjectSection(tip, StandardTaskTypeConstants.TranslateStrictBitext.BILINGUAL,
                Collections.singletonList(
                        new TIPObjectFile("Peanut_Butter.xlf", 1)));
    }
    
    private static void expectObjectSection(TIPPackage tip,
            String type, List<TIPObjectFile> files) {
        TIPObjectSection section = tip.getObjectSection(type);
        assertNotNull(section);
        assertEquals(type, section.getType());
        assertEquals(files,
                new ArrayList<TIPObjectFile>(section.getObjectFiles()));
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
