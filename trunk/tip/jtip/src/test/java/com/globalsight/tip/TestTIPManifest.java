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
        verifySampleManifest(manifest);
    }
    
    @Test
    public void testManifestSave() throws Exception {
        TIPManifest manifest = new TIPManifest(null);
        manifest.loadFromStream(getClass()
                        .getResourceAsStream("data/peanut_butter.xml"));
        TIPManifest roundtrip = roundtripManifest(manifest);
        verifySampleManifest(roundtrip);
    }
    
    @Test
    public void testNewManifest() throws Exception {
        TIPManifest manifest = TIPManifest.newManifest(null);
        manifest.setCreatorName("Test");
        manifest.setCreatorId("Test Testerson");
        manifest.setCreatorUpdate(new Date());
        manifest.setCommunication("FTP");
        manifest.setContributorTool(
                new TIPTool("TestTool", "urn:test", "1.0"));
        manifest.setTaskType(TIPTaskType.TRANSLATE);
        manifest.setSourceLanguage("en-US");
        manifest.setTargetLanguage("jp-JP");
        // Add a section
        TIPObjectSection section = 
            manifest.addObjectSection(TIPObjectSectionType.BILINGUAL);
        section.setObjectSequence(10);
        section.addObject(new TIPObjectFile("XLIFF", "test.xlf", true));
        TIPManifest roundtrip = roundtripManifest(manifest);
        // TODO: compare manifest and roundtrip
    }
    
    private TIPManifest roundtripManifest(TIPManifest src) throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        src.saveToStream(output);
        TIPManifest roundtrip = new TIPManifest(null);
        roundtrip.loadFromStream(
                new ByteArrayInputStream(output.toByteArray()));
        return roundtrip;
    }
    
    @SuppressWarnings("serial")
    static void verifySampleManifest(TIPManifest manifest) {
        assertEquals("12345-abc-6789-aslkjd-19193la-as9911", 
                     manifest.getPackageId());
        assertEquals("Test Company", manifest.getCreatorName());
        assertEquals("http://127.0.0.1/test", manifest.getCreatorId());
        Calendar c = GregorianCalendar.getInstance();
        c.setTimeInMillis(0); // Zero out the ms field or comparison may fail!
        c.set(2011, 4, 9, 22, 45, 0); // note 0-indexed month
        Date d = c.getTime();
        assertEquals(d, manifest.getCreatorUpdate());
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
        expectObjectSection(manifest, TIPObjectSectionType.REFERENCE, 1,
                new ArrayList<TIPObjectFile>() {{
                    add(new TIPObjectFile("Unknown", 
                            "Peanut_Butter.html", false));
                }});
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
        assertEquals(objectSequence, section.getObjectSequence());
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
