package com.globalsight.tip;

import static com.globalsight.tip.TIPPConstants.*;
import static com.globalsight.tip.XMLUtil.*;
import static javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
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

import com.globalsight.tip.TIPPConstants.ContributorTool;
import com.globalsight.tip.TIPPConstants.Creator;
import com.globalsight.tip.TIPPConstants.ObjectFile;

class Manifest {

	// Only for construction
	// XXX Should this go in the TIPTask somehow?
	private TIPPTaskType taskType;
	
    private PackageBase tipPackage;
    private String packageId;
    private TIPPTask task; // Either request or response
    private TIPPCreator creator = new TIPPCreator();
    
    private Map<TIPPObjectSectionType, TIPPObjectSection> objectSections = 
        new HashMap<TIPPObjectSectionType, TIPPObjectSection>();    
    
    Manifest(PackageBase tipPackage) {
        this.tipPackage = tipPackage;
    }

    static Manifest newManifest(PackageBase tipPackage) {
        Manifest manifest = new Manifest(tipPackage);
        manifest.setPackageId("urn:uuid:" + UUID.randomUUID().toString());
        return manifest;
    }
    
    static Manifest newRequestManifest(PackageBase tipPackage, TIPPTaskType type) {
    	Manifest manifest = newManifest(tipPackage);
    	TIPPTaskRequest request = new TIPPTaskRequest();
    	request.setTaskType(type.getType());
    	manifest.setTaskType(type);
    	manifest.setTask(request);
    	return manifest;
    }
    
    static Manifest newResponseManifest(PackageBase tipPackage, TIPPTaskType type) {
    	Manifest manifest = newManifest(tipPackage);
    	TIPPTaskResponse response = new TIPPTaskResponse();
    	response.setTaskType(type.getType());
    	manifest.setTaskType(type);
    	manifest.setTask(response);
    	return manifest;
    }
    
    static Manifest newResponseManifest(WriteableResponseTIPP tipPackage, 
    									   TIPP requestPackage) {
    	if (!requestPackage.isRequest()) {
    		throw new IllegalArgumentException(
    				"Can't construct a response to a response package");
    	}
    	Manifest manifest = newManifest(tipPackage);
    	// Copy all the fields over.  Tedious.
    	TIPPTaskResponse response = new TIPPTaskResponse();
    	response.setRequestCreator(requestPackage.getCreator());
    	response.setRequestPackageId(requestPackage.getPackageId());
    	response.setTaskType(requestPackage.getTaskType());
    	response.setSourceLocale(requestPackage.getSourceLocale());
    	response.setTargetLocale(requestPackage.getTargetLocale());
    	manifest.setTask(response);
    	// If it's a standard type, assign that as well.
    	manifest.setTaskType(
    			StandardTaskType.forTypeUri(requestPackage.getTaskType()));
    	return manifest;
    }
    
    void setTaskType(TIPPTaskType type) {
    	this.taskType = type;
    }
    
    TIPPTaskType getTaskType() {
    	return taskType;
    }
    
    void saveToStream(OutputStream saveStream) throws TIPPException { 
        try {
            Document document = new ManifestDOMBuilder(this).makeDocument();
            validate(document);
            TransformerFactory factory = TransformerFactory.newInstance();
            Transformer transformer = factory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty(
                    "{http://xml.apache.org/xslt}indent-amount", "2");
            transformer.transform(new DOMSource(document), 
                    new StreamResult(saveStream));
        }
        catch (Exception e) {
            throw new TIPPException(e);
        }
    }
    
    // XXX This should blow away any existing settings 
    void loadFromStream(InputStream manifestStream, TIPPLoadStatus status) 
                throws TIPPValidationException, IOException {
    	try {
	        Document document = parse(manifestStream);
	        validate(document);
	        loadManifest(document, status);
    	}
    	catch (ParserConfigurationException e) {
    		throw new RuntimeException(e);
    	}
    }    
    
    private void loadManifest(Document document, TIPPLoadStatus status) 
                            throws TIPPValidationException {
        Element manifest = getFirstChildElement(document);
        loadDescriptor(getFirstChildByName(manifest, GLOBAL_DESCRIPTOR));
        loadPackageObjects(getFirstChildByName(manifest, PACKAGE_OBJECTS), status);
    }
    
    private void loadDescriptor(Element descriptor) 
                            throws TIPPValidationException {
        packageId = getChildTextByName(descriptor, UNIQUE_PACKAGE_ID);
        
        creator = loadCreator(getFirstChildByName(descriptor, PACKAGE_CREATOR));
        // Either load the request or the response, depending on which is
        // present
        task = loadTaskRequestOrResponse(descriptor);
    }
    
    private TIPPCreator loadCreator(Element creatorEl) {
        TIPPCreator creator = new TIPPCreator();
        creator.setName(getChildTextByName(creatorEl, Creator.NAME));
        creator.setId(getChildTextByName(creatorEl, Creator.ID));
        creator.setDate(
            loadDate(getFirstChildByName(creatorEl, Creator.UPDATE)));
        creator.setTool(loadTool(
                getFirstChildByName(creatorEl, TOOL)));
        return creator;
    }
    
    private TIPPTask loadTaskRequestOrResponse(Element descriptor) 
                        throws TIPPValidationException {
        Element requestEl = getFirstChildByName(descriptor, TASK_REQUEST);
        if (requestEl != null) {
            return loadTaskRequest(requestEl);
        }
        return loadTaskResponse(getFirstChildByName(descriptor, 
                                 TASK_RESPONSE));
    }
    
    private void loadTask(Element taskEl, TIPPTask task) {
        task.setTaskType(getChildTextByName(taskEl, Task.TYPE));
        task.setSourceLocale(getChildTextByName(taskEl, Task.SOURCE_LANGUAGE));
        task.setTargetLocale(getChildTextByName(taskEl, Task.TARGET_LANGUAGE));
        setTaskType(StandardTaskType.forTypeUri(task.getTaskType()));
    }
    
    private TIPPTaskRequest loadTaskRequest(Element requestEl) {
        TIPPTaskRequest request = new TIPPTaskRequest();
        loadTask(getFirstChildByName(requestEl, TASK), request);
        return request;
    }
    
    private TIPPTaskResponse loadTaskResponse(Element responseEl) 
                                throws TIPPValidationException {
        TIPPTaskResponse response = new TIPPTaskResponse();
        loadTask(getFirstChildByName(responseEl, TASK), response);
        Element inResponseTo = getFirstChildByName(responseEl, 
                                TaskResponse.IN_RESPONSE_TO);
        response.setRequestPackageId(getChildTextByName(inResponseTo,
                                                 UNIQUE_PACKAGE_ID));
        response.setRequestCreator(loadCreator(
                getFirstChildByName(inResponseTo, PACKAGE_CREATOR)));
        response.setComment(getChildTextByName(responseEl, 
                            TaskResponse.COMMENT));
        String rawMessage = getChildTextByName(responseEl, 
                            TaskResponse.MESSAGE);
        TIPPResponseMessage msg = TIPPResponseMessage.valueOf(rawMessage);
        if (msg == null) {
            throw new TIPPValidationException(
                    "Invalid ResponseMessage value: " + msg);
        }
        response.setMessage(msg);
        return response;
    }
    
    private Date loadDate(Element dateNode) {
        return DateUtil.parseTIPDate(getTextContent(dateNode));
    }
    
    private TIPPTool loadTool(Element toolEl) {
        TIPPTool tool = new TIPPTool();
        tool.setName(getChildTextByName(toolEl, ContributorTool.NAME));
        tool.setId(getChildTextByName(toolEl, ContributorTool.ID));
        tool.setVersion(getChildTextByName(toolEl, ContributorTool.VERSION));
        return tool;
    }
    
    private void loadPackageObjects(Element parent, TIPPLoadStatus status) 
                            throws TIPPValidationException {
        // parse all the sections
        NodeList children = 
            parent.getElementsByTagName(PACKAGE_OBJECT_SECTION);
        for (int i = 0; i < children.getLength(); i++) {
            TIPPObjectSection section = 
                loadPackageObjectSection((Element)children.item(i), status);
            // Don't allow duplicate sections
            if (objectSections.containsKey(section.getType())) {
                throw new TIPPValidationException("Duplicate object section: " +
                                                  section.getType());
            }
            objectSections.put(section.getType(), section);
        }
    }
    
    private TIPPObjectSection loadPackageObjectSection(Element section,
            TIPPLoadStatus status) throws TIPPValidationException {
        String typeUri = section.getAttribute(ATTR_SECTION_TYPE);
        TIPPObjectSectionType type = TIPPObjectSectionType.byURI(typeUri);
        if (type == null) {
            status.addError(new TIPPError(TIPPError.Type.INVALID_SECTION_TYPE, 
                    "Invalid section type: " + typeUri));
            throw new TIPPValidationException("Invalid section type: " + typeUri);
        }
        TIPPObjectSection objectSection = new TIPPObjectSection(
                section.getAttribute(ATTR_SECTION_NAME), type);
        objectSection.setPackage(tipPackage);
        NodeList children = section.getElementsByTagName(OBJECT_FILE);
        for (int i = 0; i < children.getLength(); i++) {
            objectSection.addObject(loadObjectFile((Element)children.item(i)));
        }
        return objectSection;
    }
    
    private TIPPObjectFile loadObjectFile(Element file) 
                            throws TIPPValidationException {
        TIPPObjectFile object = new TIPPObjectFile();
        object.setPackage(tipPackage);
        String rawSequence = file.getAttribute(ObjectFile.ATTR_SEQUENCE);
        try {
            object.setSequence(Integer.parseInt(rawSequence));
        }
        catch (NumberFormatException e) {
            throw new TIPPValidationException(
                    "Invalid sequence value: '" + rawSequence + "'");
        }
        object.setLocation(getChildTextByName(file, ObjectFile.LOCATION));
        String name = getChildTextByName(file, ObjectFile.NAME);
        object.setName((name == null) ? object.getLocation() : name);
        return object;
    }

    Document parse(InputStream is) throws ParserConfigurationException, 
                                    TIPPValidationException, IOException {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            return builder.parse(is);
        }
        catch (SAXException e) {
            throw new TIPPValidationException(e);
        }
        finally {
            is.close();
        }
    }
    
    void validate(Document dom) throws TIPPValidationException {
        try {
            InputStream is = 
                getClass().getResourceAsStream("/TIPPManifest-1_5.xsd");
            SchemaFactory factory = 
                SchemaFactory.newInstance(W3C_XML_SCHEMA_NS_URI);
            Schema schema = factory.newSchema(new StreamSource(is));
            // need an error handler
            Validator validator = schema.newValidator();
            validator.validate(new DOMSource(dom));
            is.close();
        }
        catch (Exception e) {
            throw new TIPPValidationException(e);
        }
    }

    public String getPackageId() {
        return packageId;
    }

    public void setPackageId(String packageId) {
        this.packageId = packageId;
    }
    
    public TIPPCreator getCreator() {
        return creator;
    }

    public void setCreator(TIPPCreator creator) {
        this.creator = creator;
    }

    public TIPPTask getTask() {
        return task;
    }
    
    public void setTask(TIPPTask task) {
        this.task = task;
    }
    
    public boolean isRequest() {
        return (task instanceof TIPPTaskRequest);
    }

    /**
     * Return the object section with a given type, if it exists.  
     * @param type section type
     * @return object section for the specified section type, or
     *         null if no section with that type exists in the TIPP
     */
    public TIPPObjectSection getObjectSection(TIPPObjectSectionType type) {
        return objectSections.get(type);
    }
    
    /**
     * Return a collection of all object sections.
     * @return (possibly empty) collection of object sections
     */
    public Collection<TIPPObjectSection> getObjectSections() {
        return objectSections.values();
    }
    
    public TIPPObjectSection addObjectSection(String name, TIPPObjectSectionType type) {
    	// If we were created with a task type object, restrict the 
    	// section type to one of the choices for this task type.
    	if (taskType != null) {
    		if (!taskType.getSupportedSectionTypes().contains(type)) {
    			throw new IllegalArgumentException("Section type " + type + 
					" is not supported for task type " + taskType.getType());
    		}
    	}
        TIPPObjectSection section = new TIPPObjectSection(name, type);
        section.setPackage(tipPackage);
        objectSections.put(type, section);
        return section;
    }
    
    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (o == null || !(o instanceof Manifest)) return false;
        Manifest m = (Manifest)o;
        return m.getPackageId().equals(getPackageId()) &&
                m.getCreator().equals(getCreator()) &&
                m.getTask().equals(getTask()) &&
                m.getObjectSections().equals(getObjectSections());
    }
    
    @Override
    public String toString() {
        return "TIPManifest(id=" + getPackageId() + ", creator=" + getCreator()
                + ", task=" + getTask() + ", sections=" + getObjectSections() 
                + ")";
    }
}
