package com.adfonic.domain;

import java.util.UUID;
import javax.persistence.*;

@Entity
@Table(name="UPLOADED_CONTENT")
public class UploadedContent extends BusinessKey {
    private static final long serialVersionUID = 1L;

    @Id @GeneratedValue @Column(name="ID")
    private long id;
    @Column(name="EXTERNAL_ID",length=255,nullable=false)
    private String externalID;
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="CONTENT_TYPE_ID",nullable=false)
    private ContentType contentType;
    @Column(name="DATA",nullable=true)
    @Lob
    private byte[] data;

    {
	this.externalID = UUID.randomUUID().toString();
    }

    UploadedContent() {}
    
    public UploadedContent(ContentType contentType) { 
	this.contentType = contentType;
    }

    public long getId() { return id; };

    public byte[] getData() { return data; }
    public void setData(byte[] data) {
	this.data = data;
    }

    public ContentType getContentType() { return contentType; }
    public void setContentType(ContentType contentType) {
        this.contentType = contentType;
    }

    public String getExternalID() { return externalID; }
}
