package com.globalsight.tip;

import org.junit.*;

import com.globalsight.tip.TIPPError.Type;

import static org.junit.Assert.*;

import java.io.*;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Set;

import javax.xml.crypto.KeySelector;

public class TestTIPManifest {

	@Test
	public void testEmptyManifest() throws Exception {
		Manifest manifest = Manifest.newManifest(null);
		assertNotNull(manifest.getCreator());
		assertNotNull(manifest.getCreator().getTool());
		assertNotNull(manifest.getObjectSections());
	}
	
    @Test
    public void testManifest() throws Exception {
        Manifest manifest = new Manifest(null);
        TIPPLoadStatus status = new TIPPLoadStatus();
        manifest.loadFromStream(getClass().getResourceAsStream(
                "data/peanut_butter.xml"), status);
        assertEquals(0, status.getAllErrors().size());
        verifyRequestManifest(manifest);
    }
    
    @Test
    public void testInvalidResponseMessage() throws Exception {
        Manifest manifest = new Manifest(null);
        TIPPLoadStatus status = new TIPPLoadStatus();
        try {
            manifest.loadFromStream(getClass().getResourceAsStream(
                    "data/invalid_repsonse_message.xml"), status);
        }
        catch (ReportedException e) {
            // expected
        }
        assertEquals(1, status.getAllErrors().size());
        assertEquals(TIPPErrorSeverity.FATAL, status.getSeverity());
        assertEquals(TIPPError.Type.INVALID_MANIFEST, status.getAllErrors().get(0).getErrorType());
    }
    
    @Test
    public void testInvalidSequenceValue() throws Exception {
        Manifest manifest = new Manifest(null);
        TIPPLoadStatus status = new TIPPLoadStatus();
        try {
            manifest.loadFromStream(getClass().getResourceAsStream(
                    "data/invalid_sequence.xml"), status);
        }
        catch (ReportedException e) {
            // expected
        }
        // This shows up as a validation error
        assertEquals(1, status.getAllErrors().size());
        assertEquals(TIPPErrorSeverity.FATAL, status.getSeverity());
        assertEquals(TIPPError.Type.INVALID_MANIFEST, status.getAllErrors().get(0).getErrorType());
    }

    @Test
    public void testCustomTaskType() throws Exception {
        Manifest manifest = new Manifest(null);
        TIPPLoadStatus status = new TIPPLoadStatus();
        try {
            manifest.loadFromStream(getClass().getResourceAsStream(
                    "data/custom_task.xml"), status);
        }
        catch (ReportedException e) {
            fail();
        }
        assertEquals(0, status.getAllErrors().size());
        assertEquals("http://spartansoftware.com/tasks/test", manifest.getTask().getTaskType());
    }
    
    @Test
    public void testDuplicateSectionInManifest() throws Exception {
        Manifest manifest = new Manifest(null);
        TIPPLoadStatus status = new TIPPLoadStatus();
        try {
            manifest.loadFromStream(getClass().getResourceAsStream(
                    "data/duplicate_section_request.xml"), status);
        }
        catch (ReportedException e) {
            // expected
        }
        assertEquals(1, status.getAllErrors().size());
        assertEquals(TIPPErrorSeverity.ERROR, status.getSeverity());
        assertEquals(TIPPError.Type.DUPLICATE_SECTION_IN_MANIFEST, status.getAllErrors().get(0).getErrorType());
    }
    
    @Test
    public void testDuplicateResourcesInManifest() throws Exception {
        TIPPLoadStatus status = new TIPPLoadStatus();
        Manifest manifest = new Manifest(null);
        manifest.loadFromStream(getClass().getResourceAsStream(
                "data/duplicate_resources.xml"), status);
        new PayloadValidator().validate(manifest, 
                new TestStore(Collections.singleton("bilingual/Peanut_Butter.xlf")), status);
        TestTIPPackage.checkErrors(status, 1);
        assertEquals(TIPPError.Type.DUPLICATE_RESOURCE_IN_MANIFEST, 
                status.getAllErrors().get(0).getErrorType());
    }

    @Test
    public void testInvalidSectionInManifest() throws Exception {
        Manifest manifest = new Manifest(null);
        TIPPLoadStatus status = new TIPPLoadStatus();
        try {
            manifest.loadFromStream(getClass().getResourceAsStream(
                    "data/invalid_section_request.xml"), status);
        }
        catch (ReportedException e) {
            // expected
        }
        assertEquals(1, status.getAllErrors().size());
        assertEquals(TIPPError.Type.INVALID_MANIFEST, status.getAllErrors().get(0).getErrorType());
        assertEquals(TIPPErrorSeverity.FATAL, status.getSeverity());
    }

    @Test
    public void testInvalidSectionForTaskType() throws Exception {
        Manifest manifest = new Manifest(null);
        TIPPLoadStatus status = new TIPPLoadStatus();
        try {
            manifest.loadFromStream(getClass().getResourceAsStream(
                    "data/invalid_section_strict_bitext.xml"), status);
        }
        catch (ReportedException e) {
            // expected
        }
        assertEquals(1, status.getAllErrors().size());
        assertEquals(TIPPError.Type.INVALID_SECTION_FOR_TASK, status.getAllErrors().get(0).getErrorType());
        assertEquals(TIPPErrorSeverity.ERROR, status.getSeverity());
    }
    
    @Test
    public void testManifestSave() throws Exception {
        Manifest manifest = new Manifest(null);
        TIPPLoadStatus status = new TIPPLoadStatus();
        manifest.loadFromStream(getClass().getResourceAsStream(
                "data/peanut_butter.xml"), status);
        assertEquals(0, status.getAllErrors().size());
        status = new TIPPLoadStatus();
        Manifest roundtrip = roundtripManifest(manifest, status);
        assertEquals(0, status.getAllErrors().size());
        verifyRequestManifest(roundtrip);
    }

    @Test
    public void testResponseManifest() throws Exception {
        Manifest manifest = new Manifest(null);
        TIPPLoadStatus status = new TIPPLoadStatus();
        manifest.loadFromStream(getClass().getResourceAsStream(
                "data/peanut_butter_response.xml"), status);
        assertEquals(0, status.getAllErrors().size());
        verifySampleResponseManifest(manifest);
        status = new TIPPLoadStatus();
        Manifest roundtrip = roundtripManifest(manifest, status);
        assertEquals(0, status.getAllErrors().size());
        verifySampleResponseManifest(roundtrip);
    }

    @Test
    public void testResponseCreationFromRequest() throws Exception {
        TIPPLoadStatus status = new TIPPLoadStatus();
    	TIPP requestPackage = getSamplePackage("data/test_package.zip", status);
    	TestTIPPackage.checkErrors(status, 0);
        Manifest responseManifest = Manifest.newResponseManifest(null, requestPackage);
        assertFalse(responseManifest.isRequest());
        assertEquals(StandardTaskType.TRANSLATE_STRICT_BITEXT.getType(), 
   		 	 responseManifest.getTask().getTaskType());
        assertEquals("en-US", responseManifest.getTask().getSourceLocale());
        assertEquals("fr-FR", responseManifest.getTask().getTargetLocale());
        // Make sure the internal object was set correctly
        assertEquals(StandardTaskType.TRANSLATE_STRICT_BITEXT, 
        			 responseManifest.getTaskType());
        TIPPTaskResponse taskResponse = 
        		(TIPPTaskResponse)responseManifest.getTask();
        assertEquals(requestPackage.getCreator(), taskResponse.getRequestCreator());
        assertEquals(requestPackage.getPackageId(), taskResponse.getRequestPackageId());
    }
    
    @Test
    public void testManifestSignature() throws Exception {
        Manifest manifest = new Manifest(null);
        TIPPLoadStatus status = new TIPPLoadStatus();
        manifest.loadFromStream(getClass().getResourceAsStream(
                "data/peanut_butter.xml"), status);
        assertEquals(0, status.getAllErrors().size());
        status = new TIPPLoadStatus();
        File temp = File.createTempFile("tipp", ".xml");
        System.out.println("Using: " + temp);
        FileOutputStream fos = new FileOutputStream(temp);
        ManifestWriter mw = new ManifestWriter();
        // Generate a dummy keypair
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("DSA");
        kpg.initialize(512);
        KeyPair kp = kpg.generateKeyPair();
        mw.setKeyPair(kp);
        mw.saveToStream(manifest, fos);
        fos.flush();
        fos.close();
        
        // Make sure that the signer gives us a warning if the signature
        // exists but we don't provide the key
        Manifest roundtrip = new Manifest(null);
        TIPPLoadStatus roundtripStatus = new TIPPLoadStatus();
        FileInputStream fis = new FileInputStream(temp);
        roundtrip.loadFromStream(fis, roundtripStatus);
        TestUtils.expectLoadStatus(roundtripStatus, 1, TIPPErrorSeverity.WARN);
        assertEquals(Type.UNABLE_TO_VERIFY_SIGNATURE, 
                roundtripStatus.getAllErrors().get(0).getErrorType());
        
        // Now verify the signature for real
        roundtrip = new Manifest(null);
        roundtripStatus = new TIPPLoadStatus();
        fis = new FileInputStream(temp);
        roundtrip.loadFromStream(fis, roundtripStatus, KeySelector.singletonKeySelector(kp.getPublic()),
                                 null);
        TestUtils.expectLoadStatus(roundtripStatus, 0, TIPPErrorSeverity.NONE);
    }
    
    @Test
    public void testNewManifest() throws Exception {
        Manifest manifest = Manifest.newRequestManifest(null, 
        						StandardTaskType.TRANSLATE_STRICT_BITEXT);
        manifest.setPackageId("urn:uuid:12345");
        manifest.setCreator(new TIPPCreator("Test", "Test Testerson", getDate(
                2011, 3, 14, 6, 55, 11), new TIPPTool("TestTool", "urn:test",
                "1.0")));
        manifest.getTask().setSourceLocale("en-US");
        manifest.getTask().setTargetLocale("jp-JP");
        // Add a section
        final TIPPObjectFile file = 
                new TIPPObjectFile("test.xlf", "test.xlf");
        TIPPObjectSection section = manifest.addObjectSection("bilingual",
                TIPPObjectSectionType.BILINGUAL);
        section.addObject(file);
        TIPPLoadStatus status = new TIPPLoadStatus();
        Manifest roundtrip = roundtripManifest(manifest, status);
        assertEquals(0, status.getAllErrors().size());
        assertEquals("urn:uuid:12345", roundtrip.getPackageId());
        assertEquals(manifest.getCreator(), roundtrip.getCreator());
        assertEquals(manifest.getTask(), roundtrip.getTask());
        expectObjectSection(roundtrip, 
                TIPPObjectSectionType.BILINGUAL,
                Collections.singletonList(file));
    }

    private Manifest roundtripManifest(Manifest src, TIPPLoadStatus status) throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        new ManifestWriter().saveToStream(src, output);
        // TODO: write it out to take a look
        // - failing at a minimum because I'm putting the task inside the descriptor
        Manifest roundtrip = new Manifest(null);
        roundtrip
                .loadFromStream(new ByteArrayInputStream(output.toByteArray()), status);
        return roundtrip;
    }

    static void verifyRequestManifest(Manifest manifest) {
        verifySampleManifest(manifest, "urn:uuid:12345-abc-6789-aslkjd-19193la-as9911");
    }

    static void verifyResponseManifest(Manifest manifest) {
        verifySampleManifest(manifest, "urn:uuid:84983-zzz-0091-alpppq-184903b-aj1239");
    }

    @SuppressWarnings("serial")
    static void verifySampleManifest(Manifest manifest, String packageId) {
        assertEquals(packageId, manifest.getPackageId());
        assertEquals(new TIPPCreator("Test Company", "http://127.0.0.1/test",
                getDate(2011, 4, 9, 22, 45, 0), new TIPPTool("TestTool",
                        "http://interoperability-now.org/", "1.0")),
                manifest.getCreator());
        assertEquals(new TIPPTaskRequest(StandardTaskType.TRANSLATE_STRICT_BITEXT.getType(),
                "en-US", "fr-FR"), manifest.getTask());

        // XXX This test is cheating by assuming a particular order,
        // which is not guaranteed
        expectObjectSection(manifest, TIPPObjectSectionType.BILINGUAL,
                Collections.singletonList(
                        new TIPPObjectFile("Peanut_Butter.xlf")));
        expectObjectSection(manifest, TIPPObjectSectionType.PREVIEW,
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

    static void verifySampleResponseManifest(Manifest manifest) {
        assertEquals("urn:uuid:84983-zzz-0091-alpppq-184903b-aj1239", 
                     manifest.getPackageId());
        assertEquals(new TIPPCreator("Test Testerson", "http://interoperability-now.org",
                getDate(2011, 4, 18, 19, 03, 15), new TIPPTool("Test Workbench",
                        "http://interoperability-now.org", "2.0")),
                manifest.getCreator());
        // Then verify the response
        assertNotNull(manifest.getTask());
        assertTrue(manifest.getTask() instanceof TIPPTaskResponse);
        assertEquals(new TIPPTaskResponse(
        				StandardTaskType.TRANSLATE_STRICT_BITEXT.getType(),
                        "en-US", 
                        "fr-FR",
                        "urn:uuid:12345-abc-6789-aslkjd-19193la-as9911",
                        new TIPPCreator("Test Company",
                                        "http://127.0.0.1/test",
                                        getDate(2011, 4, 9, 22, 45, 0),
                                        new TIPPTool("TestTool", "http://interoperability-now.org/", "1.0")),
                        TIPPResponseMessage.Success, ""),
                     manifest.getTask());
        assertEquals(new TIPPCreator("Test Testerson", 
                                "http://interoperability-now.org", 
                                getDate(2011, 4, 18, 19, 3, 15), 
                                new TIPPTool("Test Workbench", 
                                        "http://interoperability-now.org", "2.0")),
                     manifest.getCreator());
        TIPPTaskResponse response = ((TIPPTaskResponse)manifest.getTask());
        assertEquals(TIPPResponseMessage.Success, 
                response.getMessage());
        assertEquals("", response.getComment());
        // TODO: verify response
    }

    /**
     * This follows the Calendar.set() parameter conventions. Note that month is
     * zero-indexed!
     */
    static Date getDate(int y, int mon, int d, int h, int min, int s) {
        Calendar c = GregorianCalendar.getInstance();
        c.setTimeInMillis(0); // Zero out the ms field or comparison may fail!
        c.set(y, mon, d, h, min, s); // note 0-indexed month
        return c.getTime();
    }

    private static void expectObjectSection(Manifest manifest,
            TIPPObjectSectionType type, List<TIPPObjectFile> files) {
        TIPPObjectSection section = manifest.getObjectSection(type);
        assertNotNull(section);
        assertEquals(type, section.getType());
        assertEquals(files,
                new ArrayList<TIPPObjectFile>(section.getObjectFiles()));
    }
    
    private TIPP getSamplePackage(String path, TIPPLoadStatus status) throws Exception {
        InputStream is = 
            getClass().getResourceAsStream(path);
        return TIPPFactory.openFromStream(is, new InMemoryBackingStore(), status);
    }

    /**
     * Dummy backing store that exposes 0-length input streams
     * for resources with the specified paths.
     */
    class TestStore extends InMemoryBackingStore {
        private Set<String> paths;
        TestStore(Set<String> paths) {
            this.paths = paths;
        }
        @Override
        public InputStream getObjectFileData(String path) {
            if (paths.contains(path)) {
                return new ByteArrayInputStream(new byte[0]);
            }
            return null;
        }
        @Override
        public Set<String> getObjectFilePaths() {
            return paths;
        }
    }
}
