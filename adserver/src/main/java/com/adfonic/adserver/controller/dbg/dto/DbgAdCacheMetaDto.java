package com.adfonic.adserver.controller.dbg.dto;

import java.io.File;
import java.util.Date;

/**
 * 
 * @author mvanek
 *
 */
public class DbgAdCacheMetaDto extends DbgCacheMetaDto {

    private Date elegibilityStartedAt;

    private Date preprocessingStartedAt;

    private Date postprocessingStartedAt;

    protected DbgAdCacheMetaDto() {
    }

    public DbgAdCacheMetaDto(Date lastCheckedAt, File adCacheFile, Date populationStartedAt, Date serializationStartedAt, Date deserializationStartedAt,
            Date deserializationFinishedAt) {
        super(lastCheckedAt, adCacheFile, populationStartedAt, serializationStartedAt, deserializationStartedAt, deserializationFinishedAt);
    }

    public Date getElegibilityStartedAt() {
        return elegibilityStartedAt;
    }

    public void setElegibilityStartedAt(Date elegibilityStartedAt) {
        this.elegibilityStartedAt = elegibilityStartedAt;
    }

    public Date getPreprocessingStartedAt() {
        return preprocessingStartedAt;
    }

    public void setPreprocessingStartedAt(Date preprocessingStartedAt) {
        this.preprocessingStartedAt = preprocessingStartedAt;
    }

    public Date getPostprocessingStartedAt() {
        return postprocessingStartedAt;
    }

    public void setPostprocessingStartedAt(Date postprocessingStartedAt) {
        this.postprocessingStartedAt = postprocessingStartedAt;
    }

}
