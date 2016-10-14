package com.adfonic.adserver.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.jmock.Expectations;
import org.junit.Test;

import com.adfonic.adserver.BaseAdserverTest;
import com.adfonic.adserver.Stoppage;
import com.adfonic.adserver.StoppageManager;
import com.adfonic.adserver.stoppages.StoppagesService;
import com.adfonic.domain.cache.dto.adserver.creative.AdvertiserDto;
import com.adfonic.domain.cache.dto.adserver.creative.CampaignDto;
import com.adfonic.domain.cache.dto.adserver.creative.CreativeDto;
import com.adfonic.jms.StopAdvertiserMessage;
import com.adfonic.jms.StopCampaignMessage;
import com.adfonic.jms.UnStopAdvertiserMessage;
import com.adfonic.jms.UnStopCampaignMessage;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class TestStoppageManagerImpl  extends BaseAdserverTest{
	/**
	 * When Lazy Init is false and stoppage return by web service is empty Map
	 * @throws IOException
	 */
	@Test
	public void testStoppageManagerImpl1() throws IOException{
		//Test Data
		final StoppagesService stoppages = mock(StoppagesService.class);
		expect(new Expectations() {{
		    oneOf (stoppages).getAdvertiserStoppages(); will(returnValue(new HashMap()));
		    oneOf (stoppages).getCampaignStoppages(); will(returnValue(new HashMap()));
		}});

        StoppageManager stoppageManager = new StoppageManagerImpl(false, stoppages);
		Map<Long,Stoppage> advertiserStoppages =  stoppageManager.getAdvertiserStoppages();
		assertEquals(0, advertiserStoppages.size());
		Map<Long,Stoppage> campaignStoppages =  stoppageManager.getCampaignStoppages();
		assertEquals(0, campaignStoppages.size());
		
		//Make sure iws Service is called only once as defined in Expectation
	}
	/**
	 * When Lazy Init is true and stoppage return by web service is empty Map
	 * getAdvertiserStoppages is called first and then getCampaignStoppages
	 * @throws IOException
	 */
	@Test
	public void testStoppageManagerImpl2() throws IOException{
		//Test Data
        final StoppagesService stoppages = mock(StoppagesService.class);
        expect(new Expectations() {{
            oneOf (stoppages).getAdvertiserStoppages(); will(returnValue(new HashMap()));
            oneOf (stoppages).getCampaignStoppages(); will(returnValue(new HashMap()));
        }});
		
		StoppageManager stoppageManager = new StoppageManagerImpl(true, stoppages);
		Map<Long,Stoppage> advertiserStoppages =  stoppageManager.getAdvertiserStoppages();
		assertEquals(0, advertiserStoppages.size());
		Map<Long,Stoppage> campaignStoppages =  stoppageManager.getCampaignStoppages();
		assertEquals(0, campaignStoppages.size());
		
		//Make sure iws Service is called only once as defined in Expectation

	}
	
	/**
	 * When Lazy Init is true and stoppage return by web service is empty Map
	 * getCampaignStoppages is called first and then getAdvertiserStoppages
	 * @throws IOException
	 */
	@Test
	public void testStoppageManagerImpl3() throws IOException{
        final StoppagesService stoppages = mock(StoppagesService.class);
        expect(new Expectations() {{
            oneOf (stoppages).getAdvertiserStoppages(); will(returnValue(new HashMap()));
            oneOf (stoppages).getCampaignStoppages(); will(returnValue(new HashMap()));
        }});
		
		StoppageManager stoppageManager = new StoppageManagerImpl(true, stoppages);
		Map<Long,Stoppage> campaignStoppages =  stoppageManager.getCampaignStoppages();
		assertEquals(0, campaignStoppages.size());
		Map<Long,Stoppage> advertiserStoppages =  stoppageManager.getAdvertiserStoppages();
		assertEquals(0, advertiserStoppages.size());
		
		//Make sure iws Service is called only once as defined in Expectation

	}
	
	/**
	 * When Lazy Init is true and web service throws IOException
	 * getAdvertiserStoppages is called first and then getCampaignStoppages
	 * @throws IOException
	 */
	@Test
	public void testStoppageManagerImpl4() throws IOException{
		//Test Data
        final StoppagesService stoppages = mock(StoppagesService.class);
        expect(new Expectations() {{
            //First time service will throw IO Exception
            oneOf (stoppages).getAdvertiserStoppages(); will(throwException(new IOException()));
            //Second time service will return empty Map
            oneOf (stoppages).getAdvertiserStoppages(); will(returnValue(new HashMap()));
            oneOf (stoppages).getCampaignStoppages(); will(returnValue(new HashMap()));
        }});

		
		StoppageManager stoppageManager = new StoppageManagerImpl(true, stoppages);
		Map<Long,Stoppage> advertiserStoppages =  stoppageManager.getAdvertiserStoppages();
		assertEquals(0, advertiserStoppages.size());
		Map<Long,Stoppage> campaignStoppages =  stoppageManager.getCampaignStoppages();
		assertEquals(0, campaignStoppages.size());
		
		//Make sure iws Service is called only once as defined in Expectation

	}
	
	/**
	 * When Lazy Init is true and web service throws IOException
	 * getCampaignStoppages is called first and then getAdvertiserStoppages
	 * @throws IOException
	 */
	@Test
	public void testStoppageManagerImpl5() throws IOException{
		//Test Data
        final StoppagesService stoppages = mock(StoppagesService.class);
        expect(new Expectations() {{
            //First time service will throw IO Exception
            oneOf (stoppages).getAdvertiserStoppages(); will(throwException(new IOException()));
            //Second time service will return empty Map
            oneOf (stoppages).getAdvertiserStoppages(); will(returnValue(new HashMap()));
            oneOf (stoppages).getCampaignStoppages(); will(returnValue(new HashMap()));
        }});
		
		StoppageManager stoppageManager = new StoppageManagerImpl(true, stoppages);
		Map<Long,Stoppage> campaignStoppages =  stoppageManager.getCampaignStoppages();
		assertEquals(0, campaignStoppages.size());
		Map<Long,Stoppage> advertiserStoppages =  stoppageManager.getAdvertiserStoppages();
		assertEquals(0, advertiserStoppages.size());
		
		//Make sure iws Service is called only once as defined in Expectation

	}

	/**
	 * When Lazy Init is false and stoppage return by web service is a Map with SOme Data
	 * @throws IOException
	 */
	@Test
	public void testStoppageManagerImpl6() throws IOException{
		//Test Data
		Long advertiserId = 123L;
		Long campaignId = 321L;
		Long timestamp = 1234L;
		Long reactivateDate = System.currentTimeMillis();
		
		final Map advertiserStoppagesOneMap = new HashMap();
		advertiserStoppagesOneMap.put(advertiserId, new Stoppage(timestamp, reactivateDate));

		final Map campaignStoppagesOneMap = new HashMap();
		campaignStoppagesOneMap.put(campaignId, new Stoppage(timestamp, reactivateDate));

        final StoppagesService stoppages = mock(StoppagesService.class);
        expect(new Expectations() {{
            oneOf (stoppages).getAdvertiserStoppages(); will(returnValue(advertiserStoppagesOneMap));
            oneOf (stoppages).getCampaignStoppages(); will(returnValue(campaignStoppagesOneMap));
        }});
		
		StoppageManager stoppageManager = new StoppageManagerImpl(false, stoppages);
		Map<Long,Stoppage> advertiserStoppages =  stoppageManager.getAdvertiserStoppages();
		assertEquals(1, advertiserStoppages.size());
		Map<Long,Stoppage> campaignStoppages =  stoppageManager.getCampaignStoppages();
		assertEquals(1, campaignStoppages.size());
		
		//Make sure iws Service is called only once as defined in Expectation
	}
	/**
	 * When Campaign has been stopped
	 * @throws IOException
	 */
	@Test
	public void testIsCreativeStopped1() throws IOException{
		//Test Data
        Long advertiserId = 123L;
        final Long campaignId = 321L;
        Long timestamp = System.currentTimeMillis();
        Long reactivateDate = null;

        final Map advertiserStoppagesOneMap = new HashMap();
        advertiserStoppagesOneMap.put(advertiserId, new Stoppage(timestamp, reactivateDate));

        final Map campaignStoppagesOneMap = new HashMap();
        campaignStoppagesOneMap.put(campaignId, new Stoppage(timestamp, reactivateDate));

        final StoppagesService stoppages = mock(StoppagesService.class);
        expect(new Expectations() {{
            oneOf (stoppages).getAdvertiserStoppages(); will(returnValue(advertiserStoppagesOneMap));
            oneOf (stoppages).getCampaignStoppages(); will(returnValue(campaignStoppagesOneMap));
        }});

        final CreativeDto creative = mock(CreativeDto.class);
        final CampaignDto campaign = mock(CampaignDto.class);
        expect(new Expectations() {{
            allowing (campaign).getId(); will(returnValue(campaignId));
            allowing (creative).getCampaign(); will(returnValue(campaign));
        }});


        StoppageManager stoppageManager = new StoppageManagerImpl(false, stoppages);
        Map<Long,Stoppage> advertiserStoppages =  stoppageManager.getAdvertiserStoppages();
        assertEquals(1, advertiserStoppages.size());
        Map<Long,Stoppage> campaignStoppages =  stoppageManager.getCampaignStoppages();
        assertEquals(1, campaignStoppages.size());


        assertTrue(stoppageManager.isCreativeStopped(creative));

        //Make sure iws Service is called only once as defined in Expectation

	}
	
	
	/**
	 * When Campaign has been stopped
	 * @throws IOException
	 */
	@Test
	public void testIsCreativeStopped3() throws IOException{
		//Test Data
        final Long advertiserId = 123L;
        final Long campaignId = 321L;
        Long timestamp = 1234L;
        Long reactivateDate = System.currentTimeMillis();

        final Map advertiserStoppagesOneMap = new HashMap();
        advertiserStoppagesOneMap.put(advertiserId, new Stoppage(timestamp, reactivateDate));

        final Map campaignStoppagesOneMap = new HashMap();
        campaignStoppagesOneMap.put(campaignId, new Stoppage(timestamp, reactivateDate));

        final StoppagesService stoppages = mock(StoppagesService.class);
        expect(new Expectations() {{
            oneOf (stoppages).getAdvertiserStoppages(); will(returnValue(advertiserStoppagesOneMap));
            oneOf (stoppages).getCampaignStoppages(); will(returnValue(campaignStoppagesOneMap));
        }});
		final CreativeDto creative = mock(CreativeDto.class);
		final CampaignDto campaign = mock(CampaignDto.class);
		final AdvertiserDto advertiser = mock(AdvertiserDto.class);
		expect(new Expectations() {{
			allowing (campaign).getId(); will(returnValue(campaignId));
			allowing (campaign).getAdvertiser(); will(returnValue(advertiser));
			allowing (creative).getCampaign(); will(returnValue(campaign));
			allowing (advertiser).getId(); will(returnValue(advertiserId));
		}});	
				
				
		StoppageManager stoppageManager = new StoppageManagerImpl(false, stoppages);
		Map<Long,Stoppage> advertiserStoppages =  stoppageManager.getAdvertiserStoppages();
		assertEquals(1, advertiserStoppages.size());
		Map<Long,Stoppage> campaignStoppages =  stoppageManager.getCampaignStoppages();
		assertEquals(1, campaignStoppages.size());
		
		
		assertFalse(stoppageManager.isCreativeStopped(creative));
		
		//Make sure iws Service is called only once as defined in Expectation
		
	}
	
	/**
	 * When No CampaignStoppage
	 * @throws IOException
	 */
	@Test
	public void testIsCreativeStopped4() throws IOException{
		//Test Data
        final Long advertiserId = 123L;
        final Long campaignId = 321L;
        Long timestamp = System.currentTimeMillis();
        Long reactivateDate = System.currentTimeMillis();

        final Map advertiserStoppagesOneMap = new HashMap();
        advertiserStoppagesOneMap.put(advertiserId, new Stoppage(timestamp, reactivateDate));

        final Map campaignStoppagesOneMap = new HashMap();
        campaignStoppagesOneMap.put(campaignId, new Stoppage(timestamp, reactivateDate));

        final StoppagesService stoppages = mock(StoppagesService.class);
        expect(new Expectations() {{
            oneOf (stoppages).getAdvertiserStoppages(); will(returnValue(advertiserStoppagesOneMap));
            oneOf (stoppages).getCampaignStoppages(); will(returnValue(new HashMap()));
        }});

        final CreativeDto creative = mock(CreativeDto.class);
        final CampaignDto campaign = mock(CampaignDto.class);
        final AdvertiserDto advertiser = mock(AdvertiserDto.class);
        expect(new Expectations() {{
            allowing (campaign).getId(); will(returnValue(campaignId));
            allowing (campaign).getAdvertiser(); will(returnValue(advertiser));
            allowing (creative).getCampaign(); will(returnValue(campaign));
            allowing (advertiser).getId(); will(returnValue(advertiserId));
        }});


        StoppageManager stoppageManager = new StoppageManagerImpl(false, stoppages);
        Map<Long,Stoppage> advertiserStoppages =  stoppageManager.getAdvertiserStoppages();
        assertEquals(1, advertiserStoppages.size());
        Map<Long,Stoppage> campaignStoppages =  stoppageManager.getCampaignStoppages();
        assertEquals(0, campaignStoppages.size());


        assertFalse(stoppageManager.isCreativeStopped(creative));

				//Make sure iws Service is called only once as defined in Expectation
			}
	
	/**
	 * When stopage is not in effect, either reactivation time has paseed or its null
	 * @throws IOException
	 */
	@Test
	public void testIsAdvertiserStopped1() throws IOException{
		//Test Data
        final Long advertiserId = 123L;
        final Long campaignId = 321L;
        Long timestamp = 1234L;
        Long reactivateDate = System.currentTimeMillis();

        final Map advertiserStoppagesOneMap = new HashMap();
        advertiserStoppagesOneMap.put(advertiserId, new Stoppage(timestamp, reactivateDate));

        final Map campaignStoppagesOneMap = new HashMap();
        campaignStoppagesOneMap.put(campaignId, new Stoppage(timestamp, reactivateDate));

        final StoppagesService stoppages = mock(StoppagesService.class);
        expect(new Expectations() {{
            oneOf (stoppages).getAdvertiserStoppages(); will(returnValue(advertiserStoppagesOneMap));
            oneOf (stoppages).getCampaignStoppages(); will(returnValue(new HashMap()));
        }});
		final AdvertiserDto advertiser = mock(AdvertiserDto.class);
		expect(new Expectations() {{
			allowing (advertiser).getId(); will(returnValue(advertiserId));
		}});


		StoppageManager stoppageManager = new StoppageManagerImpl(false, stoppages);
		Map<Long,Stoppage> advertiserStoppages =  stoppageManager.getAdvertiserStoppages();
		assertEquals(1, advertiserStoppages.size());


		assertFalse(stoppageManager.isAdvertiserStopped(advertiser));

		//Make sure iws Service is called only once as defined in Expectation
	}

	/**
	 * When stoppage is still in effect
	 * @throws IOException
	 */
	@Test
	public void testIsAdvertiserStopped2() throws IOException{
		//Test Data
        final Long advertiserId = 123L;
        final Long campaignId = 321L;
        Long timestamp = System.currentTimeMillis();
        Long reactivateDate = System.currentTimeMillis() +10;

        final Map advertiserStoppagesOneMap = new HashMap();
        advertiserStoppagesOneMap.put(advertiserId, new Stoppage(timestamp, reactivateDate));

        final Map campaignStoppagesOneMap = new HashMap();
        campaignStoppagesOneMap.put(campaignId, new Stoppage(timestamp, reactivateDate));

        final StoppagesService stoppages = mock(StoppagesService.class);
        expect(new Expectations() {{
            oneOf(stoppages).getAdvertiserStoppages();
            will(returnValue(advertiserStoppagesOneMap));
            oneOf(stoppages).getCampaignStoppages();
            will(returnValue(new HashMap<>()));
        }});

		final AdvertiserDto advertiser = mock(AdvertiserDto.class);
		expect(new Expectations() {{
			allowing (advertiser).getId(); will(returnValue(advertiserId));
		}});


		StoppageManager stoppageManager = new StoppageManagerImpl(false, stoppages);
		Map<Long,Stoppage> advertiserStoppages =  stoppageManager.getAdvertiserStoppages();
		assertEquals(1, advertiserStoppages.size());


		assertTrue(stoppageManager.isAdvertiserStopped(advertiser));

		//Make sure iws Service is called only once as defined in Expectation
	}
	/**
	 * When there is no advertiser stoppage at all
	 * @throws IOException
	 */
	@Test
	public void testIsAdvertiserStopped3() throws IOException{
		//Test Data
        final Long advertiserId = 123L;
        final Long campaignId = 321L;
        Long timestamp = System.currentTimeMillis();
        Long reactivateDate = System.currentTimeMillis() + 10;

        final Map advertiserStoppagesOneMap = new HashMap();
        advertiserStoppagesOneMap.put(advertiserId, new Stoppage(timestamp, reactivateDate));

        final Map campaignStoppagesOneMap = new HashMap();
        campaignStoppagesOneMap.put(campaignId, new Stoppage(timestamp, reactivateDate));

        final StoppagesService stoppages = mock(StoppagesService.class);
        expect(new Expectations() {{
            oneOf (stoppages).getAdvertiserStoppages(); will(returnValue(new HashMap<>()));
            oneOf (stoppages).getCampaignStoppages(); will(returnValue(campaignStoppagesOneMap));
        }});
		final AdvertiserDto advertiser = mock(AdvertiserDto.class);
		expect(new Expectations() {{
			allowing (advertiser).getId(); will(returnValue(advertiserId));
		}});


		StoppageManager stoppageManager = new StoppageManagerImpl(false, stoppages);
		Map<Long,Stoppage> advertiserStoppages =  stoppageManager.getAdvertiserStoppages();
		assertEquals(0, advertiserStoppages.size());


		assertFalse(stoppageManager.isAdvertiserStopped(advertiser));

		//Make sure iws Service is called only once as defined in Expectation
	}

//	/**
//	 * When there is no advertiser stoppage at all and lazyInit is true
//	 * @throws IOException
//	 */
	@Test
	public void testIsAdvertiserStopped4() throws IOException{
		//Test Data
        final Long advertiserId = 123L;

        final StoppagesService stoppages = mock(StoppagesService.class);
        expect(new Expectations() {{
            oneOf (stoppages).getAdvertiserStoppages(); will(returnValue(new HashMap<>()));
            oneOf (stoppages).getCampaignStoppages(); will(returnValue(new HashMap<>()));
        }});
		final AdvertiserDto advertiser = mock(AdvertiserDto.class);
		expect(new Expectations() {{
			allowing (advertiser).getId(); will(returnValue(advertiserId));
		}});


		StoppageManager stoppageManager = new StoppageManagerImpl(true, stoppages);

		assertFalse(stoppageManager.isAdvertiserStopped(advertiser));

		//Make sure iws Service is called only once as defined in Expectation
	}
//
//	/**
//	 * When stopage has been expired i.e. reactivateTime is null or has passed
//	 * @throws IOException
//	 */
	@Test
	public void testIsCampaignStopped1() throws IOException{
		//Test Data



        final Long advertiserId = 123L;
        final Long campaignId = 321L;
        Long timestamp = System.currentTimeMillis();
        Long reactivateDate = System.currentTimeMillis() - 10;

        final Map advertiserStoppagesOneMap = new HashMap();
        advertiserStoppagesOneMap.put(advertiserId, new Stoppage(timestamp, reactivateDate));

        final Map campaignStoppagesOneMap = new HashMap();
        campaignStoppagesOneMap.put(campaignId, new Stoppage(timestamp, reactivateDate));

        final StoppagesService stoppages = mock(StoppagesService.class);
        expect(new Expectations() {{
            oneOf (stoppages).getAdvertiserStoppages(); will(returnValue(new HashMap<>()));
            oneOf (stoppages).getCampaignStoppages(); will(returnValue(campaignStoppagesOneMap));
        }});


		final CampaignDto campaign = mock(CampaignDto.class);
		expect(new Expectations() {{
			allowing (campaign).getId(); will(returnValue(campaignId));
		}});


		StoppageManager stoppageManager = new StoppageManagerImpl(false, stoppages);


		assertFalse(stoppageManager.isCampaignStopped(campaign));

		//Make sure iws Service is called only once as defined in Expectation

	}
//
//	/**
//	 * When stoppage is still in effect
//	 * @throws IOException
//	 */
	@Test
	public void testIsCampaignStopped2() throws IOException{
		//Test Data

        final Long advertiserId = 123L;
        final Long campaignId = 321L;
        Long timestamp = System.currentTimeMillis();
        Long reactivateDate = System.currentTimeMillis() + 10;

        final Map advertiserStoppagesOneMap = new HashMap();
        advertiserStoppagesOneMap.put(advertiserId, new Stoppage(timestamp, reactivateDate));

        final Map campaignStoppagesOneMap = new HashMap();
        campaignStoppagesOneMap.put(campaignId, new Stoppage(timestamp, reactivateDate));

        final StoppagesService stoppages = mock(StoppagesService.class);
        expect(new Expectations() {{
            oneOf (stoppages).getAdvertiserStoppages(); will(returnValue(new HashMap<>()));
            oneOf (stoppages).getCampaignStoppages(); will(returnValue(campaignStoppagesOneMap));
        }});

		final CampaignDto campaign = mock(CampaignDto.class);
		expect(new Expectations() {{
			allowing (campaign).getId(); will(returnValue(campaignId));
		}});


		StoppageManager stoppageManager = new StoppageManagerImpl(false, stoppages);


		assertTrue(stoppageManager.isCampaignStopped(campaign));

		//Make sure iws Service is called only once as defined in Expectation

	}
//	/**
//	 * When there is no campaign Stoppage at all
//	 * @throws IOException
//	 */
	@Test
	public void testIsCampaignStopped3() throws IOException{
		//Test Data

        final Long advertiserId = 123L;
        final Long campaignId = 321L;
        Long timestamp = System.currentTimeMillis();
        Long reactivateDate = System.currentTimeMillis() + 10;

        final Map advertiserStoppagesOneMap = new HashMap();
        advertiserStoppagesOneMap.put(advertiserId, new Stoppage(timestamp, reactivateDate));

        final Map campaignStoppagesOneMap = new HashMap();
        campaignStoppagesOneMap.put(campaignId, new Stoppage(timestamp, reactivateDate));

        final StoppagesService stoppages = mock(StoppagesService.class);
        expect(new Expectations() {{
            oneOf (stoppages).getAdvertiserStoppages(); will(returnValue(advertiserStoppagesOneMap));
            oneOf (stoppages).getCampaignStoppages(); will(returnValue(new HashMap<>()));
        }});

		final CampaignDto campaign = mock(CampaignDto.class);
		expect(new Expectations() {{
			allowing (campaign).getId(); will(returnValue(campaignId));
		}});


		StoppageManager stoppageManager = new StoppageManagerImpl(false, stoppages);
		assertFalse(stoppageManager.isCampaignStopped(campaign));

		//Make sure iws Service is called only once as defined in Expectation

	}

//	/**
//	 * When there is no campaign Stoppage at all and lazyInit is true
//	 * @throws IOException
//	 */
	@Test
	public void testIsCampaignStopped4() throws IOException{
//		//Test Data


        final Long advertiserId = 123L;
        final Long campaignId = 321L;
        Long timestamp = System.currentTimeMillis();
        Long reactivateDate = System.currentTimeMillis() + 10;

        final Map advertiserStoppagesOneMap = new HashMap();
        advertiserStoppagesOneMap.put(advertiserId, new Stoppage(timestamp, reactivateDate));

        final Map campaignStoppagesOneMap = new HashMap();
        campaignStoppagesOneMap.put(campaignId, new Stoppage(timestamp, reactivateDate));

        final StoppagesService stoppages = mock(StoppagesService.class);
        expect(new Expectations() {{
            oneOf (stoppages).getAdvertiserStoppages(); will(returnValue(advertiserStoppagesOneMap));
            oneOf (stoppages).getCampaignStoppages(); will(returnValue(new HashMap<>()));
        }});

		final CampaignDto campaign = mock(CampaignDto.class);
		expect(new Expectations() {{
			allowing (campaign).getId(); will(returnValue(campaignId));
		}});


		StoppageManager stoppageManager = new StoppageManagerImpl(true, stoppages);
		assertFalse(stoppageManager.isCampaignStopped(campaign));

		//Make sure iws Service is called only once as defined in Expectation

	}
//
//	/**
//	 * When there is no campaign Stoppage at all and lazyInit is true
//	 * @throws IOException
//	 */
	@Test
	public void testOnStopAdvertiser1() throws IOException{
//		//Test Data
//
		final Long otherAdvertiserId = 1234L;
		final Date otherTimestamp = new Date(System.currentTimeMillis());
		final Date otherReactivateDate = new Date(System.currentTimeMillis()+10000) ;

        final Long advertiserId = 123L;
        final Long campaignId = 321L;
        Long timestamp = System.currentTimeMillis();
        Long reactivateDate = System.currentTimeMillis() + 10;

        final Map advertiserStoppagesOneMap = new HashMap();
        advertiserStoppagesOneMap.put(advertiserId, new Stoppage(timestamp, reactivateDate));


        final StoppagesService stoppages = mock(StoppagesService.class);
        expect(new Expectations() {{
            oneOf (stoppages).getAdvertiserStoppages(); will(returnValue(advertiserStoppagesOneMap));
            oneOf (stoppages).getCampaignStoppages(); will(returnValue(new HashMap<>()));
        }});

		final CampaignDto campaign = mock(CampaignDto.class);

		expect(new Expectations() {{
		    allowing (campaign).getId(); will(returnValue(campaignId));
		}});


		StoppageManagerImpl stoppageManager = new StoppageManagerImpl(true, stoppages);

		StopAdvertiserMessage stopAdvertiserMessage = new StopAdvertiserMessage(otherAdvertiserId, null, otherTimestamp, otherReactivateDate);
		stoppageManager.onStopAdvertiser(stopAdvertiserMessage);
		Map<Long,Stoppage> advertiserStoppages =  stoppageManager.getAdvertiserStoppages();
		assertEquals(2, advertiserStoppages.size());

		//Make sure iws Service is called only once as defined in Expectation

	}
//	/**
//	 * When iws service will throw IOException
//	 * @throws IOException
//	 */
	@Test
	public void testOnStopAdvertiser2() throws IOException{
		//Test Data
		final Long campaignId = 321L;

		final Long otherAdvertiserId = 1234L;
		final Date otherTimestamp = new Date(System.currentTimeMillis());
		final Date otherReactivateDate = new Date(System.currentTimeMillis()+10000) ;

        final StoppagesService stoppages = mock(StoppagesService.class);
        expect(new Expectations() {{
            oneOf (stoppages).getAdvertiserStoppages(); will(throwException(new IOException()));
            oneOf (stoppages).getAdvertiserStoppages(); will(returnValue(new HashMap()));
            oneOf (stoppages).getCampaignStoppages(); will(throwException(new IOException()));
        }});
		final CampaignDto campaign = mock(CampaignDto.class);

		expect(new Expectations() {{
		    allowing (campaign).getId(); will(returnValue(campaignId));
		}});


		StoppageManagerImpl stoppageManager = new StoppageManagerImpl(true, stoppages);

		StopAdvertiserMessage stopAdvertiserMessage = new StopAdvertiserMessage(otherAdvertiserId, null, otherTimestamp, otherReactivateDate);
		stoppageManager.onStopAdvertiser(stopAdvertiserMessage);
		Map<Long,Stoppage> advertiserStoppages =  stoppageManager.getAdvertiserStoppages();
		assertEquals(1, advertiserStoppages.size());

		//Make sure iws Service is called only once as defined in Expectation

	}
//	/**
//	 * @throws IOException
//	 */
	@Test
	public void testOnUnStopAdvertiser1() throws IOException{
//		//Test Data

        final Long advertiserId = 123L;
        final Long campaignId = 321L;
        Long timestamp = System.currentTimeMillis();
        Long reactivateDate = System.currentTimeMillis() + 10;

        final Map advertiserStoppagesOneMap = new HashMap();
        advertiserStoppagesOneMap.put(advertiserId, new Stoppage(timestamp, reactivateDate));

        final Map campaignStoppagesOneMap = new HashMap();
        campaignStoppagesOneMap.put(campaignId, new Stoppage(timestamp, reactivateDate));

        final StoppagesService stoppages = mock(StoppagesService.class);
        expect(new Expectations() {{
            oneOf (stoppages).getAdvertiserStoppages(); will(returnValue(advertiserStoppagesOneMap));
            oneOf (stoppages).getCampaignStoppages(); will(returnValue(new HashMap<>()));
        }});

		final CampaignDto campaign = mock(CampaignDto.class);

		expect(new Expectations() {{
		    allowing (campaign).getId(); will(returnValue(campaignId));
		}});


		StoppageManagerImpl stoppageManager = new StoppageManagerImpl(true, stoppages);
		Map<Long,Stoppage> advertiserStoppages =  stoppageManager.getAdvertiserStoppages();
		assertEquals(1, advertiserStoppages.size());

		UnStopAdvertiserMessage unStopAdvertiserMessage = new UnStopAdvertiserMessage(advertiserId);
		stoppageManager.onUnStopAdvertiser(unStopAdvertiserMessage);
		advertiserStoppages =  stoppageManager.getAdvertiserStoppages();
		assertEquals(0, advertiserStoppages.size());

		//Make sure iws Service is called only once as defined in Expectation

	}
//
//
//	/**
//	 * @throws IOException
//	 */
	@Test
	public void testOnUnStopAdvertiser2() throws IOException{
//		//Test Data


        final Long advertiserId = 123L;
        final Long campaignId = 321L;
        Long timestamp = System.currentTimeMillis();
        Long reactivateDate = System.currentTimeMillis() + 10;

        final Map advertiserStoppagesOneMap = new HashMap();
        advertiserStoppagesOneMap.put(advertiserId, new Stoppage(timestamp, reactivateDate));

        final Map campaignStoppagesOneMap = new HashMap();
        campaignStoppagesOneMap.put(campaignId, new Stoppage(timestamp, reactivateDate));

        final StoppagesService stoppages = mock(StoppagesService.class);
        expect(new Expectations() {{
            oneOf (stoppages).getAdvertiserStoppages(); will(returnValue(advertiserStoppagesOneMap));
            oneOf (stoppages).getCampaignStoppages(); will(returnValue(new HashMap()));
        }});

		final CampaignDto campaign = mock(CampaignDto.class);

		expect(new Expectations() {{
		   allowing (campaign).getId(); will(returnValue(campaignId));
		}});


		StoppageManagerImpl stoppageManager = new StoppageManagerImpl(true, stoppages);

		UnStopAdvertiserMessage unStopAdvertiserMessage = new UnStopAdvertiserMessage(advertiserId);
		stoppageManager.onUnStopAdvertiser(unStopAdvertiserMessage);
		Map<Long,Stoppage> advertiserStoppages =  stoppageManager.getAdvertiserStoppages();
		assertEquals(0, advertiserStoppages.size());

		//Make sure iws Service is called only once as defined in Expectation

	}
//	/**
//	 * Test case where iws Service will throw Exception
//	 * @throws IOException
//	 */
	@Test
	public void testOnUnStopAdvertiser3() throws IOException{
//		//Test Data

        final Long advertiserId = 123L;
        final Long campaignId = 321L;
        Long timestamp = System.currentTimeMillis();
        Long reactivateDate = System.currentTimeMillis() + 10;

        final Map advertiserStoppagesOneMap = new HashMap();
        advertiserStoppagesOneMap.put(advertiserId, new Stoppage(timestamp, reactivateDate));

        final Map campaignStoppagesOneMap = new HashMap();
        campaignStoppagesOneMap.put(campaignId, new Stoppage(timestamp, reactivateDate));

        final StoppagesService stoppages = mock(StoppagesService.class);
        expect(new Expectations() {{
            oneOf (stoppages).getAdvertiserStoppages(); will(returnValue(advertiserStoppagesOneMap));
            oneOf (stoppages).getCampaignStoppages(); will(returnValue(new HashMap()));
        }});

		final CampaignDto campaign = mock(CampaignDto.class);

		expect(new Expectations() {{
		    allowing (campaign).getId(); will(returnValue(campaignId));
		}});


		StoppageManagerImpl stoppageManager = new StoppageManagerImpl(true, stoppages);

		UnStopAdvertiserMessage unStopAdvertiserMessage = new UnStopAdvertiserMessage(advertiserId);
		stoppageManager.onUnStopAdvertiser(unStopAdvertiserMessage);
		Map<Long,Stoppage> advertiserStoppages =  stoppageManager.getAdvertiserStoppages();
		assertEquals(0, advertiserStoppages.size());

		//Make sure iws Service is called only once as defined in Expectation

	}

	@Test
	public void testOnStopCampaign1() throws IOException{
		//Test Data

		final Long otherCampaignId = 4321L;
		final Date otherTimestamp = new Date(System.currentTimeMillis());
		final Date otherReactivateDate = new Date(System.currentTimeMillis() + 1000);


        final Long advertiserId = 123L;
        final Long campaignId = 321L;
        Long timestamp = System.currentTimeMillis();
        Long reactivateDate = null;

        final Map advertiserStoppagesOneMap = new HashMap();
        advertiserStoppagesOneMap.put(advertiserId, new Stoppage(timestamp, reactivateDate));

        final Map campaignStoppagesOneMap = new HashMap();
        campaignStoppagesOneMap.put(campaignId, new Stoppage(timestamp, reactivateDate));

        final StoppagesService stoppages = mock(StoppagesService.class);
        expect(new Expectations() {{
            oneOf (stoppages).getAdvertiserStoppages(); will(returnValue(advertiserStoppagesOneMap));
            oneOf (stoppages).getCampaignStoppages(); will(returnValue(campaignStoppagesOneMap));
        }});

		final CreativeDto creative = mock(CreativeDto.class);
		final CampaignDto campaign = mock(CampaignDto.class);
		expect(new Expectations() {{
			allowing (campaign).getId(); will(returnValue(campaignId));
			allowing (creative).getCampaign(); will(returnValue(campaign));
		}});


		StoppageManagerImpl stoppageManager = new StoppageManagerImpl(true, stoppages);

		StopCampaignMessage stopCampaignMessage = new StopCampaignMessage(otherCampaignId, null, otherTimestamp, otherReactivateDate);
		stoppageManager.onStopCampaign(stopCampaignMessage);
		Map<Long,Stoppage> campaignStoppages =  stoppageManager.getCampaignStoppages();
		assertEquals(2, campaignStoppages.size());

		//Make sure iws Service is called only once as defined in Expectation
	}
//	/**
//	 * Test case wher iws service will throw exception
//	 * @throws IOException
//	 */
	@Test
	public void testOnStopCampaign2() throws IOException{
		//Test Data
		final Long otherCampaignId = 4321L;
		final Date otherTimestamp = new Date(System.currentTimeMillis());
		final Date otherReactivateDate = new Date(System.currentTimeMillis() + 1000);


        final Long advertiserId = 123L;
        final Long campaignId = 321L;
        Long timestamp = System.currentTimeMillis();
        Long reactivateDate = System.currentTimeMillis() + 10;

        final Map advertiserStoppagesOneMap = new HashMap();
        advertiserStoppagesOneMap.put(advertiserId, new Stoppage(timestamp, reactivateDate));

        final Map campaignStoppagesOneMap = new HashMap();
        campaignStoppagesOneMap.put(campaignId, new Stoppage(timestamp, reactivateDate));

        final StoppagesService stoppages = mock(StoppagesService.class);
        expect(new Expectations() {{
            oneOf (stoppages).getAdvertiserStoppages(); will(throwException(new IOException()));
            oneOf (stoppages).getAdvertiserStoppages(); will(throwException(new IOException()));
        }});

		final CreativeDto creative = mock(CreativeDto.class);
		final CampaignDto campaign = mock(CampaignDto.class);
		expect(new Expectations() {{
			allowing (campaign).getId(); will(returnValue(campaignId));
			allowing (creative).getCampaign(); will(returnValue(campaign));
		}});


		StoppageManagerImpl stoppageManager = new StoppageManagerImpl(true, stoppages);

		StopCampaignMessage stopCampaignMessage = new StopCampaignMessage(otherCampaignId, null, otherTimestamp, otherReactivateDate);
		stoppageManager.onStopCampaign(stopCampaignMessage);
		Map<Long,Stoppage> campaignStoppages =  stoppageManager.getCampaignStoppages();
		assertEquals(1, campaignStoppages.size());

		//Make sure iws Service is called only once as defined in Expectation
	}
	@Test
	public void testOnUnStopCampaign1() throws IOException{
		//Test Data
        final Long advertiserId = 123L;
        final Long campaignId = 321L;
        Long timestamp = System.currentTimeMillis();
        Long reactivateDate = null;

        final Map advertiserStoppagesOneMap = new HashMap();
        advertiserStoppagesOneMap.put(advertiserId, new Stoppage(timestamp, reactivateDate));

        final Map campaignStoppagesOneMap = new HashMap();
        campaignStoppagesOneMap.put(campaignId, new Stoppage(timestamp, reactivateDate));

        final StoppagesService stoppages = mock(StoppagesService.class);
        expect(new Expectations() {{
            oneOf (stoppages).getAdvertiserStoppages(); will(returnValue(advertiserStoppagesOneMap));
            oneOf (stoppages).getCampaignStoppages(); will(returnValue(campaignStoppagesOneMap));
        }});

		final CreativeDto creative = mock(CreativeDto.class);
		final CampaignDto campaign = mock(CampaignDto.class);
		expect(new Expectations() {{
			allowing (campaign).getId(); will(returnValue(campaignId));
			allowing (creative).getCampaign(); will(returnValue(campaign));
		}});


		StoppageManagerImpl stoppageManager = new StoppageManagerImpl(true, stoppages);

		UnStopCampaignMessage unStopCampaignMessage = new UnStopCampaignMessage(campaignId);
		stoppageManager.onUnStopCampaign(unStopCampaignMessage);
		Map<Long,Stoppage> campaignStoppages =  stoppageManager.getCampaignStoppages();
		assertEquals(0, campaignStoppages.size());
		//Make sure iws Service is called only once as defined in Expectation
	}

//	/**
//	 * Test where iws service will throw exception
//	 * @throws IOException
//	 */
	@Test
	public void testOnUnStopCampaign2() throws IOException{
		//Test Data


        final Long advertiserId = 123L;
        final Long campaignId = 321L;
        Long timestamp = System.currentTimeMillis();
        Long reactivateDate = null;

        final Map advertiserStoppagesOneMap = new HashMap();
        advertiserStoppagesOneMap.put(advertiserId, new Stoppage(timestamp, reactivateDate));

        final Map campaignStoppagesOneMap = new HashMap();
        campaignStoppagesOneMap.put(campaignId, new Stoppage(timestamp, reactivateDate));

        final StoppagesService stoppages = mock(StoppagesService.class);
        expect(new Expectations() {{
            oneOf (stoppages).getAdvertiserStoppages(); will(throwException(new IOException()));
            oneOf (stoppages).getAdvertiserStoppages(); will(throwException(new IOException()));
        }});

		final CreativeDto creative = mock(CreativeDto.class);
		final CampaignDto campaign = mock(CampaignDto.class);
		expect(new Expectations() {{
			allowing (campaign).getId(); will(returnValue(campaignId));
			allowing (creative).getCampaign(); will(returnValue(campaign));
		}});


		StoppageManagerImpl stoppageManager = new StoppageManagerImpl(true, stoppages);

		UnStopCampaignMessage unStopCampaignMessage = new UnStopCampaignMessage(campaignId);
		stoppageManager.onUnStopCampaign(unStopCampaignMessage);
		Map<Long,Stoppage> campaignStoppages =  stoppageManager.getCampaignStoppages();
		assertEquals(0, campaignStoppages.size());
		//Make sure iws Service is called only once as defined in Expectation
	}
}
