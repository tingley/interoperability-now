package com.globalsight.tip;

import java.io.InputStream;
import java.security.KeyPair;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.xml.crypto.Data;
import javax.xml.crypto.KeySelector;
import javax.xml.crypto.MarshalException;
import javax.xml.crypto.OctetStreamData;
import javax.xml.crypto.URIDereferencer;
import javax.xml.crypto.URIReference;
import javax.xml.crypto.URIReferenceException;
import javax.xml.crypto.XMLCryptoContext;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.KeyInfoFactory;
import javax.xml.crypto.dsig.keyinfo.KeyValue;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.XMLSignatureException;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.Reference;
import javax.xml.crypto.dsig.DigestMethod;
import javax.xml.crypto.dsig.Transform;
import javax.xml.crypto.dsig.spec.TransformParameterSpec;
import javax.xml.crypto.dsig.SignedInfo;
import javax.xml.crypto.dsig.CanonicalizationMethod;
import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec;
import javax.xml.crypto.dsig.SignatureMethod;
import javax.xml.crypto.dsig.dom.DOMValidateContext;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

class ManifestSigner {
    
    XMLSignatureFactory factory;
    
    ManifestSigner() {
        factory = XMLSignatureFactory.getInstance("DOM"); 
    }

    void sign(Document manifest, InputStream payload, KeyPair kp) {
        try {            
            // In accordance with the schema, place it last in the 
            // <GlobalDescriptor>
            DOMSignContext dsc = new DOMSignContext
//                    (kp.getPrivate(), findGlobalDescriptorNode(manifest));
                    (kp.getPrivate(), manifest.getDocumentElement());
            Reference ref = factory.newReference
                    ("", factory.newDigestMethod(DigestMethod.SHA1, null),
                      Collections.singletonList
                        (factory.newTransform(Transform.ENVELOPED,
                          (TransformParameterSpec) null)), null, null); 
            List<Reference> refs = new ArrayList<Reference>();
            refs.add(ref);
            if (payload != null) {
                Reference payloadRef = factory.newReference("pobjects.zip",
                        factory.newDigestMethod(DigestMethod.SHA1, null),
                        new ArrayList<Transform>(),
//                        Collections.singletonList(
                                //factory.newTransform(Transform.BASE64, (TransformParameterSpec)null)),
                        null, null);
                refs.add(payloadRef);
            }
            SignedInfo si = factory.newSignedInfo
                    (factory.newCanonicalizationMethod
                      (CanonicalizationMethod.INCLUSIVE_WITH_COMMENTS,
                        (C14NMethodParameterSpec) null),
                        factory.newSignatureMethod(SignatureMethod.DSA_SHA1, null),
                      refs);
            KeyInfoFactory kif = factory.getKeyInfoFactory(); 
            KeyValue kv = kif.newKeyValue(kp.getPublic());
            KeyInfo ki = kif.newKeyInfo(Collections.singletonList(kv)); 
            // Set up a custom URI dereferencer so "pobjects.zip" means something
            // to the xml-dsig process
            dsc.setURIDereferencer(new TIPPUriDereferencer(payload));
            // Create the signature itself
            XMLSignature signature = factory.newXMLSignature(si, ki);
            signature.sign(dsc);
        }
        catch (Exception e) {
            // TODO
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
    
    class TIPPUriDereferencer implements URIDereferencer {
        private InputStream is;
        public TIPPUriDereferencer(InputStream is) {
            this.is = is;
        }
        public Data dereference(URIReference uri, XMLCryptoContext context)
                throws URIReferenceException {
            if (PackageBase.PAYLOAD_FILE.equals(uri.getURI()) && is != null) {
                return new OctetStreamData(is);
            }
            return factory.getURIDereferencer().dereference(uri, context);
        }
    }
    
    /**
     * Validate the xml signature of a manifest DOM.  If no 
     * Signature is embedded in the manifest, no validation is
     * performed.
     * @param doc document node of a parsed manifest DOM
     * @param keySelector keySelector to provide the key with which to 
     *      verify the signature
     * @param payloadStream bytes of the raw (zipped) payload data.  May 
     *        be null (in particular, if this is a testcase where we are
     *        only working with a manifest.)
     * @return true if validation succeeds or if no signature was present,
     *         false if validation failed
     * @throws MarshalException 
     * @throws XMLSignatureException 
     */
    boolean validateSignature(Document doc, KeySelector keySelector, 
                              InputStream payloadStream) {
        try {
            Node sig = findSignatureElement(doc);
            if (sig == null) return true;
            
            DOMValidateContext valContext = 
                    new DOMValidateContext(keySelector, sig);
            valContext.setURIDereferencer(new TIPPUriDereferencer(payloadStream));
            XMLSignature signature =
                    factory.unmarshalXMLSignature(valContext);
            boolean success = signature.validate(valContext);
            if (!success) {
                // Debug: if validation fails, see where things went wrong
                @SuppressWarnings("rawtypes")
                Iterator it = signature.getSignedInfo().getReferences().iterator();
                while (it.hasNext()) {
                    Reference ref = (Reference)it.next();
                    boolean valid = ref.validate(valContext);
                    System.out.println("validating ref " + ref.getURI() + "... " + valid);
                } 
            }
            return success;
        }
        catch (Exception e) { 
            // Possible exceptions: 
            // - javax.xml.crypto.MarshalException
            // - javax.xml.crypto.dsig.XMLSignatureException
            throw new RuntimeException(e);
        }
    }
    
    boolean hasSignature(Document doc) {
        return (findSignatureElement(doc) != null);
    }
    
    protected Node findGlobalDescriptorNode(Document doc) {
        NodeList nl = doc.getElementsByTagName(TIPPConstants.GLOBAL_DESCRIPTOR);
        if (nl.getLength() != 1) {
            throw new IllegalStateException("Malformed manifest");
        }
        return nl.item(0);
    }
    
    protected Node findSignatureElement(Document doc) {
        NodeList nl = doc.getElementsByTagNameNS(XMLSignature.XMLNS, "Signature");
        if (nl.getLength() != 1) {
            return null;
        }
        return nl.item(0);
    }
}
