package com.adfonic.adserver;

import java.util.Map;

import com.adfonic.domain.AdSpace;
import com.adfonic.domain.Campaign;
import com.adfonic.domain.Creative;
import com.adfonic.domain.Publication;
import com.adfonic.domain.cache.dto.adserver.adspace.AdSpaceDto;
import com.adfonic.domain.cache.dto.adserver.adspace.PublicationDto;
import com.adfonic.domain.cache.dto.adserver.creative.CampaignDto;
import com.adfonic.domain.cache.dto.adserver.creative.CreativeDto;

public interface StatusChangeManager {
    /**
     * @return the status of a given AdSpace.  This method will return the
     * most recently tracked status of the object, if it has changed, or
     * it will fall back on the .getStatus() method if a status change
     * hasn't been tracked.
     */
    AdSpace.Status getStatus(AdSpaceDto adSpace);

    /**
     * @return the status of a given Campaign.  This method will return the
     * most recently tracked status of the object, if it has changed, or
     * it will fall back on the .getStatus() method if a status change
     * hasn't been tracked.
     */
    Campaign.Status getStatus(CampaignDto campaign);

    /**
     * @return the status of a given Creative.  This method will return the
     * most recently tracked status of the object, if it has changed, or
     * it will fall back on the .getStatus() method if a status change
     * hasn't been tracked.
     */
    Creative.Status getStatus(CreativeDto creative);

    /**
     * @return the status of a given Publication.  This method will return the
     * most recently tracked status of the object, if it has changed, or
     * it will fall back on the .getStatus() method if a status change
     * hasn't been tracked.
     */
    Publication.Status getStatus(PublicationDto publication);
    
    /*
     * Following 4 methods were exposed just to display the data on internal tool
     */
    
    /**
     * @return the map containing all adspace whose status was changed and the latest status 
     */
    Map<Long,AdSpace.Status> getAdSpaceStatusMap();
    
    /**
     * @return the map containing all campaign whose status was changed and the latest status 
     */
    Map<Long,Campaign.Status> getCampaignStatusMap();
    
    /**
     * @return the map containing all Creative whose status was changed and the latest status 
     */
    Map<Long,Creative.Status> getCreativeStatusMap();
    
    /**
     * @return the map containing all Publication whose status was changed and the latest status 
     */
    Map<Long,Publication.Status> getPublicationStatusMap();

}
