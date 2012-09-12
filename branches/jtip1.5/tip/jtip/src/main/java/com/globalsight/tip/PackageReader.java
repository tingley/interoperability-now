package com.globalsight.tip;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

class PackageReader {
    private PackageSource source;
    PackageReader(PackageSource source) {
        this.source = source;
    }

    PackageBase load(TIPPLoadStatus status) throws TIPPException, IOException {
        source.open(status);

        try {
            InputStream is = source.getPackageStream(PackageBase.MANIFEST);
            Manifest manifest = new Manifest(null);
            manifest.loadFromStream(is, status);
            // What kind of manifest was it?
            PackageBase tip = null;
            if (manifest.isRequest()) {
                tip = new WriteableRequestTIPP(source);
            }
            else {
                tip = new WriteableResponseTIPP(source);
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
            new PayloadValidator().validate(manifest, source, status);
            return tip;
        }
        catch (FileNotFoundException e) {
            status.addError(new TIPPError(TIPPError.Type.MISSING_MANIFEST));
            throw new ReportedException(e);
        }
    }
}
