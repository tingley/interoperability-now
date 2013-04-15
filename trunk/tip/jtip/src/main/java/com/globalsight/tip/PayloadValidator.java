package com.globalsight.tip;

import java.util.HashSet;
import java.util.Set;

/**
 * Validates a package payload against its manifest.
 */
class PayloadValidator {
    
    /**
     * Checks the manifest against the package source and looks for 
     * discrepancies between the expected and actual objects.
     * 
     * @param manifest
     * @param source
     * @param status
     * @return true if successful, false if an error was found
     */
    boolean validate(Manifest manifest, PackageStore store, TIPPLoadStatus status) {
        int originalErrorCount = status.getAllErrors().size();
        Set<String> objectPaths = store.getObjectFilePaths();
        Set<String> pathsInManifest = new HashSet<String>();
        for (TIPPSection section : manifest.getSections()) {
            for (TIPPResource obj : section.getResources()) {
                // TODO: some form of validation needs to be factored into 
                // the resource class.. or into the section somehow.
                if (obj instanceof TIPPFile) {
                    String expectedPath = ((TIPPFile)obj).getCanonicalObjectPath();
                    if (pathsInManifest.contains(expectedPath)) {
                        status.addError(TIPPError.Type.DUPLICATE_RESOURCE_IN_MANIFEST,
                                "Duplicate resource in manifest: " + expectedPath);
                    }
                    if (!objectPaths.contains(expectedPath)) {
                        status.addError(TIPPError.Type.MISSING_PAYLOAD_RESOURCE, 
                                "Missing resource: " + expectedPath);
                    }
                    pathsInManifest.add(expectedPath);
                }
            }
        }
        // Now check in the other direction
        for (String objectPath : objectPaths) {
            if (!pathsInManifest.contains(objectPath)) {
                status.addError(TIPPError.Type.UNEXPECTED_PAYLOAD_RESOURCE, 
                                "Unexpected package resource: " + objectPath);
            }
        }
        return (originalErrorCount == status.getAllErrors().size());
    }
}
