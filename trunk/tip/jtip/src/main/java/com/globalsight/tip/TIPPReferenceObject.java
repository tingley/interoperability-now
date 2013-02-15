package com.globalsight.tip;

public class TIPPReferenceObject extends TIPPObjectFile {

    public enum LanguageChoice {
        source,
        target;
    }
    
    private LanguageChoice languageChoice;
    
    TIPPReferenceObject() {
        super();
    }
    
    public LanguageChoice getLanguageChoice() {
        return languageChoice;
    }
    
    public void setLanguageChoice(LanguageChoice choice) {
        this.languageChoice = choice;
    }
    
    @Override
    public int hashCode() {
        return super.hashCode() * 31 +
            (languageChoice != null ? languageChoice.hashCode() : 0);
    }
    
    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (o == null || !(o instanceof TIPPReferenceObject)) {
            return false;
        }
        TIPPReferenceObject f = (TIPPReferenceObject)o;
        if (((f.languageChoice == null && languageChoice == null) ||
             (f.languageChoice != null && f.languageChoice.equals(languageChoice))) &&
            super.equals(o)) {
            return true;
        }
        return false;
    }
}
