package com.adfonic.dto.audience;

import java.util.ArrayList;
import java.util.List;

import org.jdto.annotation.DTOCascade;
import org.jdto.annotation.DTOTransient;
import org.jdto.annotation.Source;

import com.adfonic.dto.BusinessKeyDTO;

/**
 * NOTES: - the List<DMPSelectorDto> collection is not @DTOCascade'ed by design.
 * It will be initialized by the service according to a requested sorting spec.
 * - this DTO does not contain a reference to the AudienceDto that will contain
 * it, as to avoid circular references
 *
 * @author pierre
 *
 */
public class DMPAudienceDto extends BusinessKeyDTO {

    private static final long serialVersionUID = 1L;

    @DTOTransient
    // @DTOCascade
    // @Source("dmpSelectors")
    private List<DMPSelectorForDMPAudienceDto> dmpSelectors;

    @DTOCascade
    @Source("dmpVendor")
    private DMPVendorDto dmpVendor;

    @Source("userEnteredDMPSelectorExternalId")
    private String userEnteredDMPSelectorExternalId;

    public DMPAudienceDto() {
        this.dmpSelectors = new ArrayList<DMPSelectorForDMPAudienceDto>(0);
    }

    public List<DMPSelectorForDMPAudienceDto> getDmpSelectors() {
        return dmpSelectors;
    }

    public void setDmpSelectors(List<DMPSelectorForDMPAudienceDto> dmpSelectors) {
        this.dmpSelectors = dmpSelectors;
    }

    public DMPVendorDto getDmpVendor() {
        return dmpVendor;
    }

    public void setDmpVendor(DMPVendorDto dmpVendor) {
        this.dmpVendor = dmpVendor;
    }

    public String getUserEnteredDMPSelectorExternalId() {
        return userEnteredDMPSelectorExternalId;
    }

    public void setUserEnteredDMPSelectorExternalId(String userEnteredDMPSelectorExternalId) {
        this.userEnteredDMPSelectorExternalId = userEnteredDMPSelectorExternalId;
    }

}
