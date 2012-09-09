package com.globalsight.tip;

/**
 * Represents one of the section types allowed by the specification.
 */
public enum TIPPObjectSectionType {
    BILINGUAL("bilingual"),
    INPUT("input"),
    OUTPUT("output"),
    STS("sts"),
    TM("tm"),
    TERMINOLOGY("terminology"),
    REFERENCE("reference"),
    PREVIEW("preview"),
    METRICS("metrics"),
    CUSTOM("custom");

    private static final String PREFIX = 
            "http://schema.interoperability-now.org/tipp/v1.5/sections/";
    
    private String type;
    private String name;
    
    TIPPObjectSectionType(String name) {
        this.type = PREFIX + name;
        this.name = name;
    }
    
    public String getType() {
        return type;
    }
    
    public String getDefaultName() {
        return name;
    }
    
    public static TIPPObjectSectionType byURI(String uri) {
        for (TIPPObjectSectionType t : values()) {
            if (t.type.equals(uri)) {
                return t;
            }
        }
        return null;
    }
}
