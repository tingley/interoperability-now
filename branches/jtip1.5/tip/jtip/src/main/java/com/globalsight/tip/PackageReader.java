package com.globalsight.tip;

import java.io.FileNotFoundException;
import java.io.IOException;

class PackageReader {
    private PackageStore store;
    PackageReader(PackageStore store) {
        this.store = store;
    }

    PackageBase load(TIPPLoadStatus status) throws IOException {
        try {
            Manifest manifest = new Manifest(null);
            if (!manifest.loadFromStream(store.getManifestData(), status)) {
                return null;
            }
            // What kind of manifest was it?
            PackageBase tip = null;
            if (manifest.isRequest()) {
                tip = new WriteableRequestTIPP(store);
            }
            else {
                tip = new WriteableResponseTIPP(store);
            }
            tip.setManifest(manifest);
            // HACK: Doing this to resolve an ugly chicken-and-egg
            // situation.  The package is injected by the manifest into
            // the package objects when they are created, but when we are
            // creating the package from a stream, the manifest is created
            // first and the package doesn't exist yet.  So I need to go back
            // and re-inject the package once it has been created.
            for (TIPPObjectSectionType sectionType : tip.getSections()) {
                for (TIPPObjectFile file : tip.getSectionObjects(sectionType)) {
                    file.setPackage(tip);
                }
            }
            // Verify the manifest against the package contents
            new PayloadValidator().validate(manifest, store, status);
            return tip;
        }
        catch (FileNotFoundException e) {
            status.addError(TIPPError.Type.MISSING_MANIFEST);
            throw new ReportedException(e);
        }
    }
}
