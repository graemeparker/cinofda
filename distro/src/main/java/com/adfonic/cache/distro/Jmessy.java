package com.adfonic.cache.distro;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.log4j.Logger;

public class Jmessy {

    private static final transient Logger LOG = Logger.getLogger(Jmessy.class.getName());

    private static final String DOMAIN_SERIALIZER_PROPERTY_FILENAME = "/usr/local/adfonic/config/adfonic-domainserializer.properties";
    private static final String JMS_REST_BASEURL_PROP = "jms.rest.baseUrl";
    private static final String POSTBODY_PARAM = "body";

    private HttpClient client;

    public Jmessy() {
        client = HttpClientBuilder.create().build();
    }

    public static void main(String... args) throws IOException {
        LOG.info("Jesssy called directly from main. You shouldn't be doing this! Use Distro!");
    }

    private int post(String url, String batch) {
        HttpPost post = new HttpPost(url);

        List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
        urlParameters.add(new BasicNameValuePair(POSTBODY_PARAM, batch));
        HttpResponse response = null;
        try {
            post.setEntity(new UrlEncodedFormEntity(urlParameters));
            response = client.execute(post);
        } catch (ClientProtocolException cpe) {
            LOG.error("Problem with posting message to " + url,cpe);
            //have to kill the entire process here
        } catch (IOException ioe) {
            LOG.error("Problem with posting message to " + url,ioe);
            //have to kill the entire process here
        }
        return response.getStatusLine().getStatusCode();
    }

    private String prepareTopicUrl(String topic) {
        Properties domainSerializerProperties = null;
        InputStream propertiesInputStream = null;
        try {
            domainSerializerProperties = new Properties();
            propertiesInputStream = new FileInputStream(DOMAIN_SERIALIZER_PROPERTY_FILENAME);
            domainSerializerProperties.load(propertiesInputStream);
            // get jms baseurl
            String jmsBaseUrl = domainSerializerProperties.getProperty(JMS_REST_BASEURL_PROP);
            if(jmsBaseUrl != null) {
                //make jmsRestUrl
                return jmsBaseUrl + topic  + "?type=topic";
            }
        } catch (IOException ioe) {
            LOG.error("Problem with file " + DOMAIN_SERIALIZER_PROPERTY_FILENAME,ioe);
            //have to kill the entire process here
        } finally {
            if (propertiesInputStream != null) {
                try {
                    LOG.info("Closing the file stream");
                    propertiesInputStream.close();
                } catch (IOException e) {
                    LOG.error("Problem closing the file stream ", e);
                }
            }
        }
        return null;
    }

    protected void postToTopic(String topic, String batch) {
        LOG.info("Publishing message "+batch+" to JMS topic: "+topic);
        String jmsRestUrl = prepareTopicUrl(topic);
        if(jmsRestUrl != null) {
            if (batch != null) {
                LOG.info("Posting message to " + jmsRestUrl);
                int responseCode = post(jmsRestUrl, batch);
                LOG.info("Posting message to " + jmsRestUrl + ": "+responseCode);
            }
        } else {
            LOG.error("jms.rest.baseUrl not defined in " + DOMAIN_SERIALIZER_PROPERTY_FILENAME);
            //have to kill the entire process here
        }
    }
}
