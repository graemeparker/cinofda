package com.byyd.elasticsearch.model.filemover;

import java.util.List;

import com.byyd.elasticsearch.model.AbstractIndexType;

// Entity which represents Device type in ElasticSearch
//  "device": {
//        "properties": {
//            "files": {"type": "string"},   //list of files which the device is linked to
//            "deviceIdType": {"type": "string"},
//            "md5Value": {"type": "string"},
//            "rawValue": {"type": "string"},
//            "sha1Value": {"type": "string"},
//            "valid": {"type": "boolean"}
//        }
//    }
public class Device extends AbstractIndexType{
    
    private List<String> files;
    private String deviceIdType;
    private Boolean valid; 
    private String rawValue; 
    private String md5Value; 
    private String sha1Value;
    
    public Device(String id, Long version, List<String> files, String deviceIdType, Boolean valid, String rawValue, String md5Value, String sha1Value) {
        super(id, version);
        this.files = files;
        this.deviceIdType = deviceIdType;
        this.valid = valid;
        this.rawValue = rawValue;
        this.md5Value = md5Value;
        this.sha1Value = sha1Value;
    }

    public List<String> getFiles() {
        return files;
    }

    public String getDeviceIdType() {
        return deviceIdType;
    }

    public Boolean getValid() {
        return valid;
    }

    public String getRawValue() {
        return rawValue;
    }

    public String getMd5Value() {
        return md5Value;
    }

    public String getSha1Value() {
        return sha1Value;
    }
}


