package com.adfonic.adserver.simulation;

import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.adfonic.adserver.rtb.nonstd.dcadx.AdX;
import com.adfonic.adserver.rtb.nonstd.dcadx.AdX.BidRequest.Builder;
import com.adfonic.adserver.simulation.impl.SimulationAdserverDomainCacheImpl;
import com.adfonic.adserver.simulation.impl.SimulationBackupLogger;
import com.adfonic.adserver.simulation.impl.SimulationDomainCacheManager;
import com.adfonic.adserver.simulation.impl.SimulationDomainCacheManager.SimulationDomainCacheImpl;
import com.adfonic.adserver.simulation.model.AdserverDataModel;
import com.adfonic.adserver.simulation.model.ContentModel;
import com.adfonic.adserver.simulation.model.CreativeAssetAndContentModel;
import com.adfonic.adserver.simulation.model.PriorityWeightedCreatives;
import com.adfonic.adserver.simulation.model.RequestDataModel;
import com.adfonic.adserver.simulation.model.SimulationModel;
import com.adfonic.adserver.simulation.model.SizebasedFormatModel;
import com.adfonic.adserver.simulation.model.WeightedAdspaceEligibilityModel;
import com.adfonic.domain.AdSpace;
import com.adfonic.domain.BidType;
import com.adfonic.domain.Campaign;
import com.adfonic.domain.CampaignBid.BidModelType;
import com.adfonic.domain.Creative;
import com.adfonic.domain.DestinationType;
import com.adfonic.domain.RtbConfig.RtbAuctionType;
import com.adfonic.domain.TrackingIdentifierType;
import com.adfonic.domain.cache.AdserverDomainCacheManager;
import com.adfonic.domain.cache.dto.adserver.CountryDto;
import com.adfonic.domain.cache.dto.adserver.DeviceIdentifierTypeDto;
import com.adfonic.domain.cache.dto.adserver.DisplayTypeDto;
import com.adfonic.domain.cache.dto.adserver.FormatDto;
import com.adfonic.domain.cache.dto.adserver.IntegrationTypeDto;
import com.adfonic.domain.cache.dto.adserver.ModelDto;
import com.adfonic.domain.cache.dto.adserver.PublicationTypeDto;
import com.adfonic.domain.cache.dto.adserver.VendorDto;
import com.adfonic.domain.cache.dto.adserver.adspace.AdSpaceDto;
import com.adfonic.domain.cache.dto.adserver.adspace.PublicationDto;
import com.adfonic.domain.cache.dto.adserver.adspace.PublisherDto;
import com.adfonic.domain.cache.dto.adserver.adspace.RtbConfigDto;
import com.adfonic.domain.cache.dto.adserver.creative.AdspaceWeightedCreative;
import com.adfonic.domain.cache.dto.adserver.creative.AdvertiserDto;
import com.adfonic.domain.cache.dto.adserver.creative.AssetDto;
import com.adfonic.domain.cache.dto.adserver.creative.CampaignBidDto;
import com.adfonic.domain.cache.dto.adserver.creative.CampaignDto;
import com.adfonic.domain.cache.dto.adserver.creative.CompanyDto;
import com.adfonic.domain.cache.dto.adserver.creative.CreativeDto;
import com.adfonic.domain.cache.dto.adserver.creative.DestinationDto;
import com.adfonic.domain.cache.ext.AdserverDomainCache;
import com.adfonic.domain.cache.service.RtbCacheServiceImpl;
import com.google.protobuf.TextFormat;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration("classpath:/com/adfonic/adserver/simulation/simulation-context.xml")
public class SimulateRTBTest {

	private MockMvc mockMvc;

	@Autowired
	WebApplicationContext wac;
	@Autowired
	MockHttpSession session;
	@Autowired
	MockHttpServletRequest request;

	@Autowired
	SimulationBackupLogger backupLogger;
	@Autowired
	SimulationDomainCacheManager domainCache;
	@Autowired
	AdserverDomainCacheManager adserverCache;

	// private String req1 = "detected_language: \"en\" adslot {\n" +
	// "  id: 1\n"
	// + "  width: 320\n" + "  height: 50\n" + "  excluded_attribute: 7\n"
	// + "  excluded_attribute: 32\n" + "  excluded_attribute: 30\n"
	// + "  excluded_attribute: 8\n" + "  excluded_attribute: 9\n"
	// + "  excluded_attribute: 22\n" + "  excluded_sensitive_category: 19\n"
	// + "  excluded_sensitive_category: 10\n"
	// + "  excluded_sensitive_category: 31\n"
	// + "  excluded_sensitive_category: 24\n" + "  matching_ad_data {\n"
	// + "	   adgroup_id: 4546339036\n" + "    minimum_cpm_micros: 1780000\n"
	// + "  }\n" + "  slot_visibility: ABOVE_THE_FOLD\n" + "  ad_block_key: 1\n"
	// + "  publisher_settings_list_id: 7171541410938523948\n" + "  17: 0\n"
	// + "  18: 0\n" + "}\n" + "is_test: false\n" + "mobile {\n"
	// + "  platform: \"android\"\n" + "  DEPRECATED_carrier_name: \"\"\n"
	// + "  DEPRECATED_carrier_country: \"\"\n"
	// + "  app_id: \"com.soulring.dowsingdeluxe\"\n" + "  is_app: true\n"
	// + "  mobile_device_type: TABLET\n" + "  screen_orientation: 1\n"
	// + "  brand: \"samsung\"\n" + "  model: \"gt-p1000\"\n"
	// + "  os_version {\n" + "	os_version_major: 2\n"
	// + "	os_version_minor: 2\n" + "  }\n" + "  screen_width: 320\n"
	// + "  screen_height: 796\n" + "  carrier_id: 0\n"
	// + "  device_pixel_ratio_millis: 1500\n" + "}\n"
	// + "postal_code_prefix: \"BT5\"\n" + "geo_criteria_id: 1007274\n"
	// + "seller_network_id: 1\n"
	// + "publisher_settings_list_id: 7171541410938523948\n";

	private String req1 = "id: \"SMn\\263\\000\\rgl\\n\\302\\224\\320\\345\\000\\020 \"\n"
			+ "ip: \"O1\357\"\n"
			+ "user_agent: \"Mozilla/5.0 (iPhone; CPU iPhone OS 7_1 like Mac OS X) AppleWebKit/537.51.2 (KHTML, like Gecko) Mobile/11D167,gzip(gfe)\"\n"
			+ "url: \"http://mbappgewtkmbrgy3tqmrxgy888888.com/\"\n"
			+ "detected_language: \"it\"\n"
			+ "detected_vertical {\n"
			+ "  id: 39\n"
			+ "  weight: 0.5\n"
			+ "}\n"
			+ "detected_vertical {\n"
			+ "  id: 1462\n"
			+ "  weight: 0.5\n"
			+ "}\n"
			+ "adslot {\n"
			+ "  id: 1\n"
			+ "  width: 320\n"
			+ "  height: 50\n"
			+ "  excluded_attribute: 7\n"
			+ "  excluded_attribute: 30\n"
			+ "  excluded_attribute: 22\n"
			+ "  excluded_attribute: 13\n"
			+ "  excluded_attribute: 14\n"
			+ "  excluded_attribute: 15\n"
			+ "  excluded_attribute: 16\n"
			+ "  excluded_attribute: 17\n"
			+ "  excluded_attribute: 18\n"
			+ "  excluded_attribute: 19\n"
			+ "  excluded_attribute: 20\n"
			+ "  excluded_attribute: 26\n"
			+ "  excluded_attribute: 27\n"
			+ "  matching_ad_data {\n"
			+ "    adgroup_id: 4546339036\n"
			+ "    minimum_cpm_micros: 220000\n"
			+ "  }\n"
			+ "  slot_visibility: ABOVE_THE_FOLD\n"
			+ "  excluded_product_category: 10031\n"
			+ "  excluded_product_category: 10106\n"
			+ "  excluded_product_category: 13566\n"
			+ "  excluded_product_category: 13423\n"
			+ "  ad_block_key: 1\n"
			+ "  publisher_settings_list_id: 17473617890803971578\n"
//			+ "  17: 0\n"
//			+ "  18: 0\n"
			+ "}\n"
			+ "is_test: false\n"
			+ "cookie_version: 1\n"
			+ "google_user_id: \"CAESEEmXTwj0_ByIY8LlEcIY7Yg\"\n"
			+ "vertical_dictionary_version: 2\n"
			+ "timezone_offset: 120\n"
			+ "detected_content_label: 39\n"
			+ "mobile {\n"
			+ "  platform: \"iphone\"\n"
			+ "  DEPRECATED_carrier_name: \"\"\n"
			+ "  DEPRECATED_carrier_country: \"\"\n"
			+ "  app_id: \"501678276\"\n"
			+ "  is_app: true\n"
			+ "  mobile_device_type: HIGHEND_PHONE\n"
			+ "  screen_orientation: 2\n"
			+ "  app_category_ids: 60506\n"
			+ "  app_category_ids: 60525\n"
			+ "  app_category_ids: 60526\n"
			+ "  app_category_ids: 60500\n"
			+ "  app_category_ids: 69500\n"
			+ "  brand: \"apple\"\n"
			+ "  model: \"iphone\"\n"
			+ "  os_version {\n"
			+ "    os_version_major: 7\n"
			+ "    os_version_minor: 1\n"
			+ "  }\n"
			+ "  screen_width: 568\n"
			+ "  screen_height: 320\n"
			+ "  carrier_id: 0\n"
			+ "  device_pixel_ratio_millis: 2000\n"
			+ "  encrypted_advertising_id: \"SMn\263\\000\\rgl\\n\\302\\224\\320\\001\\000\\020 \\252\\206\\210i\\314\\327\\360\\276-@T\\361\\274\\315\\232\\026\\023:\\3669\"\n"
			+ "}\n"
			+ "cookie_age_seconds: 6910681\n"
			+ "postal_code: \"00186\"\n"
			+ "geo_criteria_id: 1008736\n"
			+ "seller_network_id: 1\n"
			+ "publisher_settings_list_id: 2042544978129220379\n";

	private String userAgent = "Mozilla/5.0 (iPhone; CPU iPhone OS 7_1 like Mac OS X) AppleWebKit/537.51.2 (KHTML, like Gecko) Mobile/11D167,gzip(gfe)\"";

	@Before
	public void setup() throws Exception {
		this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
	}

	// @Test
	public void testFailed() throws Exception {
		String s = "/rtb/dcadx/bid/pubId";

		this.mockMvc.perform(
				post(s).contentType(MediaType.APPLICATION_JSON).content(
						req1.getBytes("UTF-8"))).andExpect(status().is(204));

		assertEquals(
				"[Log [adAction=BID_FAILED, extraValues=[RTB_BID_FAILED, rejected]]]",
				backupLogger.getLogs().toString());
	}

	@Test
	public void testSuccess() throws Exception {
		String s = "/rtb/dcadx/bid/pubId";
		long id = 1;

		AdSpaceDto adspace = new AdSpaceDto();
		adspace.setExternalID("pubId");
		adspace.setStatus(AdSpace.Status.VERIFIED);
		adspace.setId(id++);

		PublicationDto publication = new PublicationDto();
		publication.setRtbId("RTB2AID-69db518e4a644f3c85a8613a3e9744c0");
		publication.setId(id++);
		publication.setTrackingIdentifierType(TrackingIdentifierType.DEVICE);
		publication.setAdRequestTimeout(500000L);
		publication.setCategoryId(16L);

		PublicationTypeDto pubType = new PublicationTypeDto();
		pubType.setId(id++);
		pubType.setSystemName("FooBar");
		publication.setPublicationTypeId(pubType.getId());

		IntegrationTypeDto intType = new IntegrationTypeDto();
		intType.setId(id++);
		pubType.setDefaultIntegrationTypeId(intType.getId());
		intType.setName("Test");

		CreativeDto creative = new CreativeDto();
		CampaignDto campaign = new CampaignDto();
		campaign.setCurrentBid(new CampaignBidDto());
		campaign.getCurrentBid().setAmount(0.80);
		campaign.getCurrentBid().setBidModelType(BidModelType.SECOND_PRICE);
		campaign.getCurrentBid().setBidType(BidType.CPM);
		campaign.getCurrentBid().setId(id++);

		FormatDto format = new FormatDto();
		format.setName("320x20");
		format.setId(id++);
		format.setSystemName("iPhone");
		adspace.getFormatIds().add(format.getId());

		DisplayTypeDto dtd = new DisplayTypeDto();
		format.getDisplayTypes().add(dtd);
		dtd.setName(format.getSystemName());
		dtd.setSystemName(format.getSystemName());
		dtd.setId(id++);

		creative.setId(id++);
		creative.setCampaign(campaign);
		creative.setStatus(Creative.Status.ACTIVE);
		creative.setFormatId(id++);
		DestinationDto destination = new DestinationDto();
		creative.setDestination(destination);
		destination.setId(id++);
		destination.setData("Foobar");
		destination.setDestinationType(DestinationType.URL);

		campaign.setStatus(Campaign.Status.ACTIVE);
		campaign.setId(id++);
		campaign.setThrottle(100);
		AdvertiserDto advertiser = new AdvertiserDto();
		campaign.setAdvertiser(advertiser);
		advertiser.setId(id++);
		CompanyDto company = new CompanyDto();
		advertiser.setCompany(company);
		company.setId(id++);

		PublisherDto publisher = new PublisherDto();
		publisher.setId(id++);

		RtbConfigDto rtbConfig = new RtbConfigDto();
		publisher.setRtbConfig(rtbConfig);
		rtbConfig.setBidCurrency("USD");
		rtbConfig.setAuctionType(RtbAuctionType.SECOND_PRICE);
		rtbConfig.setRtbLostTimeDuration(120000);

		publication.setPublisher(publisher);
		adspace.setPublication(publication);

		DeviceIdentifierTypeDto devType = new DeviceIdentifierTypeDto();
		devType.setSystemName("dpid");
		devType.setId(id++);

		AdserverDomainCache cache = adserverCache.getCache();
		SimulationAdserverDomainCacheImpl simAdserverCache = (SimulationAdserverDomainCacheImpl) cache;

		simAdserverCache.getRtbCacheService().addRtbPublicationAdSpace(adspace);
		((RtbCacheServiceImpl) simAdserverCache.getRtbCacheService())
				.setRtbEnabled(true);

		simAdserverCache.getCreativeService().addCreativeToCache(creative);

		Set<AdspaceWeightedCreative> w = new HashSet<AdspaceWeightedCreative>();
		AdspaceWeightedCreative awc = new AdspaceWeightedCreative();
		w.add(awc);
		awc.setCreativeIds(new Long[] { creative.getId() });
		awc.setPriority(1);

		CountryDto country = new CountryDto();
		country.setId(id++);
		country.setIsoCode("US");
		country.setName("USA");

		AssetDto asset = new AssetDto();
		creative.setAsset(dtd.getId(), 192L, asset, 192L);

		simAdserverCache.getCreativeService()
				.addAdSpaceEligibleCreative(adspace.getId(), w,
						Arrays.asList(new CountryDto[] { country }));
		((RtbCacheServiceImpl) simAdserverCache.getRtbCacheService())
				.setRtbEnabled(true);

		cache.addPublisherByExternalId("pubId", publisher.getId());
		cache.addCreativeToCache(creative);

		ModelDto model = new ModelDto();
		model.setExternalID("iPhone5s");
		model.setDeviceGroupId(id++);
		model.setVendor(new VendorDto());
		model.getVendor().setId(id++);
		model.getVendor().setName("Apple");

		HashSet<FormatDto> formats = new HashSet<>();
		formats.add(format);
		((SimulationDomainCacheImpl) domainCache.getCache()).boxableFormatSizeMap
				.put(320, 20, formats);
		((SimulationDomainCacheImpl) domainCache.getCache()).publicationTypesById
				.put(pubType.getId(), pubType);
		((SimulationDomainCacheImpl) domainCache.getCache()).integrationTypesById
				.put(intType.getId(), intType);
		((SimulationDomainCacheImpl) domainCache.getCache()).deviceIdentifierTypesById
				.put(devType.getId(), devType);
		((SimulationDomainCacheImpl) domainCache.getCache()).deviceIdentifierTypesBySystemName
				.put(devType.getSystemName(), devType);
		((SimulationDomainCacheImpl) domainCache.getCache()).deviceIdentifierTypeIdsBySystemName
				.put(devType.getSystemName(), devType.getId());
		((SimulationDomainCacheImpl) domainCache.getCache()).countriesById.put(
				country.getId(), country);
		((SimulationDomainCacheImpl) domainCache.getCache()).countriesByIsoCode
				.put(country.getIsoCode(), country);
		((SimulationDomainCacheImpl) domainCache.getCache()).countriesByIsoAlpha3
				.put(country.getName(), country);
		((SimulationDomainCacheImpl) domainCache.getCache()).displayTypesBySystemName
				.put(dtd.getSystemName(), dtd);
		((SimulationDomainCacheImpl) domainCache.getCache()).formatsById.put(
				format.getId(), format);
		((SimulationDomainCacheImpl) domainCache.getCache()).formatsBySystemName
				.put(format.getSystemName(), format);
		((SimulationDomainCacheImpl) domainCache.getCache()).modelsByExternalID
				.put("iPhone5s", model);

		
		Builder bidReq = AdX.BidRequest.newBuilder();
		TextFormat.merge(req1, bidReq);
		byte[] payload = bidReq.build().toByteArray();
		
		
		this.mockMvc.perform(post(s).contentType(MediaType.APPLICATION_OCTET_STREAM)
				.header("User-Agent", userAgent)
				.content(payload)).andExpect(status().is(200));

		SimulationModel m = new SimulationModel();
		AdserverDataModel adsData = new AdserverDataModel();
		m.setAdserverData(adsData);

		adsData.setAdspaces(new ArrayList<AdSpaceDto>());
		adsData.setPublications(new ArrayList<PublicationDto>());
		adsData.setPublicationTypes(new ArrayList<PublicationTypeDto>());
		adsData.setIntegrationTypes(new ArrayList<IntegrationTypeDto>());
		adsData.setCreatives(new ArrayList<CreativeDto>());
		adsData.setCampaignes(new ArrayList<CampaignDto>());
		adsData.setFormats(new ArrayList<FormatDto>());
		adsData.setDisplayTypes(new ArrayList<DisplayTypeDto>());
		adsData.setDestinations(new ArrayList<DestinationDto>());
		adsData.setAdvertisers(new ArrayList<AdvertiserDto>());
		adsData.setCompanies(new ArrayList<CompanyDto>());
		adsData.setCountries(new ArrayList<CountryDto>());
		adsData.setPublishers(new ArrayList<PublisherDto>());
		adsData.setDeviceIdTypes(new ArrayList<DeviceIdentifierTypeDto>());
		adsData.setAssets(new ArrayList<AssetDto>());
		adsData.setModels(new ArrayList<ModelDto>());

		adsData.getAdspaces().add(adspace);
		adsData.getAdvertisers().add(advertiser);
		adsData.getAssets().add(asset);
		adsData.getCampaignes().add(campaign);
		adsData.getCompanies().add(company);
		adsData.getCountries().add(country);
		adsData.getCreatives().add(creative);
		adsData.getDestinations().add(destination);
		adsData.getDeviceIdTypes().add(devType);
		adsData.getDisplayTypes().add(dtd);
		adsData.getFormats().add(format);
		adsData.getIntegrationTypes().add(intType);
		adsData.getModels().add(model);
		adsData.getPublications().add(publication);
		adsData.getPublicationTypes().add(pubType);
		adsData.getPublishers().add(publisher);

		SizebasedFormatModel sf = new SizebasedFormatModel();
		sf.setFormatIds(new HashSet<Long>(Arrays.asList(new Long[] { format
				.getId() })));
		sf.setHeight(20);
		sf.setWidth(320);
		adsData.setFormatBySizes(new ArrayList<SizebasedFormatModel>());
		adsData.getFormatBySizes().add(sf);

		WeightedAdspaceEligibilityModel wae = new WeightedAdspaceEligibilityModel();
		wae.setAdspaceId(adspace.getId());
		wae.setCountriesIsoCode(Arrays.asList(new String[] { "US" }));
		wae.setCreativesByPriority(new ArrayList<PriorityWeightedCreatives>());
		PriorityWeightedCreatives pwc = new PriorityWeightedCreatives();
		pwc.setPriority(1);
		pwc.setCreatives(Arrays.asList(new Long[] { creative.getId() }));

		adsData.setWeightedAdspaces(new ArrayList<WeightedAdspaceEligibilityModel>());
		adsData.getWeightedAdspaces().add(wae);

		CreativeAssetAndContentModel cac = new CreativeAssetAndContentModel();
		cac.setAssetId(asset.getId());
		cac.setComponentId(id++);
		cac.setContentTypeId(id++);
		cac.setDisplayTypeId(dtd.getId());
		adsData.setCreativeAssetContents(new ArrayList<CreativeAssetAndContentModel>());
		adsData.getCreativeAssetContents().add(cac);

		RequestDataModel req = new RequestDataModel();
		m.setRequest(req);
		req.setMethod("post");
		req.setUri(s);
		req.setContent(new ContentModel());
		req.setHeaders(new HashMap<String, String>());
		req.getHeaders().put("User-Agent", userAgent);
		req.setContent(new ContentModel());
		req.getContent().setJson(req1);
		req.getContent().setContentType(MediaType.APPLICATION_JSON.toString());

//		ObjectMapper map = new ObjectMapper();
//		String jsonOut = map.writerWithDefaultPrettyPrinter()
//				.writeValueAsString(m);
//		System.out.println(jsonOut);
	}
}
