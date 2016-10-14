package com.adfonic.adserver;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.adfonic.domain.cache.dto.adserver.creative.CampaignDto;
import com.adfonic.domain.cache.dto.adserver.creative.CreativeDto;

public class TestMutableWeightedCreative extends BaseAdserverTest {

    @Test
    public void testMutableWeightedCreative1() {
        MutableWeightedCreative mutableWeightedCreative = new MutableWeightedCreative();

        CampaignDto campaign = new CampaignDto();
        campaign.setId(38238L);
        campaign.setBoostFactor(2.4);
        CreativeDto creative = new CreativeDto();
        creative.setId(12345L);
        creative.setCampaign(campaign);

        long adSpaceId = 9999L;

        double ecpmWeight = 5.0;

        mutableWeightedCreative.setAdSpaceId(adSpaceId);
        mutableWeightedCreative.setCreative(creative);
        mutableWeightedCreative.setEcpmWeight(ecpmWeight);

        assertEquals(adSpaceId, mutableWeightedCreative.getAdSpaceId());
        assertEquals(creative, mutableWeightedCreative.getCreative());
        assertEquals(ecpmWeight, mutableWeightedCreative.getEcpmWeight(), .01);

        Double weight = ecpmWeight;
        assertEquals(weight, mutableWeightedCreative.getWeight(), .01);
        //System.out.println("mutableWeightedCreative="+mutableWeightedCreative);

        MutableWeightedCreative otherMutableWeightedCreative = new MutableWeightedCreative(mutableWeightedCreative);
        assertEquals(adSpaceId, otherMutableWeightedCreative.getAdSpaceId());
        assertEquals(creative, otherMutableWeightedCreative.getCreative());
        assertEquals(ecpmWeight, otherMutableWeightedCreative.getEcpmWeight(), .01);
        assertEquals(weight.intValue(), otherMutableWeightedCreative.getWeight(), .01);
        //System.out.println("otherMutableWeightedCreative="+otherMutableWeightedCreative);
    }
}
