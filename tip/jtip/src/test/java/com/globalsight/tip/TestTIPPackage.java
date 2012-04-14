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
import java.util.Set;

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
        for (TIPObjectFile file : 
        	 tip.getSectionObjects(StandardTaskTypeConstants
        			 .TranslateStrictBitext.BILINGUAL)) {
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
               
        TIPObjectFile f1 = tip.addSectionObject(
        		StandardTaskTypeConstants.TranslateStrictBitext.BILINGUAL,
        		"test1.xlf", 
        		new ByteArrayInputStream("test".getBytes("UTF-8"))); 
        
        File temp = File.createTempFile("tiptest", ".zip");
        OutputStream os = new FileOutputStream(temp);
        tip.saveToStream(os);
        os.close();
        TIPPackage roundTrip = 
            TIPPackageFactory.openFromStream(new FileInputStream(temp));
        assertNotNull(roundTrip);
        assertEquals(tip.getPackageId(), roundTrip.getPackageId());
        assertEquals(tip.getCreator(), roundTrip.getCreator());
        assertEquals(tip.getTaskType(), roundTrip.getTaskType());
        assertEquals(tip.getSourceLocale(), roundTrip.getSourceLocale());
        assertEquals(tip.getTargetLocale(), roundTrip.getTargetLocale());
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
        Set<String> s1 = p1.getSections();
        Set<String> s2 = p2.getSections();
        assertNotNull(s1);
        assertNotNull(s2);
        assertEquals(s1, s2);
        for (String uri : s1) {
        	List<TIPObjectFile> o1 = p1.getSectionObjects(uri);
        	List<TIPObjectFile> o2 = p2.getSectionObjects(uri);
        	assertNotNull(o1);
        	assertNotNull(o2);
        	assertEquals(o1, o2);
	        // XXX Again, this cheats slightly by assuming a particular order
            Iterator<TIPObjectFile> fit1 = o1.iterator();
            Iterator<TIPObjectFile> fit2 = o2.iterator();
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
        assertNotNull(tip.getSectionName(type));
        assertEquals(files,
                new ArrayList<TIPObjectFile>(tip.getSectionObjects(type)));
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
