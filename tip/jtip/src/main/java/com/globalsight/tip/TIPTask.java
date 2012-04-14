package com.globalsight.tip;

class TIPTask {

    private String taskType;
    private String sourceLocale, targetLocale;

    TIPTask() { }
    
    public TIPTask(String taskType, String sourceLocale, String targetLocale) {
        this.taskType = taskType;
        this.sourceLocale = sourceLocale;
        this.targetLocale = targetLocale;
    }
    
    public String getTaskType() {
        return taskType;
    }
    public void setTaskType(String taskType) {
        this.taskType = taskType;
    }
    public String getSourceLocale() {
        return sourceLocale;
    }
    public void setSourceLocale(String sourceLocale) {
        this.sourceLocale = sourceLocale;
    }
    public String getTargetLocale() {
        return targetLocale;
    }
    public void setTargetLocale(String targetLocale) {
        this.targetLocale = targetLocale;
    }
    
    /**
     * TIPCreator objects are equal if and only if all fields match.  
     */
    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (o == null || !(o instanceof TIPTask)) return false;
        TIPTask t = (TIPTask)o;
        return t.getTaskType().equals(getTaskType()) &&
                t.getSourceLocale().equals(getSourceLocale()) &&
                t.getTargetLocale().equals(getTargetLocale());
    }
    
    @Override
    public String toString() {
        return getTaskType() + "[" + getSourceLocale() + "->" + getTargetLocale()
                + "]";
    }
}
