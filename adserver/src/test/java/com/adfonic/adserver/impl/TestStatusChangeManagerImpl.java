package com.adfonic.adserver.impl;

import static org.junit.Assert.assertEquals;

import org.jmock.Expectations;
import org.junit.Before;
import org.junit.Test;

import com.adfonic.adserver.BaseAdserverTest;
import com.adfonic.domain.AdSpace;
import com.adfonic.domain.cache.dto.adserver.adspace.AdSpaceDto;
import com.adfonic.domain.cache.dto.adserver.adspace.PublicationDto;
import com.adfonic.domain.cache.dto.adserver.creative.CampaignDto;
import com.adfonic.domain.cache.dto.adserver.creative.CreativeDto;
import com.adfonic.jms.StatusChangeMessage;

public class TestStatusChangeManagerImpl extends BaseAdserverTest {

	private StatusChangeManagerImpl statusChangeManagerImpl;
	

	@Before
	public void initTests(){
		statusChangeManagerImpl = new StatusChangeManagerImpl();
		
	}
	
	@Test
	public void testStatusChangeManagerImpl01_getStatus(){
		final AdSpaceDto adSpace = mock(AdSpaceDto.class,"adSpace");
		final Long adSpaceId = randomLong();
		final AdSpace.Status adSpaceStatus = AdSpace.Status.UNVERIFIED;
		final String adSpaceChangedStatus = AdSpace.Status.VERIFIED.name();
		final StatusChangeMessage statusChangeMessage = mock(StatusChangeMessage.class,"statusChangeMessage");
		final String adSpaceEntityType = "AdSpace";
		
		expect(new Expectations() {{
			allowing (adSpace).getId();
				will(returnValue(adSpaceId));
			allowing (adSpace).getStatus();
				will(returnValue(adSpaceStatus));
			oneOf (statusChangeMessage).getEntityId();
				will(returnValue(adSpaceId));
			oneOf (statusChangeMessage).getNewStatus();
				will(returnValue(adSpaceChangedStatus));
			oneOf (statusChangeMessage).getEntityType();
				will(returnValue(adSpaceEntityType));
		}});
		
		AdSpace.Status derivedStatus = statusChangeManagerImpl.getStatus(adSpace);
		assertEquals(adSpaceStatus, derivedStatus);
		
		statusChangeManagerImpl.onStatusChange(statusChangeMessage);
		derivedStatus = statusChangeManagerImpl.getStatus(adSpace);
		assertEquals(adSpaceChangedStatus, derivedStatus.name());
	}
	
	@Test
	public void testStatusChangeManagerImpl02_getStatus(){
		final CampaignDto campaign = mock(CampaignDto.class,"campaign");
		final Long campaignId = randomLong();
		final com.adfonic.domain.Campaign.Status campaignStatus = com.adfonic.domain.Campaign.Status.NEW;
		final String campaignChangedStatus = com.adfonic.domain.Campaign.Status.ACTIVE.name();
		final StatusChangeMessage statusChangeMessage = mock(StatusChangeMessage.class,"statusChangeMessage");
		final String campaignEntityType = "Campaign";
		
		expect(new Expectations() {{
			allowing (campaign).getId();
				will(returnValue(campaignId));
			allowing (campaign).getStatus();
				will(returnValue(campaignStatus));
			oneOf (statusChangeMessage).getEntityId();
				will(returnValue(campaignId));
			oneOf (statusChangeMessage).getNewStatus();
				will(returnValue(campaignChangedStatus));
			allowing (statusChangeMessage).getEntityType();
				will(returnValue(campaignEntityType));
		}});
		
		com.adfonic.domain.Campaign.Status derivedStatus = statusChangeManagerImpl.getStatus(campaign);
		assertEquals(campaignStatus, derivedStatus);
		
		statusChangeManagerImpl.onStatusChange(statusChangeMessage);
		derivedStatus = statusChangeManagerImpl.getStatus(campaign);
		assertEquals(campaignChangedStatus, derivedStatus.name());
	}
	
	@Test
	public void testStatusChangeManagerImpl03_getStatus(){
		final CreativeDto creative = mock(CreativeDto.class,"creative");
		final Long creativeId = randomLong();
		final com.adfonic.domain.Creative.Status creativeStatus = com.adfonic.domain.Creative.Status.NEW;
		final String creativeChangedStatus = com.adfonic.domain.Creative.Status.ACTIVE.name();
		final StatusChangeMessage statusChangeMessage = mock(StatusChangeMessage.class,"statusChangeMessage");
		final String creativeEntityType = "Creative";
		
		expect(new Expectations() {{
			allowing (creative).getId();
				will(returnValue(creativeId));
			allowing (creative).getStatus();
				will(returnValue(creativeStatus));
			oneOf (statusChangeMessage).getEntityId();
				will(returnValue(creativeId));
			oneOf (statusChangeMessage).getNewStatus();
				will(returnValue(creativeChangedStatus));
			allowing (statusChangeMessage).getEntityType();
				will(returnValue(creativeEntityType));
		}});
		
		com.adfonic.domain.Creative.Status derivedStatus = statusChangeManagerImpl.getStatus(creative);
		assertEquals(creativeStatus, derivedStatus);
		
		statusChangeManagerImpl.onStatusChange(statusChangeMessage);
		derivedStatus = statusChangeManagerImpl.getStatus(creative);
		assertEquals(creativeChangedStatus, derivedStatus.name());
	}
	
	@Test
	public void testStatusChangeManagerImpl04_getStatus(){
		final PublicationDto publication = mock(PublicationDto.class,"publication");
		final Long publicationId = randomLong();
		final com.adfonic.domain.Publication.Status publicationStatus = com.adfonic.domain.Publication.Status.NEW;
		final String publicationChangedStatus = com.adfonic.domain.Publication.Status.ACTIVE.name();
		final StatusChangeMessage statusChangeMessage = mock(StatusChangeMessage.class,"statusChangeMessage");
		final String publicationEntityType = "Publication";
		
		expect(new Expectations() {{
			allowing (publication).getId();
				will(returnValue(publicationId));
			allowing (publication).getStatus();
				will(returnValue(publicationStatus));
			oneOf (statusChangeMessage).getEntityId();
				will(returnValue(publicationId));
			oneOf (statusChangeMessage).getNewStatus();
				will(returnValue(publicationChangedStatus));
			allowing (statusChangeMessage).getEntityType();
				will(returnValue(publicationEntityType));
		}});
		
		com.adfonic.domain.Publication.Status derivedStatus = statusChangeManagerImpl.getStatus(publication);
		assertEquals(publicationStatus, derivedStatus);
		
		statusChangeManagerImpl.onStatusChange(statusChangeMessage);
		derivedStatus = statusChangeManagerImpl.getStatus(publication);
		assertEquals(publicationChangedStatus, derivedStatus.name());
	}
	
	/**
	 * Unexpected Entity type
	 */
	@Test
	public void testStatusChangeManagerImpl05_getStatus(){
		final PublicationDto publication = mock(PublicationDto.class,"publication");
		final Long publicationId = randomLong();
		final com.adfonic.domain.Publication.Status publicationStatus = com.adfonic.domain.Publication.Status.NEW;
		final StatusChangeMessage statusChangeMessage = mock(StatusChangeMessage.class,"statusChangeMessage");
		final String publicationEntityType = "SomeUknowStatus";
		
		expect(new Expectations() {{
			allowing (publication).getId();
				will(returnValue(publicationId));
			allowing (publication).getStatus();
				will(returnValue(publicationStatus));
			allowing (statusChangeMessage).getEntityType();
				will(returnValue(publicationEntityType));
		}});
		
		com.adfonic.domain.Publication.Status derivedStatus = statusChangeManagerImpl.getStatus(publication);
		assertEquals(publicationStatus, derivedStatus);
		
		statusChangeManagerImpl.onStatusChange(statusChangeMessage);
		derivedStatus = statusChangeManagerImpl.getStatus(publication);
		assertEquals(publicationStatus, derivedStatus);
	}
}
