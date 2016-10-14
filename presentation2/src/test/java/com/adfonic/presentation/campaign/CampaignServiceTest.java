package com.adfonic.presentation.campaign;

import static org.junit.Assert.assertEquals;

import org.jmock.Expectations;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.adfonic.dto.campaign.CampaignDto;
import com.adfonic.dto.campaign.search.CampaignSearchDto;
import com.adfonic.test.AbstractAdfonicTest;

public class CampaignServiceTest extends AbstractAdfonicTest {
	private CampaignService service;

	@Before
	public void setUp() throws Exception {
		service = mock(CampaignService.class);
	}

	@After
	public void tearDown() throws Exception {
		service = null;
	}

	@Test
	public void doGetCampaigns() {
		final CampaignSearchDto expected = new CampaignSearchDto();

		final CampaignSearchDto search = new CampaignSearchDto();

		expect(new Expectations() {
			{
				oneOf(service).getCampaigns(search);
				will(returnValue(expected));
			}
		});
		CampaignSearchDto searchResultDto = service.getCampaigns(search);
		assertEquals("Result should be equal to the expected", expected,
				searchResultDto);
	}

	@Test
	public void doGetCampaignById() {
		final String campaigName = "CampaignName";
		final CampaignDto expected = mock(CampaignDto.class);

		final CampaignSearchDto search = new CampaignSearchDto();
		search.setId(Long.valueOf(2l));

		expect(new Expectations() {
			{
				oneOf(service).getCampaignById(search);
				will(returnValue(expected));
				allowing(expected).getName();
				will(returnValue(campaigName));
				allowing(expected).getId();
				will(returnValue(Long.valueOf(2l)));
			}
		});
		CampaignDto dto = service.getCampaignById(search);
		Assert.assertNotNull("Result object should not be null", dto);
		Assert.assertNotNull("Result object Id should not be null", dto.getId());
		Assert.assertTrue("Id's should have the same value", Long.valueOf(2l)
				.intValue() == dto.getId().intValue());
	}
	
}
