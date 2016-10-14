package com.byyd.elasticsearch.model.audienceengine;

import com.byyd.elasticsearch.model.AbstractIndexType;


// Entity which represents Audience type in ElasticSearch
//  "audience": {
//        "properties": {
//          "type": {"type": "string"},
//          "status": {"type": "string"}
//        }
//    }
public class Audience extends AbstractIndexType{
    
    private String audienceType;
    private String status;
    
    public Audience(String id, Long version, String audienceType, String status) {
        super(id, version);
        this.audienceType = audienceType;
        this.status = status;
    }
    
    public String getAudienceType() {
        return audienceType;
    }
    
    public String getStatus() {
        return status;
    }

    public enum AudienceType {
        DEVICES_FILE("DEVICES_FILE"),
        LOCATIONS_FILE("LOCATIONS_FILE");
        
        private String type;
        
        private AudienceType(String type) {
            this.type = type;
        }

        public String getType() {
            return type;
        }
        
    }

    public enum AudienceStatus {
        ACTIVE("ACTIVE");
        
        private String status;
        
        private AudienceStatus(String status){
            this.status = status;
        }
        
        public String getStatus(){
            return this.status;
        }
    }  
}