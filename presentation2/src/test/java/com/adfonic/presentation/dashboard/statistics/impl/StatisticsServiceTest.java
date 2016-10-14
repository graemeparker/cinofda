package com.adfonic.presentation.dashboard.statistics.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.jmock.Expectations;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.adfonic.dto.campaign.search.CampaignSearchDto;
import com.adfonic.dto.dashboard.DashboardDto;
import com.adfonic.dto.dashboard.statistic.AdvertiserHeadlineStatsDto;
import com.adfonic.dto.dashboard.statistic.StatisticsDto;
import com.adfonic.presentation.dashboard.statistics.StatisticsService;
import com.adfonic.test.AbstractAdfonicTest;

public class StatisticsServiceTest extends AbstractAdfonicTest {

	private StatisticsService stsService;

	@Before
	public void setUp() throws Exception {
		stsService = mock(StatisticsService.class);
	}

	@After
	public void tearDown() throws Exception {
		stsService = null;
	}

	@Test
	public void doGetDashBoardStatistics() {
		final Calendar today = Calendar.getInstance();
		final Calendar daysAgo = Calendar.getInstance();

		for (int k = 0; k < 6; k++) {
			daysAgo.roll(Calendar.DAY_OF_MONTH, false);
		}
		final StatisticsDto expectedDto = new StatisticsDto();
		expectedDto.setClicks(123);
		expectedDto.setConversions(223);
		expectedDto.setCostPerConversion(23.4);
		expectedDto.setCtr(223.66);
		expectedDto.setImpressions(22333);
		expectedDto.setSpend(23.5);

		final DashboardDto searchDto = new DashboardDto();
		searchDto.setFrom(daysAgo.getTime());
		searchDto.setTo(today.getTime());

		expect(new Expectations() {
			{
				oneOf(stsService).getDashboardStatistics(searchDto);
				will(returnValue(expectedDto));
			}
		});

		AdvertiserHeadlineStatsDto statistics = stsService.getDashboardStatistics(searchDto);
		assertEquals("Result should be equal to the expected", expectedDto,
				statistics);
	}

	@Test
	public void dogetDashboardReportingTable() {
		final Calendar daysAgo = Calendar.getInstance();

		for (int k = 0; k < 6; k++) {
			daysAgo.roll(Calendar.DAY_OF_MONTH, false);
		}
		final List<StatisticsDto> dtoList = new ArrayList<StatisticsDto>(0);
		for (int k = 0; k < 1000; k++) {
			final StatisticsDto expectedDto = new StatisticsDto();
			expectedDto.setClicks(123);
			expectedDto.setConversions(223);
			expectedDto.setCostPerConversion(23.4);
			expectedDto.setCtr(223.66);
			expectedDto.setImpressions(22333);
			expectedDto.setSpend(23.5);
			dtoList.add(expectedDto);
		}

		final DashboardDto searchDto = new DashboardDto();
		CampaignSearchDto cdto = new CampaignSearchDto();
			cdto.setName("some Name");

		expect(new Expectations() {
			{
				oneOf(stsService).getDashboardReportingTable(searchDto);
				will(returnValue(dtoList));
			}
		});

		List<StatisticsDto> statistics = stsService
				.getDashboardReportingTable(searchDto);
		assertEquals("Result should be equal to the expected", dtoList,
				statistics);
		assertTrue("Result size should be equal to the expected", dtoList.size()==
				statistics.size());		
	}
}
