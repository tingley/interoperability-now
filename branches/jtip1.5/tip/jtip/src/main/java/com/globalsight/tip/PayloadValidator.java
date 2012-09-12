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
    boolean validate(Manifest manifest, PackageSource source, TIPPLoadStatus status) {
       
        // TODO: catch things that are listed in the manifest twice!
        int originalErrorCount = status.getAllErrors().size();
        Set<String> objectPaths = source.getPackageObjects();
        Set<String> pathsInManifest = new HashSet<String>();
        for (TIPPObjectSection section : manifest.getObjectSections()) {
            for (TIPPObjectFile obj : section.getObjectFiles()) {
                String expectedPath = PackageSource.SEPARATOR + section.getName() + 
                        PackageSource.SEPARATOR + obj.getLocation();
                if (pathsInManifest.contains(expectedPath)) {
                    status.addError(new TIPPError(TIPPError.Type.DUPLICATE_RESOURCE_IN_MANIFEST,
                            "Duplicate resource in manifest: " + expectedPath));
                }
                if (!objectPaths.contains(expectedPath)) {
                    status.addError(new TIPPError(TIPPError.Type.MISSING_PAYLOAD_RESOURCE, 
                            "Missing resource: " + expectedPath));
                }
                pathsInManifest.add(expectedPath);
            }
        }
        // Now check in the other direction
        for (String objectPath : objectPaths) {
            if (!pathsInManifest.contains(objectPath)) {
                status.addError(new TIPPError(TIPPError.Type.UNEXPECTED_PAYLOAD_RESOURCE, 
                                "Unexpected package resource: " + objectPath));
            }
        }
        return (originalErrorCount == status.getAllErrors().size());
    }
}
