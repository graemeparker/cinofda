package com.adfonic.dto.targetpublisher;

import org.jdto.annotation.Source;

import com.adfonic.dto.BusinessKeyDTO;
import com.adfonic.dto.publisher.PublisherDto;

public class TargetPublisherDto extends BusinessKeyDTO {

    private static final long serialVersionUID = 1L;

    @Source(value = "publisher")
    private PublisherDto publisher;

    @Source(value = "rtb")
    private boolean rtb;

    @Source(value = "pmpAvailable")
    private boolean pmpAvailable;

    @Source(value = "name")
    private String name;

    @Source(value = "displayPriority")
    private int displayPriority;

    @Source(value = "hidden")
    private boolean hidden;

    public PublisherDto getPublisher() {
        return publisher;
    }

    public void setPublisher(PublisherDto publisher) {
        this.publisher = publisher;
    }

    public boolean isRtb() {
        return rtb;
    }

    public boolean isPmpAvailable() {
        return pmpAvailable;
    }

    public void setRtb(boolean isRtb) {
        this.rtb = isRtb;
    }

    public void setPmpAvailable(boolean pmpAvailable) {
        this.pmpAvailable = pmpAvailable;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getDisplayPriority() {
        return displayPriority;
    }

    public void setDisplayPriority(int displayPriority) {
        this.displayPriority = displayPriority;
    }

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }
}
