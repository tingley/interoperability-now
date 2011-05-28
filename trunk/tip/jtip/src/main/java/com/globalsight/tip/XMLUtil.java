package com.globalsight.tip;

import org.w3c.dom.*;

class XMLUtil {

    /**
     * Return the first child element with a given name.  
     * @param parent parent element
     * @param name child element name
     * @return 
     */
    public static Element getFirstChildByName(Element parent, String name) {
        NodeList children = parent.getElementsByTagName(name);
        if (children.getLength() == 0) {
            return null;
        }
        return (Element)children.item(0);
    }
    
    /**
     * Returns a node's first child node that is an element.
     * @param parent
     * @return first child element, or null
     */
    public static Element getFirstChildElement(Node parent) {
        NodeList children = parent.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node n = children.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                return (Element)n;
            }
        }
        return null;
    }
    
    /**
     * Returns normalized text content for a node.
     * @param node
     * @return text content of child node, trimmed
     */
    public static String getTextContent(Node node) {
        return node.getTextContent().trim();
    }
    
    public static Element appendElementChild(Document doc, Element parent, 
                                             String name) {
        Element el = doc.createElement(name);
        parent.appendChild(el);
        return el;
    }
    
    public static Element appendElementChildWithText(Document doc, Element parent,
                                             String name, String text) {
        Element el = appendElementChild(doc, parent, name);
        el.setTextContent(text);
        return el;
    }
}
