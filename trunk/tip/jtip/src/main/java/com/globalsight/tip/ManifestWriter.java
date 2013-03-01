package com.globalsight.tip;

import java.io.InputStream;
import java.io.OutputStream;
import java.security.KeyPair;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;

class ManifestWriter {

    private KeyPair keyPair;
    private InputStream payloadStream;
    
    ManifestWriter() {
    }
    
    void setKeyPair(KeyPair keyPair) {
        this.keyPair = keyPair;
    }
    
    KeyPair getKeyPair() {
        return keyPair;
    }
    
    void setPayload(InputStream payload) {
        this.payloadStream = payload;
    }
    
    InputStream getPayload() {
        return payloadStream;
    }
    
    void saveToStream(Manifest manifest, OutputStream saveStream) throws TIPPException { 
        try {
            Document document = new ManifestDOMBuilder(manifest).makeDocument();
            if (keyPair != null) {
                new ManifestSigner().sign(document, getPayload(), keyPair);
            }
            TIPPLoadStatus status = new TIPPLoadStatus();
            //validate(document, status);
            if (status.getSeverity() != TIPPErrorSeverity.NONE) {
                // XXX What to do with the errors?
                throw new TIPPException("Saved manifest was invalid");
            }
            TransformerFactory factory = TransformerFactory.newInstance();
            Transformer transformer = factory.newTransformer();
            //transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            // Can't use these or it messes up xml-dsig
            //transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            //transformer.setOutputProperty(
            //        "{http://xml.apache.org/xslt}indent-amount", "2");
            transformer.transform(new DOMSource(document),
                    new StreamResult(saveStream));
        }
        catch (TIPPException e) {
            throw e;
        }
        catch (Exception e) {
            throw new TIPPException(e);
        }
    }

}
