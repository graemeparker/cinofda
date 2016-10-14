package com.adfonic.adserver.controller.dbg.dto;

import java.io.File;
import java.util.Date;

/**
 * 
 * @author mvanek
 *
 */
public class DbgCacheMetaDto {

    private Date lastCheckedAt;

    private Long fileSize;

    private String fileName;

    private Date fileModifiedAt;

    private Date populationStartedAt;

    private Date serializationStartedAt;

    private Date deserializationStartedAt;

    private Date deserializationFinishedAt;

    protected DbgCacheMetaDto() {
        //jackson
    }

    public DbgCacheMetaDto(Date lastCheckedAt, File adCacheFile, Date populationStartedAt, Date serializationStartedAt, Date deserializationStartedAt,
            Date deserializationFinishedAt) {
        this.lastCheckedAt = lastCheckedAt;

        this.fileName = adCacheFile.getName();
        this.fileSize = adCacheFile.length();
        this.fileModifiedAt = new Date(adCacheFile.lastModified());

        this.populationStartedAt = populationStartedAt;
        this.serializationStartedAt = serializationStartedAt;
        this.deserializationStartedAt = deserializationStartedAt;
        this.deserializationFinishedAt = deserializationFinishedAt;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Date getSerializationStartedAt() {
        return serializationStartedAt;
    }

    public void setSerializationStartedAt(Date serializedAt) {
        this.serializationStartedAt = serializedAt;
    }

    public Date getFileModifiedAt() {
        return fileModifiedAt;
    }

    public void setFileModifiedAt(Date downloadedAt) {
        this.fileModifiedAt = downloadedAt;
    }

    public Date getLastCheckedAt() {
        return lastCheckedAt;
    }

    public void setLastCheckedAt(Date refreshedAt) {
        this.lastCheckedAt = refreshedAt;
    }

    public Date getPopulationStartedAt() {
        return populationStartedAt;
    }

    public Date getDeserializationStartedAt() {
        return deserializationStartedAt;
    }

    public Date getDeserializationFinishedAt() {
        return deserializationFinishedAt;
    }

}
