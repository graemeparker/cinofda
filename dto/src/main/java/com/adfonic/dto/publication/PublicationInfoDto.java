package com.adfonic.dto.publication;

import org.jdto.annotation.Source;

import com.adfonic.dto.NameIdBusinessDto;

public class PublicationInfoDto extends NameIdBusinessDto implements Comparable<PublicationInfoDto> {

    private static final long serialVersionUID = 1L;

    @Source(value = "externalID")
    private String externalID;

    @Source(value = "status")
    private com.adfonic.domain.Publication.Status status;

    @Source(value = "disclosed")
    private boolean disclosed;

    @Source(value = "friendlyName")
    private String friendlyName;

    private boolean selected;
    private String displayName = null;

    public String getExternalID() {
        return externalID;
    }

    public void setExternalID(String externalID) {
        this.externalID = externalID;
    }

    public com.adfonic.domain.Publication.Status getStatus() {
        return status;
    }

    public void setStatus(com.adfonic.domain.Publication.Status status) {
        this.status = status;
    }

    public boolean isDisclosed() {
        return disclosed;
    }

    public void setDisclosed(boolean disclosed) {
        this.disclosed = disclosed;
    }

    public String getFriendlyName() {
        return friendlyName;
    }

    public void setFriendlyName(String friendlyName) {
        this.friendlyName = friendlyName;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public String getDisplayName() {
        if (displayName == null) {
            if (!disclosed) {
                displayName = externalID;
            } else if (friendlyName != null && !"".equals(friendlyName)) {
                displayName = friendlyName;
            } else {
                displayName = name;
            }
        }
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /** Support export based on optimization flags */
    public String getPublicationExternalId() {
    	return externalID;
    }
    
    /** Support export based on optimization flags */
    public String getPublicationName() {
    	return getDisplayName();
    }
    
    @Override
    public int compareTo(PublicationInfoDto arg0) {
        if (arg0 == null || arg0.getDisplayName() == null) {
            return 1;
        } else if (getDisplayName() == null) {
            return -1;
        } else {
            return getDisplayName().toLowerCase().compareTo(arg0.getDisplayName().toLowerCase());
        }
    }
}
