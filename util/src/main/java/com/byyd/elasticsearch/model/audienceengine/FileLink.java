package com.byyd.elasticsearch.model.audienceengine;

import java.util.Date;

import com.byyd.elasticsearch.model.AbstractIndexType;

// Entity which represents FileLink type in ElasticSearch
//  "filelink": {
//      "properties": {
//          "audienceId" : {"type": "string" },
//          "fileId" : {"type": "string" },
//          "size" : {"type": "long" },
//          "date": {"type": "long"},
//          "sessionId"  : {"type": "string" },
//          "status"  : {"type": "string" },
//          "error"  : {"type": "string" }
//      }
//  }
public class FileLink extends AbstractIndexType{
    
    private String audienceId; 
    private String fileId; 
    private Long size; 
    private Date date; 
    private String sessionId; 
    private String status; 
    private String error;
  

    public FileLink(Long version, String audienceId, String fileId, Long size, Date date, String sessionId, String status, String error) {
        super(audienceId + "-" + fileId, version);
        this.audienceId = audienceId;
        this.fileId = fileId;
        this.size = size;
        this.date = date;
        this.sessionId = sessionId;
        this.status = status;
        this.error = error;
    }
    
    public String getAudienceId() {
        return audienceId;
    }
    
    public String getFileId() {
        return fileId;
    }
    
    public Long getSize() {
        return size;
    }

    public Date getDate() {
        return date;
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getStatus() {
        return status;
    }

    public String getError() {
        return error;
    }

    public enum FileLinkStatus {
        IN_PROGRESS("IN_PROGRESS"),
        CANCELLED("CANCELLED"),
        CANCELLING("CANCELLING"),
        FAILED("FAILED"),
        COMPLETED("COMPLETED");
        
        private String status;
        
        private FileLinkStatus(String status){
            this.status = status;
        }
        
        public String getStatus(){
            return this.status;
        }
    }
}
