package com.adfonic.dto.campaign;

import org.jdto.annotation.Source;

import com.adfonic.dto.NameIdBusinessDto;

public class CampaignInfoDto extends NameIdBusinessDto{

    private static final long serialVersionUID = 1L;

    // Field to store the IO Reference in UI
    @Source(value = "reference")
    private String reference;
    
    // Field to store the Opportunity ID in UI
    @Source(value = "opportunity")
    private String opportunity;

    @Source(value = "externalID")
    private String externalID;
    
    @Override
    public void setName(String name) {
        this.name = name;
    }
    
    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }
    
    public String getOpportunity() {
		return opportunity;
	}

	public void setOpportunity(String opportunity) {
		this.opportunity = opportunity;
	}

	public String getExternalID() {
        return externalID;
    }

    public void setExternalID(String externalID) {
        this.externalID = externalID;
    }
    
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("CampaignDto [name=");
        builder.append(name);
        builder.append(",");
        builder.append(super.toString());
        builder.append("]");
        return builder.toString();
    }
    
}
