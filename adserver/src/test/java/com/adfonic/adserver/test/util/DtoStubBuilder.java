package com.adfonic.adserver.test.util;

import com.adfonic.adserver.AdEvent;
import com.adfonic.adserver.BidDetails;
import com.adfonic.adserver.Impression;
import com.adfonic.adserver.TargetingContext;
import com.adfonic.domain.AdAction;
import com.adfonic.domain.cache.dto.adserver.adspace.AdSpaceDto;
import com.adfonic.domain.cache.dto.adserver.creative.CampaignDto;
import com.adfonic.domain.cache.dto.adserver.creative.CreativeDto;
import com.adfonic.test.AbstractAdfonicTest;

public class DtoStubBuilder extends AbstractAdfonicTest {
    public static Impression createImpressionRandomStub() {

        Impression impression =  new Impression();
        impression.setAdSpaceId(randomLong());
        impression.setExternalID(randomAlphaNumericString(10));

        return impression;
    }

    public static BidDetails createBidDetailsRandomStub(final TargetingContext context) {
        return createBidDetailsRandomStub(context, "10.10.10.10");
    }

    public static BidDetails createBidDetailsRandomStub(final TargetingContext context, final String ipAddress) {
        return new BidDetails() {

            private Impression impression = createImpressionRandomStub();

            @Override
            public String getIpAddress() {
                return ipAddress;
            }

            @Override
            public Impression getImpression() {
                return impression;
            }

            @Override
            public TargetingContext getBidTimeTargetingContext() {
                return context;
            }
        };
    }


    public static AdSpaceDto createAdSpaceRandomStub() {
        return new AdSpaceDto();
    }

    public static CreativeDto createCreativeRandomStub() {
    	
    	CreativeDto creativeDto = new CreativeDto();
    	creativeDto.setId(0L);
        return creativeDto;
    }
    
    public static CampaignDto createCampaignRandomStub() {
    	
    	CampaignDto campaignDto = new CampaignDto();
        return campaignDto;
    }   

    public static AdEvent createAdEvent(AdAction adAction){
        AdEvent adEvent = new AdEvent();
        adEvent.setAdAction(adAction);

        return adEvent;
    }

}
