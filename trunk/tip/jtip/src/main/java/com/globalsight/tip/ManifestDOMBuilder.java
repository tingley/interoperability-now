package com.globalsight.tip;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import static com.globalsight.tip.TIPPConstants.*;
import static com.globalsight.tip.XMLUtil.*;

/**
 * Convert a TIPManifest into a DOM tree.
 */
class ManifestDOMBuilder {

    private Manifest manifest;
    private Document document;
    
    ManifestDOMBuilder(Manifest manifest) {
        this.manifest = manifest;
    }
    
    public static final String TIPP_NAMESPACE = 
            "http://schema.interoperability-now.org/tipp/1_5/";
    
    Document makeDocument() throws ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        // Namespaces are required for xml-dsig
        factory.setNamespaceAware(true);
        DocumentBuilder docBuilder = factory.newDocumentBuilder();
        document = docBuilder.newDocument();
        Element root = document.createElement(MANIFEST);
        // QUESTIONABLE: I'm disabling writing out the schema location, because
        // a) it is causes havoc with the xml-dsig signing, for some reason, and
        // b) it's only meant to be a hint anyways.
        // root.setAttributeNS("http://www.w3.org/2001/XMLSchema-instance", 
        //      "schemaLocation", SCHEMA_LOCATION);
        root.appendChild(makeDescriptor());
        root.appendChild(makeTaskRequestOrResponse(manifest.getTask()));
        root.appendChild(makePackageObjects());
        root.setAttribute("xmlns", TIPP_NAMESPACE);
        root.setAttribute(ATTR_VERSION, SCHEMA_VERSION);
        document.appendChild(root);
        return document;
    }
    
    private Element makeDescriptor() {
        Element descriptor = document.createElement(GLOBAL_DESCRIPTOR);
        appendElementChildWithText(document, 
                descriptor, UNIQUE_PACKAGE_ID, manifest.getPackageId());
        descriptor.appendChild(makePackageCreator(manifest.getCreator()));
        return descriptor;
    }
    
    private Element makePackageCreator(TIPPCreator creator) {
        Element creatorEl = document.createElement(PACKAGE_CREATOR);
        appendElementChildWithText(document, 
                creatorEl, Creator.NAME, creator.getName());
        appendElementChildWithText(document, 
                creatorEl, Creator.ID, creator.getId());
        appendElementChildWithText(document, creatorEl, Creator.UPDATE, 
                DateUtil.writeTIPDate(creator.getDate()));
        creatorEl.appendChild(makeContributorTool(creator.getTool()));
        return creatorEl;
    }
    
    private Element makeContributorTool(TIPPTool tool) {
        Element toolEl = document.createElement(TOOL);
        appendElementChildWithText(document,
                toolEl, ContributorTool.NAME, tool.getName());
        appendElementChildWithText(document,
                toolEl, ContributorTool.ID, tool.getId());
        appendElementChildWithText(document,
                toolEl, ContributorTool.VERSION, tool.getVersion());
        return toolEl;
    }
    
    private Element makeTaskRequestOrResponse(TIPPTask task) {
        if (task instanceof TIPPTaskRequest) {
            return makeTaskRequest((TIPPTaskRequest)task);
        }
        else {
            return makeTaskResponse((TIPPTaskResponse)task);
        }
    }
    
    private Element makeTaskRequest(TIPPTaskRequest request) {
        Element requestEl = document.createElement(TASK_REQUEST);
        appendTaskData(request, requestEl);
        return requestEl;
    }
    
    private Element appendTaskData(TIPPTask task, Element parent) {
        appendElementChildWithText(document, parent, 
                Task.TYPE, task.getTaskType());
        appendElementChildWithText(document, parent, 
                Task.SOURCE_LANGUAGE, task.getSourceLocale());        
        appendElementChildWithText(document, parent, 
                Task.TARGET_LANGUAGE, task.getTargetLocale());
        return parent;
    }
    
    private Element makeTaskResponse(TIPPTaskResponse response) {
        Element responseEl = document.createElement(TASK_RESPONSE);
        responseEl.appendChild(makeInResponseTo(response));
        appendElementChildWithText(document, responseEl,
                TaskResponse.MESSAGE, response.getMessage().toString());
        String comment = response.getComment() != null ? 
                response.getComment() : "";
        appendElementChildWithText(document, responseEl,
                TaskResponse.COMMENT, comment);        
        return responseEl;
    }
    
    private Element makeInResponseTo(TIPPTaskResponse response) {
        Element inReEl = document.createElement(TaskResponse.IN_RESPONSE_TO);
        appendTaskData(response, inReEl);
        appendElementChildWithText(document, inReEl,
                UNIQUE_PACKAGE_ID, response.getRequestPackageId());
        inReEl.appendChild(makePackageCreator(response.getRequestCreator()));
        return inReEl;
    }
    
    
    private Element makePackageObjects() {
        Element objects = document.createElement(PACKAGE_OBJECTS);
        for (TIPPSection section : manifest.getObjectSections()) {
            objects.appendChild(makeObjectSection(section));
        }
        return objects;
    }
    
    private Element makeObjectSection(TIPPSection section) {
        Element sectionEl = document.createElement(section.getType().getElementName());
        sectionEl.setAttribute(ATTR_SECTION_NAME, 
                               section.getName());
        for (TIPPResource file : section.getResources()) {
            sectionEl.appendChild(makeObjectFile(file));
        }
        return sectionEl;
    }
    
    private Element makeObjectFile(TIPPResource file) {
        Element fileEl = null;
        // TODO: it would be nice if there were a better way to do this
        if (file instanceof TIPPReferenceFile) {
            fileEl = document.createElement(REFERENCE_FILE_RESOURCE);
            TIPPReferenceFile refObj = (TIPPReferenceFile)file;
            if (refObj.getLanguageChoice() != null) {
                fileEl.setAttribute(ObjectFile.ATTR_LANGUAGE_CHOICE, 
                        refObj.getLanguageChoice().name());
            }
        }
        else {
            fileEl = document.createElement(FILE_RESOURCE);
        }
        fileEl.setAttribute(ObjectFile.ATTR_SEQUENCE, String.valueOf(file.getSequence()));
        appendElementChildWithText(document, fileEl, ObjectFile.NAME,
                                   file.getName());
        if (file instanceof TIPPFile) {
            appendElementChildWithText(document, fileEl, ObjectFile.LOCATION,
                    ((TIPPFile)file).getLocation());
        }
        return fileEl;
    }
}
