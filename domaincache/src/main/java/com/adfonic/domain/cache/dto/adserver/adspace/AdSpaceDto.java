package com.adfonic.domain.cache.dto.adserver.adspace;

import java.util.HashSet;
import java.util.Set;

import com.adfonic.domain.AdSpace;
import com.adfonic.domain.UnfilledAction;
import com.adfonic.domain.cache.dto.BusinessKeyDto;

public class AdSpaceDto extends BusinessKeyDto {
    private static final long serialVersionUID = 1L;

    private String name;
    private String externalID;
    private AdSpace.Status status;
    private UnfilledAction unfilledAction;
    private boolean backfillEnabled;
    private AdSpace.ColorScheme colorScheme;
    private PublicationDto publication;
    private Set<Long> formatIds = new HashSet<Long>();

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getExternalID() {
        return externalID;
    }
    public void setExternalID(String externalID) {
        this.externalID = externalID;
    }

    public AdSpace.Status getStatus() {
        return status;
    }
    public void setStatus(AdSpace.Status status) {
        this.status = status;
    }

    public UnfilledAction getUnfilledAction() {
        return unfilledAction;
    }
    public void setUnfilledAction(UnfilledAction unfilledAction) {
        this.unfilledAction = unfilledAction;
    }

    public boolean isBackfillEnabled() {
        return backfillEnabled;
    }
    public void setBackfillEnabled(boolean backfillEnabled) {
        this.backfillEnabled = backfillEnabled;
    }

    public AdSpace.ColorScheme getColorScheme() {
        return colorScheme;
    }
    public void setColorScheme(AdSpace.ColorScheme colorScheme) {
        this.colorScheme = colorScheme;
    }

    public PublicationDto getPublication() {
        return publication;
    }
    public void setPublication(PublicationDto publication) {
        this.publication = publication;
    }

    public Set<Long> getFormatIds() {
        return formatIds;
    }
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("AdSpaceDto {");
		builder.append(getId());
		builder.append(" name=");
		builder.append(name);
		builder.append(", externalID=");
		builder.append(externalID);
		builder.append(", status=");
		builder.append(status);
		builder.append(", unfilledAction=");
		builder.append(unfilledAction);
		builder.append(", backfillEnabled=");
		builder.append(backfillEnabled);
		builder.append(", colorScheme=");
		builder.append(colorScheme);
		builder.append(", publication=");
		builder.append(publication);
		builder.append(", formatIds=");
		builder.append(formatIds);
		builder.append("}");
		return builder.toString();
	}
    
}
