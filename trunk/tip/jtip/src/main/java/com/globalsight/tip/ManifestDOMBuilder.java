package com.globalsight.tip;

import java.util.Date;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import static com.globalsight.tip.TIPConstants.*;
import static com.globalsight.tip.XMLUtil.*;

/**
 * Convert a TIPManifest into a DOM tree.
 */
class ManifestDOMBuilder {

    private TIPManifest manifest;
    private Document document;
    
    ManifestDOMBuilder(TIPManifest manifest) {
        this.manifest = manifest;
    }
    
    Document makeDocument() throws ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = factory.newDocumentBuilder();
        document = docBuilder.newDocument();
        Element root = document.createElement(MANIFEST);
        document.appendChild(root);
        root.setAttribute(ATTR_VERSION, "1.1");
        root.setAttributeNS("http://www.w3.org/2001/XMLSchema-instance", 
                "noNamespaceSchemaLocation", "TIPManifest-1-1.xsd");
        root.appendChild(makeDescriptor());
        root.appendChild(makePackageObjects());
        return document;
    }
    
    Element makeDescriptor() {
        Element descriptor = document.createElement(GLOBAL_DESCRIPTOR);
        appendElementChildWithText(document, 
                descriptor, UNIQUE_PACKAGE_ID, manifest.getPackageId());
        descriptor.appendChild(makePackageCreator());
        descriptor.appendChild(makeOrderAction());
        return descriptor;
    }
    
    // TODO: this is not printing the right date format
    Element makePackageCreator() {
        Element creator = document.createElement(PACKAGE_CREATOR);
        appendElementChildWithText(document, 
                creator, Creator.NAME, manifest.getCreatorName());
        appendElementChildWithText(document, 
                creator, Creator.ID, manifest.getCreatorId());
        appendElementChildWithText(document, creator, Creator.UPDATE, 
                DateUtil.writeTIPDate(manifest.getCreatorUpdate()));
        creator.appendChild(makeContributorTool(manifest.getContributorTool()));
        appendElementChildWithText(document, 
                creator, Creator.COMMUNICATION, manifest.getCommunication());
        return creator;
    }
    
    Element makeContributorTool(TIPTool tool) {
        Element toolEl = document.createElement(CONTRIBUTOR_TOOL);
        appendElementChildWithText(document,
                toolEl, ContributorTool.NAME, tool.getName());
        appendElementChildWithText(document,
                toolEl, ContributorTool.ID, tool.getId());
        appendElementChildWithText(document,
                toolEl, ContributorTool.VERSION, tool.getVersion());
        return toolEl;
    }
    
    Element makeOrderAction() {
        Element action = document.createElement(ORDER_ACTION);
        action.appendChild(makeOrderTask());
        // TODO: handle response if it exists
        return action;
    }
    
    Element makeOrderTask() {
        Element task = document.createElement(ORDER_TASK);
        appendElementChildWithText(document, task, OrderTask.TYPE, 
                manifest.getTaskType().getValue());
        appendElementChildWithText(document, task, 
                OrderTask.SOURCE_LANGUAGE, manifest.getSourceLanguage());
        appendElementChildWithText(document, task, 
                OrderTask.TARGET_LANGUAGE, manifest.getTargetLanguage());
        return task;
    }
    
    Element makePackageObjects() {
        Element objects = document.createElement(PACKAGE_OBJECTS);
        for (TIPObjectSection section : manifest.getObjectSections()) {
            objects.appendChild(makeObjectSection(section));
        }
        return objects;
    }
    
    Element makeObjectSection(TIPObjectSection section) {
        Element sectionEl = document.createElement(PACKAGE_OBJECT_SECTION);
        sectionEl.setAttribute(ATTR_SECTION_NAME, 
                               section.getObjectSectionType().getValue());
        appendElementChildWithText(document, sectionEl, OBJECT_SEQUENCE, 
                               Integer.toString(section.getObjectSequence()));
        for (TIPObjectFile file : section.getObjectFiles()) {
            sectionEl.appendChild(makeObjectFile(file));
        }
        return sectionEl;
    }
    
    Element makeObjectFile(TIPObjectFile file) {
        Element fileEl = document.createElement(OBJECT_FILE);
        fileEl.setAttribute(ObjectFile.ATTR_LOCALIZABLE, 
                            yesNo(file.isLocalizable()));
        appendElementChildWithText(document, fileEl, ObjectFile.TYPE,
                                   file.getType());
        appendElementChildWithText(document, fileEl, ObjectFile.LOCATION_PATH,
                file.getPath());
        return fileEl;
    }
    
    private String yesNo(boolean b) {
        return b ? YES : NO;
    }
}
