package com.adfonic.domain.cache.dto.adserver;

import java.util.HashMap;
import java.util.Map;

import com.adfonic.domain.cache.DomainCache;
import com.adfonic.domain.cache.dto.BusinessKeyDto;

public class ContentTypeDto extends BusinessKeyDto {
    private static final long serialVersionUID = 4L;

    private String name;
    private String mimeType;
    private boolean animated;

    private static final Map<String, ContentTypeDto> animatedContentTypes = new HashMap<String, ContentTypeDto>();
    private static final Map<String, ContentTypeDto> normalContentTypes = new HashMap<String, ContentTypeDto>();

    public static Map<String, ContentTypeDto> getContentTypes(boolean isAnimated) {
        return isAnimated ? animatedContentTypes : normalContentTypes;
    }

    private ContentTypeDto(String mimeType, boolean animated) {
    }

    private ContentTypeDto(Long id, String name, String mimeType, boolean animated) {
        this(mimeType, animated);
        this.name = name;
        this.setId(id);
    }

    public static Long getContentTypeId(String mimeType, boolean isAnimated, DomainCache domainCache) {
        ContentTypeDto contentType = isAnimated ? domainCache.getAnimatedContentTypeByMime(mimeType) : domainCache.getNormalContentTypeByMime(mimeType);
        return contentType == null ? null : contentType.getId();
    }

    private static ContentTypeDto getContentType(String mimeType, boolean animated) {
        return (animated ? animatedContentTypes : normalContentTypes).get(mimeType.toLowerCase());
    }

    public static ContentTypeDto getContentType(Long id, String name, String mimeType, boolean animated) {
        ContentTypeDto contentType = getContentType(mimeType, animated);
        if (contentType == null) {
            contentType = new ContentTypeDto(id, name, mimeType, animated);
            (animated ? animatedContentTypes : normalContentTypes).put(mimeType.toLowerCase(), contentType);
        } else if (!contentType.getId().equals(id)) {
            throw new RuntimeException("content-type already registered with a different id!");
        } else {
            contentType.name = name;
        }
        return contentType;
    }

    public String getName() {
        return name;
    }

    public String getMimeType() {
        return mimeType;
    }

    public boolean isAnimated() {
        return animated;
    }

    @Override
    public String toString() {
        return "ContentTypeDto {" + getId() + " mimeType=" + mimeType + "}";
    }
}
