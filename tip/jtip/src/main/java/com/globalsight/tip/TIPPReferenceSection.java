package com.globalsight.tip;

public class TIPPReferenceSection extends TIPPSection {

    public TIPPReferenceSection() {
        super(TIPPSectionType.REFERENCE);
    }
    
    protected TIPPFile createFile(String name) {
        return new TIPPReferenceFile(name, name);
    }
    
    public TIPPReferenceFile addFile(String name) {
        return (TIPPReferenceFile)super.addFile(name);
    }
}
