package com.globalsight.tip;

import static com.globalsight.tip.TIPPConstants.*;
import static com.globalsight.tip.XMLUtil.*;
import static javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;

import com.globalsight.tip.TIPPConstants.ContributorTool;
import com.globalsight.tip.TIPPConstants.Creator;
import com.globalsight.tip.TIPPConstants.ObjectFile;
import com.globalsight.tip.TIPPError.Type;

import javax.xml.crypto.KeySelector;

class Manifest {

    static final String XMLDSIG_SCHEMA_URI = 
            "http://www.w3.org/TR/xmldsig-core/xmldsig-core-schema.xsd";
    static final String XMLDSIG_NS_PREFIX =
            "http://www.w3.org/2000/09/xmldsig#";
    
    // Only for construction
	private TIPPTaskType taskType;
	
    private PackageBase tipPackage;
    private String packageId;
    private TIPPTask task; // Either request or response
    private TIPPCreator creator = new TIPPCreator();
    
    private Map<TIPPSectionType, TIPPSection> objectSections = 
        new HashMap<TIPPSectionType, TIPPSection>();
    
    
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
    
    boolean loadFromStream(InputStream manifestStream, TIPPLoadStatus status)
            throws IOException {
        return loadFromStream(manifestStream, status, null, null);
    }
    
    // XXX This should blow away any existing settings 
    boolean loadFromStream(InputStream manifestStream, TIPPLoadStatus status,
                           KeySelector keySelector, InputStream payloadStream) 
                throws IOException {
        if (manifestStream == null) {
            status.addError(TIPPError.Type.MISSING_MANIFEST);
            return false;
        }
    	try {
	        Document document = parse(manifestStream, status);
	        if (document == null) {
	            return false;
	        }
	        // Validate the schema
	        if (!validate(document, status)) {
	            return false;
	        }
	        // Validate the XML Signature if we are given a key
            if (!validateSignature(document, status, keySelector, payloadStream)) {
                return false;
            }
	        loadManifest(document, status);
	        return true;
    	}
    	catch (ParserConfigurationException e) {
    		throw new RuntimeException(e);
    	}
    }    
    
    private void loadManifest(Document document, TIPPLoadStatus status) {
        Element manifest = getFirstChildElement(document);
        loadDescriptor(getFirstChildByName(manifest, GLOBAL_DESCRIPTOR));
        // Either load the request or the response, depending on which is
        // present
        task = loadTaskRequestOrResponse(manifest);
        loadPackageObjects(getFirstChildByName(manifest, PACKAGE_OBJECTS), status);
        
        // Perform additional validation that isn't covered by the schema
        TIPPTaskType taskType = getTaskType();
        if (taskType != null) {
            for (TIPPSection section : getObjectSections()) {
                if (!taskType.getSupportedSectionTypes().contains(section.getType())) {
                    status.addError(TIPPError.Type.INVALID_SECTION_FOR_TASK, 
                            "Invalid section for task type: " + 
                                section.getType());
                }
            }
        }
    }
    
    private void loadDescriptor(Element descriptor) {
        packageId = getChildTextByName(descriptor, UNIQUE_PACKAGE_ID);
        
        creator = loadCreator(getFirstChildByName(descriptor, PACKAGE_CREATOR));
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
    
    private TIPPTask loadTaskRequestOrResponse(Element descriptor) {
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
        loadTask(requestEl, request);
        return request;
    }
    
    private TIPPTaskResponse loadTaskResponse(Element responseEl) {
        TIPPTaskResponse response = new TIPPTaskResponse();
        loadTask(responseEl, response);
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
        TIPPResponseCode msg = TIPPResponseCode.valueOf(rawMessage);
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
    
    private void loadPackageObjects(Element parent, TIPPLoadStatus status) {
        NodeList children = parent.getChildNodes();
        // parse all the sections
        for (int i = 0; i < children.getLength(); i++) {
            if (children.item(i).getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }
            TIPPSection section = 
                loadPackageObjectSection((Element)children.item(i), status);
            if (section == null) {
                continue;
            }
            // Don't allow duplicate sections
            if (objectSections.containsKey(section.getType())) {
                status.addError(TIPPError.Type.DUPLICATE_SECTION_IN_MANIFEST, 
                        "Duplicate section: " + section.getType());
                continue;
            }
            objectSections.put(section.getType(), section);
        }
    }
    
    private TIPPSection loadPackageObjectSection(Element section,
            TIPPLoadStatus status) {
        TIPPSectionType type = 
                TIPPSectionType.byElementName(section.getNodeName());
        if (type == null) {
            return null; // Should never happen
        }
        String sectionName = section.getAttribute(ATTR_SECTION_NAME);
        if (type.equals(TIPPSectionType.REFERENCE)) {
            TIPPReferenceSection refSection = new TIPPReferenceSection(sectionName);
            NodeList children = section.getElementsByTagName(FILE_RESOURCE);
            for (int i = 0; i < children.getLength(); i++) {
                refSection.addResource(loadReferenceFile((Element)children.item(i),
                        status));
            }
            return refSection;
        }
        else {
            TIPPSection objSection = new TIPPSection(sectionName, type);
            objSection.setPackage(tipPackage);
            NodeList children = section.getElementsByTagName(FILE_RESOURCE);
            for (int i = 0; i < children.getLength(); i++) {
                objSection.addResource(loadResource((Element)children.item(i),
                        status));
            }
            return objSection;
        }
    }
    
    private TIPPReferenceFile loadReferenceFile(Element file,
                            TIPPLoadStatus status) {
        TIPPReferenceFile object = new TIPPReferenceFile();
        loadFileResource(object, file, status);
        object.setLanguageChoice( 
                TIPPReferenceFile.LanguageChoice.valueOf(
                        file.getAttribute(ObjectFile.ATTR_LANGUAGE_CHOICE)));
        return object;
    }
    
    private TIPPResource loadResource(Element file,
                            TIPPLoadStatus status) {
        TIPPFile object = new TIPPFile();
        loadFileResource(object, file, status);
        return object;
    }
    
    private void loadFileResource(TIPPFile object, Element file,
                                TIPPLoadStatus status) {  
        object.setPackage(tipPackage);
        String rawSequence = file.getAttribute(ObjectFile.ATTR_SEQUENCE);
        try {
            object.setSequence(Integer.parseInt(rawSequence));
        }
        catch (NumberFormatException e) {
            // This should be caught by validation
        }
        object.setLocation(getChildTextByName(file, ObjectFile.LOCATION));
        String name = getChildTextByName(file, ObjectFile.NAME);
        object.setName((name == null) ? object.getLocation() : name);
    }
    
    Document parse(InputStream is, TIPPLoadStatus status) throws ParserConfigurationException, 
                                    IOException {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            return builder.parse(is);
        }
        catch (Exception e) {
            status.addError(TIPPError.Type.CORRUPT_MANIFEST, "Could not parse manifest", e);
            return null;
        }
        finally {
            is.close();
        }
    }
    
    boolean validate(final Document dom, TIPPLoadStatus status) {
        try {
            InputStream is = 
                getClass().getResourceAsStream("/TIPPManifest-1_5.xsd");
            SchemaFactory factory = 
                SchemaFactory.newInstance(W3C_XML_SCHEMA_NS_URI);
            factory.setResourceResolver(new LSResourceResolver() {
                public LSInput resolveResource(String type, String namespaceURI, 
                        String publicId, String systemId, String baseURI)  {
                    LSInput input = ((DOMImplementationLS)dom
                            .getImplementation()).createLSInput();
                    if (("TIPPCommon.xsd".equals(systemId) && W3C_XML_SCHEMA_NS_URI.equals(type)) ||
                         COMMON_SCHEMA_LOCATION.equalsIgnoreCase(baseURI)) {
                        input.setByteStream(getClass().getResourceAsStream("/TIPPCommon-1_5.xsd"));
                    }
                    else if (XMLDSIG_SCHEMA_URI.equalsIgnoreCase(baseURI) ||
                        XMLDSIG_NS_PREFIX.equalsIgnoreCase(namespaceURI)) {
                        input.setByteStream(getClass().getResourceAsStream("/xmldsig-core-schema.xsd"));
                    }
                    else if ("http://www.w3.org/2001/XMLSchema.dtd".equals(baseURI) ||
                             "http://www.w3.org/2001/XMLSchema.dtd".equals(systemId)) {
                        input.setByteStream(getClass().getResourceAsStream("/XMLSchema.dtd"));
                    }
                    else if ("datatypes.dtd".equals(systemId)) {
                        input.setByteStream(getClass().getResourceAsStream("/datatypes.dtd"));
                    }
                    else {
                        return null;
                    }
                    return input;
                }
            });
            Schema schema = factory.newSchema(new StreamSource(is));
            // need an error handler
            Validator validator = schema.newValidator();
            validator.validate(new DOMSource(dom));
            is.close();
            return true;
        }
        catch (Exception e) {
            status.addError(TIPPError.Type.INVALID_MANIFEST, "Invalid manifest", e);
            return false;
        }
    }

    boolean validateSignature(final Document doc, TIPPLoadStatus status,
                           KeySelector keySelector,
                           InputStream payloadStream) {
        ManifestSigner signer = new ManifestSigner();
        if (signer.hasSignature(doc)) {
            if (keySelector != null) {
                if (!signer.validateSignature(doc, keySelector,
                            payloadStream)) {
                    status.addError(Type.INVALID_SIGNATURE);
                    return false;
                }
            }
            else {
                // The manifest has a signature, but we're not able to 
                // validate it because no key was provided by the user.
                status.addError(Type.UNABLE_TO_VERIFY_SIGNATURE);
                return false;
            }
        }
        return true;
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
    public TIPPSection getObjectSection(TIPPSectionType type) {
        return objectSections.get(type);
    }
    
    /**
     * Return a collection of all object sections.
     * @return (possibly empty) collection of object sections
     */
    public Collection<TIPPSection> getObjectSections() {
        return objectSections.values();
    }
    
    public TIPPReferenceSection getReferenceSection() {
        return (TIPPReferenceSection)objectSections.get(TIPPSectionType.REFERENCE);
    }
    
    public TIPPSection addObjectSection(String name, TIPPSectionType type) {
    	// If we were created with a task type object, restrict the 
    	// section type to one of the choices for this task type.
    	if (taskType != null) {
    		if (!taskType.getSupportedSectionTypes().contains(type)) {
    			throw new IllegalArgumentException("Section type " + type + 
					" is not supported for task type " + taskType.getType());
    		}
    	}
    	TIPPSection section = null;
    	if (type == TIPPSectionType.REFERENCE) {
    	    section = new TIPPReferenceSection(name);
    	}
    	else {
    	    section = new TIPPSection(name, type);
    	}
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
