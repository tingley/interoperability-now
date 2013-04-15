package com.globalsight.tip;

import java.io.FileNotFoundException;
import java.io.IOException;

import javax.xml.crypto.KeySelector;

class PackageReader {
    private PackageStore store;
    PackageReader(PackageStore store) {
        this.store = store;
    }

    PackageBase load(TIPPLoadStatus status, KeySelector keySelector) throws IOException {
        try {
            Manifest manifest = new Manifest(null);
            if (!manifest.loadFromStream(store.getManifestData(), status, keySelector, 
                                         store.getRawPayloadData())) {
                return null;
            }
            // What kind of manifest was it?
            PackageBase tipp = null;
            if (manifest.isRequest()) {
                tipp = new RequestPackageBase(store);
            }
            else {
                tipp = new ResponsePackageBase(store);
            }
            tipp.setManifest(manifest);
            // HACK: Doing this to resolve an ugly chicken-and-egg
            // situation.  The package is injected by the manifest into
            // the package objects when they are created, but when we are
            // creating the package from a stream, the manifest is created
            // first and the package doesn't exist yet.  So I need to go back
            // and re-inject the package once it has been created.
            for (TIPPSection section : tipp.getSections()) {
                for (TIPPResource file : section.getResources()) {
                    file.setPackage(tipp);
                }
            }
            // Verify the manifest against the package contents
            new PayloadValidator().validate(manifest, store, status);
            return tipp;
        }
        catch (FileNotFoundException e) {
            status.addError(TIPPError.Type.MISSING_MANIFEST);
            throw new ReportedException(e);
        }
    }
}
