package com.globalsight.tip;

import org.junit.*;

import static org.junit.Assert.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

public class TestTIPManifest {

	@Test
	public void testEmptyManifest() throws Exception {
		TIPManifest manifest = TIPManifest.newManifest(null);
		assertNotNull(manifest.getCreator());
		assertNotNull(manifest.getCreator().getTool());
		assertNotNull(manifest.getObjectSections());
	}
	
    @Test
    public void testManifest() throws Exception {
        TIPManifest manifest = new TIPManifest(null);
        manifest.loadFromStream(getClass().getResourceAsStream(
                "data/peanut_butter.xml"));
        verifyRequestManifest(manifest);
    }

    @Test
    public void testManifestSave() throws Exception {
        TIPManifest manifest = new TIPManifest(null);
        manifest.loadFromStream(getClass().getResourceAsStream(
                "data/peanut_butter.xml"));
        TIPManifest roundtrip = roundtripManifest(manifest);
        verifyRequestManifest(roundtrip);
    }

    @Test
    public void testResponseManifest() throws Exception {
        TIPManifest manifest = new TIPManifest(null);
        manifest.loadFromStream(getClass().getResourceAsStream(
                "data/peanut_butter_response.xml"));
        verifySampleResponseManifest(manifest);
        TIPManifest roundtrip = roundtripManifest(manifest);
        verifySampleResponseManifest(roundtrip);
    }

    @Test
    public void testResponseCreationFromRequest() throws Exception {
    	TIPManifest manifest = new TIPManifest(null);
        manifest.loadFromStream(getClass().getResourceAsStream(
                "data/peanut_butter.xml"));
        TIPManifest responseManifest = TIPManifest.newResponseManifest(null, manifest);
        assertTrue(responseManifest.isResponse());
        assertEquals(StandardTaskType.TRANSLATE_STRICT_BITEXT.getType(), 
   		 	 responseManifest.getTask().getTaskType());
        assertEquals("en-US", responseManifest.getTask().getSourceLocale());
        assertEquals("fr-FR", responseManifest.getTask().getTargetLocale());
        // Make sure the internal object was set correctly
        assertEquals(StandardTaskType.TRANSLATE_STRICT_BITEXT, 
        			 responseManifest.getTaskType());
        TIPTaskResponse taskResponse = 
        		(TIPTaskResponse)responseManifest.getTask();
        assertEquals(manifest.getCreator(), taskResponse.getRequestCreator());
        assertEquals(manifest.getPackageId(), taskResponse.getRequestPackageId());
    }
    
    @Test
    public void testNewManifest() throws Exception {
        TIPManifest manifest = TIPManifest.newRequestManifest(null, 
        						StandardTaskType.TRANSLATE_STRICT_BITEXT);
        manifest.setPackageId("urn:uuid:12345");
        manifest.setCreator(new TIPCreator("Test", "Test Testerson", getDate(
                2011, 3, 14, 6, 55, 11), new TIPTool("TestTool", "urn:test",
                "1.0")));
        manifest.getTask().setSourceLocale("en-US");
        manifest.getTask().setTargetLocale("jp-JP");
        // Add a section
        final TIPObjectFile file = 
                new TIPObjectFile("test.xlf", "test.xlf", 1);
        TIPObjectSection section = manifest.addObjectSection("bilingual",
                StandardTaskTypeConstants.TranslateStrictBitext.BILINGUAL);
        section.addObject(file);
        TIPManifest roundtrip = roundtripManifest(manifest);
        assertEquals("urn:uuid:12345", roundtrip.getPackageId());
        assertEquals(manifest.getCreator(), roundtrip.getCreator());
        assertEquals(manifest.getTask(), roundtrip.getTask());
        expectObjectSection(roundtrip, 
        		StandardTaskTypeConstants.TranslateStrictBitext.BILINGUAL,
                Collections.singletonList(file));
    }

    private TIPManifest roundtripManifest(TIPManifest src) throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        src.saveToStream(output);
        TIPManifest roundtrip = new TIPManifest(null);
        roundtrip
                .loadFromStream(new ByteArrayInputStream(output.toByteArray()));
        return roundtrip;
    }

    static void verifyRequestManifest(TIPManifest manifest) {
        verifySampleManifest(manifest, "urn:uuid:12345-abc-6789-aslkjd-19193la-as9911");
    }

    static void verifyResponseManifest(TIPManifest manifest) {
        verifySampleManifest(manifest, "urn:uuid:84983-zzz-0091-alpppq-184903b-aj1239");
    }

    @SuppressWarnings("serial")
    static void verifySampleManifest(TIPManifest manifest, String packageId) {
        assertEquals(packageId, manifest.getPackageId());
        assertEquals(new TIPCreator("Test Company", "http://127.0.0.1/test",
                getDate(2011, 4, 9, 22, 45, 0), new TIPTool("TestTool",
                        "http://interoperability-now.org/", "1.0")),
                manifest.getCreator());
        assertEquals(new TIPTaskRequest(StandardTaskTypeConstants.TRANSLATE_STRICT_BITEXT_URI,
                "en-US", "fr-FR"), manifest.getTask());

        // XXX This test is cheating by assuming a particular order,
        // which is not guaranteed
        expectObjectSection(manifest, StandardTaskTypeConstants.TranslateStrictBitext.BILINGUAL,
                Collections.singletonList(
                        new TIPObjectFile("Peanut_Butter.xlf", 1)));
        expectObjectSection(manifest, StandardTaskTypeConstants.TranslateStrictBitext.PREVIEW,
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

    static void verifySampleResponseManifest(TIPManifest manifest) {
        assertEquals("urn:uuid:84983-zzz-0091-alpppq-184903b-aj1239", 
                     manifest.getPackageId());
        assertEquals(new TIPCreator("Test Testerson", "http://interoperability-now.org",
                getDate(2011, 4, 18, 19, 03, 15), new TIPTool("Test Workbench",
                        "http://interoperability-now.org", "2.0")),
                manifest.getCreator());
        // Then verify the response
        assertNotNull(manifest.getTask());
        assertTrue(manifest.getTask() instanceof TIPTaskResponse);
        assertEquals(new TIPTaskResponse(
        				StandardTaskTypeConstants.TRANSLATE_STRICT_BITEXT_URI,
                        "en-US", 
                        "fr-FR",
                        "urn:uuid:12345-abc-6789-aslkjd-19193la-as9911",
                        new TIPCreator("Test Company",
                                        "http://127.0.0.1/test",
                                        getDate(2011, 4, 9, 22, 45, 0),
                                        new TIPTool("TestTool", "http://interoperability-now.org/", "1.0")),
                        TIPTaskResponse.Message.SUCCESS, ""),
                     manifest.getTask());
        assertEquals(new TIPCreator("Test Testerson", 
                                "http://interoperability-now.org", 
                                getDate(2011, 4, 18, 19, 3, 15), 
                                new TIPTool("Test Workbench", 
                                        "http://interoperability-now.org", "2.0")),
                     manifest.getCreator());
        TIPTaskResponse response = ((TIPTaskResponse)manifest.getTask());
        assertEquals(TIPTaskResponse.Message.SUCCESS, 
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

    private static void expectObjectSection(TIPManifest manifest,
            String type, List<TIPObjectFile> files) {
        TIPObjectSection section = manifest.getObjectSection(type);
        assertNotNull(section);
        assertEquals(type, section.getType());
        assertEquals(files,
                new ArrayList<TIPObjectFile>(section.getObjectFiles()));
    }
}
