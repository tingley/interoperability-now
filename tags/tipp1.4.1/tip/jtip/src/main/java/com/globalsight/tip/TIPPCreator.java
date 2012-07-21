package com.globalsight.tip;

import java.util.Date;

public class TIPPCreator {

    private String name;
    private String id;
    private Date date;
    private TIPPTool tool = new TIPPTool();

    public TIPPCreator() { }
    
    TIPPCreator(String name, String id, Date date, TIPPTool tool) {
        this.name = name;
        this.id = id;
        this.date = date;
        this.tool = tool;
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
    public Date getDate() {
        return date;
    }
    public void setDate(Date date) {
        this.date = date;
    }
    public TIPPTool getTool() {
        return tool;
    }
    public void setTool(TIPPTool tool) {
        this.tool = tool;
    }
    
    /**
     * TIPCreator objects are equal if and only if all fields match.  
     */
    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (o == null || !(o instanceof TIPPCreator)) return false;
        TIPPCreator c = (TIPPCreator)o;
        return c.getName().equals(getName()) &&
                c.getId().equals(getId()) &&
                c.getDate().equals(getDate()) &&
                c.getTool().equals(getTool());
    }
    
    @Override
    public String toString() {
        return "TIPCreator(name=" + getName() + ", id=" + getId() +
                ", date=" + getDate() + ", tool=" + getTool() + ")";
    }
}
