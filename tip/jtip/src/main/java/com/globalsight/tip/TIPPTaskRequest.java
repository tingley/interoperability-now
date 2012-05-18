package com.globalsight.tip;

class TIPPTaskRequest extends TIPPTask {

    TIPPTaskRequest() { super(); }
    
    public TIPPTaskRequest(String taskType, String sourceLocale, String targetLocale) {
        super(taskType, sourceLocale, targetLocale);
    }
    
    @Override
    public boolean equals(Object o) {
        return super.equals(o) && 
                (o instanceof TIPPTaskRequest);
    }
    
    @Override
    public String toString() {
        return "TaskRequest(" + super.toString() + ")";
    }
}
