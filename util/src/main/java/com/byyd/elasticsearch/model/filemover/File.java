package com.byyd.elasticsearch.model.filemover;

import java.util.Date;

import com.byyd.elasticsearch.model.AbstractIndexType;

// Entity which represents File type in ElasticSearch
//  "file": {
//        "properties": {
//          "fileType": {"type": "string"},
//            "subtype": {"type": "string"},
//            "companyId": {"type": "string"},
//            "advertiserId": {"type": "string"},
//            "name": {"type": "string"},
//            "status": {"type": "string"},
//            "statusLastModified": {"type": "long"},
//            "statusMessage": {"type": "string"},
//            "s3Path": {"type": "string"},
//            "s3ModificationDate": {"type": "string"},
//            "eTag": {"type": "string"},
//            "size": {"type": "long"},
//            "totals": {"type": "long"},
//            "valids": {"type": "long"},
//            "fmHost": {"type": "string"}
//        }
//    }
public class File extends AbstractIndexType{
    
    private String fmHost;
    private String name;
    private String fileType; 
    private String subtype;
    private String companyId;
    private String advertiserId; 
    private String status;
    private String statusMessage; 
    private Date statusLastModified; 
    private String s3Path;
    private Date s3ModificationDate;
    private String eTag;
    private Long size = 0L;
    private Long totals = 0L;
    private Long valids = 0L;

    public File(String id, Long version, String fmHost, String name, String fileType, String subtype, String companyId, String advertiserId, 
                String status, String statusMessage, Date statusLastModified, String s3Path, Date s3ModificationDate, String eTag, 
                Long size, Long totals, Long valids) {
        super(id, version);
        this.fmHost = fmHost;
        this.name = name;
        this.fileType = fileType;
        this.subtype = subtype;
        this.companyId = companyId;
        this.advertiserId = advertiserId;
        this.status = status;
        this.statusMessage = statusMessage;
        this.statusLastModified = statusLastModified;
        this.s3Path = s3Path;
        this.s3ModificationDate = s3ModificationDate;
        this.eTag = eTag;
        this.size = size;
        this.totals = totals;
        this.valids = valids;
    }

    public String getFmHost() {
        return fmHost;
    }

    public String getName() {
        return name;
    }

    public String getFileType() {
        return fileType;
    }

    public String getSubtype() {
        return subtype;
    }

    public String getCompanyId() {
        return companyId;
    }

    public String getAdvertiserId() {
        return advertiserId;
    }

    public String getStatus() {
        return status;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public Date getStatusLastModified() {
        return statusLastModified;
    }

    public String getS3Path() {
        return s3Path;
    }

    public Date getS3ModificationDate() {
        return s3ModificationDate;
    }

    public String geteTag() {
        return eTag;
    }

    public Long getSize() {
        return size;
    }

    public Long getTotals() {
        return totals;
    }

    public Long getValids() {
        return valids;
    }


    public enum FileStatus{
        NEW("NEW"), 
        UPDATE("UPDATE"),
        INVALID("INVALID"),
        PROCESSING("PROCESSING"),
        STORED("STORED"),
        DELETED("DELETED"),
        NOTIFYING("NOTIFYING"),
        FAILED("FAILED");
          
        private String status;
          
        FileStatus(String status){
            this.status = status;
        }

        public String getStatus() {
            return status;
        }
    }
      
    public enum FileType{
        DEVICES("DEVICES"), 
        GEOPOINTS("GEOPOINTS"),
        UNKNOWN("UNKNOWN");
          
        private String type;
        
        FileType(String type){
            this.type = type;
        }

        public String getType() {
            return type;
        }
    }
}


