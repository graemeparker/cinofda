package com.adfonic.domain.cache.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import com.adfonic.domain.cache.dto.adserver.adspace.AdSpaceDto;
import com.adfonic.domain.cache.dto.adserver.adspace.PublicationDto;
import com.adfonic.domain.cache.dto.adserver.adspace.PublisherDto;

public class TestAdSpaceServiceImpl {

	AdSpaceService adSpaceServiceImpl;
	
	@Before
	public void beforeEverytest(){
		adSpaceServiceImpl = new AdSpaceServiceImpl();
	}
	/**
	 * Adding only 1 entry 
	 */
	@Test
	public void test01_addDormantAdSpaceExternalId(){
		String externalIdToBeAdded = "abc_123_456";
		String externalIdNotToBeAdded = "123_abc";
		adSpaceServiceImpl.addDormantAdSpaceExternalId(externalIdToBeAdded);
		assertTrue(adSpaceServiceImpl.isDormantAdSpace(externalIdToBeAdded));
		assertFalse(adSpaceServiceImpl.isDormantAdSpace(externalIdNotToBeAdded));
		
		Set<String> allDormatAdSpaceExternalIds = adSpaceServiceImpl.getDormantAdSpaceExternalIds();
		assertEquals(1, allDormatAdSpaceExternalIds.size());
		assertTrue(allDormatAdSpaceExternalIds.contains(externalIdToBeAdded));
		assertFalse(allDormatAdSpaceExternalIds.contains(externalIdNotToBeAdded));
	}
	/**
	 * Adding 2 different entry 
	 */
	@Test
	public void test02_addDormantAdSpaceExternalId(){
		String externalIdToBeAdded1 = "abc_123_456";
		String externalIdToBeAdded2 = "456_abc_123";
		String externalIdNotToBeAdded = "123_abc";
		adSpaceServiceImpl.addDormantAdSpaceExternalId(externalIdToBeAdded1);
		adSpaceServiceImpl.addDormantAdSpaceExternalId(externalIdToBeAdded2);
		assertTrue(adSpaceServiceImpl.isDormantAdSpace(externalIdToBeAdded1));
		assertTrue(adSpaceServiceImpl.isDormantAdSpace(externalIdToBeAdded2));
		assertFalse(adSpaceServiceImpl.isDormantAdSpace(externalIdNotToBeAdded));
		
		Set<String> allDormatAdSpaceExternalIds = adSpaceServiceImpl.getDormantAdSpaceExternalIds();
		assertEquals(2, allDormatAdSpaceExternalIds.size());
		assertTrue(allDormatAdSpaceExternalIds.contains(externalIdToBeAdded1));
		assertTrue(allDormatAdSpaceExternalIds.contains(externalIdToBeAdded2));
		assertFalse(allDormatAdSpaceExternalIds.contains(externalIdNotToBeAdded));
	}
	
	/**
	 * Adding 2 same entry 
	 */
	@Test
	public void test03_addDormantAdSpaceExternalId(){
		String externalIdToBeAdded1 = "abc_123_456";
		String externalIdToBeAdded2 = "abc_123_456";
		String externalIdNotToBeAdded = "123_abc";
		adSpaceServiceImpl.addDormantAdSpaceExternalId(externalIdToBeAdded1);
		adSpaceServiceImpl.addDormantAdSpaceExternalId(externalIdToBeAdded2);
		assertTrue(adSpaceServiceImpl.isDormantAdSpace(externalIdToBeAdded1));
		assertTrue(adSpaceServiceImpl.isDormantAdSpace(externalIdToBeAdded2));
		assertFalse(adSpaceServiceImpl.isDormantAdSpace(externalIdNotToBeAdded));
		
		Set<String> allDormatAdSpaceExternalIds = adSpaceServiceImpl.getDormantAdSpaceExternalIds();
		assertEquals(1, allDormatAdSpaceExternalIds.size());
		assertTrue(allDormatAdSpaceExternalIds.contains(externalIdToBeAdded1));
		assertTrue(allDormatAdSpaceExternalIds.contains(externalIdToBeAdded2));
		assertFalse(allDormatAdSpaceExternalIds.contains(externalIdNotToBeAdded));
	}
	
	/**
	 * Adding a adspace and getting it back 
	 */
	@Test
	public void test04_addAddSpaceToCache(){
		final String externalID = "EXTID1234";
		final long id = 1234;
		AdSpaceDto adSpace = new AdSpaceDto();
		adSpace.setExternalID(externalID);
		adSpace.setId(id);
		PublicationDto publication = new PublicationDto();
		PublisherDto publisher = new PublisherDto();
		String publisherExtId = "PUBLISHER_EXTID";
		Long publisherId = 123L;
		publisher.setExternalId(publisherExtId);
		publisher.setId(publisherId);
		publication.setPublisher(publisher);
		adSpace.setPublication(publication);
		
		adSpaceServiceImpl.addAddSpaceToCache(adSpace);
		assertEquals(adSpace, adSpaceServiceImpl.getAdSpaceByExternalID(externalID));
		assertEquals(adSpace, adSpaceServiceImpl.getAdSpaceById(id));
		assertEquals(1, adSpaceServiceImpl.getAllAdSpaces().length);
		assertEquals(adSpace, adSpaceServiceImpl.getAllAdSpaces()[0]);
		assertEquals(publisherId, adSpaceServiceImpl.getPublisherIdByExternalID(publisherExtId));

		//Calling these function to see that call doesn't fail. No other expectations
		adSpaceServiceImpl.beforeSerialization();
		adSpaceServiceImpl.afterDeserialize();

	}
	
	/**
	 * getting some adspace which do not exists 
	 */
	@Test
	public void test05_addAddSpaceToCache(){
		final String externalID = "EXTID1234";
		final long id = 1234;
		assertNull(adSpaceServiceImpl.getAdSpaceByExternalID(externalID));
		assertNull(adSpaceServiceImpl.getAdSpaceById(id));
	}
	
	/**
	 * getting some adspace which do not exists 
	 */
	@Test
	public void test06_addPublicationMayViewPricing(){
		final long publicationId = 1234;
		final long publicationIdCanNotSeePricing = 12345;
		adSpaceServiceImpl.addPublicationMayViewPricing(publicationId);
		assertTrue(adSpaceServiceImpl.mayPublicationViewPricing(publicationId));
		assertFalse(adSpaceServiceImpl.mayPublicationViewPricing(publicationIdCanNotSeePricing));
	}
	
}
