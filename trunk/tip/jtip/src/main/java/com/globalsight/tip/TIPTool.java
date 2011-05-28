package com.globalsight.tip;

public class TIPTool {

    private String name;
    private String id;
    private String version;
    
    TIPTool() { }

    public TIPTool(String name, String id, String version) {
        this.name = name;
        this.id = id;
        this.version = version;
    }
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
    
    /**
     * TIPTool objects are equal if and only if all fields match.  
     */
    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof TIPTool)) {
            return false;
        }
        TIPTool t = (TIPTool)o;
        return getName().equals(t.getName()) &&
               getId().equals(t.getId()) &&
               getVersion().equals(t.getVersion());
    }
    
    @Override
    public String toString() {
        return "TIPTool(name='" + name + "', id='" + id + 
                    "', version='" + version + "')";
    }
}
