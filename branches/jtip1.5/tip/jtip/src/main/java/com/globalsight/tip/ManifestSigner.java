package com.globalsight.tip;

import java.io.InputStream;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.KeyInfoFactory;
import javax.xml.crypto.dsig.keyinfo.KeyValue;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.Reference;
import javax.xml.crypto.dsig.DigestMethod;
import javax.xml.crypto.dsig.Transform;
import javax.xml.crypto.dsig.spec.TransformParameterSpec;
import javax.xml.crypto.dsig.SignedInfo;
import javax.xml.crypto.dsig.CanonicalizationMethod;
import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec;
import javax.xml.crypto.dsig.SignatureMethod;

import org.w3c.dom.Document;

@SuppressWarnings("restriction")
class ManifestSigner {
    
    XMLSignatureFactory factory;
    
    ManifestSigner() {
        factory = XMLSignatureFactory.getInstance("DOM"); 
    }

    void sign(Document manifest, InputStream payload, KeyPair kp) {
        try {            
            DOMSignContext dsc = new DOMSignContext
                    (kp.getPrivate(), manifest.getDocumentElement());
            Reference ref = factory.newReference
                    ("", factory.newDigestMethod(DigestMethod.SHA1, null),
                      Collections.singletonList
                        (factory.newTransform(Transform.ENVELOPED,
                          (TransformParameterSpec) null)), null, null); 
            List<Reference> refs = new ArrayList<Reference>();
            refs.add(ref);
            if (payload != null) {
                // TODO: include a reference for pobjects
                Reference payloadRef = factory.newReference("pobjects.zip",
                        factory.newDigestMethod(DigestMethod.SHA1, null),
                        Collections.singletonList(
                                factory.newTransform(Transform.BASE64, (TransformParameterSpec)null)),
                        null, null);
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
            // Create the signature itself
            XMLSignature signature =factory.newXMLSignature(si, ki);
            signature.sign(dsc);
        }
        catch (Exception e) {
            // TODO
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
