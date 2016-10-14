package com.adfonic.domain;

import javax.persistence.*;

@Entity
@Table(name="CONTENT_TYPE")
public class ContentType extends BusinessKey implements Named {
    private static final long serialVersionUID = 1L;

    @Id @GeneratedValue @Column(name="ID")
    private long id;
    @Column(name="NAME",length=255,nullable=false)
    private String name;
    @Column(name="MIME_TYPE",length=255,nullable=false)
    private String mimeType;
    @Column(name="ANIMATED",nullable=false)
    private boolean animated;

    ContentType() {}

    public ContentType(String name, String mimeType) {
	this(name, mimeType, false);
    }

    public ContentType(String name, String mimeType, boolean animated) {
	this.name = name;
	this.mimeType = mimeType;
	this.animated = animated;
    }

    public long getId() { return id; };
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getMIMEType() { return mimeType; }
    public void setMIMEType(String mimeType) { this.mimeType = mimeType; }

    public boolean isAnimated() { return animated; }
    public void setAnimated(boolean animated) { this.animated = animated; }
}
