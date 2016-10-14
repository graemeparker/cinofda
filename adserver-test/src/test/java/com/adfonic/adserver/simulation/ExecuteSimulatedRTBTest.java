package com.adfonic.adserver.simulation;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.StreamUtils;
import org.springframework.web.context.WebApplicationContext;

import com.adfonic.adserver.AdEvent;
import com.adfonic.adserver.rtb.mapper.YieldLabMapper;
import com.adfonic.adserver.rtb.nonstd.dcadx.AdX;
import com.adfonic.adserver.rtb.nonstd.dcadx.AdX.BidRequest.Builder;
import com.adfonic.adserver.simulation.impl.SimulationAdserverDomainCacheImpl;
import com.adfonic.adserver.simulation.impl.SimulationAdserverJmsImpl;
import com.adfonic.adserver.simulation.impl.SimulationBackupLogger;
import com.adfonic.adserver.simulation.impl.SimulationBackupLogger.Log;
import com.adfonic.adserver.simulation.impl.SimulationDomainCacheManager;
import com.adfonic.adserver.simulation.impl.SimulationDomainCacheManager.SimulationDomainCacheImpl;
import com.adfonic.adserver.simulation.model.AdserverDataModel;
import com.adfonic.adserver.simulation.model.AdserverOutputModel;
import com.adfonic.adserver.simulation.model.ComponentContentSpecMapModel;
import com.adfonic.adserver.simulation.model.ContentModel;
import com.adfonic.adserver.simulation.model.CounterOutputModel;
import com.adfonic.adserver.simulation.model.CreativeAssetAndContentModel;
import com.adfonic.adserver.simulation.model.JmsOutputModel;
import com.adfonic.adserver.simulation.model.LogOutputModel;
import com.adfonic.adserver.simulation.model.PriorityWeightedCreatives;
import com.adfonic.adserver.simulation.model.RequestDataModel;
import com.adfonic.adserver.simulation.model.ResponseDataModel;
import com.adfonic.adserver.simulation.model.SimulationModel;
import com.adfonic.adserver.simulation.model.SizebasedFormatModel;
import com.adfonic.adserver.simulation.model.VerificationModel;
import com.adfonic.adserver.simulation.model.WeightedAdspaceEligibilityModel;
import com.adfonic.domain.cache.AdserverDomainCacheManager;
import com.adfonic.domain.cache.dto.adserver.ComponentDto;
import com.adfonic.domain.cache.dto.adserver.ContentSpecDto;
import com.adfonic.domain.cache.dto.adserver.CountryDto;
import com.adfonic.domain.cache.dto.adserver.DeviceIdentifierTypeDto;
import com.adfonic.domain.cache.dto.adserver.DisplayTypeDto;
import com.adfonic.domain.cache.dto.adserver.FormatDto;
import com.adfonic.domain.cache.dto.adserver.IntegrationTypeDto;
import com.adfonic.domain.cache.dto.adserver.LanguageDto;
import com.adfonic.domain.cache.dto.adserver.ModelDto;
import com.adfonic.domain.cache.dto.adserver.PublicationTypeDto;
import com.adfonic.domain.cache.dto.adserver.adspace.AdSpaceDto;
import com.adfonic.domain.cache.dto.adserver.adspace.PublisherDto;
import com.adfonic.domain.cache.dto.adserver.creative.AdspaceWeightedCreative;
import com.adfonic.domain.cache.dto.adserver.creative.AssetDto;
import com.adfonic.domain.cache.dto.adserver.creative.CreativeDto;
import com.adfonic.domain.cache.ext.AdserverDomainCache;
import com.adfonic.domain.cache.service.RtbCacheServiceImpl;
import com.adfonic.jms.ClickMessage;
import com.adfonic.util.stats.CounterManager;
import com.google.protobuf.TextFormat;
import com.google.protobuf.TextFormat.ParseException;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration("classpath:/com/adfonic/adserver/simulation/simulation-context.xml")
public class ExecuteSimulatedRTBTest {

	private static final Logger LOG = LoggerFactory
			.getLogger(ExecuteSimulatedRTBTest.class);

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

	@Autowired
	SimulationAdserverJmsImpl adserverJms;

	@Autowired
	CounterManager counterManager;

	@Before
	public void setup() throws Exception {
		this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
		YieldLabMapper.TIMEOUT=5000000;
	}

	@Test
	public void exec() throws Exception {
		ObjectMapper mapper = new ObjectMapper();

		mapper.setDeserializerProvider(new SimulationDeserializerProvider());

		SimulationModel model = mapper.readValue(getClass().getClassLoader()
				.getResource("sim/smaato/t001/sim.json"), SimulationModel.class);

		prepareAdserverData(model);
		ResponseDataModel response = performRequest(model);
		AdserverOutputModel testOutput = generateOutput(response);

		String jsonOut = mapper.writeValueAsString(testOutput);
		System.out.println(response.getContentRequired().getJson());

		
		//ObjectWriter pw = mapper.writerWithDefaultPrettyPrinter();
		System.out.println("++++++++++" + jsonOut);

		// assertEquals(pw.writeValueAsString(model.getResponse()),
		// pw.writeValueAsString(response));
	}

	private AdserverOutputModel generateOutput(ResponseDataModel response) {
		AdserverOutputModel out = new AdserverOutputModel();
		VerificationModel<CounterOutputModel> outCount = new VerificationModel<CounterOutputModel>();

		outCount.setRequire(new ArrayList<CounterOutputModel>());
		for (String name : counterManager.getCounterNames()) {
			CounterOutputModel com = new CounterOutputModel();
			com.setName(name);
			com.setCount(counterManager.getCount(name));
			outCount.getRequire().add(com);
		}
		out.setCounter(outCount);

		if (backupLogger.getLogs() != null) {
			VerificationModel<LogOutputModel> logOut = new VerificationModel<LogOutputModel>();
			out.setBackupLogs(logOut);
			logOut.setRequire(new ArrayList<LogOutputModel>());

			for (Log log : backupLogger.getLogs()) {
				LogOutputModel lom = new LogOutputModel();
				lom.setEvent(log.getAdAction().toString());
				lom.setRequiredAttributes(Arrays.asList(log.getExtraValues()));
				logOut.getRequire().add(lom);
			}
		}

		if (adserverJms.getClicks() != null || adserverJms.getEvents() != null) {
			VerificationModel<JmsOutputModel> jmsOut = new VerificationModel<JmsOutputModel>();

			jmsOut.setRequire(new ArrayList<JmsOutputModel>());

			if (adserverJms.getEvents() != null) {
				for (AdEvent adev : adserverJms.getEvents()) {
					JmsOutputModel jom = new JmsOutputModel();
					jom.setEvent(adev.toCsv());
					jmsOut.getRequire().add(jom);
				}
			}
			
			if (adserverJms.getClicks() != null) {
				for (ClickMessage cm : adserverJms.getClicks()) {
					JmsOutputModel jom = new JmsOutputModel();
					jom.setEvent(cm.toString());
					jmsOut.getRequire().add(jom);
				}
			}
			
			if (jmsOut.getRequire().size() > 0) {
				out.setJms(jmsOut);
			}
		}

		return out;
	}

	private ResponseDataModel performRequest(SimulationModel model)
			throws Exception {
		MockHttpServletRequestBuilder mockReq;

		RequestDataModel req = model.getRequest();

		if (req.getContent() == null) {
			throw new IllegalArgumentException(
					"Invalid HTTP request without content: " + req.getUri());
		}

		if (req.getMethod().equalsIgnoreCase("post")) {
			mockReq = post(req.getUri());
		} else if (req.getMethod().equalsIgnoreCase("get")) {
			mockReq = get(req.getUri());
		} else {
			throw new IllegalArgumentException("Invalid HTTP method: "
					+ req.getMethod());
		}

		if (req.getContent().getContentType() != null) {
			mockReq.contentType(MediaType.parseMediaTypes(
					req.getContent().getContentType()).get(0));
		}

		if (req.getHeaders() != null) {
			for (String header : req.getHeaders().keySet()) {
				String value = req.getHeaders().get(header);
				mockReq.header(header, value);
			}
		}

		if (req.getContent().getJson() != null) {
			mockReq.content(req.getContent().getJson());
		}
		
		if (req.getContent().getJsonFile() != null) {
			mockReq.content(StreamUtils.copyToByteArray(getClass()
					.getClassLoader().getResourceAsStream(
							req.getContent().getJsonFile())));
		}

		if (req.getContent().getRaw() != null) {
			mockReq.content(req.getContent().getRaw());
		}
		
		if (req.getContent().getRawFile() != null) {
			mockReq.content(StreamUtils.copyToByteArray(getClass()
					.getClassLoader().getResourceAsStream(
							req.getContent().getRawFile())));
		}

		if (req.getContent().getProto() != null) {
			mockReq.content(parseProto(req.getContent().getProto()));
		}
		
		if (req.getContent().getProtoFile() != null) {
			String proto = StreamUtils.copyToString(getClass()
					.getClassLoader().getResourceAsStream(
							req.getContent().getProtoFile()), Charset.forName("UTF-8"));
			mockReq.content(parseProto(proto));
		}


		MvcResult result = this.mockMvc.perform(mockReq).andReturn();

		ResponseDataModel response = new ResponseDataModel();
		response.setCode(result.getResponse().getStatus());
		response.setHeadersRequired(new TreeMap<String, String>());

		for (String h : result.getResponse().getHeaderNames()) {
			response.getHeadersRequired().put(h,
					result.getResponse().getHeader(h));
		}

		response.setContentRequired(new ContentModel());
		response.getContentRequired().setContentType(
				result.getResponse().getContentType());
		response.getContentRequired().setJson(
				result.getResponse().getContentAsString());

		return response;
	}

	private byte[] parseProto(String proto) throws ParseException {
		Builder reqBuilder = AdX.BidRequest.newBuilder();
		
		TextFormat.merge(proto, reqBuilder);
		
		return reqBuilder.build().toByteArray();
	}

	private void prepareAdserverData(SimulationModel model) {
		Map<Long, AssetDto> assets = new HashMap<>();
		Map<Long, ContentSpecDto> tempContentSpecDto = new HashMap<Long, ContentSpecDto>();
		Map<Long, ComponentDto> tempComponentsList = new HashMap<Long, ComponentDto>();
		Map<Long, DisplayTypeDto> tempDisplayTypes = new HashMap<Long, DisplayTypeDto>();

		AdserverDomainCache cache = adserverCache.getCache();
		SimulationAdserverDomainCacheImpl simAdserverCache = (SimulationAdserverDomainCacheImpl) cache;
		SimulationDomainCacheImpl simDomainCache = (SimulationDomainCacheImpl) domainCache
				.getCache();

		AdserverDataModel adsd = model.getAdserverData();

		((RtbCacheServiceImpl) simAdserverCache.getRtbCacheService())
				.setRtbEnabled(adsd.isRtbEnabled());

		if (adsd.getAdspaces() != null) {
			for (AdSpaceDto adspace : adsd.getAdspaces()) {
				simAdserverCache.getRtbCacheService().addRtbPublicationAdSpace(
						adspace);
			}
		}

		if (adsd.getAssets() != null) {
			for (AssetDto asset : adsd.getAssets()) {
				assets.put(asset.getId(), asset);
			}
		}

		if (adsd.getCreatives() != null) {
			for (CreativeDto creative : adsd.getCreatives()) {
				simAdserverCache.getCreativeService().addCreativeToCache(creative);
				cache.addCreativeToCache(creative);
			}
		}

		if (adsd.getCountries() != null) {
			for (CountryDto country : adsd.getCountries()) {
				simDomainCache.countriesById.put(country.getId(), country);
				simDomainCache.countriesByIsoCode.put(country.getIsoCode(),
						country);
				simDomainCache.countriesByIsoAlpha3.put(country.getName(),
						country);
			}
		}
		
		if (adsd.getLanguages() != null) {
			for (LanguageDto lang : adsd.getLanguages()) {
				simDomainCache.languagesById.put(lang.getId(), lang);
				simDomainCache.languagesByIsoCode.put(lang.getISOCode(),
						lang);
			}
		}

		if (adsd.getFormats() != null) {
			for (FormatDto format : adsd.getFormats()) {
				simDomainCache.formatsById.put(format.getId(), format);
				simDomainCache.formatsBySystemName.put(format.getSystemName(), format);
				
				for (ComponentDto component : format.getComponents()) {
					simDomainCache.componentsByFormatAndSystemName.put(format.getSystemName(), component.getSystemName(), component);
					tempComponentsList.put(component.getId(), component);
				}
			}
		}

		if (adsd.getModels() != null) {
			for (ModelDto md : adsd.getModels()) {
				simDomainCache.modelsByExternalID.put(md.getExternalID(), md);
			}
		}

		if (adsd.getPublishers() != null) {
			for (PublisherDto publisher : adsd.getPublishers()) {
				cache.addPublisherByExternalId(publisher.getExternalId(),
						publisher.getId());
			}
		}

		if (adsd.getDeviceIdTypes() != null) {
			for (DeviceIdentifierTypeDto devType : adsd.getDeviceIdTypes()) {
				simDomainCache.deviceIdentifierTypesById.put(devType.getId(),
						devType);
				simDomainCache.deviceIdentifierTypesBySystemName.put(
						devType.getSystemName(), devType);
				simDomainCache.deviceIdentifierTypeIdsBySystemName.put(
						devType.getSystemName(), devType.getId());
			}
		}

		if (adsd.getPublicationTypes() != null) {
			for (PublicationTypeDto pubType : adsd.getPublicationTypes()) {
				simDomainCache.publicationTypesById.put(pubType.getId(),
						pubType);
			}
		}

		if (adsd.getIntegrationTypes() != null) {
			for (IntegrationTypeDto intType : adsd.getIntegrationTypes()) {
				simDomainCache.integrationTypesById.put(intType.getId(),
						intType);
			}
		}

		if (adsd.getIntegrationTypes() != null) {
			for (DisplayTypeDto dtd : adsd.getDisplayTypes()) {
				simDomainCache.displayTypesBySystemName.put(
						dtd.getSystemName(), dtd);
				tempDisplayTypes.put(dtd.getId(), dtd);
			}
		}

		if (adsd.getCreativeAssetContents() != null) {
			for (CreativeAssetAndContentModel caac : adsd
					.getCreativeAssetContents()) {
				CreativeDto creative = cache.getCreativeById(caac
						.getCreativeId());
				if (creative != null) {
					creative.setAsset(caac.getDisplayTypeId(),
							caac.getComponentId(),
							assets.get(caac.getAssetId()),
							caac.getContentTypeId());
				} else {
					LOG.warn("Creative not found: " + caac.getCreativeId());
				}
			}
		}

		if (adsd.getFormatBySizes() != null) {
			for (SizebasedFormatModel sfm : adsd.getFormatBySizes()) {
				Set<FormatDto> formats = new HashSet<>();
				if (sfm.getFormatIds() != null) {
					for (Long id : sfm.getFormatIds()) {
						FormatDto format = simDomainCache.formatsById.get(id);
						if (format != null) {
							formats.add(format);
						} else {
							LOG.warn("Unknown format found with id: " + id);
						}
					}
				}
				simDomainCache.boxableFormatSizeMap.put(sfm.getWidth(),
						sfm.getHeight(), formats);
			}
		}

		if (adsd.getWeightedAdspaces() != null) {
			for (WeightedAdspaceEligibilityModel waem : adsd.getWeightedAdspaces()) {
				Set<AdspaceWeightedCreative> w = new HashSet<AdspaceWeightedCreative>();

				if (waem.getCreativesByPriority() != null) {
					for (PriorityWeightedCreatives pwc : waem
							.getCreativesByPriority()) {
						AdspaceWeightedCreative awc = new AdspaceWeightedCreative();
						awc.setCreativeIds(pwc.getCreatives().toArray(
								new Long[0]));
						awc.setPriority(pwc.getPriority());
						w.add(awc);
					}
				}

				ArrayList<CountryDto> countries = new ArrayList<>();

				if (waem.getCountriesIsoCode() != null) {
					for (String cc : waem.getCountriesIsoCode()) {
						CountryDto countryDto = simDomainCache.countriesByIsoCode
								.get(cc);
						if (countryDto != null) {
							countries.add(countryDto);
						}
					}

					simAdserverCache.getCreativeService()
							.addAdSpaceEligibleCreative(waem.getAdspaceId(), w,
									countries);
				}
			}
		}
		
		if (adsd.getContentSpecs()!=null){
			for(ContentSpecDto cs : adsd.getContentSpecs()){
				tempContentSpecDto.put(cs.getId(), cs);
			}
		}
		
		if (adsd.getComponentContentSpecMap()!=null){
			for (ComponentContentSpecMapModel ccsmm : adsd.getComponentContentSpecMap()){
				ComponentDto component = tempComponentsList.get(ccsmm.getComponentId());
				DisplayTypeDto displayType = tempDisplayTypes.get(ccsmm.getDisplayTypeId());
				ContentSpecDto contentSpec = tempContentSpecDto.get(ccsmm.getContentSpecId());
				
				if (component==null){
					LOG.warn("Component defined on ComponentContentSpecMap not found: " + ccsmm.getComponentId());
				}else if (displayType==null){
					LOG.warn("DisplayType defined on ComponentContentSpecMap not found: " + ccsmm.getDisplayTypeId());
				}else if (contentSpec==null){
					LOG.warn("contentSpec defined on ComponentContentSpecMap not found: " + ccsmm.getContentSpecId());
				}else{
					component.getContentSpecMap().put(displayType, contentSpec);
				}
			}
		}
	}
}
