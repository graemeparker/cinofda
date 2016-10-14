

package com.adfonic.datacollector.kafka;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import kafka.consumer.ConsumerIterator;
import kafka.consumer.KafkaStream;
import net.byyd.archive.model.v1.AdAction;
import net.byyd.archive.model.v1.AdEvent;

import org.apache.commons.dbutils.DbUtils;

import com.adfonic.datacollector.AdEventDataCollector;
import com.adfonic.datacollector.dao.ClusterDao;
import com.adfonic.domain.cache.DataCollectorDomainCache;
import com.adfonic.domain.cache.DataCollectorDomainCacheManager;
import com.adfonic.domain.cache.dto.datacollector.publication.PublicationDto;
import com.adfonic.util.DateUtils;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class KafkaMessageProcessor extends Thread implements Runnable {
    private KafkaStream<byte[], byte[]> m_stream;
    private int m_threadNumber;
    
    private AdEventDataCollector adEventDataCollector;
    private ClusterDao clusterDao;
    private AdEventConversionUtils adEventConversionUtils;
    private DataCollectorDomainCacheManager dataCollectorDomainCacheManager;
    
    private boolean failedTopic;
    private String topic;
    private Map<Integer, Long> threadsStatus;
    private ObjectMapper objectMapper;
    
    private Map<String, Long> userAgentCache = new HashMap<String, Long>();
	private Connection conn;
	private int batchSize;
	private List<AdEventData> unfilledBatch = new ArrayList<AdEventData>();
        
    private static final transient Logger LOG = Logger.getLogger(KafkaMessageProcessor.class.getName());
 
    public KafkaMessageProcessor(KafkaStream<byte[], byte[]> a_stream, int a_threadNumber, Map<Integer, Long> threadsStatus, boolean failedTopic, String topic, int batchSize, AdEventDataCollector adEventDataCollector, ClusterDao clusterDao, AdEventConversionUtils adEventConversionUtils, DataCollectorDomainCacheManager dataCollectorDomainCacheManager) {
        this.m_threadNumber = a_threadNumber;
        this.m_stream = a_stream;
        this.threadsStatus = threadsStatus;
        this.adEventDataCollector = adEventDataCollector;
        this.clusterDao = clusterDao;
        this.adEventConversionUtils = adEventConversionUtils;
        this.dataCollectorDomainCacheManager = dataCollectorDomainCacheManager;
        this.failedTopic = failedTopic;
        this.topic = topic;
        this.objectMapper = new ObjectMapper();
        objectMapper.enable(DeserializationFeature.READ_ENUMS_USING_TO_STRING);
        this.batchSize = batchSize;
        
    }
 
    public void run() {
        LOG.info("Thread " + m_threadNumber + ": initialized for topic " + topic);
        ConsumerIterator<byte[], byte[]> it = m_stream.iterator();
        while(it.hasNext()){
            if (LOG.isLoggable(Level.FINER)) {
                LOG.fine("Thread " + m_threadNumber + ": message for topic " + topic);
            }
            byte[] objectBytes = it.next().message();
            String message = new String(objectBytes);
         
            try{
                net.byyd.archive.model.v1.AdEvent adEvent = objectMapper.readValue(message, net.byyd.archive.model.v1.AdEvent.class);
                
                //If campaignid is null and creativeid is not, we discard the event
                if(adEvent.getCampaignId()==null && adEvent.getCreativeId()!=null){
                    LOG.warning("Adevent with null campaign id with impressionid " + adEvent.getImpressionExternalID()!=null?adEvent.getImpressionExternalID():"null" + " and creative id " + adEvent.getCreativeId());
                }
                else{
                    //messages from failed topic are sent to different method
                    if(failedTopic){
                        adEventDataCollector.onJSONFailedAdEvent(adEvent,objectBytes);
                    }
                    else{
                    	adEventDataCollector.onJSONAdEvent(adEvent,objectBytes);
                    }
                }
            }catch (JsonParseException e) {
                LOG.warning("Requeueing message due to deserialization failure: " + e.getMessage());
                adEventDataCollector.sendFailedJSONMessage(message);
            } catch (JsonMappingException e) {
                LOG.warning("Requeueing message due to deserialization failure: " + e.getMessage());
                adEventDataCollector.sendFailedJSONMessage(message);
            } catch (IOException e) {
                LOG.warning("Requeueing message due to deserialization failure: " + e.getMessage());
                adEventDataCollector.sendFailedJSONMessage(message);
            }
            
            threadsStatus.put(m_threadNumber, System.currentTimeMillis());
            
        }
    LOG.info("Shutting down Thread: " + m_threadNumber);
    }

    public void shutdown(){
        if (unfilledBatch.size() > 0){
            executeBatch();
        }
    }

	private void pushBatchedUnfilled(AdEvent event, byte[] objectBytes) {
		DataCollectorDomainCache dataCollectorDomainCache = dataCollectorDomainCacheManager.getCache();
        PublicationDto publication = dataCollectorDomainCache.getPublicationById(event.getPublicationId());
        if (publication == null) {
            return;
        }
        
        com.adfonic.adserver.AdEvent ae = adEventConversionUtils.convertJsonAdEvent(event, com.adfonic.domain.AdAction.UNFILLED_REQUEST);
        Long userAgentId = userAgentCache.get(event.getUserAgentHeader());
        if (userAgentId == null) {
        	userAgentId = adEventDataCollector.establishUserAgentId(ae);
        }
		int publisherTimeId = DateUtils.getTimeID(event.getEventTime(), publication.getPublisher().getCompany().getDefaultTimeZone());
		
		unfilledBatch.add(new AdEventData(ae, null, userAgentId, null, publisherTimeId,objectBytes));
        if (unfilledBatch.size() > batchSize) {
           executeBatch();
        }
	}
	
	private void executeBatch(){
	    try {
            clusterDao.insertAdeventsBatch(unfilledBatch);
        } catch (SQLException sq) {
            if (conn != null) {
                DbUtils.closeQuietly(conn, null, null);
                conn = null;
            }
            LOG.warning("Requeueing message due to sql failure: " + sq.getMessage());
            for(AdEventData aed : unfilledBatch){
                String message = new String(aed.getOriginalMessage());
                adEventDataCollector.sendFailedJSONMessage(message);
            }
        }
        unfilledBatch = new ArrayList<AdEventData>();
	}
}
