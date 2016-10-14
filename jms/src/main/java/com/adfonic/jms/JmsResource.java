package com.adfonic.jms;

import javax.jms.Queue;
import javax.jms.Topic;

import org.apache.activemq.command.ActiveMQQueue;
import org.apache.activemq.command.ActiveMQTopic;

public class JmsResource {

    /**
     * Central JMS cluster
     * 
     * XXX consider custom qualifier annotations
     * http://docs.spring.io/spring/docs/3.0.x/spring-framework-reference/html/beans.html#beans-factorybeans-annotations
     */
    public static final String CENTRAL_JMS_FACTORY = "CentralJmsConnectionFactory";
    public static final String CENTRAL_JMS_TEMPLATE = "CentralJmsTemplate";

    // AdServer (RTB) -> Combined
    public static final Queue RTB_PUBLICATION_PERSISTENCE = new ActiveMQQueue("adfonic.rtb.publication.persistence");
    public static final Queue RTB_APP_BUNDLE_PERSISTENCE = new ActiveMQQueue("adfonic.rtb.bundle.persistence");
    public static final Queue RTB_ADSPACE_ADD_FORMAT = new ActiveMQQueue("adfonic.rtb.adspace.format");

    // AdServer (NON RTB) -> Combined

    public static final Queue ADSPACE_VERIFIED = new ActiveMQQueue("adfonic.adSpace.verified");
    public static final Queue ADSPACE_REACTIVATE = new ActiveMQQueue("adfonic.adSpace.dormant.reactivate");
    public static final Queue CLICK_FORWARD = new ActiveMQQueue("adfonic.click.forward");
    public static final Queue TRACKING_ACTION = new ActiveMQQueue("adfonic.tracking.action");

    // Combined -> AdServer(s) so Topics

    public static final Topic STATUS_CHANGE_TOPIC = new ActiveMQTopic("adfonic.status.change");
    public static final Topic UNSTOP_ADVERTISER_TOPIC = new ActiveMQTopic("adfonic.unStopAdvertiser");
    public static final Topic UNSTOP_CAMPAIGN_TOPIC = new ActiveMQTopic("adfonic.unStopCampaign");

    // Combined -> Adfonic API 

    public static final Topic MONDRIAN_CACHE_FLUSH_TOPIC = new ActiveMQTopic("adfonic.mondrian.cache.flush");

    // DomainSerializer -> Combined

    public static final Queue EXCHANGE_CREATIVE_AUDIT = new ActiveMQQueue("adfonic.publisher.creative.approval");

    // Tasks & Admin & SSO -> Combined 

    public static final Queue EMAIL_OUTBOUND_QUEUE = new ActiveMQQueue("adfonic.email.outbound");

    // DataCollector -> AdServers

    public static final Topic STOP_ADVERTISER_TOPIC = new ActiveMQTopic("adfonic.stopAdvertiser");
    public static final Topic STOP_CAMPAIGN_TOPIC = new ActiveMQTopic("adfonic.stopCampaign");

    // Tasks -> DataCollector
    public static final Topic UA_UPDATED_TOPIC = new ActiveMQTopic("adfonic.userAgent.updated");

    /**
     * AdEvent JMS cluster
     */

    public static final String ADEVENT_JMS_FACTORY = "AdEventJmsConnectionFactory";
    public static final String ADEVENT_JMS_TEMPLATE = "AdEventJmsTemplate";

    // AdServer & Combined -> DataCollector 
    public static final Queue ADEVENT_QUEUE = new ActiveMQQueue("adfonic.adEvent.v3");
    public static final Queue ADEVENT_BATCH_QUEUE = new ActiveMQQueue("adfonic.adEvent.batch");
    public static final Queue ADEVENT_CLICK_QUEUE = new ActiveMQQueue("adfonic.adEvent.click.v2");

    /**
     * Queue for alive checks. Central and AdEvent
     */
    public static final Queue TEST_QUEUE = new ActiveMQQueue("test.check");

}
