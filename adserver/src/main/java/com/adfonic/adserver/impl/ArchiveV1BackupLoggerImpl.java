package com.adfonic.adserver.impl;

import java.math.BigDecimal;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PreDestroy;
import javax.servlet.http.HttpServletRequest;

import kafka.common.KafkaException;
import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;
import net.byyd.archive.model.v1.AdAction;
import net.byyd.archive.model.v1.AdEvent;
import net.byyd.archive.model.v1.ArchiveV1JsonWriter;
import net.byyd.archive.model.v1.Gender;
import net.byyd.archive.model.v1.V1DomainModelMapper;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.log4j.SimpleLayout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import uk.org.simonsite.log4j.appender.TimeAndSizeRollingAppender;

import com.adfonic.adserver.AdserverConstants;
import com.adfonic.adserver.BackupLogger;
import com.adfonic.adserver.DisplayTypeUtils;
import com.adfonic.adserver.Impression;
import com.adfonic.adserver.LocalBudgetManager;
import com.adfonic.adserver.LocalBudgetManager.ClickRegisterState;
import com.adfonic.adserver.Parameters;
import com.adfonic.adserver.ReservePot;
import com.adfonic.adserver.TargetingContext;
import com.adfonic.adserver.financial.FinancialCalc;
import com.adfonic.adserver.financial.Input;
import com.adfonic.adserver.financial.LicenseCPM_RTB;
import com.adfonic.adserver.financial.ManagedCPC_NONrtb;
import com.adfonic.adserver.financial.ManagedCPC_RTB;
import com.adfonic.adserver.financial.ManagedCPM_NONrtb;
import com.adfonic.adserver.financial.ManagedCPM_RTB;
import com.adfonic.adserver.financial.MarginShareCPC_NONrtb;
import com.adfonic.adserver.financial.MarginShareCPC_RTB;
import com.adfonic.adserver.financial.MarginShareCPM_NONrtb;
import com.adfonic.adserver.financial.MarginShareCPM_RTB;
import com.adfonic.adserver.financial.Output;
import com.adfonic.adserver.rtb.impl.RtbBidLogicImpl;
import com.adfonic.adserver.rtb.nativ.ByydDevice;
import com.adfonic.adserver.rtb.nativ.ByydRequest;
import com.adfonic.adserver.rtb.nativ.ByydUser;
import com.adfonic.domain.BidType;
import com.adfonic.domain.Medium;
import com.adfonic.domain.RtbConfig;
import com.adfonic.domain.UnfilledReason;
import com.adfonic.domain.cache.DomainCache;
import com.adfonic.domain.cache.dto.adserver.CountryDto;
import com.adfonic.domain.cache.dto.adserver.DisplayTypeDto;
import com.adfonic.domain.cache.dto.adserver.FormatDto;
import com.adfonic.domain.cache.dto.adserver.GeotargetDto;
import com.adfonic.domain.cache.dto.adserver.ModelDto;
import com.adfonic.domain.cache.dto.adserver.OperatorDto;
import com.adfonic.domain.cache.dto.adserver.adspace.AdSpaceDto;
import com.adfonic.domain.cache.dto.adserver.adspace.PublicationDto;
import com.adfonic.domain.cache.dto.adserver.adspace.PublisherDto;
import com.adfonic.domain.cache.dto.adserver.creative.BidDeductionDto;
import com.adfonic.domain.cache.dto.adserver.creative.CampaignBidDto;
import com.adfonic.domain.cache.dto.adserver.creative.CampaignDto;
import com.adfonic.domain.cache.dto.adserver.creative.CompanyDto;
import com.adfonic.domain.cache.dto.adserver.creative.CreativeDto;
import com.adfonic.domain.cache.ext.AdserverDomainCache;
import com.adfonic.geo.USState;
import com.adfonic.util.stats.CounterManager;

/**
 * Backup logger (formerly CSV logging in AdEventLoggerImpl)
 * 
 * @see https://tickets.adfonic.com/browse/SC-19
 * 
 *      NOTE: This class can work alone, but it's intended to work in
 *      conjunction with com.adfonic.adserver.BackupLoggerFilter in order to
 *      track the elapsed time during the request. If you add that filter to
 *      your web.xml, then request times will be written to the log. If you
 *      don't use the filter, that's fine, but you'll see -1 in place of the
 *      elapsed request time.
 */
@Component
public class ArchiveV1BackupLoggerImpl implements BackupLogger {
    private static final transient Logger LOG = Logger.getLogger(ArchiveV1BackupLoggerImpl.class.getName());

    private static final String EXPLICIT_GEO_LOCATION = "EXPLICIT";

    private static final ThreadLocal<List<AdEvent>> TL_QUEUE = new ThreadLocal<List<AdEvent>>();
    private static final ThreadLocal<Long> TL_FILTER_REQUEST_START_TIME = new ThreadLocal<Long>();
    private static final ThreadLocal<Long> TL_CONTROLLER_REQUEST_START_TIME = new ThreadLocal<Long>();

	private static String shard;

	private static String serverName;

    private static final BigDecimal THOUSAND = new BigDecimal("1000");

    private V1DomainModelMapper mapper = new V1DomainModelMapper();
    private ArchiveV1JsonWriter writer = new ArchiveV1JsonWriter();

    // Custom log4j layout that spits out only the message itself
    private static final class OurSimpleLayout extends SimpleLayout {
        @Override
        public String format(org.apache.log4j.spi.LoggingEvent event) {
            return (String) event.getMessage() + LINE_SEP;
        }
    }

    private final boolean enabled;
    private final TimeAndSizeRollingAppender appender;
    private final org.apache.log4j.Logger backupLogger;
    
    // added Kafka Logger

    private String kafkaTopicPrefix;
    private String kafkaTopicPostfix;
    private String environment;
    private String clusterShard;
    private String kafkaBufferSize;
    private String kafkaBatchMessages;
    private String kafkaMaxQueueTimeMs;
    private String kafkaMetadataRefresh;
    private String kafkaProducerType;
    private String kafkaCompressionCodec;
    private String kafkaBrokers;
    private String kafkaZookeeper;
    private boolean kafkaLoggerEnabled;
    private EnumSet<AdAction> kafkaForward = EnumSet.of(AdAction.AD_SERVED, AdAction.CLICK, 
    		AdAction.IMPRESSION, AdAction.BID_SERVED, AdAction.RTB_LOST);
    private Producer<String, String> kafkaLogger;

    @Autowired
    private DisplayTypeUtils displayTypeUtils;
    
    @Autowired
    private LocalBudgetManager budgetManager;
    
    @Autowired
    private ReservePot reservePot;
    
    @Autowired
    private CounterManager counterManager;

    @Autowired
    public ArchiveV1BackupLoggerImpl(@Value("${BackupLogger.enabled}") boolean enabled, @Value("${BackupLogger.fileName}") String fileName,
            @Value("${BackupLogger.datePattern:.yyyy-MM-dd-HH}") String datePattern, @Value("${BackupLogger.maxFileSize:100MB}") String maxFileSize,
            @Value("${BackupLogger.maxRollFileCount:2147483647}") int maxRollFileCount, @Value("${BackupLogger.scavengeInterval:-1}") int scavengeInterval,
            @Value("${KafkaLogger.zookeepers}") String zookeepers, @Value("${KafkaLogger.brokers}") String brokers, @Value("${KafkaLogger.topicprefix:adevents}") String topicPrefix,
            @Value("${KafkaLogger.shard}") String shard, @Value("${KafkaLogger.environment}") String environment,@Value("${KafkaLogger.posfix:j3}") String topicPosfix, 
            @Value("${KafkaLogger.enabled:true}") boolean kafkaLoggerEnabled,@Value("${KafkaLogger.unfilledenabled:true}") boolean kafkaUnfilledEnabled,
            @Value("${KafkaLogger.kafkaBufferSize:10485760}") String kafkaBufferSize, @Value("${KafkaLogger.kafkaBatchMessages:1000}") String kafkaBatchMessages,
            @Value("${KafkaLogger.kafkaMaxQueueTimeMs:1000}") String kafkaMaxQueueTimeMs, @Value("${KafkaLogger.metadata.refresh.time.ms:60000}") String kafkaMetadataRefresh,
            @Value("${KafkaLogger.compression.codec:1}") String kafkaCompressionCodec, @Value("${KafkaLogger.producer.type:async}") String kafkaProducerType)
            throws java.io.IOException {
        this.enabled = enabled;

        if (!enabled) {
            LOG.warning("Backup logging is DISABLED");
            appender = null;
            backupLogger = null;
            return;
        }

        LOG.info("Backup logging to: " + fileName + datePattern);

        appender = new TimeAndSizeRollingAppender(new OurSimpleLayout(), fileName, true);
        appender.setDatePattern(datePattern);
        appender.setMaxFileSize(maxFileSize);
        appender.setMaxRollFileCount(maxRollFileCount);
        appender.setScavengeInterval(scavengeInterval);
        appender.activateOptions();

        backupLogger = org.apache.log4j.Logger.getLogger("BackupLogger");
        // Usually this has no appenders to start with, but just in case...
        backupLogger.removeAllAppenders();
        // Add our custom appender
        backupLogger.addAppender(appender);
        // Disable additivity so our logging doesn't cascade up to the
        // root logger and end up in catalina.out and what not.
        backupLogger.setAdditivity(false);
        // We're only going to be using INFO
        backupLogger.setLevel(org.apache.log4j.Level.INFO);
        
        this.kafkaZookeeper = zookeepers;
        this.kafkaBrokers = brokers;
        this.kafkaBatchMessages = kafkaBatchMessages;
        this.kafkaMaxQueueTimeMs = kafkaMaxQueueTimeMs;
        this.kafkaBufferSize = kafkaBufferSize;
        this.kafkaMetadataRefresh = kafkaMetadataRefresh;
        this.kafkaCompressionCodec = kafkaCompressionCodec;
        this.kafkaProducerType = kafkaProducerType;
        this.kafkaTopicPrefix = topicPrefix;
        this.clusterShard = shard;
        this.environment = environment;
        this.kafkaTopicPostfix = topicPosfix;
        this.kafkaLoggerEnabled = kafkaLoggerEnabled;
        this.serverName = InetAddress.getLocalHost().getHostName();
        String[] serverParts = serverName.split("\\.");
        this.shard = serverParts.length > 1 ? serverParts[1] : null;
        
        if(kafkaUnfilledEnabled){
            kafkaForward.add(AdAction.RTB_FAILED);
            kafkaForward.add(AdAction.UNFILLED_REQUEST);
        }

        if(kafkaLoggerEnabled){
            recreateKafkaLogger();
        }
    }

    private void recreateKafkaLogger() {
    	if (kafkaLogger != null) {
    		try {
    			kafkaLogger.close();
    		} catch (Throwable t) {
    			LOG.warning("Unable to close kafka logger: " + t.getMessage());
    		}
    	}
    	
    	Properties props = new Properties();
		 
    	props.put("zk.connect", kafkaZookeeper);
        props.put("metadata.broker.list", kafkaBrokers);
        props.put("compression.codec", kafkaCompressionCodec);
        props.put("producer.type",kafkaProducerType);
        props.put("queue.buffering.max.ms", kafkaMaxQueueTimeMs);
        props.put("queue.buffering.max.messages", Integer.toString(Integer.parseInt(kafkaBatchMessages) * 10));
        props.put("batch.num.messages",kafkaBatchMessages);
        props.put("send.buffer.bytes","" + kafkaBufferSize);
        props.put("topic.metadata.refresh.interval.ms", kafkaMetadataRefresh);
        props.put("client.id", serverName);
        
        props.put("serializer.class", "kafka.serializer.StringEncoder");
        props.put("request.required.acks","-1");
		 
		ProducerConfig config = new ProducerConfig(props);
    	kafkaLogger = new Producer<String, String>(config);
	}

	@PreDestroy
    public void destroy() {
        if (appender != null) {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("Flushing and closing the appender");
            }
            appender.close();
        }
    }

    @Override
    public void startFilterRequest() {
        TL_QUEUE.set(new ArrayList<AdEvent>());
        TL_FILTER_REQUEST_START_TIME.set(System.currentTimeMillis());
    }

    @Override
    public void startControllerRequest() {
        if (TL_QUEUE.get() == null) {
            TL_QUEUE.set(new ArrayList<AdEvent>());
        }
        TL_CONTROLLER_REQUEST_START_TIME.set(System.currentTimeMillis());
    }

    /**
     * Log tab-separated values to a single line in the backup log file.
     */
    void log(AdEvent ae) {
        List<AdEvent> queue = TL_QUEUE.get();
        if (queue != null) {
            // BackupLoggerFilter has been used, and we're timing the request.
            // So don't actually write anything to the log file yet. Instead
            // we're just going to "queue up" the write for later, which will
            // be taken care of by the filter calling endRequest().
            queue.add(ae);
        } else {
            // Looks like the filter wasn't used, so just immediately write
            // the values to the log file
            writeToLogFile(ae);
        }
    }

    @Override
    public void endFilterRequest() {
        // See if any values were "queued up" to be logged. If none of the
        // log* methods were invoked, then we don't have to bother logging.
        try {
            List<AdEvent> queue = TL_QUEUE.get();
            if (CollectionUtils.isEmpty(queue)) {
                if (LOG.isLoggable(Level.FINER)) {
                    LOG.finer("Queue is empty, not logging anything");
                }
                return;
            }

            if (LOG.isLoggable(Level.FINER)) {
                LOG.finer("Queue size: " + queue.size());
            }

            // Update the elapsed request time in the values list
            long currentTime = System.currentTimeMillis();
            long elapsedTime = currentTime - TL_FILTER_REQUEST_START_TIME.get();
            if (LOG.isLoggable(Level.FINER)) {
                LOG.finer("Request took: " + elapsedTime + "ms");
            }

            long controllerTime = 0;
            if (TL_CONTROLLER_REQUEST_START_TIME.get() != null) {
                controllerTime = currentTime - TL_CONTROLLER_REQUEST_START_TIME.get();
            }

            for (AdEvent ae : queue) {
                ae.setResponseController(controllerTime);
                ae.setResponseOverall(elapsedTime);
                writeToLogFile(ae);
            }
        } finally {
            // Clear our ThreadLocal references (avoid leaks and carryover)
            TL_QUEUE.remove();
            TL_FILTER_REQUEST_START_TIME.remove();
            TL_CONTROLLER_REQUEST_START_TIME.remove();
        }
    }

    void writeToLogFile(AdEvent ae) {
        StringBuilder sb = new StringBuilder();
        writer.write(ae, sb);
        String json = sb.toString();
        if (ae.getAdAction() != AdAction.NO_PUBLICATION) {
        	backupLogger.info(json);
        }
		if(kafkaLoggerEnabled){
		    sendToKafka(ae, json);
		}
    }

    private void sendToKafka(AdEvent ae, String json) {
    	if (kafkaForward.contains(ae.getAdAction())) {
    		try {
    			String topic = kafkaTopicPrefix + "." + ae.getAdAction().getShortName() + "_" + clusterShard + "_" + environment + "_" + kafkaTopicPostfix;
    			kafkaLogger.send(new KeyedMessage<String, String>(topic, json));
    		}catch(KafkaException ke) {
    			LOG.warning("Unable to send message to kafka: " + json);
    			recreateKafkaLogger();
    		}
    	}
	}

	@Override
    public void logBidServed(Impression impression, Date eventTime, TargetingContext context, ByydRequest req) {
        if (!enabled) {
            return;
        }

        AdEvent ae = mapper.map(impression);
        addCommonValues(ae, AdAction.BID_SERVED, eventTime, context, impression);
        ae.setRtbBidPrice(impression.getRtbBidPrice());
        addDisplayTypeId(ae, impression, context);
        addBidRequest(ae, req);

        log(ae);
    }

    private void addBidRequest(AdEvent ae, ByydRequest req) {
        if (req != null) {
            ByydUser user = req.getUser();
            if (user != null) {
                ae.setGender(Gender.from(user.getGender()));
                if (user.getAge() != null) {
                    ae.setAgeFrom(user.getAge());
                }

                if (user.getAgeRange() != null) {
                    ae.setAgeFrom(user.getAgeRange().getStart());
                    ae.setAgeTo(user.getAgeRange().getEnd());
                }

                if (user.getDateOfBirth() != null) {
                    try {
                        ae.setDateOfBirth(new SimpleDateFormat("YYYY").parse(user.getDateOfBirth()));
                    } catch (Exception t) {
                        try {
                            ae.setDateOfBirth(new SimpleDateFormat("YYYYMMdd").parse(user.getDateOfBirth()));
                        } catch (Exception t2) {
                            try {
                                ae.setDateOfBirth(new SimpleDateFormat("YYYYMM").parse(user.getDateOfBirth()));
                            } catch (Exception t3) {
                            }
                        }
                    }
                }

                if (user.getPostalCode() != null) {
                    ae.setPostalCode(user.getPostalCode());
                }
            }
            ByydDevice device = req.getDevice();
            if (device != null) {
                if (device.getCoordinates() != null) {
                    ae.setLongitude(device.getCoordinates().getLongitude());
                    ae.setLatitude(device.getCoordinates().getLatitude());
                }
            }
        }

    }

    @Override
    public void logAdServed(Impression impression, Date eventTime, TargetingContext context) {
        if (!enabled) {
            return;
        }

        AdEvent ae = mapper.map(impression);
        addCommonValues(ae, AdAction.AD_SERVED, eventTime, context, impression);
        ae.setRtbBidPrice(impression.getRtbBidPrice());
        addDisplayTypeId(ae, impression, context);
        addAccounting(ae, impression, context);

        if (budgetManager != null) {
        	// disabled BudgetManagement on NonRTB
//        	CreativeDto creative = context.getAdserverDomainCache().getCreativeById(impression.getCreativeId());
//        	if (creative != null) {
//        		if (creative.getCampaign().isBudgetManagerEnabled() &&
//        			(creative.getCampaign().getCurrentBid().getBudgetType() == BudgetType.IMPRESSIONS || 
//        			creative.getCampaign().getCurrentBid().getBudgetType() == BudgetType.MONETARY)) {
//        			BigDecimal totalCostCPM = ae.getAccountingCost().multiply(THOUSAND);
//	        		if (!budgetManager.verifyAndReserveBudget(impression.getExternalID(),  creative.getCampaign(), totalCostCPM, true)) {
//	        			counterManager.incrementCounter("BM.NonRTB.Overspend." + creative.getCampaign().getId());
//	        		}
//					budgetManager.acquireBudget(impression.getExternalID(), totalCostCPM);
//        		}
//        	} else {
//        		LOG.warning("Invalid creative served: " + impression + "/" + impression.getCreativeId());
//        	}
        } else {
        	LOG.warning("No Budgetmanager wired! Ad Served");
        }

        log(ae);
    }
    
    @Override
    public void logImpression(Impression impression, Date eventTime, TargetingContext context) {
        if (!enabled) {
            return;
        }

        AdEvent ae = mapper.map(impression);
        addCommonValues(ae, AdAction.IMPRESSION, eventTime, context, impression);
        ae.setRtbBidPrice(impression.getRtbBidPrice());
        addDisplayTypeId(ae, impression, context);
        addAccounting(ae, impression, context);

        log(ae);
    }

    public void addAccounting(AdEvent ae, Impression impression, TargetingContext context) {
        try {
            AdserverDomainCache dc = context.getAdserverDomainCache();
            long creativeId = impression.getCreativeId();
            CreativeDto creative = dc.getCreativeById(creativeId);

            AdSpaceDto adSpace = getAdSpaceFromContextOrCache(impression, context);

            PublicationDto publication = adSpace.getPublication();
            PublisherDto publisher = publication.getPublisher();

            CampaignDto campaign = creative.getCampaign();

            com.adfonic.adserver.AdEvent aeOrig = new com.adfonic.adserver.AdEvent();
            aeOrig.setEventTime(ae.getEventTime(), null);
            aeOrig.setRtbBidPrice(ae.getRtbBidPrice());
            aeOrig.setRtbSettlementPrice(ae.getRtbSettlementPrice());
            aeOrig.setAdAction(translate(ae.getAdAction()));

            CompanyDto company = campaign.getAdvertiser().getCompany();
            CampaignBidDto bidForTheAdAtAdServeTime = campaign.getCurrentBid();
            BigDecimal bidAmount = new BigDecimal(bidForTheAdAtAdServeTime.getAmount());

            boolean isSaaS = company.isSaaS();
            boolean isMarginShare = company.getMarginShareDSP() != 1.0;
            boolean isRtb = publisher.isRtbEnabled();
            BidType bidType = bidForTheAdAtAdServeTime.getBidType();

            FinancialCalc financialCalc = chooseCase(isSaaS, isMarginShare, isRtb, bidType);

            Output o = new Output();
            if (financialCalc != null) {
                Input i = prepareInput(ae, publisher, campaign, company, bidAmount);

                o = financialCalc.calculate(i);
            }
            
            ae.setIoReference(campaign.getReference());
            ae.setCampaignCurrentDataFeeId(campaign.getDataFeeId());
            ae.setAccountingPayout(o.getPayout());
            ae.setAccountingBuyerPremium(o.getBuyer_premium());
            ae.setAccountingDataRetail(o.getData_fee());
            ae.setAccountingThirdPartyAdServing(o.getThird_pas_fee());
            ae.setAccountingDspMargin(o.getDsp_margin());
            ae.setAccountingCustMargin(o.getCust_margin());
            ae.setAccountingCampaignDiscount(o.getCampaign_discount());
            ae.setAccountingCost(o.getAccountingCost());

            if (campaign.getBidDeductions() != null && campaign.getBidDeductions().size() > 0) {
            	Map<String, BigDecimal> m = new HashMap<>();
            	for (BidDeductionDto bd : campaign.getBidDeductions()) {
            		if (bd.isPayerIsByyd() && bd.getAmount() != null) {
            			if (bd.getThirdPartyVendorFreeText() != null) {
            				m.put(bd.getThirdPartyVendorFreeText(), bd.getAmount());
            			} else if (bd.getThirdPartyVendorId() != null) {
            				m.put(bd.getThirdPartyVendorId().toString(), bd.getAmount());
            			}
            		}
            	}
            	if (m.size() > 0 ) {
            		ae.setAccountingBidDeductionsByyd(m);
            	}
            	
            	m = new HashMap<>();
            	for (BidDeductionDto bd : campaign.getBidDeductions()) {
            		if (!bd.isPayerIsByyd() && bd.getAmount() != null) {
            			if (bd.getThirdPartyVendorFreeText() != null) {
            				m.put(bd.getThirdPartyVendorFreeText(), bd.getAmount());
            			} else if (bd.getThirdPartyVendorId() != null) {
            				m.put(bd.getThirdPartyVendorId().toString(), bd.getAmount());
            			}
            		}
            	}
            	if (m.size() > 0 ) {
            		ae.setAccountingBidDeductionsCustomer(m);
            	}
            }
            // TODO ???
            //          ae.setAccountingDataWholesale(adAcc.getDataWholesale());

            // in case of a licensed customer
            if (isSaaS) {
                ae.setAccountingDirectCost(o.getDirect_cost());
                ae.setAccountingDirectCostRaw(new BigDecimal(company.getDirectCostOrZero()/bidType.getQuantity()));
                ae.setAccountingTechFee(o.getTech_fee());
            }

            ae.clearAccountingNumberZeros();
            
            //mad-3305 detailed log
            if(ae.getAdAction() == net.byyd.archive.model.v1.AdAction.CLICK) {
            	if(ae.getAccountingDspMargin()==null || ae.getAccountingCost()==null) {
            		
            		String exId = impression.getExternalID();
            		Input i = prepareInput(ae, publisher, campaign, company, bidAmount);
            		LOG.info("missing accounting data " +
            				" exId " + exId +
            				" isSaaS " + isSaaS +
            				" isMarginShare " + isMarginShare +
            				" isRtb " + isRtb +
            				" bidType " + bidType +
            				" financialCalc " + ((financialCalc==null) ? "null" : financialCalc.getClass().getSimpleName()) +
            				" input " + i
            				);
            		
            	}
            	
            }
        } catch (RuntimeException re) {
        	String exId = impression.getExternalID();
            LOG.warning("Error on financials: impression:" + exId + " " + re + "/" + re.getStackTrace()[0]);
        }
    }

	private Input prepareInput(AdEvent ae, PublisherDto publisher, CampaignDto campaign, CompanyDto company,
			BigDecimal bidAmount) {
		Input i = new Input();
		i.adAction = translate(ae.getAdAction());
		i.settlementPrice = doubleValueOrNull(ae.getRtbSettlementPrice());
		i.publisherRevShare = publisher.getCurrentRevShare();
		i.buyerPremium = publisher.getBuyerPremium();
		i.directCost = company.getDirectCostOrZero();
		i.mediaCostMarkup = company.getMediaCostMargin();
		i.marginShareDSP = company.getMarginShareDSP();
		i.dataFee = campaign.getDataFee();
		i.richMediaFee = campaign.getRmAdServingFee();
		i.campaignDiscount = campaign.getAgencyDiscount();
		i.bidAmout = doubleValueOrNull(bidAmount);
		return i;
	}

    AdSpaceDto getAdSpaceFromContextOrCache(Impression impression, TargetingContext context) {
        AdSpaceDto adSpace = context.getAdSpace();
        if (adSpace == null) {
            long adSpaceId = impression.getAdSpaceId();
            AdserverDomainCache adc = context.getAdserverDomainCache();
            adSpace = adc.getAdSpaceById(adSpaceId);
        }
        return adSpace;
    }

    private Double doubleValueOrNull(BigDecimal value) {
        if (value == null) {
            return null;
        }
        return value.doubleValue();
    }

    FinancialCalc chooseCase(boolean isSaaS, boolean isMarginShare, boolean isRtb, BidType bidType) {
        if (isSaaS) {
            return new LicenseCPM_RTB();
        }

        switch (bidType) {
        case CPC:
            if (isMarginShare) {
                if (isRtb) {
                    return new MarginShareCPC_RTB();
                } else {
                    return new MarginShareCPC_NONrtb();
                }
            } else {
                if (isRtb) {
                    return new ManagedCPC_RTB();
                } else {
                    return new ManagedCPC_NONrtb();
                }
            }

        case CPM:
            if (isMarginShare) {
                if (isRtb) {
                    return new MarginShareCPM_RTB();
                } else {
                    return new MarginShareCPM_NONrtb();
                }
            } else {
                if (isRtb) {
                    return new ManagedCPM_RTB();
                } else {
                    return new ManagedCPM_NONrtb();
                }
            }
        default:
            break;
        }
        return null;
    }

    private com.adfonic.domain.AdAction translate(AdAction adAction) {
        com.adfonic.domain.AdAction retval = null;

        switch (adAction) {
        case AD_SERVED:
            retval = com.adfonic.domain.AdAction.AD_SERVED;
            break;
        case AD_FAILED:
            retval = com.adfonic.domain.AdAction.BID_FAILED;
            break;
        case CLICK:
            retval = com.adfonic.domain.AdAction.CLICK;
            break;
        case CONVERSION:
            retval = com.adfonic.domain.AdAction.CONVERSION;
            break;
        case IMPRESSION:
            retval = com.adfonic.domain.AdAction.IMPRESSION;
            break;
        case INSTALL:
            retval = com.adfonic.domain.AdAction.INSTALL;
            break;
        case UNFILLED_REQUEST:
            retval = com.adfonic.domain.AdAction.UNFILLED_REQUEST;
            break;
        default:
            break;
        }

        return retval;
    }

    @Override
    public void logUnfilledRequest(UnfilledReason unfilledReason, Date eventTime, TargetingContext context) {
        if (!enabled) {
            return;
        }

        AdEvent ae = new AdEvent();
        addCommonValues(ae, AdAction.UNFILLED_REQUEST, unfilledReason == null ? "unknown" : unfilledReason.name(), eventTime, context, null);
        log(ae);
    }

    @Override
    public void logAdRequestFailure(String reason, TargetingContext context, String... extraValues) {
        if (!enabled) {
            return;
        }
        AdEvent ae = new AdEvent();
        addCommonValues(ae, AdAction.AD_FAILED, reason, context);
        addExtraValues(ae, extraValues);
        log(ae);
    }

    @Override
    public void logRtbBidSuccess(Impression impression, BigDecimal price, Date eventTime, TargetingContext context) {
    }

    @Override
    public void logRtbBidFailure(String reason, TargetingContext context, ByydRequest req, String... extraValues) {
        if (!enabled) {
            return;
        }
        AdEvent ae = new AdEvent();
        AdAction action = AdAction.RTB_FAILED;
        if (extraValues != null && extraValues.length > 0) {
	        if(extraValues[0].startsWith(RtbBidLogicImpl.MESSAGE_NO_PUBLICATION_FOUND)) {
	        	action = AdAction.NO_PUBLICATION;
	        } else if (extraValues[0].contains("message='NO_CREATIVES'") ||
	        		extraValues[0].contains("message='Out of Budget'") ) {
	        	action = AdAction.UNFILLED_REQUEST;
	        }
        }
        
        addCommonValues(ae, action, reason, context);
        addExtraValues(ae, extraValues);
        addBidRequest(ae, req);

        log(ae);
    }

    @Override
    public void logRtbLoss(Impression impression, Date eventTime, TargetingContext context, String... extraValues) {
        if (!enabled) {
            return;
        }
        AdEvent ae = mapper.map(impression);
        
        if (budgetManager != null) {
            CreativeDto creative = context.getAdserverDomainCache().getCreativeById(impression.getCreativeId());
            if (isBudgetManagerEnabled(creative)) {
                budgetManager.releaseBudget(impression.getExternalID());

                BigDecimal maxBidThreshold = creative.getCampaign().getMaxBidThreshold();
                if (maxBidThreshold != null && maxBidThreshold.signum() > 0) {
                    BigDecimal priceBoost = impression.getPriceBoost();
                    LOG.info("rtbLoss deposit campaignId " + creative.getCampaign().getId() + " priceBoost " + priceBoost);
                    reservePot.deposit(creative.getCampaign().getId(), impression.getPriceBoost());
                }
            }
        } else {
            LOG.warning("No Budgetmanager wired! RTB Loss");
        }

        addCommonValues(ae, AdAction.RTB_LOST, eventTime, context, impression);
        addDisplayTypeId(ae, impression, context);
        addExtraValues(ae, extraValues);
        log(ae);
    }

	private boolean isBudgetManagerEnabled(CreativeDto creative) {
		return (creative != null && creative.getCampaign().isBudgetManagerEnabled() && creative.getCampaign().getCurrentBid() != null 
				&& (creative.getCampaign().getCurrentBid().getBidType() == BidType.CPM || //
				        creative.getCampaign().getCurrentBid().getBidType() == BidType.CPC )
				);
	}

    @Override
    public void logRtbWinSuccess(Impression impression, BigDecimal settlementPrice, Date eventTime, TargetingContext context) {
        if (!enabled) {
            return;
        }
        
        if (budgetManager != null) {
        	CreativeDto creative = context.getAdserverDomainCache().getCreativeById(impression.getCreativeId());
        	if (isBudgetManagerEnabled(creative)) {
        		boolean isSaaS = creative.getCampaign().getAdvertiser().getCompany().isSaaS();
        		budgetManager.acquireBudget(impression.getExternalID(), settlementPrice, !isSaaS);
        		
        		
                BigDecimal maxBidThreshold = creative.getCampaign().getMaxBidThreshold();
                if (maxBidThreshold != null && maxBidThreshold.signum() > 0) {
                    BigDecimal rtbBidPrice = impression.getRtbBidPrice();
                    BigDecimal carriedOver = rtbBidPrice.subtract(settlementPrice);
                    
                    LOG.info("winSuccess deposit campaignId " + creative.getCampaign().getId() + " rtbBidPrice " + rtbBidPrice + " settlementPrice " + settlementPrice + " carriedOver " + carriedOver);
                    reservePot.deposit(creative.getCampaign().getId(), carriedOver);
                }
        	}
        } else {
            LOG.warning("No Budgetmanager wired! RTB Win");
        }

        AdEvent ae = mapper.map(impression);
        addCommonValues(ae, AdAction.AD_SERVED, eventTime, context, impression);
        addDisplayTypeId(ae, impression, context);
        ae.setRtbSettlementPrice(settlementPrice);
        addAccounting(ae, impression, context);
        log(ae);
    }

    @Override
    public void logRtbWinFailure(String impressionExternalID, String reason, TargetingContext context, String... extraValues) {
        if (!enabled) {
            return;
        }
        AdEvent ae = new AdEvent();
        addCommonValues(ae, AdAction.RTB_WIN_FAILED, reason, context);
        ae.setImpressionExternalID(impressionExternalID);
        addExtraValues(ae, extraValues);
        log(ae);
    }

    @Override
    public void logBeaconSuccess(Impression impression, Date eventTime, TargetingContext context) {
        if (!enabled) {
            return;
        }
        
        if (budgetManager != null) {
        	AdSpaceDto adspace = context.getAdserverDomainCache().getAdSpaceById(impression.getAdSpaceId());
        	
        	if (adspace != null) {
        		CreativeDto creative = context.getAdserverDomainCache().getCreativeById(impression.getCreativeId());
        		if (isBudgetManagerEnabled(creative)) {
        			boolean isSaaS = creative.getCampaign().getAdvertiser().getCompany().isSaaS();
        			if (adspace.getPublication().getPublisher().getRtbConfig().getWinNoticeMode() == RtbConfig.RtbWinNoticeMode.BEACON) {
        				budgetManager.acquireBudget(impression.getExternalID(), impression.getRtbSettlementPrice(), !isSaaS);
        			}
        		}
        	} else {
        		LOG.warning("Invalid adspace served: " + impression + "/" + impression.getAdSpaceId());
        	}
        } else {
        	LOG.warning("No Budgetmanager wired! RTB Win");
        }

        AdEvent ae = mapper.map(impression);
        addCommonValues(ae, AdAction.IMPRESSION, eventTime, context, impression);
        addDisplayTypeId(ae, impression, context);
        log(ae);
    }

    @Override
    public void logBeaconFailure(String impressionExternalID, String reason, TargetingContext context, String... extraValues) {
        if (!enabled) {
            return;
        }
        AdEvent ae = new AdEvent();
        addCommonValues(ae, AdAction.IMPRESSION_FAILED, reason, context);
        ae.setImpressionExternalID(impressionExternalID);
        addExtraValues(ae, extraValues);
        log(ae);
    }

    @Override
    public void logBeaconFailure(Impression impression, String reason, TargetingContext context, String... extraValues) {
        if (!enabled) {
            return;
        }
        AdEvent ae = mapper.map(impression);
        addCommonValues(ae, AdAction.IMPRESSION_FAILED, reason, context);
        addDisplayTypeId(ae, impression, context);
        addExtraValues(ae, extraValues);
        log(ae);
    }

    @Override
    public void logClickSuccess(Impression impression, AdSpaceDto adspace, Date eventTime, Long campaignId, TargetingContext context) {
        if (!enabled) {
            return;
        }
        
        ClickRegisterState outcome = ClickRegisterState.NORMAL;
        AdAction action = AdAction.CLICK;
        if (budgetManager != null) {
        	CreativeDto creative = context.getAdserverDomainCache().getCreativeById(impression.getCreativeId());
        	if (creative != null) {
        		if (isBudgetManagerEnabled(creative)) {
        			outcome = budgetManager.registerClick(impression.getExternalID(), creative.getCampaign());
        			switch(outcome) {
        			case DUPLICATE:
        			case OVER_BUDGET:
        				action = AdAction.CLICK_FAILED;
        				break;
        			case NORMAL:
        				break;
        			}
        		}
        	} else {
        		LOG.warning("Invalid creative served: " + impression + "/" + impression.getCreativeId());
        	}
        } else {
            LOG.warning("No Budgetmanager wired! Ad Served");
        }
        
        AdEvent ae = mapper.map(impression);
        addCommonValues(ae, action, eventTime, context, impression);
        addPublicationInformation(ae, adspace);
        addDisplayTypeId(ae, impression, context);
        addAccounting(ae, impression, context);
        addExtraValues(ae, new String[]{outcome.toString()});
        //Make sure campaign id is set
        if(campaignId!=null){
            ae.setCampaignId(campaignId);
        }

        log(ae);
    }

    @Override
    public void logClickFailure(String impressionExternalID, String reason, TargetingContext context, String... extraValues) {
        if (!enabled) {
            return;
        }
        AdEvent ae = new AdEvent();
        addCommonValues(ae, AdAction.CLICK_FAILED, reason, context);
        ae.setImpressionExternalID(impressionExternalID);
        addExtraValues(ae, extraValues);
        log(ae);
    }

    @Override
    public void logClickFailure(Impression impression, String reason, TargetingContext context, String... extraValues) {
        if (!enabled) {
            return;
        }
        AdEvent ae = mapper.map(impression);
        addCommonValues(ae, AdAction.CLICK_FAILED, reason, context);
        addDisplayTypeId(ae, impression, context);
        log(ae);
    }

    /**
     * Add values that are common for every request/result
     * 
     * @param impression
     */
    static void addCommonValues(AdEvent ae, AdAction outcome, Date eventTime, TargetingContext context, Impression impression) {
        addCommonValues(ae, outcome, null, eventTime, context, impression);
    }

    /**
     * Add values that are common for every request/result
     */
    static void addCommonValues(AdEvent ae, AdAction outcome, String reason, TargetingContext context) {
        addCommonValues(ae, outcome, reason, new Date(), context, null);
    }

    /**
     * Add values that are common for every request/result
     * 
     * @param impression
     */
    @SuppressWarnings("unchecked")
    static void addCommonValues(AdEvent ae, AdAction outcome, String reason, Date eventTime, TargetingContext context, Impression impression) {
        if (context != null) {
            String requestURI = null;
            // Request URI and client IP per SC-87
            HttpServletRequest httpServletRequest = context.getHttpServletRequest();
            if (httpServletRequest != null) {
                if (httpServletRequest.getQueryString() != null) {
                    requestURI = httpServletRequest.getRequestURI() + "?" + httpServletRequest.getQueryString();
                } else {
                    requestURI = httpServletRequest.getRequestURI();
                }
                String remoteAddr = httpServletRequest.getRemoteAddr();
                ae.setRequestURL(requestURI);
                ae.setRequestHost(remoteAddr);
            }

            ae.setIpAddress((String) context.getAttribute(Parameters.IP));
            ae.setTestMode(context.isFlagTrue(Parameters.TEST_MODE));
            ae.setShard(shard);
            ae.setServerName(serverName);
            ae.setHost(serverName);

            AdSpaceDto adSpace = context.getAdSpace();
            if (adSpace != null) {
                ae.setAdSpaceId(adSpace.getId());
                ae.setAdSpaceExternalId(adSpace.getExternalID());
                PublicationDto publication = adSpace.getPublication();
                if (publication != null) {
                    ae.setPublicationId(publication.getId());
                    ae.setPublicationDomain(publication.getBundleName());
                    if (publication.getPublisher() != null) {
                        ae.setExchangeId(publication.getPublisher().getId());
                    }
                }
            }
            // Value from bid request is prefered over adspace->publication->bundle association (see above)
            // but it is avaliable only for rtb bid events and not for win, impression, click, etc... 
            ByydRequest byydRequest = context.getAttribute(TargetingContext.BYYD_REQUEST);
            if (byydRequest != null) {
                if (byydRequest.getMedium() == Medium.APPLICATION) {
                    ae.setPublicationDomain(byydRequest.getBundleName());
                } else {
                	ae.setApplication(0L);
                    String publicationUrl = byydRequest.getPublicationUrlString();
                    if (publicationUrl != null && publicationUrl.startsWith("http")) {
                        try {
                            ae.setPublicationDomain(new URL(publicationUrl).getHost());
                        } catch (MalformedURLException mux) {
                            // ignore - can't do anything about that
                        }
                    }
                }
            }

            if (ae.getModelId() == null) {
                ModelDto model = context.getAttribute(TargetingContext.MODEL);
                if (model != null) {
                    ae.setModelId(model.getId());
                }
            }

            com.adfonic.domain.Gender gender = context.getAttribute(TargetingContext.GENDER);
            if (gender != null) {
                ae.setGender(gender == com.adfonic.domain.Gender.FEMALE ? Gender.FEMALE : Gender.MALE);
            }

            ae.setUserAgentHeader(context.getEffectiveUserAgent());

            addDeviceIdentifiers(ae, context, impression);

            ae.setClickUrlCookie(context.getCookie(AdserverConstants.CLICK_ID_COOKIE));
            ae.setHeaderReferrer(context.getHeader("Referer"));
            enrichGeoData(ae, context);

            if (ae.getOperatorId() == null) {
                OperatorDto operator = context.getAttribute(TargetingContext.OPERATOR, OperatorDto.class);
                if (operator != null) {
                    ae.setOperatorId(operator.getId());
                }
            }
        }
        ae.setEventTime(eventTime);
        ae.setAdAction(outcome);
        ae.setDetailReason(reason);
        if (impression != null) {
            ae.setImpressionExternalID(impression.getExternalID());
            ae.setCreativeId(impression.getCreativeId());
            ae.setRtbBidPrice(impression.getRtbBidPrice());
            ae.setGeoType(EXPLICIT_GEO_LOCATION.equals(impression.getLocationSource()) ? 1L : 2L);
        }
    }

    private static void addDeviceIdentifiers(AdEvent ae, TargetingContext context, Impression impression) {
        if (impression != null && MapUtils.isNotEmpty(impression.getDeviceIdentifiers())) {
            ae.setDeviceIdentifiers(impression.getDeviceIdentifiers());
        } else {
            Map<Long, String> devIds = (Map<Long, String>) context.getAttribute(TargetingContext.DEVICE_IDENTIFIERS);
            if (MapUtils.isNotEmpty(devIds)) {
                ae.setDeviceIdentifiers(devIds);
            } else {
                ae.setDeviceIdentifiers(null);
            }
        }
    }

    private static void enrichGeoData(AdEvent ae, TargetingContext context) {
        CountryDto country = context.getAttribute(TargetingContext.COUNTRY);
        if (country != null) {
            ae.setCountryId(country.getId());
            ae.setGeoCountry(country.getIsoCode());
            // FIXME: Use 3 Letter ISO Code
        } else {
            DomainCache domainCache = context.getDomainCache();
            if (domainCache != null) {
                Long countryId = ae.getCountryId();
                if (countryId != null) {
                    CountryDto countryDto = domainCache.getCountryById(countryId);
                    ae.setGeoCountry(countryDto.getIsoCode());
                }
            }
        }
        GeotargetDto geotarget = context.getAttribute(TargetingContext.GEOTARGET, GeotargetDto.class);
        if (geotarget != null) {
            ae.setGeotargetId(geotarget.getId());
        }

        USState usstate = context.getAttribute(TargetingContext.US_STATE);
        if (usstate != null) {
            ae.setGeoRegion(usstate.name());
        }

        String postalCode = context.getAttribute(TargetingContext.POSTAL_CODE);
        if (postalCode != null) {
            ae.setGeoPostalCode(postalCode);
        }
    }

    void addDisplayTypeId(AdEvent ae, Impression impression, TargetingContext context) {
        if (impression != null && context != null) {
            CreativeDto creative = context.getAdserverDomainCache().getCreativeById(impression.getCreativeId());
            if (creative != null) {
                ae.setCreativeId(creative.getId());
                ae.setFormatId(creative.getFormatId());
                CampaignDto campaign = creative.getCampaign();
                ae.setAdvertiserId(campaign.getAdvertiser().getId());
                // ae.setAdOpsOwnerId(campaign.getAdvertiser().);

                if (campaign != null) {
                    ae.setCampaignId(campaign.getId());
                    if (campaign.getCurrentBid() != null && campaign.getCurrentBid().getBidType() != null) {
                        ae.setBidType(campaign.getCurrentBid().getBidType().toString());
                    }
                    ae.setAdomain(campaign.getAdvertiserDomain());
                }

                FormatDto format = context.getDomainCache().getFormatById(creative.getFormatId());

                if (format != null) {
                    ae.setFormatId(ae.getFormatId());
                    DisplayTypeDto displayType = displayTypeUtils.getDisplayType(format, context);
                    if (displayType != null) {
                        ae.setDisplayTypeId(displayType.getId());
                    }
                }
            }
        }
    }

    private void addExtraValues(AdEvent ae, String[] extraValues) {
        StringBuilder extra = new StringBuilder();
        for (String s : extraValues) {
            if (extra.length() != 0) {
                extra.append(";");
            }
            extra.append(s);
        }
        ae.setDetailReason(extra.toString());
    }
    
    private void addPublicationInformation(AdEvent ae, AdSpaceDto adSpace){
        if(adSpace!=null){
            if(adSpace.getExternalID()!=null){
                ae.setAdSpaceExternalId(adSpace.getExternalID());
            }
            if(adSpace.getPublication()!=null){
                ae.setPublicationId(adSpace.getPublication().getId());
                ae.setPublicationDomain(adSpace.getPublication().getBundleName());
            }
            if (adSpace.getPublication().getPublisher() != null) {
                ae.setExchangeId(adSpace.getPublication().getPublisher().getId());
            }
        }
    }

}
