package com.adfonic.dto.audience;

import java.util.ArrayList;
import java.util.List;

import org.jdto.annotation.DTOCascade;
import org.jdto.annotation.Source;

import com.adfonic.domain.FirstPartyAudience.Type;
import com.adfonic.dto.BusinessKeyDTO;

public class FirstPartyAudienceDto extends BusinessKeyDTO {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    @Source("active")
    private boolean active;

    @Source("muidSegmentId")
    private Long muidSegmentId;

    @Source("type")
    private Type type;

    @Source("externalID")
    private String externalID;

    @DTOCascade
    @Source("campaigns")
    private List<FirstPartyAudienceCampaignDto> campaigns = new ArrayList<FirstPartyAudienceCampaignDto>(0);

    @DTOCascade
    @Source("deviceIdsUploadHistory")
    private List<FirstPartyAudienceDeviceIdsUploadHistoryDto> deviceIdsUploadHistory = new ArrayList<FirstPartyAudienceDeviceIdsUploadHistoryDto>(0);

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Long getMuidSegmentId() {
        return muidSegmentId;
    }

    public void setMuidSegmentId(Long muidSegmentId) {
        this.muidSegmentId = muidSegmentId;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public List<FirstPartyAudienceCampaignDto> getCampaigns() {
        return campaigns;
    }

    public void setCampaigns(List<FirstPartyAudienceCampaignDto> campaigns) {
        this.campaigns = campaigns;
    }

    public String getExternalID() {
        return externalID;
    }

    public void setExternalID(String externalID) {
        this.externalID = externalID;
    }

    public List<FirstPartyAudienceDeviceIdsUploadHistoryDto> getDeviceIdsUploadHistory() {
        return deviceIdsUploadHistory;
    }

    public void setDeviceIdsUploadHistory(List<FirstPartyAudienceDeviceIdsUploadHistoryDto> deviceIdsUploadHistory) {
        this.deviceIdsUploadHistory = deviceIdsUploadHistory;
    }

}
