package com.adfonic.tasks.combined.tracker;

import kafka.common.KafkaException;
import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import net.byyd.archive.model.v1.AdEvent;
import net.byyd.archive.model.v1.ArchiveV1JsonWriter;
import net.byyd.archive.model.v1.V1DomainModelMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.adfonic.adserver.AdEventFactory;
import com.adfonic.adserver.Click;
import com.adfonic.domain.AdSpace;
import com.adfonic.domain.AdSpace_;
import com.adfonic.domain.Creative;
import com.adfonic.domain.Creative_;
import com.adfonic.tracker.ClickService;
import com.byyd.middleware.creative.service.CreativeManager;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.jpa.FetchStrategyBuilder;
import com.byyd.middleware.publication.service.PublicationManager;

public abstract class AbstractTrackerRetry {

    private final transient Logger LOG = LoggerFactory.getLogger(getClass().getName());

    static final FetchStrategy AD_SPACE_FETCH_STRATEGY = new FetchStrategyBuilder().addInner(AdSpace_.publication).build();

    static final FetchStrategy CREATIVE_FETCH_STRATEGY = new FetchStrategyBuilder().addInner(Creative_.campaign).build();

    @Autowired
    private PublicationManager publicationManager;
    @Autowired
    private CreativeManager creativeManager;
    @Autowired
    private AdEventFactory adEventFactory;
    @Autowired
    private ClickService clickService;
    @Autowired
    private Producer<String,String> kafkaLogger;
    @Autowired
    private String topicPrefix;
    @Autowired
    private String topicPosfix;
    @Autowired
    private boolean kafkaEnabled;
    
    private V1DomainModelMapper mapper = new V1DomainModelMapper();
    private ArchiveV1JsonWriter writer = new ArchiveV1JsonWriter();

    protected final AdSpace getAdSpace(Click click) {
        return publicationManager.getAdSpaceById(click.getAdSpaceId(), AD_SPACE_FETCH_STRATEGY);
    }

    protected final Creative getCreative(Click click) {
        return creativeManager.getCreativeById(click.getCreativeId(), CREATIVE_FETCH_STRATEGY);
    }

    protected final AdEventFactory getAdEventFactory() {
        return adEventFactory;
    }

    protected final ClickService getClickService() {
        return clickService;
    }
    
    protected final AdEvent getJSONAdEvent(com.adfonic.adserver.AdEvent adEvent) {
        return mapper.map(adEvent);
    }
    
    protected final void logAdEvent(AdEvent adEvent) {
        if(kafkaEnabled){
            LOG.debug("Sending an AdEvent to kafka topic, adAction={}", adEvent.getAdAction());
            StringBuilder sb = new StringBuilder();
            writer.write(adEvent, sb);
            String json = sb.toString();
            try {
                String topic = topicPrefix + "." + adEvent.getAdAction().getShortName() + topicPosfix;
                kafkaLogger.send(new KeyedMessage<String, String>(topic, json));
            }catch(KafkaException ke) {
                LOG.error("Unable to send message to kafka: " + json);
            }
        }
    }
}
