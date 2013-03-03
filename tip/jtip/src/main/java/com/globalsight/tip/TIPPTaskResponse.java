package com.globalsight.tip;

class TIPPTaskResponse extends TIPPTask {
    private String requestPackageId;
    private TIPPCreator requestCreator;
    private TIPPResponseCode message;
    private String comment;
    

    TIPPTaskResponse() { super(); }
    
    public TIPPTaskResponse(String taskType, String sourceLocale, String targetLocale,
                            String requestPackageId, TIPPCreator requestCreator,
                            TIPPResponseCode message, String comment) {
        super(taskType, sourceLocale, targetLocale);
        this.requestPackageId = requestPackageId;
        this.requestCreator = requestCreator;
        this.message = message;
        this.comment = comment;
    }
    
    /**
     * Create a response header based on an existing request Manifest.
     * @param request
     */
    public TIPPTaskResponse(TIPPTaskRequest request, 
    		String requestPackageId, TIPPCreator requestCreator) {
    	super(request.getTaskType(), request.getSourceLocale(), 
    		  request.getTargetLocale());
    	this.requestPackageId = requestPackageId;
    	this.requestCreator = requestCreator;
    }

    public String getRequestPackageId() {
        return requestPackageId;
    }

    public void setRequestPackageId(String requestPackageId) {
        this.requestPackageId = requestPackageId;
    }

    public TIPPCreator getRequestCreator() {
        return requestCreator;
    }

    public void setRequestCreator(TIPPCreator requestCreator) {
        this.requestCreator = requestCreator;
    }

    public TIPPResponseCode getMessage() {
        return message;
    }

    public void setMessage(TIPPResponseCode message) {
        this.message = message;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
    
    @Override
    public boolean equals(Object o) {
        if (!super.equals(o) || 
            !(o instanceof TIPPTaskResponse)) return false;
        TIPPTaskResponse r = (TIPPTaskResponse)o;
        return r.getMessage().equals(getMessage()) &&
                r.getComment().equals(getComment()) &&
                r.getRequestPackageId().equals(getRequestPackageId()) &&
                r.getRequestCreator().equals(getRequestCreator());
    }
    
    @Override
    public String toString() {
        return "TaskResponse(task=" + super.toString() + ", message=" + getMessage() +
                ", commment='" + getComment() + "', requestId=" + 
                getRequestPackageId() + ", requestCreator=" + requestCreator + ")";
    }

}
