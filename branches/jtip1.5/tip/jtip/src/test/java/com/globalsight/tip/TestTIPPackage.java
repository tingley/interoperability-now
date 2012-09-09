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
import com.globalsight.tip.TIPPObjectFile;
import com.globalsight.tip.TIPPObjectSection;
import com.globalsight.tip.TIPP;

import static org.junit.Assert.*;

public class TestTIPPackage {
    
    @Test
    public void testPackageLoad() throws Exception {
        TIPP tip = getSamplePackage("data/test_package.zip");
        verifyRequestPackage(tip);
        for (TIPPObjectFile file : 
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
        TIPP tip = getSamplePackage("data/test_package.zip");
        File temp = File.createTempFile("tiptest", ".zip");
        OutputStream os = new BufferedOutputStream(new FileOutputStream(temp));
        tip.saveToStream(os);
        os.close();
        TIPP roundtrip  = TIPPFactory.openFromStream(
                new BufferedInputStream(new FileInputStream(temp)));
        verifyRequestPackage(roundtrip);
        comparePackageParts(tip, roundtrip);
        temp.delete();
        assertTrue("Could not clean up package", tip.close());
        assertTrue("Could not clean up pacakge", roundtrip.close());
    }
    
    @Test
    public void testResponsePackage() throws Exception {
        TIPP tip = getSamplePackage("data/test_response_package.zip");
        assertFalse(tip.isRequest());
        verifyResponsePackage((ResponseTIPP)tip);
        File temp = File.createTempFile("tiptest", ".zip");
        OutputStream os = new BufferedOutputStream(new FileOutputStream(temp));
        tip.saveToStream(os);
        os.close();
        TIPP roundtrip  = TIPPFactory.openFromStream(
                new BufferedInputStream(new FileInputStream(temp)));
        assertFalse(roundtrip.isRequest());
        verifyResponsePackage((ResponseTIPP)roundtrip);
        comparePackageParts(tip, roundtrip);
        temp.delete();
        assertTrue("Could not clean up package", tip.close());
        assertTrue("Could not clean up pacakge", roundtrip.close());
    }
    
    @Test
    public void testNewPackage() throws Exception {
        WriteableRequestTIPP tip = TIPPFactory.newRequestPackage(StandardTaskType.TRANSLATE_STRICT_BITEXT);
        tip.setCreator(
            new TIPPCreator("testname", "testid", 
                           TestTIPManifest.getDate(2011, 7, 12, 20, 35, 12), 
                           new TIPPTool("jtip", 
                                   "http://code.google.com/p/interoperability-now", "0.14"))
        );
        String requestPackageId = tip.getPackageId();
        assertNotNull(requestPackageId);
        assertTrue(requestPackageId.startsWith("urn:uuid"));
        tip.setSourceLocale("en-US");
        tip.setTargetLocale("fr-FR");
               
        TIPPObjectFile f1 = tip.addSectionObject(
        		StandardTaskTypeConstants.TranslateStrictBitext.BILINGUAL,
        		"test1.xlf", 
        		new ByteArrayInputStream("test".getBytes("UTF-8"))); 
        
        File temp = File.createTempFile("tiptest", ".zip");
        OutputStream os = new FileOutputStream(temp);
        tip.saveToStream(os);
        os.close();
        TIPP roundTrip = 
            TIPPFactory.openFromStream(new FileInputStream(temp));
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
    
    private TIPP getSamplePackage(String path) throws Exception {
        InputStream is = 
            getClass().getResourceAsStream(path);
        return TIPPFactory.openFromStream(is);
    }
    
    private void comparePackageParts(TIPP p1, TIPP p2) throws Exception {
        Set<String> s1 = p1.getSections();
        Set<String> s2 = p2.getSections();
        assertNotNull(s1);
        assertNotNull(s2);
        assertEquals(s1, s2);
        for (String uri : s1) {
        	List<TIPPObjectFile> o1 = p1.getSectionObjects(uri);
        	List<TIPPObjectFile> o2 = p2.getSectionObjects(uri);
        	assertNotNull(o1);
        	assertNotNull(o2);
        	assertEquals(o1, o2);
	        // XXX Again, this cheats slightly by assuming a particular order
            Iterator<TIPPObjectFile> fit1 = o1.iterator();
            Iterator<TIPPObjectFile> fit2 = o2.iterator();
            while (fit1.hasNext()) {
                TIPPObjectFile f1 = fit1.next();
                assertTrue(fit2.hasNext());
                TIPPObjectFile f2 = fit2.next();
                assertEquals(f1, f2);
                InputStream is1 = f1.getInputStream();
                InputStream is2 = f2.getInputStream();
                verifyBytes(is1, is2);
                is1.close();
                is2.close();
            }
        }
    }
    
    static void verifyRequestPackage(TIPP tip) {
        assertTrue(tip.isRequest());
    	verifySamplePackage(tip, "urn:uuid:12345-abc-6789-aslkjd-19193la-as9911");
    }

    @SuppressWarnings("serial")
    static void verifySamplePackage(TIPP tip, String packageId) {
        assertEquals(packageId, tip.getPackageId());
        assertEquals(new TIPPCreator("Test Company", "http://127.0.0.1/test",
                TestTIPManifest.getDate(2011, 4, 9, 22, 45, 0), new TIPPTool("TestTool",
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
                        new TIPPObjectFile("Peanut_Butter.xlf", 1)));
        expectObjectSection(tip, StandardTaskTypeConstants.TranslateStrictBitext.PREVIEW,
                new ArrayList<TIPPObjectFile>() {
                    {
                        add(new TIPPObjectFile(
                                "Peanut_Butter.html.skl", 1));
                        add(new TIPPObjectFile(
                                "resources/20px-Padlock-silver.svg.png", 2));
                        add(new TIPPObjectFile("resources/load.php", 3));
                        add(new TIPPObjectFile(
                                "resources/290px-PeanutButter.jpg", 4));
                        add(new TIPPObjectFile(
                                "resources/load(1).php", 5));
                        add(new TIPPObjectFile(
                                "resources/magnify-clip.png", 6));
                    }
                });
    }
    
    @SuppressWarnings("serial")
    static void verifyResponsePackage(ResponseTIPP tip) {
        assertEquals("urn:uuid:84983-zzz-0091-alpppq-184903b-aj1239", tip.getPackageId());
        assertEquals(new TIPPCreator("Test Testerson", "http://interoperability-now.org",
                TestTIPManifest.getDate(2011, 4, 18, 19, 3, 15), new TIPPTool("Test Workbench",
                        "http://interoperability-now.org", "2.0")),
                        tip.getCreator());
        assertEquals(new TIPPCreator("Test Company", "http://127.0.0.1/test",
                TestTIPManifest.getDate(2011, 4, 9, 22, 45, 0), new TIPPTool("TestTool",
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
                        new TIPPObjectFile("Peanut_Butter.xlf", 1)));
    }
    
    private static void expectObjectSection(TIPP tip,
            String type, List<TIPPObjectFile> files) {
        assertNotNull(tip.getSectionName(type));
        assertEquals(files,
                new ArrayList<TIPPObjectFile>(tip.getSectionObjects(type)));
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
