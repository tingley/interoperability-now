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
    
    Document makeDocument() throws ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = factory.newDocumentBuilder();
        document = docBuilder.newDocument();
        Element root = document.createElement(MANIFEST);
        document.appendChild(root);
        root.setAttribute(ATTR_VERSION, SCHEMA_VERSION);
        root.setAttributeNS("http://www.w3.org/2001/XMLSchema-instance", 
                "noNamespaceSchemaLocation", SCHEMA_LOCATION);
        root.appendChild(makeDescriptor());
        root.appendChild(makePackageObjects());
        return document;
    }
    
    Element makeDescriptor() {
        Element descriptor = document.createElement(GLOBAL_DESCRIPTOR);
        appendElementChildWithText(document, 
                descriptor, UNIQUE_PACKAGE_ID, manifest.getPackageId());
        descriptor.appendChild(makePackageCreator(manifest.getCreator()));
        descriptor.appendChild(makeTaskRequestOrResponse(manifest.getTask()));
        return descriptor;
    }
    
    Element makePackageCreator(TIPPCreator creator) {
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
    
    Element makeContributorTool(TIPPTool tool) {
        Element toolEl = document.createElement(TOOL);
        appendElementChildWithText(document,
                toolEl, ContributorTool.NAME, tool.getName());
        appendElementChildWithText(document,
                toolEl, ContributorTool.ID, tool.getId());
        appendElementChildWithText(document,
                toolEl, ContributorTool.VERSION, tool.getVersion());
        return toolEl;
    }
    
    Element makeTaskRequestOrResponse(TIPPTask task) {
        if (task instanceof TIPPTaskRequest) {
            return makeTaskRequest((TIPPTaskRequest)task);
        }
        else {
            return makeTaskResponse((TIPPTaskResponse)task);
        }
    }
    
    Element makeTaskRequest(TIPPTaskRequest request) {
        Element requestEl = document.createElement(TASK_REQUEST);
        requestEl.appendChild(makeTask(request));
        return requestEl;
    }
    
    Element makeTask(TIPPTask task) {
        Element taskEl = document.createElement(TASK);
        appendElementChildWithText(document, taskEl, 
                Task.TYPE, task.getTaskType());
        appendElementChildWithText(document, taskEl, 
                Task.SOURCE_LANGUAGE, task.getSourceLocale());        
        appendElementChildWithText(document, taskEl, 
                Task.TARGET_LANGUAGE, task.getTargetLocale());
        return taskEl;
    }
    
    Element makeTaskResponse(TIPPTaskResponse response) {
        Element responseEl = document.createElement(TASK_RESPONSE);
        responseEl.appendChild(makeTask(response));
        responseEl.appendChild(makeInResponseTo(response));
        appendElementChildWithText(document, responseEl,
                TaskResponse.MESSAGE, response.getMessage().toString());
        String comment = response.getComment() != null ? 
                response.getComment() : "";
        appendElementChildWithText(document, responseEl,
                TaskResponse.COMMENT, comment);        
        return responseEl;
    }
    
    Element makeInResponseTo(TIPPTaskResponse response) {
        Element inReEl = document.createElement(TaskResponse.IN_RESPONSE_TO);
        appendElementChildWithText(document, inReEl,
                UNIQUE_PACKAGE_ID, response.getRequestPackageId());
        inReEl.appendChild(makePackageCreator(response.getRequestCreator()));
        return inReEl;
    }
    
    
    Element makePackageObjects() {
        Element objects = document.createElement(PACKAGE_OBJECTS);
        for (TIPPObjectSection section : manifest.getObjectSections()) {
            objects.appendChild(makeObjectSection(section));
        }
        return objects;
    }
    
    Element makeObjectSection(TIPPObjectSection section) {
        Element sectionEl = document.createElement(PACKAGE_OBJECT_SECTION);
        sectionEl.setAttribute(ATTR_SECTION_NAME, 
                               section.getName());
        sectionEl.setAttribute(ATTR_SECTION_TYPE, section.getType().getType());
        for (TIPPObjectFile file : section.getObjectFiles()) {
            sectionEl.appendChild(makeObjectFile(file));
        }
        return sectionEl;
    }
    
    Element makeObjectFile(TIPPObjectFile file) {
        Element fileEl = document.createElement(OBJECT_FILE);
        // TODO: is sequence optional?
        fileEl.setAttribute(ObjectFile.ATTR_SEQUENCE, String.valueOf(file.getSequence()));
        appendElementChildWithText(document, fileEl, ObjectFile.NAME,
                                   file.getName());
        appendElementChildWithText(document, fileEl, ObjectFile.LOCATION,
                file.getLocation());
        return fileEl;
    }
}
