package com.globalsight.tip;

public class TIPTaskRequest extends TIPTask {

    TIPTaskRequest() { super(); }
    
    public TIPTaskRequest(String taskType, String sourceLocale, String targetLocale) {
        super(taskType, sourceLocale, targetLocale);
    }
    
    @Override
    public boolean equals(Object o) {
        return super.equals(o) && 
                (o instanceof TIPTaskRequest);
    }
    
    @Override
    public String toString() {
        return "TaskRequest(" + super.toString() + ")";
    }
}
