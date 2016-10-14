package com.adfonic.tasks.config.combined;

import java.util.Map;

import javax.jms.ConnectionFactory;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.pool.PooledConnectionFactory;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.listener.DefaultMessageListenerContainer;

import com.adfonic.jms.AdSpaceVerifiedMessage;
import com.adfonic.jms.CreativeApprovalMessage;
import com.adfonic.jms.JmsResource;
import com.adfonic.jms.JmsUtils;
import com.adfonic.jms.StatusChangeMessage;
import com.adfonic.tasks.combined.consumers.AdSpaceVerifier;
import com.adfonic.tasks.combined.consumers.ClickForwardHandler;
import com.adfonic.tasks.combined.consumers.DormantAdSpaceReactivator;
import com.adfonic.tasks.combined.consumers.OutboundEmailHandler;
import com.adfonic.tasks.combined.consumers.PublisherCreativeHandler;
import com.adfonic.tasks.combined.consumers.RtbPersistenceHandler;
import com.adfonic.tasks.combined.consumers.TrackingMessageHandler;
import com.adfonic.tasks.xaudit.impl.CreativeAuditStatusTask;
import com.adfonic.tracking.TrackingMessage;

/**
 * Mapping JMS consumers to not have it in textual spring xml file (more type-safe you know...)
 * 
 * They should not be started for single off tasks...
 * 
 * @author mvanek
 *
 */
@Configuration
public class CombinedJmsSpringConfig {

    private final org.slf4j.Logger LOG = LoggerFactory.getLogger(getClass().getName());

    @Autowired
    @Qualifier(JmsResource.CENTRAL_JMS_FACTORY)
    private ConnectionFactory centralConnectionFactory;

    @Autowired
    private RtbPersistenceHandler rtbPersistenceHandler;

    @Autowired
    AdSpaceVerifier adSpaceVerifier;

    @Autowired
    DormantAdSpaceReactivator dormantAdSpaceReactivator;

    @Autowired
    ClickForwardHandler clickForwardHandler;

    @Autowired
    TrackingMessageHandler trackingMessageHandler;

    @Autowired
    CreativeAuditStatusTask creativeAuditStatusTask;

    @Autowired
    PublisherCreativeHandler publisherCreativeHandler;

    @Autowired
    OutboundEmailHandler outboundEmailHandler;

    @Bean(name = JmsResource.ADEVENT_JMS_FACTORY, initMethod = "start", destroyMethod = "stop")
    public PooledConnectionFactory centralActiveMqConnectionFactory(@Value("${adevent.jms.broker.url}") String brokerUrl,
            @Value("${adevent.jms.pool.maxConnections}") Integer maxConnections) {
        ActiveMQConnectionFactory activemqFactory = new ActiveMQConnectionFactory(brokerUrl);
        PooledConnectionFactory pooledFactory = new PooledConnectionFactory(activemqFactory);
        pooledFactory.setMaxConnections(maxConnections);
        return pooledFactory;
    }

    @Bean(name = JmsResource.ADEVENT_JMS_TEMPLATE)
    public JmsTemplate centralJmsTemplate(@Qualifier(JmsResource.ADEVENT_JMS_FACTORY) ConnectionFactory connectionFactory) {
        return new JmsTemplate(connectionFactory);
    }

    @Bean
    public DefaultMessageListenerContainer onOutboundEmail() {
        MessageListener listener = new MessageListener() {
            @Override
            public void onMessage(Message jms) {
                try {
                    outboundEmailHandler.onOutboundEmail((Map<String, Object>) ((ObjectMessage) jms).getObject());
                } catch (Exception x) {
                    LOG.error("JMS recieve failed", x);
                }
            }
        };
        return JmsUtils.buildQueueContainer(centralConnectionFactory, JmsResource.EMAIL_OUTBOUND_QUEUE, 5, listener);
    }

    @Bean
    public DefaultMessageListenerContainer onCreativeApprovalNotification() {
        MessageListener listener = new MessageListener() {
            @Override
            public void onMessage(Message jms) {
                try {
                    publisherCreativeHandler.onCreativeApprovalNotification((CreativeApprovalMessage) ((ObjectMessage) jms).getObject());
                } catch (Exception x) {
                    LOG.error("JMS recieve failed", x);
                }
            }
        };
        return JmsUtils.buildQueueContainer(centralConnectionFactory, JmsResource.EXCHANGE_CREATIVE_AUDIT, listener);
    }

    @Bean
    public DefaultMessageListenerContainer onStatusChange() {
        MessageListener listener = new MessageListener() {
            @Override
            public void onMessage(Message jms) {
                try {
                    creativeAuditStatusTask.onStatusChange((StatusChangeMessage) ((ObjectMessage) jms).getObject());
                } catch (Exception x) {
                    LOG.error("JMS recieve failed", x);
                }
            }
        };
        return JmsUtils.buildTopicContainer(centralConnectionFactory, JmsResource.STATUS_CHANGE_TOPIC, listener);
    }

    @Bean
    public DefaultMessageListenerContainer onClickForwardRequest() {
        MessageListener listener = new MessageListener() {
            @Override
            public void onMessage(Message jms) {
                try {
                    clickForwardHandler.onClickForwardRequest((String) ((ObjectMessage) jms).getObject());
                } catch (Exception x) {
                    LOG.error("JMS recieve failed", x);
                }
            }
        };
        return JmsUtils.buildQueueContainer(centralConnectionFactory, JmsResource.CLICK_FORWARD, listener);
    }

    @Bean
    public DefaultMessageListenerContainer onTrackingActionMessage() {
        MessageListener listener = new MessageListener() {
            @Override
            public void onMessage(Message jms) {
                try {
                    trackingMessageHandler.onTrackingActionMessage((TrackingMessage) ((ObjectMessage) jms).getObject());
                } catch (Exception x) {
                    LOG.error("JMS recieve failed", x);
                }
            }
        };
        return JmsUtils.buildQueueContainer(centralConnectionFactory, JmsResource.TRACKING_ACTION, listener);
    }

    @Bean
    public DefaultMessageListenerContainer reactivateDormantAdSpace() {
        MessageListener listener = new MessageListener() {
            @Override
            public void onMessage(Message jms) {
                try {
                    dormantAdSpaceReactivator.reactivateDormantAdSpace((String) ((ObjectMessage) jms).getObject());
                } catch (Exception x) {
                    LOG.error("JMS recieve failed", x);
                }
            }
        };
        return JmsUtils.buildQueueContainer(centralConnectionFactory, JmsResource.ADSPACE_REACTIVATE, listener);
    }

    @Bean
    public DefaultMessageListenerContainer onAdSpaceVerified() {
        MessageListener listener = new MessageListener() {
            @Override
            public void onMessage(Message jms) {
                try {
                    adSpaceVerifier.onAdSpaceVerified((AdSpaceVerifiedMessage) ((ObjectMessage) jms).getObject());
                } catch (Exception x) {
                    LOG.error("JMS recieve failed", x);
                }
            }
        };
        return JmsUtils.buildQueueContainer(centralConnectionFactory, JmsResource.ADSPACE_VERIFIED, listener);
    }

    @Bean
    public DefaultMessageListenerContainer onRtbAdSpaceAddFormatRequest() {
        MessageListener listener = new MessageListener() {
            @Override
            public void onMessage(Message jms) {
                try {
                    rtbPersistenceHandler.onRtbAdSpaceAddFormatRequest((Map) ((ObjectMessage) jms).getObject());
                } catch (Exception x) {
                    LOG.error("JMS recieve failed", x);
                }
            }
        };
        return JmsUtils.buildQueueContainer(centralConnectionFactory, JmsResource.RTB_ADSPACE_ADD_FORMAT, listener);
    }

    @Bean
    public DefaultMessageListenerContainer onRtbPublicationPersistenceRequest() {
        MessageListener listener = new MessageListener() {
            @Override
            public void onMessage(Message jms) {
                try {
                    rtbPersistenceHandler.onRtbPublicationPersistenceRequest((Map) ((ObjectMessage) jms).getObject());
                } catch (Exception x) {
                    LOG.error("JMS recieve failed", x);
                }
            }
        };
        return JmsUtils.buildQueueContainer(centralConnectionFactory, JmsResource.RTB_PUBLICATION_PERSISTENCE, listener);
    }

    @Bean
    public DefaultMessageListenerContainer onRtbBundlePersistenceRequest() {
        MessageListener listener = new MessageListener() {
            @Override
            public void onMessage(Message jms) {
                try {
                    rtbPersistenceHandler.onRtbBundlePersistenceRequest((Map) ((ObjectMessage) jms).getObject());
                } catch (Exception x) {
                    LOG.error("JMS recieve failed", x);
                }
            }
        };
        return JmsUtils.buildQueueContainer(centralConnectionFactory, JmsResource.RTB_APP_BUNDLE_PERSISTENCE, listener);
    }

    @Bean
    public DefaultMessageListenerContainer onTestCentralMessage(@Qualifier(JmsResource.CENTRAL_JMS_FACTORY) ConnectionFactory connectionFactory) {
        MessageListener listener = new MessageListener() {
            @Override
            public void onMessage(Message jms) {
                try {
                    LOG.debug("Test Central JMS recieved: " + jms);
                } catch (Exception x) {
                    LOG.error("JMS recieve failed", x);
                }
            }
        };
        return JmsUtils.buildQueueContainer(connectionFactory, JmsResource.TEST_QUEUE, listener);
    }

    @Bean
    public DefaultMessageListenerContainer onTestAdEventMessage(@Qualifier(JmsResource.ADEVENT_JMS_FACTORY) ConnectionFactory connectionFactory) {
        MessageListener listener = new MessageListener() {
            @Override
            public void onMessage(Message jms) {
                try {
                    LOG.debug("Test AdEvent JMS recieved: " + jms);
                } catch (Exception x) {
                    LOG.error("JMS recieve failed", x);
                }
            }
        };
        return JmsUtils.buildQueueContainer(connectionFactory, JmsResource.TEST_QUEUE, listener);
    }

}
