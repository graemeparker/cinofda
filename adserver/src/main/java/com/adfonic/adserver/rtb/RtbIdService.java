package com.adfonic.adserver.rtb;

import com.adfonic.adserver.rtb.nativ.ByydRequest;
import com.adfonic.domain.cache.dto.adserver.FormatDto;
import com.adfonic.domain.cache.dto.adserver.adspace.AdSpaceDto;

public interface RtbIdService {
    /**
     * One-stop shop for handling an unrecognized RTB_ID.  This method takes
     * care of ensuring that if the publication needs to be created, a JMS
     * message will be queued for that.  Conversely, the implementation must
     * know when an RTB_ID already exists in the system, and the publication
     * does not need to be created.
     * @param bidRequest the BidRequest associated with the unrecognized RTB_ID
     * @param publisherId the id of the publisher
     * @param rtbId the RTB_ID value that was unrecognized
     */
    void handleUnrecognizedRtbId(ByydRequest bidRequest, long publisherId, String rtbId);

    /**
     * This method send a JMS message to link the publication with the bundle 
     * name (both passed as parameters)
     *  
     * @param bidRequest  The BidRequest associated with missing bundle id
     * @param publicationId Id of the publication
     */
    void handleBundleAssociation(ByydRequest bidRequest, Long publicationId);

    void addFormatToAdSpace(AdSpaceDto adSpace, FormatDto format);
}