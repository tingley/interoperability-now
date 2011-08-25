package com.globalsight.tip;

import java.util.Date;

public class TIPResponse {
    private String referenceId;
    private String name;
    private String id;
    private Date update;
    private Message message;
    private String comment;
    private TIPTool tool;
    
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
    }
    
    TIPResponse() {
    }
    
    TIPResponse(String referenceId, String name, String id, Date update, 
                Message message, String comment, TIPTool tool) {
        this.referenceId = referenceId;
        this.name = name;
        this.id = id;
        this.update = update;
        this.message = message;
        this.comment = comment; 
        this.setTool(tool);
    }

    public String getReferenceId() {
        return referenceId;
    }
    
    public void setReferenceId(String referenceId) {
        this.referenceId = referenceId;
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

    public Date getUpdate() {
        return update;
    }

    public void setUpdate(Date update) {
        this.update = update;
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

    public void setTool(TIPTool tool) {
        this.tool = tool;
    }

    public TIPTool getTool() {
        return tool;
    }
    
}
