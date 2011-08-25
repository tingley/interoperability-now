package com.globalsight.tip;

import org.junit.*;

import com.globalsight.tip.TIPManifest;
import com.globalsight.tip.TIPObjectFile;
import com.globalsight.tip.TIPObjectSection;
import com.globalsight.tip.TIPObjectSectionType;
import com.globalsight.tip.TIPTaskType;
import com.globalsight.tip.TIPTool;

import static org.junit.Assert.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

public class TestTIPManifest {

    @Test
    public void testManifest() throws Exception {
        TIPManifest manifest = new TIPManifest(null);
        manifest.loadFromStream(getClass()
                        .getResourceAsStream("data/peanut_butter.xml"));
        verifyRequestManifest(manifest);
    }
    
    @Test
    public void testManifestSave() throws Exception {
        TIPManifest manifest = new TIPManifest(null);
        manifest.loadFromStream(getClass()
                        .getResourceAsStream("data/peanut_butter.xml"));
        TIPManifest roundtrip = roundtripManifest(manifest);
        verifyRequestManifest(roundtrip);
    }
    
    @Test
    public void testResponseManifest() throws Exception {
        TIPManifest manifest = new TIPManifest(null);
        manifest.loadFromStream(getClass()
                .getResourceAsStream("data/peanut_butter_response.xml"));
        verifySampleResponseManifest(manifest);
        TIPManifest roundtrip = roundtripManifest(manifest);
        verifySampleResponseManifest(roundtrip);
    }
    
    @SuppressWarnings("serial")
    @Test
    public void testNewManifest() throws Exception {
        TIPManifest manifest = TIPManifest.newManifest(null);
        manifest.setCreatorName("Test");
        manifest.setCreatorId("Test Testerson");
        Date date = getDate(2011, 3, 14, 6, 55, 11);
        manifest.setCreatorUpdate(date);
        manifest.setCommunication("FTP");
        TIPTool tool = new TIPTool("TestTool", "urn:test", "1.0");
        manifest.setContributorTool(tool);
        manifest.setTaskType(TIPTaskType.TRANSLATE);
        manifest.setSourceLanguage("en-US");
        manifest.setTargetLanguage("jp-JP");
        // Add a section
        final TIPObjectFile file = 
            new TIPObjectFile("XLIFF", "test.xlf", true);
        TIPObjectSection section = 
            manifest.addObjectSection(TIPObjectSectionType.BILINGUAL);
        section.addObject(file);
        TIPManifest roundtrip = roundtripManifest(manifest);
        assertEquals("Test", roundtrip.getCreatorName());
        assertEquals("Test Testerson", roundtrip.getCreatorId());
        assertEquals(date, roundtrip.getCreatorUpdate());
        assertEquals("FTP", roundtrip.getCommunication());
        assertEquals(tool, roundtrip.getContributorTool());
        assertEquals(TIPTaskType.TRANSLATE, roundtrip.getTaskType());
        assertEquals("en-US", roundtrip.getSourceLanguage());
        assertEquals("jp-JP", roundtrip.getTargetLanguage());
        expectObjectSection(roundtrip, TIPObjectSectionType.BILINGUAL, 10, 
                new ArrayList<TIPObjectFile>() {{
                    add(file);
                }});
    }
    
    private TIPManifest roundtripManifest(TIPManifest src) throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        src.saveToStream(output);
        TIPManifest roundtrip = new TIPManifest(null);
        roundtrip.loadFromStream(
                new ByteArrayInputStream(output.toByteArray()));
        return roundtrip;
    }
    
    
    static void verifyRequestManifest(TIPManifest manifest) {
        verifySampleManifest(manifest, 
                    "12345-abc-6789-aslkjd-19193la-as9911");
    }
    
    static void verifyResponseManifest(TIPManifest manifest) {
        verifySampleManifest(manifest, "84983-zzz-0091-alpppq-184903b-aj1239");
    }
    
    @SuppressWarnings("serial")
    static void verifySampleManifest(TIPManifest manifest, String packageId) {
        assertEquals(packageId, manifest.getPackageId());
        assertEquals("Test Company", manifest.getCreatorName());
        assertEquals("http://127.0.0.1/test", manifest.getCreatorId());
        assertEquals(getDate(2011, 4, 9, 22, 45, 0), 
                manifest.getCreatorUpdate());
        assertEquals("FTP", manifest.getCommunication());
        assertEquals(new TIPTool("TestTool", 
                "http://interoperability-now.org/", "1.0"), 
                manifest.getContributorTool());
        
        assertEquals(TIPTaskType.TRANSLATE, manifest.getTaskType());
        assertEquals("en-US", manifest.getSourceLanguage());
        assertEquals("fr-FR", manifest.getTargetLanguage());
        
        // XXX This test is cheating by assuming a particular order,
        // which is not guaranteed
        expectObjectSection(manifest, TIPObjectSectionType.BILINGUAL, 1,
                new ArrayList<TIPObjectFile>() {{
                    add(new TIPObjectFile("XLIFF", 
                            "Peanut_Butter.xlf", true));
                }});
        expectObjectSection(manifest, TIPObjectSectionType.INPUT, 1,
                new ArrayList<TIPObjectFile>() {{
                    add(new TIPObjectFile("Unknown", 
                            "Peanut_Butter.html.skl", false));
                    add(new TIPObjectFile("Unknown", 
                            "resources/20px-Padlock-silver.svg.png", false));
                    add(new TIPObjectFile("Unknown", 
                            "resources/load.php", false));
                    add(new TIPObjectFile("Unknown", 
                            "resources/290px-PeanutButter.jpg", false));
                    add(new TIPObjectFile("Unknown", 
                            "resources/load(1).php", false));
                    add(new TIPObjectFile("Unknown", 
                            "resources/magnify-clip.png", false));
                }});
    }
    
    static void verifySampleResponseManifest(TIPManifest manifest) {
        // First sample all the normal fields
        verifyResponseManifest(manifest);
        // Then verify the response
        TIPResponse response = manifest.getResponse();
        assertNotNull(response);
        assertEquals("Test Testerson", response.getName());
        assertEquals("http://interoperability-now.org", response.getId());
        assertEquals(getDate(2011, 4, 18, 19, 3, 15), response.getUpdate()); 
        assertEquals(TIPResponse.Message.SUCCESS, response.getMessage());
        assertEquals(new TIPTool("Test Workbench", 
                "http://interoperability-now.org", "2.0"), response.getTool());
        assertEquals("", response.getComment());
    }
    
    /**
     * This follows the Calendar.set() parameter conventions.  
     * Note that month is zero-indexed!
     */
    static Date getDate(int y, int mon, int d, int h, int min, int s) {
        Calendar c = GregorianCalendar.getInstance();
        c.setTimeInMillis(0); // Zero out the ms field or comparison may fail!
        c.set(y, mon, d, h, min, s); // note 0-indexed month
        return c.getTime();
    }
    
    private static void expectObjectSection(TIPManifest manifest,
                                     TIPObjectSectionType type,
                                     int objectSequence,
                                     List<TIPObjectFile> files) {
        Collection<TIPObjectSection> sections = 
            manifest.getObjectSections(type);
        assertEquals(1, sections.size());
        TIPObjectSection section = sections.iterator().next();
        assertNotNull(section);
        assertEquals(type, section.getObjectSectionType());
        expectObjectFiles(files, 
                new ArrayList<TIPObjectFile>(section.getObjectFiles()));        
    }
    
    private static void expectObjectFiles(List<TIPObjectFile> expected, 
                                   List<TIPObjectFile> actual) {
        assertEquals(expected.size(), actual.size());
        for (int i = 0; i < expected.size(); i++) {
            TIPObjectFile e = expected.get(i);
            TIPObjectFile a = actual.get(i);
            assertEquals("Mismatch in file " + i, e.getType(), a.getType());
            assertEquals("Mismatch in file " + i, e.getPath(), a.getPath());
            assertEquals("Mismatch in file " + i, e.isLocalizable(), a.isLocalizable());
        }
    }
}
