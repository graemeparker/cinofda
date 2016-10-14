package com.adfonic.dto.format;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jdto.annotation.DTOCascade;
import org.jdto.annotation.Source;

import com.adfonic.dto.NameIdBusinessDto;
import com.adfonic.dto.campaign.creative.ContentTypeDto;

public class ContentSpecDto extends NameIdBusinessDto {
    
    private static final long serialVersionUID = 1L;

    private static final String PATTERN = "\\w+=(\\d+)\\;\\w+=(\\d+)";
    private static final String MAN_PATTERN = "\\w+=(\\d+)\\;\\w+=(\\d+);\\w+=(\\d+)";

    private Integer width;

    private Integer height;

    private Integer maxBytes;

    private String maxSize = null;

    @DTOCascade
    @Source(value = "contentTypes")
    private Set<ContentTypeDto> contentTypes;

    @Source(value = "manifest")
    private String manifest;

    public Set<ContentTypeDto> getContentTypes() {
        return contentTypes;
    }

    public void setContentTypes(Set<ContentTypeDto> contentTypes) {
        this.contentTypes = contentTypes;
    }

    public String getManifest() {
        return manifest;
    }

    public void setManifest(String manifest) {
        this.manifest = manifest;
    }

    public Integer getWidth() {
        if (width == null) {
            Pattern p;
            if (manifest.contains("maxBytes")) {
                p = Pattern.compile(MAN_PATTERN);
            } else {
                p = Pattern.compile(PATTERN);
            }
            Matcher search = p.matcher(manifest);
            if (search.find()) {
                width = Integer.valueOf(search.group(1));
            } else {
                width = 0;
            }
        }
        return width;
    }

    public void setWidth(Integer width) {
        this.width = width;
    }

    public Integer getHeight() {
        if (height == null) {
            Pattern p;
            if (manifest.contains("maxBytes")) {
                p = Pattern.compile(MAN_PATTERN);
            } else {
                p = Pattern.compile(PATTERN);
            }
            Matcher search = p.matcher(manifest);
            if (search.find()) {
                height = Integer.valueOf(search.group(2));
            } else {
                height = 0;
            }
        }
        return height;
    }

    public void setHeight(Integer height) {
        this.height = height;
    }

    public Integer getMaxBytes() {
        if (maxBytes == null) {
            Pattern p = Pattern.compile(MAN_PATTERN);
            Matcher search = p.matcher(manifest);
            if (search.find()) {
                maxBytes = Integer.valueOf(search.group(3));
            } else {
                maxBytes = 0;
            }
        }
        return maxBytes;
    }

    public void setMaxBytes(Integer maxBytes) {
        this.maxBytes = maxBytes;
    }

    public String getMaxSize() {
        if (maxSize == null) {
            switch (getWidth()) {
            case 120:
                if (getHeight() == 20) {
                    maxSize = "1.5 KB";
                } else {
                    maxSize = "30 KB";
                }
                break;
            case 168:
                maxSize = "3 KB";
                break;
            case 216:
                maxSize = "4.5 KB";
                break;
            case 300:
                if (getHeight() == 50) {
                    maxSize = "7.5 KB";
                } else {
                    maxSize = "30 KB";
                }
                break;
            case 320:
                if (getHeight() == 50) {
                    maxSize = "7.5 KB";
                } else {
                    maxSize = "50 KB";
                }
                break;
            case 728:
                maxSize = "30 KB";
                break;
            case 468:
                maxSize = "10 KB";
                break;
            case 1024:
                maxSize = "30 KB";
                break;
            default:
                maxSize = "";

            }
        }
        return maxSize;
    }

    public void setMaxSize(String maxSize) {
        this.maxSize = maxSize;
    }
}
