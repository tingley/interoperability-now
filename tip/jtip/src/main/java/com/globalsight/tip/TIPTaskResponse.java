package com.globalsight.tip;

public class TIPTaskResponse extends TIPTask {
    private String requestPackageId;
    private TIPCreator requestCreator;
    private Message message;
    private String comment;
    
    public enum Message {
        SUCCESS("Success"),
        FAILURE("Failure");
        
        private String value;
        
        Message(String value) {
            this.value = value;
        }
        public String getValue() {
            return value;
        }
        public static Message fromValue(String value) {
            for (Message m : values()) {
                if (m.getValue().equals(value)) {
                    return m;
                }
            }
            return null;
        }
        @Override
        public String toString() {
            return value;
        }
    }

    TIPTaskResponse() { super(); }
    
    public TIPTaskResponse(String taskType, String sourceLocale, String targetLocale,
                            String requestPackageId, TIPCreator requestCreator,
                            Message message, String comment) {
        super(taskType, sourceLocale, targetLocale);
        this.requestPackageId = requestPackageId;
        this.requestCreator = requestCreator;
        this.message = message;
        this.comment = comment;
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

    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
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
