package com.adfonic.domainserializer;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.jmock.Expectations;
import org.junit.Before;
import org.junit.Test;

import com.adfonic.domain.cache.dto.adserver.adspace.AdSpaceDto;
import com.adfonic.domain.cache.dto.adserver.adspace.PublicationDto;
import com.adfonic.domain.cache.dto.adserver.adspace.PublisherDto;
import com.adfonic.domain.cache.dto.adserver.creative.CampaignBidDto;
import com.adfonic.domain.cache.dto.adserver.creative.CampaignDto;
import com.adfonic.domain.cache.dto.adserver.creative.CreativeDto;
import com.adfonic.domain.cache.dto.adserver.creative.SegmentDto;
import com.adfonic.domain.cache.listener.DSRejectionListenerImpl;
import com.adfonic.domainserializer.EligibilityChecker;
import com.adfonic.domainserializer.loader.AdCacheBuildContext;
import com.adfonic.test.AbstractAdfonicTest;

public class TestAdserverDomainCacheLoader extends AbstractAdfonicTest {

    EligibilityChecker eligibilityChecker;
    DSRejectionListenerImpl listener = new DSRejectionListenerImpl();

    @Before
    public void init() {
        eligibilityChecker = new EligibilityChecker(null, 1, true);
    }

    /*
     * Test when a Campaign doesn't belong to DSP
     */
    @Test
    public void test01_isEligibleBasedOnSegmentTargettedPublisher() {
        AdCacheBuildContext td = new AdCacheBuildContext();
        final CreativeDto creative = new CreativeDto();
        final CampaignDto campaign = new CampaignDto();
        final CampaignBidDto campaignBid = new CampaignBidDto();
        final SegmentDto segment = new SegmentDto();
        final boolean maximum = false;
        creative.setCampaign(campaign);
        campaign.setCurrentBid(campaignBid);
        campaignBid.setMaximum(maximum);
        creative.setSegment(segment);

        final AdSpaceDto adspace = mock(AdSpaceDto.class, "adspace");
        expect(new Expectations() {
            {
                allowing(adspace).getPublication();
            }
        });
        assertTrue(eligibilityChecker.isEligibleBasedOnSegmentTargettedPublisher(creative, adspace, td));
    }

    /*
     * Test when a Campaign belongs to DSP(campaignBid.maximum=true) and adspace publisher id is one of the published displayed on UI
     * and No publisher selected on UI (Set is null)
     */
    @Test
    public void test02_isEligibleBasedOnSegmentTargettedPublisher() {
        final AdCacheBuildContext td = mock(AdCacheBuildContext.class, "td");
        final CreativeDto creative = new CreativeDto();
        final SegmentDto segment = new SegmentDto();
        final CampaignDto campaign = new CampaignDto();
        final CampaignBidDto campaignBid = new CampaignBidDto();
        final boolean maximum = true;
        final Long segmentId = randomLong();
        final boolean isIncludeAdfonicNetwork = false;

        creative.setCampaign(campaign);
        creative.setSegment(segment);
        campaign.setCurrentBid(campaignBid);
        campaignBid.setMaximum(maximum);
        segment.setId(segmentId);
        segment.setIncludeAdfonicNetwork(isIncludeAdfonicNetwork);

        final AdSpaceDto adspace = new AdSpaceDto();
        final PublicationDto publication = new PublicationDto();
        final PublisherDto publisher = new PublisherDto();
        final Long publisherId = randomLong();
        final boolean isTargettedPublisherId = true;
        final Set<Long> targettedPublisherIdList = null;
        adspace.setPublication(publication);
        publication.setPublisher(publisher);
        publisher.setId(publisherId);

        expect(new Expectations() {
            {
                allowing(td).getDsListener();
                will(returnValue(listener));
                allowing(td).isTargettedPublisherId(publisherId);
                will(returnValue(isTargettedPublisherId));
                allowing(td).getSegmentTargettedPublisherBySegmentId(segmentId);
                will(returnValue(targettedPublisherIdList));

            }
        });
        assertTrue(eligibilityChecker.isEligibleBasedOnSegmentTargettedPublisher(creative, adspace, td));
    }

    /*
     * Test when a Campaign belongs to DSP(campaignBid.maximum=true) and adspace publisher id is one of the published displayed on UI
     * and No publisher selected on UI (Set is null)
     */
    @Test
    public void test02_01_isEligibleBasedOnSegmentTargettedPublisher() {
        final AdCacheBuildContext td = mock(AdCacheBuildContext.class, "td");
        final CreativeDto creative = new CreativeDto();
        final SegmentDto segment = new SegmentDto();
        final CampaignDto campaign = new CampaignDto();
        final CampaignBidDto campaignBid = new CampaignBidDto();
        final boolean maximum = true;
        final Long segmentId = randomLong();
        final boolean isIncludeAdfonicNetwork = true;

        creative.setCampaign(campaign);
        creative.setSegment(segment);
        campaign.setCurrentBid(campaignBid);
        campaignBid.setMaximum(maximum);
        segment.setId(segmentId);
        segment.setIncludeAdfonicNetwork(isIncludeAdfonicNetwork);

        final AdSpaceDto adspace = new AdSpaceDto();
        final PublicationDto publication = new PublicationDto();
        final PublisherDto publisher = new PublisherDto();
        final Long publisherId = randomLong();
        final boolean isTargettedPublisherId = true;
        final Set<Long> targettedPublisherIdList = null;

        adspace.setPublication(publication);
        publication.setPublisher(publisher);
        publisher.setId(publisherId);

        expect(new Expectations() {
            {
                allowing(td).getDsListener();
                will(returnValue(listener));
                allowing(td).isTargettedPublisherId(publisherId);
                will(returnValue(isTargettedPublisherId));
                allowing(td).getSegmentTargettedPublisherBySegmentId(segmentId);
                will(returnValue(targettedPublisherIdList));

            }
        });
        assertFalse(eligibilityChecker.isEligibleBasedOnSegmentTargettedPublisher(creative, adspace, td));
    }

    /*
     * Test when a Campaign belongs to DSP(campaignBid.maximum=true) and adspace publisher id is one of the published displayed on UI
     * and No publisher selected on UI (Set is not null and Empty)
     */
    @Test
    public void test03_isEligibleBasedOnSegmentTargettedPublisher() {
        final AdCacheBuildContext td = mock(AdCacheBuildContext.class, "td");
        final CreativeDto creative = new CreativeDto();
        final SegmentDto segment = new SegmentDto();
        final CampaignDto campaign = new CampaignDto();
        final CampaignBidDto campaignBid = new CampaignBidDto();
        final boolean maximum = true;
        final Long segmentId = randomLong();
        final boolean isIncludeAdfonicNetwork = false;

        creative.setCampaign(campaign);
        creative.setSegment(segment);
        campaign.setCurrentBid(campaignBid);
        campaignBid.setMaximum(maximum);
        segment.setId(segmentId);
        segment.setIncludeAdfonicNetwork(isIncludeAdfonicNetwork);

        final AdSpaceDto adspace = new AdSpaceDto();
        final PublicationDto publication = new PublicationDto();
        final PublisherDto publisher = new PublisherDto();
        final Long publisherId = randomLong();
        final boolean isTargettedPublisherId = true;
        final Set<Long> targettedPublisherIdList = new HashSet<Long>();

        adspace.setPublication(publication);
        publication.setPublisher(publisher);
        publisher.setId(publisherId);

        expect(new Expectations() {
            {
                allowing(td).getDsListener();
                will(returnValue(listener));
                allowing(td).isTargettedPublisherId(publisherId);
                will(returnValue(isTargettedPublisherId));
                allowing(td).getSegmentTargettedPublisherBySegmentId(segmentId);
                will(returnValue(targettedPublisherIdList));

            }
        });
        assertTrue(eligibilityChecker.isEligibleBasedOnSegmentTargettedPublisher(creative, adspace, td));
    }

    @Test
    public void test03_01_isEligibleBasedOnSegmentTargettedPublisher() {
        final AdCacheBuildContext td = mock(AdCacheBuildContext.class, "td");
        final CreativeDto creative = new CreativeDto();
        final SegmentDto segment = new SegmentDto();
        final CampaignDto campaign = new CampaignDto();
        final CampaignBidDto campaignBid = new CampaignBidDto();
        final boolean maximum = true;
        final Long segmentId = randomLong();
        final boolean isIncludeAdfonicNetwork = true;

        creative.setCampaign(campaign);
        creative.setSegment(segment);
        campaign.setCurrentBid(campaignBid);
        campaignBid.setMaximum(maximum);
        segment.setId(segmentId);
        segment.setIncludeAdfonicNetwork(isIncludeAdfonicNetwork);

        final AdSpaceDto adspace = new AdSpaceDto();
        final PublicationDto publication = new PublicationDto();
        final PublisherDto publisher = new PublisherDto();
        final Long publisherId = randomLong();
        final boolean isTargettedPublisherId = true;
        final Set<Long> targettedPublisherIdList = new HashSet<Long>();

        adspace.setPublication(publication);
        publication.setPublisher(publisher);
        publisher.setId(publisherId);

        expect(new Expectations() {
            {
                allowing(td).getDsListener();
                will(returnValue(listener));
                allowing(td).isTargettedPublisherId(publisherId);
                will(returnValue(isTargettedPublisherId));
                allowing(td).getSegmentTargettedPublisherBySegmentId(segmentId);
                will(returnValue(targettedPublisherIdList));

            }
        });
        assertFalse(eligibilityChecker.isEligibleBasedOnSegmentTargettedPublisher(creative, adspace, td));
    }

    /*
     * Test when a Campaign belongs to DSP(campaignBid.maximum=true) and adspace publisher id is one of the published displayed on UI
     * and adspace publisher selected on UI (Set is not null and non Empty)
     */
    @Test
    public void test04_isEligibleBasedOnSegmentTargettedPublisher() {
        final AdCacheBuildContext td = mock(AdCacheBuildContext.class, "td");
        final CreativeDto creative = new CreativeDto();
        final SegmentDto segment = new SegmentDto();
        final CampaignDto campaign = new CampaignDto();
        final CampaignBidDto campaignBid = new CampaignBidDto();
        final boolean maximum = true;
        final Long segmentId = randomLong();

        creative.setCampaign(campaign);
        creative.setSegment(segment);
        campaign.setCurrentBid(campaignBid);
        campaignBid.setMaximum(maximum);
        segment.setId(segmentId);

        final AdSpaceDto adspace = new AdSpaceDto();
        final PublicationDto publication = new PublicationDto();
        final PublisherDto publisher = new PublisherDto();
        final Long publisherId = randomLong();
        final boolean isTargettedPublisherId = true;
        final Set<Long> targettedPublisherIdList = new HashSet<Long>();
        targettedPublisherIdList.add(publisherId);

        adspace.setPublication(publication);
        publication.setPublisher(publisher);
        publisher.setId(publisherId);

        expect(new Expectations() {
            {
                allowing(td).getDsListener();
                will(returnValue(listener));
                allowing(td).isTargettedPublisherId(publisherId);
                will(returnValue(isTargettedPublisherId));
                allowing(td).getSegmentTargettedPublisherBySegmentId(segmentId);
                will(returnValue(targettedPublisherIdList));

            }
        });
        assertTrue(eligibilityChecker.isEligibleBasedOnSegmentTargettedPublisher(creative, adspace, td));
    }

    /*
     * Test when a Campaign belongs to DSP(campaignBid.maximum=true) and adspace publisher id is one of the published displayed on UI
     * and some publisher selected on UI (Set is not null and non Empty), but adspace publisher is not selected
     */
    @Test
    public void test05_isEligibleBasedOnSegmentTargettedPublisher() {
        final AdCacheBuildContext td = mock(AdCacheBuildContext.class, "td");
        final CreativeDto creative = new CreativeDto();
        final SegmentDto segment = new SegmentDto();
        final CampaignDto campaign = new CampaignDto();
        final CampaignBidDto campaignBid = new CampaignBidDto();
        final boolean maximum = true;
        final Long segmentId = randomLong();

        creative.setCampaign(campaign);
        creative.setSegment(segment);
        campaign.setCurrentBid(campaignBid);
        campaignBid.setMaximum(maximum);
        segment.setId(segmentId);

        final AdSpaceDto adspace = new AdSpaceDto();
        final PublicationDto publication = new PublicationDto();
        final PublisherDto publisher = new PublisherDto();
        final Long publisherId = randomLong();
        final Long seomeSelectedPublisherId = randomLong();

        final boolean isTargettedPublisherId = true;
        final Set<Long> targettedPublisherIdList = new HashSet<Long>();
        targettedPublisherIdList.add(seomeSelectedPublisherId);

        adspace.setPublication(publication);
        publication.setPublisher(publisher);
        publisher.setId(publisherId);

        expect(new Expectations() {
            {
                allowing(td).getDsListener();
                will(returnValue(listener));
                allowing(td).isTargettedPublisherId(publisherId);
                will(returnValue(isTargettedPublisherId));
                allowing(td).getSegmentTargettedPublisherBySegmentId(segmentId);
                will(returnValue(targettedPublisherIdList));

            }
        });
        assertFalse(eligibilityChecker.isEligibleBasedOnSegmentTargettedPublisher(creative, adspace, td));
    }

    /*
     * Test when a Campaign belongs to DSP(campaignBid.maximum=true) and adspace publisher id is
     * NOT one of the published displayed on UI
     * and some publisher selected on UI (Set is not null and non Empty), but adspace publisher is not selected
     */
    @Test
    public void test06_isEligibleBasedOnSegmentTargettedPublisher() {
        final AdCacheBuildContext td = mock(AdCacheBuildContext.class, "td");
        final CreativeDto creative = new CreativeDto();
        final SegmentDto segment = new SegmentDto();
        final CampaignDto campaign = new CampaignDto();
        final CampaignBidDto campaignBid = new CampaignBidDto();
        final boolean maximum = true;
        final Long segmentId = randomLong();
        final boolean isIncludeAdfonicNetwork = true;

        creative.setCampaign(campaign);
        creative.setSegment(segment);
        campaign.setCurrentBid(campaignBid);
        campaignBid.setMaximum(maximum);
        segment.setId(segmentId);
        segment.setIncludeAdfonicNetwork(isIncludeAdfonicNetwork);

        final AdSpaceDto adspace = new AdSpaceDto();
        final PublicationDto publication = new PublicationDto();
        final PublisherDto publisher = new PublisherDto();
        final Long publisherId = randomLong();
        final boolean isTargettedPublisherId = false;

        adspace.setPublication(publication);
        publication.setPublisher(publisher);
        publisher.setId(publisherId);

        expect(new Expectations() {
            {
                allowing(td).getDsListener();
                will(returnValue(listener));
                allowing(td).isTargettedPublisherId(publisherId);
                will(returnValue(isTargettedPublisherId));
                allowing(td).getSegmentTargettedPublisherBySegmentId(segmentId);
                will(returnValue(null));

            }
        });
        assertTrue(eligibilityChecker.isEligibleBasedOnSegmentTargettedPublisher(creative, adspace, td));
    }

    /*
     * Test when a Campaign belongs to DSP(campaignBid.maximum=true) and adspace publisher id is
     * NOT one of the published displayed on UI
     * and some publisher selected on UI (Set is not null and non Empty), but adspace publisher is not selected
     */
    @Test
    public void test07_isEligibleBasedOnSegmentTargettedPublisher() {
        final AdCacheBuildContext td = mock(AdCacheBuildContext.class, "td");
        final CreativeDto creative = new CreativeDto();
        final SegmentDto segment = new SegmentDto();
        final CampaignDto campaign = new CampaignDto();
        final CampaignBidDto campaignBid = new CampaignBidDto();
        final boolean maximum = true;
        final Long segmentId = randomLong();
        final boolean isIncludeAdfonicNetwork = false;

        creative.setCampaign(campaign);
        creative.setSegment(segment);
        campaign.setCurrentBid(campaignBid);
        campaignBid.setMaximum(maximum);
        segment.setId(segmentId);
        segment.setIncludeAdfonicNetwork(isIncludeAdfonicNetwork);

        final AdSpaceDto adspace = new AdSpaceDto();
        final PublicationDto publication = new PublicationDto();
        final PublisherDto publisher = new PublisherDto();
        final Long publisherId = randomLong();
        final boolean isTargettedPublisherId = false;

        final Set<Long> segmentTargettedPublisherIds = new HashSet<Long>();
        segmentTargettedPublisherIds.add(randomLong());

        adspace.setPublication(publication);
        publication.setPublisher(publisher);
        publisher.setId(publisherId);

        expect(new Expectations() {
            {
                allowing(td).getDsListener();
                will(returnValue(listener));
                allowing(td).isTargettedPublisherId(publisherId);
                will(returnValue(isTargettedPublisherId));
                allowing(td).getSegmentTargettedPublisherBySegmentId(segmentId);
                will(returnValue(segmentTargettedPublisherIds));
            }
        });
        assertFalse(eligibilityChecker.isEligibleBasedOnSegmentTargettedPublisher(creative, adspace, td));
    }
}
