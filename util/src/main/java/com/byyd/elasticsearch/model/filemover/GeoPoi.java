package com.byyd.elasticsearch.model.filemover;

import com.byyd.elasticsearch.model.AbstractIndexType;

// Entity which represents GeoPoi type in ElasticSearch
//  "geopoi": {   
//      "properties": {
//          "file": {"type": "string"},
//          "name": {"type": "string"},
//          "latitude": {"type": "string"},
//          "longitude": {"type": "string"},
//          "radius": {"type": "string"},
//          "valid": {"type": "boolean"}
//      }
//  }
public class GeoPoi extends AbstractIndexType{
    
    private String file; 
    private Boolean valid;
    private String name; 
    private String latitude; 
    private String longitude; 
    private String radius;
    
    public GeoPoi(String id, Long version, String file, Boolean valid, String name, String latitude, String longitude, String radius) {
        super(id, version);
        this.file = file;
        this.valid = valid;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.radius = radius;
    }

    public String getFile() {
        return file;
    }

    public Boolean getValid() {
        return valid;
    }

    public String getName() {
        return name;
    }

    public String getLatitude() {
        return latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public String getRadius() {
        return radius;
    }
    
}

