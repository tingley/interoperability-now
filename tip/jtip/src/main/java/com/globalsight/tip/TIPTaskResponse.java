package com.globalsight.tip;

class TIPTaskResponse extends TIPTask {
    private String requestPackageId;
    private TIPCreator requestCreator;
    private TIPResponseMessage message;
    private String comment;
    

    TIPTaskResponse() { super(); }
    
    public TIPTaskResponse(String taskType, String sourceLocale, String targetLocale,
                            String requestPackageId, TIPCreator requestCreator,
                            TIPResponseMessage message, String comment) {
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
    public TIPTaskResponse(TIPTaskRequest request, 
    		String requestPackageId, TIPCreator requestCreator) {
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

    public TIPCreator getRequestCreator() {
        return requestCreator;
    }

    public void setRequestCreator(TIPCreator requestCreator) {
        this.requestCreator = requestCreator;
    }

    public TIPResponseMessage getMessage() {
        return message;
    }

    public void setMessage(TIPResponseMessage message) {
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
            !(o instanceof TIPTaskResponse)) return false;
        TIPTaskResponse r = (TIPTaskResponse)o;
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
