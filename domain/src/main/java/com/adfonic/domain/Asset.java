package com.adfonic.domain;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * Stores the data that is part of a Creative.
 */
@Entity
@Table(name="ASSET")
public class Asset extends BusinessKey implements HasExternalID {
    private static final long serialVersionUID = 1L;

    @Id @GeneratedValue @Column(name="ID")
    private long id;
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="CREATIVE_ID",nullable=false)
    private Creative creative;
    @ManyToOne(fetch=FetchType.EAGER)
    @JoinColumn(name="CONTENT_TYPE_ID",nullable=false)
    private ContentType contentType;
    @Column(name="EXTERNAL_ID",length=255,nullable=false)
    private String externalID;
    @Column(name="DATA",nullable=true)
    @Lob
    private byte[] data;

    {
        this.externalID = UUID.randomUUID().toString();
    }

    Asset() {}

    Asset(Creative creative, ContentType contentType) {
        this.creative = creative;
        this.contentType = contentType;
    }

    public long getId() { return id; };

    public byte[] getData() { return data; }
    public void setData(byte[] data) {
        this.data = data;
    }

    /**
     * Returns a String containing the textual data of this asset,
     * or null if the data is binary.
     */
    public String getDataAsString() {
        if (data != null && isText()) {
            return new String(data);
        }
        return null;
    }

    /**
     * Returns a UTF-8 String containing the textual data of this asset,
     * or null if the data is binary.
     */
    public String getDataAsUtf8String() {
        if (data != null && isText()) {
            try {
                return new String(data, "utf-8");
            } catch (java.io.UnsupportedEncodingException e) {
                throw new UnsupportedOperationException("Man, if you don't know utf-8 then what DO you know?", e);
            }
        }
        return null;
    }

    public Creative getCreative() { return creative; }

    public ContentType getContentType() { return contentType; }
    public void setContentType(ContentType contentType) {
        this.contentType = contentType;
    }

    public String getExternalID() { return externalID; }

    public boolean isText() {
        return contentType.getMIMEType().startsWith("text/");
    }
}
