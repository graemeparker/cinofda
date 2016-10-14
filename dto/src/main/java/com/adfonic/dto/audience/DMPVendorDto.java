package com.adfonic.dto.audience;

import java.util.List;

import org.jdto.annotation.DTOCascade;
import org.jdto.annotation.DTOTransient;
import org.jdto.annotation.Source;

import com.adfonic.domain.AudiencePrices;
import com.adfonic.dto.NameIdBusinessDto;
import com.adfonic.dto.publisher.PublisherDto;

/**
 * NOTE: the List<DMPAttributeDto> collection is not @DTOCascade'ed by design.
 * It will be initialized by the service according to a requested sorting spec.
 *
 * @author pierre
 *
 */
public class DMPVendorDto extends NameIdBusinessDto {

    private static final long serialVersionUID = 1L;

    @DTOTransient
    private List<DMPAttributeDto> dmpAttributes;
    
    @Source("defaultAudiencePrices")
    private AudiencePrices audiencePrices;
    
    @DTOCascade
    @Source("publishers")
    private List<PublisherDto> publishers;
    
    @Source("restricted")
    private Boolean restricted;
    
    @Source("adminOnly")
    private Boolean adminOnly;

    public List<DMPAttributeDto> getDMPAttributes() {
        return dmpAttributes;
    }

    public void setDMPAttributes(List<DMPAttributeDto> dmpAttributes) {
        this.dmpAttributes = dmpAttributes;
    }

	public AudiencePrices getAudiencePrices() {
		return audiencePrices;
	}

	public void setAudiencePrices(AudiencePrices audiencePrices) {
		this.audiencePrices = audiencePrices;
	}

	public List<PublisherDto> getPublishers() {
		return publishers;
	}

	public void setPublishers(List<PublisherDto> publishers) {
		this.publishers = publishers;
	}

	public Boolean getRestricted() {
		return restricted;
	}

	public void setRestricted(Boolean restricted) {
		this.restricted = restricted;
	}

	public Boolean getAdminOnly() {
		return adminOnly;
	}

	public void setAdminOnly(Boolean adminOnly) {
		this.adminOnly = adminOnly;
	}

}
