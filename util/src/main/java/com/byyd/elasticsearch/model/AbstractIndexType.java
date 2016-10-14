package com.byyd.elasticsearch.model;

public abstract class AbstractIndexType {
    
    // ElasticSarch id
    private String id;
    
    // Elastic search version
    private Long version;

    public AbstractIndexType(String id, Long version) {
        super();
        this.id = id;
        this.version = version;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }
}
