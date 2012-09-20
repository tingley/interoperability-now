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
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.junit.*;

import com.globalsight.tip.TIPPObjectFile;
import com.globalsight.tip.TIPP;

import static org.junit.Assert.*;

public class TestTIPPackage {
    
    public static void checkErrors(TIPPLoadStatus status, int expectedErrorCount) {
        if (expectedErrorCount != status.getAllErrors().size()) {
            System.out.println("Expected " + expectedErrorCount + 
                    " errors but found " + status.getAllErrors().size());
            for (TIPPError error : status.getAllErrors()) {
                System.out.println("> " + error);
            }
        }
        assertEquals(expectedErrorCount, status.getAllErrors().size());
    }
    
    @Test
    public void testPackageLoad() throws Exception {
        TIPPLoadStatus status = new TIPPLoadStatus();
        TIPP tip = getSamplePackage("data/test_package.zip", status);
        checkErrors(status, 0);
        verifyRequestPackage(tip);
        for (TIPPObjectFile file : 
        	 tip.getSectionObjects(TIPPObjectSectionType.BILINGUAL)) {
            // Just instantiating the input stream is the real test..
            InputStream is = file.getInputStream();
            assertNotNull(is);
            is.close();
        }
        assertTrue("Could not clean up package", tip.close());
    }
    
    @Test
    public void testVerifyMissingManifest() throws Exception {
        TIPPLoadStatus status = new TIPPLoadStatus();
        TIPP tipp = getSamplePackage("data/missing_manifest.zip", status);
        assertNull(tipp);
        checkErrors(status, 1);
        assertEquals(TIPPErrorSeverity.FATAL, status.getSeverity());
        TIPPError error = status.getAllErrors().get(0);
        assertEquals(TIPPError.Type.MISSING_MANIFEST, error.getErrorType());
    }
    
    @Test
    public void testVerifyCorruptManifest() throws Exception {
        TIPPLoadStatus status = new TIPPLoadStatus();
        TIPP tipp = getSamplePackage("data/corrupt_manifest.zip", status);
        assertNull(tipp);
        checkErrors(status, 1);
        assertEquals(TIPPErrorSeverity.FATAL, status.getSeverity());
        assertEquals(TIPPError.Type.CORRUPT_MANIFEST, status.getAllErrors().get(0).getErrorType());
    }
    
    @Test
    public void testVerifyMissingPayload() throws Exception {
        TIPPLoadStatus status = new TIPPLoadStatus();
        TIPP tipp = getSamplePackage("data/missing_payload.zip", status);
        // manifest is intact, so we should get a TIPP object
        assertNotNull(tipp);
        checkErrors(status, 7);
        assertEquals(TIPPErrorSeverity.ERROR, status.getSeverity());
        assertEquals(TIPPError.Type.MISSING_PAYLOAD_RESOURCE, status.getAllErrors().get(0).getErrorType());
        assertEquals(TIPPError.Type.MISSING_PAYLOAD_RESOURCE, status.getAllErrors().get(1).getErrorType());
        assertEquals(TIPPError.Type.MISSING_PAYLOAD_RESOURCE, status.getAllErrors().get(2).getErrorType());
        assertEquals(TIPPError.Type.MISSING_PAYLOAD_RESOURCE, status.getAllErrors().get(3).getErrorType());
        assertEquals(TIPPError.Type.MISSING_PAYLOAD_RESOURCE, status.getAllErrors().get(4).getErrorType());
        assertEquals(TIPPError.Type.MISSING_PAYLOAD_RESOURCE, status.getAllErrors().get(5).getErrorType());
        assertEquals(TIPPError.Type.MISSING_PAYLOAD_RESOURCE, status.getAllErrors().get(6).getErrorType());
    }
    
    @Test
    public void testVerifyCorruptPayloadZip() throws Exception {
        TIPPLoadStatus status = new TIPPLoadStatus();
        TIPP tipp = getSamplePackage("data/corrupt_payload_zip.zip", status);
        assertNotNull(tipp);
        checkErrors(status, 7);
        assertEquals(TIPPErrorSeverity.ERROR, status.getSeverity());
        for (TIPPError error : status.getAllErrors()) {
            assertEquals(TIPPError.Type.MISSING_PAYLOAD_RESOURCE, error.getErrorType());
        }
    }

    @Test
    public void testManifestPayloadMismatch() throws Exception {
        TIPPLoadStatus status = new TIPPLoadStatus();
        TIPP tipp = getSamplePackage("data/manifest_payload_mismatch.zip", status);
        assertNotNull(tipp);
        checkErrors(status, 2);
        assertEquals(TIPPError.Type.MISSING_PAYLOAD_RESOURCE, status.getAllErrors().get(0).getErrorType());
        assertEquals(TIPPError.Type.UNEXPECTED_PAYLOAD_RESOURCE, status.getAllErrors().get(1).getErrorType());
    }
    
    @Test
    public void testVerifyCorruptPackageZip() throws Exception {
        TIPPLoadStatus status = new TIPPLoadStatus();
        TIPP tipp = getSamplePackage("data/corrupt_package_zip.zip", status);
        assertNull(tipp);
        assertEquals(1, status.getAllErrors().size());
        assertEquals(TIPPErrorSeverity.FATAL, status.getSeverity());
        TIPPError error = status.getAllErrors().get(0);
        assertEquals(TIPPError.Type.MISSING_MANIFEST, error.getErrorType());
    }
    
    @Test
    public void testPackageSave() throws Exception {
        // Load the package, save it out to a zip file, read it back.
        TIPPLoadStatus status = new TIPPLoadStatus();
        TIPP tip = getSamplePackage("data/test_package.zip", status);
        assertEquals(0, status.getAllErrors().size());
        File temp = File.createTempFile("tiptest", ".zip");
        OutputStream os = new BufferedOutputStream(new FileOutputStream(temp));
        tip.saveToStream(os);
        os.close();
        status = new TIPPLoadStatus();
        TIPP roundtrip  = TIPPFactory.openFromStream(
                new BufferedInputStream(new FileInputStream(temp)),
                new InMemoryBackingStore(), status);
        assertEquals(0, status.getAllErrors().size());
        verifyRequestPackage(roundtrip);
        comparePackageParts(tip, roundtrip);
        temp.delete();
        assertTrue("Could not clean up package", tip.close());
        assertTrue("Could not clean up pacakge", roundtrip.close());
    }
    
    @Test
    public void testResponsePackage() throws Exception {
        TIPPLoadStatus status = new TIPPLoadStatus();
        TIPP tip = getSamplePackage("data/test_response_package.zip", status);
        checkErrors(status, 0);
        assertFalse(tip.isRequest());
        verifyResponsePackage((ResponseTIPP)tip);
        File temp = File.createTempFile("tiptest", ".zip");
        OutputStream os = new BufferedOutputStream(new FileOutputStream(temp));
        tip.saveToStream(os);
        os.close();
        status = new TIPPLoadStatus();
        TIPP roundtrip  = TIPPFactory.openFromStream(
                new BufferedInputStream(new FileInputStream(temp)), 
                new InMemoryBackingStore(), status);
        assertEquals(0, status.getAllErrors().size());
        assertFalse(roundtrip.isRequest());
        verifyResponsePackage((ResponseTIPP)roundtrip);
        comparePackageParts(tip, roundtrip);
        temp.delete();
        assertTrue("Could not clean up package", tip.close());
        assertTrue("Could not clean up pacakge", roundtrip.close());
    }
    
    @Test
    public void testNewPackage() throws Exception {
        PackageStore store = new InMemoryBackingStore();
        WriteableRequestTIPP tip = TIPPFactory.newRequestPackage(StandardTaskType.TRANSLATE_STRICT_BITEXT, store);
        tip.setCreator(
            new TIPPCreator("testname", "testid", 
                           TestTIPManifest.getDate(2011, 7, 12, 20, 35, 12), 
                           new TIPPTool("jtip", 
                                   "http://code.google.com/p/interoperability-now", "0.15"))
        );
        String requestPackageId = tip.getPackageId();
        assertNotNull(requestPackageId);
        assertTrue(requestPackageId.startsWith("urn:uuid"));
        tip.setSourceLocale("en-US");
        tip.setTargetLocale("fr-FR");
               
        tip.addSectionObject(TIPPObjectSectionType.BILINGUAL,
        		"test1.xlf", 
        		new ByteArrayInputStream("test".getBytes("UTF-8"))); 
        
        File temp = File.createTempFile("tiptest", ".zip");
        OutputStream os = new FileOutputStream(temp);
        tip.saveToStream(os);
        os.close();
        TIPPLoadStatus status = new TIPPLoadStatus();
        TIPP roundTrip = 
            TIPPFactory.openFromStream(new FileInputStream(temp), new InMemoryBackingStore(), status);
        assertEquals(0, status.getAllErrors().size());
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
    
    private TIPP getSamplePackage(String path, TIPPLoadStatus status) throws Exception {
        InputStream is = 
            getClass().getResourceAsStream(path);
        return TIPPFactory.openFromStream(is, new InMemoryBackingStore(), status);
    }
    
    private void comparePackageParts(TIPP p1, TIPP p2) throws Exception {
        Set<TIPPObjectSectionType> s1 = p1.getSections();
        Set<TIPPObjectSectionType> s2 = p2.getSections();
        assertNotNull(s1);
        assertNotNull(s2);
        assertEquals(s1, s2);
        for (TIPPObjectSectionType type : s1) {
        	List<TIPPObjectFile> o1 = p1.getSectionObjects(type);
        	List<TIPPObjectFile> o2 = p2.getSectionObjects(type);
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
        assertEquals(StandardTaskType.TRANSLATE_STRICT_BITEXT.getType(),
        			 tip.getTaskType());
        assertEquals("en-US", tip.getSourceLocale());
        assertEquals("fr-FR", tip.getTargetLocale());

        // XXX This test is cheating by assuming a particular order,
        // which is not guaranteed
        expectObjectSection(tip, TIPPObjectSectionType.BILINGUAL,
                Collections.singletonList(
                        new TIPPObjectFile("Peanut_Butter.xlf", 1)));
        expectObjectSection(tip, TIPPObjectSectionType.PREVIEW,
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
        assertEquals(StandardTaskType.TRANSLATE_STRICT_BITEXT.getType(),
        			 tip.getTaskType());
        assertEquals("en-US", tip.getSourceLocale());
        assertEquals("fr-FR", tip.getTargetLocale());

        // XXX This test is cheating by assuming a particular order,
        // which is not guaranteed
        expectObjectSection(tip, TIPPObjectSectionType.BILINGUAL,
                Collections.singletonList(
                        new TIPPObjectFile("Peanut_Butter.xlf", 1)));
    }
    
    private static void expectObjectSection(TIPP tip,
            TIPPObjectSectionType type, List<TIPPObjectFile> files) {
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
