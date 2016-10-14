package com.adfonic.adserver;

import java.util.Arrays;
import java.util.List;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

import net.anthavio.aspect.ApiPolicyOverride;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

import com.adfonic.http.ApiClient;
import com.adfonic.jms.JmsResource;
import com.adfonic.jms.StatusChangeMessage;
import com.byyd.celtra.CeltraAnalyticsClient;
import com.byyd.celtra.CeltraAnalyticsRequest;
import com.byyd.celtra.CeltraAnalyticsRequest.Dimension;
import com.byyd.celtra.CeltraAnalyticsRequest.Direction;
import com.byyd.celtra.CeltraAnalyticsRequest.Filter;
import com.byyd.celtra.CeltraAnalyticsRequest.Format;
import com.byyd.celtra.CeltraAnalyticsRequest.Metric;
import com.byyd.celtra.CeltraAnalyticsRequest.Operator;
import com.byyd.celtra.CeltraAnalyticsRequest.SortValue;
import com.byyd.celtra.CeltraAnalyticsRequest.Spec;
import com.byyd.celtra.CeltraAnalyticsResponse;
import com.github.os72.protocjar.Protoc;

/**
 * @author mvanek
 * 
 * Rather than downloading/compiling/installing protobuf compiler
 * 
 * https://github.com/os72/protoc-jar
 * 
 */
@ApiPolicyOverride
public class ProtocMain {

    public static void main(String[] args) {
        try {
            int x = 0 / 1000;
            System.out.println(x);
            //String[] argsx = { "--help" };
            //doOpenX();
            //sendStatusChangeJms();
            //celtra();
            doAdX();
        } catch (Exception x) {
            System.err.println("Bummer!");
            x.printStackTrace();
        }
    }

    /**
     * http://shrd1factual01.qa.adf.local:8080/ping
     * enrichment-demo.adsquare.com
     */
    public static void celtra() {
        ApiClient apiClient = new ApiClient("celtra", "https://hub.celtra.com", 500, 1500, 60, 2, 5, 30);
        CeltraAnalyticsClient client = new CeltraAnalyticsClient(apiClient, "79947265", "4a9865ef7933675edad4db885c42b5ae858d044e");
        List<Metric> metrics = Arrays.asList(Metric.sessions, Metric.creativeViews, Metric.interactions);
        List<Dimension> dimensions = Arrays.asList(Dimension.campaignId, Dimension.campaignName);
        List<Filter> fiters = Arrays.asList(/*new Filter(Dimension.accountId, Operator.in, "1"),*/new Filter(Dimension.accountDate, Operator.gt, "2015-02-01"));
        List<SortValue> sorts = Arrays.asList(new SortValue(Metric.sessions, Direction.desc));
        Spec spec = new Spec(metrics, dimensions, fiters, sorts, 25);
        CeltraAnalyticsRequest request = new CeltraAnalyticsRequest(Format.json, spec);
        CeltraAnalyticsResponse response = client.execute(request);
        System.out.println(response);
        client.close();
    }

    public static void sendStatusChangeJms() {
        ActiveMQConnectionFactory activemqFactory = new ActiveMQConnectionFactory("tcp://localhost:61616");
        JmsTemplate template = new JmsTemplate(activemqFactory);
        MessageCreator creator = new MessageCreator() {

            @Override
            public Message createMessage(Session session) throws JMSException {
                //return session.createObjectMessage(new StopAdvertiserMessage(1, "reason", new Date(), new Date()));
                return session.createObjectMessage(new StatusChangeMessage("Creative", 4621, "PENDING"));
            }
        };
        template.send(JmsResource.STATUS_CHANGE_TOPIC, creator);
        System.out.println("Sent!");
    }

    /**
     * http://docs.openx.com/ad_exchange_adv/index.html#ox_rtb_api.html
     */
    public static void doOpenX() {
        String[] argsx = { "--proto_path=./src/main/resources/OpenX", "--java_out=src/main/java", "./src/main/resources/OpenX/ssrtb.proto_v17.txt" };
        try {
            Protoc.runProtoc(argsx);
        } catch (Exception x) {
            x.printStackTrace();
        }
    }

    /**
     * https://developers.google.com/ad-exchange/rtb/downloads
     */
    public static void doAdX() {
        String[] argsx = { "--proto_path=./src/main/resources/DoubleClickAdX", "--java_out=src/main/java", "./src/main/resources/DoubleClickAdX/realtime-bidding-proto.v77.txt" };
        try {
            Protoc.runProtoc(argsx);
        } catch (Exception x) {
            x.printStackTrace();
        }
    }
}
