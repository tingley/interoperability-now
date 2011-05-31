package com.globalsight.tip;

import static com.globalsight.tip.TIPConstants.*;
import static com.globalsight.tip.XMLUtil.*;
import static javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.globalsight.tip.TIPConstants.ContributorTool;
import com.globalsight.tip.TIPConstants.Creator;
import com.globalsight.tip.TIPConstants.ObjectFile;
import com.globalsight.tip.TIPConstants.OrderTask;

public class TIPManifest {

    private TIPPackage tipPackage;
    private String packageId;
    private String creatorName;
    private String creatorId;
    private Date creatorUpdate;
    private String communication;
    private TIPTool contributorTool;
    private TIPTaskType taskType;
    private String sourceLanguage;
    private String targetLanguage;
    private TIPResponse response;
    private Map<TIPObjectSectionType, List<TIPObjectSection>> objectSections = 
        new EnumMap<TIPObjectSectionType, 
                    List<TIPObjectSection>>(TIPObjectSectionType.class);    
    
    TIPManifest(TIPPackage tipPackage) {
        this.tipPackage = tipPackage;
        for (TIPObjectSectionType type : TIPObjectSectionType.values()) {
            objectSections.put(type, new ArrayList<TIPObjectSection>());
        }
    }

    static TIPManifest newManifest(TIPPackage tipPackage) {
        TIPManifest manifest = new TIPManifest(tipPackage);
        manifest.setPackageId(UUID.randomUUID().toString());
        return manifest;
    }
    
    void saveToStream(OutputStream saveStream) 
            throws IOException, SAXException, TransformerException, 
                   ParserConfigurationException {
        Document document = new ManifestDOMBuilder(this).makeDocument();
        validate(document);
        TransformerFactory factory = TransformerFactory.newInstance();
        Transformer transformer = factory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        transformer.transform(new DOMSource(document), new StreamResult(saveStream));
    }
    
    // TODO: exceptions, of course
    // XXX This should blow away any existing settings 
    void loadFromStream(InputStream manifestStream) throws SAXException, IOException, ParserConfigurationException {
        Document document = parse(manifestStream);
        validate(document);
        loadManifest(document);
    }    
    
    private void loadManifest(Document document) {
        Element manifest = getFirstChildElement(document);
        loadDescriptor(getFirstChildByName(manifest, GLOBAL_DESCRIPTOR));
        loadPackageObjects(getFirstChildByName(manifest, PACKAGE_OBJECTS));
    }
    
    private void loadDescriptor(Element descriptor) {
        packageId = getChildTextByName(descriptor, UNIQUE_PACKAGE_ID);
        loadCreator(getFirstChildByName(descriptor, PACKAGE_CREATOR));
        loadAction(getFirstChildByName(descriptor, ORDER_ACTION));
    }
    
    private void loadCreator(Element creator) {
        creatorName = getChildTextByName(creator, Creator.NAME);
        creatorId = getChildTextByName(creator, Creator.ID);
        creatorUpdate = 
            loadDate(getFirstChildByName(creator, Creator.UPDATE));
        communication = getChildTextByName(creator, Creator.COMMUNICATION);
        contributorTool = loadContributorTool(
                getFirstChildByName(creator, CONTRIBUTOR_TOOL));
    }
    
    private void loadAction(Element action) {
        loadTask(getFirstChildByName(action, ORDER_TASK));
        Element responseEl = getFirstChildByName(action, ORDER_RESPONSE);
        if (responseEl != null) {
            response = loadResponse(responseEl);
        }
    }
    
    private void loadTask(Element task) {
        String rawType = getChildTextByName(task, OrderTask.TYPE);
        taskType = TIPTaskType.fromValue(rawType);
        if (taskType == null) {
            throw new IllegalStateException("Invalid task type '" + rawType + 
                                            "'");
        }
        sourceLanguage = getChildTextByName(task, OrderTask.SOURCE_LANGUAGE);
        targetLanguage = getChildTextByName(task, OrderTask.TARGET_LANGUAGE);
    }
    
    private TIPResponse loadResponse(Element responseEl) {
        TIPResponse response = new TIPResponse();
        response.setName(getChildTextByName(responseEl, OrderResponse.NAME));
        response.setId(getChildTextByName(responseEl, OrderResponse.ID));
        response.setComment(getChildTextByName(responseEl, 
                            OrderResponse.COMMENT));
        String rawDate = getChildTextByName(responseEl, OrderResponse.UPDATE);
        response.setUpdate(DateUtil.parseTIPDate(rawDate));
        String rawMessage = getChildTextByName(responseEl, 
                            OrderResponse.MESSAGE);
        TIPResponse.Message msg = TIPResponse.Message.fromValue(rawMessage);
        if (msg == null) {
            throw new IllegalArgumentException(
                    "Invalid ResponseMessage value: " + msg);
        }
        response.setMessage(msg);
        response.setTool(loadContributorTool(
                getFirstChildByName(responseEl, CONTRIBUTOR_TOOL)));
        return response;
    }
    
    private Date loadDate(Element dateNode) {
        return DateUtil.parseTIPDate(getTextContent(dateNode));
    }
    
    private TIPTool loadContributorTool(Element toolEl) {
        TIPTool tool = new TIPTool();
        tool.setName(getChildTextByName(toolEl, ContributorTool.NAME));
        tool.setId(getChildTextByName(toolEl, ContributorTool.ID));
        tool.setVersion(getChildTextByName(toolEl, ContributorTool.VERSION));
        return tool;
    }
    
    private void loadPackageObjects(Element parent) {
        // parse all the sections
        NodeList children = 
            parent.getElementsByTagName(PACKAGE_OBJECT_SECTION);
        for (int i = 0; i < children.getLength(); i++) {
            TIPObjectSection section = 
                loadPackageObjectSection((Element)children.item(i));
            objectSections.get(section.getObjectSectionType()).add(section);
        }
    }
    
    private TIPObjectSection loadPackageObjectSection(Element section) {
        TIPObjectSection objectSection = new TIPObjectSection();
        String sectionName = section.getAttribute(ATTR_SECTION_NAME);
        objectSection.setObjectSectionType(
                TIPObjectSectionType.fromValue(sectionName));
        if (objectSection.getObjectSectionType() == null) {
            throw new IllegalStateException("Invalid sectionname: '" + 
                                            sectionName + "'");
        }
        String rawSequence = getChildTextByName(section, OBJECT_SEQUENCE);
        try {
            objectSection.setObjectSequence(Integer.valueOf(rawSequence));
        }
        catch (NumberFormatException e) {
            throw new IllegalStateException("Invalid sequence number: '" + 
                    rawSequence + "'");
        }
        NodeList children = section.getElementsByTagName(OBJECT_FILE);
        for (int i = 0; i < children.getLength(); i++) {
            objectSection.addObject(loadObjectFile((Element)children.item(i)));
        }
        return objectSection;
    }
    
    private TIPObjectFile loadObjectFile(Element file) {
        TIPObjectFile object = new TIPObjectFile();
        object.setPackage(tipPackage);
        String rawLocalizable = file.getAttribute(ObjectFile.ATTR_LOCALIZABLE);
        if (rawLocalizable.equals(YES)) {
            object.setLocalizable(true);
        }
        else if (rawLocalizable.equals(NO)) {
            object.setLocalizable(false);
        }
        else {
            throw new IllegalStateException("Invalid yes/no value: '" + 
                                            rawLocalizable + "'");
        }
        object.setType(getChildTextByName(file, ObjectFile.TYPE));
        object.setPath(getChildTextByName(file, ObjectFile.LOCATION_PATH));
        return object;
    }

    Document parse(InputStream is) throws ParserConfigurationException, SAXException, IOException {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            return builder.parse(is);
        }
        finally {
            is.close();
        }
    }
    
    void validate(Document dom) throws SAXException, IOException {
        InputStream is = getClass().getResourceAsStream("/TIPManifest-1-2.xsd");
        SchemaFactory factory = SchemaFactory.newInstance(W3C_XML_SCHEMA_NS_URI);
        Schema schema = factory.newSchema(new StreamSource(is));
        // need an error handler
        Validator validator = schema.newValidator();
        validator.validate(new DOMSource(dom));
        is.close();
    }

    public String getPackageId() {
        return packageId;
    }

    public void setPackageId(String packageId) {
        this.packageId = packageId;
    }

    public String getCreatorName() {
        return creatorName;
    }

    public void setCreatorName(String creatorName) {
        this.creatorName = creatorName;
    }

    public String getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(String creatorId) {
        this.creatorId = creatorId;
    }

    public Date getCreatorUpdate() {
        return creatorUpdate;
    }

    public void setCreatorUpdate(Date creatorUpdate) {
        this.creatorUpdate = creatorUpdate;
    }

    public String getCommunication() {
        return communication;
    }

    public void setCommunication(String communication) {
        this.communication = communication;
    }
    
    public TIPTool getContributorTool() {
        return contributorTool;
    }
    
    public void setContributorTool(TIPTool tool) {
        this.contributorTool = tool;
    }

    public TIPTaskType getTaskType() {
        return taskType;
    }

    public void setTaskType(TIPTaskType taskType) {
        this.taskType = taskType;
    }

    public String getSourceLanguage() {
        return sourceLanguage;
    }

    public void setSourceLanguage(String sourceLanguage) {
        this.sourceLanguage = sourceLanguage;
    }

    public String getTargetLanguage() {
        return targetLanguage;
    }

    public void setTargetLanguage(String targetLanguage) {
        this.targetLanguage = targetLanguage;
    }

    /**
     * Return the response object, or null if this is a task-only manifest.
     * @return response, or null
     */
    public TIPResponse getResponse() {
        return response;
    }
    
    public void setResponse(TIPResponse response) {
        this.response = response;
    }
    
    /**
     * Return a collection of all object sections with a given type.  
     * @param type section type
     * @return (possibly empty) collection of object sections
     */
    public Collection<TIPObjectSection> getObjectSections(
                                TIPObjectSectionType type) {
        return objectSections.get(type);
    }
    
    /**
     * Return a collection of all object sections.
     * @return (possibly empty) collection of object sections
     */
    public Collection<TIPObjectSection> getObjectSections() {
        List<TIPObjectSection> merged = new ArrayList<TIPObjectSection>();
        for (TIPObjectSectionType type : TIPObjectSectionType.values()) {
            merged.addAll(objectSections.get(type));
        }
        return merged;
    }
    
    public TIPObjectSection addObjectSection(TIPObjectSectionType type) {
        TIPObjectSection section = new TIPObjectSection();
        section.setObjectSectionType(type);
        objectSections.get(type).add(section);
        return section;
    }
    
}
